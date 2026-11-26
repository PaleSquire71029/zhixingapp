# 知行原生界面框架重建与设计语言规划

> 版本：1.0  
> 制定日期：2026-11-27  
> 当前基线：PR #92 的 docs/ui-first-calculator-roadmap 分支；main 仍保持 165a0ca2 基线，F0–F3 原型尚未合并  
> 目标设备：iQOO Neo5 活力版（竖屏优先，兼顾 60Hz / 120Hz）  
> 技术：Kotlin + Jetpack Compose + AndroidX  
> 设计方向：**知行·流序（Zhixing Flow）**  
> 核心决策：先重建 UI 框架，再迁移计算器与工具页面；不在旧页面上继续堆叠功能。

本文是 UI 框架重建的主规划。页面功能开发顺序仍见 [CALCULATOR_UI_FIRST_ROADMAP.md](./CALCULATOR_UI_FIRST_ROADMAP.md)，实际完成状态统一记入 [IMPLEMENTATION_STATUS.md](./IMPLEMENTATION_STATUS.md)。本规划与旧的计算器页面构想冲突时，以本文的框架层规则为准。

---

## 1. 为什么需要重建，而不是继续修补

### 1.1 当前代码结构的实际问题

对 PR #92 所基于的仓库代码进行初步检查后，发现：

1. `MainActivity.kt` 同时负责系统窗口配置、根状态、材质设置恢复、一级导航、二级页面路由、页面转场、页面内容和底部 Dock。它已经承担了太多职责，后续每增加一个页面都容易继续扩大这个入口文件。
2. 当前计算 Tab 仍使用通用介绍型占位布局，而不是最终计算器工作区；AI 等一级入口也还没有形成完整的业务页面。
3. 「我的」、工具目录、单位换算等页面各自维护滚动容器与间距规则；当前能看到多个 `verticalScroll(rememberScrollState())`，但没有统一的整页边缘滚动行为。
4. 现有 Color / Shape / Spacing / Type Tokens 已有基础，但尚未形成完整设计系统：缺少统一的组件规格、Motion Tokens、页面容器、层级规则、统一交互状态和统一滚动物理模型。
5. 玻璃材质、页面布局、过渡动画和局部交互尚未形成明确边界；新页面很容易复制已有 Composable，再逐渐出现圆角、间距、阴影、动画速度各不相同的问题。
6. 页面状态、导航状态、材质状态和业务状态目前分散在根组件及各页面中；需要把状态所有权、页面生命周期和返回恢复规则统一起来。
7. 若持续直接修改旧框架，容易形成更多条件分支和重复 UI。即使单页短期能显示，整体可维护性、帧稳定性和手势一致性仍会继续下降。

这些问题说明当前需要一次有边界的 UI 架构重建。重建不是推翻全部业务代码，也不是重写数学内核。

### 1.2 重建目标

最终需要一套可反复搭建新页面的原生 UI 系统，而不只是几张重新设计的页面：

- 有自己的视觉语言和 Design Tokens。
- 有一致的布局、卡片、面板、按钮、键盘、列表、输入和结果组件。
- 有统一的材质规则，玻璃效果不是到处复制的修饰符。
- 有统一的动画时序、弹簧参数、页面切换和控件反馈规则。
- 有统一的滚动、嵌套手势、边缘回弹和导航 Dock 协调机制。
- 有清晰的 App Shell、导航、页面状态、组件和业务逻辑分层。
- 有性能预算、自动化回归和目标设备验收流程。
- 后续增加数学、函数、化学、AI 页面时，只需组合既有组件和页面模板。

### 1.3 本次不做的事

- 不重写 C0–C8 数学内核，也不在本轮追加 C5.19。
- 不推翻已经移植并使用的 Backdrop 底层。
- 不把每个控件都做成高成本的模糊玻璃。
- 不先追求复杂动画数量；先建立统一运动规律，再按需要增加动效。
- 不在一个 PR 里一次性迁移整个应用。
- 不因为重建框架而强制拆分多个 Gradle module；继续保持单一 `:app`，先通过包和职责隔离。

---

## 2. 知行·流序设计语言

### 2.1 设计定位

**知行·流序（Zhixing Flow）** 是这次重建采用的内部设计系统名称。

目标不是仿制某个系统的截图，而是吸收 Apple 风格中克制的层次、连续的空间运动、材质响应和自然的物理反馈，再形成知行自己的颜色、形状、组件比例和交互特征。

设计关键词：

- **轻透：** 保持浅色和空气感，避免深色大块、厚重阴影和过度描边。
- **有序：** 页面层级清晰，常见操作的位置、间距、圆角和反馈保持一致。
- **连贯：** 页面、面板、键盘、导航和浮层之间的动效有共同节奏。
- **有触感：** 控件有按压、拖动、释放与状态变化反馈，但不夸张弹跳。
- **流畅：** 动画首先保证连续帧和触控响应，不为视觉效果牺牲操作速度。
- **可读：** 数学公式、数值、长文本和输入内容优先保证清晰，不能被玻璃折射扭曲。

### 2.2 视觉基础层

建立以下正式 Token 分类，不再在页面里大量硬编码 dp、颜色和动画时间：

