# 抖增 - 抖音离线缓存增强模块

![icon](https://img.shields.io/badge/Version-1.0.0-blue)
![LSPosed](https://img.shields.io/badge/LSPosed-Required-orange)
![Target](https://img.shields.io/badge/Target-抖音-brightgreen)

## 📱 功能介绍

**抖增** 是一个基于 Xposed/LSPosed 的抖音离线缓存增强模块，可以突破抖音官方对离线缓存数量的限制，让你能够缓存更多视频。
![截图](pic/shot1.jpg "这是截图")

### 主要特性

- ✅ **自定义缓存数量** - 支持设置任意缓存数量（建议不超过10000）
- ✅ **自动适配混淆** - 使用 DexKit 技术，自动适配抖音版本升级

### 适用场景

- 想在高铁/飞机上离线观看抖音视频
- 想缓存更多喜欢的视频
- 抖音官方缓存数量限制不够用

## 📦 安装方法

### 前置条件
- 已安装 [LSPosed](https://github.com/LSPosed/LSPosed) 框架

### 安装步骤

1. **下载模块APK**
   - 从 [GitHub Releases](https://github.com/YOUR_USERNAME/DouZeng/releases) 下载最新版本
   - 或者从 LSPosed 商店直接搜索安装

2. **安装APK**
   ```bash
   adb install douzeng-v1.0.0-release.apk
   ```

3. **启用模块**
   - 打开 LSPosed 管理器
   - 找到"抖增"模块并启用
   - 勾选"抖音"作为作用域

4. **配置缓存数量**
   - 打开"抖增"应用
   - 设置你想要的缓存数量（如1500）
   - 点击保存

5. **重启抖音**
   - 强制停止抖音
   - 重新启动抖音


## ⚠️ 注意事项

1. **模块作用域** - 必须在LSPosed中将作用域设置为抖音
2. **重启生效** - 修改设置后需要重启抖音才能生效
3. **缓存数量** - 建议不超过10000，过大可能导致性能问题

## 🐛 问题反馈

如果遇到问题，请在 [GitHub Issues](https://github.com/YOUR_USERNAME/DouZeng/issues) 中反馈，并提供：

- 抖音版本号
- Android系统版本
- LSPosed版本号
- 相关日志（logcat）

## 📄 许可证

本项目基于 MIT 许可证开源。

## 🙏 致谢

- [LSPosed](https://github.com/LSPosed/LSPosed) - Xposed 框架的现代实现
- [DexKit](https://github.com/LuckyPray/DexKit) - 用于动态查找混淆类和方法
- [Xposed](https://github.com/rovo89/Xposed) - 原始 Xposed 框架
