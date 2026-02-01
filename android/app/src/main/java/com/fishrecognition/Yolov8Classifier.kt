package com.fishrecognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * YOLOv8 专用分类器
 * 使用 TensorFlow Lite Interpreter API 直接推理
 */
class Yolov8Classifier(
    private val context: Context,
    private val modelPath: String
) : IFishClassifier {
    companion object {
        private const val TAG = "Yolov8Classifier"
        private const val INPUT_SIZE = 640
        private const val CONFIDENCE_THRESHOLD = 0.5f
        private const val IOU_THRESHOLD = 0.45f
        private const val MAX_RESULTS = 10
    }

    private var interpreter: Interpreter? = null
    private var inputBuffer: ByteBuffer? = null
    private var labels: List<String> = emptyList()
    override var isSetupComplete = false

    /**
     * 初始化解释器
     */
    override fun setup(): Boolean {
        return try {
            val modelBuffer = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelBuffer, options)
            
            // 准备输入缓冲区
            inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
            inputBuffer?.order(ByteOrder.nativeOrder())
            
            // 加载标签
            labels = loadLabels()
            
            // 打印模型信息用于调试
            logModelInfo()
            
            Log.d(TAG, "YOLOv8 interpreter initialized successfully")
            isSetupComplete = true
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up YOLOv8 interpreter", e)
            isSetupComplete = false
            false
        }
    }

    /**
     * 打印模型输入输出信息
     */
    private fun logModelInfo() {
        interpreter?.let { interp ->
            val inputCount = interp.inputTensorCount
            val outputCount = interp.outputTensorCount
            
            Log.d(TAG, "输入张量数量: $inputCount")
            for (i in 0 until inputCount) {
                val inputTensor = interp.getInputTensor(i)
                Log.d(TAG, "输入[$i]: shape=${inputTensor.shape().contentToString()}, type=${inputTensor.dataType()}")
            }
            
            Log.d(TAG, "输出张量数量: $outputCount")
            for (i in 0 until outputCount) {
                val outputTensor = interp.getOutputTensor(i)
                Log.d(TAG, "输出[$i]: shape=${outputTensor.shape().contentToString()}, type=${outputTensor.dataType()}")
            }
        }
    }

    /**
     * 加载标签文件
     */
    private fun loadLabels(): List<String> {
        return try {
            context.assets.open("labels.txt").bufferedReader().useLines { it.toList() }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading labels", e)
            getDefaultLabels()
        }
    }

    private fun getDefaultLabels(): List<String> {
        return listOf(
            "goldfish", "carp", "salmon", "trout", "bass",
            "tuna", "cod", "catfish", "pike", "perch"
        )
    }

    /**
     * 检测图片中的鱼类
     */
    override fun detectFish(bitmap: Bitmap): List<FishDetection> {
        val interp = interpreter ?: return emptyList()
        val buffer = inputBuffer ?: return emptyList()

        return try {
            // 预处理图片
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
            buffer.rewind()
            preprocessImage(resizedBitmap, buffer)

            // 获取输出张量形状
            val outputTensor = interp.getOutputTensor(0)
            val outputShape = outputTensor.shape()
            Log.d(TAG, "输出形状: ${outputShape.contentToString()}")

            // YOLOv8 输出通常是 [1, numClasses+4, numBoxes] 或 [1, numBoxes, numClasses+4]
            val outputArray = when {
                outputShape.size == 3 && outputShape[1] > outputShape[2] -> {
                    // 形状: [1, 84, 8400] -> 转置处理
                    val output = Array(1) { Array(outputShape[1]) { FloatArray(outputShape[2]) } }
                    interp.run(buffer, output)
                    transposeOutput(output[0])
                }
                outputShape.size == 3 -> {
                    // 形状: [1, 8400, 84]
                    val output = Array(1) { Array(outputShape[1]) { FloatArray(outputShape[2]) } }
                    interp.run(buffer, output)
                    output[0]
                }
                else -> {
                    Log.e(TAG, "不支持的输出形状: ${outputShape.contentToString()}")
                    return emptyList()
                }
            }

            // 后处理
            val detections = postProcess(outputArray, bitmap.width, bitmap.height)
            
            // NMS 非极大值抑制
            nms(detections)
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting fish", e)
            emptyList()
        }
    }

    /**
     * 预处理图片到 ByteBuffer
     */
    private fun preprocessImage(bitmap: Bitmap, buffer: ByteBuffer) {
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            // 归一化到 [0, 1]
            buffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f) // R
            buffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)  // G
            buffer.putFloat((pixel and 0xFF) / 255.0f)          // B
        }
    }

    /**
     * 转置输出 [84, 8400] -> [8400, 84]
     */
    private fun transposeOutput(output: Array<FloatArray>): Array<FloatArray> {
        val rows = output.size
        val cols = output[0].size
        val transposed = Array(cols) { FloatArray(rows) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                transposed[j][i] = output[i][j]
            }
        }
        return transposed
    }

    /**
     * 后处理: 解析 YOLO 输出
     */
    private fun postProcess(output: Array<FloatArray>, imgWidth: Int, imgHeight: Int): MutableList<FishDetection> {
        val detections = mutableListOf<FishDetection>()
        val numClasses = output[0].size - 4  // 前4个是 bbox (x, y, w, h)

        for (i in output.indices) {
            val row = output[i]
            
            // 获取最大类别置信度
            var maxClassScore = 0f
            var maxClassIndex = 0
            for (c in 4 until row.size) {
                if (row[c] > maxClassScore) {
                    maxClassScore = row[c]
                    maxClassIndex = c - 4
                }
            }

            if (maxClassScore >= CONFIDENCE_THRESHOLD) {
                // 解析边界框 (中心点格式 -> 角点格式)
                val cx = row[0] / INPUT_SIZE * imgWidth
                val cy = row[1] / INPUT_SIZE * imgHeight
                val w = row[2] / INPUT_SIZE * imgWidth
                val h = row[3] / INPUT_SIZE * imgHeight

                val left = cx - w / 2
                val top = cy - h / 2
                val right = cx + w / 2
                val bottom = cy + h / 2

                val label = if (maxClassIndex < labels.size) labels[maxClassIndex] else "Unknown"
                
                detections.add(
                    FishDetection(
                        className = label,
                        confidence = maxClassScore,
                        boundingBox = RectF(left, top, right, bottom)
                    )
                )
            }
        }

        return detections
    }

    /**
     * 非极大值抑制 (NMS)
     */
    private fun nms(detections: MutableList<FishDetection>): List<FishDetection> {
        if (detections.isEmpty()) return emptyList()

        // 按置信度排序
        detections.sortByDescending { it.confidence }

        val result = mutableListOf<FishDetection>()
        val used = BooleanArray(detections.size)

        for (i in detections.indices) {
            if (used[i]) continue
            
            result.add(detections[i])
            if (result.size >= MAX_RESULTS) break

            for (j in i + 1 until detections.size) {
                if (used[j]) continue
                if (iou(detections[i].boundingBox, detections[j].boundingBox) > IOU_THRESHOLD) {
                    used[j] = true
                }
            }
        }

        return result
    }

    /**
     * 计算 IoU (Intersection over Union)
     */
    private fun iou(a: RectF, b: RectF): Float {
        val intersectLeft = maxOf(a.left, b.left)
        val intersectTop = maxOf(a.top, b.top)
        val intersectRight = minOf(a.right, b.right)
        val intersectBottom = minOf(a.bottom, b.bottom)

        val intersectArea = maxOf(0f, intersectRight - intersectLeft) * maxOf(0f, intersectBottom - intersectTop)
        val aArea = (a.right - a.left) * (a.bottom - a.top)
        val bArea = (b.right - b.left) * (b.bottom - b.top)
        val unionArea = aArea + bArea - intersectArea

        return if (unionArea > 0) intersectArea / unionArea else 0f
    }

    /**
     * 释放资源
     */
    override fun close() {
        interpreter?.close()
        interpreter = null
        inputBuffer = null
    }
}
