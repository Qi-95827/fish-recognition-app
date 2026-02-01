# 鱼类识别移动端 App

基于 TensorFlow Lite 的 Android 鱼类识别应用，采用 **Google Material Design 3** 设计风格。

## 功能特性

- **实时相机识别**: 打开相机拍摄鱼类照片进行识别
- **相册图片识别**: 从手机相册选择已有的鱼类图片进行识别
- **多鱼种支持**: 支持20种常见鱼类识别
- **Material Design 3**: 现代 Google 风格 UI 设计
- **快速响应**: 优化的模型，秒级识别速度
- **权限管理**: 智能的相机权限请求和处理

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9.20 | 开发语言 |
| AndroidX Core | 1.12.0 | Android核心库 |
| CameraX | 1.3.1 | 相机功能 |
| TensorFlow Lite | 2.14.0 | AI模型推理 |
| Material Components | 1.11.0 | MD3 UI组件 |
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
│   │   │   │   ├── ImageUtils.kt             # 图像工具类
│   │   │   │   └── Application.kt            # 应用类
│   │   │   ├── res/
│   │   │   │   ├── drawable/                 # Material 3 图标资源
│   │   │   │   ├── values/                   # MD3 颜色/尺寸/主题
│   │   │   │   └── layout/                   # 布局文件
│   │   │   └── assets/                       # 模型文件
│   │   │       ├── fish_model.tflite         # TensorFlow Lite模型
│   │   │       └── labels.txt                # 鱼类标签
│   │   ├── build.gradle                      # 应用构建配置
│   │   └── proguard-rules.pro                # 混淆规则
│   ├── build.gradle                          # 项目构建配置
│   ├── settings.gradle                       # Gradle设置
│   └── gradle.properties                     # Gradle属性
├── docs/
│   └── PROJECT_DOCUMENTATION.md              # 详细项目文档
├── model/
│   └── README.md                             # 模型文件说明
├── BUILD.md                                  # 构建说明
├── QUICKSTART.md                             # 快速开始指南
└── README.md                                 # 项目说明
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

采用 **Google Material Design 3 (Material You)** 设计:

- 遵循 Material 3 设计规范
- 简洁现代的界面风格
- 标准化的圆角和间距
- BottomAppBar + FAB 底部导航模式
- 清晰的信息层级

### 配色方案

```xml
md_primary:           #1A73E8  Google Blue (主色调)
md_secondary:         #5F6368  Google Grey (辅助色)
md_surface:           #FFFFFF  白色表面
md_on_surface:        #1F1F1F  深灰文字
md_primary_container: #D2E3FC  浅蓝色容器
md_outline:           #DADCE0  边框色
```

## 支持的鱼类

应用支持识别以下20种常见鱼类:

| 英文名 | 中文名 | 英文名 | 中文名 |
|--------|--------|--------|--------|
| Goldfish | 金鱼 | Salmon | 三文鱼 |
| Carp | 鲤鱼 | Trout | 鳟鱼 |
| Bass | 鲈鱼 | Tuna | 金枪鱼 |
| Cod | 鳕鱼 | Catfish | 鲶鱼 |
| Pike | 狗鱼 | Perch | 鲈鱼 |
| Tilapia | 罗非鱼 | Mackerel | 鲭鱼 |
| Snapper | 鲷鱼 | Grouper | 石斑鱼 |
| Sardine | 沙丁鱼 | Swordfish | 剑鱼 |
| Halibut | 比目鱼 | Flounder | 鲆鱼 |
| Anchovy | 鳀鱼 | Herring | 鲱鱼 |

## 使用说明

1. **首次启动**: 授予相机权限
2. **拍照识别**: 点击底部蓝色 FAB 按钮拍照
3. **相册识别**: 点击左侧相册按钮选择图片
4. **查看结果**: 识别结果显示在底部卡片中
5. **再次识别**: 点击结果卡片中的"再试一次"按钮

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
