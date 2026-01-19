# 模型文件目录

将下载的模型文件放在此目录下，然后复制到 `android/app/src/main/assets/` 中。

## 模型文件

### fish_model.tflite
- 类型: TensorFlow Lite模型
- 来源: YOLOv8-Nano 或其他模型导出
- 用途: 鱼类识别推理

### labels.txt
- 类型: 文本文件
- 格式: 每行一个类别标签
- 示例:
  ```
  goldfish
  carp
  salmon
  trout
  ...
  ```

## 获取模型步骤

### 使用YOLOv8
```bash
# 1. 安装ultralytics
pip install ultralytics

# 2. 导出为TensorFlow Lite格式
yolo export model=yolov8n.pt format=tflite

# 3. 生成的文件在 runs/detect/export/
```

### 使用预训练模型
1. 从 [Ultralytics Hub](https://hub.ultralytics.com/) 下载预训练的鱼类检测模型
2. 导出为.tflite格式
3. 放入此目录

## 复制到项目
模型准备完成后，复制到Android assets目录:
```bash
cp fish_model.tflite ../android/app/src/main/assets/
cp labels.txt ../android/app/src/main/assets/
```
