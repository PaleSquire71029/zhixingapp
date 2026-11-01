# 知行 Android App

知行计算器 + 知行AI 的全新 Android 原生应用工程。

## 项目方向

- 平台：仅 Android
- 技术：Kotlin + Jetpack Compose
- 视觉：知行 Light Liquid，全浅色设计，借鉴 iOS 26 Liquid Glass 的光学材质与交互特征
- 玻璃：固定使用 Backdrop 1.0.6，直接移植官方 Catalog 液态玻璃组件源码；自研 AGSL 仅保留为实验，不作为主材质引擎
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

开始任何实现前，先检查以上文档和仓库当前 commit。液态玻璃按专项计划第 25 节直接移植官方 Catalog 1.0.6 组件；每次只推进一个可构建小步骤，CI 失败先修复，不继续叠加新功能。

## 当前进度

- [x] 第 1 小步：原生 Compose 工程骨架、浅色首页和四个导航占位入口。
- [x] 第 2 小步：补齐标准 Gradle Wrapper 9.3.1。
- [x] 第 3 小步：系统栏、edge-to-edge、安全区与键盘窗口适配代码已提交（构建和真机验收待确认）。
- [x] GitHub Actions Run #15：导航单元测试与 Debug APK 构建成功，Artifact 已取回。
- [x] 主分支自动保存 APK 已配置。
- [x] 首版图标 WebP 截断问题已在 PR #3 修复并合并到 main。
- [x] Android Build #32：资源校验、单元测试、APK 打包和仓库归档成功。
- [x] 0.1.1 / versionCode 2 已发布到仓库 APK 路径；请安装新包，不要继续使用 0.1.0。
- [x] 设计系统小项：浅色颜色、间距、形状与文字 Tokens 已接入 ZhixingTheme 统一入口；原有外观保持不变。
- [x] Glass Lab 第 1 小步：独立静态材质实验页、首页入口及返回逻辑已提交。
- [x] Glass Lab 第 2 小步：增加独立背景层 10.dp 模糊 / 清晰开关与低版本回退。
- [x] Glass Lab 第 3 小步：用 GraphicsLayer 捕获实验画布局部快照，按玻璃面板位置裁切并支持手动刷新；不是全窗口实时采样。
- [x] Glass Lab 第 4 小步：裁切坐标抽成纯函数，并增加 3 组边界单测；测试尚未执行。
- [x] Glass Lab 第 5 小步：增加默认关闭的前台动态采样，每秒最多刷新一次；离开页面或 App 进入后台时暂停。
- [x] Glass Lab 第 6 小步：API 33+ 增加可调强度的 AGSL 单通道边缘折射；RGB 不分离；API 26–32 回退快照 / 模糊；本轮未创建 APK。
- [x] Glass Lab 第 7 小步：修正折射方向的宽高比偏斜，增加纯 Kotlin 几何镜像与单元测试；JVM / Android 测试待运行。
- [x] Glass Lab 第 8 小步：增加独立方向性边缘高光开关，支持关闭折射时单独预览；本轮未创建 APK，测试待运行。
- [x] Glass Lab 第 9 小步：增加默认关闭的微量 RGB 色散开关与 0–0.8 px 调节；红蓝反向偏移、绿色保持中心采样；本轮未创建 APK。
- [x] Glass Lab 第 10 小步：增加样本面板的指尖跟随高光和轻量按压缩放；触摸不消费外层滚动手势；本轮未创建 APK。
- [x] Glass Lab 第 11 小步：加入 Premium / Standard / Lite / Off 四档性能策略，手动微调后转为自定义；本轮未创建 APK。
- [ ] 当前门槛：在 Android 真机安装并检查状态栏、底部导航和横竖屏。
- [ ] 当前门槛：对四档策略执行 Android 编译与真机性能验收；之后再单独研究更广范围的共享绘制树 backdrop，最后逐项迁移计算器与 AI。

> 当前 APK 仍是原生 UI 框架预览版。液态玻璃主界面此前使用的是基于 Backdrop 原语的自研原型，与用户提供的官方 Catalog APK 差距明显；2026-11-01 起已将路线改为直接移植 Kyant0/AndroidLiquidGlass tag 1.0.6 的 Catalog 组件，详见专项计划第 25 节。计算器、AI、化学、历史和云同步功能尚未迁移。

## APK 下载

- [下载知行 Android 0.1.1 Debug APK](https://raw.githubusercontent.com/PaleSquire71029/zhixingapp/main/artifacts/zhixingapp-preview.apk)（已存入仓库，后续构建成功会自动更新）
- [GitHub Actions 构建记录与备用 Artifact](https://github.com/PaleSquire71029/zhixingapp/actions/workflows/android-build.yml)

## 旧项目参考

- [知行计算器及 AI 后端](https://github.com/PaleSquire71029/calculator)
- [知行AI及项目交接文档](https://github.com/PaleSquire71029/zhixing_ai)

旧仓库只作为算法、接口、数据和功能验收参考。新 App 的界面和交互应使用原生 Android 实现，不得把旧网页整体装进 WebView。