| Token 类别 | 负责范围 |
|---|---|
| Color | 背景、文字、品牌色、语义色、分隔线、选中 / 错误状态 |
| Typography | 品牌标题、页面标题、分区标题、正文、辅助文字、数学文本 |
| Spacing | 页面边距、组件间隔、卡片内部间距、键盘间隔 |
| Shape | 面板、卡片、按钮、键帽、胶囊、弹层圆角 |
| Elevation | 视觉层次、柔和阴影和浮层高度 |
| Material | 透明度、模糊、边缘高光、折射、反射与降级档位 |
| Motion | 时长、缓动 / 弹簧、位移、缩放、淡入淡出与交互速度 |
| Interaction | 按压、选中、拖动、禁用、焦点、加载和错误反馈 |
| Layout | 内容最大宽度、安全区、页面固定区域、滚动视口与底部留白 |

### 2.3 颜色与表面

保持仅浅色的知行 Light 方向：

- 页面底色以柔和、低对比的浅色为主，可以有非常轻的环境色过渡，但不使用抢眼的动态彩色背景。
- 主强调色保留知行蓝系；只用于主要操作、当前选中、关键图表和重要状态。
- 普通内容面板以清晰、低噪声的浅色表面为主。
- 辅助区使用轻微色差或透明层次区分，不依赖重阴影。
- 错误、警告、成功分别使用稳定且克制的语义色；不让每个页面自行定义红 / 绿 / 黄。
- 文本、公式、图表刻度和小字号标签始终使用不折射的清晰渲染路径。

统一区分三种主要表面：

1. **Canvas：** 页面环境底色。
2. **Soft Surface：** 主要内容承载面，强调阅读、输入和信息清晰。
3. **Flow Glass：** 用在导航 Dock、浮动工具条、少量需要前后景关系的浮层与交互面板。

玻璃不是默认卡片材质。普通列表行、文字区和密集数学内容不应都执行独立的大模糊 / 折射 pass。

### 2.4 形状与比例

以统一 Token 控制圆角层级，以下是设计初始范围，最终值在组件样板和目标设备上校准：

| 层级 | 初始圆角范围 | 使用位置 |
|---|---:|---|
| 大型页面面板 | 26–32dp | 主结果面板、主要工作区 |
| 普通内容面板 | 20–26dp | 分组、工具区域、信息容器 |
| 控件 | 14–18dp | 按钮、输入框、选择器 |
| 小型键帽 / 标签 | 12–18dp | 数字键、运算符、标签 |
| 胶囊 | 完全圆角 | 导航 Dock、切换条和少量浮动操作 |

规则：

- 同一层级使用同一组半径，不让每个页面临时挑圆角。
- 面板与控件有尺寸层级，但不能所有元素都使用最大圆角。
- 触控热区可以大于视觉形状，不以视觉边界限制可点击区域。
- 不为了做出“液态玻璃感”而给所有组件加夸张外发光。

### 2.5 字体和数学内容

- 保留项目当前字体策略，不在框架重建中擅自替换字体族。
- 重新整理字号、行高、字重和数字对齐 Tokens。
- 计算结果、输入表达式和图表刻度要有专门的数学排版规则。
- 长公式允许横向浏览或在编辑区内定位，不挤压页面整体布局。
- 动画中的文字尽量只做必要的颜色 / 透明度 / 位移过渡，不持续缩放数学符号。

---

## 3. 组件体系与页面模板

### 3.1 组件层级

建立四层可复用组件：

1. **Foundation：** Tokens、主题、形状、绘制工具、Motion、滚动行为。
2. **Primitives：** Surface、Button、IconButton、TextField、Chip、Divider、进度反馈、空状态等基础组件。
3. **Patterns：** 页面标题区、分组列表、工具目录行、输入面板、结果面板、底部操作区、弹出面板。
4. **Screens：** 计算主页、方程、不等式、函数、单位换算、AI、历史和我的等页面。

页面不再复制完整布局和材质参数，而是组合 Patterns 与 Primitives。

### 3.2 第一批基础组件

| 组件 | 统一责任 | 关键状态 |
|---|---|---|
| ZhixingAppScaffold | 全局窗口、安全区、背景、内容层与浮动 Dock | 正常、键盘占位、子页面 |
| ZhixingScreen | 页面宽度、水平边距、垂直节奏和固定区域 | 初始、滚动、窄屏 |
| ZhixingScrollContainer | 单页滚动、手势仲裁、整页边界回弹 | 顶部边界、底部边界、拖动、回弹 |
| ZhixingSurface | 统一表面材质和形状 | 普通、柔和、玻璃 |
| ZhixingButton | 主 / 次 / 文字 / 图标操作 | 默认、按下、禁用、加载 |
| ZhixingInput | 输入框、焦点、错误和清除 | 空、输入、焦点、错误、禁用 |
| ZhixingSegmentedControl | 模式切换、筛选和选中胶囊 | 选中、拖动、快速切换 |
| ZhixingListRow | 列表行、图标、辅助文案和箭头 | 默认、选中、按压、禁用 |
| ZhixingResultPanel | 答案、辅助结果、错误和复制 | 成功、空、错误、不支持 |
| ZhixingSheet | 底部 / 居中浮层统一动画和遮罩 | 展开、收起、拖动 |
| ZhixingKeyboardKey | 数字、运算符和模板键的统一触控反馈 | 按下、长按、禁用 |

