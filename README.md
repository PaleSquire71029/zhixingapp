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
- [x] 设计系统小项：浅色颜色、间距、形状与文字 Tokens 已接入 ZhixingTheme 统一入口；原有外观保持不变。
- [x] Glass Lab 第 1 小步：独立静态材质实验页、首页入口及返回逻辑已提交。
- [x] Glass Lab 第 2 小步：增加独立背景层 10.dp 模糊 / 清晰开关与低版本回退。
- [x] Glass Lab 第 3 小步：用 GraphicsLayer 捕获实验画布局部快照，按玻璃面板位置裁切并支持手动刷新；不是全窗口实时采样。
- [x] Glass Lab 第 4 小步：裁切坐标抽成纯函数，并增加 3 组边界单测；测试尚未执行。
- [x] Glass Lab 第 5 小步：增加默认关闭的前台动态采样，每秒最多刷新一次；离开页面或 App 进入后台时暂停。
- [x] Glass Lab 第 6 小步：API 33+ 增加可调强度的 AGSL 单通道边缘折射；RGB 不分离；API 26–32 回退快照 / 模糊；本轮未创建 APK。
- [ ] 当前门槛：在 Android 真机安装并检查状态栏、底部导航和横竖屏。
- [ ] 后续：编译与真机检查动态采样和 AGSL 折射的帧率、内存、温度及低版本回退；之后再单独研究更广范围的共享绘制树 backdrop、边缘高光与触摸反馈，最后逐项迁移计算器与 AI。

> 当前 APK 定位为原生 UI 框架预览版。0.1.1 已修复损坏的图标资源并通过 CI 构建；真机启动仍待用户确认。计算器、AI、化学、历史和云同步功能尚未迁移。

## APK 下载

- [下载知行 Android 0.1.1 Debug APK](https://raw.githubusercontent.com/PaleSquire71029/zhixingapp/main/artifacts/zhixingapp-preview.apk)（已存入仓库，后续构建成功会自动更新）
- [GitHub Actions 构建记录与备用 Artifact](https://github.com/PaleSquire71029/zhixingapp/actions/workflows/android-build.yml)

## 旧项目参考

- [知行计算器及 AI 后端](https://github.com/PaleSquire71029/calculator)
- [知行AI及项目交接文档](https://github.com/PaleSquire71029/zhixing_ai)

旧仓库只作为算法、接口、数据和功能验收参考。新 App 的界面和交互应使用原生 Android 实现，不得把旧网页整体装进 WebView。
