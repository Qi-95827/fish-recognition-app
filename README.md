# 鱼类识别移动端 App

基于 TensorFlow Lite 的 Android 鱼类识别应用，采用粘土风格设计。

## 功能特性

- **实时相机识别**: 打开相机拍摄鱼类照片进行识别
- **相册图片识别**: 从手机相册选择已有的鱼类图片进行识别
- **多鱼种支持**: 支持20种常见鱼类识别
- **粘土风格UI**: 柔和、可爱的视觉设计
- **快速响应**: 优化的模型，秒级识别速度
- **权限管理**: 智能的相机权限请求和处理

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9.20 | 开发语言 |
| AndroidX Core | 1.12.0 | Android核心库 |
| CameraX | 1.3.1 | 相机功能 |
| TensorFlow Lite | 2.14.0 | AI模型推理 |
| Material Components | 1.11.0 | UI组件 |
| Coroutines | 1.7.3 | 异步处理 |

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
│   │   │   │   ├── drawable/                # 资源文件(粘土风格)
│   │   │   │   ├── values/                  # 值资源
│   │   │   │   └── layout/                  # 布局文件
│   │   │   └── assets/                      # 模型文件
│   │   │       ├── fish_model.tflite       # TensorFlow Lite模型
│   │   │       └── labels.txt               # 鱼类标签
│   │   ├── build.gradle                     # 应用构建配置
│   │   └── proguard-rules.pro               # 混淆规则
│   ├── build.gradle                        # 项目构建配置
│   ├── settings.gradle                     # Gradle设置
│   └── gradle.properties                   # Gradle属性
├── docs/
│   └── models.md                           # 模型选择指南
├── model/
│   └── README.md                           # 模型文件说明
├── BUILD.md                                # 构建说明
├── QUICKSTART.md                           # 快速开始指南
└── README.md                               # 项目说明
```

## 系统要求

- Android 8.0 (API 26) 或更高版本
- 支持相机功能
- 建议 2GB 内存以上

## 快速开始

### 1. 获取模型

使用 YOLOv8 导出 TensorFlow Lite 模型:

```bash
pip install ultralytics
yolo export model=yolov8n.pt format=tflite
cp yolov8n.tflite android/app/src/main/assets/fish_model.tflite
```

### 2. 构建应用

```bash
cd android
./gradlew assembleDebug
```

### 3. 安装应用

```bash
adb install android/app/build/outputs/apk/debug/app-debug.apk
```

详细说明请参考 [BUILD.md](BUILD.md) 和 [QUICKSTART.md](QUICKSTART.md)

## 视觉风格

采用 **粘土风格 (Claymorphism)** 设计:

- 柔和的圆角 (12-36dp)
- 微妙的阴影和深度感
- 鲜艳但不刺眼的配色 (粉橘色/青蓝色系)
- 半透明的毛玻璃效果
- 可爱的图标和插画风格

### 配色方案

```xml
clay_primary:       #FF9A8B  粉橘色
clay_secondary:     #A0E7E5  青蓝色
clay_accent:        #FFB4A2  橙红色
clay_background:    #FDF6F3  米白色
clay_surface:      #FFF9F5  浅米色
clay_text:          #4A4A4A  深灰色
```

## 支持的鱼类

应用支持识别以下20种常见鱼类:

| 英文名 | 中文名 |
|--------|--------|
| Goldfish | 金鱼 |
| Carp | 鲤鱼 |
| Salmon | 三文鱼 |
| Trout | 鳟鱼 |
| Bass | 鲈鱼 |
| Tuna | 金枪鱼 |
| Cod | 鳕鱼 |
| Catfish | 鲶鱼 |
| Pike | 狗鱼 |
| Perch | 鲈鱼 |
| Tilapia | 罗非鱼 |
| Mackerel | 鲭鱼 |
| Snapper | 鲷鱼 |
| Grouper | 石斑鱼 |
| Sardine | 沙丁鱼 |
| Swordfish | 剑鱼 |
| Halibut | 比目鱼 |
| Flounder | 鲆鱼 |
| Anchovy | 鳀鱼 |
| Herring | 鲱鱼 |

## 使用说明

1. **首次启动**: 授予相机权限
2. **拍照识别**: 点击底部的大相机按钮拍照
3. **相册识别**: 点击相册按钮选择已有图片
4. **查看结果**: 识别结果会显示在底部卡片中，包含中英文名称和置信度
5. **再试一次**: 点击结果卡片中的"再试一次"按钮隐藏结果

## 模型推荐

### YOLOv8-Nano (推荐)

**仓库**: [ultralytics/ultralytics](https://github.com/ultralytics/ultralytics)

- 模型体积小 (~6MB)
- 推理速度快 (移动端 ~30ms)
- 支持目标检测（可同时识别多条鱼）
- 活跃的社区支持

## 常见问题

### Q: 识别准确率不高怎么办？

A: 可以考虑:
1. 使用更好的光线条件拍摄
2. 确保鱼类主体清晰可见
3. 尝试不同角度拍摄
4. 使用自定义训练的模型

### Q: 如何添加新的鱼类类别？

A: 需要重新训练模型:
1. 收集新鱼类的图片数据
2. 更新数据集配置
3. 训练新模型
4. 导出为 TFLite 格式
5. 替换现有的模型文件和标签文件

## 下一步计划

- [ ] 添加识别历史记录功能
- [ ] 支持分享识别结果
- [ ] 添加鱼类百科信息
- [ ] 支持多语言界面
- [ ] 优化模型性能
- [ ] 添加识别结果详情页

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License