### 3.3 页面模板

所有二级页面尽量使用统一结构：

- 页面顶部：返回、标题、可选的右侧动作。
- 页面主体：输入区、主要内容、结果区或列表。
- 页面底部：必要时提供固定操作栏；滚动视口预留 Dock / 系统手势区空间。
- 页面状态：加载、空、编辑、成功、错误、不支持、无数据。
- 返回：保留合理的输入、筛选和滚动状态，不重新初始化整个页面。

工具页面保持功能导向，不使用网页式 Hero + 多排大卡片。

---

## 4. 动效系统：Motion Language

### 4.1 动效分层

建立统一 Motion Tokens，禁止每个页面自由组合动画时长：

| 动效类型 | 初始时间范围 | 典型场景 |
|---|---:|---|
| Micro | 80–140ms | 按键压下、图标颜色、轻量反馈 |
| Control | 140–220ms | 开关、胶囊、局部展开 |
| Content | 200–300ms | 结果替换、面板展开、局部布局变化 |
| Navigation | 240–360ms | 二级页面进入 / 返回、主要场景切换 |
| Settle | 由弹簧参数控制 | 拖动释放、边界回弹、胶囊归位 |

上表是初始调校区间，不是所有动画都必须使用相同固定时长。连续手势优先使用带速度连续性的弹簧，不用固定时长 tween 强行赶到目标位置。

### 4.2 动画规则

- 一个对象的一次状态变化只由一个主动画状态驱动，避免多个 LaunchedEffect 同时抢控制权。
- 用户手指直接操控的位移必须跟手；动画只负责手势结束后的稳定过渡。
- 页面切换不能闪白、重置 Dock 或丢失子页面状态。
- 按压反馈快进快出，不能拖慢按键连点。
- 结果变化要稳定，不允许整个结果区域每次输入都上下跳动。
- 动画播放期间再次输入或切页时，必须可中断并从当前视觉状态继续。
- 不增加无意义的永久循环动画；背景环境不持续占用高频帧。
- 尊重系统减少动态效果偏好；关闭或降低动效时仍保留状态可理解性。

### 4.3 页面转场

统一定义：

- 一级 Tab 切换：内容轻量淡入 / 位移，导航胶囊与目的地状态同步；不要让整页出现大幅缩放。
- 二级页进入：从当前页面自然推进，使用轻微横向或纵向位移 + 淡入，避免突然替换。
- 返回：方向与进入一致，支持系统返回和预测性返回手势。
- Sheet：从其锚定区域进入，遮罩透明度与面板运动同步。
- 局部内容展开：只动变化区域，不让整个页面重新播放进场动画。

### 4.4 动效验收

- 连续切换 Tab 不闪烁、不跳位置。
- 快速点击 / 连续输入不丢事件。
- 拖动中松手、反向拖动和中途再次按下都能平滑接管。
- 页面转场不引起明显掉帧。
- 低性能材质档位减少昂贵绘制，但布局与操作行为不改变。

---

## 5. 重点交互：整页式边界回弹

这是本次重建的明确需求，不接受默认橡皮筋拉伸作为最终效果。

### 5.1 目标观感

- 普通滚动时，内容自然连续上滑 / 下滑。
- 到达顶部或底部后继续拖动，页面内容作为一个完整、刚性的整体继续产生有限位移。
- 松手后，整个内容视图整体弹回正常边界。
- 内容内部的文字、卡片、按钮、图标不拉长、不压扁、不互相错位。
- 浮动底部导航 Dock 和系统状态栏 / 手势区保持稳定，不跟随内容整体移动。
- 不出现页面被放大、上下边缘被拉伸或卡片独立弹动的视觉效果。

### 5.2 具体交互规则

**底部边界：**

1. 用户向上滚动，页面正常移动到最大滚动位置。
2. 继续向上拖动时，不再改变真实 scroll offset，而是使整个页面内容视口整体向上偏移一小段距离。
3. 阻尼逐渐增大，越拉越难，不允许无限移动。
4. 松手或惯性滑动结束时，内容整体通过弹簧动画返回零位移。
5. 手指在回弹过程中重新接触页面，应从当前位移和速度接管，不能突然跳回。

**顶部边界：** 使用镜像逻辑，向下拉动时整个内容视口整体向下偏移，再以相同物理规则回弹。

**固定元素：**

- 页面内容整体回弹。
- 全局底部 Dock 固定，不随内容位移。
- 页面固定操作栏是否随内容移动由页面模板明确决定；默认固定操作栏不移动，内容区域整体回弹。
- 系统栏和系统手势区域永远不参与变换。

### 5.3 技术实现路线

优先建立自己的 `ZhixingWholePageOverscrollEffect`，基于 Compose Foundation 的滚动 / overscroll 扩展点实现，不依赖一个无法控制动画和边界规则的现成橡皮筋组件。

建议结构：

