# 鱼类识别模型选择

## 推荐GitHub模型

### 1. YOLOv8-Nano (强烈推荐)

**仓库**: [ultralytics/ultralytics](https://github.com/ultralytics/ultralytics)

**优点**:
- 模型体积小 (~6MB)
- 推理速度快 (移动端 ~30ms)
- 支持目标检测（可同时识别多条鱼）
- 活跃的社区支持
- 官方提供TensorFlow Lite导出

**如何使用**:
```bash
# 安装ultralytics
pip install ultralytics

# 导出为TFLite
yolo export model=yolov8n.pt format=tflite
```

**适用场景**: 实时识别、多目标检测

---

### 2. MobileNetV3 + 自定义鱼类分类器

**仓库**: [tensorflow/models](https://github.com/tensorflow/models)

**优点**:
- 超轻量级 (~2-3MB)
- Google官方支持
- 适合纯分类任务
- TensorFlow Lite原生支持

**使用方式**:
1. 使用预训练的MobileNetV3
2. 在鱼类数据集上微调
3. 导出为.tflite格式

**适用场景**: 单条鱼分类识别

---

### 3. Fish4Knowledge 预训练模型

**数据集来源**: Fish4Knowledge项目

**特点**:
- 23种常见鱼类
- 包含水下环境照片
- 开源数据集

**获取方式**:
- Kaggle搜索 "Fish4Knowledge dataset"
- 或在GitHub搜索相关模型

---

### 4. EfficientNet-Lite

**仓库**: [tensorflow/tpu](https://github.com/tensorflow/tpu)

**优点**:
- 平衡的精度和速度
- 轻量级变体 (EfficientNet-Lite0)
- 专为边缘设备优化

**模型大小**:
- EfficientNet-Lite0: ~4.7MB
- EfficientNet-Lite1: ~5.4MB

---

## 推荐鱼类数据集

### Fish4Knowledge
- 23种鱼类
- ~27,000张图像
- 水下环境真实数据

### Kaggle Fish Dataset
- [Fish Dataset](https://www.kaggle.com/datasets)
- 包含9种常见鱼类
- 每种类别约1000张图像

### 自定义数据集
如需识别特定鱼类，建议自己收集数据：
1. 网络爬虫收集图片
2. 手动标注
3. 使用LabelImg等工具标注

---

## 模型集成流程

### 步骤1: 获取模型
```bash
# 方法1: 使用预训练的YOLOv8n
yolo export model=yolov8n.pt format=tflite

# 方法2: 在鱼类数据集上微调
yolo detect train data=fish_data.yaml model=yolov8n.pt epochs=100
```

### 步骤2: 将模型放入项目
```
fish-recognition-app/
└── android/
    └── app/
        └── src/
            └── main/
                └── assets/
                    ├── fish_model.tflite  # 模型文件
                    └── labels.txt          # 标签文件
```

### 步骤3: 在Android中加载
```kotlin
// 使用TensorFlow Lite API加载模型
val model = FishModel.newInstance(context)
val image = TensorImage.fromBitmap(bitmap)
val outputs = model.process(image)
```

---

## 性能对比

| 模型 | 大小 | 推理时间 | 精度 | 支持多目标 |
|------|------|----------|------|-----------|
| YOLOv8n | 6MB | ~30ms | 高 | ✓ |
| MobileNetV3 | 2.5MB | ~15ms | 中 | ✗ |
| EfficientNet-Lite0 | 4.7MB | ~25ms | 高 | ✗ |

---

## 快速开始

1. 克隆YOLO仓库:
```bash
git clone https://github.com/ultralytics/ultralytics
cd ultralytics
```

2. 安装依赖:
```bash
pip install ultralytics tensorflow
```

3. 导出模型:
```bash
yolo export model=yolov8n.pt format=tflite
```

4. 将生成的.tflite文件复制到Android项目的assets文件夹
