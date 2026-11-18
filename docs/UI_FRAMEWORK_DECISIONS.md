# 知行·流序 UI 框架技术决策记录

> 决策日期：2026-11-19  
> 状态：F0 已冻结；后续如有新证据再通过单独决策记录调整  
> 关联：[UI_FRAMEWORK_AUDIT.md](./UI_FRAMEWORK_AUDIT.md) · [UI_FRAMEWORK_MIGRATION_MATRIX.md](./UI_FRAMEWORK_MIGRATION_MATRIX.md)

## 1. 决策摘要

| 领域 | 决策 | 本轮是否改代码 |
|---|---|---|
| Gradle 结构 | 保持单一 :app，先做包分层 | 否 |
| 设计语言 | 建立 Zhixing Flow，浅色、清晰、轻透、克制 | 否 |
| Glass | 保留 Backdrop 1.0.6 + Shapes 1.2.0，在其上建立知行 facade | 否 |
| 导航 | 暂不引入 Navigation 3；先整理轻量 typed route / AppState | 否 |
| 整页回弹 | F3 基于 Compose Foundation 自定义 OverscrollEffect 做独立原型 | 否 |
| 物理呈现 | 对整个内容 viewport 做刚性平移，不缩放、不拉伸 | 否 |
| 动画与性能 | 单一状态 owner；高频位移读在 graphics layer / draw 阶段 | 否 |
| 业务保留 | 单位换算 Presenter、DataStore、数学 domain 与已验收 Dock 保留 | 否 |
| 页面迁移 | 新旧页面并存，逐页替换、逐页验收 | 否 |

F0 是审计与决定，不是应用实现阶段；不得把以上技术方案描述为已开发。

## 2. 项目结构和职责

### 2.1 保持单模块

当前项目是一个 :app Gradle module，数学 domain 与 Compose UI 已有目录边界。现阶段拆多个 Gradle module 会增加构建与依赖管理成本，还不能直接解决 MainActivity 职责过载。

继续保持单模块，以 package 层次渐进整理：

ci.us.zhixing.app
- MainActivity.kt：最终只保留 Window + setContent
- domain：现有纯 Kotlin 算法，不随 UI 重建重写
- navigation：typed destination / route contract
- ui
  - theme：知行 Flow tokens + Theme
  - foundation：layout / motion / scroll policy
  - components：primitives + reusable patterns
  - shell：AppScaffold / fixed Dock / viewport
  - navigation：AppState / Back / page transitions
  - screens：calculator / AI / tools / units / my / history
  - glass：Backdrop adapter + glass implementation

该结构是逐步达到的目标，不要求一次性建立全部目录。迁移前不移动 domain 代码。

### 2.2 MainActivity 的最终责任

F4 完成后，MainActivity 只做：

- Android 系统窗口与 Edge-to-edge 初始化。
- 将依赖 / settings store 注入 App 根入口。
- 调用 setContent 并启动 Zhixing Flow Theme / App。

AppState、路由、Dock、页面容器、状态恢复和页面转场移入独立文件。F4 前不提前拆 MainActivity。

## 3. 导航决策：暂不使用 Navigation 3

当前只有 4 个 Tab 和 3 个单层二级页面，二级返回只需退出到对应根 Tab；没有深链、多层页面栈、多个独立返回栈或复杂结果传递。当前路由仅为字符串，确实需要在 F4 建立类型安全的 Destination / DetailRoute，但没有必要现在额外引入导航框架。

**当前决定：**

1. F1–F3 不引入导航库。
2. F4 先以轻量 sealed destination / route model 和一个 AppState 管理一级 Tab 与子页。
3. 使用明确的返回策略和 SaveableStateHolder 保留每个根 Tab / 子页状态。
4. Dock 是 App Shell 的固定 sibling overlay，不加入页面滚动 viewport。
5. 只有当出现多层嵌套流程、deep link、独立 back stack、复杂导航结果等真实需求时，再做 Navigation 3 POC。
6. 若做 POC，使用当时稳定版，不用 alpha；需要同时评估版本兼容、迁移成本和测试覆盖。

截至本决策日期，Navigation 3 已有稳定版，但“项目可用”不等于“当前产品需要”。减少不必要依赖优先于追求新框架。

## 4. 玻璃渲染决策：保留 Backdrop

当前项目已经依赖：
- io.github.kyant0:backdrop:1.0.6
- io.github.kyant0:shapes:1.2.0

