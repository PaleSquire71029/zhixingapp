# 知行 Android App

知行计算器 + 知行 AI 的全新 Android 原生应用。项目采用 Kotlin + Jetpack Compose，从原生工程和主壳开始重建，逐步迁移原计算器的数学、方程、函数、单位与化学能力；不将旧网页整体装入 WebView。

> **进度核对日期：2026-11-27**  
> **仓库：** [PaleSquire71029/zhixingapp](https://github.com/PaleSquire71029/zhixingapp)  
> **默认分支：** `main`  
> **当前 UI 开发：** `docs/ui-first-calculator-roadmap`（PR #92，尚未合并）  
> **当前工程版本：** 0.1.2 / versionCode 3  
> **最近完整算法与 APK CI：** [Android Build #473](https://github.com/PaleSquire71029/zhixingapp/actions/runs/44337799622)（C5.18 最终 PR，完整 JVM 测试、Android 测试源码编译和 Debug APK 组装通过）  
> **当前 UI 框架 CI：** [Build #525](https://github.com/PaleSquire71029/zhixingapp/actions/runs/45986224060)（PR #92 F4.5 Shell 导航回归，JVM 测试、Android 测试源码编译、Debug APK 组装与 Artifact 上传完整通过；Artifact 15191837513）  
> **文档权威规则：** [文档体系修复计划](docs/DOCUMENTATION_REPAIR_PLAN.md)；当前进度以 `docs/IMPLEMENTATION_STATUS.md` 为准。

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
| 主壳与 Liquid Glass | **用户确认目标设备验收通过** | 浮动玻璃 Dock、二级页面转场、材质控件和触摸反馈；PR #88 仍在 GitHub open，未将其状态写成已合并 |
| PR #88 | **功能验收完成** | 连续滑块、开关长按手势、120Hz 窗口刷新率偏好；iQOO Neo5 活力版真机验收已通过 |
| 数学解析与代数内核 | **C0–C4 已完成** | Tokenizer、Parser、常数求值、多项式、有理函数、实根 |
| 方程求解 | **C5.1–C5.18 已通过 CI** | 多项式、分式、绝对值、根式、指数 / 对数、二元一次方程组、正弦 / 余弦 / 正切周期方程、指定区间筛选、辅助角组合三角方程、正弦等式及正弦—余弦等式 |
| 不等式求解 | **C6.1–C6.2 已完成** | 一至三次多项式不等式、分式不等式 |
| 函数工具内核 | **C7.1–C7.6 已完成** | 点集校验、一次 / 二次 / 三次 / 反比例拟合、定义域、求值和安全采样 |
| 单位换算内核 | **C8.1–C8.7 已完成** | 九类目录、76 个线性单位、3 个温度仿射单位、统一入口及旧版回归验收 |
| 原生单位换算页面 | **C8.8.2 交互完善已通过 CI** | 复制完整等式、清除输入、未完成数字状态、64 字符限制、窄屏纵向布局和 TalkBack 语义；真机验收按用户要求暂缓 |
| Zhixing Flow UI 框架 | **F0–F3、F4.1–F4.5 已完成 CI；进入 F5 逐页迁移** | Activity split（#509）、typed routes（#511）、NavigationState（#516）、Dock overlay（#518）、PageViewport（#520）、页面转场（#522）、Shell 回归（#525）通过 CI；PR #92 仍 open，未合并 |
| 计算器原生页面 / 专用输入 | **待接入** | 计算内核已建立，仍需完成可用的原生输入、结果展示和页面交互 |
| 函数图像 UI | **待接入** | 函数拟合、定义域和采样数据层已完成，Compose 图表页面尚未完成 |
| 化学配平与反应预测 | **待迁移** | 旧项目已有算法参考，Android 原生迁移尚未完成 |
| 知行 AI Android 客户端 | **待接入** | 已有接入方案，Android 客户端尚未完成 |
| 原生账号、云历史 | **待接入** | 尚未完成 Android 端认证、持久化与云端历史接入 |

**当前工作重心：Zhixing Flow F5 Existing Screens 逐页迁移。** F0–F3 与 F4.1–F4.5 Shell 框架均已完成 CI。F4.5 新增 5 项 JVM 回归测试，覆盖四个根 Tab 的旧整数 Saver 往返、二级 route 字符串 Saver 往返、根 Tab 选择时关闭二级页、全部二级页返回原根 Tab 和非法恢复索引回退；Build #525（Run ID `45986224060`）完整 success。另新增 5 项 `MainActivity` instrumentation 测试源码，覆盖四个 Tab、History / Unit Converter 系统 Back、Activity 重建恢复和 Dock / PageViewport 层级；当前 CI 只编译这些源码，不启动模拟器执行。PR #92 仍 open、未合并；尚未迁移业务页面、不改数学 Domain。下一步从工具目录、我的 / 历史 / 材质设置和单位换算开始逐页迁移。详细规划见 [界面框架重建计划](docs/ZHIXING_DESIGN_SYSTEM_REBUILD_PLAN.md)。

---

## 项目进展时间线

### 2026-11-21：Zhixing Flow F2 核心组件库与 Design Gallery

- 新增 `ui/components` 基础组件：`ZhixingSurface`、`ZhixingButton`、`ZhixingInput`、`ZhixingListRow`、`ZhixingSegmentedControl`、`ZhixingSheet`、`ZhixingResultPanel`。
- 新增独立 `DesignGalleryScreen`，提供 390dp 窄屏 / 720dp 宽屏预览；600dp 为单列 / 双列切换阈值。
- 组件覆盖按压、选中、禁用、错误、加载、焦点状态，并提供 Button / RadioButton / Heading / TextField 错误等语义。
- 新增 2 项 JVM 布局阈值测试与 5 项 Compose UI 交互 / 语义测试。Build #499（Run ID `44826734182`） 完整 success；Artifact 14673652163。
- CI 只编译 androidTest 源码，没有运行模拟器 / 真机 instrumentation tests。Gallery 未接入导航，现有业务页面、MainActivity、玻璃算法和 Gradle 依赖均未修改。

### 2026-11-27：Zhixing Flow F3 整页刚性回弹独立原型

- 新增 `ZhixingRigidOverscrollPhysics` 与 `ZhixingRigidOverscrollEffect`，使用现有 Compose Foundation `OverscrollEffect`，由单一页面级 owner 处理 scroll / fling 的未消费量。
- 内容 `ScrollState` 不越界；页面 viewport 只通过一个外层 `graphicsLayer.translationY` 做最多 48dp 的刚性平移，不改变测量尺寸、不缩放、不拉伸。拖动阻尼随位移增大而衰减，释放后使用现有 Zhixing Flow Spring Token 回位。
- 反向手势先释放已有位移，再把剩余 delta 交还内容；没有可滚动范围时不产生空白回弹；横向 delta 保留给内层横向控件。
- 新增独立 `ZhixingScrollPrototypeScreen` 与 Debug-only `ZhixingScrollLabActivity`，使用 `zhixing://scroll-lab` 深链打开；Release 不包含该 Activity，未修改 MainActivity / 正式导航。
- 新增 8 项 JVM 物理 / 边界测试和 3 项 Compose 手势 / viewport 测试。Build #506（Run ID `45859680761`）完整 success；Debug Artifact 15138440787（31,319,661 bytes，SHA-256 `b6d7f404bf30edcf9208a93a2a8c1a8836d224053f2e64fea9e5fbf1f28d3b13`）。
- CI 编译了 3 项 Android instrumentation 测试源码，但没有启动模拟器执行它们。用户在目标设备安装测试后确认体验没有问题；未把自动化源码编译误记为模拟器测试执行。

### 2026-11-27：Zhixing Flow F4.1 / F4.2 App Shell 与导航基础重构

- F4.1 将原有根 Compose 内容从 `MainActivity.kt` 移到 `ui/shell/ZhixingAppRoot.kt`；Activity 只处理 Edge-to-edge、系统栏对比度、adjustResize 和 setContent。Build #509 完整 success。
- F4.2 新增 `AppRootRoute` / `AppSecondaryRoute`，根 Tab 与二级页切换由 enum 表达；Saver 保留旧 Tab 整数索引、二级 route 字符串及 SaveableStateHolder key。
- 新增 3 项路由 / 旧状态兼容 JVM 测试；Build #511（Run ID `45862127405`）完整 success，JVM 测试、Android 测试源码编译、Debug APK 组装和 Artifact 上传均通过。
- 保留现有四入口顺序、Dock / Backdrop / 材质设置、页面内容、二级转场与返回行为；未引入 Navigation 3 或新增 Gradle 依赖，尚未迁移业务页面。

### 2026-11-27：Zhixing Flow F4.3 / F4.4a 导航状态与固定 Dock 分层

- F4.3 新增 `AppNavigationState`，统一管理四个根 Tab、二级页进入 / 关闭和 Back 行为；新增 3 项 JVM 状态转移测试，覆盖二级页返回、切换根 Tab 自动关闭二级页、无二级页时 Back 不改变状态。
- 两个 `rememberSaveable` 状态调用留在 Shell 根 Composable 原位置与原顺序，`rememberSaveableStateHolder` 仍排在其后；状态对象只包装原有 MutableState，不在新的 Composable 层级内创建保存槽位，尽量保持旧恢复位置稳定。
- Build #516（Run ID `45863354210`）完整 success；Debug Artifact 15138024765（31,319,453 bytes）。
- F4.4a 新增 `ui/shell/ZhixingDockOverlay.kt`，将固定 Dock wrapper 从根 Shell 提取；继续复用 `ZhixingLiquidBottomBarPlacement` / `ZhixingCatalogBottomBar`，原 Backdrop / GlassMaterialState / destination list / selected index / selection callback / 68dp 高度不变。
- Build #518（Run ID `45863814661`）完整 success；Debug Artifact 15139212037（31,321,224 bytes，SHA-256 `6ded753757237e5945dff083d10441672a30a3d38cb35474e7ea79bf90d6ee3a`）。
- F4.4a 只调整 Dock 的代码归属，没有改变 Dock 视觉、动画、Insets / IME 避让、Backdrop 算法或正式路由行为；未迁移业务页面。

### 2026-11-27：Zhixing Flow F4.4b 页面 viewport 与 Dock overlay 分层

- 新增 `ui/shell/ZhixingPageViewport.kt`，使用 layout-neutral 的全尺寸 Box 承载现有根页面分支。
- 根 Shell 外层 Box 现在包含两个兄弟层：`ZhixingPageViewport` 页面内容层与 `ZhixingDockOverlay` 固定导航层；Dock 不进入页面内容 viewport。
- viewport 不添加 clip、scroll、padding、Insets 或 graphics transform；原页面 `verticalScroll`、`Scaffold.innerPadding`、系统栏 / IME 与 Dock 的 68dp / Backdrop / 选择回调保持不变。
- Build #520（Run ID `45864663317`）完整 success；Debug Artifact 15139507502（31,323,452 bytes，SHA-256 `d30cc66c929e8523efc38bf41082ccaf3de344641da14cce0c7779d5f174cb9f`）。
- 当前只完成结构分层，不代表页面已统一接入 F3 scroll container，也没有迁移旧业务页面。

### 2026-11-27：Zhixing Flow F4.4c 二级页面转场 owner 提取

- 新增 `ui/shell/ZhixingSecondaryPageTransition.kt`，将原有 `AnimatedContent` 以及 fade / slide / scale 转场封装为单一 owner；`ZhixingAppRoot` 只传入当前二级路由和页面内容。
- 保持原转场参数：淡入 230ms、横向进入 280ms（容器宽度 / 9）、初始 scale 0.985；淡出 170ms、横向退出 220ms（反向宽度 / 14）、目标 scale 0.99；label 保持 `zhixingSecondaryPageTransition`。
- `BackHandler` 仍在根 Shell，导航状态仍由 `AppNavigationState` 管理；没有调整页面内容、Dock / viewport 层级或业务行为。
- 新增 2 项 JVM 转场契约测试。Build #522（Run ID `45866322423`）完整 success；Debug Artifact 15140782538（31,324,748 bytes，SHA-256 `39fe13e1666839332199f89237284fe046e4a0dfb35ad33ee182dfd7b26c600f`）。
- CI 执行 JVM 单测、编译 androidTest 源码、组装 Debug APK 并上传 Artifact；未启动模拟器执行 instrumentation tests，也未进行本步真机转场验收。首次运行遇到 Maven SLF4J 元数据解析失败，重跑失败 job 后通过。

### 2026-11-27：Zhixing Flow F4.5 Shell 导航与状态回归

- 新增 `AppShellNavigationRegressionTest` 5 项 JVM 测试，覆盖四个根 Tab 的旧整数 Saver 往返、三个二级 route 的旧字符串 Saver 往返、根 Tab 变更时关闭二级页、每个根 Tab / 二级页组合的返回语义，以及非法根索引恢复回退。
- 新增 `ZhixingAppShellNavigationIntegrationTest` 5 项 Activity instrumentation 测试源码，覆盖四入口切换、History / Unit Converter 的 Android Back、Activity recreate 后二级页与原根 Tab 恢复，以及 `ZhixingPageViewport` / `ZhixingDockOverlay` 为 Shell host 直接兄弟层。
- 在 Shell host、PageViewport、Dock 外层 Box 加入仅供测试定位的 semantics `testTag`；没有添加尺寸 / padding / clip / scroll / graphicsLayer 或绘制逻辑。
- Build #524 首轮发现 Saver receiver 和 TestRule import 错误；分别修复后 Build #525（Run ID `45986224060`）完整 success。Debug Artifact 15191837513（31,324,808 bytes，SHA-256 `24a523278b0fadf732435c57decf104714aac8926e6288d75a805a03741e3125`）。
- Build #525 执行并通过全部 JVM 单测、编译 Android instrumentation 测试源码、组装 Debug APK 与上传 Artifact；未启动模拟器运行这 5 项 androidTest，也未完成 F4.5 真机回归。

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
- **C5.8：** 将绝对值方程内部式从一次扩展到二次，复用现有二次根引擎；覆盖 `|x²−1|=3` 双根、`|x²−1|=1` 三解合并、零目标去重，并明确三次内部式暂缓；通过 [Build #429](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43550028977)。
- **C5.9：** 新增二元一次方程组，区分唯一解、无解和无穷多解；覆盖零系数、近奇异独立方程、非有限输入、解溢出和非零解下溢；通过 [Build #431](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43550576403)。
- **C5.10：** 新增实数立方根方程 `∛P(x)=Q(x)`，限制 `deg(P)≤3`、`deg(Q)≤1`；利用实数立方映射的一一对应求候选并回代原式，覆盖负解、恒等式、无解、结构限制和立方溢出；通过 [Build #433](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43551030029)。
- **C5.11：** 新增变量右侧绝对值方程 `|P(x)|=Q(x)`，限制 `deg(P)≤2`、`deg(Q)≤1`；分别求解 `P−Q=0` / `P+Q=0`，检查 `Q≥0`，恒等分支保留非负定义域；11 个 JUnit 测试方法通过 [Build #437](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43551712552)。中间重构 Build #435 曾因重复声明编译失败，已由最终修正提交修复，并在 Build #436 / #437 通过。
- **C5.12：** 新增弧度制正弦方程 `sin(ax+b)=c`。当 `a≠0` 时返回一到两个周期解族 `x=x₀+2kπ/|a|`；当 `a=0` 时分类为全体实数或无解；覆盖 ±1 边界合并、负系数、越界目标、周期溢出与相位精度；11 个 JUnit 测试方法通过 [Build #439](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43552472439)。
- **C5.13：** 新增弧度制余弦方程 `cos(ax+b)=c`，以 `±acos(c)` 两个角度分支生成周期解族；覆盖两族 / 边界单族、相位偏移、负系数、常数方程、目标越界、周期溢出和相位精度；11 个 JUnit 测试方法通过 [Build #440](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43552940320)。
- **C5.14：** 新增弧度制正切方程 `tan(ax+b)=c`；`a≠0` 时返回周期为 `π/|a|` 的单个周期解族，`a=0` 时检查常数角及 `π/2+kπ` 极点；保留类型化非有限 / 精度错误。13 个 JUnit 测试方法通过 Build #441（Run ID 43578324526）。
- **C5.15：** 新增正弦 / 余弦 / 正切周期解族的指定区间筛选，支持开闭端点、负周期索引、多解族排序去重、解数上限和数值精度保护；15 个筛选器测试与 3 个真实 solver 联动测试通过 Build #456（Run ID 43910484276）。
- **C5.16：** 新增辅助角公式型组合三角方程 `A·sin(ωx+φ)+B·cos(ωx+φ)=C)，使用 `hypot/atan2` 化为正弦方程并复用既有 solver；覆盖振幅边界、零振幅、非有限输入、溢出 / 下溢、相位精度和原式回代；15 个 JUnit 测试方法通过 Build #459（Run ID 43912093268）。
- **C5.17：** 新增 `sin(ax+b)=sin(cx+d)`，左右角系数与相位独立；按 `U−V=2kπ` / `U+V=(2k+1)π` 分解，复用 `SineEquationSolver` 和 C5.15 区间筛选器；覆盖恒等 / 无解、不同频率、负系数、非有限输入、溢出 / 下溢、超大相位精度保护及原式回代；16 个 JUnit 测试方法通过 Build #463（Run ID 44181614254）；PR #89 已 squash 合并到 main，合并提交 `03da7ae52239c609a3bd51f89c86c3cf97030e04`，最终 PR 头 Build #466（Run ID 44182197389）再次完整 success。
- **C5.18：** 新增 `sin(ax+b)=cos(cx+d)`，使用 `cos(V)=sin(π/2−V)` 转交 C5.17 的 `SineEqualityEquationSolver`，不重复实现周期求根；检查 `π/2` 相位平移精度，并对所有输出周期族回代原始正弦—余弦等式。新增 15 个 JUnit 测试方法；完整 Android Build #473（Run ID `44337799622`）通过全部 JVM 单元测试、Android 测试源码编译、Debug APK 组装、Artifact 上传与清理；PR [#90](https://github.com/PaleSquire71029/zhixingapp/pull/90) 已 squash 合并到 main，合并提交 `62b5ad2942b107891eb703b465bf4b8a4f9e4b6c`。初始 Build #472 的一个常数角精度测试失败，已修正测试夹具并增加防止误报恒等的回归测试。

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

截至目前，单位内核包含 **76 个线性单位定义 + 3 个温度仿射单位定义**，并提供统一的九类转换入口；C8.7 旧版有效输入兼容回归已通过。C8.8.1 已完成原生页面首版和基本输入 / 结果展示，仍待交互细节完善与真机 UI 验收。

---

## 当前计算器工作重点

当前执行顺序以 [CALCULATOR_UI_FIRST_ROADMAP.md](docs/CALCULATOR_UI_FIRST_ROADMAP.md) 为准：**先做界面和现有能力接入，后做数学扩展。**

1. **UI-0：** 盘点真实页面路由、Compose 组件、domain 接口与测试入口。
2. **UI-1–UI-3：** 完成计算主页、标准 / 科学输入、结果展示、错误分类和复制。
3. **UI-4–UI-7：** 完成工具目录、方程 / 不等式 / 函数页面，并接入当前已有 solver、函数拟合和采样能力。
4. **UI-8–UI-10：** 完成本地历史、增强题目编辑器、无障碍 / 性能和目标设备界面验收。
5. **M-0 以后：** 界面阶段稳定后，再统一接入注册表并补齐通用函数、方程、不等式及高中其他题型。

**明确暂缓：** C5.19 及更多零散方程变体；不为维持 C5.x 编号连续而扩展题型。单位换算页面的设备验收继续按用户此前要求暂缓，不阻塞计算主页开发。

## 已知未完成事项

- 计算 Tab 尚未完成完整原生计算器页面接入；C0–C8.7 的算法 / 数据层完成，C8.8.1–C8.8.2 完成独立单位换算工具，不等于完整计算器 UI 已完成。
- 专用数学输入 / QuestionDocument、完整结果过程展示与函数图像 Compose UI 仍需按 UI 优先路线逐步接入。
- 函数图表 Compose UI 尚未完成；目前完成的是拟合、定义域、求值和安全采样 domain。
- 化学配平、反应预测尚未完成 Android 原生迁移。
- 知行 AI Android 客户端、原生账号与云端历史尚未完成。
- 正式 Release 签名 / AAB 发布流程尚未完成。

---

## 构建与验收记录

- 单位换算页面首次构建验收：[Android Build #424](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43436816915)，主 Gradle 步骤 `BUILD SUCCESSFUL in 46s`。
- 文档链接校验基线：[Android Build #426](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43445595015)，15 份 Markdown 无本地断链。
- C5.17 代码验收：[Android Build #463](https://github.com/PaleSquire71029/zhixingapp/actions/runs/44181614254)，对应代码提交 `9928f593a476296d2957c24e0e8c4f54f396376e`；全部 JVM 单元测试、Android 测试源码编译、Debug APK 组装与 Artifact 上传通过。PR #89 合并到 main 的提交为 `03da7ae52239c609a3bd51f89c86c3cf97030e04`；最终 PR 头 [Android Build #466](https://github.com/PaleSquire71029/zhixingapp/actions/runs/44182197389) 也完整 success，Markdown 链接、资源、全部测试、APK 组装、Artifact 上传和清理均通过。
- C5.18 代码验收：[Android Build #473](https://github.com/PaleSquire71029/zhixingapp/actions/runs/44337799622)，PR #90 头提交 `e6ef6ecfd0b93daf2de1dad48092029a2254c232`；15 个新增 JVM 测试、全量 JVM 单测、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传及 Actions 清理均通过。PR #90 已合并到 main，合并提交 `62b5ad2942b107891eb703b465bf4b8a4f9e4b6c`。首轮 Build #472（Run ID `44337385602`）曾因一个不精确的常数相等测试夹具失败；修复后由 Build #473 完整复验通过。
- 最新 C5.17 Debug APK Artifact：[zhixingapp-debug / 14373092029（31,212,321 bytes）](https://github.com/PaleSquire71029/zhixingapp/actions/runs/44182197389/artifacts/14373092029)。CI 只编译 androidTest 源码，没有运行模拟器 / 真机 instrumentation tests。
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
10. [APK 首次真机验收清单](docs/APK_TEST_CHECKLIST.md)
11. [文档体系修复计划与权威规则](docs/DOCUMENTATION_REPAIR_PLAN.md)

开始下一步前，先检查 `main` 最新提交和上述文档；计算功能仍按小步实施，先保证正确性和测试，再接入 UI。

## APK 获取

- [C5.18 最新完整 CI 的 Debug APK Artifact（Build #473 / Run 44337799622 / Artifact 14441438575）](https://github.com/PaleSquire71029/zhixingapp/actions/runs/44337799622/artifacts/14441438575)
- [GitHub Actions 构建记录](https://github.com/PaleSquire71029/zhixingapp/actions/workflows/android-build.yml)
- [仓库归档 Debug APK](https://raw.githubusercontent.com/PaleSquire71029/zhixingapp/main/artifacts/zhixingapp-preview.apk)（归档包可能落后于最新源码；下载前先核对构建号和版本）

## 旧项目参考

- [知行计算器及 AI 后端](https://github.com/PaleSquire71029/calculator)
- [知行 AI 及项目交接文档](https://github.com/PaleSquire71029/zhixing_ai)

旧仓库只作为算法、接口、数据结构、字体和功能样例的参考。新 App 界面与交互使用 Android 原生实现，不把旧网页整体放进 WebView。
