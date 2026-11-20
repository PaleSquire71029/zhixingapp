# 知行 UI 框架 F0 现状审计

> 审计日期：2026-11-19  
> 仓库：PaleSquire71029/zhixingapp  
> 审计基线：main / 165a0ca2ff77aeae0868d8dbd2087c09cd9e6260  
> 规划分支：docs/ui-first-calculator-roadmap  
> 规划 PR：[ #92 — Zhixing Flow UI framework rebuild](https://github.com/PaleSquire71029/zhixingapp/pull/92)  
> 审计范围：Android Kotlin / Compose 源码、Gradle 配置、Manifest、测试源和 CI 工作流  
> 结论：**F0 静态审计完成；F1 可开始。应用源码未修改。**

## 1. 审计结论摘要

当前工程并非“没有 UI”，而是已有一个可运行的原生壳和若干独立功能页面，但页面架构还没有形成统一框架：

- 一级导航有四个入口：计算、知行 AI、工具、我的。
- 计算与 AI 仍是展示介绍文字的占位内容；并没有端到端计算工作区或 AI 对话工作区。
- 工具目录目前只开放单位换算；单位换算已具备真实的输入、类别 / 单位选择、交换、即时结果和复制流程。
- 我的页面含最近使用空状态、历史入口、材质设置入口和应用版本信息。
- 历史页是明确的空状态；尚未接入本地历史存储。
- 材质设置页可以操作玻璃效果参数，参数会写入 Preferences DataStore。
- 主壳的玻璃 Dock 已单独实现；其行为与材质控件已有用户设备验收记录，应列为重构保护项，而不是本轮推翻对象。

最直接的框架问题是：MainActivity.kt 同时拥有窗口设置、全局 UI 状态、DataStore 订阅、路由、返回处理、页面转场、页面选择、Backdrop host 和 Dock 布局；不同页面各自声明滚动容器和 Insets 规则。现在开始新增页面会继续加重主入口和页面间的不一致。

## 2. 工程与技术基线

| 项目 | main 当前事实 |
|---|---|
| Gradle 模块 | 单一 :app，settings.gradle.kts 只 include :app |
| Android Gradle Plugin | 9.1.1 |
| Kotlin Compose 插件 | 2.4.21 |
| Gradle Wrapper | 9.3.1 |
| Compose BOM | 2026.06.00 |
| Activity Compose | 1.13.0 |
| Material | Material 3 |
| Backdrop | io.github.kyant0:backdrop:1.0.6 |
| Shapes | io.github.kyant0:shapes:1.2.0 |
| DataStore | Preferences DataStore 1.3.0 |
| compileSdk / targetSdk | 36 / 36 |
| minSdk | 26 |
| Java source / target | 17 |
| Manifest | 单个 launcher MainActivity；windowSoftInputMode=adjustResize |

其他依赖包括 Symja 3.2.0 及为 Android 运行时选择的 Guava / Jackson / Log4j API 版本。当前没有直接加入 Navigation Compose / Navigation 3 依赖，也没有单独的动画框架依赖。

**结构决策：** 继续保留单一 Gradle module；先在 ui 包内建立职责边界，不为 UI 重建增加过早的多模块构建复杂度。

## 3. 页面与路由清单

### 3.1 一级导航

| route | 显示名称 | 当前实现 | 实际状态 |
|---|---|---|---|
| calculator | 计算 | MainActivity.kt 的通用介绍布局 | 占位；不是计算器 |
| ai | 知行 AI | MainActivity.kt 的通用介绍布局 | 占位；没有对话输入 / 消息流 |
| tools | 工具 | ToolsDirectoryScreen | 真实目录；当前只列出单位换算 |
| my | 我的 | MyScreen | 真实页面；最近使用为空状态，提供二级入口 |

一级导航数据位于 navigation/AppDestination.kt 的 AppDestinations.all。当前四个 Tab 的标题、图标、介绍文字与 route 都由同一数据列表提供。

### 3.2 二级页面与打开 / 返回路径

| 二级目的地 | 入口 | 页面实现 | 返回方式 / 状态 |
|---|---|---|---|
| unit-converter | 工具 → 单位换算 | ui/units/UnitConverterScreen.kt | 页面回调 onBack 清除 mySubpage；输入和选择使用 rememberSaveable |
| history | 我的 → 历史记录 | ui/history/HistoryScreen.kt | onBack 清除子页状态；当前无历史持久层 |
| material | 我的 → 外观 / 材质设置 | ui/lab/GlassLabScreen.kt | onBack 清除子页状态；设置保存至 DataStore |

目前二级目的地保存在一个可空字符串 mySubpage 中，使用 BackHandler(enabled = mySubpage != null) 处理返回；AnimatedContent 负责页面淡入、水平位移和轻微缩放。它不是完整的导航栈，没有深链或多层返回栈。

MainActivity.kt 使用 rememberSaveable 保存一级 tab / 子页字符串，并以 rememberSaveableStateHolder 为根 Tab 和三个二级页面提供独立状态保存容器。

### 3.3 当前页面树

MainActivity
- Android Window / Edge-to-edge / adjustResize
- ZhixingApp
  - 一级 Tab state / 子页 state / SaveableStateHolder
  - GlassMaterialSettingsStore Flow + preview / persist
  - ZhixingTheme
  - AnimatedContent(mySubpage)
    - material → GlassLabScreen → BackdropHost
    - history → HistoryScreen → BackdropHost
    - unit → BackdropHost → UnitConverterScreen
    - root pages
      - BackdropHost
      - calculator / ai placeholder
      - tools → ToolsDirectoryScreen
      - my → MyScreen
      - fixed ZhixingCatalogBottomBar / Liquid Dock

该图表示当前代码调用关系，不是建议永久保留的目标架构。Dock 位于页面内容之外的底部覆盖层，没有被放进页面的纵向滚动容器。

## 4. 滚动与手势审计

### 4.1 完整检索结果

基于 main 源码与 GitHub 代码搜索，当前 UI 有 **6 处纵向滚动 + 1 处横向滚动**：

| 文件 | 组件 / 功能 | 当前机制 |
|---|---|---|
| MainActivity.kt | 计算 / AI 占位根页面 | Column.verticalScroll(rememberScrollState()) |
| ui/my/MyScreen.kt | 我的页面 | Column.verticalScroll(rememberScrollState()) |
| ui/units/UnitConverterScreen.kt | 工具目录 | verticalScroll(rememberScrollState()) |
| ui/units/UnitConverterScreen.kt | 单位换算页 | verticalScroll(rememberScrollState()) |
| ui/history/HistoryScreen.kt | 历史空状态页 | verticalScroll(rememberScrollState()) |
| ui/lab/GlassLabScreen.kt | 材质设置页 | verticalScroll(rememberScrollState()) |
| ui/units/UnitConverterScreen.kt | 单位类别横向 Chips | horizontalScroll(rememberScrollState()) |

搜索未发现以下实现：

- LazyColumn / LazyRow / Lazy Grid。
- Modifier.nestedScroll / 自定义 NestedScrollConnection。
- OverscrollEffect / 自定义 Modifier.overscroll。
- 一个统一持有整页边缘行为的 Scroll Container。

因此，不同页面目前依赖各自的 Compose Foundation 滚动容器和默认边缘行为；代码里没有用户指定的“整个内容视口作为刚性整体上移并整体回弹”的机制。源代码检索不能代替设备上对默认系统边缘视觉的精确判断，本审计不对未实测的手机物理手感下结论。

### 4.2 需要共同协调的手势

- 材质页内的 Liquid Slider 使用横向拖动识别和 touch-slop / 轴向意图判断；纵向手势要交还给页面。
- Liquid Toggle 的长按 / 拖动识别已针对父级滚动做过处理。
- 单位换算页的类别 Chips 是横向滚动容器，必须允许明显的纵向拖动传递给页面。
- 单位选择使用 DropdownMenu；文本输入会弹出系统 IME。
- 后续计算器键盘、图表缩放 / 拖动、Sheet 和页面横向切换也需要手势优先级。

**约束：** 新整页回弹只能有一个 owner。不能同时保留默认拉伸与自定义 viewport 位移，也不能让滑块、开关、横向列表或文本输入失去自己的手势语义。

## 5. Insets、输入法和固定区域

- Activity 使用 enableEdgeToEdge，系统栏为透明浅色样式；Android Q+ 关闭系统栏自动对比 scrim。
- Window 使用 SOFT_INPUT_ADJUST_RESIZE。
- 根页面通过 Material 3 Scaffold 的 innerPadding 处理根区域。
- 历史、材质和单位换算页面自行应用 WindowInsets.safeDrawing；单位换算另加 imePadding()。
- 我的和工具目录依赖父级传入的 Modifier / Scaffold padding。
- 不同页面的顶部、底部留白目前是本地数值；Dock 的间距没有统一的 Layout Token 契约。
- 68dp 的浮动 Dock 由父级底部覆盖层布局，不属于页面滚动内容。

F4 需要统一页面 viewport、系统 Insets、IME、Dock 和固定操作栏之间的责任，避免重复加安全区或最后一项被 Dock 遮挡。

## 6. Backdrop 与玻璃材料资产

### 6.1 渲染链路

| 文件 / 类 | 责任 |
|---|---|
| ui/glass/BackdropHost.kt | 用 rememberLayerBackdrop 建立共享 Backdrop，并以 layerBackdrop 记录静态浅色渐变背景 |
| ui/glass/ZhixingGlassSurface.kt | 使用共享 Backdrop 和 drawBackdrop 绘制 glass surface；按能力选择 vibrancy / blur / lens / highlight |
| ui/glass/GlassSurfaceCapabilities.kt | API 能力降级策略 |
| ui/glass/ZhixingLiquidButton.kt | 知行按钮适配 |
| ui/glass/ZhixingLiquidSlider.kt | 知行连续玻璃滑块 |
| ui/glass/ZhixingLiquidToggle.kt | 知行玻璃开关 |
| ui/glass/ZhixingCatalogBottomBar.kt / ZhixingLiquidBottomBar.kt | 浮动玻璃 Dock 和导航胶囊 |
| ui/glass/backdrop106/** | 移植的 Backdrop Catalog 1.0.6 交互组件与动效源码 |

主壳根页面、工具和我的页面复用当前 root Backdrop；单位换算、历史和材质设置在各自的二级页面路径中建立自己的 BackdropHost。当前不是每个 surface 单独创建 Backdrop，但独立子页的 host 边界仍有统一封装空间。

### 6.2 材质状态和持久化

GlassMaterialState.kt 维护九项独立参数：

1. 透明度百分比。
2. 模糊半径 dp。
3. 总开关。
4. 折射开关。
5. 折射强度 dp。
6. 色散开关。
7. 边缘高光开关。
8. 环境反射开关。
9. 触摸反馈开关。

GlassMaterialSettingsStore.kt 使用 Preferences DataStore 文件名 glass_material_settings，逐项存储九个设置；读入时处理 IOException 和范围 / 非有限值。调用方先用 previewMaterial 更新内存预览，用户提交后才保存，避免滑块每帧进行磁盘写入。

### 6.3 平台降级和性能档位

resolveGlassSurfaceCapabilities 当前约定：

- API 26+：允许 Backdrop sampling。
- API 31+：允许 blur / vibrancy。
- API 33+：允许 lens refraction。
- 关闭玻璃时，关闭采样和全部光学效果。

GlassPerformanceTier.kt 定义 PREMIUM / STANDARD / LITE / OFF / CUSTOM 五个档位。普通数学文本、输入和公式应保持清晰，不把每个内容卡片都转成高成本模糊玻璃。

### 6.4 第三方依赖与许可证

- Backdrop 版本：io.github.kyant0:backdrop:1.0.6。
- Shapes 版本：io.github.kyant0:shapes:1.2.0。
- 仓库有 THIRD_PARTY_LICENSES/AndroidLiquidGlass-1.0.6-LICENSE.txt；上游采用 Apache-2.0。F1 不升级、不替换。
- 优先建立知行 adapter / token / surface facade，将第三方 API 限制在 glass implementation 内，不让业务页面直接依赖第三方类型。

## 7. 当前设计 Tokens

| 文件 | 当前事实 | 缺少的统一能力 |
|---|---|---|
| ColorTokens.kt | 浅色 Material 3 ColorScheme | 完整语义 / 状态 / 材质色命名 |
| TypeTokens.kt | 品牌、标题、正文、辅助文案等 TextStyle | 数学内容、数字、行高角色与系统字号策略 |
| SpacingTokens.kt | 12 个基础 Dp spacing token | 页面 / 内容 / 控件 / 键盘 / 固定操作区完整间距刻度 |
| ShapeTokens.kt | 28dp hero、18dp feature | 通用 Shape 体系及与 Glass shapes 的收敛 |
| ZhixingGlassTokens.kt | glass 半径、press scale、若干时长 / spring 参数 | 与通用 Motion / Interaction token 的统一 |
| ZhixingTheme.kt | colors / type / spacing / shapes | Elevation、Material、Motion、Interaction、Layout token 入口 |

页面仍有硬编码尺寸：例如主壳 logo 高度 54dp、Dock 高度 68dp、页面独立 top / bottom padding、单位面板 17dp、单位菜单最大高度 360dp，以及各处自定义圆角。F1 不要求一次性把所有业务页尺寸迁入 token；先建立完整体系和兼容层，页面迁移时再逐步替换。

字体策略为当前 Compose / Android 默认字体回退，没有额外字体资产；框架重建不擅自换字体。

## 8. 状态所有权与页面逻辑

| 功能 | 当前状态持有 | 当前持久化 |
|---|---|---|
| 一级 Tab | MainActivity 的 rememberSaveable index | Saved state |
| 二级子页 | MainActivity 的 rememberSaveable nullable String | Saved state |
| 各根页 UI | SaveableStateHolder keyed by route | Saved state |
| 单位换算 | rememberSaveable category / source / target / input / copied key | Saved state |
| 材质 UI 控件 | GlassLabScreen 的本地 rememberSaveable 值 | 通过回调写 Material DataStore |
| 材质全局设置 | MainActivity preview + store Flow | Preferences DataStore |
| 历史记录 | 暂无 | 暂无本地 history store |

单位换算页面在 ui/units/UnitConverterScreen.kt 中调用既有 Presenter / domain 层；本次不重写其换算算法。UI 重建要保持其输入、类别选择、源 / 目标单位选择、交换、即时结果、复制完整等式、清除输入、未完成数字处理和 TalkBack 语义。

## 9. 测试和 CI 基线

### 9.1 已有测试资产

- JVM：数学 tokenizer / parser / evaluator / algebra / solvers / function sampling / units domain。
- UI 纯 Kotlin 测试：导航数据契约、glass material state / capabilities / touch feedback / drag math / performance tier、Backdrop 几何 / 折射数学、UnitConversionPresenter。
- Android instrumentation 源：BackdropHost、Dock drag / insets orientation / layout accessibility / MainActivity integration / semantics / material preview integration。

### 9.2 工作流实际执行什么

.github/workflows/android-build.yml 执行：

1. Markdown 本地链接检查。
2. JDK 17、Android SDK API 36 / Build Tools 36.0.0。
3. Android 图片资源完整性检查。
4. Gradle setup。
5. :app:testDebugUnitTest。
6. :app:compileDebugAndroidTestKotlin。
7. :app:assembleDebug。
8. 上传 Debug APK artifact。

**重要：** workflow 会编译 androidTest，但没有启动 emulator 执行 instrumentation；也没有当前完整 UI 滚动性能基准或长帧统计。CI success 不是设备滑动、光学玻璃或 120Hz 体验通过。

### 9.3 本次构建记录

- 审计前最新规划 Build：[#482 / Run ID 44346557259](https://github.com/PaleSquire71029/zhixingapp/actions/runs/44346557259)，已成功。
- 对应 commit：bf203cba582bd217d7bdb266901c5f61d44549b6。
- F0 文档提交会触发新的 PR workflow，应以新 SHA 对应的 run 结果更新，不把 Build #482 误写成 F0 文档新提交的 CI。

## 10. 已验证行为与必须保护的资产

- 用户已验收的主壳 / Glass Dock 交互和 PR #88 涉及的连续滑块、开关长按与纵向滚动仲裁、刷新率偏好，不应因重建而回退。
- 材质九项参数可持久化，Off 模式不应清除其他光学设置。
- 单位换算的 9 个类别、76 个线性单位、3 个温度仿射单位、Presenter 和错误模型继续保留。
- 数学 domain 包 domain/math/**、函数采样、单位换算 domain 不属于 UI 重建范围。
- 当前历史是空状态，不可伪造历史数据或提前设计假的列表内容。
- 计算与 AI 现在是占位；后续重建时必须替换成真正工作区，不要把占位介绍卡当成已实现业务。

## 11. 尚未能在静态审计中确定的内容

本次通过 GitHub 仓库审计，不能远程操作 iQOO 手机，因此以下内容明确未实测：

- 当前 main APK 在目标设备上的整页滚动边界观感、实际弹性 / stretch 细节。
- 当前各页面滚到底部最后一个控件与 Dock 的真实遮挡情况。
- 滑块 / 开关 / 横向 Chips 与滚动竞争在本次基线 APK 上的实际触感。
- 新整页回弹的任何帧率、最大位移、弹簧速度或 60Hz / 120Hz 数据。
- 新截图基线。

已有用户确认的 Dock 与材质控件真机验收作为回归保护；本轮不重复要求用户提供手机日志。F3 原型生成 APK 后，需请用户在目标手机验收新回弹。

## 12. F0 结论与下一阶段

F0 的静态源码审计与技术方案已足够明确，不需要继续做大而空的架构讨论。

**F1 进入范围：**

- 建立完整的 Color / Typography / Spacing / Shape / Elevation / Material / Motion / Interaction / Layout Token 分类。
- 收敛现有 ZhixingGlassTokens 与通用 motion / shape token 的职责，保留当前值和调用兼容性。
- 扩展 ZhixingTheme 入口并新增 Token 合约单测。
- 不修改页面、导航、MainActivity、玻璃算法或 Gradle 依赖。
- 旧页面保持可编译、可运行；完整 workflow 通过后进入 F2。

参考：
- [MainActivity.kt](https://github.com/PaleSquire71029/zhixingapp/blob/main/app/src/main/java/ci/us/zhixing/app/MainActivity.kt)
- [UnitConverterScreen.kt](https://github.com/PaleSquire71029/zhixingapp/blob/main/app/src/main/java/ci/us/zhixing/app/ui/units/UnitConverterScreen.kt)
- [MyScreen.kt](https://github.com/PaleSquire71029/zhixingapp/blob/main/app/src/main/java/ci/us/zhixing/app/ui/my/MyScreen.kt)
- [GlassLabScreen.kt](https://github.com/PaleSquire71029/zhixingapp/blob/main/app/src/main/java/ci/us/zhixing/app/ui/lab/GlassLabScreen.kt)
- [GlassMaterialSettingsStore.kt](https://github.com/PaleSquire71029/zhixingapp/blob/main/app/src/main/java/ci/us/zhixing/app/ui/glass/GlassMaterialSettingsStore.kt)
- [Build #482](https://github.com/PaleSquire71029/zhixingapp/actions/runs/44346557259)
