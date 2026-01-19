# 鱼类识别App - 快速开始指南

## 简介

这是一个基于 TensorFlow Lite 的移动端鱼类识别应用，采用粘土风格设计，支持通过相机拍照或从相册选择图片进行鱼类识别。

## 功能特性

- **实时相机识别**: 打开相机，拍摄鱼类照片进行识别
- **相册图片识别**: 从手机相册选择已有的鱼类图片进行识别
- **多鱼种支持**: 支持20种常见鱼类识别
- **粘土风格UI**: 柔和、可爱的视觉设计
- **快速响应**: 优化的模型，秒级识别速度

## 系统要求

- Android 8.0 (API 26) 或更高版本
- 支持相机功能
- 建议 2GB 内存以上

## 快速开始

### 1. 获取模型

在使用前，需要准备 TensorFlow Lite 模型文件:

#### 方法A: 下载预训练模型 (推荐)

```bash
# 安装 ultralytics
pip install ultralytics

# 导出 YOLOv8-Nano 模型为 TFLite 格式
yolo export model=yolov8n.pt format=tflite

# 复制模型文件
cp yolov8n.tflite android/app/src/main/assets/fish_model.tflite
```

#### 方法B: 使用自定义训练模型

如果你有自己的鱼类数据集，可以训练自定义模型:

```bash
# 训练模型
yolo detect train data=your_data.yaml model=yolov8n.pt epochs=100

# 导出为 TFLite
yolo export model=runs/detect/train/weights/best.pt format=tflite

# 复制模型文件
cp best.tflite android/app/src/main/assets/fish_model.tflite
```

### 2. 构建应用

#### 使用 Android Studio

1. 用 Android Studio 打开 `android` 目录
2. 等待 Gradle 同步完成
3. 点击运行按钮 (绿色三角形)

#### 使用命令行

```bash
cd android
./gradlew assembleDebug
```

生成的 APK 位于: `android/app/build/outputs/apk/debug/app-debug.apk`

### 3. 安装应用

#### 通过 ADB 安装

```bash
adb install android/app/build/outputs/apk/debug/app-debug.apk
```

#### 直接传输 APK

将 APK 文件复制到手机并手动安装

### 4. 使用应用

1. 授予相机权限
2. 点击相机按钮拍摄照片，或点击相册按钮选择图片
3. 等待识别结果
4. 查看识别出的鱼类名称和置信度

## 项目结构

```
fish-recognition-app/
├── android/                        # Android项目
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/fishrecognition/
│   │   │   │   ├── MainActivity.kt           # 主活动
│   │   │   │   ├── FishClassifier.kt         # 鱼类分类器
│   │   │   │   ├── ImageUtils.kt            # 图像工具类
│   │   │   │   └── Application.kt            # 应用类
│   │   │   ├── res/
│   │   │   │   ├── drawable/                # 资源文件
│   │   │   │   ├── values/                  # 值资源
│   │   │   │   └── layout/                  # 布局文件
│   │   │   └── assets/                      # 模型文件
│   │   │       ├── fish_model.tflite       # TensorFlow Lite模型
│   │   │       └── labels.txt               # 鱼类标签
│   │   └── build.gradle                     # 应用构建配置
│   └── build.gradle                        # 项目构建配置
├── docs/                                   # 文档
├── model/                                  # 模型文件存储
├── BUILD.md                                # 构建说明
└── README.md                               # 项目说明
```

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9.20 | 开发语言 |
| AndroidX Core | 1.12.0 | Android核心库 |
| CameraX | 1.3.1 | 相机功能 |
| TensorFlow Lite | 2.14.0 | AI模型推理 |
| Material Components | 1.11.0 | UI组件 |
| Coroutines | 1.7.3 | 异步处理 |

## 常见问题

### Q: 识别准确率不高怎么办？

A: 可以考虑:
1. 使用更好的光线条件拍摄
2. 确保鱼类主体清晰可见
3. 尝试不同角度拍摄
4. 使用自定义训练的模型

### Q: 模型文件需要放在哪里？

A: 模型文件 `fish_model.tflite` 应该放在 `android/app/src/main/assets/` 目录下

### Q: 如何添加新的鱼类类别？

A: 需要重新训练模型:
1. 收集新鱼类的图片数据
2. 更新数据集配置
3. 训练新模型
4. 导出为 TFLite 格式
5. 替换现有的模型文件和标签文件

### Q: 应用启动时提示"模型加载失败"？

A: 请检查:
1. `fish_model.tflite` 文件是否存在于 assets 目录
2. 模型文件格式是否正确
3. 查看 Logcat 获取详细错误信息

## 下一步

- [ ] 添加识别历史记录功能
- [ ] 支持分享识别结果
- [ ] 添加鱼类百科信息
- [ ] 支持多语言界面
- [ ] 优化模型性能

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License
