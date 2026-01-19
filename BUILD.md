# 构建说明

## 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.2
- Kotlin 1.9.20

## 构建步骤

### 1. 克隆项目

```bash
git clone <repository-url>
cd fish-recognition-app
```

### 2. 准备模型文件

在运行应用之前，需要准备 TensorFlow Lite 模型文件：

#### 方法1: 使用 YOLOv8 预训练模型

```bash
# 安装ultralytics
pip install ultralytics

# 下载并导出模型
yolo export model=yolov8n.pt format=tflite

# 复制模型文件
cp yolov8n.tflite android/app/src/main/assets/fish_model.tflite
```

#### 方法2: 使用自定义训练的模型

1. 准备鱼类数据集
2. 训练模型:
   ```bash
   yolo detect train data=your_data.yaml model=yolov8n.pt epochs=100
   ```
3. 导出为 TFLite 格式:
   ```bash
   yolo export model=runs/detect/train/weights/best.pt format=tflite
   ```
4. 复制到 assets 目录

### 3. 在 Android Studio 中打开

1. 打开 Android Studio
2. 选择 "Open an Existing Project"
3. 选择 `fish-recognition-app/android` 目录

### 4. 同步 Gradle

Android Studio 会自动同步 Gradle。如果没有自动同步:
- 点击 File -> Sync Project with Gradle Files

### 5. 构建项目

#### 调试版本

```bash
cd android
./gradlew assembleDebug
```

或在 Android Studio 中:
- Build -> Build Bundle(s) / APK(s) -> Build APK(s)

#### 发布版本

```bash
cd android
./gradlew assembleRelease
```

## 运行应用

### 在真机上运行

1. 启用开发者选项
2. 启用 USB 调试
3. 通过 USB 连接手机
4. 点击运行按钮 (绿色三角形)

### 在模拟器上运行

1. 创建虚拟设备 (AVD)
   - Tools -> Device Manager -> Create Device
   - 推荐使用 Pixel 4 或更高版本
2. 点击运行按钮

## 依赖管理

### 添加新依赖

在 `android/app/build.gradle` 的 dependencies 块中添加:

```gradle
implementation 'com.example:library:1.0.0'
```

### 更新依赖

```bash
cd android
./gradlew dependencies --refresh-dependencies
```

## 故障排除

### Gradle 同步失败

```bash
cd android
./gradlew clean
./gradlew build
```

### 模型加载失败

确保:
1. 模型文件已放置在 `android/app/src/main/assets/` 目录
2. 文件名为 `fish_model.tflite`
3. 模型格式正确 (TFLite)

### 相机权限问题

确保在 `AndroidManifest.xml` 中声明了相机权限:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

## 签名发布版本

### 生成签名密钥

```bash
keytool -genkey -v -keystore fish-release.keystore -alias fish-key -keyalg RSA -keysize 2048 -validity 10000
```

### 配置签名

在 `android/app/build.gradle` 中添加:

```gradle
android {
    signingConfigs {
        release {
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
            storeFile keystoreProperties['storeFile'] ? file(keystoreProperties['storeFile']) : null
            storePassword keystoreProperties['storePassword']
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## 测试

### 单元测试

```bash
cd android
./gradlew test
```

### 仪器测试

```bash
cd android
./gradlew connectedAndroidTest
```
