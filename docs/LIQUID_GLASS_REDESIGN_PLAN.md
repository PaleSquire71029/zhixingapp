# 知行 Android App：主界面液态玻璃重设计专项计划

> 文档版本：2.0
> 建立日期：2026-11-01
> 目标仓库：PaleSquire71029/zhixingapp
> 执行分支：main（规划文档）；后续兼容性试验可使用独立分支
> 状态：2026-11-01 已按用户提供的官方 Catalog APK 调整为直接移植路线；旧自研增量方案暂停
> 关联文档：[Android App 总体规划](./ANDROID_APP_MASTER_PLAN.md)、[实施状态账本](./IMPLEMENTATION_STATUS.md)、[项目现状与操作指南](./PROJECT_STATUS_AND_GUIDE.md)

本文专门记录这次液态玻璃重做的设计决策、技术验证、实施顺序、涉及文件、性能策略和验收条件。后续每完成一个阶段，都必须更新本文的阶段状态，并同步更新 \`IMPLEMENTATION_STATUS.md\`。规划中的功能不得描述成已经完成。

**路线变更（2026-11-01）：** 用户提供了 Backdrop Catalog 官方 APK 作为效果基准。执行策略从“基于 Backdrop 原语自行逐项搭建”改为“固定到官方 1.0.6 标签，直接移植其 Catalog 已验证的液态玻璃组件源码，再适配知行导航、品牌和浅色主题”。本文件末尾第 25 节为当前最高优先级执行路线，旧阶段记录仅保留作历史，不再要求沿旧自研方案继续叠加效果。

---

## 1. 本次改造目标

将当前原生 App 的普通 Material 3 底部导航和实验性质的玻璃卡片，升级为可在主界面实际使用的「知行 Light Liquid Glass」材质系统。

这不是只给控件加一层半透明白色背景。目标是形成具有真实背景参与感的轻量光学材质：背景颜色和形状能够透过玻璃，模糊程度可调，边缘有克制的高光和折射，触摸时产生流动反馈，导航选中状态具有连续的形变动画。

### 1.1 用户明确提出的要求

1. 只开发 Android 原生 App，使用当前 Kotlin + Jetpack Compose 工程。
2. 尽可能还原 iOS 26 Liquid Glass 的材质和交互特征，但要保持知行自己的设计语言。
3. 透明程度和模糊程度必须可以分别调整，不能把二者做成一个强度参数。
4. 当前玻璃卡片中的动态小球出现一卡一卡的现象，必须解决其根因。
5. 使用经过检查的开源液态玻璃项目作为渲染基础，不继续把现有实验 Shader 当成最终引擎。
6. 液态玻璃底部导航直接进入主界面，不再只放在单独实验室中展示。
7. 底部四个导航入口继续保留：计算、知行AI、工具、历史。
8. 主界面及更多合适的交互元素要使用统一玻璃材质，但不能把所有内容卡片都变成玻璃。
9. 仅使用浅色视觉，不增加深色模式或大面积深色面板。
10. 保留用户提供的知行 Logo 和既有品牌气质；不擅自更换 Logo、字体或添加大体积字体文件。
11. 不增加计算器账号登录。普通计算功能仍应保持本地可用。
12. 每一小步都要控制范围、保留回滚点，并记录真实的构建和测试结果。
13. 当前阶段只做源码和测试；没有明确要求时不生成 APK。

### 1.2 这次不做的事

- 不重写计算器算法、AI 网络层、账号或云历史。
- 不重做整套应用导航架构，不改变四个入口的业务含义。
- 不把玻璃效果强行应用到正文、公式、长列表和所有信息卡片。
- 不使用每帧将整张屏幕复制成 Bitmap 的方式冒充实时材质。
- 不通过简单提高当前 1 秒快照频率来掩盖卡顿。
- 不在未验证兼容性的情况下直接升级所有 Gradle、AGP、Kotlin 和 Compose 版本。
- 不把仅适用于某个 Android API 的效果宣传成所有设备完全一致。
- 不以主观截图代替构建、自动化测试和真机交互验收。

---

## 2. 当前代码基线与已知问题

### 2.1 工程基线（2026-11-01 核对）

| 项目 | 当前仓库配置 |
|---|---|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Android Gradle Plugin | 9.1.1 |
| Kotlin Compose 插件 | 2.4.21 |
| Gradle Wrapper | 9.3.1 |
| Compose BOM | 2026.06.00 |
| compileSdk / targetSdk | 36 / 36 |
| minSdk | 26 |
| 主题 | 知行自定义浅色主题 |
| 主 Activity | \`app/src/main/java/ci/us/zhixing/app/MainActivity.kt\` |
| 玻璃实验室 | \`app/src/main/java/ci/us/zhixing/app/ui/lab/GlassLabScreen.kt\` |
| 玻璃性能档位 | \`app/src/main/java/ci/us/zhixing/app/ui/lab/GlassPerformanceTier.kt\` |
| 现有折射实验 | \`app/src/main/java/ci/us/zhixing/app/ui/lab/SingleChannelRefraction.kt\` |
| 底部导航契约 | \`app/src/main/java/ci/us/zhixing/app/navigation/AppDestination.kt\` |

### 2.2 动态小球卡顿的已确认原因

当前 GlassLab 中存在以下组合：

- \`BACKDROP_SAMPLE_INTERVAL_MS = 1_000L\`；
- 动态背景的动画本身持续运动；
- 玻璃面板显示的是上次捕获的背景 ImageBitmap；
- 动态模式每隔约 1 秒才重新捕获一次背景。

因此，背景小球本身可以持续移动，但玻璃面板里看到的却是一张每秒才更新一次的旧图像，视觉上自然会跳动。问题不只是动画时长设置不合理，而是背景动画和玻璃采样没有处在同一个连续渲染链路中。

**修复原则：** 采用经过验证的实时 Backdrop 渲染/采样方案，或使用等价的帧同步渲染架构；不采用每秒更新 Bitmap，也不直接改成每 16 毫秒整屏截图。必须同时观察帧时间、内存分配、发热和滚动流畅度。

### 2.3 当前首页状态

当前 MainActivity 中：

- 使用 Material 3 \`NavigationBar\` 和 \`NavigationBarItem\`；
- 导航容器为普通白色底；
- 四个标签和文案已存在；
- 首页仍以占位内容为主；
- GlassLab 通过单独的按钮进入，尚未作为主界面底栏材质；
- 尚无全局共享的实时背景源；
- 当前实验室的透明度、模糊、折射和性能档位仍属于实验界面，不等同于主界面已经具备完整玻璃能力。

---

## 3. 设计方案：知行 Light Liquid Glass

### 3.1 核心视觉原则

- **覆盖面要广。** 首页及后续主要功能页的大部分可交互元素和主要容器，都应使用官方液态玻璃组件，或使用同一材质系统的立体化浅色表面；不能只把底部导航做成玻璃。
- **建立统一空间层级。** 背景、普通内容面、玻璃控件、悬浮工具、弹出面板形成清楚的前后景关系；用真实阴影、接触阴影、边缘高光、折射和局部透光建立高度，不用一圈粗描边假装 3D。
- **内容保持清楚。** 正文、数学公式、计算结果、聊天内容、历史条目等优先放在可读的浅色玻璃 / 磨砂容器中；玻璃负责容器表面，不能让文字本身变模糊、变形或失去对比度。
- **大部分元素采用玻璃，但避免无意义叠层。** 导航、卡片容器、按钮、输入框、键帽、滑块、开关、标签、弹出层、工具栏和浮动操作均纳入材质系统；相邻组件采用不同高度和透明度，而不是层层叠加多个完全相同的玻璃片。
- **背景必须参与材质。** 采用官方 LayerBackdrop / CombinedBackdrop 组织真实背景与前景内容；让背景色块、图标和控件轮廓在适当区域产生可辨认的折射，不以静态渐变贴图替代动态采样。
- **立体效果来自光学与空间，而非装饰堆叠。** 保留透镜边缘位移、色散、高光、环境反射、柔和阴影、接触暗边、按压凹陷和抬起浮层；反射颜色随知行浅色主题调整。
- **动画统一并连续。** 按压、释放、拖动、选中、切换、展开、收起、滚动反馈和页面进入退出使用一致的 Spring / 阻尼参数体系；避免互不协调的独立 tween 和瞬移。
- **品牌优先。** 保留知行 Logo、品牌主色、中文排版气质和现有字体策略；参考 Apple 的材质和运动规律，不照搬 Apple 图标、文案或品牌界面。
- **浅色优先。** 不引入深色模式或大面积深色面板；高光、阴影和玻璃色调均以明亮、通透为主。

### 3.2 主界面底部导航

底部导航将从贴边的白色 Material NavigationBar 改为悬浮的圆角胶囊形玻璃栏：

- 保留四个入口及现有文案：计算、知行AI、工具、历史。
- 整体与屏幕底部和系统手势区域留出安全距离，不遮挡内容。
- 背景使用可调透明度、可调模糊和实时背景折射。
- 选中态使用可移动的液态指示区域，随索引位置平滑移动并略有弹性。
- 点击选项时，选中区域和图标同步过渡；图标有轻微缩放，文字淡入/位移，不做大幅弹跳。
- 手指按压时出现局部高光/轻微形变；松开后平滑回弹。
- 四个选项保持足够的触控面积和文字对比度。
- 首版先完成点击切换和选中动画；拖动跨 Tab、横向跟手形变、滚动时自动收缩等增强交互在基本版本稳定后再分步实现。
- 不允许玻璃背景动画影响导航点击命中区域或造成触控延迟。

### 3.3 其他玻璃元素的使用范围

按页面结构覆盖大部分元素，优先复用直接移植的官方组件，不为每一种控件重新造一套视觉：

| 页面 / 区域 | 应采用的玻璃与立体效果 | 动画要求 |
|---|---|---|
| 全局背景与页面框架 | 连续背景采样、环境光斑、远近层次；玻璃表面能读取背景真实颜色与形状 | 背景光照低频柔和变化；页面切换时背景不闪断 |
| 首页品牌区 / 顶部工具栏 | 浮起式玻璃工具条、玻璃图标按钮、浅色立体徽标底座 | 点击压低、松手回弹、局部高光跟随 |
| 首页主功能区 | 大部分功能入口卡片采用半透明磨砂玻璃；用不同高度、阴影和边缘反射区分主次 | 卡片轻触抬起、点击压下；进入页面采用轻微位移与淡入 |
| 底部导航 | 官方 LiquidBottomTabs：背景玻璃、前景内容采样、选中透镜、色散 | 阻尼拖动、速度形变、选中指示器滑行、按压放大、指尖高光 |
| 计算器显示区 | 具有前后分层的玻璃显示面；结果区域更清晰，表达式区域轻微折射；重要结果有内外层次 | 数值更新轻微滚动 / 淡入，不让每次输入引发整个面板跳动 |
| 数字与运算键盘 | 大部分按键作为独立凸起玻璃键帽；数字键、运算键、功能键有克制的高度差和不同反射 | 按下有压陷、接触阴影变浅、高光移动；松开弹回；长按与连按不丢事件 |
| 科学 / 函数 / 化学工具 | 工具栏、选项、公式输入框、控制项和主要卡片均采用玻璃表面；复杂公式留在清晰内容层 | 切换模式、打开符号面板、展开推导区采用连续展开 / 收起 |
| 知行 AI 对话页 | 顶部栏、消息气泡容器、附件 / 工具栏、输入框、发送按钮、快捷提示芯片使用统一玻璃 | 消息进入轻微上移淡入；发送按钮按压；输入框聚焦高光；底部输入面板平滑跟随 IME |
| 工具页 | 工具分组、工具入口卡片、筛选 chips、浮动操作和选择器采用多层玻璃 | 列表项触摸抬起，筛选状态弹性切换 |
| 历史页 | 搜索栏、筛选栏、历史分组容器、条目操作菜单采用玻璃；长文本主体保持易读 | 搜索聚焦、筛选、展开详情和删除撤销有连贯过渡 |
| 设置 / 弹窗 / 底部面板 | 对话框、BottomSheet、菜单、Picker、滑块与开关尽量使用官方玻璃组件 | 展开 / 收起带缩放、位移和遮罩淡变；拖动松手有阻尼吸附 |
| 图表和可视化 | 图表控制、图例、提示浮层用玻璃；曲线、网格和数据标签保持清晰 | 提示框跟随数据点，视图缩放 / 平移有惯性与边界约束 |

**覆盖口径：** 目标是大部分可交互控件与主要容器具有玻璃材质或明确的立体层次；不是把每个文字 Glyph、数学公式笔画或每个像素都做成玻璃。对于高密度内容，采用清晰的磨砂内容面，让容器是玻璃、内容仍可读。

### 3.4 玻璃材质可调参数

所有参数都需要有稳定的默认值、明确的上下限、无障碍可读性保护和性能档位约束。

| 参数 | 计划范围 | 初始建议 | 说明 |
|---|---:|---:|---|
| 透明程度 | 0–100% | 约 78% | 数值越高，背景可见程度越高；实现时换算为材质覆盖层 Alpha |
| 背景模糊 | 0–24 dp | 12 dp | 与透明程度独立调节；0 表示不模糊 |
| 折射高度 / 量 | 与官方 Lens 参数一致，按 dp 配置 | 底栏基础 24 / 24 dp；选中胶囊按压 10 / 14 dp | 使用官方参考值，按组件尺寸和 API 能力缩放 |
| 边缘高光 | 0–100% | 官方 Default Highlight 基线 | 由边缘法线 / 高光样式控制，不用普通描边代替 |
| 色散 | 0–1（材质强度） | 底栏选中胶囊按 Catalog 默认开启 | 通过官方多通道采样；可在性能设置里关闭 |
| 环境反射 | 0–100% | 柔和、低饱和 | 与背景实际颜色协调，不叠加固定彩虹渐变 |
| 空间高度 | 组件级 0–4 层 | 卡片、按钮、键帽和浮层分别设层级 | 由阴影、接触暗边、高光宽度与形变共同表达 |
| 触摸反馈 | 开/关 | 开 | 官方 InteractiveHighlight / DampedDragAnimation；结束后弹性恢复 |

参数行为要求：

- 透明程度变化不能偷偷改变模糊半径。
- 模糊半径变化不能改变背景整体亮度或折射强度。
- 滑块拖动时实时预览，离开设置页后保留用户选择。
- 过高透明度或过低对比度时，必须保留文字可读性；可通过轻微亮度遮罩/边缘增强保护内容，而不是改成深色大面板。
- 性能档位可以限制某些效果，但不能让控件显示的值与最终实际效果严重不一致。

---

## 4. 开源渲染引擎评估与选型原则

### 4.1 首选候选：Kyant0 / AndroidLiquidGlass（Backdrop）

项目地址：https://github.com/Kyant0/AndroidLiquidGlass

- 项目采用 Apache License 2.0；集成时保留许可证和必要的版权声明。
- 提供 Compose Multiplatform 的 Backdrop 液态玻璃效果库。
- 示例包含 LiquidButton、LiquidToggle、LiquidSlider、LiquidBottomTabs 等组件写法。
- 仓库 README 明确说明库本身不提供可直接拿来用的完整高层 UI 控件；这些组件是示例，因此知行需要自己实现导航栏、按钮和参数控制界面。
- Maven Central 候选坐标：\`io.github.kyant0:backdrop\`。
- 不复制 Catalog 的整套演示 App，但**直接移植它在 1.0.6 标签中已经实现的玻璃组件源码**（底栏、阻尼拖动、交互高光、按钮、滑块、开关等）；保留原版权 / Apache-2.0 声明，再适配知行导航状态与浅色设计。此路线优先于继续手写相似组件。

官方与发布信息：
- 项目源码与 README：<https://github.com/Kyant0/AndroidLiquidGlass>
- Backdrop 文档：<https://kyant.gitbook.io/backdrop>
- Maven Central：<https://central.sonatype.com/artifact/io.github.kyant0/backdrop>

### 4.2 版本兼容风险（必须先验证）

截至本计划建立时可查到的版本信息：

| 候选版本 | 公开依赖信息 | 与当前工程的差异 |
|---|---|---|
| Backdrop 2.0.0 | Kotlin 2.3.21、Compose 1.11.0；上游工程使用 AGP 9.2.1、Gradle 9.5.0、compileSdk 37 | 当前工程 Kotlin 插件 2.2.10、AGP 9.1.1、Gradle 9.3.1、compileSdk 36 |
| Backdrop 2.0.1 | Kotlin 2.4.10、Compose 1.12.0；上游工程使用更高的 SDK/构建工具配置 | 与当前工程版本差距更大 |

因此，不能直接在主分支添加最新依赖后就认定可用。兼容性试验应先验证：

1. Maven Central 是否能正常解析 Android 对应变体；
2. Kotlin 元数据与当前 Kotlin Compose 插件是否兼容；
3. Compose 运行时版本是否会引入冲突或重复类；
4. 当前 compileSdk 36 是否能消费该 AAR，还是必须升级到 37；
5. AGP 9.1.1 / Gradle 9.3.1 是否足够构建应用，还是需要同步调整；
6. 真正的 Android 设备渲染是否正常；
7. Android API 26–32 是否能安全降级；
8. 许可证、源码和依赖树是否可审计。

**版本策略：** 先对 2.0.0 做最小兼容性试验，再决定是否测试 2.0.1。若必须升级，采用能通过完整 CI 的最小升级集合；不为追最新版本而无必要地整体升级全部构建工具。若该库无法在合理升级范围内稳定接入，则评估其他具有明确许可证、可复现构建和真实 Android 实现的方案，不仓促采用许可证不清楚或仅有展示效果的项目。

### 4.2.1 Kotlin / AGP / Gradle 兼容矩阵补充（2026-11-01）

官方 Kotlin Gradle Plugin 兼容表显示：

- KGP 2.3.20–2.3.21 的完整支持范围为 Gradle 7.6.3–9.3.0、AGP 8.2.2–9.0.0；
- KGP 2.4.0–2.4.10 的完整支持范围为 Gradle 7.6.3–9.5.0、AGP 8.5.2–9.1.0；
- KGP 2.4.20–2.4.21 的完整支持范围为 Gradle 7.6.3–9.7.0、AGP 8.5.2–9.3.1。

因此，Backdrop 2.0.0 使用的 Kotlin 2.3.21 与当前 AGP 9.1.1 不在官方完整支持范围内；Backdrop 2.0.1 所用的 Kotlin 2.4.10 也比当前 AGP 9.1.1 的完整支持上限低一个补丁版本。兼容性试验优先采用 KGP 2.4.21 作为候选，与当前 AGP 9.1.1 和 Gradle 9.3.1 配对验证；这三者在上述官方矩阵范围内。AGP 9.1.1 官方支持 Android API 37，因此如该库 AAR 的编译约束要求，可将 compileSdk / CI SDK 升级到 37，而不必同时升级 AGP 和 Gradle。

另一个需要专门验证的风险：Backdrop 2.0.1 的 Android AAR POM 声明了 `org.jetbrains.compose.foundation:foundation-android`、`org.jetbrains.compose.ui:ui-android` 和 `ui-graphics-android` 1.12.0；知行 App 当前 UI 使用 AndroidX Compose BOM。必须检查 Gradle 变体选择、传递依赖和最终编译 classpath，不能仅因依赖能下载就认为与现有 Compose UI 没有冲突。

参考：
- [Kotlin Gradle Plugin compatibility](https://kotlinlang.org/docs/gradle-configure-project.html)
- [AGP 9.1.1 release notes and API 37 support](https://developer.android.com/build/releases/agp-9-1-0-release-notes)
- [Backdrop 2.0.1 Android artifact metadata](https://central.sonatype.com/artifact/io.github.kyant0/backdrop-android)



#### 4.2.2 Backdrop 1.0.6：当前正式依赖基线

Backdrop 1.0.6 已经作为正式依赖存在于 app/build.gradle.kts，不再是待选择候选：

- 当前依赖：io.github.kyant0:backdrop:1.0.6。
- 官方上游 tag 1.0.6 的 Android Catalog 使用 compileSdk 36，包名为 com.kyant.backdrop.catalog，versionName = 1.0.0。
- 用户上传的参考 APK 清单识别出的包名 / 应用版本与该 Catalog 配置相符；这使 tag 1.0.6 成为可直接核对的源码基准。
- 当前知行工程已通过包含 Backdrop 1.0.6 的 Android Build CI；本轮不再把升级到 Backdrop 2.x 作为前置条件。
- Catalog 组件源码依赖 io.github.kyant0:shapes:1.2.0（上游 1.0.6 目录中的版本声明）；移植时按原版本显式固定，并由 CI 验证变体 / 传递依赖。

#### 4.2.3 参考 APK 的源码基准与直接移植范围

**唯一移植基准：** [Kyant0/AndroidLiquidGlass tag 1.0.6](https://github.com/Kyant0/AndroidLiquidGlass/tree/1.0.6)，tag commit：896a94a3ade1cc1a940b92365f942a34971fecda。

优先移植的 Catalog 源文件：

| 官方源文件（tag 1.0.6） | 作用 | 知行侧适配 |
|---|---|---|
| catalog/.../components/LiquidBottomTabs.kt | 完整液态玻璃底栏、双层采样和选中透镜 | 保留四个 AppDestination 与现有回调 |
| catalog/.../components/LiquidBottomTab.kt | Tab 点击语义及按压缩放 | 映射 Role.Tab、无障碍标签 |
| catalog/.../utils/DampedDragAnimation.kt | 阻尼位置、速度、按压进度、X/Y 缩放 | 保留统一动画状态，不再叠加自研 Spring |
| catalog/.../utils/InteractiveHighlight.kt | 指尖跟随径向高光与按压反馈 | 按 API 33+ 使用 RuntimeShader，低版本按原方式回退 |
| catalog/.../utils/DragGestureInspector.kt | 官方拖动识别与取消逻辑 | 检查与页面纵向滚动、点击手势的冲突 |
| catalog/.../components/LiquidButton.kt | 液态按钮 | 用于主界面适合悬浮的操作入口 |
| catalog/.../components/LiquidSlider.kt | 液态滑块 | 逐步替换材质设置中对应控件 |
| catalog/.../components/LiquidToggle.kt | 液态开关 | 逐步替换玻璃开关 |

背景采样按官方 BackdropDemoScaffold.kt 的结构移植：背景图像先通过 layerBackdrop(backdrop) 注册为 Backdrop source，前景玻璃组件再使用同一个 backdrop；优先采用 tag 1.0.6 的 catalog/src/main/res/drawable/wallpaper_light.webp 作为首轮视觉对照背景。此处采样的是明确的背景源，不做每帧整屏 Bitmap 截图，也不将前景自身循环录入背景层。

**版本 / 工程策略：**

1. 保持当前 Backdrop 1.0.6，不迁移到 kmp 主分支上的 2.0.x API。
2. 只补齐 Catalog 明确使用的 shapes:1.2.0（如果当前依赖树尚未提供），不引入整套 Compose Multiplatform 工程。
3. 不为了移植示例组件而升级 AGP、Gradle、SDK 或 Compose BOM；只有 CI 明确证明是必要条件时，才做最小兼容调整。
4. 把官方源文件复制进知行自己的 ui/glass/catalog/（或等价隔离包），保留 Apache-2.0 原始版权头；每个改动文件注明“基于 Kyant0/AndroidLiquidGlass tag 1.0.6 修改”，仓库保留上游 LICENSE。
5. 只适配浅色模式、知行主题色、现有安全区、四项导航和设置参数；不照搬 Catalog 的展示页、导航结构、深色模式或整套演示 App。
6. 高成本的自适应亮度逐帧截图、进阶 demo 手势和实验页效果先不默认启用；基础材质达到参考效果后再评估。


### 4.3 引入前检查清单

- [ ] 源仓库、作者、许可证和许可证文本可核验；
- [ ] 固定依赖版本，不使用动态版本；
- [ ] 确认 Android artifact / variant；
- [ ] 检查 transitive dependencies；
- [ ] 确认最小 Android API；
- [ ] 确认是否依赖 Kotlin / Compose / SDK 的特定版本；
- [ ] 用隔离的最小 Composable 完成依赖解析与编译；
- [ ] 在完整 App 的 CI 中完成单元测试和 Debug 编译；
- [ ] 将许可证文件或 NOTICE 信息纳入仓库；
- [ ] 不把未经核验的第三方示例控件视为库的稳定公共 API。

---

## 5. 目标技术结构

以下结构是逐步形成的目标，不要求一次性拆成多个 Gradle Module：

- \`ui/glass/BackdropHost.kt\`：提供共享背景与 Backdrop 生命周期；
- \`ui/glass/ZhixingGlassSurface.kt\`：知行玻璃容器，统一透明度、模糊、高光和圆角；
- \`ui/glass/GlassMaterialState.kt\`：材质设置状态、范围校验和持久化适配；
- \`ui/glass/GlassPerformancePolicy.kt\`：根据 API、设备性能和用户选择裁剪效果；
- \`ui/navigation/ZhixingLiquidBottomBar.kt\`：主界面底部液态玻璃导航；
- \`ui/components/ZhixingGlassButton.kt\`：轻量按钮和图标按钮；
- \`ui/settings/GlassAppearanceSheet.kt\`：透明度、模糊和其他材质参数设置；
- \`ui/theme/\`：继续使用现有知行浅色设计 Tokens，不重复建立另一套主题。

目标边界：

- 渲染引擎只负责背景采样、模糊/折射和必要的光学效果；
- 知行自定义组件负责品牌颜色、形状、动效、点击语义和无障碍信息；
- 业务页面只消费稳定的组件 API，不直接操作 RuntimeShader 或第三方 Backdrop 内部对象；
- 所有可保存的材质参数集中管理，避免每个页面分别存一份互相冲突的设置。

---

## 6. 分阶段执行清单

每个阶段尽量控制在一个可独立检查的小任务内。阶段没有完成验收时，不提前叠加下一阶段的复杂度。

### 阶段 0：专项计划与基线确认

**目标：** 建立本专项的唯一执行计划，记录当前工程版本、文件位置、卡顿根因与设计约束。

**文件范围：**
- 新增本文；
- 更新 \`docs/IMPLEMENTATION_STATUS.md\`；
- 必要时更新 \`docs/PROJECT_STATUS_AND_GUIDE.md\` 的文档索引。

**验收条件：**
- 文档进入 GitHub；
- 用户可以从实施状态账本找到本文；
- 当前项目状态与未完成事项没有被误写为完成。

**状态：** 已完成（2026-11-01，专项文档已提交至 main）。

### 阶段 1：开源库兼容性验证

**目标：** 证明 Backdrop 能否以可控的版本升级成本进入当前 Android 工程。

**工作项：**
1. 核实 2.0.0 / 2.0.1 的 Maven 发布信息、源码版本和许可证；
2. 对照上游 Gradle、Kotlin、Compose 和 compileSdk 版本；
3. 在隔离分支建立最小依赖验证，不改主界面；
4. 只添加最小测试 Composable，验证 Backdrop API 编译和 Android 变体；
5. 若失败，记录明确错误和最小解决方案；
6. 完成 CI 后才决定依赖版本和构建工具升级幅度。

**主要文件（试验期间）：**
- \`app/build.gradle.kts\`
- \`build.gradle.kts\`
- \`gradle/wrapper/gradle-wrapper.properties\`
- \`.github/workflows/android-build.yml\`
- 试验用最小 Composable 与许可证文件

**验收条件：**
- 依赖可重复解析；
- Kotlin / Compose 版本不冲突；
- Debug 构建成功；
- 既有导航单测和其他测试继续通过；
- 没有引入不必要的 SDK/Gradle 大版本升级；
- 若不兼容，主分支保持不变。

**状态：** 已完成（2026-11-01）。Backdrop 1.0.6 在 PR #4 的 CI Run #39 通过：Kotlin 编译、既有单元测试和 Debug 构建全部成功。正式合并后进入阶段 2。验证报告：[BACKDROP_COMPATIBILITY_SPIKE.md](./BACKDROP_COMPATIBILITY_SPIKE.md)。

### 阶段 2：建立连续的共享背景渲染源

**目标：** 解决动态小球在玻璃面板里每秒跳一下的问题。

**工作项：**
1. 按选定引擎建立单一共享 Backdrop source；
2. 将动态渐变和光斑放在玻璃面板可正确采样的背景层；
3. 玻璃面板读取连续渲染的背景，而不是每秒导出旧 ImageBitmap；
4. 页面进入前台时运行，离开/后台时及时停止动画和渲染任务；
5. 避免每个玻璃控件独立复制整屏背景；
6. 保持正常列表滚动和触控响应。

**主要文件：**
- \`ui/glass/BackdropHost.kt\`
- \`ui/glass/GlassLabScreen.kt\`
- 必要的渲染适配层

**验收条件：**
- 动态背景和玻璃内背景视觉连续，不再有 1 秒更新一次的跳帧；
- 不存在每秒截图方案；
- 生命周期停止时不继续空转；
- 自动化构建通过；
- 真机上观察动画连续性、触控、滚动和发热。

**状态：** 代码与 CI 已完成（2026-11-01）；目标真机验收待执行。PR #5 已 squash 合并到 main，合并提交 `a71acf50a07e6a0c4d992e0e2f6664120a05cdd2`。Android Build #45（Run ID `41256336581`）通过资源校验、`:app:testDebugUnitTest`、`:app:assembleDebug` 和 Artifact 上传。CI 只证明构建与单元测试通过；真机连续性、滚动、触控、发热仍未验收。

### 阶段 3：实现知行玻璃基础组件

**目标：** 将第三方渲染能力封装为可复用、参数明确的知行材质。

**工作项：**
1. 建立 \`ZhixingGlassSurface\`；
2. 分离透明程度与背景模糊参数；
3. 加入合理的圆角、浅色覆盖、边缘高光；
4. API 不支持时降级到浅色半透明材质；
5. 加入参数范围保护和默认值；
6. 不修改旧计算器/AI 业务。

**验收条件：**
- 透明度滑块和模糊滑块互不影响；
- 0 模糊时不执行模糊；
- 参数超出范围时不会导致 Shader 崩溃；
- 文字图标可读；
- 低版本有清晰的降级表现。

**状态：** 执行中（2026-11-01）。S3.1 参数模型已提交到独立分支 `feature/glass-material-state`（commit `8419d3514d6aef67345a67ad874ba8810d5f2eba`）；S3.2 边界与默认值单元测试尚未编写，模型未合并到 main。

### 阶段 4：重做主界面底部导航（先实现点击和基础动画）

**目标：** 将现有 Material 3 白色导航栏替换为悬浮液态玻璃导航。

**工作项：**
1. 新建 \`ZhixingLiquidBottomBar\`；
2. 保留四个目的地与业务回调；
3. 导航栏与系统手势区域适配；
4. 选中胶囊平滑移动；
5. 图标和标签同步缩放/淡入；
6. 按压出现局部高光，松开回弹；
7. 确保点击热区足够大。

**验收条件：**
- 四个入口可正确切换；
- 快速连续点击不会错乱；
- 选中动画连续，不出现布局跳动；
- 文字在高透明、低模糊设置下仍清晰；
- 主界面滚动内容不被导航遮挡。

**状态：** 待执行。

### 阶段 5：主界面集成与布局调整

**目标：** 让液态玻璃导航成为真实主界面的默认导航。

**工作项：**
1. 在 MainActivity 主界面替换旧 NavigationBar；
2. 处理底部内容 padding 和系统导航栏 inset；
3. 保留知行 Logo、品牌标题与四个入口；
4. 将“打开材质实验室”调整为合适的材质设置入口；
5. 确保 GlassLab 仍可单独预览，但不再承担主导航本身。

**验收条件：**
- App 启动即看到新底栏；
- 四个目的地仍然可访问；
- 不出现底部内容被遮挡、导航偏移或图标截断；
- 返回键行为正常。

**状态：** 待执行。

### 阶段 6：材质设置面板与实时调节

**目标：** 允许用户直接调整透明度和模糊程度。

**工作项：**
1. 从主界面可达的“材质设置”入口打开轻量面板；
2. 提供透明程度滑块（0–100%）；
3. 提供模糊半径滑块（0–24 dp）；
4. 参数修改立即反映在底部玻璃导航和预览组件上；
5. 保存用户选择；
6. 提供恢复默认值；
7. 设置面板自身保持浅色清爽，不叠加多层玻璃。

**验收条件：**
- 两个主要参数独立变化；
- 关闭并重新打开设置后能恢复上次设置；
- 重置能恢复明确的默认值；
- 低端性能档位会正确限制高成本效果。

**状态：** 待执行。

### 阶段 7：增强交互和更多玻璃元素

**目标：** 在底栏稳定后，逐步加入触摸形变及其他交互玻璃控件。

**工作项：**
1. 选中胶囊加入更精细的弹性形变；
2. 触摸高光随按压位置移动；
3. 评估拖动切换 Tab 的手势；
4. 评估下滑收起、上滑恢复的底栏行为；
5. 在主界面添加少量玻璃快捷操作；
6. 添加知行玻璃按钮、开关或滑块的统一样式。

**验收条件：**
- 手势不会与页面滚动冲突；
- 动效可在系统动画缩减场景下降级；
- 快捷控件不影响正文阅读；
- 无玻璃叠层和明显耗电问题。

**状态：** 待执行。

### 阶段 8：性能档位和 Android 版本降级

**目标：** 让效果在不同设备上稳定运行，不以牺牲基础操作为代价。

**计划档位：**

| 档位 | 目标表现 |
|---|---|
| 完整 | 实时 Backdrop、适度模糊、折射、边缘光和触摸反馈 |
| 标准 | 降低采样/Shader 复杂度，保留连续背景和基础玻璃 |
| 轻量 | 关闭高成本折射和色散，保留浅色半透明、必要的静态高光 |
| 关闭 | 使用普通浅色表面，确保功能完整 |

**工作项：**
1. 评估渲染成本；
2. 为不同 API 与性能档位建立明确策略；
3. 在后台/页面离开时停止不必要的动画；
4. 不采用高频 Bitmap 全屏读取；
5. 支持减少动态效果的系统设置或用户选择；
6. 在目标真机检查滑动、点击、温升与电量变化。

**验收条件：**
- 所有档位都能正常导航；
- 轻量/关闭模式不崩溃；
- 后台停止动画；
- 连续滑动无明显掉帧；
- 无持续性高温或异常电量消耗；
- 对低 API 设备显示清晰且合理的降级材质。

**状态：** 待执行。

### 阶段 9：自动化测试、CI 与真机验收

**目标：** 在后续引入计算器/AI 页面前，稳定玻璃系统。

**测试范围：**
- 材质参数边界单测；
- 性能档位解析单测；
- 四个导航目的地与点击切换测试；
- 设置保存/恢复测试；
- Backdrop 生命周期测试；
- Android API 不支持场景的降级测试；
- Debug APK 构建；
- 用户目标设备真机安装、启动、返回、点击、滚动和设置操作。

**验收条件：**
- GitHub Actions 测试通过；
- Debug 编译通过；
- 真机无闪退；
- 无 UI 错位或底栏遮挡；
- 玻璃动画连续；
- 用户确认观感后，才将玻璃基础组件用于后续业务页面。

**状态：** 待执行。

---

## 6.1 下一轮执行拆分：每次只推进一个编号

从本节开始，将主界面液态玻璃实现拆成可独立提交的小步。每次会话只处理一个编号；没有通过本步验收，不进入下一步。

### S2：先完成实时背景实验的质量门槛

| 小步 | 只做什么 | 修改范围 | 完成条件 |
|---|---|---|---|
| S2.1 | 修复 Off 档仍挂载实时 Backdrop 与动态动画的问题 | GlassLabScreen.kt | Off 不挂载 `layerBackdrop`，只绘制静态浅色背景；其他档位不变 |
| S2.2 | 验证 PR #5 的源码、依赖与 API 降级 | 当前 PR #5 分支 | CI 的 Kotlin 编译、JUnit 和 Debug assemble 全部通过；不生成或归档 APK |
| S2.3 | 合并实时背景实验并同步文档 | PR #5 与状态文档 | 主分支不再包含 1 Hz Bitmap 快照链路；状态账本有真实 CI 结果 |

### S3：先做最小玻璃表面，不触碰主导航

| 小步 | 只做什么 | 修改范围 | 完成条件 |
|---|---|---|---|
| S3.1 | 定义材质参数模型 | 新增 GlassMaterialState.kt | ✅ 完成：透明度 0–100%、模糊 0–24 dp；分别校验、分别有默认值 |
| S3.2 | 给参数模型添加纯 Kotlin 单测 | 新增对应 Test | ✅ 完成：边界、非法值、默认值测试通过 |
| S3.3 | 建立单个 ZhixingGlassSurface | 新增 ZhixingGlassSurface.kt | ✅ 完成：支持 Backdrop、浅色覆盖、圆角和边缘高光；不包含导航业务 |
| S3.4 | 补充 API 降级与 Off 路径 | 同一玻璃组件及测试 | ✅ 完成：API 26–30 仅背景采样；API 31+ 可模糊；Off 不创建绘制采样节点 |

### S4：建立主页面唯一的共享背景宿主

| 小步 | 只做什么 | 修改范围 | 完成条件 |
|---|---|---|---|
| S4.1 | 建立静态浅色背景宿主 | 新增 BackdropHost.kt | ✅ 完成：单一背景源；低饱和渐变和静态柔光，不改变首页内容 |
| S4.2 | 让主界面内容层透明地位于背景上 | MainActivity.kt | ✅ 完成：调整根层级，保留原卡片、Logo、四个入口和白色底栏 |
| S4.3 | 验证玻璃采样范围和滚动层级 | Android Compose UI 测试/CI | △ 部分完成：代码层级审查与 androidTest 源码编译通过；Hosted Runner 缺少 KVM，模拟器无法启动，点击断言仍待设备验证 |

### S5：独立实现四入口玻璃底栏

| 小步 | 只做什么 | 修改范围 | 完成条件 |
|---|---|---|---|
| S5.1 | 创建无动画的悬浮胶囊底栏外形 | 新增 ZhixingLiquidBottomBar.kt | ✅ 完成：浅色玻璃外观、胶囊圆角和双层细描边；暂不改主页面 |
| S5.2 | 迁移四个导航项及点击回调 | 同一底栏文件与导航测试 | ✅ 完成：复用 AppDestinations 的四项数据、受控选中态及点击回调；未接入首页 |
| S5.3 | 处理悬浮位置与系统 Insets | 新增 ZhixingLiquidBottomBarPlacement.kt | ✅ 完成：避开导航/手势区域，可选避开 IME，保持独立水平与底部间距；尚未接入 MainActivity |
| S5.4 | 单独添加选中胶囊的连续位移动画 | ZhixingLiquidBottomBarItems.kt | ✅ 已实现并通过 CI：单个共享胶囊使用无过冲弹簧并可重新定目标；快速连点实机 / 仪器验证仍待做 |
| S5.5 | 单独添加图标/标签过渡 | ZhixingLiquidBottomBarItems.kt | ✅ 完成：图标轻微缩放 / 透明度，标签淡入；仅绘制层变化，不改变测量和点击范围 |
| S5.6 | 单独添加按压高光与回弹 | ZhixingLiquidBottomBarItems.kt | ✅ 完成：按压高光和内容轻微弹压，释放后弹簧恢复；触控容器尺寸保持不变 |

### S6：替换主导航并开放材质设置

| 小步 | 只做什么 | 修改范围 | 完成条件 |
|---|---|---|---|
| S6.1 | 在主 Scaffold 中替换原 NavigationBar | MainActivity.kt | ✅ 完成：共享 Backdrop 液态玻璃底栏已成为首页正式导航，四个目的地与原状态回调保留 |
| S6.2 | 添加设置入口 | MainActivity.kt | ✅ 完成：品牌区增加独立“材质设置”入口，复用现有 GlassLab，不占用底栏导航项 |
| S6.3 | 添加透明度单独滑块 | 复用 GlassLabScreen / GlassMaterialState | ✅ 完成：透明度滑块实时更新首页共享材质，松手保存；不改变模糊值 |
| S6.4 | 添加模糊与折射控制 | GlassLabScreen / ZhixingGlassSurface | ✅ 完成：模糊独立调节；API 33+ 提供独立透镜折射开关和强度，低版本降级 |
| S6.5 | 保存和恢复设置 | GlassMaterialSettingsStore / Preferences DataStore | ✅ 完成：透明度、模糊、Off、折射开关和折射强度均可跨进程保存与恢复；支持重置 |

### S7：收尾和扩展

先验收主导航，再考虑快捷按钮、开关、拖动切换 Tab、底栏收缩等增强交互。不得为了追求动画而同时引入多项手势。

**每个小步的统一结束记录：** 修改文件、Commit SHA、CI 状态、测试命令、未测项目、是否允许进入下一步。GitHub 写入成功不代表 CI 通过；CI 通过也不代表真机通过。

---

## 7. 质量与回滚策略

每个实施阶段必须遵守：

1. **小范围提交。** 一个提交只解决一个明确问题，不在同一提交中同时升级构建工具、重写导航和迁移业务功能。
2. **先验证后合并。** 第三方依赖兼容性试验优先放在独立分支；CI 未通过不合并。
3. **保留可回滚点。** 不覆盖用户原 Logo 和业务文件；每个阶段记录 commit SHA。
4. **不伪造验证结果。** GitHub 文件写入成功不等于 Android 编译成功；CI 运行中不写成已通过；未真机测试不说“真机正常”。
5. **不自动生成 APK。** 只有用户明确要求时才生成或触发 APK 构建。
6. **同步账本。** 每一阶段结束，更新本文状态、\`IMPLEMENTATION_STATUS.md\` 和必要的操作说明。
7. **保护旧项目。** 本轮只改 \`zhixingapp\`，不修改 calculator 或 zhixing_ai 两个旧仓库。

---

## 8. 总体验收标准

只有同时满足以下条件，才能称作“主界面液态玻璃基础版完成”：

- 主界面默认使用悬浮液态玻璃底部导航；
- 四个入口工作正常；
- 背景动画在玻璃内部连续显示，不再每秒跳帧；
- 透明程度和模糊半径可以分别调节；
- 材质设置可以保存并恢复；
- 玻璃按钮/交互控件的视觉语言一致；
- 内容文字、公式和图标清晰可读；
- 浅色界面没有大块深色面板；
- 低版本有明确降级方案；
- 完整 CI 测试、Debug 构建通过；
- 目标 Android 真机安装、启动和交互验收通过。

在全部验收完成前，只能使用“部分完成”“代码已提交待验证”等准确状态，不得宣称已完全还原 iOS 26 Liquid Glass。

## 9. S3.1–S3.2 实施记录（2026-11-01）

- [x] S3.1：新增 `GlassMaterialState.kt`，以不可变数据模型分别保存透明度和模糊半径。
- [x] 默认值为透明度 78%、模糊 12 dp；合法范围分别为 0–100% 和 0–24 dp。
- [x] 对模糊值拒绝 NaN、正无穷和负无穷；透明度与模糊均在构造时校验。
- [x] S3.2：新增 `GlassMaterialStateTest.kt`，覆盖默认值、两个参数的上下边界、独立设置、越界输入和非有限模糊值。
- [x] PR [#6](https://github.com/PaleSquire71029/zhixingapp/pull/6) 已通过 CI 并 squash 合并到 main；合并提交：`5639a8ad4664eab0b7707c5caeb8145dd82fbe95`。
- [x] Android Build #48（Run ID `41256717498`）成功：资源校验、Gradle 配置、JVM 单测、Debug APK 编译和 Artifact 上传均通过。
- [ ] 本步没有将模型接入 UI、DataStore、Backdrop 或 MainActivity；也没有把 PR 临时 APK 发布到 main。
- [ ] 尚未完成目标真机上的材质显示、交互、帧率、发热和耗电验收。

**下一小步：** S3.3，只建立单个 `ZhixingGlassSurface` 基础组件；复用现有 Backdrop 能力并接受材质参数，不实现底栏、不修改 MainActivity。

## 10. S3.3 可复用玻璃表面组件（2026-11-01）

- [x] 新增 `ZhixingGlassSurface.kt`，将 Backdrop 绘制、高光、圆角和浅色覆盖封装为单一可复用表面。
- [x] Backdrop 由父级提供，组件不自行创建独立背景记录层，便于多个玻璃表面共享一个背景宿主。
- [x] 透明度只换算为白色覆盖层 Alpha；模糊半径只传入 Backdrop blur 效果，两个参数保持独立。
- [x] Backdrop 缺失时采用普通浅色表面回退。
- [x] PR [#7](https://github.com/PaleSquire71029/zhixingapp/pull/7) 已通过 CI 并合并；合并提交：`e6f78a0101369fe65cc3cf930b48da9ace03cfb7`。
- [x] Android Build #49（Run ID `41260636118`）资源校验、Gradle、JUnit、Kotlin 编译、Debug APK 构建和 Artifact 上传均通过。
- [ ] 组件尚未接入 MainActivity / 生产主导航；真机材质效果、触控、滚动和性能仍待验证。

**下一小步：** S3.4，为组件补充显式 Off 开关和 API 26–30 / 31+ 能力策略；Off 必须不挂载 `drawBackdrop` 采样节点，并用纯 Kotlin 测试覆盖策略边界。

## 11. S3.4 API 降级与 Off 路径验收（2026-11-01）

- [x] 为 `ZhixingGlassSurface` 增加显式 `glassEnabled` 开关；关闭时使用浅色回退面，不挂载 `drawBackdrop` 修饰符。
- [x] 抽取纯 Kotlin `resolveGlassSurfaceCapabilities`，明确 API 26+ 的 Backdrop 采样能力与 API 31+ 的系统模糊能力。
- [x] 模糊半径为 0 时不创建模糊效果；越界、NaN、正负无穷均拒绝。
- [x] 新增 `GlassSurfaceCapabilitiesTest.kt`，覆盖 API 26–30、31–32、33+、零模糊、Off 和非法值。
- [x] PR [#8](https://github.com/PaleSquire71029/zhixingapp/pull/8) 已通过 CI 并 squash 合并；合并提交：`bd2c5bf3bcc0d143ac0230ab8e550ca27715ea11`。
- [x] Android Build #50（Run ID `41260858724`）成功；资源检查、Gradle、JVM 单测、Kotlin 编译、Debug APK 构建和 Artifact 上传均通过。
- [ ] 该组件的 Off 路径只保证不挂载单个表面的采样节点；整个主机进入 Off 时仍需要由宿主避免创建共享 Backdrop，留待 S4 阶段的宿主设计处理。
- [ ] 尚未进行目标 Android 真机上的 GPU、帧率、滚动、温度和耗电测量。

**下一小步：** S4.1，只新增 `BackdropHost.kt` 静态浅色共享背景宿主；单一背景层提供给后续玻璃表面，包含低饱和渐变与静态柔光，不修改首页内容和导航。

## 12. S4.1 静态共享背景宿主验收（2026-11-01）

- [x] 新增 `BackdropHost.kt`，由宿主统一创建一个 `LayerBackdrop` 并将 Backdrop 交给前景内容。
- [x] 背景采用低饱和浅蓝、淡紫与暖白渐变，叠加三处柔光；使用 `drawWithCache` 缓存画笔。
- [x] 本步刻意保持静态，不引入无限动画、定时器、Bitmap 截图或逐帧导出。
- [x] 首轮 CI 发现 `matchParentSize` 引用不可解析；已改为 `fillMaxSize`，没有改动 Backdrop 架构。
- [x] PR [#9](https://github.com/PaleSquire71029/zhixingapp/pull/9) 第二轮 CI Build #52（Run ID `41261146162`）完整成功，随后 squash 合并。
- [x] 合并提交：`496c098fdd1d41c4f979074a6ae5757b942b69b6`。
- [ ] 该宿主尚未接入生产首页；尚无真机视觉、滚动、帧率、温度与功耗验收。

**下一小步：** S4.2，只调整 `MainActivity.kt` 的根层级：将原首页 Scaffold 放到 BackdropHost 前景，并将 Scaffold 容器背景设为透明；保留首页内容、原白色 NavigationBar 和所有点击逻辑。

## 13. S4.2 首页背景层级接入（2026-11-01）

- [x] 将原首页 Scaffold 放入 `BackdropHost` 的前景内容层，确保静态共享背景位于首页内容之后绘制。
- [x] 将 Scaffold 容器背景改为透明，让共享浅色背景可见。
- [x] 保留原知行 Logo、Hero 卡片、功能说明行、材质实验室入口和四个导航目的地。
- [x] 保留原 Material 3 白色 NavigationBar、标签、选中态和点击回调；没有提前迁移液态玻璃底栏。
- [x] GlassLab 仍走自己的独立页面路径，本步不改变它的 backdrop 宿主。
- [x] PR [#10](https://github.com/PaleSquire71029/zhixingapp/pull/10) 与 Android Build #53（Run ID `41261281542`）通过后合并；合并提交：`5436ccbf2add0e9e76ae0f803ec3113da5599028`。
- [ ] 尚未完成真机状态栏 / 手势 Insets、滚动、背景采样显示和发热耗电验收。

**当前小步：** S4.3，增加 Android Compose 仪器测试，验证两个玻璃表面共享同一个 Backdrop，并确认前景点击可到达控件；背景像素采样和真实设备帧率仍需物理设备验收。

## 14. S5.1 悬浮底栏胶囊外壳（2026-11-01）

- [x] 新增 `ZhixingLiquidBottomBar.kt`，提供独立的 `ZhixingLiquidBottomBarSurface`。
- [x] 使用 `RoundedCornerShape(percent = 50)` 建立胶囊轮廓，复用 `ZhixingGlassSurface` 与父级共享 Backdrop。
- [x] 增加两层细描边：柔白外沿与低对比冷色内沿；保留玻璃表面的高光和浅色覆盖。
- [x] 首轮 CI 因错误导入 `drawRoundRect` 失败；移除无效导入后第二轮 CI 通过。
- [x] PR [#12](https://github.com/PaleSquire71029/zhixingapp/pull/12) 已通过 CI 并 squash 合并；合并提交：`f57991f10976e0ca62e9c41454ee63f15e4c865a`。
- [x] Android Build #57（Run ID `41261908712`）资源检查、Gradle、JUnit、Kotlin 编译、Debug APK 构建和 Artifact 上传均成功。
- [ ] 该组件尚未接入首页；导航项、选中态、悬浮位置、系统 Insets、动画和真机显示仍由 S5.2–S6 分步完成。
- [ ] S4.3 PR #11 的模拟器交互测试仍在独立 CI 中运行，尚未据此标记通过。

**下一小步：** S5.2，在独立组件中迁移四个导航项和点击回调，并覆盖选中状态；不接入 MainActivity、不添加位移动画。

## 15. S5.2 四入口与受控选中状态（2026-11-01）

- [x] 新增 `ZhixingLiquidBottomBarItems.kt`，创建独立 `ZhixingLiquidBottomBar` 内容组件。
- [x] 直接复用 `AppDestination` / `AppDestinations.all` 数据模型，不复制四个入口的标签和符号。
- [x] 支持调用方传入 `selectedIndex` 与 `onDestinationSelected(index)`；组件不持有导航状态。
- [x] 使用 `Role.Tab` 与 selectable 语义，选中项显示浅色胶囊背景，未选中项保持透明。
- [x] 保持静态，没有选中位移动画、缩放、拖动手势、系统 Insets 或 MainActivity 修改。
- [x] PR [#13](https://github.com/PaleSquire71029/zhixingapp/pull/13) 已通过 CI 并 squash 合并；合并提交：`c7077ad0c5331436ef9fc41e2a966d748c12de8f`。
- [x] Android Build #58（Run ID `41262141510`）资源检查、Gradle、JUnit、Kotlin 编译、Debug APK 构建及 Artifact 上传均成功。
- [ ] 组件尚未接入首页，真机横向空间、文字可读性、触控面积和系统 Insets 待后续步骤验收。
- [ ] S4.3 PR #11 的 Android 模拟器测试仍在单独工作流运行，不能把 S5.2 的 CI 成功视为 S4.3 已通过。

**下一小步：** S5.3，单独处理悬浮底栏相对屏幕底部的位置和系统 Insets；先在独立组件或预览中验证，不替换 MainActivity。

## 16. S5.3 悬浮位置与系统 Insets（2026-11-01）

- [x] 新增 `ZhixingLiquidBottomBarPlacement.kt`，提供 BoxScope 底部居中定位容器。
- [x] 使用 `navigationBarsPadding()` 避开 Android 底部导航栏和手势区域。
- [x] 默认使用 `imePadding()`，键盘弹出时将底栏内容上移；可通过 `liftForIme = false` 关闭该行为。
- [x] 水平边距与底部留白独立于系统 Insets 管理：默认水平 16 dp、底部 10 dp。
- [x] PR [#14](https://github.com/PaleSquire71029/zhixingapp/pull/14) 已通过 CI 并 squash 合并；合并提交：`bdf3198cb16693d03af0eb4dab2c0a5e04429485`。
- [x] Android Build #59（Run ID `41262297446`）资源检查、Gradle、JUnit、Kotlin 编译、Debug APK 构建和 Artifact 上传均成功。
- [ ] 尚未在 MainActivity 应用该定位容器；手势导航、三键导航、横屏和键盘行为仍需模拟器 / 真机验收。
- [ ] S4.3 PR #11 的共享 Backdrop 仪器测试仍在另一条 CI 中等待结果。

**下一小步：** S5.4，只为选中胶囊添加连续位移动画；仍不替换首页导航，先验证连续快速切换时不跳位、不重叠。

## 17. S5.4 选中胶囊连续位移动画（2026-11-01）

- [x] 将 S5.2 的逐项选中背景替换为单个共享胶囊指示器，绘制在四个导航项下方。
- [x] 根据容器宽度和 `selectedIndex` 计算目标横向偏移，四项保持等宽。
- [x] 使用 `animateDpAsState` 与无过冲弹簧；连续选择变化时动画从当前值重新定目标，不创建多个互相竞争的独立指示器。
- [x] 保持 `Role.Tab` 语义、选中态颜色和点击回调不变；没有加入拖动手势、图标/标签过渡或首页集成。
- [x] PR [#15](https://github.com/PaleSquire71029/zhixingapp/pull/15) 已通过 CI 并 squash 合并；合并提交：`4e3eac017835ea1548badf3665c8b5db6c59f265`。
- [x] Android Build #60（Run ID `41262482557`）资源检查、Gradle、JUnit、Kotlin 编译、Debug APK 构建和 Artifact 上传均成功。
- [ ] 快速连续点击时无跳位 / 重叠的动态验收仍需仪器测试或真机测试；CI 编译通过不代表动画交互已在设备上验收。
- [ ] 尚未替换 MainActivity 的正式导航栏。

**下一小步：** S5.5，单独添加图标与标签的轻微过渡（小幅缩放 / 淡入）；不得挤压布局或改变导航项触控尺寸。

## 18. S5.5 图标与标签轻微过渡（2026-11-01）

- [x] 将导航项的图标缩放控制在 0.96–1.0，选中时轻微放大。
- [x] 图标透明度在 0.88–1.0 之间变化；标签透明度在 0.78–1.0 之间变化。
- [x] 使用 170 ms FastOutSlowIn tween，切换时平缓过渡。
- [x] 图标通过 `graphicsLayer` 调整缩放 / Alpha，标签只调整 Alpha；不改变 RowScope 项目测量宽高和 selectable 点击区域。
- [x] 为每个目的地使用稳定 route key，避免组合顺序变化时把动画状态分配给错误的导航项。
- [x] PR [#16](https://github.com/PaleSquire71029/zhixingapp/pull/16) 已通过 CI 并 squash 合并；合并提交：`be056decb918611d4734083d442b4b1c1c7cc63f`。
- [x] Android Build #63（Run ID `41262700012`）资源检查、Gradle、JUnit、Kotlin 编译、Debug APK 构建和 Artifact 上传均成功。
- [ ] 图标 / 标签在连续快速点击时的实际视觉流畅度和触控命中仍需 Compose 仪器或真机验收。

**下一小步：** S5.6，单独添加按压高光与轻微回弹；不添加拖动手势，不改触控区域和导航回调。

## 19. S5.6 按压高光与轻微回弹（2026-11-01）

- [x] 每个导航项通过独立 `MutableInteractionSource` 读取按压状态。
- [x] 按压时显示低透明度白色圆角高光，释放时 Alpha 回到 0。
- [x] 只对导航图标 / 标签所在的内层视觉内容做 0.975 倍缩放，使用无过冲弹簧恢复到 1.0。
- [x] 外层 `selectable` 容器不缩放，测量尺寸与触摸命中范围保持原值；没有引入拖动手势。
- [x] PR [#17](https://github.com/PaleSquire71029/zhixingapp/pull/17) 已通过 CI 并 squash 合并；合并提交：`75babe9b963df3225fc67270c6eb94c112c9d8d8`。
- [x] Android Build #66（Run ID `41262910437`）资源检查、Gradle、JUnit、Kotlin 编译、Debug APK 构建和 Artifact 上传均成功。
- [ ] 真实按压取消、快速连点、滚动冲突和物理设备触控仍待仪器 / 真机验收。

**下一小步：** S6.1，在 MainActivity 中把原 Material 3 NavigationBar 替换为新的玻璃底栏；使用共享 Backdrop 和 Insets 定位容器，保留原四个导航目的地及受控选中索引；本步不加入 DataStore 或材质设置面板。

## 20. S6.1 首页正式接入液态玻璃底栏（2026-11-01）

- [x] 在 `MainActivity.kt` 中移除原 Material 3 `NavigationBar` / `NavigationBarItem`。
- [x] 正式使用 `ZhixingLiquidBottomBar`，将首页 `BackdropHost` 提供的同一个 Backdrop 传给底栏玻璃表面。
- [x] 使用 `ZhixingLiquidBottomBarPlacement`，底栏位于 Scaffold bottomBar 槽中，避开系统导航 / 手势 Insets，并在 IME 弹出时上移。
- [x] 保留 `AppDestinations.all`、`selectedIndex` 的 `rememberSaveable` 状态及原点击索引更新行为；首页卡片和 GlassLab 路径不变。
- [x] PR [#18](https://github.com/PaleSquire71029/zhixingapp/pull/18) 已通过 CI 并 squash 合并；合并提交：`95f6a422534f86cd6db64c0980f5ab824c9e897e`。
- [x] Android Build #67（Run ID `41263159541`）资源校验、Gradle、JUnit、Kotlin 编译、Debug APK 构建和 Artifact 上传均成功。
- [ ] 尚未在目标真机验证系统手势 / 三键 Insets、IME、滚动遮挡、触控、帧率、温度和功耗。
- [ ] 材质透明度 / 模糊设置面板与 DataStore 持久化尚未实现；属于 S6.2–S6.5。

**下一小步：** S6.5，使用 DataStore 保存并恢复透明度、模糊半径和 Off 开关；进程重建后设置恢复，提供重置默认值。

## 21. S4.3 共享 Backdrop 与前景点击验证（部分完成，2026-11-01）

- [x] 源码结构审查确认：`layerBackdrop` 仅记录背景渐变子树；宿主的前景内容是兄弟层，不会被重新录入同一采样层。
- [x] 背景层没有 `clickable`、`pointerInput` 或语义点击处理，不主动消费前景触摸。
- [x] 新增 Android Compose 仪器测试：两个 `ZhixingGlassSurface` 使用同一个 Backdrop；测试断言两个内容节点可见，分别点击后计数从 0 增至 1、2。
- [x] PR [#11](https://github.com/PaleSquire71029/zhixingapp/pull/11) 已合并；合并提交：`11fc135175210569bbadf0786e3acf8ece788ac2`。
- [x] Android Build（Run ID `41263348169`）成功：资源检查、JVM 单测、`compileDebugAndroidTestKotlin` 仪器测试源码编译、Debug APK 构建与 Artifact 上传均通过。
- [x] 已从 PR #11 移除需要实际启动模拟器的 CI 步骤，防止无 KVM 的 hosted runner 长时间卡住。
- [ ] 实际仪器点击断言**没有执行**。原 API 35 x86_64 模拟器日志明确显示 Runner 无 `/dev/kvm` 权限、x86_64 无硬件加速，ADB 持续报告 `device offline`，最终 8 分钟超时。
- [ ] 因此 S4.3 仅完成架构审查和测试源码编译，不标记“运行时交互通过”；两个表面的真实点击行为、采样像素、闪烁和真机性能仍待可用 Android 设备 / 带 KVM 的运行环境验证。

**后续处理：** 等有可用模拟器或真机时，再运行 `connectedDebugAndroidTest`；在此之前不得把仪器测试源码编译成功等同于点击断言通过。

## 22. S6.2 首页材质设置入口（2026-11-01）

- [x] 将品牌区右侧原静态“原生预览”标记改为按钮语义的“材质设置”入口。
- [x] 入口调用既有 `GlassLabScreen`，复用已有材质实验界面，不新建第二个重复面板。
- [x] 删除首页下方重复的“打开材质实验室”按钮，减少冗余入口。
- [x] 保留首页四个玻璃导航目的地、选中状态和 Backdrop 底栏，不增加第五个导航项。
- [x] PR [#19](https://github.com/PaleSquire71029/zhixingapp/pull/19) 已合并；合并提交：`afadc577c4bd7e6b3a69719a58bfcb7c1d3e2161`。
- [x] Android Build #?（Run ID `41263447325`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41263447325)。
- [ ] 真机入口触控面积、TalkBack 朗读及小屏顶部布局仍待验收。
- [ ] 透明度 / 模糊滑块尚未接入首页玻璃底栏；DataStore 持久化尚未实现。

**下一小步：** S6.3，先把透明度作为单独参数接入首页底栏并提供滑块；调整透明度不改变模糊半径。

## 23. S6.3–S6.4 材质参数实时同步（2026-11-01）

- [x] 在 `ZhixingApp` 中使用 `rememberSaveable` 持有首页透明度和模糊状态，并构造统一 `GlassMaterialState`。
- [x] 将同一 `GlassMaterialState` 传给生产 `ZhixingLiquidBottomBar`，底栏外观由该状态驱动。
- [x] 打开 Glass Lab 时将首页当前材质参数传入，避免滑块从另一组默认数值开始。
- [x] 透明度滑块变化只更新 `transparencyPercent`；模糊滑块变化只更新 `blurRadiusDp`，两者独立。
- [x] Premium / Standard / Lite / Off 预设同步对应材质数值；Off 设置 `enabled = false`，玻璃表面使用浅色回退且不添加单表面 Backdrop 采样节点。
- [x] `GlassMaterialState.enabled` 默认值为 `true`；新增单测覆盖默认开启及 Off 后透明度 / 模糊数值保持独立。
- [x] PR [#22](https://github.com/PaleSquire71029/zhixingapp/pull/22) 已 squash 合并；合并提交：`c473d29d4fb28b1a9c49d46fff79a6c78be3ba86`。
- [x] Android Build（Run ID `41264097084`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41264097084)。
- [ ] 该步骤只完成运行期间的状态同步与 rememberSaveable 恢复；DataStore 的跨进程重启持久化仍未实现。
- [ ] Off 时单个玻璃表面不采样，但首页 `BackdropHost` 目前仍创建共享 LayerBackdrop；整个宿主在 Off 时跳过创建属于后续性能优化。
- [ ] 真机滑块返回首页即时效果、低版本模糊回退、滚动、帧率和电量仍待验收。

**下一小步：** S6.5，持久化 `transparencyPercent`、`blurRadiusDp` 与 `enabled`，进程重启后恢复并支持恢复默认值。

## 24. S6.5 材质设置 DataStore 持久化（2026-11-01）

- [x] 新增 Preferences DataStore 1.3.0 仓库 `GlassMaterialSettingsStore`。
- [x] 保存五个相互独立的材质属性：`transparencyPercent`、`blurRadiusDp`、`enabled`、`refractionEnabled`、`refractionStrengthDp`。
- [x] 默认首页材质：透明度 68%、模糊 12 dp、玻璃开启、折射开启、折射强度 1.8 dp。
- [x] 应用启动后从 DataStore 恢复；首次读取完成前暂不开放材质实验室入口，避免默认值抢先覆盖已保存设置。
- [x] 透明度 / 模糊 / 折射强度拖动时仅更新预览状态，松手后写入 DataStore；折射开关与性能预设立即保存。
- [x] Off 只写入 `enabled = false`，不清空透明度、模糊和折射参数；重新开启可以恢复之前的光学设置。
- [x] 增加“重置材质参数”操作，删除保存项并恢复五项首页默认值。
- [x] 新增 `GlassMaterialSettingsStoreTest`，覆盖默认值、五项保存 / 读取、参数独立性与重置。
- [x] PR [#27](https://github.com/PaleSquire71029/zhixingapp/pull/27) 已 squash 合并；合并提交：`ff20f47119b19d2442515a608e4e612835486abc`。
- [x] Android Build #122（Run ID `41265710542`）结果为 `success`：资源检查、JUnit、androidTest 源码编译、Kotlin 编译、Debug APK 构建与 Artifact 上传均通过。
- [ ] 仍需在真机执行进程强制结束 / 重启，确认 DataStore 真实恢复；需同时检查滑块快速拖动、Off 复开和低版本折射降级。
- [ ] Off 时单个玻璃表面不采样，但首页共享 `BackdropHost` 目前仍会创建 `LayerBackdrop`；整层宿主在 Off 时停止创建仍是后续性能优化。

**当前进度：** S6.1–S6.5 的主代码已合入 main。接下来优先做真机外观 / Insets / 交互 / 性能验收，再决定是否继续扩大光学效果（例如色散、环境反射），不应在未实机验证前叠加复杂动效。

## 25. 2026-11-01：按用户参考 APK 直接移植官方液态玻璃（当前执行路线）

> 本节优先级高于本文早先的 S5 / S6 自研拆分。后续不再为了追求“自己实现”而重复编写官方已有的动画和材质代码。

### 25.1 已确认的参考基准

- [x] 用户上传的 base.apk 已识别为 Backdrop Catalog 应用；APK 中包名为 com.kyant.backdrop.catalog，应用版本为 1.0.0。
- [x] 上游 tag 1.0.6 的 catalog/build.gradle.kts 同样声明该 applicationId、versionCode 1、versionName 1.0.0 和 compileSdk 36。
- [x] 已核对 tag 1.0.6 源码；底栏不是只有一个 lens，而是由 LiquidBottomTabs、DampedDragAnimation、InteractiveHighlight、前景 LayerBackdrop 和 CombinedBackdrop 协同构成。
- [x] 知行当前主分支已经依赖 io.github.kyant0:backdrop:1.0.6，无需先升级到 2.x。
- [x] 确认上游许可证为 Apache-2.0；直接移植时保留版权头、许可证文本并标注修改文件。

### 25.2 移植原则

- 以 tag 1.0.6 为唯一源码基线，不使用会变化的 kmp 主分支版本。
- 直接复制 / 适配已实现的 Catalog 组件，不重新手写同等复杂的拖动动画、交互高光、折射和色散算法。
- 复用现有 Backdrop 依赖；只添加上游 Catalog 所需且当前工程缺失的固定依赖（预期 io.github.kyant0:shapes:1.2.0，以依赖树和 CI 确认）。
- 保留知行 App 的 AppDestinations、四个业务入口、Logo、现有字体策略、浅色主题、DataStore 设置和系统 Insets。
- 背景使用真实图像 / 清晰定义的背景层作为采样源；页面内容继续在其前方绘制。首轮移植使用官方 wallpaper_light.webp 对齐参考 APK 的折射效果，后续再替换成知行品牌背景。
- 不直接复制 Catalog 的 Activity、演示导航、演示页集合或所有演示依赖；只搬移所需组件与必需辅助类。
- 不使用额外渐变尾迹去“假装”官方效果；优先删除与官方组件重复的旧自研装饰动画。

### 25.3 新的实施步骤（一个 PR 一个可构建小步）

| 步骤 | 内容 | 预计修改范围 | 验收要求 |
|---|---|---|---|
| P0 | 冻结旧自研玻璃动效并确认可回滚点 | 文档 / 分支 / 主分支状态 | PR #48 未合并；旧拖尾、独立 Spring、速度拉伸标记为待被官方组件替换 |
| P1 | 直接移植官方 DampedDragAnimation、InteractiveHighlight、DragGestureInspector | 新增隔离源码文件 | ✅ 已完成：与 1.0.6 上游逐字对照（仅包名适配）；PR #49 合并，Android Build #180 成功 |
| P2.1 | 隔离移植官方 LiquidBottomTabs / LiquidBottomTab 源码及局部状态 | 新增 backdrop106/components；固定 shapes 1.2.0 | ✅ 已完成：PR #50 合并，Android Build #183 全部成功 |
| P2.2 | 建立 Zhixing 官方底栏包装层 | 新增 ZhixingCatalogBottomBar.kt | ✅ 已完成：PR #51 合并（commit `ce7539907a666db829de3c32b5c88228e39bd5bb`），Android Build #185 成功；生产 UI 未切换 |
| P2.3.1 | 底栏语义、标签与调用方状态测试 | `ZhixingCatalogBottomBarSemanticsTest.kt` | ✅ 已完成：PR #52 合并，Android Build #192 成功；仪器测试源码完成编译，尚未在实体设备执行 |
| P2.3.2 | 官方拖动切换与边界行为测试 | `ZhixingCatalogBottomBarDragTest.kt` | 当前隔离分支新增四项：左右跨两项、快速反向、首项边界钳制；等待 CI 编译 |
| P2.3.3 | RTL 与布局 / 无障碍回归测试 | Compose 仪器测试 | RTL 排列、窄屏文字与触控区域、不重复暴露采样副本 |
| P2.4.1 | 将官方底栏材质参数开放并映射知行设置 | `LiquidBottomTabs.kt`、`ZhixingCatalogBottomBar.kt` | 透明度 / 模糊 / 折射强度 / 色散 / 触摸反馈分别映射到现有 `GlassMaterialState`；保留上游 1.0.6 默认视觉基线，不让设置成为无效 UI |
| P2.4.2 | 将参数化官方底栏接入首页共享 Backdrop | `MainActivity.kt` 与包装层 | 复用唯一共享 Backdrop、`ZhixingLiquidBottomBarPlacement` 和系统 Insets；保留四项目的地及调用方导航状态 |
| P2.4.3 | 集成回归与视觉验收 | Compose 测试 / 目标真机 | 验证设置调整即时生效、横竖屏和 IME 不遮挡、底栏触摸与拖动、材质清晰度与帧率 |
| P2.5 | 删除旧底栏重复手势与装饰动画 | 旧 ZhixingLiquidBottomBarItems.kt | 只清理官方接管的逻辑，不改业务状态；构建与导航测试通过 |
| P2.6 | 导航交互与性能验收 | UI 测试 / 真机检查记录 | 点击、拖动、取消、快速切换、横竖屏、系统 Insets、帧率通过后才结束 P2 |
| P3 | 移植官方 BackdropDemoScaffold 背景源与对照壁纸 | BackdropHost、drawable、资源声明 | 使用真实可采样背景；先与参考 APK 同壁纸对照，再替换成知行品牌背景 |
| P4 | 建立统一玻璃表面、凸起控件和空间层级 Tokens | GlassSurface、MaterialState、ElevationTokens | 有基础磨砂、折射、色散、高光、阴影、接触暗边；透明度 / 模糊独立 |
| P5 | 批量移植并适配官方 LiquidButton、LiquidSlider、LiquidToggle | 玻璃组件库、Glass Lab | 组件保持官方行为和动画，接入知行品牌色、数据状态、无障碍和设置持久化 |
| P6 | 扩大首页玻璃覆盖 | 首页、顶部栏、主要功能入口卡片、设置入口 | 大部分主要容器和可交互元素采用玻璃 / 立体表面，视觉层级清晰 |
| P7 | 建立统一动画系统并覆盖常见交互 | 动画 Tokens、按钮、卡片、键帽、开关、滑块、面板 | 按压、抬起、拖动、切换、展开、收起、页面转场具有统一物理连续性 |
| P8 | 为计算器、AI、工具、历史页建立玻璃组件适配规范 | 各页面模板与组件层 | 页面迁移时复用统一组件，不在业务页重复实现 Shader / 动效 |
| P9 | 全局视觉对齐、性能分级和目标真机验收 | 测试 / CI / 视觉对照 / 性能档位 | 与参考 APK 同背景、同尺寸近似对照；覆盖 API 能力、帧时间、温升、耗电及可读性 |

### 25.4 关键默认表现对齐表

首轮以官方 Catalog 的基础表现为参照，不先用旧知行数值压低效果：

| 项目 | 官方 Catalog 1.0.6 参考值 | 知行移植要求 |
|---|---:|---|
| 底栏整体模糊 | 8 dp | 首轮 8 dp，可由独立设置调整 |
| 底栏整体透镜 | 24 dp 高 / 24 dp 折射量 | 默认对齐 |
| 选中胶囊按压透镜 | 10 dp 高 / 14 dp 折射量 × pressProgress | 直接使用按压进度驱动 |
| 色散 | 选中胶囊开启 chromatic aberration | 首轮与 Catalog 一致，之后允许用户关闭 |
| 内容面板 | 白色 40% 填充 | 保持浅色、可读并允许透明度调节 |
| 按压进度 | 独立 spring | 不再用单一 tween 代替 |
| 拖动速度 | DampedDragAnimation 内部速度动画 | 使用上游实现，不叠加第二套 VelocityTracker |
| 指尖高光 | InteractiveHighlight + API 33+ RuntimeShader | 直接移植；API 26–32 使用官方 fallback |

注：知行将延续用户要求的浅色模式，不移植 Catalog 中用于深色主题的暗色材质分支。参数设置仍可独立调节；上表是移植验收基线，不是取消设置功能。

### 25.5 完成标准与回滚规则

- P1–P5 每一步 CI 必须通过后才可合并；失败只修本步，不跳到下一项。
- 以相同参考壁纸做 screenshot / 真机视觉比较，避免因为背景不同误判折射质量。
- 不把“源码搬入”说成效果已验收；真机拖动和图形渲染仍须用户检查。
- 若新组件与现有业务语义冲突，优先在包装层适配，不篡改上游核心算法。
- 上游源码文件保留版权头和 Apache-2.0 许可证，修改处写明来源、tag 和变更内容。
- 用户确认参考效果达到预期后，才扩展到其他页面；计算器算法和 AI 服务不在本次移植范围内。

**当前下一步：** P2.2 已完成代码，等待 Android CI。通过后做 P2.3 导航语义与点击测试；目前仍未接入 MainActivity。


### 25.6 全局立体材质系统（这次必须实现，不只停留在底栏）

**总体目标：** 主界面及后续业务页大部分控件都要有玻璃或具有真实空间高度的浅色材质。页面看上去应有明显的前后层次、可见的厚度和光线反馈，而不是把一堆普通白卡片简单加透明度。

统一分层建议：

| 深度层 | 对象 | 表现方式 |
|---|---|---|
| Z0 背景层 | 壁纸、页面背景、柔和环境光 | 提供真实颜色和形状作为折射采样源；不与前景循环采样 |
| Z1 内容承载层 | 页面大区块、消息列表、公式/结果阅读区域 | 浅色磨砂玻璃，较低高度和柔和接触阴影，确保内容对比度 |
| Z2 控件层 | 按钮、输入框、筛选 chips、列表操作、卡片入口 | 边缘折射、高光、内阴影和轻微凸起，触摸时发生压陷 |
| Z3 浮动层 | 底部导航、浮动按钮、工具栏、弹出菜单 | 更强的背景折射与环境反射、更明确的外部阴影和边缘厚度 |
| Z4 面板层 | BottomSheet、Dialog、选择器、材质设置面板 | 独立前景面、背景遮罩、空间抬升和弹性进出场 |

空间表现 Tokens 应统一维护：表面透明度、模糊、折射高度 / 量、色散、边缘高光宽度与角度、环境反射、外部阴影、接触阴影、内阴影、组件高度、按压缩放 / 压陷量。不要为每一个按钮写一套互相冲突的固定颜色和阴影。

### 25.7 动画复刻清单（不仅是底栏动画）

| 动画类别 | 需要还原的行为 | 统一要求 |
|---|---|---|
| 触摸 / 点击 | 接触瞬间的局部高光、控件压陷、释放回弹 | 使用官方 InteractiveHighlight / Spring；位置连续，不延迟触控逻辑 |
| 拖动 | 阻尼跟随、速度响应、拉伸、倾斜 / X-Y 缩放、松手吸附 | 优先复用 DampedDragAnimation；不再叠加独立拖动速度系统 |
| 选中切换 | 底栏胶囊滑动、文字和图标轻微形变、色散随进度出现 | 状态统一驱动，不出现指示器和内容错位 |
| 浮层进出 | 菜单、Dialog、BottomSheet、选择器展开和关闭 | 结合位移、缩放、透明度、背景遮罩及弹性落位 |
| 页面转场 | 页面进入 / 返回、工具切换、历史详情打开 | 轻微空间位移、淡入淡出和连续背景；避免夸张翻页 |
| 卡片 / 按钮状态 | 触摸抬起、按下、禁用、选中、加载 | 通过高度、阴影、反射和表面透明度表现状态，不仅改变颜色 |
| 开关 / 滑块 | 滑块拇指移动、轨道变化、开关拨动和回弹 | 移植官方 LiquidSlider / LiquidToggle 的交互方式 |
| 键盘 / 键帽 | 数字键按压、连按、长按、删除连续反馈 | 每颗键帽独立压陷和反射，输入事件与动画解耦，不能因动画丢键 |
| AI 对话 | 消息出现、输入框聚焦、发送、工具面板展开 | 消息进入轻微上浮，输入面板随键盘连续移动，不能影响流式输出 |
| 滚动 / 视差 | 页面滚动时浮层和背景的轻微相对运动 | 仅对少数层使用克制视差；不能以高频截图制造动画，不影响滚动帧率 |

动画统一参数应集中为知行 motion tokens：按压进入/退出弹簧、阻尼与刚度、速度归一化、缩放范围、浮层进入 / 退出时长、遮罩插值和减少动态效果的降级策略。能直接复用上游动画的地方不重新造同类函数；上游没有覆盖的页面转场再由知行统一补充。

**严禁以“所有元素都动”为目标。** 静态时材质应有空间感；用户触摸、拖动、切换或打开界面时再有针对性地响应。持续循环动画仅限背景中低强度环境光，并受生命周期和性能档位约束。

### 25.8 当前执行入口

下次继续本仓库时，从 main 最新提交和当前 PR 状态开始：

1. P1、P2.1、P2.2 已完成并合入 main；PR #51 对应 Android Build #185 成功。
2. P2.3.1 已完成并合入 main（PR #52，merge commit `4578f54fafd0cfc20f5ba0cdc2aabfdce32ae376`）；Android Build #192 成功。
3. 当前分支 `port/catalog-1.0.6-p2-3-2-drag-tests` 正在执行 P2.3.2，新增四个真实拖动交互场景；等待 CI 后才可合并。
4. 当前 CI 只编译 `androidTest` 源码，不会启动模拟器执行仪器测试；必须区分“测试源码编译通过”与“设备交互通过”。
5. P2.3.2 完成后再做 P2.3.3 RTL / 窄屏 / 无障碍回归。P2.3 完成后按 P2.4.1 材质参数映射、P2.4.2 首页接入、P2.4.3 集成验收的顺序继续。
6. P2.4.1 必须保证当前 `GlassMaterialState` 的透明度、模糊、折射、色散、触摸反馈都有真实效果映射，不能只保留设置 UI 而让官方组件参数写死。
7. 所有步骤继续固定官方 1.0.6 源码、Apache-2.0 归属和固定依赖版本。

**当前仍不修改 MainActivity；生产底栏尚未切换到官方 Catalog 组件。**



### 25.9 本轮进度记录（2026-11-01）

- [x] P2.2 PR #51 合并，commit `ce7539907a666db829de3c32b5c88228e39bd5bb`；Android Build #185（Run ID `41280875247`）全部成功。
- [x] P2.3.1 PR #52 已合并，commit `4578f54fafd0cfc20f5ba0cdc2aabfdce32ae376`；Android Build #192（Run ID `41281478165`）全部成功。
- [x] P2.3.1 新增三项测试：四个 Tab 角色 / 标签 / 选中态、点击回调、调用方外部 selectedIndex 更新。
- [x] P2.3.1 首次编译发现 hasRole API 不存在；已改用 `SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)`，最终 CI 成功。
- [x] P2.3.2 分支 `port/catalog-1.0.6-p2-3-2-drag-tests` 已建立；新增四项拖动测试：左右跨两项、快速反向、最左边界钳制。
- [ ] P2.3.2 当前等待 CI；由于工作流只编译 `androidTest`，新增拖动操作尚未在模拟器或真机实际执行。
- [ ] P2.3.3 尚未开始；生产 MainActivity 仍未切换底栏。

