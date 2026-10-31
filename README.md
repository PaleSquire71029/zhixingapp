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

1. [Android App 总体规划与迁移蓝图](docs/ANDROID_APP_MASTER_PLAN.md)
2. [实施状态与下一步清单](docs/IMPLEMENTATION_STATUS.md)

开始任何实现前，先检查这两份文件和仓库当前 commit。每次只推进一个小步骤；构建或测试失败时先修复，不继续叠加新功能。

## 当前进度

- [x] 第 1 小步：原生 Compose 工程骨架、浅色首页和四个导航占位入口。
- [ ] 第 2 小步：补齐标准 Gradle Wrapper。
- [ ] 第 3 小步：整理系统栏、安全区和键盘适配。
- [ ] 后续：独立 Liquid Glass 实验室，再逐项迁移计算器与 AI。

## 旧项目参考

- [知行计算器及 AI 后端](https://github.com/PaleSquire71029/calculator)
- [知行AI及项目交接文档](https://github.com/PaleSquire71029/zhixing_ai)

旧仓库只作为算法、接口、数据和功能验收参考。新 App 的界面和交互应使用原生 Android 实现，不得把旧网页整体装进 WebView。
