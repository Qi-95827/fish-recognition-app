package com.fishrecognition

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fishrecognition.databinding.ActivitySettingsBinding

/**
 * 设置页面 Activity
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var modelPreferences: ModelPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        modelPreferences = ModelPreferences(this)

        setupToolbar()
        setupModelSelection()
    }

    /**
     * 设置工具栏
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * 设置模型选择
     */
    private fun setupModelSelection() {
        // 读取当前选中的模型
        val currentModel = modelPreferences.getSelectedModel()
        
        // 根据当前模型设置 RadioButton 状态
        when (currentModel) {
            "best_float32.tflite" -> binding.radioModel1.isChecked = true
            "yolov8n_float32.tflite" -> binding.radioModel2.isChecked = true
            else -> binding.radioModel1.isChecked = true
        }

        // 更新卡片选中状态
        updateCardSelection(currentModel)

        // 模型1点击事件
        binding.modelCard1.setOnClickListener {
            binding.radioModel1.isChecked = true
            onModelSelected("best_float32.tflite")
        }
        binding.radioModel1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onModelSelected("best_float32.tflite")
            }
        }

        // 模型2点击事件
        binding.modelCard2.setOnClickListener {
            binding.radioModel2.isChecked = true
            onModelSelected("yolov8n_float32.tflite")
        }
        binding.radioModel2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onModelSelected("yolov8n_float32.tflite")
            }
        }
    }

    /**
     * 模型选择变更处理
     */
    private fun onModelSelected(modelFileName: String) {
        val previousModel = modelPreferences.getSelectedModel()
        
        if (previousModel != modelFileName) {
            modelPreferences.setSelectedModel(modelFileName)
            updateCardSelection(modelFileName)
            
            // 获取模型显示名称
            val modelInfo = ModelPreferences.AVAILABLE_MODELS.find { it.fileName == modelFileName }
            val displayName = modelInfo?.displayName ?: modelFileName
            
            Toast.makeText(
                this,
                getString(R.string.model_switched, displayName),
                Toast.LENGTH_SHORT
            ).show()
            
            // 标记需要重新加载模型
            setResult(RESULT_OK)
        }
    }

    /**
     * 更新卡片选中样式
     */
    private fun updateCardSelection(selectedModel: String) {
        val isModel1Selected = selectedModel == "best_float32.tflite"
        
        // 更新卡片边框颜色
        binding.modelCard1.strokeColor = if (isModel1Selected) {
            getColor(R.color.md_primary)
        } else {
            getColor(R.color.md_outline_variant)
        }
        binding.modelCard1.strokeWidth = if (isModel1Selected) 2 else 1

        binding.modelCard2.strokeColor = if (!isModel1Selected) {
            getColor(R.color.md_primary)
        } else {
            getColor(R.color.md_outline_variant)
        }
        binding.modelCard2.strokeWidth = if (!isModel1Selected) 2 else 1

        // 同步 RadioButton
        binding.radioModel1.isChecked = isModel1Selected
        binding.radioModel2.isChecked = !isModel1Selected
    }
}
