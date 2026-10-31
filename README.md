# 知行 Android App

知行计算器 + 知行AI 的全新 Android 原生应用工程。

## 项目方向

- 平台：仅 Android
- 技术：Kotlin + Jetpack Compose
- 视觉：知行 Light Liquid，全浅色设计，借鉴 iOS 26 Liquid Glass 的光学材质与交互特征
- 玻璃：AGSL / RuntimeShader 为核心，先进行独立真机实验
- 迁移：全新 App UI，原有功能逐步迁移，保留可验证的算法与后端能力
- 本地计算：不强制登录
- AI：通过受保护的知行 Worker 接入
- 本地数据：Room；AI 云端历史：Worker + Supabase

## 开发前必读

1. [项目现状、操作指南与后续开发手册](docs/PROJECT_STATUS_AND_GUIDE.md)（新对话接续优先阅读）
2. [实施状态与下一步清单](docs/IMPLEMENTATION_STATUS.md)
3. [APK 首次真机验收清单](docs/APK_TEST_CHECKLIST.md)
4. [Android App 总体规划与迁移蓝图](docs/ANDROID_APP_MASTER_PLAN.md)

开始任何实现前，先检查这两份文件和仓库当前 commit。每次只推进一个小步骤；构建或测试失败时先修复，不继续叠加新功能。

## 当前进度

- [x] 第 1 小步：原生 Compose 工程骨架、浅色首页和四个导航占位入口。
- [x] 第 2 小步：补齐标准 Gradle Wrapper 9.3.1。
- [x] 第 3 小步：系统栏、edge-to-edge、安全区与键盘窗口适配代码已提交（构建和真机验收待确认）。
- [x] GitHub Actions Run #15：导航单元测试与 Debug APK 构建成功，Artifact 已取回。
- [x] 主分支自动保存 APK 已配置。
- [x] 首版图标 WebP 截断问题已在 PR #3 修复并合并到 main。
- [x] Android Build #32：资源校验、单元测试、APK 打包和仓库归档成功。
- [x] 0.1.1 / versionCode 2 已发布到仓库 APK 路径；请安装新包，不要继续使用 0.1.0。
- [x] 设计系统第 1 小项：浅色颜色 Tokens 已抽取，保持原有配色；本轮未创建 APK。
- [ ] 当前门槛：在 Android 真机安装并检查状态栏、底部导航和横竖屏。
- [ ] 后续：继续拆分间距、形状和字体 Tokens；之后建立独立 Liquid Glass 实验室，再逐项迁移计算器与 AI。

> 当前 APK 定位为原生 UI 框架预览版。0.1.1 已修复损坏的图标资源并通过 CI 构建；真机启动仍待用户确认。计算器、AI、化学、历史和云同步功能尚未迁移。

## APK 下载

- [下载知行 Android 0.1.1 Debug APK](https://raw.githubusercontent.com/PaleSquire71029/zhixingapp/main/artifacts/zhixingapp-preview.apk)（已存入仓库，后续构建成功会自动更新）
- [GitHub Actions 构建记录与备用 Artifact](https://github.com/PaleSquire71029/zhixingapp/actions/workflows/android-build.yml)

## 旧项目参考

- [知行计算器及 AI 后端](https://github.com/PaleSquire71029/calculator)
- [知行AI及项目交接文档](https://github.com/PaleSquire71029/zhixing_ai)

旧仓库只作为算法、接口、数据和功能验收参考。新 App 的界面和交互应使用原生 Android 实现，不得把旧网页整体装进 WebView。