仓库已保存上游 Apache-2.0 许可证文件。当前实现支持共享 Backdrop、实时背景采样、按 API 控制 blur / vibrancy / lens 能力，以及轻量 Surface fallback。

**决定不再换玻璃库，也不升级 Backdrop 2.x。** 先建立 Zhixing Flow 的材质 token、surface facade 和使用规则：

- 玻璃只用于 Dock、浮动工具条和少量浮层，不给每张内容卡片添加独立高成本模糊。
- 普通文字、数学公式、数字输入、图表刻度保持清晰，不做色散或影响阅读的折射。
- 保留九项材质设置和 Preferences DataStore；预览状态只在内存更新，提交 / 松手后再持久化。
- 保留 API 能力降级和 PREMIUM / STANDARD / LITE / OFF / CUSTOM 行为。
- 保持一个页面级共享 BackdropHost；后续统一 host owner 和关闭材质时的采样开销。

不增加 Lottie / Rive 或另一套玻璃效果依赖。高频交互优先使用 Compose 自己的动画和绘制能力。

## 5. 整页式回弹决策

这是用户明确提出的交互要求，必须按“刚性内容视口”实现，不能回到橡皮筋拉伸。

### 5.1 目标行为

- 正常滚动时，内容照常滚动。
- 顶 / 底到达边界后继续拖动，真实 scroll offset 不再越界；未消费的拖动量由整页 overscroll owner 接管。
- 内容 viewport 作为一个刚性整体，在 Y 方向有限平移；不得缩放、拉伸、变形或逐卡片独立位移。
- 松手或 fling 结束后，用具有速度连续性的 spring 回到 0。
- 用户反方向拖动时，先释放当前 viewport 位移，再恢复正常内容滚动。
- 顶部和底部行为镜像；fling 越过边界也必须由同一 owner 处理。
- Dock、系统栏、IME、固定操作区不跟随页面 viewport 平移。

### 5.2 首选技术路径

F3 首选 AndroidX Compose Foundation 的 OverscrollEffect，使用一个页面级实例协调 scroll / fling 未消费量；视觉偏移只应用在包住该页面可滚动内容的**单个外层 graphics layer**。

Compose Foundation 的 OverscrollEffect API 为自定义 overscroll 提供 scroll / fling 处理入口；API 细节与当前 Compose BOM 对齐，F3 以编译 POC 为准。若确认当前 BOM 下该 API 无法让独立 viewport 正确承载位移，再退回单一 NestedScrollConnection 实现。两套方案不能同时生效。

建议的职责拆分：

1. ScrollState 只保留真实内容滚动位置。
2. PageOverscrollState 只保留边缘位移、手势速度和当前方向。
3. 一个 ZhixingScrollContainer 负责 scroll effect、手势协调和回弹生命周期。
4. 内层页面只负责提供可滚动内容，不自行添加另一层 overscroll。
5. Dock 和固定区域与 viewport 同级布局，避免一起被平移。

### 5.3 物理参数与限制

- 原型最大位移起始范围：32–56dp；这是调试范围，不是最终设计值。
- 阻尼采用非线性衰减，接近上限时增量越来越小。
- 松手时保留手指释放速度，使用 spring 回归，不使用固定时间线性回位。
- 不对整个 viewport 做 scale，不改变 measured size，不增加 / 减少内容高度。
- 明确消费和交还手势的规则；一个页面只允许一个整页 overscroll owner。
- 禁止自定义效果与系统默认 stretch 同时显示，禁止逐 item 动画模拟整体回弹。

### 5.4 手势仲裁验收矩阵

| 场景 | 预期 |
|---|---|
| 顶部继续向下拖 | 整页 viewport 向下有限位移，松手整体回弹 |
| 底部继续向上拖 | 整页 viewport 向上有限位移，松手整体回弹 |
| 短拖 / 长拖 | 都受非线性阻尼和统一位移上限约束 |
| 高速 fling 越界 | 内容自然减速，overscroll 由同一 owner 接管并回弹 |
| 回弹中反向拖 | 手势可立即接管当前动画，不跳到旧目标 |
| 横向分类 Chips | 明确横向意图仍由横向列表处理；纵向意图交给页面 |
| Slider / Toggle | 原有拖动、长按和触摸反馈不受影响 |
| IME 弹出 / 收起 | 视口尺寸变化时不残留旧位移、不被键盘带偏 |
| Dock / 系统返回 | Dock 固定；返回手势不被 overscroll 抢占 |
| 未来函数图表 | 图表自己的缩放 / 平移手势有清晰优先级 |

