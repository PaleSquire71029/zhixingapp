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
8. [计算器 AI 原生接入方案](docs/AI_CALCULATOR_ANDROID_INTEGRATION_PLAN.md)
9. [高中题型、专用输入法、函数图像与 AI 协同规划](docs/CALCULATOR_INPUT_SOLVER_GRAPH_PLAN.md)

开始任何实现前，先检查以上文档和仓库当前 commit。计算迁移按 C0–C12 计划推进；C4.2.3 原式定义域与数值精度保护、C4.3.1 / C4.3.2 一次与二次实根已通过对应 CI。C5.1 / C5.2 常数至二次方程、C5.3 三次方程和 C5.4 分式方程均已接入纯 Kotlin 内核；三次方程覆盖单实根、三个不同实根、二重根、三重根和小根消去边界，分式方程保留原式定义域并剔除增根。C5.3 / C5.4 通过 Android Build #400（Run ID `43397103889`）；C5.5 线性内部绝对值方程通过 Build #401；C5.6 平方根方程 `√P(x)=Q(x)`（deg(P)≤2、deg(Q)≤1）通过 Build #402（Run ID `43398997674`）。已覆盖平方增根剔除、原式定义域、残差检查和恒等式受限解集。C5.7 指数 / 对数方程已实现四种受限形式，并通过 Build #403（Run ID `43400220986`）完整 CI。C6.1 一至三次多项式不等式已实现符号区间、严格 / 非严格端点和重根符号判断，并通过 Build #404（Run ID `43401527163`）完整 CI，Artifact ID `14025750621`。 C6.2 分式不等式已通过 Build #405（Run ID `43403410088`）完整 CI，保留原始定义域空洞、分母零点与除法限制，Artifact ID `14026112216`。C7.1 坐标点模型与输入校验已通过 Build #406（Run ID `43404794011`），Artifact ID `14027365163`。C7.2 一次函数拟合已通过 Build #409（Run ID `43410045585`），Artifact ID `14029077035`；两点确定插值直线，多点采用普通最小二乘。C7.3 二次函数拟合已通过 Build #412（Run ID `43411748705`），Artifact ID `14029827428`，大小 12,503,320 bytes；三点插值、多点最小二乘使用列主元 Householder QR。C7.4 三次函数拟合已通过 Build #414（Run ID `43413083875`），Artifact ID `14030506504`，大小 12,510,326 bytes；四点插值、多点最小二乘使用四列列主元 Householder QR，并覆盖低次退化数据和极端数值范围。C7.5 反比例函数拟合已通过 Build #415（Run ID `43414570879`），Artifact ID `14031401965`，大小 12,517,040 bytes；支持单点定参、多点纵向普通最小二乘，明确拒绝 x=0，并覆盖归一化与系数精度边界。C7.6 统一函数定义域与安全图像采样已通过 Build #416（Run ID `43415412088`），Artifact ID `14031292575`，大小 12,528,905 bytes；四类函数共用定义域 / 求值入口，反比例函数在 x=0 两侧分段采样，并覆盖采样预算与精度边界。函数图表 UI、表达式解析和真机测试仍未完成。每个小步骤只做代码检查和针对性测试，不要求每一步都构建 APK；在阶段检查点统一执行完整 CI。计算器 AI 的原生接入方案已单独规划。

## 当前进度（核对至 2026-11-13）

