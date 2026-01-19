# 手动下载模型指南

由于自动下载遇到网络问题，请按以下步骤手动获取模型文件。

## 方法1: 从 GitHub 下载预编译模型（推荐）

### 步骤 1: 访问 Ultralytics 发布页面

在浏览器中打开以下地址：
```
https://github.com/ultralytics/assets/releases
```

### 步骤 2: 下载 YOLOv8n TFLite 模型

1. 找到最新版本的 "YOLOv8n Float32 TFLite" 下载链接
2. 或者直接访问这个链接（如果可用）：
   ```
   https://github.com/ultralytics/assets/releases/download/v8.4.0/yolov8n_float32.tflite
   ```

### 步骤 3: 重命名并复制

1. 将下载的文件重命名为 `fish_model.tflite`
2. 复制到以下目录：
   ```
   android/app/src/main/assets/fish_model.tflite
   ```

---

## 方法2: 使用 Python 转换（需要 TensorFlow）

### 步骤 1: 安装依赖

```bash
pip install ultralytics tensorflow==2.14.0
```

### 步骤 2: 下载并转换模型

```bash
# 下载 YOLOv8n 模型
python -c "from ultralytics import YOLO; YOLO('yolov8n.pt').download()"

# 转换为 TFLite 格式
python -c "from ultralytics import YOLO; model = YOLO('yolov8n.pt'); model.export(format='tflite')"

# 复制到项目
cp yolov8n_float32.tflite android/app/src/main/assets/fish_model.tflite
```

---

## 方法3: 使用 Hugging Face（备选）

1. 访问 Ultralytics 的 Hugging Face 页面
2. 下载 YOLOv8n TFLite 模型
3. 重命名为 `fish_model.tflite` 并放入 assets 目录

---

## 验证模型

模型文件应该具有以下特征：
- 文件大小: 约 10-20 MB
- 文件扩展名: `.tflite`
- 可以用文本编辑器打开看到二进制内容

## 常见问题

### Q: 下载后应用显示"模型加载失败"

A: 请检查：
1. 文件是否正确放置在 `android/app/src/main/assets/` 目录
2. 文件名是否为 `fish_model.tflite`
3. 文件大小是否正常（应该大于 1MB）
4. 构建时是否包含了 assets 文件夹

### Q: 转换时 TensorFlow 安装失败

A: Windows 路径问题，可以尝试：
1. 使用 WSL (Windows Subsystem for Linux)
2. 在虚拟机中运行 Linux
3. 使用在线服务进行转换

## 完成

模型文件放置正确后，即可在 Android Studio 中运行应用。
