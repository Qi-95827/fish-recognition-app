package com.fishrecognition

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.fishrecognition.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.*

/**
 * 鱼类识别主活动
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var modelPreferences: ModelPreferences
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var fishClassifier: IFishClassifier? = null
    private var currentModelPath: String = ""
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 设置页面返回结果
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // 始终检查模型是否已更改，无论返回方式如何
        reloadClassifierIfNeeded()
    }

    // 鱼类 emoji 映射
    private val fishEmojis = mapOf(
        "Goldfish" to "🐟",
        "Carp" to "🐟",
        "Salmon" to "🐟",
        "Trout" to "🐟",
        "Bass" to "🐟",
        "Tuna" to "🐟",
        "Cod" to "🐟",
        "Catfish" to "🐟",
        "Pike" to "🐟",
        "Perch" to "🐟",
        "Tilapia" to "🐟",
        "Mackerel" to "🐟",
        "Snapper" to "🐠",
        "Grouper" to "🐠",
        "Sardine" to "🐟",
        "Swordfish" to "🐟",
        "Halibut" to "🐟",
        "Flounder" to "🐟",
        "Anchovy" to "🐟",
        "Herring" to "🐟"
    )

    // 相机权限请求
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            showPermissionDeniedDialog()
        }
    }

    // 图片选择
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                processSelectedImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        modelPreferences = ModelPreferences(this)
        setupViews()
        setupClassifier()
        checkCameraPermission()
    }

    /**
     * 初始化视图
     */
    private fun setupViews() {
        // 拍照按钮
        binding.captureButton.setOnClickListener {
            captureImage()
        }

        // 相册按钮
        binding.galleryButton.setOnClickListener {
            openGallery()
        }

        // 再试一次按钮
        binding.tryAgainButton.setOnClickListener {
            hideResultCard()
        }

        // 工具栏菜单点击
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    openSettings()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * 打开设置页面
     */
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        settingsLauncher.launch(intent)
    }

    /**
     * 如果模型已更改则重新加载
     */
    private fun reloadClassifierIfNeeded() {
        val newModelPath = modelPreferences.getSelectedModel()
        if (newModelPath != currentModelPath) {
            android.util.Log.d("MainActivity", "模型已切换: $currentModelPath -> $newModelPath")
            fishClassifier?.close()
            fishClassifier = null
            
            // 获取模型显示名称
            val modelInfo = ModelPreferences.AVAILABLE_MODELS.find { it.fileName == newModelPath }
            val displayName = modelInfo?.displayName ?: newModelPath
            
            // 先显示切换中提示
            Toast.makeText(this, "正在加载: $displayName", Toast.LENGTH_SHORT).show()
            
            setupClassifier()
        }
    }

    /**
     * 隐藏结果卡片
     */
    private fun hideResultCard() {
        binding.resultCard.visibility = View.GONE
    }

    /**
     * 初始化分类器
     */
    private fun setupClassifier() {
        try {
            currentModelPath = modelPreferences.getSelectedModel()
            android.util.Log.d("MainActivity", "正在加载模型: $currentModelPath")
            
            // 根据模型类型选择合适的分类器
            fishClassifier = createClassifier(currentModelPath)
            val success = fishClassifier?.setup() ?: false

            if (!success) {
                // 模型加载失败，可能是格式不兼容
                showModelLoadErrorDialog(currentModelPath)
            } else {
                android.util.Log.d("MainActivity", "模型加载成功: $currentModelPath")
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "模型初始化异常", e)
            Toast.makeText(this, "模型初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
            showModelLoadErrorDialog(currentModelPath)
        }
    }

    /**
     * 根据模型类型创建对应的分类器
     * 注意：当前所有模型都是 YOLOv8 格式，使用 Interpreter API
     */
    private fun createClassifier(modelPath: String): IFishClassifier {
        // 所有模型都使用 YOLOv8 分类器（Interpreter API）
        // best_float32.tflite 和 yolov8n_float32.tflite 都是 YOLOv8 格式
        android.util.Log.d("MainActivity", "使用 YOLOv8 分类器加载: $modelPath")
        return Yolov8Classifier(this, modelPath)
    }

    /**
     * 显示模型加载失败对话框
     */
    private fun showModelLoadErrorDialog(modelPath: String) {
        val modelInfo = ModelPreferences.AVAILABLE_MODELS.find { it.fileName == modelPath }
        val displayName = modelInfo?.displayName ?: modelPath
        
        AlertDialog.Builder(this)
            .setTitle("模型加载失败")
            .setMessage(
                "无法加载模型: $displayName\n\n" +
                "可能的原因:\n" +
                "• 模型文件格式不兼容\n" +
                "• 模型文件已损坏\n\n" +
                "建议切换回默认模型后重试。"
            )
            .setPositiveButton("切换到默认模型") { _, _ ->
                // 切换回默认模型
                modelPreferences.setSelectedModel(ModelPreferences.DEFAULT_MODEL)
                setupClassifier()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 检查相机权限
     */
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * 启动相机
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "相机启动失败", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 拍照并识别
     */
    private fun captureImage() {
        imageCapture?.let { capture ->
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                cacheDir.resolve("temp_image.jpg")
            ).build()

            showLoading()

            capture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        hideLoading()
                        Toast.makeText(
                            this@MainActivity,
                            "拍照失败: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val filePath = output.savedUri?.path ?: return
                        recognizeFish(filePath)
                    }
                }
            )
        }
    }

    /**
     * 打开相册
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    /**
     * 处理选中的图片
     */
    private fun processSelectedImage(uri: Uri) {
        showLoading()

        mainScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val filePath = cacheDir.resolve("gallery_image.jpg").absolutePath
                    inputStream?.use { input ->
                        java.io.FileOutputStream(filePath).use { output ->
                            input.copyTo(output)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        recognizeFish(filePath)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hideLoading()
                        Toast.makeText(this@MainActivity, "图片加载失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * 识别鱼类
     */
    private fun recognizeFish(imagePath: String) {
        mainScope.launch(Dispatchers.IO) {
            try {
                // 加载并预处理图片
                var bitmap = ImageUtils.loadBitmapFromPath(imagePath)
                if (bitmap != null) {
                    // 修正旋转角度
                    bitmap = ImageUtils.fixImageRotation(bitmap, imagePath)

                    // 进行识别
                    val results = fishClassifier?.detectFish(bitmap) ?: emptyList()

                    withContext(Dispatchers.Main) {
                        hideLoading()
                        if (results.isNotEmpty()) {
                            showResult(results[0])
                        } else {
                            showNoFishDetected()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        hideLoading()
                        Toast.makeText(this@MainActivity, "图片加载失败", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    hideLoading()
                    Toast.makeText(this@MainActivity, "识别失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 显示识别结果
     */
    private fun showResult(detection: FishDetection) {
        // 显示结果卡片
        binding.resultCard.visibility = View.VISIBLE

        // 设置鱼类图标
        val fishEmoji = fishEmojis[detection.className] ?: "🐟"
        binding.fishIcon.text = fishEmoji

        // 设置文本信息
        binding.fishNameText.text = detection.getChineseName()
        binding.fishNameEngText.text = detection.className

        // 置信度百分比
        val confidencePercent = (detection.confidence * 100).toInt()
        binding.confidencePercentText.text = "$confidencePercent%"

        // 更新置信度进度条
        binding.confidenceProgress?.progress = confidencePercent
    }

    /**
     * 显示未检测到鱼类
     */
    private fun showNoFishDetected() {
        // 显示结果卡片
        binding.resultCard.visibility = View.VISIBLE

        binding.fishIcon.text = "❓"
        binding.fishNameText.text = "未识别"
        binding.fishNameEngText.text = ""
        binding.confidencePercentText.text = "0%"
        binding.confidenceProgress?.progress = 0
    }

    /**
     * 显示加载中状态
     */
    private fun showLoading() {
        binding.resultCard.visibility = View.GONE
        binding.loadingContainer.visibility = View.VISIBLE
        binding.captureButton.isEnabled = false
        binding.galleryButton.isEnabled = false
    }

    /**
     * 隐藏加载中状态
     */
    private fun hideLoading() {
        binding.loadingContainer.visibility = View.GONE
        binding.captureButton.isEnabled = true
        binding.galleryButton.isEnabled = true
    }

    /**
     * 显示权限拒绝对话框
     */
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.camera_permission_title)
            .setMessage(R.string.camera_permission_message)
            .setPositiveButton(R.string.btn_settings) { _, _ ->
                // 打开应用设置
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                startActivity(intent)
            }
            .setNegativeButton(R.string.btn_deny, null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
        cameraProvider?.unbindAll()
        fishClassifier?.close()
    }
}
