package com.fishrecognition

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.*

/**
 * 鱼类识别主活动
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var fishClassifier: FishClassifier? = null
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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

        setupViews()
        setupClassifier()
        checkCameraPermission()
    }

    /**
     * 初始化视图
     */
    private fun setupViews() {
        // 拍照按钮 - 点击时播放弹跳动画
        binding.captureButton.setOnClickListener {
            playButtonBounceAnimation(it)
            captureImage()
        }

        // 相册按钮 - 点击时播放弹跳动画
        binding.galleryButton.setOnClickListener {
            playButtonBounceAnimation(it)
            openGallery()
        }

        // 再试一次按钮 - 隐藏结果卡片时播放滑出动画
        binding.tryAgainButton.setOnClickListener {
            hideResultCardWithAnimation()
        }
    }

    /**
     * 播放按钮弹跳动画
     */
    private fun playButtonBounceAnimation(view: View) {
        val bounceAnim = AnimationUtils.loadAnimation(this, R.anim.button_bounce)
        view.startAnimation(bounceAnim)
    }

    /**
     * 隐藏结果卡片（带滑出动画）
     */
    private fun hideResultCardWithAnimation() {
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        slideDown.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                binding.resultCard.visibility = View.GONE
            }
        })
        binding.resultCard.startAnimation(slideDown)
    }

    /**
     * 初始化分类器
     */
    private fun setupClassifier() {
        try {
            fishClassifier = FishClassifier(this)
            val success = fishClassifier?.setup() ?: false

            if (!success) {
                showModelMissingDialog()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "模型初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
            showModelMissingDialog()
        }
    }

    /**
     * 显示模型缺失对话框
     */
    private fun showModelMissingDialog() {
        AlertDialog.Builder(this)
            .setTitle("模型文件缺失")
            .setMessage(
                "检测不到 fish_model.tflite 模型文件。\n\n" +
                "请按以下步骤手动下载模型：\n\n" +
                "1. 访问 https://github.com/ultralytics/assets/releases\n" +
                "2. 下载 'YOLOv8n Float32 TFLite' 文件\n" +
                "3. 将文件重命名为 'fish_model.tflite'\n" +
                "4. 复制到 android/app/src/main/assets/ 目录"
            )
            .setPositiveButton("我知道了") { _, _ -> }
            .setNegativeButton("查看文档") { _, _ ->
                // 可以添加跳转到文档页面的逻辑
            }
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

                    override fun onImageSaved(output: ImageCapture.OutputFileOptions.OutputFileResults) {
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
        // 显示结果卡片并播放滑入动画
        binding.resultCard.visibility = View.VISIBLE
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.resultCard.startAnimation(slideUp)

        binding.resultTitle.text = getString(R.string.result_title)
        binding.fishNameText.text = detection.getChineseName()
        binding.fishNameEngText.text = detection.className
        binding.confidenceText.text = "${getString(R.string.confidence)}: ${detection.getConfidencePercent()}"

        // 根据置信度显示不同的背景色
        val bgColor = when {
            detection.confidence >= 0.8f -> getColor(R.color.clay_success)
            detection.confidence >= 0.6f -> getColor(R.color.clay_warning)
            else -> getColor(R.color.clay_error)
        }
        binding.confidenceText.setTextColor(bgColor)
    }

    /**
     * 显示未检测到鱼类
     */
    private fun showNoFishDetected() {
        // 显示未检测到结果卡片并播放滑入动画
        binding.resultCard.visibility = View.VISIBLE
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.resultCard.startAnimation(slideUp)

        binding.resultTitle.text = getString(R.string.no_fish_detected)
        binding.fishNameText.text = "—"
        binding.fishNameEngText.text = ""
        binding.confidenceText.text = ""
    }

    /**
     * 显示加载中状态
     */
    private fun showLoading() {
        binding.resultCard.visibility = View.GONE
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.captureButton.isEnabled = false
        binding.galleryButton.isEnabled = false
    }

    /**
     * 隐藏加载中状态
     */
    private fun hideLoading() {
        binding.loadingProgressBar.visibility = View.GONE
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
