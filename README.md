# 知行 Android App

知行计算器 + 知行AI 的全新 Android 原生应用工程。

## 项目方向

- 平台：仅 Android
- 技术：Kotlin + Jetpack Compose
- 视觉：知行 Light Liquid，全浅色设计，借鉴 iOS 26 Liquid Glass 的光学材质与交互特征
- 玻璃：固定使用 Backdrop 1.0.6，直接移植官方 Catalog 组件；目标是让首页与后续主要页面的大部分容器、按钮、输入区、键帽和浮层具有液态玻璃 / 立体层次，并统一复刻触摸、拖动、切换和展开动画
- 迁移：全新 App UI，原有功能逐步迁移，保留可验证的算法与后端能力
- 本地计算：不强制登录
- AI：通过受保护的知行 Worker 接入
- 本地数据：Room；AI 云端历史：Worker + Supabase

## 开发前必读

1. [项目现状、操作指南与后续开发手册](docs/PROJECT_STATUS_AND_GUIDE.md)（新对话接续优先阅读）
2. [实施状态与下一步清单](docs/IMPLEMENTATION_STATUS.md)
3. [APK 首次真机验收清单](docs/APK_TEST_CHECKLIST.md)
4. [Android App 总体规划与迁移蓝图](docs/ANDROID_APP_MASTER_PLAN.md)
5. [液态玻璃参考 APK 直接移植计划](docs/LIQUID_GLASS_REDESIGN_PLAN.md)
6. [计算器核心迁移计划](docs/CALCULATOR_CORE_MIGRATION_PLAN.md)
7. [计算器原生页面设计](docs/CALCULATOR_NATIVE_UI_DESIGN.md)

开始任何实现前，先检查以上文档和仓库当前 commit。计算迁移按 C0–C12 计划逐项推进；当前主线是 C2 CI 验证后进入 C3。液态玻璃与计算内核分开开发，每次只推进一个可构建小步骤，CI 失败先修复，不继续叠加新功能。

## 当前进度（核对至 2026-11-08）

- [x] 原生 Android 工程、Kotlin + Jetpack Compose、浅色知行主题和 GitHub Actions Debug 构建链路。
- [x] 一级导航调整为「计算 / 知行 AI / 工具 / 我的」；「我的」页已接入历史空状态、材质设置入口和应用版本信息。
- [x] 底部导航采用圆润填色图标与浮动液态玻璃 Dock；二级页面已加入淡入、轻微横移和缩放转场。
- [x] Glass Lab 与主要材质实验控件已完成一轮官方 Backdrop 1.0.6 适配；具体光学效果、滑块连续性和性能仍需目标真机验收。
- [x] 计算核心 C0.1 黄金样例已建立，C1 Tokenizer 已通过 CI。
- [x] C2 AST / 递归下降 Parser 已通过 Android Build #350（Run ID `42443214070`）。
- [x] C3 常数表达式求值器与全量算术黄金样例测试已提交；当前等待 CI 验证。
- [ ] 计算器页面 / 数学键盘、方程、不等式、函数、单位、化学、AI 客户端、账号和云历史尚未完成原生迁移。
- [ ] PR #88 的连续滑块 / 开关长按手势与 120Hz 窗口偏好仍按独立任务验收；不能与数学内核 C3 合并。
- [ ] 真机仍需验收导航 Dock 的玻璃表现、二级页面转场、窄屏安全区与整体触控体验。

> **版本说明：** 工程配置当前为 0.1.2 / versionCode 3。仓库中的 APK 归档可能落后于最新源代码；下载前应先核对最新成功的 Android Build 与 APK 版本，不把旧归档误认为最新产物。

## APK 下载

- [下载仓库归档的 Debug APK](https://raw.githubusercontent.com/PaleSquire71029/zhixingapp/main/artifacts/zhixingapp-preview.apk)（该归档可能落后于当前源码；安装前请核对 APK 版本与最新成功构建记录）
- [GitHub Actions 构建记录与备用 Artifact](https://github.com/PaleSquire71029/zhixingapp/actions/workflows/android-build.yml)

## 旧项目参考

- [知行计算器及 AI 后端](https://github.com/PaleSquire71029/calculator)
- [知行AI及项目交接文档](https://github.com/PaleSquire71029/zhixing_ai)

旧仓库只作为算法、接口、数据和功能验收参考。新 App 的界面和交互应使用原生 Android 实现，不得把旧网页整体装进 WebView。
