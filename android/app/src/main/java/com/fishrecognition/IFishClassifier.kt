package com.fishrecognition

import android.graphics.Bitmap

/**
 * 鱼类分类器接口
 * 统一不同模型类型的调用方式
 */
interface IFishClassifier {
    /**
     * 初始化分类器
     * @return 是否初始化成功
     */
    fun setup(): Boolean
    
    /**
     * 检测图片中的鱼类
     * @param bitmap 待检测的图片
     * @return 检测结果列表
     */
    fun detectFish(bitmap: Bitmap): List<FishDetection>
    
    /**
     * 释放资源
     */
    fun close()
    
    /**
     * 是否初始化完成
     */
    val isSetupComplete: Boolean
}