- `ZhixingScrollContainer` 管理唯一滚动状态和 overscroll 状态。
- `ZhixingWholePageOverscrollEffect` 接收未被滚动容器消费的 delta 与 fling 速度。
- 由单一状态维护边缘位移、方向、手指速度和回弹状态。
- 在页面内容视口的最外层图形层应用统一 translation；不要对每张卡片分别添加 translation。
- 使用 Compose 的 `graphicsLayer { }` 等延迟读取方式消费高频动画值，避免每帧触发大规模 Composition / Layout。
- 默认系统视觉 overscroll 必须关闭或替换，防止自定义整页回弹和系统拉伸叠加。
- 同一个页面只能有一个整页 overscroll owner；嵌套列表、横向分页、键盘区域和图表手势必须有明确仲裁规则。

初始参数由原型实测决定，不在未测试前锁死：

- 最大整页位移先测试约 32–56dp 区间。
- 使用非线性阻尼，靠近最大位移时增长明显变慢。
- 回弹采用带速度连续性的 Spring，阻尼以一次稳定归位为目标，不做多次大幅弹跳。
- 不缩放、不改变内容高度、不使用弹性 mesh 或 per-item stretch。

如果 Compose 当前版本中的 `OverscrollEffect` API 无法完整满足整页视口变换，则评估自定义 `NestedScrollConnection`；仍然只保留一个 overscroll 状态和一个整体图层，不要混用两个独立物理系统。

### 5.4 必须处理的手势冲突

- 页面纵向滚动与横向 Tab / Pager。
- 页面滚动与横向滑块。
- 页面滚动与开关长按。
- 触底回弹与底部 Dock 手势。
- 图表拖动 / 缩放与外层纵向滚动。
- 输入法弹出、键盘收起与页面滚动。
- 系统返回预测手势与页面拖动。
- 快速 fling 到边界后继续反向拖动。

### 5.5 回弹验收用例

- 短拖触底后释放：整体回弹。
- 长拖触底：位移被限制，不发生内容拉长。
- 高速 fling 触底：保持连续，不突然停住或反向跳跃。
- 触底后手指反向拖动：先释放回弹位移，再恢复正常滚动。
- 页面内容很少、不足以滚动：不产生伪回弹。
- 内嵌横向控件 / 图表：不被错误地转成纵向回弹。
- 固定 Dock 不移动、不闪烁、不遮挡最后一项。
- 顶部与底部行为对称。
- TalkBack、系统字体放大和减少动态效果场景下仍可使用。

---

## 6. App Shell 与代码架构重整

### 6.1 目标职责

`MainActivity` 只保留系统窗口配置和 Compose 根入口；不再承载页面内容与业务路由细节。

建议的包结构（按职责，不按过度拆模块）：

```text
app/src/main/java/ci/us/zhixing/app/
  MainActivity.kt
  app/
    ZhixingApp.kt
    ZhixingAppState.kt
    AppEnvironment.kt
  navigation/
    AppRoute.kt
    AppNavHost.kt
    AppTransitionSpec.kt
    AppBackHandler.kt
  ui/
    foundation/
      theme/
        ColorTokens.kt
        TypographyTokens.kt
        ShapeTokens.kt
        SpacingTokens.kt
        ElevationTokens.kt
        MotionTokens.kt
        MaterialTokens.kt
      motion/
        ZhixingMotion.kt
        ZhixingTransition.kt
      scroll/
        ZhixingScrollContainer.kt
        ZhixingWholePageOverscrollEffect.kt
        OverscrollState.kt
      layout/
        ZhixingScreen.kt
        ZhixingAppScaffold.kt
    components/
      surface/
      buttons/
      inputs/
      controls/
      lists/
      sheets/
      navigation/
      feedback/
    feature/
      calculator/
      equation/
      inequality/
      function/
      units/
      tools/
      ai/
      history/
      my/
    state/
      UiResult.kt
      UiMessage.kt
```

这是目标结构，不代表所有目录都要在第一步一次性建空文件。每个目录随真实组件出现再创建。

### 6.2 状态所有权

- App Shell：一级导航、子页面栈、全局材质设置、窗口 / 系统栏。
- Screen ViewModel：本页输入、选择、计算状态和短期 UI 状态。
- Domain：数学、单位和其他业务结果，不依赖 Compose。
- Data：本地持久化和未来网络数据。
- Component：只持有必要的短暂手势状态，不持有业务模型。
- 不能把整个应用的几十个状态提升到 MainActivity，也不能把导航状态藏在一个大型 Composable 内。

### 6.3 页面状态恢复

- Tab 切换保留合理的页面状态。
- 二级页面返回时恢复输入、选择、筛选和滚动位置。
- 进程重建后，使用 Saveable / ViewModel / DataStore 等合适工具恢复该恢复的状态。
- 临时动画进度不写入持久化。
- 旧页面迁移过程中不能破坏单位换算的输入、选择和复制语义，也不能删除材质设置持久化。

---

## 7. 开源项目与技术选择

采用“成熟基础库 + 知行自有适配层”的方式。开源库负责可靠的底层能力，知行负责统一视觉、交互和产品规则。

