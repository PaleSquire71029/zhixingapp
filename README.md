# 知行 Android App

知行计算器 + 知行 AI 的全新 Android 原生应用。项目采用 Kotlin + Jetpack Compose，从原生工程和主壳开始重建，逐步迁移原计算器的数学、方程、函数、单位与化学能力；不将旧网页整体装入 WebView。

> **进度核对日期：2026-11-13**  
> **仓库：** [PaleSquire71029/zhixingapp](https://github.com/PaleSquire71029/zhixingapp)  
> **分支：** `main`  
> **当前工程版本：** 0.1.2 / versionCode 3  
> **最近完整 CI：** [Android Build #423](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43435614443)（C8.7 单位换算兼容性回归）

---

## 项目目标与技术路线

- **平台：** 仅 Android。
- **技术：** Kotlin、Jetpack Compose、Material 3；当前保持一个 `:app` Gradle 模块，按 domain / ui 分层。
- **设计：** 知行 Light Liquid，全浅色界面；借鉴 iOS 26 Liquid Glass 的材质、折射、空间层次与动画规律，同时保留知行自己的品牌和交互语言。
- **玻璃实现：** 以官方 Backdrop Catalog 1.0.6 为移植基础，不以静态半透明背景冒充实时液态玻璃。
- **计算：** 数学和数值算法优先放在无 Android / Compose 依赖的纯 Kotlin domain 层，先有边界测试，再接原生界面。
- **普通计算：** 本地可用，不强制登录。
- **AI、云历史和账号：** 属于后续产品能力；已存在接入规划，不代表 Android 客户端和云端功能已经完成。

## 当前进度总览

| 模块 | 当前状态 | 说明 |
|---|---|---|
| Android 原生工程 | **已完成** | Kotlin + Compose、Gradle Wrapper、主题、基础导航和 CI |
| 主壳与 Liquid Glass | **真机验收通过** | 浮动玻璃 Dock、二级页面转场、材质控件和触摸反馈 |
| PR #88 | **功能验收完成** | 连续滑块、开关长按手势、120Hz 窗口刷新率偏好；iQOO Neo5 活力版真机验收已通过 |
| 数学解析与代数内核 | **C0–C4 已完成** | Tokenizer、Parser、常数求值、多项式、有理函数、实根 |
| 方程求解 | **C5.1–C5.7 已完成** | 常数 / 多项式、三次、分式、绝对值、平方根、受限指数 / 对数方程 |
| 不等式求解 | **C6.1–C6.2 已完成** | 一至三次多项式不等式、分式不等式 |
| 函数工具内核 | **C7.1–C7.6 已完成** | 点集校验、一次 / 二次 / 三次 / 反比例拟合、定义域、求值和安全采样 |
| 单位换算内核 | **C8.1–C8.7 已完成** | 九类目录、76 个线性单位、3 个温度仿射单位、统一入口及旧版回归验收 |
| 计算器原生页面 / 专用输入 | **待接入** | 计算内核已建立，仍需完成可用的原生输入、结果展示和页面交互 |
| 函数图像 UI | **待接入** | 函数拟合、定义域和采样数据层已完成，Compose 图表页面尚未完成 |
| 化学配平与反应预测 | **待迁移** | 旧项目已有算法参考，Android 原生迁移尚未完成 |
| 知行 AI Android 客户端 | **待接入** | 已有接入方案，Android 客户端尚未完成 |
| 原生账号、云历史 | **待接入** | 尚未完成 Android 端认证、持久化与云端历史接入 |

**当前工作重心：计算器功能。** C8.7 已完成单位换算兼容性回归与 Domain API 验收；下一步将统一入口接入原生单位换算页面，同时继续扩展方程题型、完善数学输入和函数图像能力。

---

## 项目进展时间线

### 1. 2026-10-31：项目启动与 Android 原生基础

- 新建独立仓库 `PaleSquire71029/zhixingapp`，确定只做 Android，不在旧网页上继续堆叠移动端 UI。
- 建立 Kotlin + Jetpack Compose 工程，采用单一 `:app` 模块；配置 AGP 9.1.1、Gradle 9.3.1、JDK 17、compileSdk / targetSdk 36、minSdk 26。
- 建立浅色知行主题和四个一级入口；随后完善为「计算 / 知行 AI / 工具 / 我的」。
- 加入用户提供的品牌图标和桌面启动图标；修复旧 WebP 资源损坏导致的启动闪退 / 图标缺失问题，并加入 Android 资源完整性检查。
- 建立 Edge-to-edge、系统栏、安全区和键盘窗口基础配置；建立 GitHub Actions 单元测试与 Debug APK 构建链路。
- 后续将颜色、间距、形状、文字等主题 Tokens 集中管理，保留浅色方案和既有字体策略。

### 2. 2026-11-01 起：Liquid Glass 主壳与材质系统

- 对比用户提供的参考 APK 后，确定直接移植官方 Backdrop Catalog 1.0.6 的路线，暂停此前零散自研玻璃方案。
- 将玻璃渲染基础适配到知行浅色主题，逐步用于主界面浮动 Dock、材质实验室和可交互控件。
- 底部导航改为浮动液态玻璃 Dock，使用圆润、填色的图标；二级页面加入淡入、轻微横移和缩放转场。
- 材质设置包含透明度、模糊等参数，并持续完善滑块、开关、触摸形变、拖动和释放动画。
- 将主壳真机验收与算法 CI 区分：液态玻璃效果必须在目标设备上检查，不能只凭 APK 编译成功判断。

### 3. PR #88：连续滑块、开关手势与刷新率偏好

关联记录：[PR #88 — Fix continuous glass slider drag and switch long-press scrolling](https://github.com/PaleSquire71029/zhixingapp/pull/88)

**实现与验收内容：**

- 材质滑块取消离散步进，支持连续拖动，不再出现固定百分比跳格。
- 拖动期间直接跟随手指更新玻璃形变；松手时再同步速度与弹簧动画，避免每次指针更新都反复启动 / 取消动画。
- 开关拇指使用可消费的水平拖动手势；长按后消费后续移动，避免父级纵向滚动抢走开关手势。
- 保留正常滚动语义：长按识别前的纵向滑动仍可滚动设置页面。
- 增加滑块连续值、开关长按滚动仲裁、正常纵向滚动和刷新率策略相关回归测试。
- 增加 120Hz 窗口刷新率偏好策略：请求值受 Android 版本和设备实际支持的显示模式约束，不假设系统一定会授予 120Hz。

**iQOO Neo5 活力版真机验收通过**

用户已确认相关滑块连续拖动、开关长按与页面滚动、刷新率偏好以及主壳相关交互的真机验收通过。该事项属于 **主壳 / Liquid Glass 阶段**，不属于计算器数学内核阶段；不再列为待真机验收事项。

**GitHub 状态说明：** 截至本次核对，PR #88 在 GitHub 上仍显示为 open，因此这里记录的是功能开发和真机验收已完成，不将 PR 状态误写为已经合并到 `main`。

### 4. 2026-10-31 至 2026-11-13：计算器纯 Kotlin 内核迁移

计算器迁移遵循“先锁定旧逻辑语义和边界，再实现纯 Kotlin 算法，最后接入 Compose 页面”的顺序。以下 Build 均用于对应阶段的自动化验收；没有把 APK 编译等同于计算器 UI 已完成。

#### C0–C3：样例、解析与基础求值

- **C0.1：** 建立旧计算器语法和行为的黄金样例，为后续迁移提供可核对的输入与预期结果。
- **C1：** Tokenizer，处理数字、运算符、括号、变量和符号归一化。
- **C2：** AST 与递归下降 Parser，支持基础四则运算、隐式乘法、一元正负、幂、百分号和括号；通过 [Build #350](https://github.com/PaleSquire71029/zhixingapp/actions/runs/42443214070)。
- **C3：** 常数表达式求值器，明确除零、非有限值和数值格式边界；通过 Build #363。

#### C4：多项式、有理函数与实根

- **C4.1：** 多项式系数模型、规范化、加减乘、缩放与 Horner 求值；通过 Build #365。
- **C4.2.1：** 有理函数模型及原式定义域约束；通过 Build #366。
- **C4.2.2：** 有理函数加减乘除、幂和定义域传播；通过 Build #370。
- **C4.2.3：** 单点求值、原始分母零点约束、可去间断点保留、非有限中间值和精度损失保护；经 Build #372 / #379 / #381 / #383 验收。
- **C4.3.1：** 零多项式、常数与一次多项式实根分类；通过 Build #388。
- **C4.3.2：** 二次多项式实根，覆盖两个不同实根、重根、无实根、近消去小根和数值范围边界；通过 Build #391。四次及以上仍明确返回不支持，不伪造结果。

#### C5：方程求解

- **C5.1 / C5.2：** 常数、一次和二次多项式方程；通过 Build #393 / #396。
- **C5.3：** 三次多项式方程；支持单实根、三个不同实根、二重根和三重根等分类。
- **C5.4：** 分式方程，保留原式定义域并剔除增根；C5.3 / C5.4 通过 Build #400。
- **C5.5：** 线性内部绝对值方程 `|ax+b|=c`；通过 Build #401。
- **C5.6：** 受限平方根方程 `√P(x)=Q(x)`，支持平方后的增根剔除、定义域和回代检查；通过 Build #402。
- **C5.7：** 四种受限指数 / 对数方程形式，覆盖底数、真数定义域、回代和数值精度；通过 [Build #403](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43400220986)。

#### C6：不等式求解

- **C6.1：** 一至三次多项式不等式，处理符号区间、严格 / 非严格端点及重根；通过 [Build #404](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43401527163)。
- **C6.2：** 分式不等式，保留分母零点、原式定义域空洞和可去间断点；通过 [Build #405](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43403410088)。

#### C7：函数工具内核

- **C7.1：** 坐标点模型与输入校验，覆盖空输入、非有限坐标、重复横坐标和点数下限；通过 Build #406。
- **C7.2：** 一次函数拟合；两点插值、多点普通最小二乘；通过 Build #409。
- **C7.3：** 二次函数拟合；三点插值、多点最小二乘，使用列主元 Householder QR；通过 Build #412。
- **C7.4：** 三次函数拟合；四点插值、多点最小二乘，使用列主元 Householder QR；通过 Build #414。
- **C7.5：** 反比例函数拟合，支持单点定参与多点最小二乘，明确处理 x=0；通过 Build #415。
- **C7.6：** 统一四类拟合函数的定义域、求值和安全图像采样；反比例函数在 x=0 两侧分段，避免错误连接；通过 [Build #416](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43415412088)。

#### C8：单位换算内核

- **C8.1：** 九类单位目录和物理量纲模型；温度明确标记为仿射转换；通过 Build #417。
- **C8.2：** 长度、面积、体积 36 项具体单位定义，保留旧 ID、因子、标签和默认选择；通过 Build #418。
- **C8.3：** 质量 9 项、速度 6 项、时间 9 项；线性单位累计 60 项；通过 Build #419。
- **C8.4：** 数据存储 10 项、角度 6 项；线性单位累计 76 项，区分 1024 与 1000 进位；通过 Build #420。
- **C8.5：** 摄氏度、华氏度、开尔文仿射转换；以开尔文为内部基准，覆盖绝对零度、负温度和非有限值；通过 Build #421。
- **C8.6：** 统一线性 / 仿射 `UnitConverter`，检查类别、物理量纲、单位归属、溢出和精度下溢；九类转换入口及跨类别拒绝测试通过 [Build #422](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43431479631)。
- **C8.7：** 对照旧版 `calculator.html` 完成兼容性回归：76 个线性单位的 780 组有序配对 × 5 个输入（3,900 次），温度 45 次，另核对九类顺序 / 默认单位；通过 [Build #423](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43435614443)。稳定接口契约见 [UNIT_CONVERSION_API.md](docs/UNIT_CONVERSION_API.md)。

截至目前，单位内核包含 **76 个线性单位定义 + 3 个温度仿射单位定义**，并提供统一的九类转换入口；C8.7 旧版有效输入兼容回归与 Domain API 迁移验收已通过。原生单位换算 UI、输入解析与结果展示仍待接入。

---

## 当前计算器工作重点

下一阶段以用户可直接使用的计算器功能为中心，优先把已验收的数学内核接入 Android 原生页面。

1. **原生单位换算页面：** 按 [Domain API 契约](docs/UNIT_CONVERSION_API.md) 接入九类类别、源 / 目标单位选择、输入换算、结果格式化和同类单位一览；UI 只消费 `UnitConverter`，不复制算法。
2. **扩展方程题型：** 继续完善高中常见方程形式、组合形式、定义域约束和回代验证；每种新题型单独建模、单独测试。
3. **原生输入链路：** 把已有 Tokenizer / Parser 与 Android 计算页接起来，设计适合手机操作的数学表达式输入方式和专用键盘，避免依赖系统键盘完成复杂数学输入。
4. **函数页面：** 将 C7 的点集、拟合、定义域和采样能力接入原生函数页面，再实现坐标轴、曲线绘制、缩放和点位交互。
5. **补齐计算器其他核心工具：** 继续迁移化学方程式解析 / 配平与反应预测，保持精确算法和可验证结果。
6. **逐步形成完整计算体验：** 先让已经完成的 domain 内核进入真实页面，再扩展 AI 辅助解题等能力。

当前仍需完成的重点不是重新开发已经通过测试的单位比例表，而是扩大数学能力、完善输入体验，并把已完成的计算内核接到原生界面。

## 已知未完成事项

- 计算 Tab 尚未完成完整原生计算器页面接入；C0–C8.7 的算法 / 数据层完成，不等于所有能力已经在 UI 中可见。
- 自定义数学输入法、复杂题目结构化输入和完整结果过程展示仍需实现。
- 函数图表 Compose UI 尚未完成；目前完成的是拟合、定义域、求值和安全采样 domain。
- 化学配平、反应预测尚未完成 Android 原生迁移。
- 知行 AI Android 客户端、原生账号与云端历史尚未完成。
- 正式 Release 签名 / AAB 发布流程尚未完成。

---

## 构建与验收记录

- 最新单位换算内核完整 CI：[Android Build #423](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43435614443)。
- Build #423 已通过资源检查、全部 JVM 单元测试、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传和 Actions 清理；主 Gradle 步骤 `BUILD SUCCESSFUL in 46s`。
- 对应 Debug APK Artifact：[zhixingapp-debug / 14042138313（12,553,719 bytes）](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43435614443/artifacts/14042138313)。
- CI 的 Android 测试步骤完成的是 instrumentation test 源码编译；除 PR #88 相关的用户真机验收记录外，不把普通 CI 的源码编译描述成模拟器或真机已运行。
- 主壳 / Liquid Glass / PR #88 的 iQOO Neo5 活力版真机验收已由用户确认通过。

## 开发前必读

1. [项目现状、操作指南与后续开发手册](docs/PROJECT_STATUS_AND_GUIDE.md)
2. [实施状态与下一步清单](docs/IMPLEMENTATION_STATUS.md)
3. [Android App 总体规划与迁移蓝图](docs/ANDROID_APP_MASTER_PLAN.md)
4. [液态玻璃参考 APK 直接移植计划](docs/LIQUID_GLASS_REDESIGN_PLAN.md)
5. [计算器核心迁移计划](docs/CALCULATOR_CORE_MIGRATION_PLAN.md)
6. [计算器原生页面设计](docs/CALCULATOR_NATIVE_UI_DESIGN.md)
7. [高中题型、专用输入法、函数图像与 AI 协同规划](docs/CALCULATOR_INPUT_SOLVER_GRAPH_PLAN.md)
8. [单位换算 Domain API 契约](docs/UNIT_CONVERSION_API.md)
9. [计算器 AI 原生接入方案](docs/AI_CALCULATOR_ANDROID_INTEGRATION_PLAN.md)
9. [APK 首次真机验收清单](docs/APK_TEST_CHECKLIST.md)

开始下一步前，先检查 `main` 最新提交和上述文档；计算功能仍按小步实施，先保证正确性和测试，再接入 UI。

## APK 获取

- [最新完整 CI 的 Debug APK Artifact（Build #423）](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43431479631/artifacts/14039917793)
- [GitHub Actions 构建记录](https://github.com/PaleSquire71029/zhixingapp/actions/workflows/android-build.yml)
- [仓库归档 Debug APK](https://raw.githubusercontent.com/PaleSquire71029/zhixingapp/main/artifacts/zhixingapp-preview.apk)（归档包可能落后于最新源码；下载前先核对构建号和版本）

## 旧项目参考

- [知行计算器及 AI 后端](https://github.com/PaleSquire71029/calculator)
- [知行 AI 及项目交接文档](https://github.com/PaleSquire71029/zhixing_ai)

旧仓库只作为算法、接口、数据结构、字体和功能样例的参考。新 App 界面与交互使用 Android 原生实现，不把旧网页整体放进 WebView。