### 5.5 性能实现原则

动画状态读取尽可能延迟到 Modifier lambda / draw / graphicsLayer { }，例如只更新 translationY；避免每帧把 offset 读进高层 Composition 或影响布局测量。graphicsLayer 不应改变内容实际尺寸，布局和触控坐标要保持一致。

F3 独立原型必须同时提供单元测试、Compose UI 手势测试与 iQOO Neo5 活力版真机验收。完成后才封装成通用 ZhixingScrollContainer。

## 6. 动画系统与性能规则

- 每个视觉状态对象只允许一个主动画状态 owner；避免多个 LaunchedEffect 对同一位移值竞争。
- 手势跟手阶段不启动追赶式动画；release 阶段再启动 spring。
- 状态变化需可打断和重入；第二次触摸从当前可见状态接管。
- 使用 graphicsLayer / lambda Modifier 延迟读取高频动画值；不要每帧触发大范围重组与布局。
- 不为玻璃面板各自创建背景快照；共享 host，并为低端档位提供成本更低的材质表现。
- 不增加持续播放但与操作无关的无限动画。
- 尊重系统减少动态效果设置。
- 避免将数学求解或高密度函数采样放入主线程。

## 7. F1 的精确改动边界

F1 只做 Foundation Tokens 与 Theme，不迁移页面和导航。

### 7.1 预计改动文件

现有文件：
- app/src/main/java/ci/us/zhixing/app/ui/theme/ColorTokens.kt
- TypeTokens.kt
- SpacingTokens.kt
- ShapeTokens.kt
- ZhixingGlassTokens.kt
- ZhixingTheme.kt

新增文件：
- ui/theme/ElevationTokens.kt
- ui/theme/MaterialTokens.kt
- ui/theme/MotionTokens.kt
- ui/theme/InteractionTokens.kt
- ui/theme/LayoutTokens.kt

新增测试：
- app/src/test/java/ci/us/zhixing/app/ui/theme/DesignTokenContractTest.kt

### 7.2 F1 实施规则

- 先按现有颜色、字体、圆角和 spacing 建立完整命名体系，不擅自更换色值、字体或页面观感。
- 收敛重复的 glass shape / motion token，但先保留旧属性访问兼容，避免旧页面突然编译失败。
- Material Token 只描述 Canvas / Soft Surface / Flow Glass / Overlay 等材质选择，不在 F1 修改 Glass 渲染器。
- Elevation、Layout、Interaction 和 Motion 有明确职责，不再将所有数值堆入一个对象。
- 单测覆盖 Token 的关键范围、稳定性、可读命名和兼容值；当前旧页面必须照常编译。
- 不修改 MainActivity、不迁移 Screens、不重写业务逻辑、不升级 / 新增 Gradle 依赖。
- F1 完成后跑完整 Android workflow；CI 通过才能进入 F2。

## 8. 开源方案总表

| 方案 | 使用方式 | 决策 |
|---|---|---|
| AndroidX Compose Foundation | Scroll / gesture / custom overscroll | 使用现有依赖；F3 POC |
| AndroidX Compose Animation | spring / AnimatedContent / state transitions | 使用现有 Compose Animation API，不增加替代动画库 |
| Material 3 | 基础组件语义、可访问性和行为参照 | 保留；样式由 Zhixing Flow 控制 |
| Kyant Backdrop 1.0.6 | Glass / backdrop sampling | 保留；不升级 / 替换 |
| Kyant Shapes 1.2.0 | 与 Backdrop 搭配的形状 | 保留 |
| Navigation 3 | 更复杂的导航栈 | 当前不引入，出现真实复杂栈需求后做稳定版 POC |
| Lottie / Rive | 复杂矢量 / 时间线动画 | 当前不引入；控件动画用原生 Compose |

外部技术参考（以实施时官方文档为准）：
- [Compose Foundation OverscrollEffect](https://developer.android.com/reference/kotlin/androidx/compose/foundation/OverscrollEffect)
- [Compose Animation performance](https://developer.android.com/develop/ui/compose/animation/quick-guide)
- [Navigation 3 releases](https://developer.android.com/jetpack/androidx/releases/navigation3)
- [Backdrop / AndroidLiquidGlass source](https://github.com/Kyant0/AndroidLiquidGlass)
