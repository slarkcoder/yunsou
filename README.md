# 云搜

一款基于 [PanSou](https://github.com/fish2018/pansou) API 的 Android 云盘资源搜索应用，支持多网盘搜索、链接有效性检测、搜索结果缓存等功能。

## 功能

- **多网盘搜索** — 支持百度网盘、阿里云盘、夸克网盘、天翼云盘、UC网盘、迅雷、PikPak 等 13 种网盘资源搜索
- **结果去重合并** — 同名资源按网盘类型分组展示，清晰直观
- **链接有效性检测** — 自动并发检测链接有效性，移除失效链接，按画质关键词（蓝光 > 原盘 > 4K > 1080p > 高清）排序
- **搜索历史** — 保留最近 10 条搜索关键词，点击即可快速搜索
- **缓存加速** — 搜索结果本地缓存 7 天，二次搜索秒加载
- **自部署服务器** — 支持自定义 API 服务器地址，可连接自己的 PanSou 实例
- **服务器状态检测** — 自动检测 API 服务器连接状态，结果缓存 1 小时
- **版本更新** — 自动检测 GitHub Release 最新版本，提示下载
- **深色模式** — 默认暗黑主题，支持切换

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| 网络 | Retrofit + OkHttp + Kotlin Serialization |
| DI | Hilt |
| 架构 | ViewModel + StateFlow (MVVM) |
| 最低 SDK | 26 (Android 8.0) |
| 目标 SDK | 35 |

## 构建

1. 克隆项目
```bash
git clone https://github.com/slarkcoder/yunsou.git
```

2. 用 Android Studio 打开，Sync Gradle

3. 构建 APK
```bash
./gradlew assembleRelease
```

APK 输出至 `app/build/outputs/apk/release/yunsou_*.apk`

## API

应用依赖 [PanSou](https://github.com/fish2018/pansou) API 服务。你可以使用公共实例，或自行部署。

默认服务器地址：`https://pan.slarker.me/`

在设置页面可自定义 API 地址（需以 `/` 结尾）。

## 开源协议

MIT License

## 致谢

- [PanSou](https://github.com/fish2018/pansou) — 开源云盘搜索 API