| 项目 / 技术 | 规划用途 | 决策 |
|---|---|---|
| AndroidX Compose Foundation | 滚动、Nested Scroll、指针输入、布局和自定义 overscroll 扩展 | 作为主框架，优先用官方能力实现整页回弹 |
| AndroidX Compose Animation | Spring、AnimatedContent、AnimatedVisibility、过渡和动画状态 | 作为唯一主要动效引擎，不额外引入多个动画库 |
| Material 3 | 无障碍语义、交互行为和必要基础控件 | 保留行为基础；视觉由 Zhixing Flow tokens / components 控制 |
| Kyant AndroidLiquidGlass / Backdrop 1.0.6 | 背景采样、玻璃渲染、折射与材质能力 | 当前项目已经移植并使用；保留底层，外面建立 Zhixing Glass 适配层 |
| AndroidX Navigation 3 | 类型化路由、返回栈、状态恢复、预测性返回 | 作为候选进行最小 POC；确认能替代当前自定义路由后再决定采用，不因“新”而强制迁移 |
| Lottie / Rive 等动画运行时 | 特定品牌演示或复杂插画动画 | 不作为常规按键、页面切换、滚动回弹的引擎，避免给高频交互增加运行时负担 |

### 7.1 当前已验证的开源基础

- `app/build.gradle.kts` 已依赖 `io.github.kyant0:backdrop:1.0.6` 与 `io.github.kyant0:shapes:1.2.0`。
- 仓库内已有官方 Backdrop Catalog 1.0.6 相关移植代码和第三方许可证记录。
- 不重复移植、不在本次框架重建时替换已存在的 Backdrop 核心。
- 新增开源依赖前先检查许可证、维护状态、Android 兼容、包体积和性能；在 `THIRD_PARTY_NOTICES.md` 记录依赖来源与许可证。

### 7.2 Navigation 3 评估要求

当前一级目的地列表和二级页面返回状态主要由根 Compose 逻辑管理。Navigation 3 有稳定版可评估：

- 先在独立分支建立只有两个页面的最小 POC。
- 对比当前导航的页面状态保留、返回栈、快速连续 push / pop 和预测性返回。
- 只使用稳定版；不直接采用 1.3 alpha。
- 若 Navigation 3 的复杂度或包体积不值得，保留轻量自有路由状态机，并补齐相同测试。
- 不在框架重建的第一步直接替换导航。

### 7.3 不引入的依赖

- 不为了一个普通弹簧动画引入完整动画运行时。
- 不并列采用多个负责导航的库。
- 不重复引入已经由 Compose Foundation 提供的手势 / 滚动基础功能。
- 不用无人维护的自定义 overscroll 库作为整页回弹核心。
- 不为每种玻璃控件另加一个互不兼容的视觉库。

---

## 8. 性能与流畅性标准

### 8.1 性能原则

- 高频动画优先使用 `graphicsLayer` / lambda Modifier 延迟读取，减少 Compose 重组。
- 滚动帧路径中不执行数据库、网络、数学求解或昂贵的状态转换。
- 背景模糊和折射集中在少数明确的玻璃层，不给每一个小按钮各开一次重效果。
- 列表使用稳定 key，避免项目重排时重复创建大量昂贵组件。
- 页面状态与动画帧状态分离。
- 资源加载、复杂图表采样和计算放到可取消的后台任务。
- 不使用永久无限动画掩盖性能问题。
- 所有性能档位的业务结果一致，只改变昂贵视觉效果。

### 8.2 目标设备帧预算

目标设备支持高刷新率，但不假设系统一定持续授予最高刷新率。

- 120Hz 的单帧预算约 8.33ms；60Hz 约 16.67ms。
- 连续滚动、整页回弹、底部 Dock 移动和键盘连续点击分别测量。
- 同时检查冷启动、从后台返回、玻璃材质首次加载和页面切换。
- 不能只看平均帧率；需要关注长帧、帧时间分布和用户感知卡顿。
- 如果高级折射影响稳定性，优先减少折射采样或降低材质档位，不削弱基础触控和主转场。

### 8.3 不允许的性能做法

- 每帧改变大量 Composable 参数导致整页重组。
- 对同一页面同时运行多个相互竞争的动画循环。
- 滚动时不断创建 / 销毁 Backdrop Host。
- 每个列表项重复进行全尺寸背景模糊。
- 为视觉效果持续绘制与用户交互无关的动画。
- 将数学求解和图表高密度采样放到主线程。

---

## 9. 分阶段执行：框架先行

整体分成 F0–F7。每一小步都单独 PR，CI 通过后再进入下一步。

| 阶段 | 名称 | 工作范围 | 必须交付 |
|---|---|---|---|
| F0 | 现状审计与边界冻结 | 页面 / 路由 / 滚动 / 玻璃 / 状态 / 依赖盘点；记录当前构建和测试基线 | UI 资产清单、问题清单、目标结构图、开源决策记录 |
| F1 | Zhixing Flow Foundations | 完整 Tokens：颜色、字体、间距、形状、层级、材质、Motion、Interaction | 新主题入口和设计规范；旧页面仍能运行 |
| F2 | 核心组件库 | Surface、Button、Input、ListRow、SegmentedControl、Sheet、ResultPanel 等 | 可预览、可测试的组件目录和样例页 |
| F3 | Whole Page Overscroll | 自定义整页式顶部 / 底部回弹原型、手势仲裁、弹簧归位 | 独立滚动演示页、单测 / UI 测试、目标设备验收 |
| F4 | App Shell 与导航 | 拆分 MainActivity、统一 Shell / Nav / 状态恢复 / 页面转场 | 新旧导航功能对照通过的 App Shell |
| F5 | 页面迁移基座 | 先迁移工具目录、我的 / 历史、单位换算；保持业务逻辑与已有材质设置 | 代表性页面在新框架下运行 |
| F6 | 核心页面重建 | 计算主页、AI 入口、方程 / 不等式 / 函数页面逐个接入 | 计算器 UI 主流程与工具工作区 |
| F7 | 统一验收与清理 | 性能、无障碍、窗口适配、回归、旧组件移除 | 新框架完成记录和迁移清单 |

