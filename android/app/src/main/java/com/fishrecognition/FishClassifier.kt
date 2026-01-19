package com.fishrecognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * 鱼类识别器 - 使用TensorFlow Lite Task API
 */
class FishClassifier(private val context: Context) {

    companion object {
        private const val TAG = "FishClassifier"
        private const val MODEL_PATH = "best_float32.tflite"
        private const val LABELS_PATH = "labels.txt"
        private const val MAX_RESULTS = 5
        private const val CONFIDENCE_THRESHOLD = 0.5f
        private const val INPUT_SIZE = 640
    }

    private var objectDetector: ObjectDetector? = null
    private val labels: List<String> = loadLabels()
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .build()

    /**
     * 加载标签文件
     */
    private fun loadLabels(): List<String> {
        return try {
            val inputStream = context.assets.open(LABELS_PATH)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.useLines { it.toList() }
        } catch (e: IOException) {
            Log.e(TAG, "Error loading labels", e)
            getDefaultLabels()
        }
    }

    /**
     * 获取默认标签（当标签文件不存在时使用）
     */
    private fun getDefaultLabels(): List<String> {
        return listOf(
            "goldfish", "carp", "salmon", "trout", "bass",
            "tuna", "cod", "catfish", "pike", "perch"
        )
    }

    /**
     * 初始化检测器
     */
    var isSetupComplete = false

    fun setup(): Boolean {
        return try {
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(BaseOptions.builder().setNumThreads(4).build())
                .setScoreThreshold(CONFIDENCE_THRESHOLD)
                .setMaxResults(MAX_RESULTS)
                .build()

            val modelFile = FileUtil.loadMappedFile(context, MODEL_PATH)
            objectDetector = ObjectDetector.createFromFileAndOptions(modelFile, options)

            Log.d(TAG, "Object detector initialized successfully")
            isSetupComplete = true
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up object detector", e)
            isSetupComplete = false
            false
        }
    }

    /**
     * 检测图片中的鱼类
     */
    fun detectFish(bitmap: Bitmap): List<FishDetection> {
        objectDetector ?: return emptyList()

        return try {
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val processedImage = imageProcessor.process(tensorImage)
            val detections = objectDetector!!.detect(processedImage)

            detections.map { detection ->
                FishDetection(
                    className = getLabel(detection),
                    confidence = detection.categories[0].score,
                    boundingBox = detection.boundingBox
                )
            }.filter { it.confidence >= CONFIDENCE_THRESHOLD }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting fish", e)
            emptyList()
        }
    }

    /**
     * 获取标签名称
     */
    private fun getLabel(detection: Detection): String {
        val index = detection.categories[0].index
        return if (index < labels.size) {
            labels[index]
        } else {
            "Unknown"
        }
    }

    /**
     * 释放资源
     */
    fun close() {
        objectDetector?.close()
        objectDetector = null
    }
}

/**
 * 鱼类检测结果数据类
 */
data class FishDetection(
    val className: String,
    val confidence: Float,
    val boundingBox: RectF
) {
    /**
     * 获取置信度百分比
     */
    fun getConfidencePercent(): String {
        return "%.1f%%".format(confidence * 100)
    }

    /**
     * 获取中文名称
     */
    fun getChineseName(): String {
        return fishNameMapping[className.lowercase()] ?: className
    }

    companion object {
        // 鱼类英中映射
        private val fishNameMapping = mapOf(
            "goldfish" to "金鱼",
            "carp" to "鲤鱼",
            "salmon" to "三文鱼",
            "trout" to "鳟鱼",
            "bass" to "鲈鱼",
            "tuna" to "金枪鱼",
            "cod" to "鳕鱼",
            "catfish" to "鲶鱼",
            "pike" to "狗鱼",
            "perch" to "鲈鱼",
            "tilapia" to "罗非鱼",
            "mackerel" to "鲭鱼",
            "snapper" to "鲷鱼",
            "grouper" to "石斑鱼",
            "sardine" to "沙丁鱼"
        )
    }
}
