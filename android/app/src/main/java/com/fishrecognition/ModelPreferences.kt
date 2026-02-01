package com.fishrecognition

import android.content.Context
import android.content.SharedPreferences

/**
 * 模型配置管理类
 * 使用 SharedPreferences 持久化存储用户选择的模型
 */
class ModelPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "fish_recognition_prefs"
        private const val KEY_SELECTED_MODEL = "selected_model"
        
        // 可用的模型列表
        val AVAILABLE_MODELS = listOf(
            ModelInfo(
                fileName = "best_float32.tflite",
                displayName = "自定义训练模型",
                description = "针对特定鱼类优化的专用模型，识别精度更高"
            ),
            ModelInfo(
                fileName = "yolov8n_float32.tflite",
                displayName = "YOLOv8n 通用模型",
                description = "通用目标检测模型，支持更多种类"
            )
        )
        
        // 默认模型
        const val DEFAULT_MODEL = "best_float32.tflite"
    }

    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 获取当前选中的模型文件名
     */
    fun getSelectedModel(): String {
        return prefs.getString(KEY_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    /**
     * 保存选中的模型
     */
    fun setSelectedModel(modelFileName: String) {
        prefs.edit().putString(KEY_SELECTED_MODEL, modelFileName).apply()
    }

    /**
     * 获取当前选中模型的显示信息
     */
    fun getSelectedModelInfo(): ModelInfo {
        val selectedModel = getSelectedModel()
        return AVAILABLE_MODELS.find { it.fileName == selectedModel } 
            ?: AVAILABLE_MODELS[0]
    }
}

/**
 * 模型信息数据类
 */
data class ModelInfo(
    val fileName: String,
    val displayName: String,
    val description: String
)