### 9.1 F0：现状审计与边界冻结（已完成静态代码审计）

**完成日期：2026-11-19。** 本次审计以真实 main 源码为准，而非以本规划中的预期路径推断实现。

**基线与验证：**

- 审计代码基线：main / 165a0ca2ff77aeae0868d8dbd2087c09cd9e6260。
- 规划分支：docs/ui-first-calculator-roadmap；PR [#92](https://github.com/PaleSquire71029/zhixingapp/pull/92)，当前仍 open、未合并。
- 审计前最近规划提交：bf203cba582bd217d7bdb266901c5f61d44549b6；Build #482 / Run ID 44346557259 已成功。该 Build 对应此前规划提交，不代表本次 F0 文档新提交的 CI。
- F0 只新增 / 更新文档，不修改 Kotlin、Gradle、Manifest、资源或依赖。

**本次已完成的静态审计：**

1. 列明四个一级 Tab 与三个真实二级目的地，确认计算、AI 仍为占位内容；单位换算、历史空状态和材质设置已有真实页面。
2. 对 UI 源文件及 GitHub 代码搜索逐项核对滚动：6 处纵向 verticalScroll、1 处横向 horizontalScroll；未发现 LazyColumn / LazyRow、NestedScrollConnection、Modifier.nestedScroll、OverscrollEffect 或自定义 Modifier.overscroll。
3. 盘点主壳中的 BackdropHost、共享玻璃 surface、玻璃按钮 / 滑块 / 开关、材质配置、Android API 降级和性能档位。
4. 核对九项材质属性、Preferences DataStore 的键与读写时机；确认滑块拖动采用内存预览、松手提交，避免每帧写盘。
5. 核对颜色 / 字体 / 间距 / 形状 / 玻璃动效 Tokens 与页面硬编码尺寸；确认仍缺 Elevation、Material、Layout、完整 Motion / Interaction Tokens。
6. 核对 rememberSaveable、SaveableStateHolder、二级页 BackHandler、单位换算输入状态及 IME / Insets 的不同处理方式。
7. 核对单模块 Compose 依赖、Backdrop / Shapes 版本、第三方许可证、JVM / androidTest 测试资产和 CI 实际执行范围。
8. 明确用户已验收的 Dock / 材质控件行为是保留约束；新整页回弹尚不存在，不能将其描述为已实测。

**交付文档：**

- [UI_FRAMEWORK_AUDIT.md](./UI_FRAMEWORK_AUDIT.md)：源码事实、页面 / 路由 / 滚动 / 玻璃 / 状态 / 依赖 / 测试清单。
- [UI_FRAMEWORK_DECISIONS.md](./UI_FRAMEWORK_DECISIONS.md)：Navigation 3、Backdrop、整页回弹、状态边界和 F1 技术决策。
- [UI_FRAMEWORK_MIGRATION_MATRIX.md](./UI_FRAMEWORK_MIGRATION_MATRIX.md)：现有页面、目标页面和必须保留的行为映射。

**静态审计边界：**

GitHub 源码审计无法替代 iQOO Neo5 活力版真机观察。本次没有伪造当前滚动手感、掉帧数据或截图。已有 Dock / 材质滑块 / 开关真机验收作为回归保护；整页式回弹需到 F3 有原型和 APK 后，再测顶部 / 底部、fling、反向拖动、IME、嵌套控件以及 60Hz / 120Hz。

**F0 结论：**

- F1 已完成并提交到 PR #92 分支；当前可进入 F2，不需要继续做整体架构探索。
- F1 只完善 Design Tokens 和主题契约；不改 MainActivity、不迁移页面、不加导航库、不引入新的玻璃渲染依赖。
- F3 单独验证整页回弹物理；F4 再拆分 App Shell；F5 起逐页迁移。

### 9.2 F1–F2：Foundation 和组件样板

#### F1：Zhixing Flow Foundations（已完成，Build #488 success）

F1 已在 PR #92 的规划分支完成实现；当前分支 head `74160abbeaa8002a96ded0d6eff7932e2f6400ff` 的 Build #488（Run ID `44404842628`）完整 success。新增 Elevation、Material、Motion、Interaction、Layout 五组 Token，并将现有 Color、Typography、Spacing、Shape 归并为九组统一入口。

- `ZhixingTheme` 保留原有 `colors / type / spacing / shapes` 访问方式，并新增 `palette / typography / spacingTokens / elevation / material / motion / interaction / layout`。
- 现有颜色值、文字字号 / 行高 / 字重 / 字距、默认字体回退、间距、28dp / 18dp 首页形状和 12–32dp 玻璃半径均保持不变。
- `ZhixingGlassRadii / ZhixingGlassShapes / ZhixingGlassMotion` 作为兼容 facade，底层值统一委托给 Shape / Motion Tokens；现有 Backdrop 组件无需迁移。
- Color 保留现有品牌 / 表面 / 文字 / 描边值，补齐 Material 3 error / tertiary 默认语义角色和既有 0.12 选中容器色；未臆造 success / warning 色。
- Elevation 只集中记录当前 4dp / 6dp / 10dp 阴影半径；Material 只定义 Canvas / Soft Surface / Flow Glass / Overlay 角色与阅读优先策略，不接管玻璃渲染。
- Interaction 覆盖 IDLE / PRESSED / SELECTED / DISABLED / FOCUSED / LOADING / ERROR 七态、48dp 触控热区、2dp 焦点描边、0.12 pressed / selected alpha、0.38 disabled alpha；Motion 收录 120 / 180 / 220 / 280ms 时序和既有 spring；Layout 记录 68dp Dock 与固定层 / 单一 viewport 的边界约束。
- 新增 `DesignTokenContractTest`，包含 9 项 JUnit 测试，覆盖九类 Token、旧访问器兼容、关键值稳定性和主题入口；未增加 Gradle 依赖。
- Build #488 完整 success：全部 JVM 单元测试、Android 测试源码编译、Debug APK 组装和 Artifact 上传通过；仅编译 androidTest 源码，不代表设备 / 真机执行。
- F1 不修改 MainActivity、Screens、导航、GlassMaterialState / DataStore、玻璃渲染算法或性能档位；没有执行真机视觉验收。

#### F2：核心组件库与 Design Gallery（已完成，2026-11-21）

- 已新增 `ZhixingSurface`、`ZhixingButton`、`ZhixingInput`、`ZhixingListRow`、`ZhixingSegmentedControl`、`ZhixingSheet`、`ZhixingResultPanel` 七个基础组件。
- 已新增独立 `DesignGalleryScreen`，390dp / 720dp Preview；600dp 作为单列 / 双列断点；不注册 MainActivity / 导航。
- 组件状态覆盖按压、选中、禁用、错误、加载和焦点；提供 Button / RadioButton / Heading / TextField 错误语义。
- 新增 2 项 JVM 布局阈值测试与 5 项 Compose UI 交互 / 语义测试。Build #499 完整 success；CI 执行 JVM 测试、编译 androidTest 源码并组装 Debug APK，但不启动模拟器运行 instrumentation tests。
- 只消费 F1 Tokens；不改现有页面、MainActivity、导航、玻璃算法、材质设置、性能档位或 Gradle 依赖。旧页面继续运行，等待 F5 逐页迁移。


### 9.3 F3：整页回弹原型

**实现状态（2026-11-27）：独立代码原型已提交并通过 Build #506；目标设备验收未完成。**

- `ZhixingRigidOverscrollPhysics` 提供像素级阻尼、位移夹取、反向手势释放、fling 起始投影和无滚动范围判定。
- `ZhixingRigidOverscrollEffect` 使用一个 Compose Foundation `OverscrollEffect` owner；`ZhixingPageScrollContainer` 保持真实 ScrollState 不越界，只在单个外层 graphicsLayer 设置 translationY，最大位移 48dp。
- 松手 / fling 后使用现有 Motion Spring Token 回位；内容不够滚动时不制造回弹；横向 delta 不由页面效果吞掉。
- `ZhixingScrollPrototypeScreen` 是独立长内容实验页；Debug-only `ZhixingScrollLabActivity` 通过 `zhixing://scroll-lab` 打开，不接入正式导航 / MainActivity，Release 不包含该入口。
- 测试：8 项 JVM 物理 / 边界单测，3 项 Compose 手势 / viewport androidTest 源码。Build #506（Run ID `45859680761`）通过全部 JVM 测试、Android 测试源码编译、Debug APK 组装与 Artifact 上传；CI 未启动模拟器执行 androidTest。
- 使用独立演示页面，不在所有业务页面上直接实验。
- 分别验证顶端、底端、短拖、长拖、高速 fling、反向拖动和嵌套控件。
- 先确保内容整体位移与回弹正确，再调阻尼、最大位移和弹簧。
- 目标设备验收通过后，将其封装进统一 ZhixingScrollContainer。
- 明确禁止页面拉伸、卡片独立缩放和默认 Android stretch 与自定义位移叠加。

### 9.4 F4：App Shell / Nav 重建

- MainActivity 缩减为系统窗口配置和 setContent。
- 统一 AppState、目的地、子页面栈、预测性返回和状态恢复。
- 底部玻璃 Dock 作为独立导航层，不进入页面滚动图层。
- 页面只向 Shell 提供内容和必要状态，不再持有全局导航实现。
- 对照旧行为验证四个一级 Tab、历史、材质、单位换算及系统返回。

### 9.5 F5–F6：逐页迁移

建议顺序：

1. 工具目录 / 我的 / 历史：先验证列表、统一滚动和返回状态。
2. 单位换算：验证已有输入、单位选择、交换、复制、窄屏和 TalkBack 语义不回退。
3. 计算主页：使用新组件库实现标准模式、输入区和主键盘。
4. 科学模式与结果展示。
5. 方程、不等式和函数页面。
6. 知行 AI 页面与其他独立功能。

迁移时旧页和新页短期并存，通过路由切换验收；一个页面完全通过后再删除旧实现，不同时重写所有页面。

### 9.6 F7：整体收尾

- 核对所有旧组件是否仍被引用；有替代和测试后再删除。
- 清理重复 Tokens、硬编码动画和多套滚动状态。
- 检查窄屏、大字号、横屏（若页面支持）、系统键盘和系统返回。
- 检查 Dock 覆盖、系统安全区和底部最后一项可见性。
- 目标设备执行 60Hz / 120Hz、连续输入、长列表、滚动回弹和转场验收。
- 更新 README、IMPLEMENTATION_STATUS、项目交接指南与本规划。

---

## 10. 质量门槛与验收矩阵

| 维度 | 自动化 / 静态检查 | 目标设备检查 |
|---|---|---|
| 设计 Tokens | Token 引用 / 主题预览 / 编译检查 | 不同页面观感一致 |
| 组件 | Compose UI 状态、点击、语义测试 | 手感、可读性和误触 |
| 动效 | 状态转换、打断 / 重入测试 | 过渡连续、无跳帧 |
| 整页回弹 | 边界、阻尼、方向和状态单测；Compose 手势测试 | 顶底触边、回弹、反向接管 |
| 导航 | route / back stack / state restoration 测试 | 连续切 Tab、返回、预测性返回 |
| 玻璃材质 | API 降级与状态测试 | 真实背景采样和性能 |
| 输入 | 编辑、焦点、键盘、恢复测试 | 连续输入、系统 IME 和单手操作 |
| 性能 | 基准测试、长帧采样 | 120Hz / 60Hz 主观流畅度 |
| 无障碍 | TalkBack 语义、焦点和点击目标 | 实际播报和焦点顺序 |

CI 通过不等于真机验收通过。玻璃光学效果、整页回弹手感和 120Hz 流畅度必须目标设备检查。

---

## 11. 风险与应对

| 风险 | 应对 |
|---|---|
| 一次性替换导致所有页面回归 | 新旧框架并存，按页面迁移，逐步切换 |
| 自定义 overscroll 与内层手势冲突 | F3 独立原型先测试；统一手势仲裁 |
| 玻璃效果拖慢低端设备 | 玻璃层数预算、性能档位和低成本回退 |
| Navigation 3 引入复杂度 | 先 POC，对比现有路由，不合适就不采用 |
| 组件库过度抽象 | 先从实际页面抽取，不提前建大量空泛组件 |
| UI 重建影响数学内核 | UI 层重构，domain 包和 API 保持稳定 |
| 目标设备帧率波动 | 60Hz / 120Hz 分别验证，优先消除长帧 |
| 旧组件清理过早 | 新页面通过回归和真机验收后才删除 |

---

## 12. 当前执行边界与下一步

截至 2026-11-27：

- F0 静态审计、F1 九类 Tokens / Theme 契约、F2 七个基础组件与 Design Gallery 已完成。
- F3 独立代码原型已提交：`ZhixingRigidOverscrollPhysics`、`ZhixingRigidOverscrollEffect`、`ZhixingPageScrollContainer`、`ZhixingScrollPrototypeScreen`；Debug-only `ZhixingScrollLabActivity` 使用 `zhixing://scroll-lab` 深链打开。
- F3 当前实现使用一个 Foundation OverscrollEffect 和一个外层 graphicsLayer；真实 ScrollState 不越界，页面只作最多 48dp 的 Y 轴刚性平移，不缩放 / 拉伸；反向手势先释放位移；无滚动范围时不产生回弹。
- 新增 8 项 JVM 物理 / 边界测试和 3 项 Compose 手势 / viewport 测试。Build #506（Run ID `45859680761`）完整 success；Debug Artifact [15138440787](https://github.com/PaleSquire71029/zhixingapp/actions/runs/45859680761/artifacts/15138440787)，31,319,661 bytes。
- 当前 CI 只编译 androidTest 源码，不运行模拟器 instrumentation tests；iQOO Neo5 活力版的顶底边界、快速 fling、反向接管、IME、嵌套控件、60Hz / 120Hz 实测仍待完成。
- PR #92 仍 open、未合并；main 仍为 `165a0ca2ff77aeae0868d8dbd2087c09cd9e6260`。
- Backdrop 1.0.6、Shapes 1.2.0、玻璃控件与 DataStore、材质设置、性能分级、单位换算、数学 Domain 和已验收 Dock 保持不变。
- F3 不修改 MainActivity、正式导航、现有业务页面、玻璃算法或 Gradle 依赖；F3 完成设备验收后再进入 F4 / F5。

**下一步：** 在目标设备打开 Debug APK 的 Scroll Lab，检查顶部 / 底部短拖与长拖、高速 fling、回弹中反向接管、横向 Chips / Slider / Switch / 输入框、IME 弹出收起、固定 Header / Footer、60Hz / 120Hz 帧感与 Dock / 系统栏是否保持独立。根据真机表现微调阻尼 / 弹簧，再决定是否封装为正式 `ZhixingScrollContainer`。不提前迁移页面。