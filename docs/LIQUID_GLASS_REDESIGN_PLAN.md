# 知行 Android App：主界面液态玻璃重设计专项计划

> 文档版本：1.0
> 建立日期：2026-11-01
> 目标仓库：PaleSquire71029/zhixingapp
> 执行分支：main（规划文档）；后续兼容性试验可使用独立分支
> 状态：规划已建立；实施按阶段推进
> 关联文档：[Android App 总体规划](./ANDROID_APP_MASTER_PLAN.md)、[实施状态账本](./IMPLEMENTATION_STATUS.md)、[项目现状与操作指南](./PROJECT_STATUS_AND_GUIDE.md)

本文专门记录这次液态玻璃重做的设计决策、技术验证、实施顺序、涉及文件、性能策略和验收条件。后续每完成一个阶段，都必须更新本文的阶段状态，并同步更新 \`IMPLEMENTATION_STATUS.md\`。规划中的功能不得描述成已经完成。

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
| Kotlin Compose 插件 | 2.2.10 |
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

- **玻璃属于交互层。** 优先放在底部导航、快捷操作、按钮、开关、滑块、浮动工具入口和弹出面板。
- **内容保持清楚。** 正文、数学公式、计算结果、历史列表等使用清晰的浅色内容面，不叠加多层玻璃。
- **不做玻璃套玻璃。** 同一视觉区域只保留一个主要玻璃容器，减少叠层和文字对比度损失。
- **背景要有可读的光影变化。** 可使用低饱和渐变和少量动态光斑，让折射和透明度有实际内容可采样；背景不能抢正文注意力。
- **边缘高光克制。** 采用轻微明暗差和细边缘反射，不使用粗白边、强彩虹边或夸张发光。
- **品牌优先。** 保留知行 Logo、品牌主色和现有中文排版气质。苹果设计只作为交互与材质参考，不照搬 Apple 图标、文案和布局细节。
- **浅色优先。** 不引入深色玻璃底板，不用大块黑灰区域承载导航。
- **动效有物理连续性。** 选择态位置、形状、光斑和触摸反馈应连续过渡，避免突然切换或固定频率刷新造成的跳动。

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

按以下顺序逐步引入，不一次性给所有组件换材质：

1. 首页/主界面的悬浮底部导航；
2. 页面顶部的轻量快捷操作入口；
3. 主要操作按钮和图标按钮；
4. 玻璃设置入口和材质预览面板；
5. 少量适合悬浮显示的工具控制与弹出菜单；
6. 后续迁移到计算和 AI 页面时，仅应用于工具栏、悬浮操作和必要的交互控件。

不建议玻璃化的元素：长文本卡片、公式正文、数字键盘主体、密集历史列表、图表曲线区域和大面积说明区域。

### 3.4 玻璃材质可调参数

所有参数都需要有稳定的默认值、明确的上下限、无障碍可读性保护和性能档位约束。

| 参数 | 计划范围 | 初始建议 | 说明 |
|---|---:|---:|---|
| 透明程度 | 0–100% | 约 78% | 数值越高，背景可见程度越高；实现时换算为材质覆盖层 Alpha |
| 背景模糊 | 0–24 dp | 12 dp | 与透明程度独立调节；0 表示不模糊 |
| 折射强度 | 0–8 px | 约 2–3 px | 仅在目标 API 和设备性能允许时启用 |
| 边缘高光 | 0–100% | 约 45% | 控制边缘反射强度，不改变整体透明度 |
| 色散 | 0–0.8 px | 默认关闭或极低 | 仅作为可选细节，不能造成明显彩边 |
| 触摸反馈 | 开/关 | 开 | 高光跟随按压位置，结束后平滑消散 |

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
- 本项目不直接复制其 Demo App 的整体界面，而是复用其渲染基础和必要的实现思路。

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



#### 4.2.2 Backdrop 1.0.6：当前优先兼容候选

2026-11-01 的隔离 CI 试验已确认 2.0.1 的 Android AAR / 传递 Compose 组件要求 `compileSdk 37`。而 CI Runner 的 SDK 仓库返回 `Failed to find package 'platforms;android-37'`，暂时无法在当前流水线中安装该平台包。第二轮 API 36 试验虽成功完成 SDK 安装和 Gradle 配置，但在 `checkDebugAarMetadata` 阶段因要求 API 37 而失败；没有进入 Kotlin 源码编译，也没有证明 2.0.1 的源代码本身不兼容。

因此转测 **Backdrop 1.0.6**：

- 上游 tag `1.0.6` 的 Android 工程使用 Android Gradle Plugin 9.0.0、Kotlin Compose 插件 2.3.10、AndroidX Compose 1.10.3 和 `compileSdk 36`；
- 发布制品为 Android 原生 Jetpack Compose 库，不是 2.x 新版 Compose Multiplatform Android variant；
- 仍包含 `rememberLayerBackdrop`、`Modifier.layerBackdrop`、`Modifier.drawBackdrop`、`blur` 和 `lens` 等此次所需 API；
- 许可证仍为 Apache-2.0；
- 试验分支暂时使用 KGP 2.4.21 与现有 AGP 9.1.1 / Gradle 9.3.1，以满足当前 Kotlin 官方兼容矩阵，最终仍以 CI 编译结果为准。

Backdrop 1.0.6 是当前首选候选，不代表已经验证兼容。PR #4 第三轮 CI 正在测试它。若通过，仍需进一步进行真实界面渲染、帧连续性、内存与设备性能测试；若失败，保留 main，不合并试验配置。

参考：
- [Backdrop 1.0.6 release](https://github.com/Kyant0/AndroidLiquidGlass/releases/tag/1.0.6)
- [Backdrop 1.0.6 发布信息与许可证](https://mvnrepository.com/artifact/io.github.kyant0/backdrop/1.0.6)
- [Backdrop 上游源码](https://github.com/Kyant0/AndroidLiquidGlass/tree/1.0.6)

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
| S4.3 | 验证玻璃采样范围和滚动层级 | Android Compose UI 测试/CI | ⏳ 进行中：验证多个玻璃表面共享一个背景，前景内容可见且点击事件正常；采样边界需结合代码层级与真机检查验收 |

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
| S6.1 | 在主 Scaffold 中替换原 NavigationBar | MainActivity.kt | 首页启动即显示玻璃底栏，四个目的地仍可用 |
| S6.2 | 添加设置入口 | 底栏或主界面一个入口 | 不挤占四个导航项，不遮挡品牌区域 |
| S6.3 | 添加透明度单独滑块 | GlassAppearanceSheet.kt | 只影响透明度，不改变模糊值 |
| S6.4 | 添加模糊单独滑块 | 同一设置面板 | 只影响模糊值，0 dp 时关闭模糊计算 |
| S6.5 | 保存和恢复设置 | MaterialState / DataStore 适配 | 进程重建后参数恢复，重置可回到默认值 |

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