- [x] 原生 Android 工程、Kotlin + Jetpack Compose、浅色知行主题和 GitHub Actions Debug 构建链路。
- [x] 一级导航调整为「计算 / 知行 AI / 工具 / 我的」；「我的」页已接入历史空状态、材质设置入口和应用版本信息。
- [x] 底部导航采用圆润填色图标与浮动液态玻璃 Dock；二级页面已加入淡入、轻微横移和缩放转场。
- [x] Glass Lab 与主要材质实验控件已完成一轮官方 Backdrop 1.0.6 适配；具体光学效果、滑块连续性和性能仍需目标真机验收。
- [x] 计算核心 C0.1 黄金样例已建立，C1 Tokenizer 已通过 CI。
- [x] C2 AST / 递归下降 Parser 已通过 Android Build #350（Run ID `42443214070`）。
- [x] C3 常数表达式求值器已通过 Android Build #363（Run ID `42444326915`）。
- [x] C4.1 多项式基础运算与零多项式规范化已通过 Android Build #365。
- [x] C4.2.1 有理函数模型与定义域不变量已通过 Android Build #366。
- [x] C4.2.3 单点求值、原式定义域、精确零点和分子 / 分母精度损失保护完成 CI 验收（Build #372 / #379 / #381 / #383）；未进行模拟器 / 真机仪器测试。
- [x] C4.3.1 零 / 常数根集分类与一次实根通过 Build #388（Run ID `42456084109`）。
- [x] C4.3.2 二次多项式实根通过 Build #391（Run ID `42544184281`）：覆盖两个不同实根、重根、无实根、判别式近消去、稳定小根、溢出 / 下溢和归一化残差；C5.3 后开放三次，四次及以上仍明确返回 `UnsupportedDegree`。
- [x] C4.3.2 源码最终提交 `9db9d21c9506b401959654bc6ecefc3ec7c043ff`；首次 Build #390 因重复声明根错误辅助函数 / 残差阈值失败，读取日志后在该提交修复，Build #391 完整通过；未删除或放宽有效断言。
- [ ] C4.3 目前只完成 CI / JVM 单元测试与 Debug APK 构建；未启动模拟器或执行真机仪器测试。
- [x] C4.2.2 有理式加减乘除、幂与定义域传播已通过 Android Build #370。
- [x] 已新增计算器 AI 原生接入方案；Worker 移动端安全认证与 Android 客户端尚未实施。
- [x] C5.1 / C5.2 常数至二次方程通过 Build #393 / #396；C5.3 三次方程和 C5.4 分式方程通过 Build #400；C5.5 `|ax+b|=c` 通过 Build #401；C5.6 `√P(x)=Q(x)`（P≤二次、Q≤一次）通过 Build #402（Run ID `43398997674`）。
- [x] C5.7 指数 / 对数方程：支持常底线性指数式等式、对数等于常数、同底线性真数对数等式；定义域、回代和数值精度边界已覆盖 Build #403。
- [x] C6.1 一至三次多项式不等式通过 Build #404（Run ID `43401527163`）：覆盖严格 / 非严格端点、重根、三实根、无解 / 全体实数与高次暂缓。
- [x] C6.2 分式不等式通过 Build #405（Run ID `43403410088`）：覆盖分子 / 分母零点、可去间断点、原式历史定义域、除法限制和精度边界。
- [x] C7.1 坐标点模型与输入校验通过 Build #406（Run ID `43404794011`）：覆盖空输入、非有限坐标、重复 x（含正负零）、点数下限和输入快照。
- [x] C7.2 一次函数拟合通过 Build #409（Run ID `43410045585`）：支持两点插值与多点普通最小二乘，覆盖噪声数据、极端数值缩放、退化精度及系数溢出。
- [x] C7.3 二次函数拟合通过 Build #412（Run ID `43411748705`）：三点插值、多点普通最小二乘，使用列主元 Householder QR，覆盖退化点集、精度损失和系数溢出。
- [x] C7.4 三次函数拟合通过 Build #414（Run ID `43413083875`）：四点插值、多点普通最小二乘，使用列主元 Householder QR，覆盖低次退化、无效输入及系数溢出 / 下溢。
- [x] C7.5 反比例函数拟合通过 Build #415（Run ID `43414570879`）：单点确定参数，多点按纵向残差平方和做普通最小二乘；x=0 返回独立定义域错误，16 个新增 JUnit 方法通过。
- [x] C7.6 统一函数定义域与安全图像采样通过 Build #416（Run ID `43415412088`）：支持四类已拟合函数的统一求值；反比例函数在 x=0 处分成独立 segment，18 个新增 JUnit 方法通过。
- [ ] 下一步 C8 单位换算纯 Kotlin 内核；随后推进化学、计算器页面 / 数学键盘、AI 客户端、账号和云历史。
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
