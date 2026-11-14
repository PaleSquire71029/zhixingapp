# 知行 Android App 实施状态

> 最后更新：2026-11-14  
> 当前仓库：PaleSquire71029/zhixingapp  
> 当前分支：main  
> 项目接手指南：[docs/PROJECT_STATUS_AND_GUIDE.md](./PROJECT_STATUS_AND_GUIDE.md)  
> 总规划：[docs/ANDROID_APP_MASTER_PLAN.md](./ANDROID_APP_MASTER_PLAN.md)

本文件是项目状态账本。每次开发结束必须更新，不能只在聊天中报告进度。详细操作步骤、APK 获取和排错方法见 [项目现状与操作指南](./PROJECT_STATUS_AND_GUIDE.md)。

---

## 1. 当前项目状态

**当前进度（核对至 2026-11-14）：** 数学与工具 Domain 已完成 C0.1–C8.7；方程能力已继续推进至 C5.13（弧度制余弦周期方程）；Build #440（Run ID 43552940320）完整 success，JVM 单测、Android 测试源码编译、Debug APK 组装、Artifact 上传及 Actions 清理均通过；C8.8.1–C8.8.2 已将单位换算接入原生「工具」页并完善交互，支持九类切换、输入、源 / 目标选择与交换、即时结果、同类单位一览、完整等式复制、清除输入、未完成数字中间态、64 字符长度边界、窄屏纵向单位选择及 TalkBack 语义。所有换算仍统一调用 `UnitConverter`，未复制转换公式。Build #428（Run ID `43545850658`，代码 SHA `2b62e75f6f20224b50550d0f655d3d511ccc2cd3`）完整 success：JVM 单元测试、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传和 Actions 清理通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 50s`。Debug Artifact `14093618375`，12,612,565 bytes。Build #428 没有执行模拟器 / 真机 instrumentation tests；按用户要求，单位换算页的 iQOO 真机视觉、输入法和触控验收暂缓。Build #427 曾因 Compose semantics 的两个错误导入 / 属性用法编译失败，已在提交 `2b62e75f6f20224b50550d0f655d3d511ccc2cd3` 修复并由 #428 验证。完整主计算器页面（C12）、专用数学输入、函数图像 UI、化学原生迁移、AI 客户端、账号与云历史仍未完成。

| 项目 | 状态 | 说明 |
|---|---|---|
| Android 原生工程 | 已完成 | Kotlin + Jetpack Compose；单一 `:app` 模块；AGP 9.1.1 / Gradle 9.3.1 / JDK 17；compileSdk / targetSdk 36、minSdk 26 |
| 主壳与导航 | 已接入 | 四个一级入口：计算 / 知行 AI / 工具 / 我的；浮动玻璃 Dock、二级页面动画与材质组件已在 main |
| Liquid Glass 真机体验 | 部分真机验收 | 主壳与 PR #88 相关设备验收以用户确认和对应 PR 记录为准；Build / 源码编译本身不等同光学体验验收 |
| 设计 Tokens | 已实现并通过 CI 编译 | 浅色颜色、形状、间距、文字 Tokens 与 ZhixingTheme 已接入；Build #428 编译通过 |
| 表达式数学内核 | C0.1–C4.3 已完成 | Tokenizer、AST / Parser、常数求值、多项式、有理函数定义域 / 求值、一次 / 二次实根 |
| 方程求解 | C5.1–C5.13 已通过 CI | 常数 / 一至三次、分式、绝对值（常数右侧 / 线性右侧，内部式最高二次）、平方根与立方根（受限结构）、受限指数 / 对数、二元一次方程组、基础正弦 / 余弦周期方程 |
| 不等式求解 | C6.1–C6.2 已完成 | 一至三次多项式不等式、分式不等式 |
| 函数 Domain | C7.1–C7.6 已完成 | 坐标点校验、一次 / 二次 / 三次 / 反比例拟合、统一定义域 / 求值 / 安全采样 |
| 单位换算 Domain | C8.1–C8.7 已完成 | 九类目录、76 个线性单位、3 个温度仿射单位、统一入口与旧版兼容性回归 |
| 原生单位换算页面 | C8.8.2 交互完善已通过 CI；真机验收暂缓 | 完整等式复制、清除、输入中间态 / 长度边界、窄屏纵向选择、TalkBack 语义；Build #428 成功；未执行设备验收 |
| 完整计算器主页面 | 未完成 | C12 尚未实施；不把独立单位换算工具页算作 C12 |
| 专用数学输入 / QuestionDocument | 规划已恢复，未完成实现 | 题目文档模型、模板编辑、撤销 / 重做、专用键盘和符号搜索尚待实现 |
| 函数图像 UI | 未完成 | Domain 求值 / 采样已完成；Compose 曲线、坐标轴、视口和交互未接入 |
| 化学配平 / 反应预测 | 未迁移 | 需要原生 Kotlin 算法、守恒与条件回归 |
| 知行 AI Android 客户端 | 未开始 | Worker 移动端安全通道、网络 / SSE、上下文面板尚未接入 |
| 账号与云历史 | 未开始 | Android 认证、本地持久化和云端历史尚未接入 |
| 最新 Debug APK | Build #440 | [Artifact 14097190828](https://github.com/PaleSquire71029/zhixingapp/actions/runs/43552940320/artifacts/14097190828)，12,633,478 bytes；包含 C5.13 余弦周期方程，完整 Actions workflow success |
| Release AAB | 未开始 | 正式签名与发布构建尚未配置 |

| 项目 | 状态 | 说明 |
|---|---|---|
| 新建 App 仓库 | 已完成 | zhixingapp / main |
| 旧项目审计 | 已完成（2026-10-31） | 检查 calculator、zhixing_ai、zhixingapp |
| 旧 AI 交接文档 | 已阅读 | 项目总览、遗漏需求补充 |
| 技术路线 | 已确定 | Android 原生 Kotlin + Jetpack Compose |
| Android 工程配置 | 已提交 | AGP 9.1.1、Gradle 9.3.1、JDK 17 |
| 最小原生首页 | 已提交；修复版待真机验收 | 一级导航现为计算 / 知行 AI / 工具 / 我的；新增我的页与历史、材质入口，业务功能仍按阶段迁移 |
| 浅色主题基线 | 已提交 | 仅浅色，不含深色主题；颜色方案已集中到 ColorTokens.kt |
| 颜色 Tokens | 代码已抽取，待构建验证 | 仅移动既有 lightColorScheme，颜色值没有变化；本轮不生成 APK |
| 间距 Tokens | 代码已抽取，待构建验证 | 常用页面留白、模块间距和内部 padding 集中管理；数值保持不变；本轮不生成 APK |
| 形状 Tokens | 代码已抽取，待构建验证 | 首页两种既有圆角集中管理；28.dp、18.dp 数值不变；本轮不生成 APK |
| 文字 Tokens | 代码已抽取，待构建验证 | 首页现有字号、行高、字重和字距集中管理；不引入字体文件、不改变字体族；本轮不生成 APK |
| 统一主题入口 | 代码已提交，待构建验证 | ZhixingTheme 统一暴露 colors、type、spacing、shapes，并承载 Material 3 浅色主题 |
| 系统栏与安全区 | 代码已提交，待验收 | Edge-to-edge、浅色系统图标、Scaffold 安全区与 adjustResize 已配置 |
| 导航契约单元测试 | 已提交 | 四个入口、路由唯一性和文案完整性 |
| GitHub Actions 构建 | 已通过（Run #32） | 图片资源完整性检查、导航单测、assembleDebug、Artifact 上传、APK 自动归档全部成功；真机待验收 |
| Gradle Wrapper | 已完成 | 已添加 Gradle 9.3.1 wrapper JAR、properties、POSIX 与 Windows 启动脚本 |
| Glass Lab 动态局部背景快照与 AGSL 折射 | 代码已提交，待构建与真机验收 | API 33+ 使用 RuntimeShader 对局部快照做单通道边缘位移；强度 0–8 px 可调；RGB 不分离；API 26–32 回退快照/模糊 |
| 液态玻璃导航 | 主分支已接入，待真机验收 | 已采用浮动玻璃 Dock、Backdrop 背景采样 / 模糊 / 折射与圆润填色导航图标；二级页面使用 AnimatedContent 淡入、轻微横移和缩放。CI 只能证明编译与测试，不代替目标手机上的光学和交互验收 |
| 计算迁移 | C4.3.2、C5.1–C5.7、C6.1–C6.2、C7.1–C7.6 与 C8.1 已通过对应完整 CI | 支持多项式方程 / 不等式、四类函数拟合、统一定义域 / 求值与安全采样；单位侧已建立九类目录和量纲模型，具体单位与数值转换仍待做；Compose 图表 UI 与真机验收待做 |
| AI 接入 | 未开始 | 尚无 Worker 客户端 |
| 账号与云历史 | 未开始 | 尚无原生认证与历史层 |
| Debug APK | 工程版本 0.1.2 / versionCode 3 | 版本配置已更新；Android Build #336 曾成功上传 `zhixingapp-debug` Artifact。仓库直链归档是否已更新须以归档提交核验，不把旧 APK 误标成 0.1.2 |
| Release AAB | 未开始 | 尚未配置正式签名与发布构建 |

## 2. 已完成提交

### 第 1 小步：原生 Compose 工程骨架

- 目标：让仓库成为一个清晰、最小、可持续扩展的原生 Android 项目。
- 文件：settings.gradle.kts、build.gradle.kts、gradle.properties、app/build.gradle.kts、AndroidManifest.xml、strings.xml、MainActivity.kt、AppDestination.kt、AppDestinationTest.kt、.gitignore、GitHub Actions workflow。
- 技术：AGP 9.1.1、Gradle 9.3.1、JDK 17、Compose BOM 2026.06.00、compileSdk/targetSdk 36、minSdk 26。
- UI：浅色知行首页，四个导航入口：计算、知行AI、工具、历史。此时页面只是原生骨架，不代表功能已迁移。
- 测试：配置两个导航契约单元测试。首次 CI 在 Android SDK 初始化阶段失败，原因是 setup-android@v3 请求已下架的 tools 包；尚未运行 Gradle 编译。
- Commit：`7d20f87ec4cbd34be3ad3e28eafacd427760116c`。
- 后续补充：标准 Gradle Wrapper 已在第 2 小步补齐（见提交 SHA）。

## 3. 本轮新增与修复记录（2026-10-31）

- [x] 将用户提供的知行蓝紫渐变标识加入 drawable-nodpi，并配置 adaptive launcher icon（普通与圆形）。
- [x] 首页品牌区显示同一图标。
- [x] 补齐标准 Gradle Wrapper 9.3.1：JAR、properties、gradlew、gradlew.bat。
- [x] 从 GitHub Actions 日志定位真实失败原因：Compose 1.12.1 要求 compileSdk 37，而项目原来编译到 API 36。
- [x] 最初尝试 API 37，但 GitHub Actions sdkmanager 返回 Failed to find package platforms;android-37。官方资料确认 Compose 1.12 要求 API 37，Compose 1.11.3 对应 2026.06 稳定 BOM，因此本阶段改用 2026.06.00 / API 36，先保证工程能在当前 SDK 仓库构建；targetSdk 保持 36。
- [ ] 等待本次提交触发的 CI 结果；若失败，只修这一处，不叠加 Glass Lab。

提交：
- 图标：`148ac1cbf77f9a3b1cdab76d93daa6aca33620bb`
- 首页品牌区：`8d5c0319383a98ad59f7ff3480acdb29efe91e4f`
- Gradle Wrapper：`57eb45f4552b8db3a05e637b861767c7ebbeadad`
- API 37 SDK 包不可安装后的依赖回退与 CI 修复：`2ef20471f186835b37096b18fc8e6968e26d5792`。

## 3.1 小步 3：系统栏、安全区与输入法窗口（2026-10-31）

- [x] 使用 AndroidX Activity 的 edge-to-edge 配置；状态栏与导航栏保持透明，让浅色 App 背景自然延伸到系统栏区域。
- [x] 使用 light system-bar style，状态栏和导航栏使用深色系统图标，匹配浅色主题。
- [x] 关闭 Android 10+ 系统栏自动对比度 scrim，避免系统额外覆盖一层不受控的灰色底色。
- [x] 在 Activity 与 Manifest 中设置 adjustResize，作为后续输入框、AI 对话和自定义键盘的基础。
- [x] 继续由 Material 3 Scaffold / NavigationBar 处理 safe drawing 与导航栏 Insets，不在页面中重复叠加状态栏高度。
- [x] GitHub Actions Run #15 的单元测试和 Debug APK 构建通过。
- [x] zhixingapp-debug Artifact 已上传并取回；APK 文件 SHA-256 已核对。
- [ ] 真机检查状态栏、手势导航、三键导航、横竖屏与键盘弹出/收起。
- 当前只记录代码落地，不将未执行的构建或真机测试标记为通过。

修改文件：
- `app/src/main/java/ci/us/zhixing/app/MainActivity.kt`
- `app/src/main/AndroidManifest.xml`

## 3.2 小步 3 构建验收结果（2026-10-31）

- GitHub Actions：Android Build #15
- Run ID：`41206049511`
- 结果：`success`
- 检查项：JDK 17、Android SDK API 36、Build Tools 36.0.0、Gradle Wrapper、导航单元测试、Debug APK assemble、Artifact 上传，全部成功。
- PR 验证提交：`5f9a75de1a33cf267855e1547d6d8830ce41517d`
- Artifact：`zhixingapp-debug`，Artifact ID `13044483076`
- APK：`app-debug.apk`，11,491,233 bytes
- SHA-256：`218fe484bffe249e39b02006184512f4a4a837ee37809b527aed712b0fde7785`
- Actions 页面：[查看 Android Build #15](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41206049511)
- APK 是 Debug 预览包；测试通过不等于已在实际手机上完成启动、系统栏和键盘验收。
- 验收清单：[docs/APK_TEST_CHECKLIST.md](./APK_TEST_CHECKLIST.md)

## 3.3 主分支 APK 自动归档（2026-10-31）

- [x] 主分支 CI 在单元测试和 Debug APK 构建成功后自动复制 APK 到 `artifacts/zhixingapp-preview.apk`。
- [x] APK 已随自动化构建提交到 main。
- [x] GitHub 仓库 Git Tree 已确认该路径存在，文件大小 11,491,233 bytes。
- Git blob SHA：`a86068d623390ea92c9d5a09c7b25a2d938b80d9`
- 直接下载：<https://raw.githubusercontent.com/PaleSquire71029/zhixingapp/main/artifacts/zhixingapp-preview.apk>
- 对 `artifacts/**` 配置 paths-ignore，避免归档提交再次触发 CI 循环。
- Actions Run：[#22](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41206736514)
- 注意：这是 Debug 预览版，真机安装与视觉验收仍待用户进行。

## 3.4 启动闪退与图标缺失修复（2026-10-31）

- [x] 从已归档 APK 中提取 `res/drawable-nodpi-v4/zhixing_logo.webp` 检查文件头。
- [x] 确认旧文件长度为 5,193 bytes，但 RIFF 声明的总长度为 9,614 bytes；PIL 无法解码该图片。
- [x] 确认该图片同时被首页 `painterResource(R.drawable.zhixing_logo)` 和 launcher adaptive icon XML 引用。
- [x] 从用户原始图标重新提取透明背景并编码为有效 WebP；替换同一资源路径，保持 Kotlin 与 XML 资源名称不变。
- [x] 将 versionCode 提升至 2、versionName 提升至 0.1.1，避免与上一版混淆。
- [x] 增加 `scripts/verify_android_resources.py`，在 Gradle 编译前验证 WebP RIFF 长度、PNG chunk CRC、JPEG 首尾标记和 launcher 资源引用。
- [x] PR #3 已合并到 main（合并提交：`7bdedf7ff1c4259166be75ac75588f83c46c2fca`）。
- [x] Android Build #32 的图片资源校验、单元测试和 APK 构建通过。
- [x] `artifacts/zhixingapp-preview.apk` 已被 0.1.1 修复版覆盖（11,490,765 bytes）。
- [x] APK 内部检查确认 launcher XML 与新的 WebP 资源均存在，WebP RIFF 长度完整。
- [ ] 在用户 Android 真机安装、启动并确认桌面图标显示。

**判断：** 资源截断是两个问题的共同明确原因。修复版 CI 已通过且新 APK 已归档；最终是否彻底解决闪退仍以用户真机启动为准。

## 3.5 修复合入主分支（2026-10-31）

- PR：[#3 修复启动闪退与图标](https://github.com/PaleSquire71029/zhixingapp/pull/3)，已合并。
- 合并提交：`7bdedf7ff1c4259166be75ac75588f83c46c2fca`
- 修复资源：`app/src/main/res/drawable-nodpi/zhixing_logo.webp`
- Git blob SHA：`ceebc900762d478fae30eebd68dcc1f7b1a57893`
- GitHub 树中资源大小：4,728 bytes；拉取后逐字节复核 RIFF 声明长度 4,728 bytes，WebP chunk 遍历完整。
- 新版本：0.1.1 / versionCode 2。
- CI 资源校验脚本：`scripts/verify_android_resources.py`。
- [ ] 主分支 Build 对该合并版本完成资源检查、单元测试、Debug APK 构建与自动归档。
- [ ] 用户手机确认可以启动且桌面图标显示。

## 3.6 0.1.1 修复版构建与归档结果（2026-10-31）

- GitHub Actions：Android Build #32
- Run ID：`41208283410`
- 结果：`success`
- 检查步骤：JDK 17、Android SDK API 36、图片资源完整性校验、Gradle、导航单元测试、Debug APK 打包、Artifact 上传、主分支 APK 自动归档，全部成功。
- Actions：[查看 Build #32](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41208283410)
- Artifact ID：`13045686024`
- APK：`app-debug.apk`，11,490,765 bytes
- APK SHA-256：`d20ff37aaa0b6eb54d19dff1d2bcb5ea3c9f69bd17ffc6d38572bf0e2643d9ba`
- 仓库文件：`artifacts/zhixingapp-preview.apk`
- Git blob SHA：`684074dbd41938feef7ff871f86abea3c4a2ea72`
- 版本：`0.1.1`，versionCode `2`
- [直接下载 0.1.1 Debug APK](https://raw.githubusercontent.com/PaleSquire71029/zhixingapp/main/artifacts/zhixingapp-preview.apk)
- 设备端安装、首次启动、桌面图标呈现仍待用户验收。

## 4. 下一步拆分（一次只做一个小项）

### 第 2 小步：补齐 Gradle Wrapper

- [x] 加入 wrapper properties、gradlew、gradlew.bat 和 wrapper JAR。
- [x] 将 CI 切换为 ./gradlew。
- [x] Wrapper 配置指向 Gradle 9.3.1；本地/CI 实际执行结果仍以构建日志为准。

### 第 3 小步：整理系统窗口与安全区

- [x] 已提交 edge-to-edge、浅色系统图标与 adjustResize 配置。
- [x] 使用 Compose Scaffold 组织窗口内容与底部导航。
- [ ] 通过 CI 构建与单元测试。
- [ ] 在目标真机检查状态栏、手势/三键导航、键盘弹出和横竖屏。
- [ ] 本小步不加入玻璃效果。

### 第 4 小步：把首页占位样式拆成设计组件

- [x] 抽取知行颜色常量到 `ui/theme/ColorTokens.kt`；原有颜色值保持不变。
- [ ] 抽取页面间距和圆角。
- [ ] 抽取基础标题、说明文字和分隔样式。
- [ ] 不增加复杂动画。

### 第 5 小步：建立 Glass Lab 静态页面

- [ ] 新建单独实验入口。
- [ ] 先制作纯浅色背景和一个玻璃容器。
- [ ] 记录目标设备和 Android API。
- [ ] 暂时不影响主导航。

### 第 6 小步：验证背景采样

- [ ] 加入简单可识别的彩色背景内容。
- [ ] 验证玻璃层是否正确采集背景。
- [ ] 检查滚动、裁切与层级。

### 第 7 小步：验证基础模糊

- [ ] 加入可调 blur 参数。
- [ ] 比较不同强度。
- [ ] 确认文字对比度和 GPU 成本。

### 第 8 小步：验证单通道边缘折射

- [ ] 单独实现一个 AGSL 位移 Shader。
- [ ] 只作用于一个实验玻璃容器。
- [ ] 暂不增加 RGB 色散和复杂交互。

### 第 9 小步：增加边缘高光与轻微色散

- [ ] 分别验证高光和色散。
- [ ] 默认强度克制。
- [ ] 对照无特效截图。

### 第 10 小步：增加触摸反馈

- [ ] 先做按压反馈。
- [ ] 再做拖动高光。
- [ ] 最后才评估液体融合动画。

### 第 11 小步：性能档位

- [ ] Premium / Standard / Lite / Off。
- [ ] 测量帧时间和温度。
- [ ] 设备不支持时自动降级。

完成以上实验并确认性能后，再把 GlassSurface 抽象成主 UI 组件。

### 3.7 设计系统第 1 小项：抽取浅色颜色 Tokens（2026-10-31）

- [x] 新建 `app/src/main/java/ci/us/zhixing/app/ui/theme/ColorTokens.kt`。
- [x] 将 `ZhixingLightColors` 从 `MainActivity.kt` 移出，并通过 import 引用。
- [x] 所有颜色数值原样保留；不更换色彩、不改变当前页面样式。
- [x] 本轮提交使用 `[skip ci]`，按用户要求不触发 APK 构建，不创建新 APK。
- [ ] 后续正常代码验证：运行导航单元测试与编译检查；该验证应与用户允许创建下一份 APK 的轮次分开进行。

本项只完成颜色集中管理；字体、圆角、间距、Glass Lab 均未处理。

### 3.8 设计系统第 2 小项：抽取间距 Tokens（2026-10-31）

- [x] 新建 `app/src/main/java/ci/us/zhixing/app/ui/theme/SpacingTokens.kt`。
- [x] 将首页页面边距、区块间距、标题间距、徽标内边距、主内容区内边距、功能条内边距和微小间隔集中到 `ZhixingSpacing`。
- [x] `MainActivity.kt` 改为引用间距 Tokens；未改变任何既有 dp 数值或布局顺序。
- [x] 图标高度、圆角半径、边框宽度、导航栏 elevation 等非间距尺寸仍留在原处，避免扩大本轮范围。
- [x] 本轮提交使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 后续统一安排一次构建验证；在用户允许生成 APK 前不执行构建。

本项只集中管理现有间距；颜色、字体、形状和页面视觉效果不变。


### 3.9 设计系统第 3 小项：抽取形状 Tokens（2026-10-31）

- [x] 新建 `app/src/main/java/ci/us/zhixing/app/ui/theme/ShapeTokens.kt`。
- [x] 将首页主内容面板的 28.dp 圆角与功能条的 18.dp 圆角集中到 `ZhixingShapes`。
- [x] `MainActivity.kt` 改为引用 `ZhixingShapes.hero` 与 `ZhixingShapes.feature`；数值和布局未改变。
- [x] 圆形头像/标记仍使用 `CircleShape`，不纳入本次修改。
- [x] 本轮提交使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 后续统一安排构建验证；在用户允许生成 APK 前不执行构建。

本项只集中管理现有圆角；颜色、间距、字体和页面视觉效果不变。

## 4. 后续功能仍按小项执行

### 计算器
1. 建立表达式测试样例；
2. 迁移 tokenizer；
3. 迁移 parser；
4. 迁移基础求值；
5. 迁移科学函数；
6. 处理幂和负号优先级；
7. 迁移方程；
8. 迁移不等式；
9. 处理分母零点；
10. 迁移函数图像；
11. 迁移单位换算；
12. 迁移化学配平；
13. 迁移反应预测；
14. 迁移本地历史；
15. 实现数学键盘。

每一个小项都必须有独立测试；禁止一次性重写全部计算器。

### 知行AI
1. 核对 Worker 健康检查；
2. 核对模型列表；
3. 实现非流式请求；
4. 实现 SSE 分段解析；
5. 实现停止请求；
6. 实现错误恢复；
7. 实现 Markdown；
8. 实现 LaTeX；
9. 实现图片选择；
10. 实现文件选择；
11. 实现计算器上下文传递。

### 账号与历史
1. 画出原生认证状态流；
2. 验证用户名登录；
3. 实现登录 API；
4. 实现 Token 安全保存；
5. 实现 Token 刷新；
6. 实现登出；
7. 核验 calculator-ai 产品 ID；
8. 设计 SQL migration；
9. 实现历史列表；
10. 实现消息加载；
11. 实现幂等保存；
12. 实现删除；
13. 测试用户隔离。

---

## 5. 当前阻断项

- [x] 已确认 API 36 / Compose BOM 2026.06.00 配置下 GitHub Actions Android 构建成功（Run #15）。
- [x] 标准 Gradle Wrapper 已补齐；当前 Wrapper 版本为 Gradle 9.3.1。
- [ ] 目标真机和 Android API 版本待确认/实测；当前用户反馈首版 APK 启动即闪退。
- [ ] 液态玻璃库的正式引入仍未决定。
- [ ] Worker 的 calculator-ai app_id 分流仍未修复。
- [ ] 云历史 app_id SQL 约束仍未迁移。

## 6. 每次开发记录模板

### YYYY-MM-DD / 第 N 小步 / 功能名称

- 目标：
- 修改文件：
- Commit SHA：
- 构建命令：
- 自动化测试结果：
- 真机型号与 Android API：
- UI / 性能验证：
- 安全检查：
- 未完成项：
- 是否进入下一小步：

---

## 7. 维护要求

- 每次只选一个小步实施；如同一步出现构建问题，先修复，不继续堆新功能。
- 小步完成后更新本文件。
- 未执行的测试保持“待验证”。
- 只通过模拟器不能标记为真机通过。
- 不得把“代码已写”记为“已构建”或“已验证”。
- 新对话接续项目时，先读取本文件和主规划，再检查仓库最新 commit 及 CI 结果。


### 3.10 设计系统第 4 小项：抽取文字 Tokens（2026-10-31）

- [x] 新建 `app/src/main/java/ci/us/zhixing/app/ui/theme/TypeTokens.kt`，集中管理品牌标题、标题、正文、辅助文字、徽标与导航符号样式。
- [x] 将 `MainActivity.kt` 中对应的内联字号、字重与 Material 3 默认文字样式引用改为 `ZhixingType`。
- [x] 保留现有字号、行高、字重、字距和默认字体回退；不添加字体资源，不改变后续字体选择。
- [x] 本轮提交使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 后续统一安排编译与 UI 验证；在用户允许生成 APK 前不执行构建。

本项只集中管理当前首页实际使用的文字层级；颜色、间距、形状和页面布局不变。


### 3.11 设计系统第 5 小项：建立 ZhixingTheme 统一主题入口（2026-10-31）

- [x] 新建 `app/src/main/java/ci/us/zhixing/app/ui/theme/ZhixingTheme.kt`。
- [x] 统一暴露 `colors`、`type`、`spacing`、`shapes` 四组 Tokens。
- [x] `MainActivity.kt` 改为通过 `ZhixingTheme { ... }` 应用主题，并通过 `ZhixingTheme.*` 访问四组设计 Tokens。
- [x] 保留 Material 3 未显式设置的默认 typography / shapes，避免影响导航标签等组件原有外观。
- [x] 对照原 Material 3 样式修正文字角色：品牌副标题与徽标保留 Medium 字重；功能项标题保留 bodyMedium 的 0.25.sp 字距。
- [x] 本轮使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 后续安排一次编译、导航单测与 UI 检查；真机验收仍单独记录。

本项只统一主题访问入口并校准文字样式映射，不增加页面、依赖或玻璃特效。


### 3.12 Glass Lab 第 1 小项：静态材质实验页（2026-10-31）

- [x] 新建 `app/src/main/java/ci/us/zhixing/app/ui/lab/GlassLabScreen.kt`，作为独立实验页面，不覆盖首页或四个主导航入口。
- [x] 首页加入轻量的“打开材质实验室”入口；实验页提供页面内返回按钮。
- [x] 接入系统返回键：实验页打开时按 Android 返回键会回到主界面。
- [x] 使用浅色渐变画布、彩色背景形状和白色半透明样本层，先观察透明度、边缘轮廓与文字可读性。
- [x] 所有颜色、文字、间距和形状均使用 `ZhixingTheme` Tokens；没有加入深色面板、第三方依赖或真实渲染特效。
- [x] 明确标注当前只是静态叠层示意，不包含实时背景采样、模糊、折射、色散或动态效果。
- [x] 本轮使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 待后续统一执行编译、导航与页面构建验证，并在真机检查窄屏和滚动。

本轮只建立静态实验入口与基础构图，尚不能称为已完成 Liquid Glass 材质。


### 3.13 Glass Lab 第 2 小项：背景层模糊对照（2026-10-31）

- [x] 将实验样本后方的彩色图形整理为独立背景绘制层。
- [x] 增加 10.dp 模糊 / 清晰对照开关；Android 12（API 31）以下自动保持清晰背景，不调用不受支持的系统模糊效果。
- [x] 半透明前景样本与文字独立绘制，不应用 blur，方便检查文字可读性和边缘轮廓。
- [x] 继续使用浅色渐变、知行主题 Tokens 与圆角裁切，不加入折射、色散、动态高光或第三方依赖。
- [x] 页面内明确说明：Compose `Modifier.blur` 只模糊目标组件自身绘制内容，不等于捕获任意兄弟组件或窗口的 backdrop blur。
- [x] 本轮使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 后续真机检查模糊边缘、圆角裁切、滚动性能与温度；真正 backdrop capture 需要单独技术验证。

参考：[Android Developers — Blurring in Compose](https://developer.android.com/develop/ui/compose/graphics/blur/overview)。

本项验证的是独立背景层模糊与前景清晰度，不应宣称已经实现真正的跨组件实时背景采样。


### 3.14 Glass Lab 第 3 小项：GraphicsLayer 局部背景快照（2026-10-31）

- [x] 使用 `rememberGraphicsLayer()` 与 `Modifier.drawWithContent` 录制实验背景 Composable 的实际绘制结果。
- [x] 使用 `GraphicsLayer.toImageBitmap()` 导出快照；首次进入时自动采集，并提供“重新采样背景”手动刷新。
- [x] 按玻璃面板在样本画布中的位置裁切图像源区域，使显示内容对应面板背后的图形，而不是把整张背景压缩进面板。
- [x] 快照图像作为玻璃面板底层，可切换 10.dp 模糊 / 清晰状态；白色浅色叠层和文字单独绘制，避免文字被模糊。
- [x] API 31 以下仍展示背景快照，但禁用系统模糊，不引入 CPU 每帧处理或第三方依赖。
- [x] 明确记录边界：只采集此实验 Composable，快照按需刷新，不捕获其他页面、任意兄弟组件、系统窗口或连续实时画面。
- [x] 本轮使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 待编译验证图形层 API 与裁切代码；真机检查采样位置、圆角边缘、滚动性能、内存与温度。

参考：[Android 官方 Compose GraphicsLayer 位图捕获示例](https://developer.android.com/develop/ui/compose/graphics/draw/modifiers#write-contents-of-a-composable-to-a-bitmap)。

这是一项受控区域的真实绘制快照实验，不应标记为全窗口、跨组件或实时动态 backdrop。


### 3.15 Glass Lab 第 4 小项：背景快照裁切边界测试（2026-10-31）

- [x] 将面板源区域计算抽取为纯函数 `calculateBackdropCrop`，返回明确的像素矩形。
- [x] 新增 `GlassBackdropCropTest`，覆盖正常居中裁切、极小图像边界和无效输入。
- [x] 绘制层只在裁切矩形有效时执行 `drawImage`，避免零尺寸或越界输入。
- [x] 不引入新依赖；本轮仍使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 后续统一运行 JVM 单测和 Android 编译；测试代码当前仅已提交，未声称已通过。

本项只保护背景快照的像素裁切计算，不改变 Glass Lab 的视觉参数。


### 3.16 Glass Lab 第 5 小项：前台限频动态背景快照（2026-11-01）

- [x] 新增默认关闭的动态采样开关；启用后以轻微移动的背景形状验证快照更新。
- [x] 动态快照频率限制为每 1 秒最多一次，避免以每帧导出位图的方式持续占用内存与图形资源。
- [x] 通过 Activity `Lifecycle` / `LifecycleEventObserver` 监听 `ON_START` 与 `ON_STOP`；App 停止时取消动态采样循环。
- [x] 页面离开组合时，`LaunchedEffect` 和组合级 CoroutineScope 会随 Composition 生命周期取消。
- [x] 使用互斥锁串行化手动与动态采样，避免同一 GraphicsLayer 上并发导出快照。
- [x] 显示采样成功次数与暂停状态；手动采样入口仍保留。
- [x] 本轮使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 待编译与 JUnit 验证 Lifecycle / Compose API；真机检查 1 Hz 采样的帧率、内存、温度和后台暂停行为。

参考：[LifecycleEventObserver](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleEventObserver)、[Compose side-effects](https://developer.android.com/develop/ui/compose/side-effects)。

动态采样只演示受控实验 Composable 的离散快照更新，仍不等于整个 App 的实时 backdrop。


### 3.17 Glass Lab 第 6 小项：AGSL 单通道边缘折射（2026-11-01）

- [x] 新增独立 `SingleChannelRefraction.kt`，将 RuntimeShader、BitmapShader 输入和 AGSL 源码隔离在实验模块。
- [x] API 33+（Android 13）创建 RuntimeShader；API 26–32 不创建 Shader，沿用局部快照和可用的模糊回退。
- [x] AGSL 只对采样坐标做轻微径向边缘位移；所有 RGB 通道共享同一采样点，不含色散或颜色分离。
- [x] 折射强度限制在 0–8 px，提供 Slider；折射开关可独立启用/关闭。
- [x] 复用已编译 RuntimeShader，通过 BitmapShader 更新局部快照输入；不在每次绘制时重新编译 AGSL 源码。
- [x] AGSL 初始化失败时退回普通快照绘制，并显示状态信息。
- [x] 本轮使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 待执行 Android 编译与 JUnit；真机检查折射方向、边缘连续性、圆角裁切、Shader 发热与掉帧。

参考：[Android 官方 AGSL + Compose ShaderBrush 示例](https://developer.android.com/develop/ui/views/graphics/agsl/using-agsl#using-runtime-shader-with-jetpack-compose)、[RuntimeShader API（API 33+）](https://developer.android.com/reference/android/graphics/RuntimeShader)。

当前实验只实现单通道采样坐标位移，不包含色散、高光、触摸形变或正式界面组件。



### 3.18 Glass Lab 第 7 小项：修正非正方形面板的折射方向（2026-11-01）

- [x] 将 AGSL 边缘强度掩模与实际像素位移方向分开计算；边缘强度仍按面板归一化坐标计算，位移方向改为面板中心到当前像素的真实像素向量。
- [x] 修正宽屏 / 高屏面板上归一化坐标造成的方向偏斜；最大位移仍受 0–8 px 强度限制。
- [x] 在纯 Kotlin calculateRefractionOffset 中镜像 AGSL 的几何计算，供 JVM 单元测试验证；该函数不参与运行时 Shader 绘制。
- [x] 扩展 RefractionMathTest，覆盖中心与内部零位移、非正方形面板方向、最大位移上限、无效尺寸与非有限坐标。
- [x] 保持 RGB 共用采样坐标；不增加色散、高光、触摸反馈或第三方依赖。
- [x] 本轮使用 [skip ci]，不触发构建、不生成 APK。
- [ ] 待执行 JUnit 与 Android 编译；仍需真机检查视觉方向、边缘过渡和性能。纯 Kotlin 检查不能代替 AGSL 编译或 GPU 实测。

本项是对现有单通道折射几何的纠偏，不代表 Shader 已通过 Android 设备运行验收。


### 3.19 Glass Lab 第 8 小项：方向性边缘高光（2026-11-01）

- [x] 在现有 AGSL 单通道 Shader 中加入独立边缘高光参数；不新增 Shader 程序或第三方依赖。
- [x] 高光采用浅白色软边，以上沿与左缘为主、右缘与下沿极弱的方向性轮廓；由 Compose 面板圆角裁切约束最终形状。
- [x] 高光宽度按 dp 转换为像素，使用柔和过渡带；强度参数上限为 0.46，避免覆盖主体背景。
- [x] 新增“边缘高光”独立开关；可在折射强度为 0 时单独观察高光，也可关闭高光单独观察折射。
- [x] Android 13 / API 33 以下禁用该 AGSL 高光开关，不创建 RuntimeShader；保留快照与模糊降级。
- [x] 新增纯 Kotlin 边缘强度镜像函数与测试，覆盖上沿、左右方向差异、内部无高光和非法输入。
- [x] 本轮使用 [skip ci]，不触发构建、不生成 APK。
- [ ] 待运行 JVM 测试与 Android 编译；真机检查高光是否过亮、圆角是否自然、模糊开关对高光的影响及 GPU 性能。

本项实现的是实验页内的受控局部轮廓高光，不是系统窗口级材质边缘，也没有加入色散或触摸高光。


### 3.20 Glass Lab 第 9 小项：默认关闭的微量 RGB 色散（2026-11-01）

- [x] 在现有 AGSL RuntimeShader 中增加色散采样，不创建第二个 Shader。
- [x] 仅对红、蓝通道进行方向相反的微移，绿色通道保持折射后的中心采样坐标。
- [x] 色散掩模沿用玻璃边缘强度，避免在面板中心出现明显色差。
- [x] 采样偏移先按面板局部像素计算，再按 sourceSize / resolution 转换到背景快照采样坐标，避免快照缩放比例改变实际色散大小。
- [x] 色散最大偏移限制为 0.8 px；默认值 0.25 px；开关默认关闭，并提供 0–0.8 px 调节。
- [x] 关闭色散时通过 uniform 分支只执行一次背景采样，避免额外的 RGB 三次采样成本。
- [x] 新增纯 Kotlin 色散偏移镜像函数与单元测试，覆盖强度边界、中心静止区、边缘上限、方向与无效坐标。
- [x] 本轮使用 [skip ci]，不触发构建、不生成 APK。
- [ ] 待运行 JVM 测试与 Android 编译；真机检查色边是否过于明显、背景细节是否出现彩边、动态快照下的 GPU / 帧率表现。

色散属于独立可选实验，默认关闭；未加入彩虹渐变、全屏色差或触摸形变。


### 3.21 Glass Lab 第 10 小项：指尖跟随高光与轻量按压反馈（2026-11-01）

- [x] 在玻璃样本容器上监听单指按下、移动和抬起；不消费 PointerEvent，让外层纵向滚动仍可接收手势。
- [x] 将面板内触摸坐标归一化为局部位置，驱动同一 AGSL Shader 的柔和径向高光；高光半径约 68 dp。
- [x] 按下时面板轻微缩至 0.992 倍，并提高触摸高光；松开后通过短时动画恢复尺寸并淡出高光。
- [x] 触摸高光与边缘高光、折射及色散互相独立；不会修改采样图像，也不触发重新采样。
- [x] 无额外依赖；不增加持续动画，不消费滚动手势；API 33 以下没有 AGSL 触摸高光，仍可浏览快照与模糊回退。
- [x] 新增触摸高光强度与径向衰减纯 Kotlin 镜像函数、5 项 JVM 测试。
- [x] 本轮使用 [skip ci]，不触发构建、不生成 APK。
- [ ] 待执行 Android 编译 / JUnit；真机检查滑动时页面滚动是否正常、单指拖动是否稳定、取消手势时状态复位、触摸反馈延迟和 GPU 表现。

本项只对 Glass Lab 的样本面板添加短时触摸反馈，不代表已实现通用玻璃组件或全页面交互。


### 3.22 Glass Lab 第 11 小项：Premium / Standard / Lite / Off 性能档位（2026-11-01）

- [x] 新增纯 Kotlin GlassPerformanceTier / GlassPerformanceConfig 预设模型，不引入额外依赖。
- [x] Premium：启用 1 Hz 动态局部快照、10 dp 模糊、3.5 px 折射、方向性边缘高光和触摸反馈；色散仍默认关闭。
- [x] Standard：静态背景快照、6 dp 模糊、1.8 px 折射、边缘高光与触摸反馈；色散默认关闭。
- [x] Lite：保留一次性局部背景快照和较高透明度叠层，不运行模糊 / AGSL Shader，也不启用动态采样和触摸反馈。
- [x] Off：停止背景采样并隐藏背景快照，使用 0.96 alpha 浅色面板；不运行玻璃 Shader、模糊或触摸反馈。
- [x] 根据系统 API 自动降级：API 33 以下不启用 RuntimeShader；API 31 以下不启用 Modifier.blur；低版本仍保留安全的快照 / 纯浅色回退。
- [x] 增加档位选择 UI；手动修改单个参数后标记为“自定义”，重新选择档位可恢复整组预设。
- [x] 手动开启动态采样时自动允许背景捕获；Off 模式禁用手动重新采样。
- [x] 增加纯 Kotlin 档位解析测试，覆盖四档配置与低版本能力回退。
- [x] 本轮使用 [skip ci]，不触发构建、不生成 APK。
- [ ] 待执行 JUnit / Android 编译；真机测量各档位帧率、内存、温度、耗电与滑动响应。

这四档是 Glass Lab 的资源策略预设，非系统自动性能检测；Premium 不代表保证满帧，最终档位需根据目标设备实测调整。


### 3.23 主分支完整性复核与 APK 构建验收（2026-11-01）

- [x] 核对 main 当前 HEAD 为 `a3f21e3af6a68bf7acba254fc07f9f67f0f834d2`。
- [x] 与上一稳定节点 `15b38eb8b6f9601f48633628b1d32f1ab20f6dbb` 比较最近 6 个提交：均为新增 / 修改，没有删除文件。
- [x] 确认 `MainActivity.kt`、`AppDestination.kt`、`ZhixingTheme.kt`、四个主题 Tokens、用户提供的 `zhixing_logo.webp`、Manifest 启动图标引用均保留。
- [x] 确认四个底部导航入口仍在，Glass Lab 入口仍在主界面。
- [x] 确认 Gradle Wrapper、AGP / Kotlin Compose 插件配置、Android SDK 36、JDK 17、资源完整性脚本及原有测试文件均存在。
- [x] 确认 Android Build 工作流同时执行资源校验、`:app:testDebugUnitTest` 和 `:app:assembleDebug`，成功后上传 Artifact 并更新仓库内 APK。
- [x] GitHub Actions Android Build #35（Run ID `41222655740`，构建源提交 `54fb057954f62aadec3f9751c5becc2f4d16c80d`）完整成功。
- [x] Android 图片资源完整性校验通过。
- [x] `:app:testDebugUnitTest` 通过；`:app:assembleDebug` 通过。
- [x] `zhixingapp-debug` Artifact 上传成功（Artifact ID `13050204041`）；本次生成 `app-debug.apk` 为 11,589,185 bytes。
- [x] 工作流已将新 APK 提交到 `artifacts/zhixingapp-preview.apk`；主分支文件大小与本次 Artifact 中提取的 APK 一致。
- [x] 提取 APK 后检查 ZIP 结构，确认 `AndroidManifest.xml`、`resources.arsc` 及 8 个 DEX 文件存在；SHA-256：`1b0cd79c2aabf914c1c8bcd0f4ff8d1d61b42a4bed726c14bf2af93d7ccc6c8e`。
- [ ] 待用户在目标 Android 真机安装，检查启动、Glass Lab 四档切换、触摸、滚动、发热和耗电。

本节是仓库源码与配置的静态核对记录，不代表 APK 已经构建或真机运行通过。

---

## 10. 液态玻璃主界面重设计专项（2026-11-01）

**专项计划：** [LIQUID_GLASS_REDESIGN_PLAN.md](./LIQUID_GLASS_REDESIGN_PLAN.md)

- [x] 已建立专项设计与实施计划，包含现状、卡顿根因、开源依赖评估、UI方案、技术结构、9个实施阶段、验收标准和回滚规则。
- [x] 已确认当前玻璃实验室的跳帧根因：动态背景每 1 秒才捕获一次 ImageBitmap，面板显示的是过期快照。
- [x] 已核对当前工程版本：AGP 9.1.1、Kotlin Compose 插件 2.2.10、Gradle 9.3.1、Compose BOM 2026.06.00、compileSdk 36。
- [x] 已核对 Kyant0/AndroidLiquidGlass 的许可证与发布信息：Apache-2.0；库提供 Backdrop 渲染基础，但没有可直接依赖的完整高层导航组件，底栏需要由知行自行实现。
- [x] 已发现 Backdrop 2.0.0 / 2.0.1 与当前 Kotlin、Compose、SDK 构建环境有版本差异，需要先做兼容性验证。
- [ ] 阶段 1：在隔离分支完成开源库依赖解析和最小 Android 编译验证。
- [ ] 阶段 2：用共享实时 Backdrop 替换每秒 Bitmap 快照，解决玻璃内动态内容跳帧。
- [ ] 阶段 3：封装知行玻璃基础组件与独立透明度/模糊参数。
- [ ] 阶段 4：实现四入口悬浮液态玻璃底栏及基础动画。
- [ ] 阶段 5：集成到底部主导航并调整系统 Insets。
- [ ] 阶段 6：加入可保存的材质设置面板。
- [ ] 阶段 7：增强触摸反馈、交互形变及更多快捷玻璃控件。
- [ ] 阶段 8：完成性能档位和 API 降级。
- [ ] 阶段 9：完成 CI、Debug 编译与目标真机验收。

**当前状态：** 专项计划与兼容性验证已完成；Backdrop 1.0.6 已在隔离 PR 的 CI 中通过编译/单测/Debug 构建，正在整理为正式依赖基线。生产主界面导航尚未修改；没有生成提交到 main 的 APK。

### 液态玻璃专项进度补充（2026-11-01）

- [x] Backdrop 2.0.1 开源库审计完成，确认 Apache-2.0 及其 GraphicsLayer / Modifier Node 背景绘制实现。
- [x] 试验确认 2.0.1 Android artifact 与 Compose 1.12.0 依赖要求 compileSdk 37。
- [x] 记录 CI 环境限制：当前 Runner SDK 仓库无法找到 `platforms;android-37`；API 36 安装成功后，AAR Metadata 检查因 minCompileSdk=37 失败。
- [x] 已找到 Backdrop 1.0.6 作为更适合当前工程的候选：上游 Android 项目使用 AndroidX Compose 1.10.3 和 compileSdk 36。
- [x] Backdrop 1.0.6 的依赖解析、Kotlin 编译、单元测试和 Debug 构建已通过 PR #4 的 CI Run #39（2026-10-31）：https://github.com/PaleSquire71029/zhixingapp/actions/runs/41226739564。
- [ ] 将已验证的 Backdrop 1.0.6 与 Kotlin Compose 插件 2.4.21 基线正式合并到 main（PR #4）。
- [ ] 未修改 MainActivity/生产导航；未向 main 提交 APK。

### 实时 Backdrop 阶段进度（2026-11-01）

- [x] 修正 Android Build 工作流：日常 push/PR 仍执行测试和构建，但只有手动触发 `workflow_dispatch` 才会把新 APK 提交到 `artifacts/zhixingapp-preview.apk`。
- [x] 已确认 PR #4 合并后的既有 push 工作流 Run #40 曾自动更新仓库预览 APK（提交 `57f63a382207e3d34284f48e214829c0b9cbca6a`）；之后的工作流已更改为手动更新，避免常规源码提交重写 APK。
- [x] 已在 `feature/live-backdrop-host` 分支将 GlassLab 由每秒一次 Bitmap 快照改为 Backdrop 1.0.6 的共享实时背景层。
- [x] 已移除 GlassLab 的 `toImageBitmap()`、1 秒定时采样循环和旧位图绑定 Shader 路径；背景与玻璃面板改为同一 Compose 绘制链路。
- [x] 已把透明程度（0–100%）与模糊半径（0–24 dp）做成独立控制；Off 档使用普通浅色表面；API 26–30、31–32、33+ 有不同降级行为。
- [x] 已更新性能档位单元测试，验证 Premium / Standard / Lite / Off 与 API 降级规则。
- [ ] PR CI 构建、测试和运行结果待验证；未合并 Stage 2 实现。
- [ ] 当前尚未将液态玻璃底栏集成到 MainActivity；底栏属于后续阶段。


### 10.3 液态玻璃小步执行记录（2026-11-01）

- [x] 复核 PR #5：范围限定为 GlassLab、性能档位和单元测试，不修改 MainActivity 正式底栏。
- [x] 修复 Off 档资源浪费：Off 不创建 LayerBackdrop、不挂载 `layerBackdrop`，不运行动态光斑，仅保留静态浅色背景和普通面板。
- [x] 修正实时 Backdrop 迁移中的编译问题：foundation.border 导入、Kotlin 格式化字符串插值、Backdrop 类型收窄与非空值作用域。
- [x] 将 PR #5 分支整理到最新 main 基线上，避免分支落后造成 dirty merge state；保留四个目标文件及主分支期间新增的文档。
- [x] PR #5 CI 通过并合并；详见 10.4。
- [x] 已在 `LIQUID_GLASS_REDESIGN_PLAN.md` 新增 S2.1–S7 小步表，将玻璃组件、共享背景宿主、底栏外观、导航回调、Insets、动画和设置分开验收。
- [ ] 尚未修改 MainActivity 正式底部导航；主页面仍使用原 Material 3 NavigationBar。
- [ ] 目标真机的动态连续性、滚动/触控、帧率、温度、GPU/内存与耗电验收未执行。

### 10.4 实时 Backdrop 阶段 2 合并与 CI 验收（2026-11-01）

- [x] PR：[#5 实时 Backdrop 阶段 2](https://github.com/PaleSquire71029/zhixingapp/pull/5)，已合并至 main。
- [x] 合并提交：`a71acf50a07e6a0c4d992e0e2f6664120a05cdd2`。
- [x] Android Build #45：Run ID `41256336581`，结果 `success`；[查看完整运行](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41256336581)。
- [x] 图片资源校验、Gradle 配置、Kotlin 编译、`:app:testDebugUnitTest`、`:app:assembleDebug` 和 `zhixingapp-debug` Artifact 上传均成功。
- [x] 临时 Artifact ID：`13063297144`；APK 大小 11,248,442 bytes；SHA-256：`60a49cab67e1d5b87c534b84dcb1615c8bede9f0e76de53ae72d2d8c3dab64df`。
- [x] PR 事件下“提交安装预览 APK 到 main”步骤跳过；本次没有改写 `artifacts/zhixingapp-preview.apk`，也没有向 main 提交新的 APK。
- [x] `GlassLabScreen.kt` 已移除每秒一次 Bitmap 导出、1 秒采样循环、旧位图绑定 Shader 和相关快照状态；背景和玻璃面板使用同一 Backdrop 绘制链路。
- [x] 透明程度（0–100%）与背景模糊（0–24 dp）独立调节；Off 路径不创建 Backdrop 对象；API 26–30、31–32、33+ 按能力降级。
- [x] Premium / Standard / Lite / Off 性能档位单元测试更新并通过。
- [ ] 真机验收仍待执行；CI 成功不代表目标设备的动画流畅度、温度和耗电已经通过。

**下一小步：** S3.1，仅新增 `GlassMaterialState.kt` 纯 Kotlin 材质参数模型（透明度 0–100%、模糊 0–24 dp、默认值和范围保护）。本步不接 UI、不修改 MainActivity、不新增设置面板。


### 10.5 S3.1 知行玻璃材质参数模型（2026-11-01）

- [x] 新增 `app/src/main/java/ci/us/zhixing/app/ui/glass/GlassMaterialState.kt`，以不可变 data class 保存材质设置。
- [x] 透明度与模糊半径使用独立字段：`transparencyPercent: Int`、`blurRadiusDp: Float`。
- [x] 默认值：透明度 78%，模糊 12 dp；范围分别为 0–100% 与 0–24 dp。
- [x] 构造时校验透明度范围；模糊必须为有限数且位于 0–24 dp。通过 data class copy 更新时同样执行构造校验。
- [x] 模型已随 PR #6 合并至 main；原始开发提交：`8419d3514d6aef67345a67ad874ba8810d5f2eba`。
- [x] 不接入 UI、DataStore、Backdrop 或主导航，保持本步范围独立。

本小步只创建纯 Kotlin 参数模型，没有改变任何生产界面行为。

### 10.6 S3.2 材质参数模型单元测试与合并验收（2026-11-01）

- [x] 新增 `app/src/test/java/ci/us/zhixing/app/ui/glass/GlassMaterialStateTest.kt`。
- [x] 覆盖默认值、透明度 0/100 边界、模糊 0/24 dp 边界、两个参数独立设置、越界值拒绝。
- [x] 覆盖 `Float.NaN`、`Float.POSITIVE_INFINITY` 与 `Float.NEGATIVE_INFINITY` 均被拒绝。
- [x] PR [#6](https://github.com/PaleSquire71029/zhixingapp/pull/6) 已 squash 合并；合并提交：`5639a8ad4664eab0b7707c5caeb8145dd82fbe95`。
- [x] Android Build #48（Run ID `41256717498`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41256717498)。
- [x] 资源校验、Gradle 配置、JVM 单测、Debug APK 编译和 Artifact 上传步骤均成功。
- [x] PR 构建中的“提交安装预览 APK 到 main”步骤按工作流规则跳过；没有在本次源码合并中更新 `artifacts/zhixingapp-preview.apk`。
- [ ] 尚未将材质模型接入实际玻璃组件；尚未完成目标 Android 真机的显示、滚动、触控、帧率、温度和耗电验收。

**下一小步：** S4.2，只调整 `MainActivity.kt` 根层级，让现有首页内容位于共享背景之上；保留旧 NavigationBar。

### 10.7 S3.3 可复用玻璃表面组件与合并验收（2026-11-01）

- [x] 新增 `app/src/main/java/ci/us/zhixing/app/ui/glass/ZhixingGlassSurface.kt`。
- [x] 组件由调用方传入共享 Backdrop，不自行创建背景记录层。
- [x] 透明度独立控制白色覆盖层 Alpha；模糊半径独立控制 Backdrop blur。
- [x] 未提供 Backdrop 时使用圆角浅色面板回退。
- [x] PR [#7](https://github.com/PaleSquire71029/zhixingapp/pull/7) 已 squash 合并；合并提交：`e6f78a0101369fe65cc3cf930b48da9ace03cfb7`。
- [x] Android Build #49（Run ID `41260636118`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41260636118)。
- [x] 资源检查、Gradle 配置、JVM 单测、Kotlin 编译、Debug APK 构建及 Artifact 上传均通过。
- [x] 本步未修改 `MainActivity.kt`、导航或设置存储，没有向 main 发布新的 APK。
- [ ] 尚未完成真机渲染、触控、滚动、帧率、温度和耗电验收。

**下一小步：** S3.4，为组件增加显式 Off 开关和 API 26–30 / 31+ 降级策略；Off 不挂载 `drawBackdrop` 采样节点，并补充单测。

### 10.8 S3.4 API 降级与 Off 路径合并验收（2026-11-01）

- [x] 为 `ZhixingGlassSurface` 增加显式 `glassEnabled` 参数；关闭时使用浅色回退，不挂载 `drawBackdrop` 采样修饰符。
- [x] 新增纯 Kotlin `GlassSurfaceCapabilities.kt`，区分 API 26+ 的 Backdrop 背景采样和 API 31+ 的模糊能力。
- [x] 零模糊不启用 blur；非法范围和非有限值在能力解析层拒绝。
- [x] 新增 `GlassSurfaceCapabilitiesTest.kt`，覆盖 API 26–30、31–32、33+、Off、零模糊和非法参数等 7 组测试。
- [x] PR [#8](https://github.com/PaleSquire71029/zhixingapp/pull/8) 已 squash 合并；合并提交：`bd2c5bf3bcc0d143ac0230ab8e550ca27715ea11`。
- [x] Android Build #50（Run ID `41260858724`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41260858724)。
- [x] 资源校验、Gradle 配置、JVM 单测、Kotlin 编译、Debug APK 构建与 Artifact 上传均通过。
- [x] 本次未修改 `MainActivity.kt`、首页、导航与持久化设置，也未将 PR 临时 APK 发布到 main。
- [ ] 整个共享背景宿主 Off 时不创建 LayerBackdrop 的行为，留待 S4 主机集成阶段完成。
- [ ] 目标 Android 真机性能、外观和交互尚未验收。

**下一小步：** S4.1，只新增 `BackdropHost.kt` 静态浅色共享背景宿主；不修改首页内容和导航。

### 10.9 S4.1 静态共享背景宿主与合并验收（2026-11-01）

- [x] 新增 `app/src/main/java/ci/us/zhixing/app/ui/glass/BackdropHost.kt`。
- [x] 一个宿主统一创建一个共享 `LayerBackdrop`，通过内容 lambda 向前景传递 Backdrop。
- [x] 使用低饱和浅色渐变和三处静态柔光，画笔通过 `drawWithCache` 缓存。
- [x] 保持静态，不添加持续动画、Timer、Bitmap 快照或逐帧图像导出。
- [x] 首轮 PR #9 CI 因错误导入 `matchParentSize` 失败；修正为 `fillMaxSize` 后重新构建。
- [x] PR [#9](https://github.com/PaleSquire71029/zhixingapp/pull/9) 第二轮 Build #52（Run ID `41261146162`）成功；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41261146162)。
- [x] PR #9 已 squash 合并；合并提交：`496c098fdd1d41c4f979074a6ae5757b942b69b6`。
- [x] S4.2 已将现有首页 Scaffold 放入共享宿主前景，生产首页已能显示共享浅色背景。
- [x] S4.2 保留原 Logo、卡片、功能行、材质实验室入口和四个导航项；原白色 NavigationBar 与导航回调未改。
- [x] PR [#10](https://github.com/PaleSquire71029/zhixingapp/pull/10) 的 Build #53（Run ID `41261281542`）通过并合并；合并提交：`5436ccbf2add0e9e76ae0f803ec3113da5599028`。
- [ ] 真机层级、状态栏 / 手势 Insets、滚动、视觉和性能验收待执行。

**下一小步：** S4.3，增加 Android Compose 仪器测试，验证共享 Backdrop 的多个前景消费者和点击交互。

### 10.10 S4.2 首页接入共享背景与合并验收（2026-11-01）

- [x] 修改 `app/src/main/java/ci/us/zhixing/app/MainActivity.kt`，将原首页 Scaffold 放入 `BackdropHost` 前景内容层。
- [x] Scaffold 的 `containerColor` 改为透明，共享低饱和浅色背景由宿主绘制在后方。
- [x] 保留知行 Logo、首页 Hero 卡片、功能行、材质实验室入口和全部首页内容。
- [x] 保留原 Material 3 白色 NavigationBar、四个入口、选中状态与点击回调；未提前替换玻璃底栏。
- [x] PR [#10](https://github.com/PaleSquire71029/zhixingapp/pull/10) 已 squash 合并；合并提交：`5436ccbf2add0e9e76ae0f803ec3113da5599028`。
- [x] Android Build #53（Run ID `41261281542`）通过；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41261281542)。
- [ ] 尚未完成目标手机上的 Insets、安全区域、滚动层级、背景采样画面和设备性能实测。

### 10.11 S4.3 共享背景宿主交互验证（部分完成，2026-11-01）

- [x] 代码审查确认 `layerBackdrop` 仅包裹背景渐变子树；前景内容由同一宿主的兄弟层绘制，不会被重新录入同一采样层。
- [x] 确认背景层没有 `clickable`、`pointerInput` 或语义点击修饰符，不主动消费前景触摸。
- [x] 新增 Compose Android 仪器测试：两个 `ZhixingGlassSurface` 使用同一 Backdrop，测试覆盖两个内容节点可见及两处点击后计数更新。
- [x] PR #11 已合并，合并提交：`11fc135175210569bbadf0786e3acf8ece788ac2`。
- [x] Android Build（Run ID `41263348169`）结果为 `success`：资源检查、JVM 单测、`compileDebugAndroidTestKotlin` 仪器测试源码编译、Debug APK 构建和 Artifact 上传均通过。
- [x] 已从常规 CI 移除模拟器启动步骤，避免当前 Hosted Runner 在无 KVM 环境下持续卡住。
- [ ] 仪器测试的实际点击断言没有执行。API 35 x86_64 模拟器日志显示 Runner 无 `/dev/kvm` 权限，ADB 长期 `device offline`，8 分钟后超时。
- [ ] 因此此项仅完成架构审查与测试源码编译；两个表面的真实点击、采样像素、闪烁和真机性能仍待可用 Android 设备 / 带 KVM 的环境验证。

**后续处理：** 有可用模拟器或真机时运行 `:app:connectedDebugAndroidTest`；不可把 androidTest 源码编译成功等同于点击断言通过。

### 10.12 S5.1 悬浮胶囊玻璃底栏外壳与合并验收（2026-11-01）

- [x] 新增 `app/src/main/java/ci/us/zhixing/app/ui/glass/ZhixingLiquidBottomBar.kt`。
- [x] `ZhixingLiquidBottomBarSurface` 复用共享 Backdrop 与 `ZhixingGlassSurface`，形状为 50% 圆角胶囊。
- [x] 增加外层柔白描边与内层低对比冷色细线，保持静态外观。
- [x] 首轮 CI 的 Kotlin 编译报告 `drawRoundRect` 无效导入；已删除导入，保留 DrawScope 作用域内绘制调用。
- [x] PR [#12](https://github.com/PaleSquire71029/zhixingapp/pull/12) 已 squash 合并；合并提交：`f57991f10976e0ca62e9c41454ee63f15e4c865a`。
- [x] Android Build #57（Run ID `41261908712`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41261908712)。
- [x] 资源校验、Gradle、JUnit、Kotlin 编译、Debug APK 构建和 Artifact 上传全部通过。
- [x] 本步未修改 `MainActivity.kt`，未替换 Material 3 NavigationBar，也未加入导航项、选中动画或 Insets 逻辑。
- [ ] 真机上胶囊比例、玻璃采样、触控区域和性能待验收。
- [ ] S4.3 PR #11 的 Android 模拟器仪器测试仍待 CI 最终结果。

**下一小步：** S5.2，在独立底栏组件中添加四个导航项和点击回调，并验证选中状态；仍不修改 MainActivity。

### 10.13 S5.2 四个玻璃底栏入口与受控选中态（2026-11-01）

- [x] 新增 `app/src/main/java/ci/us/zhixing/app/ui/glass/ZhixingLiquidBottomBarItems.kt`。
- [x] 使用现有 `AppDestination` 列表，四项为计算、知行AI、工具、历史。
- [x] 接受 `selectedIndex` 和 `onDestinationSelected(index)`，选中状态由调用方统一控制。
- [x] 使用 selectable 的 `Role.Tab` 语义；当前选中项显示浅色背景，其他项透明。
- [x] PR [#13](https://github.com/PaleSquire71029/zhixingapp/pull/13) 已 squash 合并；合并提交：`c7077ad0c5331436ef9fc41e2a966d748c12de8f`。
- [x] Android Build #58（Run ID `41262141510`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41262141510)。
- [x] 资源校验、Gradle、JVM 单测、Kotlin 编译、Debug APK 构建与 Artifact 上传均通过。
- [x] 本步未修改 `MainActivity.kt`，尚未替换现有 Material 3 NavigationBar；无选中移动动画或手势。
- [ ] 真机文字适配、触摸面积、低高度布局和系统 Insets 待下一阶段验收。
- [ ] S4.3 PR #11 模拟器交互验证仍独立等待 CI 结果。

**下一小步：** S5.3，单独处理底栏悬浮位置与系统 Insets，不接入正式首页。

### 10.14 S5.3 悬浮位置与系统 Insets 合并验收（2026-11-01）

- [x] 新增 `app/src/main/java/ci/us/zhixing/app/ui/glass/ZhixingLiquidBottomBarPlacement.kt`。
- [x] 在全屏 Box 内底部居中放置内容，先应用 `navigationBarsPadding()`，避开系统导航栏 / 手势区域。
- [x] 默认再应用 `imePadding()`，键盘打开时把底栏上移；提供 `liftForIme` 参数控制。
- [x] 水平边距默认为 16 dp，底部留白 10 dp，使用独立 padding，不与系统 Insets 混为一项。
- [x] PR [#14](https://github.com/PaleSquire71029/zhixingapp/pull/14) 已 squash 合并；合并提交：`bdf3198cb16693d03af0eb4dab2c0a5e04429485`。
- [x] Android Build #59（Run ID `41262297446`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41262297446)。
- [x] 资源校验、Gradle 配置、JVM 单测、Kotlin 编译、Debug APK 构建和 Artifact 上传均通过。
- [x] 未修改 `MainActivity.kt`，尚未把底栏放入正式首页。
- [ ] 手势导航、三键导航、横屏、IME 弹出和物理设备安全区域仍待可视化验收。
- [ ] S4.3 PR #11 的模拟器测试仍未返回最终结果。

**下一小步：** S5.4，仅为选中胶囊添加连续位移动画；不替换首页导航。

### 10.15 S5.4 选中胶囊连续位移动画与合并验收（2026-11-01）

- [x] 修改 `ZhixingLiquidBottomBarItems.kt`，由一个共享选中胶囊替代逐项 selected background。
- [x] 根据等分项宽度计算目标位置，通过 `animateDpAsState` + `Spring.DampingRatioNoBouncy` / `Spring.StiffnessMediumLow` 连续移动。
- [x] 快速更改选中索引时，单一动画目标会重新定位；没有独立多指示器或并行动画。
- [x] 保留原四项点击回调和 `Role.Tab` 选中语义。
- [x] PR [#15](https://github.com/PaleSquire71029/zhixingapp/pull/15) 已 squash 合并；合并提交：`4e3eac017835ea1548badf3665c8b5db6c59f265`。
- [x] Android Build #60（Run ID `41262482557`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41262482557)。
- [x] 资源校验、Gradle、JVM 单测、Kotlin 编译、Debug APK 构建、Artifact 上传均通过。
- [ ] 快速连点过程的动画连续性和触控命中尚未完成仪器 / 真机验证。
- [ ] 仍未替换 MainActivity 的现有 Material 3 NavigationBar。

**下一小步：** S5.5，单独添加图标 / 标签的轻微过渡，不改变每个导航项的测量和触控尺寸。

### 10.16 S5.5 图标与标签轻微过渡合并验收（2026-11-01）

- [x] 修改 `ZhixingLiquidBottomBarItems.kt`，选中图标 1.0 倍、未选中 0.96 倍；图标 Alpha 0.88–1.0。
- [x] 标签 Alpha 0.78–1.0；使用 170 ms FastOutSlowIn tween。
- [x] 图标 / 标签的变化通过 `graphicsLayer` 完成，不改变布局测量、导航项宽度或 selectable 的点击区域。
- [x] 使用 destination route key 保持每个导航项动画状态稳定。
- [x] PR [#16](https://github.com/PaleSquire71029/zhixingapp/pull/16) 已 squash 合并；合并提交：`be056decb918611d4734083d442b4b1c1c7cc63f`。
- [x] Android Build #63（Run ID `41262700012`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41262700012)。
- [x] 资源校验、Gradle、JVM 单测、Kotlin 编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 快速连点时视觉与触控区域不变的实际设备验收仍待进行。

**下一小步：** S5.6，单独添加按压高光与轻微回弹，不添加拖动手势或改变点击区域。

### 10.17 S5.6 按压高光与轻微回弹合并验收（2026-11-01）

- [x] 修改 `ZhixingLiquidBottomBarItems.kt`，通过 `MutableInteractionSource` 追踪单项按压状态。
- [x] 按下时显示柔白按压高光，松开时淡出。
- [x] 内层图标 / 标签内容按压时缩至 0.975 倍，释放后用无过冲 spring 回到原始大小。
- [x] 外层 `selectable` 容器不变形，触控区域与布局尺寸不随动画改变。
- [x] PR [#17](https://github.com/PaleSquire71029/zhixingapp/pull/17) 已 squash 合并；合并提交：`75babe9b963df3225fc67270c6eb94c112c9d8d8`。
- [x] Android Build #66（Run ID `41262910437`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41262910437)。
- [x] 资源检查、Gradle、JVM 单测、Kotlin 编译、Debug APK 构建与 Artifact 上传全部通过。
- [ ] 真机快速按压、取消手势、滚动冲突与触控命中尚未实测。

**下一小步：** S6.1，正式替换 MainActivity 中的 Material 3 NavigationBar；保留四个目的地和原状态回调，使用已实现的共享 Backdrop / Insets 组件。

### 10.18 S6.1 首页正式接入液态玻璃底栏（2026-11-01）

- [x] 修改 `app/src/main/java/ci/us/zhixing/app/MainActivity.kt`：移除 Material 3 `NavigationBar` 与 `NavigationBarItem`。
- [x] 正式接入 `ZhixingLiquidBottomBar`，向其传入首页 `BackdropHost` 的共享 Backdrop。
- [x] 使用 `ZhixingLiquidBottomBarPlacement` 处理底部导航栏 / 手势区域 Insets 和可选 IME 上移。
- [x] 保留原 `AppDestinations.all`、`rememberSaveable selectedIndex`、点击回调及首页内容；GlassLab 仍独立显示。
- [x] PR [#18](https://github.com/PaleSquire71029/zhixingapp/pull/18) 已 squash 合并；合并提交：`95f6a422534f86cd6db64c0980f5ab824c9e897e`。
- [x] Android Build #67（Run ID `41263159541`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41263159541)。
- [x] 资源检查、Gradle、JVM 单测、Kotlin 编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机上手势 / 三键导航、IME 弹出、滚动内容底部留白、触控命中、动画帧率、温度和电池耗电仍待验收。
- [ ] 尚未增加材质设置入口、透明度 / 模糊调节和 DataStore 保存。

**下一小步：** S6.2，添加独立材质设置入口；保留四个底栏项的位置，不在本步增加滑块和持久化。

### 10.19 S6.2 首页独立材质设置入口（2026-11-01）

- [x] 首页右上角入口由静态“原生预览”标记改为可点击的“材质设置”。
- [x] 点击后复用现有 `GlassLabScreen`，没有创建重复材质页面。
- [x] 删除首页下方重复的“打开材质实验室”按钮。
- [x] 保留四个玻璃底栏目的地、选中状态和导航回调。
- [x] PR [#19](https://github.com/PaleSquire71029/zhixingapp/pull/19) 已 squash 合并；合并提交：`afadc577c4bd7e6b3a69719a58bfcb7c1d3e2161`。
- [x] Android Build（Run ID `41263447325`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41263447325)。
- [ ] 真机入口触控面积、TalkBack 朗读和小屏顶部布局仍待验收。

本步只添加材质设置入口，未增加新的滑块或持久化。

### 10.20 S6.3–S6.4 材质参数实时同步与透镜折射合并验收（2026-11-01）

- [x] 在 `ZhixingApp` 提升透明度、模糊和折射状态，并将统一 `GlassMaterialState` 传给生产 `ZhixingLiquidBottomBar`。
- [x] 打开 Glass Lab 时读取首页当前五项材质状态；透明度、模糊、折射开关和强度都可实时预览、独立回写。
- [x] Premium / Standard / Lite / Off 预设同步材质参数；Off 只关闭当前玻璃表面，不清空用户保存的光学数值。
- [x] 透镜折射通过 Backdrop `lens()` 效果接入共享玻璃表面，仅 Android API 33+ 启用；API 26–32 按能力降级。
- [x] `GlassMaterialState` 校验折射强度 0–8 dp，默认开启且默认强度 1.8 dp；新增范围、非有限值、Off 和 API 降级测试。
- [x] PR [#22](https://github.com/PaleSquire71029/zhixingapp/pull/22) 已合并；提交：`c473d29d4fb28b1a9c49d46fff79a6c78be3ba86`。
- [x] PR [#26](https://github.com/PaleSquire71029/zhixingapp/pull/26) 已合并；提交：`ff410b76d998cc13f6825707dfb5e98ca1cce0cf`。
- [x] Build #68（Run ID `41264097084`）和折射 Build（Run ID `41265216013`）均成功；折射构建包含 JVM 测试、androidTest 源码编译和 Debug APK 构建。
- [ ] 真机透镜边缘变形、强度平滑调节、API 32 以下降级视觉和滚动 / 帧率仍待验收。

### 10.21 S6.5 五项材质状态 DataStore 持久化与合并验收（2026-11-01）

- [x] 新增 `app/src/main/java/ci/us/zhixing/app/ui/glass/GlassMaterialSettingsStore.kt`，使用 Preferences DataStore 1.3.0。
- [x] 保存五个独立属性：透明度、模糊半径、玻璃启用状态、折射开关、折射强度。
- [x] 默认首页材质为 68% 透明度、12 dp 模糊、玻璃开启、折射开启、折射强度 1.8 dp。
- [x] 首页从 DataStore Flow 恢复设置；首次读取结束前禁用材质设置入口，避免默认值覆盖持久化配置。
- [x] Glass Lab 拖动滑块时即时预览，松手时才保存；折射开关和性能预设立即保存。
- [x] Off 仅持久化 `enabled = false`，不清空原透明度、模糊和折射值；重开后可恢复。
- [x] 增加“重置材质参数”，删除已存键并恢复五项默认值。
- [x] 新增 `GlassMaterialSettingsStoreTest.kt`，覆盖默认值、五项保存 / 读取、参数独立性和重置。
- [x] PR [#27](https://github.com/PaleSquire71029/zhixingapp/pull/27) 已 squash 合并；合并提交：`ff20f47119b19d2442515a608e4e612835486abc`。
- [x] Android Build #122（Run ID `41265710542`）结果为 `success`；[查看 CI](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41265710542)。
- [x] 资源检查、JUnit、`:app:compileDebugAndroidTestKotlin`、Kotlin 编译、Debug APK 构建和 Artifact 上传均通过。
- [ ] 尚未在目标真机强制结束进程并重启，真实恢复行为待实测；滑块快速拖动、Off 复开、低版本折射降级也待设备验收。
- [ ] Off 时单个玻璃表面不采样，但宿主级 `BackdropHost` 目前仍会创建共享 `LayerBackdrop`；后续可在整层宿主 Off 时跳过创建。

**S6 阶段状态：** S6.1–S6.5 代码与 CI 均已完成；真机功能 / 视觉 / 性能验收仍未完成。

### 10.22 S6.6 边缘高光接入正式玻璃材质（2026-11-01）

- [x] 将 `edgeHighlightEnabled` 加入统一 `GlassMaterialState`，默认开启。
- [x] 将边缘高光状态加入 `GlassMaterialSettingsStore`，DataStore 现保存六项材质属性。
- [x] 正式 `ZhixingGlassSurface` 根据共享开关绘制 `Highlight.Default`；关闭时返回空高光，不叠加额外白边。
- [x] Glass Lab 从首页当前状态初始化边缘高光开关；切换时实时预览并立即持久化。
- [x] 重置材质参数时，边缘高光恢复默认开启；性能预设继续决定各自高光状态。
- [x] 扩展材质状态默认值和 DataStore 往返保存测试，覆盖关闭高光后的持久化。
- [x] PR #28（S6.6）Android Build #126 / Run ID `41266372906`：资源校验、JUnit、仪器测试源码编译与 Debug APK 构建全部通过；已合并，提交 `4c800912f7347c62b5071ed8d39a7b0addc7d268`。
- [ ] 真机上检查上沿/侧缘高光是否自然、白边是否过强、不同背景亮度下的可见度与 GPU 帧率。

本步只把已有 Highlight 效果的开关正式接入生产材质；高光形状/强度曲线与环境反射仍待后续设计和真机校准。

### 10.23 S6.7 低成本环境反射接入正式材质（2026-11-01）

- [x] 在 `GlassMaterialState` 增加 `environmentReflectionEnabled`，默认开启。
- [x] 将环境反射开关加入 Preferences DataStore；材质设置由七项独立属性组成。
- [x] 新增 `GlassEnvironmentReflection.kt`，按玻璃表面尺寸绘制柔和冷蓝、紫色、暖色径向环境光与一条低强度反射光带。
- [x] 正式 `ZhixingGlassSurface` 在基础透明覆盖层之上绘制环境反射；关闭开关时跳过环境光绘制。
- [x] Glass Lab 增加环境反射开关，预览卡和首页共用同一绘制 helper；开关改变时即时预览并保存。
- [x] Reset 恢复默认环境反射；Lite 预设关闭环境反射，Premium / Standard 保持开启，Off 不清除用户光学参数。
- [x] 扩展材质默认值与 DataStore 七项往返保存测试，覆盖环境反射关闭后的持久化。
- [x] PR #29（S6.7）Android Build #128 / Run ID `41266876259`：资源检查、JUnit、仪器测试源码编译与 Debug APK 构建全部通过；已合并，提交 `0766a20e73267948e3a8cf7c6b92945bf110026c`。
- [ ] 真机检查冷暖反射强度、不同亮度背景上的观感、文字对比度与帧率。

本步是低功耗的环境色反射叠加，不是物理级实时环境贴图或 ray tracing；没有新增持续动画时钟、全屏快照或 Shader。动态随触摸移动的反射留待后续独立步骤。

### 10.24 S6.8 触摸跟随光效与导航胶囊轻微形变（2026-11-01）

- [x] 在共享材质状态增加 `touchFeedbackEnabled`，DataStore 扩展为八项独立属性，默认开启。
- [x] 底部导航项通过 `PressInteraction` 读取真实按压坐标，将每个 Tab 的局部坐标映射到整条玻璃底栏坐标。
- [x] 玻璃底栏在触点位置绘制径向柔光；按压期间淡入，松开或取消后平滑淡出。
- [x] 选中胶囊在按压时做轻微纵向压缩和横向补偿回弹；仅缩放装饰指示器与图标 / 标签内容，不改变 `selectable` 外层测量尺寸及点击区域。
- [x] Glass Lab 增加“触摸光效与形变”开关；预览卡支持按下与拖动跟随触点，释放后恢复。
- [x] Lite / 性能预设和 Off 的状态行为复用已有配置；触摸反馈设置可保存、恢复和重置。
- [x] 新增触点坐标映射边界单测，并扩展材质状态与 DataStore 八项持久化测试。
- [x] PR #30（S6.8）Android Build #130 / Run ID `41267449459`：资源校验、JUnit、仪器测试源码编译、Debug APK 构建和 Artifact 上传全部通过；已合并，提交 `bd033e1da00289a76c45b00d3e0bfa89276fd5c3`。
- [ ] 真机检查触摸轨迹跟随、快速连续切换、点击命中区域、动画帧率及长时间运行的发热 / 功耗。

本步使用 Compose InteractionSource 与轻量径向绘制，不引入全屏捕获、持续动画时钟或新 Shader；目前的形变是可控的视觉反馈，不是完整的物理液体模拟。


### 10.25 S6.9 Backdrop 原生色散折射接入（2026-11-01）

- [x] 核对当前依赖 `io.github.kyant0:backdrop:1.0.6` 的源码：`lens()` 已提供 `chromaticAberration` 参数，并在 Android 13（API 33）以下跳过 RuntimeShader；无需新增依赖或复制 Shader。
- [x] 在 `GlassMaterialState` 增加独立的 `chromaticDispersionEnabled`，默认关闭，并接入 DataStore 保存、读取、重置。
- [x] 正式共享玻璃表面将开关传给 Backdrop `lens()`，底部导航等复用 `ZhixingGlassSurface` 的组件同步生效。
- [x] Glass Lab 增加色散折射开关与系统版本提示；关闭折射或不支持 API 时不能开启。
- [x] Premium 在 API 33+ 默认启用色散；Standard 和 Lite 不启用，Off 不清除已保存的自定义材质参数。
- [x] 玻璃预览卡与首页调用相同的 Backdrop 色散参数。
- [x] 新增默认值、DataStore 九项属性往返保存、Premium 预设及 API 26–32 降级测试。
- [x] PR #31 已 squash 合并，提交 `b3cbda5f77273e1823556edcc25b8668bb87e905`。
- [x] Android Build #131 / Run ID `41268187782`：资源检查、JUnit、仪器测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机色散边缘、彩边自然程度、不同背景下的文字可读性和帧率待验收。

本步使用 Backdrop 1.0.6 自带的 `chromaticAberration` 着色器分支；没有额外依赖、自制 RGB 边缘描线或持续动画。色散为库内固定效果，当前版本不提供独立色散强度参数。


### 10.26 S6.10 底部导航按压驱动的动态透镜响应（2026-11-01）

- [x] 新增纯 Kotlin 参数解析 `resolveTouchRefractionParameters`，按触摸进度连续计算透镜高度和折射量。
- [x] 触摸进度从 0 到 1 时，底栏透镜高度由 22 dp 增至 26 dp，折射量平滑增加最多 16%；松开 / 取消后回到材质原值。
- [x] 将触摸状态提升到玻璃底栏外层，通过 130 ms tween 驱动 Backdrop `lens()` 参数；与原有触摸光效、胶囊形变共用按压事件。
- [x] 仅底部导航传入动态进度；其他 `ZhixingGlassSurface` 和 Glass Lab 预览默认进度为 0，不改变其基础效果。
- [x] 关闭触摸反馈、关闭玻璃或关闭折射时，动态透镜进度归零；不改变 `selectable` 外层尺寸和点击命中区域。
- [x] 新增参数纯函数测试，覆盖闲置、完整按压、部分按压及非法值。
- [x] PR #32 已 squash 合并，提交 `4f9279f7c476d33d63f45f3da4bf4676900f113f`。
- [x] Android Build（Run ID `41271365006`）：资源检查、JUnit、仪器测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机检查快速点击、长按、取消触摸、低帧率时动画一致性及实际折射观感。
 
本步不新增持续动画时钟、快照、Shader、图层或依赖；折射变化只跟随真实触摸事件，并在松开后停止。


### 10.27 S6.11 Backdrop 原生 Vibrancy 色彩活力（2026-11-01）

- [x] 核对 Backdrop 1.0.6 的 `vibrancy()` 实现：其通过 ColorMatrix saturation 1.5 提升被采样背景颜色活力；Android 12（API 31）以下由库内部自动跳过。
- [x] 将 Vibrancy 加入统一 `GlassSurfaceCapabilities`：仅 API 31+ 且模糊或折射至少一项有效时启用。
- [x] 正式 `ZhixingGlassSurface` 与 Glass Lab 预览使用相同的效果顺序：Vibrancy → Blur → Lens。
- [x] Lite（模糊 / 折射均关闭）和 Off 不执行 Vibrancy；API 26–30 自动降级，避免无效 RenderEffect。
- [x] 扩展能力策略测试：覆盖 API 26–30、31–32、33+、仅折射、无有效光学效果及 Off。
- [x] PR [#33](https://github.com/PaleSquire71029/zhixingapp/pull/33) 已 squash 合并；合并提交 `b4a31695893fd7f03438e26fbb8be2549ce25d86`。
- [x] Android Build（Run ID `41272191950`）结果为 `success`；资源检查、JUnit、仪器测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机检查颜色是否过饱和、玻璃下背景对比度、白色 / 彩色背景表现与帧率。

本步使用 Backdrop 库自身的 Vibrancy ColorMatrix，不叠加自制滤镜；Vibrancy 只作用于被玻璃采样的背景，玻璃表面内的前景文字仍由 Compose 正常绘制。实际观感仍需在设备上校准。

### 10.28 S6.12 底部导航选中项独立折射玻璃层（2026-11-01）

- [x] 将原本的纯色选中背景替换为独立的 Backdrop drawBackdrop 层；透镜随共享选中胶囊连续移动。
- [x] 选中层单独采样页面 Backdrop，并按当前材质能力应用 Vibrancy、Blur、Lens；API 低版本或折射关闭时保留轻量回退。
- [x] 选中层复用当前折射强度和色散开关，增加克制的前景色调与边缘高光。
- [x] 选中层沿用 S6.10 的按压进度，触摸时透镜高度和折射量动态增强；释放后回归静态状态。
- [x] 保持原有动画位置、四项导航内容、Role.Tab 语义和 selectable 触控区域；玻璃装饰层不接管点击。
- [x] PR [#34](https://github.com/PaleSquire71029/zhixingapp/pull/34) 已 squash 合并；合并提交 `3b6adc0b0c39c8ecc8be9a48e321006710a42e60`。
- [x] Android Build #138（Run ID `41273661779`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 设备上检查折射是否清晰但不过强、文字对比度、帧率和耗电仍待验收。

本步只让选中胶囊拥有独立实时折射层；尚未实现手指拖动选项、液体形变传播或完整 iOS 26 系统级材质行为。

### 10.29 S6.13 选中玻璃胶囊内外缘层次（2026-11-01）

- [x] 在选中胶囊独立 Backdrop 层加入轻量外投影，提升其与底栏背景的空间分离。
- [x] 加入低强度内阴影，形成玻璃边缘厚度；不使用额外自定义绘制或自制 Shader。
- [x] 高光、外投影与内阴影沿用同一触摸进度，按压时略微增强，松开后恢复；没有常驻动画时钟。
- [x] 触摸反馈或折射关闭时压力进度归零；不改变导航位置、点击区域和前景文字。
- [x] PR [#35](https://github.com/PaleSquire71029/zhixingapp/pull/35) 已 squash 合并；合并提交 `c7ea4eaa586f7b377680b62ed6c8f8a4cdd92e7d`。
- [x] Android Build #140（Run ID `41273865974`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机需检查边缘是否过黑、投影是否糊边、不同屏幕亮度下层次感与帧率。

本步只调整选中胶囊的内外缘光学层次，不引入拖动选项或新的材质控制项。

### 10.30 S6.14 选中胶囊切换时的液态拉伸（2026-11-01）

- [x] 在选中索引变化时触发一次短暂的形变脉冲：移动初段横向舒展，随后回弹收拢。
- [x] 形变与现有位置 Spring 动画并行；只作用于装饰胶囊的 GraphicsLayer，不改变导航项测量尺寸、语义或点击区域。
- [x] 快速切换时取消并重启当前脉冲，避免旧动画延迟覆盖最新目标。
- [x] 不使用常驻动画时钟；只有选中索引改变时运行一次 280 ms 的短动画。
- [x] PR [#36](https://github.com/PaleSquire71029/zhixingapp/pull/36) 已 squash 合并；合并提交 `6d61bdd975e76937ceefbc6f65676e7350db10a4`。
- [x] Android Build #142（Run ID `41274024797`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 设备上需确认拉伸幅度自然、连续切换不抖动、低帧率下动画不拖沓。

本步先完成选中胶囊自身的轻量拉伸 / 回弹，不模拟完整流体质量守恒或拖动跟手；这两项仍留待后续单独实现。

### 10.31 S6.15 选中胶囊横向拖动跟手（2026-11-01）

- [x] 底栏容器仅在按下位置落在当前选中胶囊范围内时，观察横向拖动手势；未超过横向 touch slop 时不接管普通点击。
- [x] 拖动超过阈值后，以指针横向位移直接更新胶囊像素位置，使独立折射层实时跟随手指。
- [x] 松手时根据胶囊当前位置计算最近导航项，再调用原有 `onDestinationSelected`；不在拖动途中连续切换页面。
- [x] 松手后由现有 Spring 动画从手指位置收拢到目标项中心；触摸光效和动态折射复用拖动状态。
- [x] 增加 `resolveBottomBarDragIndex` 纯函数和单测，覆盖中心阈值、越界钳制、单项导航及非法参数。
- [x] PR [#37](https://github.com/PaleSquire71029/zhixingapp/pull/37) 已 squash 合并；合并提交 `59ebc757050589d6dbee42bd6ab0ee2dbeee0b62`。
- [x] Android Build #146（Run ID `41274500641`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机验证手指跟随、横向拖动与上下滑动冲突、普通点击不受干扰及快速拖动体验仍待进行。

本步只支持从当前选中胶囊开始的横向拖动；尚未加入胶囊吸附弹性、液体尾迹或跨项形变传播。

### 10.32 S6.16 拖动方向驱动的胶囊弹性形变（2026-11-01）

- [x] 新增 `resolveDirectionalDragDeformation`，按每帧横向位移方向和归一化幅度计算胶囊拉伸量。
- [x] 过滤小于 1.5 px 的指针抖动，拉伸量限制在 0.035–0.12，避免高速滑动时过度变形。
- [x] 用 TransformOrigin 把胶囊固定在拖动反方向的一侧，使视觉拉伸朝移动方向延伸；同时轻微压扁纵向高度。
- [x] 使用 72 ms 轻量 tween 平滑更新拉伸量；手指停住或释放时恢复，不增加常驻动画。
- [x] 扩展纯函数单测，覆盖方向、幅度、抖动过滤、上限和非法参数。
- [x] PR [#38](https://github.com/PaleSquire71029/zhixingapp/pull/38) 已 squash 合并；合并提交 `1270eb69c4ecaa0ea744f207659cd71ae46087cc`。
- [x] Android Build #148（Run ID `41274798346`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机确认两种拖动方向的支点是否正确、快速反向是否抖动、拉伸是否自然。

本步只增加方向性的局部形变；尚未实现沿胶囊边缘传播的真实液体尾迹或完整流体求解。

### 10.33 S6.17 拖动胶囊渐隐尾迹（2026-11-01）

- [x] 在拖动胶囊后方添加短渐变光带，方向与拖动方向相反，靠近胶囊连接处逐渐增强。
- [x] 尾迹长度随当前弹性拉伸幅度略微变化，左右方向切换时同步反转渐变。
- [x] 使用 Compose 横向渐变与圆角裁剪，不增加 Backdrop 采样层、位图快照或自定义 Shader。
- [x] 尾迹仅在有效横向拖动且触摸反馈开启时出现；指针停住时不持续刷新，松手后 120 ms 淡出。
- [x] PR [#39](https://github.com/PaleSquire71029/zhixingapp/pull/39) 已 squash 合并；合并提交 `dd5d113c324ab9de89e666b34051f7d4258ac70d`。
- [x] Android Build #150（Run ID `41275034729`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 设备上检查尾迹与胶囊连接、浅色背景可见性、拖动反向时的连续性及帧率。

本步是低成本视觉尾迹，不是流体模拟或真正的折射拖影；后续仍可进一步做局部形变传播与采样层融合。

### 10.34 S6.18 尾迹方向反转平滑过渡（2026-11-01）

- [x] 将尾迹渐变方向从瞬时左右切换改为 100 ms 轻量过渡，减少快速反向拖动时的突兀翻转。
- [x] 尾迹位置在胶囊两侧之间同步平滑移动，避免渐变翻面时光带突然跳位。
- [x] 方向过渡只在方向状态变化时产生；不增加持续动画时钟、额外背景采样或 Shader。
- [x] PR [#40](https://github.com/PaleSquire71029/zhixingapp/pull/40) 已 squash 合并；合并提交 `98d5900bc5ba815093b9324c9eb3348f34baef81`。
- [x] Android Build #152（Run ID `41275231798`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机检查连续左右反转、快速拖动跟手性、光带是否穿越文字区域以及帧率。

本步只平滑尾迹方向和位置，不改变胶囊拖动坐标、最终吸附位置和导航回调。

### 10.35 S6.19 尾迹颜色与强度自适应（2026-11-01）

- [x] 尾迹颜色改为读取 `ZhixingTheme.colors.primary`，避免固定浅蓝色与应用主题不一致。
- [x] 尾迹透明度按现有方向拉伸量映射：轻拖时更淡，快速拖动时增强；仍由 120 ms 动画平滑跟随。
- [x] 不新增速度采样器、动画时钟、Backdrop 层、Shader 或依赖；继续复用现有拖动形变状态。
- [x] PR [#41](https://github.com/PaleSquire71029/zhixingapp/pull/41) 已 squash 合并；合并提交 `a25db4db128c8a225c52d83ff6f900f0126d267c`。
- [x] Android Build #154（Run ID `41275412031`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机确认主题色对比度、浅色背景可见性、强度变化是否突兀及帧率。

本步只改变尾迹的视觉颜色和强度，不改动拖动位置、方向过渡和吸附行为。

### 10.36 S6.20 底栏玻璃边缘的主题反射层（2026-11-01）

- [x] 将底栏外缘和内缘从固定纯白 / 浅蓝描边改为低强度渐变反射，颜色跟随 `ZhixingTheme.colors.primary`。
- [x] 边缘亮度按当前透明度设置轻微调整；用户改变透明度时，玻璃轮廓层次同步变化。
- [x] 继续使用原有 Backdrop 玻璃表面和两道细描边，不新增采样层、模糊半径、Shader 或第三方依赖。
- [x] 保留原有底栏尺寸、圆角、内容层级和触控区域。
- [x] PR [#42](https://github.com/PaleSquire71029/zhixingapp/pull/42) 已 squash 合并；合并提交 `1cb11dc549e19acbbaae7d5b7ae7fddf00cbead3`。
- [x] Android Build #156（Run ID `41275785234`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机确认高亮不过曝、主题色不过饱和、不同透明度下轮廓清晰度和帧率。

本步只优化玻璃边缘的环境色反射，不代表已实现真实物理光线追踪或完整 iOS 26 系统材质。

### 10.37 S6.21 选中胶囊环境色反射（2026-11-01）

- [x] 在选中胶囊独立 Backdrop 折射层中复用现有 `drawEnvironmentReflection()` 环境反射绘制。
- [x] 环境反射沿用现有冷色 / 紫色 / 暖色渐变与柔和高光，受 `environmentReflectionEnabled` 设置控制。
- [x] 继续使用共享 Backdrop 和原有表面绘制，不创建额外采样层、离屏位图或 Shader。
- [x] 不改变选中胶囊尺寸、定位、导航项语义和点击范围。
- [x] PR [#43](https://github.com/PaleSquire71029/zhixingapp/pull/43) 已 squash 合并；合并提交 `4fa0b4bed935e84eeea021035a2cdc979fb8a762`。
- [x] Android Build #158（Run ID `41275981455`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 设备上检查反射是否清晰、冷暖过渡是否自然、浅色背景下是否过亮及帧率。

本步复用已有环境反射绘制，不是实时物理环境映射或光线追踪；高光与底纹在真机上的融合程度仍需实际验收。

### 10.38 S6.22 选中胶囊透明度与材质设置同步（2026-11-01）

- [x] 新增 `resolveGlassOverlayAlpha`，统一把 0–100% 透明度转换为表面填充 Alpha。
- [x] 通用玻璃表面、选中胶囊实时折射层和无折射降级外观复用同一透明度映射。
- [x] 修复选中胶囊原先固定 0.20 / 0.82 Alpha 的问题；材质透明度变化现在会同步反映到胶囊表面。
- [x] 补充单元测试，覆盖 0%、默认 78%、100% 以及越界输入；模糊半径仍保持独立。
- [x] PR [#44](https://github.com/PaleSquire71029/zhixingapp/pull/44) 已 squash 合并；合并提交 `c4d1659bd1a59ecaff8ba579585fc7e5cce11604`。
- [x] Android Build #161（Run ID `41276361678`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机检查透明度滑块从低到高时的视觉连续性、文字对比度和纯透明模式的选中辨识度。

本步统一的是表面填充透明度，不改变 Backdrop 的实时折射强度或模糊半径。

### 10.39 S6.23 统一透明度自适应边缘高光（2026-11-01）

- [x] 新增 `resolveGlassHighlightAlpha`，按透明度基值和按压进度计算统一边缘高光强度。
- [x] 通用玻璃表面与选中折射胶囊复用同一计算，不再分别使用固定 0.42 和 0.52 起始高光。
- [x] 透明度越高，边缘轮廓高光适当增强；按压时再进行有限幅度的增强，输入限制在有效范围。
- [x] 补充测试，覆盖 0%、78%、100% 透明度、半按压、全按压及非法输入。
- [x] PR [#45](https://github.com/PaleSquire71029/zhixingapp/pull/45) 已 squash 合并；合并提交 `90dba4a3c30524f241014fe21c56629e11ef9230`。
- [x] Android Build #163（Run ID `41276664062`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机检查边缘不过曝、不同亮度下轮廓清晰度、按压反馈和帧率。

本步统一的是边缘高光强度控制，不模拟实时光源追踪；视觉结果仍需不同屏幕条件下实际验收。

### 10.40 S6.24 选中胶囊联合采样底栏文字与图标（2026-11-01）

- [x] 按 Backdrop 1.0.6 官方 `LiquidBottomTabs` 示例的分层思路，新增独立 `LayerBackdrop` 记录底栏图标和文字。
- [x] 使用 `rememberCombinedBackdrop(pageBackdrop, tabContentBackdrop)` 合并页面背景与底栏内容，让选中折射胶囊能够采样底栏前景图形。
- [x] 采样副本仅绘制图标 / 标签，不添加点击处理、导航回调或无障碍节点；实际可见 Row 与原有 selectable 保持不变。
- [x] 复用项目现有共享 Backdrop 依赖 `io.github.kyant0:backdrop:1.0.6`，没有复制整个演示应用，也不新增依赖。
- [x] PR [#46](https://github.com/PaleSquire71029/zhixingapp/pull/46) 已 squash 合并；合并提交 `b8f086a99727d01589d5263294ef4cecfe53569c`。
- [x] Android Build #165（Run ID `41277136986`）结果为 `success`；资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 真机重点检查选中胶囊经过文字和图标时是否产生自然折射、采样位置是否对齐、快速拖动时是否闪烁及帧率。

本步引入官方示例中的 CombinedBackdrop 采样结构，是对现有实现的关键补强；仍需进一步完善拖动弹性、光照响应和系统环境适配，尚不能宣称完整复刻 iOS 26。

### 10.41 S6.25 基于实际指针速度的胶囊弹性（2026-11-01）

- [x] 在横向拖动识别期间用 Compose `VelocityTracker` 记录真实指针速度，不再仅依赖相邻事件的位移量。
- [x] 新增 `resolveVelocityDragStretch`，以每秒跨越的标签宽度数归一化速度，降低设备密度与采样频率差异对形变的影响。
- [x] 将速度响应与原方向性位移形变合并，取较强值作为当前拉伸目标，并限制在 0–0.12 的范围。
- [x] 保留原拖动方向、目标索引计算、触控 slop 和导航回调逻辑不变。
- [x] 增加单元测试，覆盖零速、线性响应、不同像素密度的一致性、快速拖动上限与非法速度输入。
- [ ] PR / CI 待执行；真机检查快速甩动、慢速拖动、突然反向、不同刷新率下拉伸表现与帧率。

本步先把真实速度引入局部弹性形变；尚未引入完整阻尼跟手、松手惯性或 iOS 式流体质量传播。


### 10.42 参考 APK 对照后改为直接移植官方 Catalog（2026-11-01）

- [x] 检查用户上传的参考 APK：包名为 `com.kyant.backdrop.catalog`，版本为 `1.0.0`。
- [x] 对照 Kyant0/AndroidLiquidGlass tag `1.0.6`（commit `896a94a3ade1cc1a940b92365f942a34971fecda`）：Catalog Gradle 配置的 applicationId / versionName / compileSdk 与参考 APK 相符。
- [x] 确认官方 Catalog 底栏完整使用 `LiquidBottomTabs`、`LiquidBottomTab`、`DampedDragAnimation`、`InteractiveHighlight`、`DragGestureInspector`，以及 LayerBackdrop / CombinedBackdrop 双层采样；不是只使用单个 lens。
- [x] 确认知行 App 当前已依赖 `io.github.kyant0:backdrop:1.0.6`；本轮选择直接移植同版本 Catalog 组件，不升级到 2.x，不复制整个 Demo App。
- [x] 确认上游 Apache-2.0 许可证；移植时需保留原版权声明、LICENSE，并标注修改来源。
- [x] PR [#47](https://github.com/PaleSquire71029/zhixingapp/pull/47) 已合并（提交 `8325ddfbe9b2c217f05ea4fab670a1eb3b024f5f`，Android Build #167 成功）；该自研速度形变仅为历史原型，后续由官方阻尼动画统一接管。
- [x] PR [#48](https://github.com/PaleSquire71029/zhixingapp/pull/48) 在参考 APK 对照后关闭，未合并；不把其中单独 Spring-follow 实现作为后续基线。
- [x] 更新专项计划第 25 节，锁定官方 tag 1.0.6，并明确 P1–P6 直接移植步骤。
- [ ] P1：搬入官方辅助类（DampedDragAnimation、InteractiveHighlight、DragGestureInspector），保留 Apache 版权头；只新增文件，不改生产 UI。
- [ ] 暂停继续增加自研 VelocityTracker、拖动尾迹、独立 Spring、装饰性反射等与官方组件重复的实现。
- [ ] 仍需在目标 Android 真机对比参考 APK：背景、边缘折射、色散、触摸高光、拖动响应、帧率、温升与耗电。

**当前下一步：** P1，直接移植官方三个辅助类与手势识别工具；CI 通过后才进入 P2 底栏组件替换。


### 10.43 扩大液态玻璃覆盖范围与动画复刻计划（2026-11-01）

- [x] 按用户新要求，将目标从“只有底栏玻璃”扩大为“首页及后续主要页面的大部分交互元素和主要容器采用液态玻璃或立体化浅色材质”。
- [x] 在专项计划第 25.6 节增加全局 Z0–Z4 空间层级、全页面元素覆盖矩阵与统一动画复刻清单。
- [x] 明确优先直接复用 Catalog 1.0.6 的源码；文字、数学公式及高密度内容必须清晰可读。
- [x] 本节初版执行入口已由 10.44–10.48 的实际实施记录取代；当前执行入口统一以文档第 25.8 节及本账本最新编号为准。

**当前下一步：** P2.3.2 官方底栏拖动交互测试；生产 MainActivity 仍不切换。


### 10.44 P1 官方 Catalog 1.0.6 辅助类移植完成（2026-11-01）

- [x] 固定上游仓库 `Kyant0/AndroidLiquidGlass`、tag `1.0.6`、commit `896a94a3ade1cc1a940b92365f942a34971fecda`。
- [x] 移植 `DampedDragAnimation.kt`、`InteractiveHighlight.kt`、`DragGestureInspector.kt` 至 `ui/glass/backdrop106`。
- [x] 逐文件去除知行包名适配后，与上游源码逐字一致。
- [x] 添加完整 Apache-2.0 LICENSE 与 `THIRD_PARTY_NOTICES.md` 来源说明。
- [x] PR [#49](https://github.com/PaleSquire71029/zhixingapp/pull/49) 已 squash 合并；合并提交 `52c97bb1c4be64e5709ade87477b428d881fda36`。
- [x] Android Build #180（Run ID `41280383296`）成功：资源校验、JUnit、Android 测试源码编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 该步骤没有连接生产 UI；真机触摸和 Shader 呈现需要后续 P2 / P9 验收。

### 10.45 P2.1 官方 LiquidBottomTabs 源码隔离移植（已完成，2026-11-01）

- [x] 从固定上游 tag 1.0.6 获取 `LiquidBottomTabs.kt` 与 `LiquidBottomTab.kt`。
- [x] 移植到 `ui/glass/backdrop106/components`，将 helper 引用重定向到已合并的隔离包；保留官方布局、手势、透镜参数与色散配置。
- [x] 按上游 Catalog 的依赖声明添加固定版本 `io.github.kyant0:shapes:1.2.0`；没有升级 Backdrop / Compose / AGP / Kotlin。
- [x] 更新第三方来源说明，扩展已移植文件清单。
- [x] 两个组件源码经包名反向还原后与上游固定 tag 逐字一致。
- [x] PR [#50](https://github.com/PaleSquire71029/zhixingapp/pull/50) 已 squash 合并；合并提交 `c07a0e34977f05c1c5feca465f04d8b605a48d4d`。
- [x] Android Build #183（Run ID `41280683284`）全部成功：资源校验、JUnit、androidTest 源码编译、Debug APK 构建和 Artifact 上传。
- [ ] 未修改 MainActivity；新组件仍未接入生产底栏，真机交互待后续验收。

### 10.46 P2.2 知行四入口适配层（已完成，2026-11-01）

- [x] 新增 `ZhixingCatalogBottomBar.kt`，仅包装官方 `LiquidBottomTabs` / `LiquidBottomTab`。
- [x] 映射现有 `AppDestinations.all`：四个入口、route 稳定 key、原 label 与 symbol 均沿用，不复制或改写导航数据。
- [x] selectedIndex 和 `onDestinationSelected` 仍由调用方控制，包装层不创建第二份导航状态。
- [x] 为每个 Tab 保留 `Role.Tab`（来自官方组件），并添加当前选中语义。
- [x] 使用现有 `ZhixingTheme` 的字号、颜色和文字 Tokens；不引入新字体、不增加新动画。
- [x] 没有修改 MainActivity、旧底栏、BackdropHost、系统 Insets、DataStore 或业务页面。
- [x] PR [#51](https://github.com/PaleSquire71029/zhixingapp/pull/51) 已 squash 合并；合并提交：`ce7539907a666db829de3c32b5c88228e39bd5bb`。
- [x] Android Build #185（Run ID `41280875247`）成功：资源校验、JUnit、androidTest Kotlin 编译、Debug APK 构建和 Artifact 上传全部通过。
- [ ] 生产 UI 未切换；官方组件真机折射、触摸、拖动和帧率验收仍在后续步骤。


### 10.47 P2.3.1 官方底栏语义与调用方状态测试（已合并，2026-11-01）

- [x] 新增 `ZhixingCatalogBottomBarSemanticsTest.kt`，使用现有 `createAndroidComposeRule<ComponentActivity>()` 和真实 `BackdropHost` 挂载适配层。
- [x] 覆盖四个 Tab 均暴露 `Role.Tab`、原中文标签可见、选中态只跟随传入的 selectedIndex。
- [x] 覆盖点击“工具”后回调更新外部选中索引，以及外部直接切换到“历史”时语义同步变化。
- [x] 测试不改生产组件、不改导航模型、不改 MainActivity，也不引入额外依赖。
- [x] 首次 Android Build #188 暴露 Compose UI Test API 使用错误；改用 `SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)` 后修复。
- [x] PR [#52](https://github.com/PaleSquire71029/zhixingapp/pull/52) 已 squash 合并；merge commit：`4578f54fafd0cfc20f5ba0cdc2aabfdce32ae376`。
- [x] Android Build #192（Run ID `41281478165`）全部成功：资源校验、JUnit、androidTest Kotlin 编译、Debug APK 构建和 Artifact 上传。
- [ ] CI 工作流仅编译 `androidTest`，不会在 Hosted 模拟器执行仪器测试；设备交互、视觉和性能仍待实机验证。


### 10.48 P2.3.2 官方底栏拖动与边界测试（已合并，2026-11-01）

- [x] 建立隔离分支 `port/catalog-1.0.6-p2-3-2-drag-tests`，未改 MainActivity 或生产底栏。
- [x] 新增 `ZhixingCatalogBottomBarDragTest.kt`，通过 `performTouchInput` 在真实共享 Backdrop 容器上注入水平拖动。
- [x] 覆盖从“计算”向右跨两项到“工具”、从“历史”向左跨两项到“知行AI”。
- [x] 覆盖拖动中快速反向后根据最终位置吸附，以及拖至最左边界时索引保持为 0。
- [x] 首次 Android Build #194 暴露触摸注入作用域不存在 `size` 属性；已改用 `visibleSize.width` 计算，并由后续构建验证。
- [x] PR [#53](https://github.com/PaleSquire71029/zhixingapp/pull/53) 已 squash 合并；merge commit：`54da045b95bcc4e460597d3a92a3e0f748f23b20`。
- [x] Android Build #196（Run ID `41282146320`）全部成功：资源校验、JUnit、androidTest Kotlin 编译、Debug APK 构建和 Artifact 上传。
- [ ] CI 未在 Hosted 模拟器执行手势；拖动跟手性、吸附动画与真实帧率仍需目标设备验收。

### 10.49 P2.4 材质设置与首页接入拆分约束（计划更新，2026-11-01）

- [x] 将 P2.4 拆为 2.4.1a / 2.4.1b 参数映射与绘制、2.4.2 MainActivity 接入、2.4.3 集成验收。
- [x] 明确官方 1.0.6 底栏的 8dp blur、24dp lens 与固定表面 alpha 不能覆盖知行原有 DataStore 设置；P2.4.1a / 1b 已逐项将透明度、模糊、折射、色散、高光、环境反射、触摸反馈映射到 `GlassMaterialState`。
- [x] 明确首页接入必须复用唯一共享 Backdrop 和 `ZhixingLiquidBottomBarPlacement`，保持现有系统 Insets、IME 抬升和四项目的地。
- [x] P2.4.2 已在 PR #57 完成生产调用切换；旧底栏仍保留，待 P2.5 单独清理。


### 10.50 P2.3.3 RTL、窄屏与无障碍回归测试（已合并，2026-11-01）

- [x] 新增 `ZhixingCatalogBottomBarLayoutAccessibilityTest.kt`，在真实共享 Backdrop Host 中覆盖三个布局 / 无障碍场景。
- [x] RTL：通过标签语义节点的 root bounds，检查首个目的地“计算”显示在最后一个目的地“历史”的右侧，并验证四项标签可见。
- [x] 窄屏：将底栏约束为 240dp，验证四项标签均可见、语义节点有非空边界。
- [x] 无障碍：在 unmerged semantics tree 中确认恰好暴露四个 Role.Tab，避免隐藏采样层重复出现可访问节点。
- [x] 未修改生产适配层、导航数据、MainActivity、依赖或旧底栏。
- [x] PR [#54](https://github.com/PaleSquire71029/zhixingapp/pull/54) 已 squash 合并；merge commit：`7b24af78590abc6de546f34e66f75a2d0bfe57fc`。
- [x] Android Build #198（Run ID `41282327355`）全部成功：资源校验、JUnit、androidTest Kotlin 编译、Debug APK 构建和 Artifact 上传。
- [ ] CI 未在 Hosted 模拟器执行布局断言；真机 RTL、屏幕裁剪与 TalkBack 顺序仍需设备验收。


### 10.51 P2.4.1a 材质参数解析器与 JUnit 测试（已合并，2026-11-01）

- [x] 新增 `CatalogBottomBarMaterialParameters.kt`：将现有 `GlassMaterialState` 映射为官方 Catalog 底栏所需的填充 Alpha、模糊开关 / 半径、折射可用性 / 强度比例，以及色散、高光、环境反射和触摸反馈开关。
- [x] 透明度沿用 `resolveGlassOverlayAlpha`，不与模糊互相推导；保留 DataStore 原字段和值域。
- [x] 以知行默认折射强度 1.8dp 对应 Catalog 原透镜 1x；0–8dp 映射到 0–1.5x，避免最大值产生过度拉伸。
- [x] 复用 `resolveGlassSurfaceCapabilities`：API 26+ 保留背景采样，API 31+ 才使用模糊 / vibrancy，API 33+ 才使用折射；Off 时关闭全部光学 / 触摸反馈。
- [x] 新增六项单元测试，覆盖 Home 默认映射、透明度 / 模糊独立、折射范围、API 降级、开关独立和 Off 状态。
- [x] PR [#55](https://github.com/PaleSquire71029/zhixingapp/pull/55) 已 squash 合并；merge commit：`677b0b0ea9fbcefac5d2e35d2573e9f93b12fac6`。
- [x] Android Build #200（Run ID `41282549664`）全部成功：JUnit、androidTest Kotlin 编译、Debug APK 构建和 Artifact 上传。

### 10.52 P2.4.1b 官方底栏材质参数接线（已合并，2026-11-01）

- [x] `ZhixingCatalogBottomBar` 新增可传入 `GlassMaterialState` 的参数，默认值保留现有调用兼容。
- [x] `LiquidBottomTabs` 新增 material 参数，调用纯解析器获取透明度 Alpha、blur、vibrancy、refraction scale、chromatic dispersion、edge highlight、environment reflection 和 touch feedback 开关。
- [x] 背景材质按设置使用模糊与 24dp 基准折射；折射强度通过 0–1.5x 比例控制。
- [x] 透明度不仅影响底栏基底填充，也映射到选中胶囊的表面遮罩；浅色主题不再跟随 Android 系统暗色模式。
- [x] 选中透镜将知行色散设置应用到 chromaticAberration；高光、环境反射和触摸动效均按各自开关生效。
- [x] 全局 Off 使用浅色胶囊回退而非 drawBackdrop；折射关闭时不创建隐藏标签采样层 / CombinedBackdrop。
- [x] 触摸反馈关闭时，P1 `DampedDragAnimation` 跳过 press / release 弹簧并不创建 `InteractiveHighlight` / RuntimeShader；拖动位移、速度采样与吸附仍保留。
- [x] 每次拖动开始都重置 VelocityTracker，避免关闭触摸反馈后沿用上一段拖动的速度。
- [x] 新增仪器测试：Off 状态四个 Tab 可见且点击可切换；整体 Off 和仅关闭触摸反馈时，拖动仍可切换到“工具”。
- [x] PR [#56](https://github.com/PaleSquire71029/zhixingapp/pull/56) 已 squash 合并；merge commit：`25057905dbbe76881f9f7d77e7e62b156ccbd14b`。
- [x] Android Build #206（Run ID `41283390026`）全部成功：资源校验、JUnit、androidTest Kotlin 编译、Debug APK 构建和 Artifact 上传。
- [ ] Android CI 仅编译仪器测试源码；真实拖动跟手、折射视觉、高光和帧率仍需目标设备验收。



### 10.53 P2.4.2 MainActivity 集成官方底栏（已合并，2026-11-01）

- [x] `MainActivity.kt` 的生产底栏调用已从 `ZhixingLiquidBottomBar` 切换为 `ZhixingCatalogBottomBar`。
- [x] 继续传入原有 `glassMaterial`，实时预览和 DataStore 持久化设置无需新增状态或重新保存。
- [x] 继续复用同一 `BackdropHost` 提供的 Backdrop、`ZhixingLiquidBottomBarPlacement`、68dp 容器高度与导航栏 / IME Insets 处理。
- [x] `selectedIndex` 和 `onDestinationSelected` 仍由 MainActivity 所有；四个原路由、中文标签、glyph、标题和描述数据未改变。
- [x] 新增 `ZhixingCatalogBottomBarMainActivityIntegrationTest.kt`：检查四个 Role.Tab、初始计算页选中、点击知行AI和工具后页面标题同步更新。
- [x] 首次 Android Build #208 发现 Activity 集成测试错误导入 `onAllNodes` / `onNode` 顶层符号；已移除 provider member 的错误导入。
- [x] PR [#57](https://github.com/PaleSquire71029/zhixingapp/pull/57) 已 squash 合并；merge commit：`3ac995fd7069148bea56cb8baaf11ab794987cdd`。
- [x] Android Build #209（Run ID `41283758964`）全部成功：资源校验、JUnit、androidTest Kotlin 编译、Debug APK 构建和 Artifact 上传。
- [ ] CI 编译了 Activity 仪器测试源，但未在 Hosted 模拟器实际启动 Activity；真机四入口、横竖屏、Insets / IME 和视觉仍需验收。
- [ ] 旧 `ZhixingLiquidBottomBar` 与其手势 / 装饰代码暂时保留，待 P2.4.3 设备验收后在 P2.5 单独清理。


### 10.54 P2.4.3a Glass Lab 性能档位与 DataStore 回归（已合并，2026-11-01）

- [x] 新增 `ZhixingCatalogMaterialPreviewIntegrationTest.kt`，通过实际 MainActivity 打开 Glass Lab。
- [x] 覆盖 Off → Standard 档位切换，验证 UI 档位标识变化，且返回首页后官方四个 Tab 仍可见 / 可导航。
- [x] 覆盖“重置材质参数”后回到默认自定义状态，再返回首页确认底栏和导航仍可用。
- [x] 测试 `@After` 调用 `GlassMaterialSettingsStore.resetToDefaults()`，避免仪器测试把被测设备 DataStore 留在 Off 或自定义档位。
- [x] PR [#58](https://github.com/PaleSquire71029/zhixingapp/pull/58) 已 squash 合并；merge commit：`9286a44ddc41eaad18413d098dfd939847ca5945`。
- [x] Android Build #214（Run ID `41284104033`）全部成功：资源校验、JUnit、androidTest Kotlin 编译、Debug APK 构建和 Artifact 上传。
- [ ] CI 仅编译 Activity 仪器测试源，未在 Hosted 模拟器执行交互；真实 Glass Lab 即时视觉仍需设备验收。


### 10.55 P2.4.3b IME / Insets 与横竖屏回归测试（代码已建立，待 CI，2026-11-01）

- [x] 新增 `ZhixingCatalogBottomBarInsetsOrientationTest.kt`，复用生产 `ZhixingLiquidBottomBarPlacement` 和官方 Catalog 包装层。
- [x] IME 场景使用可聚焦 `BasicTextField` 唤起输入法，读取 `WindowInsets.ime`，检查底栏底边位于 IME 顶部之上，并验证四个 Tab 仍可访问。
- [x] 方向场景通过 `MainActivity.requestedOrientation` 强制横屏再恢复竖屏，检查四个 Tab 始终可见。
- [x] 未修改生产 Insets 辅助函数、MainActivity、导航状态或依赖；本步骤只新增 instrumentation tests。
- [ ] Android CI 待验证；工作流只编译 `androidTest` 源码，不会在 Hosted 模拟器执行这些设备相关用例。
- [ ] 真实导航栏高度、输入法窗口行为、横竖屏视觉和窄屏触控仍需目标设备验收。


### 10.56 P2.4.3b–P2.4.3c 底栏连续性与静止材质细化（2026-11-01）

- [x] PR [#60](https://github.com/PaleSquire71029/zhixingapp/pull/60) 的 IME / Insets 与横竖屏测试源码已合并；Android Build #221（Run ID `41288408310`）成功。CI 只编译仪器测试源码，不等同于在目标设备执行。
- [x] PR [#62](https://github.com/PaleSquire71029/zhixingapp/pull/62) 修复选中索引回调身份不稳定引起的动画状态重置；新增胶囊位置变化回归测试；Android Build #223（Run ID `41289697114`）成功。
- [x] PR [#63](https://github.com/PaleSquire71029/zhixingapp/pull/63) 增加选中胶囊静止态基础折射、边缘高光和内外阴影；按压时继续增强；Android Build #224（Run ID `41289997437`）成功。
- [x] PR #63 合并 commit：`2a1a553697229e7c2b485e891425a6595d965470`。
- [ ] P2.4.3c 真机视觉与性能验收待完成；不能把 CI 编译成功表述为真机表现已通过。
- [ ] 旧 `ZhixingLiquidBottomBar` 继续保留，直到用户完成 P2.4.3c 验收，之后才进入 P2.5 清理。


### 10.57 G1–G2 圆角 Tokens 与首页内容玻璃化（2026-11-01）

- [x] PR [#64](https://github.com/PaleSquire71029/zhixingapp/pull/64) 合并，补充设计规范：按组件尺寸采用 12 / 16 / 20 / 24 / 28 / 32dp 圆角，区分内容层、交互层、悬浮层与面板层；定义按钮、键帽、卡片、开关、弹层和页面转场的动画基线。
- [x] PR [#65](https://github.com/PaleSquire71029/zhixingapp/pull/65) 合并，commit `7c4ea0d33c0b8b1402e9c861d3dc5c2cf7f85817`；新增 `ZhixingGlassTokens.kt` 和四项 Token JVM 测试；Android Build #229（Run ID `41290656895`）成功。
- [x] PR [#66](https://github.com/PaleSquire71029/zhixingapp/pull/66) 合并，commit `679e4649e2b5375a1efedd8715a4af55871d5673`；首页 Hero 和三个功能内容条目采用共享 `ZhixingGlassSurface`，文本不启用色散折射；Android Build #232（Run ID `41291006825`）成功。
- [ ] 真机视觉、阴影强弱和滚动流畅度仍待目标设备验收。

### 10.58 G3 官方 LiquidButton（2026-11-01）

- [x] PR [#67](https://github.com/PaleSquire71029/zhixingapp/pull/67) 合并，commit `e6ba694e3cc2f384156c3a21081127e1c860cb82`。
- [x] 从固定的 AndroidLiquidGlass Catalog 1.0.6 移植 `LiquidButton.kt`；知行适配层使用 16dp 紧凑控件圆角、48dp 高度、轻量浅色表面，并保留官方透镜与按压高光。
- [x] 首页“材质设置”入口切换为液态按钮；增加 MainActivity 集成测试，检查按钮可启用并打开 Glass Lab。
- [x] Android Build #235（Run ID `41292689956`）全部成功；Artifact 上传完成。
- [ ] CI 编译了 Activity 测试但未实际启动设备；按钮实际触感 / 视觉还需目标手机验收。

### 10.59 G4.1 官方 LiquidToggle（2026-11-01）

- [x] PR [#68](https://github.com/PaleSquire71029/zhixingapp/pull/68) 合并，commit `0eb46ffb3951cbd4539759b9f039903728aeb509`。
- [x] 移植官方 `LiquidToggle.kt` 的阻尼拖动、玻璃滑块、内外阴影和组合 Backdrop；使用固定浅色主题色，不依赖系统深色模式。
- [x] 新增 `ZhixingLiquidToggle.kt`：稳定 selected reader / callback 身份；公开 enabled 与 touch-feedback 设置。
- [x] 仅把 Glass Lab 的“边缘折射”一个设置项替换为液态开关；原 `refractionEnabled` 状态、性能档位禁用条件和 DataStore 写入流程保持不变。
- [x] 开关补充 `Role.Switch`、`toggleableState`、ContentDescription、语义点击与 disabled 语义。
- [x] 新增 Activity 集成测试，覆盖开关状态语义与切换后标题更新；Android Build #239（Run ID `41293096869`）全部成功。
- [ ] CI 只编译仪器测试源；实际拖动、无障碍读屏、折射表现和触控延迟仍需目标手机验收。


### 10.60 G4.2 色散折射 LiquidToggle（已合并，2026-11-01）

- [x] PR [#71](https://github.com/PaleSquire71029/zhixingapp/pull/71) 已合并，commit `cefb07b22aedf61d3fb29ca9928477671c65418e`。
- [x] “色散折射”由文字按钮换成 `ZhixingLiquidToggle`，状态仍写入原有 `chromaticDispersionEnabled`；未新增独立持久化字段。
- [x] 保留 Android 13+ 支持条件、边缘折射开启条件、Off 性能档位禁用条件及统一触摸反馈配置。
- [x] 新增语义测试覆盖依赖禁用、开启后状态切换，并尝试恢复测试前设置。
- [x] Android Build #245（Run ID `41319607292`）通过：JUnit、androidTest Kotlin 编译、Debug APK 组装与 Artifact 上传成功。
- [ ] 目标设备实际拖动 / 弹性、读屏点击、色散画面效果、性能和发热仍待验收。
- [ ] 下一步 G4.3：边缘高光开关迁移。


### 10.61 G4.3 Glass Lab 全部控件批量液态化（待 CI，2026-11-01）

- [x] 新增官方 Catalog 1.0.6 `LiquidSlider.kt` 移植与 `ZhixingLiquidSlider.kt` 包装层，使用阻尼拖动、真实 Backdrop、玻璃滑块头与动态填充轨道。
- [x] 三个滑块迁移：透明程度、背景模糊、折射强度。保留各自原始值域、steps、API / 依赖门槛、实时预览与松手提交策略。
- [x] 剩余三个设置项“边缘高光”“环境反射”“触摸光效与形变”接入液态开关；现有“边缘折射”“色散折射”保持不变。
- [x] Premium / Standard / Lite / Off 性能档位、返回与重置入口改用液态按钮，并共享触摸反馈设置。
- [x] 新增 Activity 集成测试覆盖新增开关、Slider 语义和无障碍 setProgress；测试结束后清理材质设置。
- [ ] Android Build、JUnit、androidTest 源码编译、Debug APK 和 Artifact 等待本 PR 检查；自动化流程不会代替目标真机运行。
- [ ] 需在目标设备验收开关拖拽、滑块连续拖动 / 点击、TalkBack、滚动、帧率、温升与耗电。


### 10.62 G4.3 全部材质实验室控件液态化（已合并，2026-11-02）

- [x] PR [#73](https://github.com/PaleSquire71029/zhixingapp/pull/73) 合并，merge commit `77cf6324121ad8ce4fa0c8bd501d87a7cf3d8697`。
- [x] Android Build Run ID `41321362749` 成功：资源检查、JUnit、androidTest 源码编译、Debug APK 构建和 Artifact 上传成功。
- [x] Glass Lab 三个开关、三个滑块、四档性能按钮、返回和重置操作完成液态组件迁移；保留原参数、能力门槛和提交策略。
- [ ] 自动化工作流没有在目标手机执行仪器测试；滑块连续拖动、轨道点击、读屏、帧率和温升仍需真机验收。

### 10.63 G5 首页功能入口玻璃化（计划）

- [ ] G5.1：先盘点首页卡片和底部四个目的地，确定真实点击行为；制作一个入口卡样板。
- [ ] G5.2：其余入口统一迁移到共享 Backdrop 玻璃组件。
- [ ] G5.3：增加入口点击、选中状态、返回、窄屏和滚动回归测试。
- [ ] G5.4：目标设备验收视觉、触控与性能。



### 10.64 G4.4 导航胶囊连续位移动画同步（2026-11-02）

- [x] `ZhixingCatalogBottomBar` 改为向 Catalog 组件传递明确的 `selectedIndex` 值，去除函数对象身份作为动画状态键的隐式依赖。
- [x] 外部点击 / 页面切换由单一 `LaunchedEffect` 驱动现有 `DampedDragAnimation`；拖动结束直接回调目标路由并回弹，避免双重动画任务。
- [x] 新增可控 Compose 时钟测试，验证胶囊在中间帧发生真实位移，而不只是验证动画结束后的坐标。
- [x] Android Build Run ID `41473379855`（main commit `c38805b6145eb54a8e942103d371686a7fb92873`）成功：资源校验、JUnit、androidTest 源码编译、Debug APK 组装与 Artifact 上传通过。
- [ ] 真机视觉、帧率、触摸手感与系统减少动态效果适配仍待验收。


### 10.65 G5.1 首页入口现状审计（2026-11-02）

- [x] 检查 MainActivity.kt、AppDestination.kt 与全部 39 个主源码文件；当前尚无独立 calculator / ai / tools / history 页面包。
- [x] 确认顶部“材质设置”是真实按钮；Hero 仅随所选 Tab 更新介绍；三条 FeatureLine 没有点击行为；四个 Tab 当前只切换目的地摘要。
- [x] 决定不把静态 FeatureLine 改成伪按钮，避免出现点击无效或跳到空页面的入口。
- [ ] G5.1 可交互入口样板等待首个真实功能页面 / 稳定路由落地后再做；随后按真实业务行为设计首页快捷入口。


### 27.7 材质设置页重新设计（2026-11-02）

- [x] 将原“Glass Lab / Live”实验室式长列表改为接近 iOS 系统设置的浅色分组布局：大标题、简洁返回 / 恢复默认操作、性能模式分段选择，以及表面外观、光学效果、触摸反馈分组。
- [x] 移除页面底部动态背景与独立玻璃样本卡片，同时删除仅服务于该预览的动画、触摸追踪和采样辅助代码；设置页不再展示预览区。
- [x] 保留现有性能档位、全部开关、三个滑块的值域 / 步进、API 能力门槛、实时状态更新与 DataStore 提交 / 重置路径。
- [x] 分组面板继续复用共享 Backdrop；面板关闭折射与色散，避免设置文字和控件被光学位移影响。
- [x] PR #83 的 Android Build #269（Run ID `41476106045`）成功：资源校验、JVM 单测、androidTest 源码编译、Debug APK 组装与 Artifact 上传全部通过。
- [x] 构建 Artifact ID `13156938318`；APK 大小 `12,841,371 bytes`；SHA-256 `3f11c32dbf973159b3f2cbb346cce22c12308dcb086bb715e643438fa4e1e878`。
- [ ] 真机验收仍待完成：检查窄屏分组、滑块可拖动性、切换状态和关闭材质后的降级外观。


### 27.8 iQOO Neo5 活力版材质设置页手势与尺寸适配（2026-11-02）

- [x] 根据设备型号 V2118A 核对官方屏幕参数：6.57 英寸、2408×1080、20:9、LCD、最高 144Hz；以窄屏纵向布局作为本轮目标验收环境。
- [x] 修复 LiquidSlider 与纵向滚动容器的手势竞争：新增横向轴向锁识别，只有横向位移先超过 touch slop 且大于纵向位移时才接管；纵向手势不消费事件，交给父级 verticalScroll。
- [x] 将滑块可拖动命中区域扩大到 48×48dp，保持可见玻璃滑块头 40×24dp；轨道点击 / 无障碍 setProgress / 松手提交路径保留。
- [x] 增加横向拖动修改值与滑块区域纵向手势继续滚动的 Activity 测试源码。
- [x] 材质设置页加入 WindowInsets.safeDrawing，避免顶部操作与状态栏重叠，并为底部手势区保留空间。
- [x] 收敛窄屏版式：四档性能模式更换为短中文标签，外层分组间距、滑块行和开关行内边距适度收紧。
- [x] PR #84 已合并，merge commit `0e4f854a40f5eb52f1058ebaa70480a9cbe552e5`。
- [x] Android Build #278（Run ID `41479660281`）成功：资源检查、JVM 单测、androidTest Kotlin 源码编译、Debug APK 组装与 Artifact 上传全部通过。
- [x] 构建 Artifact `13160125773`；APK `12,841,371 bytes`；SHA-256 `d782f87baa9cc9b7f5f81efa3d54326cf6563aeefe6d74cba0a94ce7060d4e77`。
- [ ] 真机验收：横向拖动 3 个滑块、在滑轨上纵向划动页面、状态栏 / 底部手势区、144Hz 流畅度和窄屏文字完整性。


### 27.9 保留完整液态动效的滑块性能优化与触摸反馈开关材质修复（2026-11-04）

- [x] LiquidSlider 改为单一轴向锁手势识别器处理横向拖动与轨道点击，移除滑轨 / 滑块头重复 pointerInput，避免同一次触摸被两套识别器争抢。
- [x] 拖动期间采用合并帧队列直接跟随指尖，避免每个 MotionEvent 都创建新的 spring coroutine；松手后保留原有弹簧收尾、惯性速度、高光和透镜形变。
- [x] 材质设置中的三个滑块将高频数值状态限制在各自 SliderSetting 子树，参数持久化和全局材质更新在松手时提交，减少整个页面与多层 Backdrop 同步重组。
- [x] 触摸反馈开关轨道从纯色 drawBehind 改为共享 Backdrop 的 blur + lens + ambient highlight + shadow 玻璃轨道；滑块头保留透镜、边缘高光、内阴影与按压形变。
- [x] 添加最底部「触摸光效与形变」开关的可见与切换回归测试。
- [x] PR #85 已合并，merge commit `a6f2da42953ebf047ece77b3f1643c0f4da4d88b`。
- [x] Android Build #282（Run ID `41677099435`）成功：资源检查、JVM 单测、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传全部通过。
- [x] 构建 Artifact `13250469318`；APK `12,841,371 bytes`；SHA-256 `6313979286d0b0d145b96e7d2c70ca3203f7c54872a2d39902c4820c03e5498d`。
- [ ] 真机验收：持续拖动与快速往返、松手回弹、纵向页面滚动、最底部触摸反馈开关的静态 / 按压玻璃效果及整页帧率。
- [ ] 真机验收：持续拖动、快速往返、松手回弹、滚动手势、最底部开关闲置 / 按压玻璃效果和整页帧率。


### 27.10 冷启动首屏与滑块帧路径性能优化（2026-11-04）

- [x] 移除滑块拖动期间的 Channel + 协程帧中转；改为直接更新 Compose 快速浮点快照状态，松手时只将最终值同步回 Animatable，并保留原有弹簧、速度挤压、按压和回弹效果。
- [x] 滑轨填充长度由每帧改变布局宽度改为 graphicsLayer scaleX，拖动时不再触发 fillMaxWidth 比例布局测量。
- [x] 将材质设置行中的实时数值文本拆成独立重组作用域；滑块通过稳定值读取器接收状态，避免拖动时重建整个 LiquidSlider / Backdrop 子树。
- [x] 首屏三张静态说明卡片继续使用共享 Backdrop 采样和边缘高光，但取消每张卡片独立的模糊 / vibrancy / 环境反射通道；主视觉卡片、顶部液态按钮、滑块与导航栏继续保留完整光学效果和交互动效。
- [x] PR #86 已合并，merge commit `fba4e5ed5e22ef555bbab33f16f2844dd6edcc95`。
- [x] Android Build #284（Run ID `41679016000`）成功：资源检查、JVM 单测、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传全部通过。
- [x] 构建 Artifact `13252920600`；APK `12,841,371 bytes`；SHA-256 `0979a1f1a6cae046103aa6ee26d86ea3957d56cb4087f513c9bcc2e5bebccf0e`。
- [ ] 合并后 main 分支构建仍在运行；真机验收：冷启动首屏帧时间、三个滑块连续往返拖动、松手弹簧、首屏滚动与导航胶囊动画。
- [ ] 设备实测：iQOO Neo5 活力版冷启动首屏帧时间、三个滑块连续往返拖动、松手弹簧、首屏滚动与导航胶囊动画。
- [ ] 目前没有接入 Macrobenchmark / 帧时间采集模块，因此不能仅凭 CI 编译结果宣称达到稳定 60 / 90 / 144fps；需要后续真机帧时间数据验收。


### 27.11 回归原版 Catalog 滑块 / 开关交互并修复启动闪帧（2026-11-05）

- [x] 检查用户之前上传的 `base.apk`：DEX 中包含 `Backdrop Catalog` 与 `com/kyant/backdrop/catalog/MainActivity` 标识；按 Backdrop Catalog 1.0.6 源码逐项对照滑块、开关、手势检测和滚动容器实现。
- [x] 滑块恢复为原版交互分工：轨道负责点击定位，玻璃滑块头负责拖动；滑块头使用 Compose Foundation `detectHorizontalDragGestures`，横向触摸越过 touch slop 后才接管，纵向意图会取消该检测器，让设置页 `Column.verticalScroll` 接管，不再使用自定义轴向指针循环。
- [x] 保留 48dp 滑块头触控区域、无障碍进度语义、步进吸附，以及 GPU 图层填充缩放；恢复 Catalog Animatable + spring + VelocityTracker 的原始拖动与松手回弹状态链。
- [x] 开关轨道恢复 Catalog 原版半透明色带（`layerBackdrop + clip + drawBehind`），移除新增的模糊 / lens 轨道采样；玻璃旋钮恢复 combined Backdrop、lens、高光、内阴影、速度形变。
- [x] “触摸光效与形变”开关自身常驻触摸反馈，不再把自身是否启用的值作为自身触摸动效开关，关闭状态下仍能通过按压预览动效并重新启用。
- [x] 启动时先读取 DataStore 的真实材质配置再创建主页 Backdrop；读取期间显示与正式背景一致的静态渐变，避免先绘制默认材质再切换用户配置导致首帧闪烁。
- [x] Android 窗口启动背景改为与主页渐变一致的 drawable，避免系统启动窗口的纯白底与应用首屏产生明显色差闪帧。
- [x] 增加回归测试：滑块头横向拖动、轨道点击、从滑块头开始纵向滚动、触摸反馈开关在关闭状态下重新启用。
- [x] PR #87 已合并，merge commit `a76eae5b3daf9ac152df63d6b2579b79cf46f301`。
- [x] Android Build #289（Run ID `41874462608`）成功：资源校验、JVM 单测、androidTest Kotlin 源码编译、Debug APK 组装与 Artifact 上传全部通过。
- [x] 构建 Artifact `13345538217`；APK `12,842,023 bytes`；SHA-256 `f1f5641d68c56e6d449ecb8ceb8f175bf2b8a6218271ccef8cebd90163fece3f`。
- [ ] 真机验收：iQOO Neo5 活力版上测试滑块拖动 / 轨道点击 / 从滑块头纵向滚动、开关透明玻璃与按压反馈、冷启动闪帧。
- [ ] 真机验收：iQOO Neo5 活力版检查滑块连续拖动、从滑块处纵向滚动、开关透明玻璃与按压形变、冷启动过渡是否消除闪帧。


 
### 27.16 阶段 4：计算器原生核心迁移准备（2026-11-06）

- [x] 核对旧仓库 `PaleSquire71029/calculator` 当前 main：核心计算器为 `calculator.html`（约 283k 字符 / 4,262 行）；化学配平与预测分别位于 `js/chemistry-balancer.js`、`js/chemistry-predictor.js`。
- [x] 确认旧数学内核包含 tokenizer、递归下降解析器、多项式 / 有理函数运算、实根求解、方程、不等式、坐标函数拟合与单位换算；先迁移纯逻辑，不搬 UI。
- [x] 核对新 App 当前只有 `:app` 模块，导航四页仍是占位壳；尚无独立 Kotlin 数学域包。
- [x] 新增 `docs/CALCULATOR_CORE_MIGRATION_PLAN.md`，将阶段 4 拆为 C0–C12，明确语法语义、黄金样例、代码包结构、算法依赖与验收门槛。
- [x] 当前下一项：C0.1 建立表达式 / 方程 / 不等式黄金样例；这一步只整理测试夹具和旧逻辑对照，不实现 parser、不接 UI。
- [ ] PR #88（连续滑块、开关长按滚动仲裁、120Hz 窗口偏好）仍未合并；材质手势与显示刷新率工作必须单独收尾，不与数学内核混在同一个 PR。
- [ ] Android Build Run ID `42196358207`（Build #322）的 APK 组装和 Artifact 上传通过，但 API 30 模拟器仪器测试因 ADB `device offline` / `No compatible devices connected` 而失败；自动化设备环境未成功运行，不能记为手势断言通过。
- [ ] iQOO Neo5 活力版真机验收仍未完成：连续拖动、长按开关锁定滚动、正常纵向滚动、系统最终刷新率与真实帧时间均待实测。
- [ ] 120Hz 使用窗口 preferredRefreshRate 偏好；系统是否授予 120Hz、是否稳定 120fps，仍取决于屏幕模式、省电 / 温控和真实帧时间数据，不能把 API 请求等同于实测结果。


### 27.17 原生计算器页面设计预研与 120 帧真机反馈（2026-11-06）

- [x] 用户反馈：iQOO Neo5 活力版当前安装版本的 **120 帧效果已经生效**。此项按用户目标设备实机反馈记录为已确认，不再沿用 27.16 中“120Hz 是否生效仍待验证”的旧判断。
- [x] 区分验证范围：云端 API 30 模拟器 ADB offline 只代表该次 CI 仪器测试环境不可用，不否定用户在真实设备上的 120 帧体验。
- [ ] 本项目尚未保存 Perfetto / Macrobenchmark 帧时间分布报告；这属于进一步性能分析，不影响记录用户已确认 120 帧生效这一事实。
- [x] 新增 `docs/CALCULATOR_NATIVE_UI_DESIGN.md`：计算器页面先按原生单手操作重新构思，不照搬旧网页；明确算式 / 结果区、固定四列数字键盘、标准 / 科学模式、独立工具页、材质重点和状态覆盖。
- [x] 确认现有首页四个底部目的地仍为占位路由，当前尚无真实计算器页面与 Kotlin 数学域代码；本次只完成设计预研，没有修改 App UI / 算法代码。
- [ ] PR #88（连续滑块、开关长按滚动仲裁与 120Hz 窗口偏好）仍需独立收尾；用户实机 120 帧反馈与该 PR 的 CI / 合并状态分别记录，不能混为同一个结论。
- [ ] 下一项代码开发仍为 C0.1：建立表达式 / 方程 / 不等式黄金样例；页面设计稿作为 C12 原生 UI 实施基线。



### 27.18 底部导航调整与图标体系设计（2026-11-06）

- [x] 用户确定：将原「历史」一级 Tab 替换为「我的」；历史记录与应用设置统一收纳在「我的」页面中。
- [x] 一级导航更新为「计算 / 知行 AI / 工具 / 我的」，不再单独占用历史 Tab。
- [x] 「我的」页面规划包含：轻量身份区、最近使用、历史记录入口、材质设置入口、应用设置分组和底部版本信息；普通本地计算不强制登录。
- [x] 新增四枚统一自绘图标的方向：计算（显示窗 + 按键点阵）、知行 AI（双弧信息流）、工具（错位几何模块）、我的（头像核心 + 肩部弧线）；历史和设置另有配套二级图标。
- [x] 图标规格统一为 24dp 画布、圆角端点、约 1.8–2dp 描边、浅蓝 / 蓝紫局部选中填充；用 Compose ImageVector / Path 实现，避免 emoji 与系统默认图标混搭。
- [x] 更新 `docs/CALCULATOR_NATIVE_UI_DESIGN.md` 至 0.2 版，补齐导航结构、「我的」信息架构、图标规范与实现验收要求。
- [ ] 目前仅完成设计约定，尚未改动 `AppDestinations`、底部导航 UI 或新增「我的」页面；须在后续导航 UI 小步中实施。
- [ ] 下一项算法开发仍是 C0.1；「我的」导航实现应另列独立 UI 任务，不能伪装为已完成。



## 24. 2026-11-06：一级导航与「我的」页面首个实现小步

- [x] 底部一级导航改为「计算 / 知行 AI / 工具 / 我的」，原「历史」一级 Tab 已移除。
- [x] 四枚一级图标改为统一 24dp 几何线稿；历史与材质设置使用同一套二级图标规范，保留原有液态胶囊和手势动画实现。
- [x] 新增「我的」原生页面：轻量品牌身份区、最近使用空状态、历史记录入口、材质设置入口和动态读取的应用版本号。
- [x] 材质设置从计算器首页移除，改由「我的」进入现有 Glass Lab；返回后保留「我的」选中状态。
- [x] 新增历史记录页面与明确空状态。当前版本还没有本地计算历史存储，因此不显示虚构记录，也不将 AI 对话伪装成本地计算记录。
- [x] 为根页面与「我的」二级页面加入可保存状态容器，支持返回时恢复页面滚动与设置页面状态。
- [x] 将 Debug 版本提升至 0.1.2 / versionCode 3。
- [x] 新增 / 更新导航契约单测与主界面仪器测试。
- [x] GitHub Actions Android Build #336（Run ID：`42226324380`，Commit：`62104cc39d047e232f45596f8d88d58a4eeb622c`）整体成功。
- [x] `:app:testDebugUnitTest`、`:app:compileDebugAndroidTestKotlin` 和 `:app:assembleDebug` 均通过；仪器测试本轮只完成 Kotlin 编译，没有在模拟器 / 真机上执行。
- [x] Debug APK 已上传为 Artifact：`zhixingapp-debug`，Artifact ID `13506396489`，压缩包大小 12,360,525 bytes；这是工作流 ZIP 的大小，不是 APK 文件大小。
- [ ] 通过带有 `build-preview-apk-once` 标记的后续提交触发仓库自动归档；归档完成后核验 `artifacts/zhixingapp-preview.apk`。
- [ ] iQOO Neo5 活力版真机需检查四个图标对齐、导航胶囊动画、历史 / 材质页返回、窄屏与系统字体放大。
- [ ] 本项不包含计算核心、AI 客户端、账号、本地历史存储和应用设置偏好迁移；这些仍按独立计划推进。
- 过程中 CI 曾分别发现缺少 `dp` 导入及备用导航预览仍引用 `destination.symbol`；已修正，最终成功构建对应 Commit 为 `62104cc`。
- Actions：<https://github.com/PaleSquire71029/zhixingapp/actions/runs/42226324380>


## 25. 2026-11-06：圆角图标与整条液态玻璃导航底座

- [x] 一级导航与「我的」二级入口统一使用 Material Symbols Rounded：calculate、smart_toy、handyman、person、history、tune。
- [x] 将 Google Material Symbols Rounded 源图形转换为 Android 原生 VectorDrawable，资源本地打包；主导航支持描边与填充态的轻量交叉动画，选中色与图标填充同步变化。
- [x] 添加 Material Symbols Apache 2.0 许可证及来源说明：`app/src/main/assets/licenses/`。
- [x] 整条底部导航底座应用浅色玻璃渐变、Backdrop 采样、模糊 / 折射（受系统 API 能力约束）、环境反射、边缘高光和柔和内外阴影；选中胶囊采用浅蓝玻璃层，保留原有拖动弹簧与滑动动画。
- [x] 修复首轮 CI 暴露的两个问题：移除整条底座上重复的 drawBackdrop 参数；修正 `animateColorAsState` 的 Compose 导入路径。
- [x] GitHub Actions Run `42230609031`（Commit：`338de5ad167fac89bc255cda141e15b1055d5aca`）通过：`:app:testDebugUnitTest`、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug` 均成功。
- [x] Debug APK 工作流 Artifact：`zhixingapp-debug`，Artifact ID `13507224321`，ZIP 大小 12,374,313 bytes（此数值为压缩包大小，不是 APK 大小）。
- [ ] 本轮仪器测试只完成 Kotlin 编译，尚未在模拟器 / 真机执行；iQOO Neo5 活力版仍需验收玻璃折射、图标视觉权重、标签对齐、窄屏和系统字体放大。
- [ ] 仓库内可安装 APK 需通过一次性预览归档工作流生成并核验。
- Actions：<https://github.com/PaleSquire71029/zhixingapp/actions/runs/42230609031>


### 27.19 C0.1：表达式、方程与不等式黄金样例（2026-11-06）

- [x] 审计旧仓库 `calculator.html` 的 tokenizer、递归下降解析器、有理函数表示、方程与不等式求解路径。
- [x] 新增 `CalculatorGoldenCases.kt`，建立 token 归一化 / 错误、基础算术与优先级、百分号、方程根与分母排除、不等式开闭区间与高次边界样例。
- [x] 明确使用语义结果记录解集与定义域排除，不把解释文字排版当成数学结果契约。
- [x] 新增 `CalculatorGoldenCasesTest.kt`，验证样例 ID、期望结果互斥关系、区间结构、定义域标记和覆盖标签。
- [x] 本步 JUnit 已通过，验证的是黄金样例数据完整性；尚无 parser / evaluator / solver，因此**不代表旧数学算法已经迁移或通过等价性测试**。
- [ ] C1 Tokenizer 尚未开始；后续实现必须逐项匹配本批词法期望，并扩展更多异常情况。


### 27.20 C1：纯 Kotlin Tokenizer（2026-11-06）

- [x] 新增 `domain/math/model/MathToken.kt`：定义 token 类型、原始词面、数字值、源偏移和类型化词法错误。
- [x] 新增 `domain/math/parser/MathTokenizer.kt`：实现纯 Kotlin 词法扫描，不依赖 Android / Compose / JavaScript。
- [x] 保留旧版运算符、Unicode 乘除号与负号别名、全角括号、全角 x、上标 ² / ³ 归一化规则；仍保持 ASCII 大写 X 为非法字符，与旧版 tokenizer 一致。
- [x] 数字扫描支持 `.5`、`1.`；单独小数点和多个小数点按旧版中文错误语义返回失败。
- [x] token 与错误均映射回原始输入偏移；上标展开生成的两个 token 都指向原始上标字符。
- [x] 新增 `MathTokenizerTest.kt`：消费 C0.1 全部 tokenizer 黄金样例，并增加源偏移、空白处理、别名和大写 X 回归测试。
- [x] GitHub Actions Run `42234439958`（提交 `a17c579`）成功：JVM 单元测试、androidTest Kotlin 源码编译、Debug APK 组装及 Artifact 上传均通过。
- [ ] 尚无 parser / evaluator，不能进行端到端计算等价性验证；仪器测试源码仅编译，未在模拟器 / 真机执行。
- [ ] 尚未接入计算器 UI；下一小步为 C2 AST 与递归下降解析器。
- 过程记录：C0.1 初次编译发现黄金样例域模型字段名错配，已拆分为方程定义域排除点与不等式排除点，并修正相应完整性测试；本次 C1 CI 将同时复验 C0.1 与 Tokenizer。


### 27.21 C2：AST 与递归下降解析器（2026-11-08）

- [x] 新增 `domain/math/model/MathExpression.kt`：定义数字、变量、一元表达式、二元表达式、百分号和显式分组节点，并为每个节点保留原始输入半开区间。
- [x] 新增 `domain/math/parser/MathExpressionParser.kt`：递归下降实现加减、乘除、隐式乘法、一元正负、幂链、后缀百分号和括号。
- [x] 乘方高于一元正负；乘除按左结合；幂指数链按右结合；保留旧版只接受非负整数指数链、指数中间值上限 64 的词法后语法限制。
- [x] 显式乘法与隐式乘法在 AST 中有独立标记；显式括号保留为 GroupExpression，不提前丢失输入分组。
- [x] 解析器只构建 AST，不执行运算、不处理除零、不做多项式化简或求根；常数表达式求值留在 C3。
- [x] 新增 `MathExpressionParserTest.kt`：覆盖 -2^2、(-2)^2、-(2^2)、幂右结合、除法左结合、隐式乘法、百分号、错误类型和 Unicode 原始位置。
- [x] C2 对应 Android Build #350（Run ID `42443214070`）已通过 JUnit、androidTest Kotlin 源码编译和 Debug APK 构建；未执行设备端仪器测试。
- [ ] 未接入 Compose 页面或键盘；下一步 C3 常数表达式求值与错误边界。



### C2 首轮 CI 反馈修正（2026-11-08）

- [x] Android Build Run `42443068196` 的 Kotlin 编译已通过，失败仅发生在 `powerChainsAreRightAssociative` 的 AST 测试断言。
- [x] 确认实现生成的是预期右结合树 `2^(3^2)`；测试错误地把内层幂节点左侧常数写成 2，已修正为 3，并同时断言右侧常数为 2。
- [x] 修正后的 C2 Android Build #350（Run ID `42443214070`）完整 CI 成功；资源校验、JUnit、androidTest 源码编译、Debug APK 组装与 Artifact 上传均通过。


### 27.22 C2 右结合 AST 断言修正（2026-11-08）

- [x] C2 实现提交：`ea711e0e712fd748699ad23a55392bb498592e60`（`feat: add calculator expression AST and parser`）。
- [x] 首轮 Android Build Run `42443068196` 的 Kotlin 编译成功；失败点仅为 `powerChainsAreRightAssociative` 测试对内层幂节点的期望值写错，不是 Parser 实现编译失败。
- [x] 复核 `2^3^2` 的 AST 应为 `2^(3^2)`；修正测试，要求根节点右侧为幂节点，内层幂的左侧常数为 3、右侧常数为 2。
- [x] 修正提交：`782dd334f40d4ab17e6c87dd5962e3f3be65475f`（`test: correct right-associative power AST assertion`）。
- [ ] 修正提交当前尚无可核实的完整 CI 成功记录；必须重新运行并确认 JUnit、androidTest 源码编译和 Debug APK 组装后，才能把 C2 标记为 CI 通过。
- [x] C2 仍只负责构建 AST，不做求值；CI 通过后进入 C3。


### 27.23 C3：常数表达式求值器与算术回归（2026-11-08）

- [x] 新增 `domain/math/model/MathEvaluation.kt`：定义 evaluator 错误码、求值结果，以及 tokenizer / parser / evaluator 三阶段的统一计算结果。
- [x] 新增 `domain/math/evaluator/MathExpressionEvaluator.kt`：递归求值 Number / Group / Unary / Binary / Percent AST；实现加减乘除、幂、正负号与百分号缩放。
- [x] 新增 `MathArithmeticCalculator` 纯 Kotlin 入口，串联 Tokenizer → Parser → Evaluator；保留各阶段错误消息、来源偏移和词面信息。
- [x] 除数为正零或负零均明确返回“除数不能为 0”；溢出、Infinity、NaN 不作为成功数值返回；包含 x 的表达式返回旧版常数计算提示。
- [x] 不在本步进行小数格式化、分数化简、科学计数法显示或 UI 接入；成功结果保留原始 Double。
- [x] 新增 `MathExpressionEvaluatorTest.kt`：遍历全部算术黄金样例，另测除负零、非有限数、百分号连用、0^0、正号、变量错误偏移和 tokenizer / parser 阶段错误。
- [x] C3 Android Build #363（Run ID `42444326915`）成功：JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传通过。
- [ ] C3 只验证常数计算；不代表方程 / 不等式、函数、单位、化学或计算 UI 已完成。


### 27.24 C3 完整 CI 验收与 C4.1 多项式基础运算（2026-11-08）

- [x] C3 Android Build #363（Run ID `42444326915`）结果为 success；Artifact `zhixingapp-debug`，Artifact ID `13603006686`。
- [x] C3 已通过算术黄金样例回归，覆盖四则、优先级、幂、百分号、除零、变量拒绝和错误阶段 / 偏移。
- [x] 新增 `domain/math/model/Polynomial.kt`：系数按常数项到高次项升序保存；规范化尾部近零系数，零多项式保持 `[0]`。
- [x] 新增 `domain/math/model/PolynomialOperation.kt`：定义多项式运算 / 求值成功与错误结果。
- [x] 新增 `domain/math/algebra/PolynomialArithmetic.kt`：实现多项式加、减、乘、常数缩放与 Horner 求值；非有限输入 / 中间结果返回类型化错误。
- [x] 新增 `PolynomialArithmeticTest.kt`：覆盖系数顺序、次数、零多项式、尾项裁剪、加减乘、缩放、求值与非有限数边界。
- [ ] C4.1 的 JUnit、androidTest 源码编译和 Debug APK 构建待本次提交 CI 验证。
- [ ] 有理函数、原式定义域约束的合并 / 传播和实根求解尚未实现；不得将多项式基础运算标记为整个 C4 已完成。


### 27.25 C4.1 代码复核：零多项式规范化补强（2026-11-08）

- [x] 复核 C4.1 的 Polynomial 规范化逻辑，发现仅裁剪尾部近零项时，若所有系数都在容差内，最终会留下一个近零常数项，未统一成约定的零多项式表示 `[0.0]`。
- [x] 修复 `Polynomial.from`：先统一 `-0.0` 为 `0.0`，再裁剪尾部近零项；若全部剩余系数均严格小于 `ZERO_TOLERANCE`，统一归一为 `[0.0]`。
- [x] 扩展 `PolynomialArithmeticTest`：覆盖全系数近零、多项式常数项近零、负零规范化，并明确验证恰好等于容差的系数不会被误裁剪。
- [x] Android Build #364（Run ID `42444575859`，提交 `3618078`）最终结论为 `success`；首版 C4.1 的 JUnit、androidTest 源码编译、Debug APK 组装与 Artifact 上传通过。Artifact ID `13602144195`，ZIP 大小 12,410,686 bytes。
- [ ] 修正提交 `d6f411f` 对应 Android Build #365（Run ID `42444758613`）目前状态为 `in_progress`；该构建才是零多项式规范化补丁的验收依据，尚未取得最终结论或 Artifact。
- [ ] C4.2 有理函数与原式定义域约束传播仍未开始；本轮没有接入 Compose、键盘或 UI。


### 27.26 C4.1 验收完成与 C4.2.1 有理函数模型（2026-11-08）

- [x] 零多项式规范化补丁提交：`d6f411fa7e6cc740e5f2147c0d4cfac237ecf8b5`。
- [x] Android Build #365（Run ID `42444758613`）最终结论为 `success`；JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传通过。
- [x] Artifact `zhixingapp-debug`，ID `13603335893`，ZIP 大小 12,411,287 bytes；哈希 `sha256:82d711aaac03b9fda0254199e38d62254464b5620f6fce9ab54f101d17f80379`。
- [x] C4.1 完成：多项式模型、加减乘、缩放、Horner 求值、零多项式规范化和非有限值边界全部已通过 CI。
- [x] 新增 `domain/math/model/RationalFunction.kt`：分子 / 分母之外，独立保存原式定义域限制；创建时自动加入当前非恒定分母，并去重。
- [x] 有理函数模型拒绝零多项式分母；拒绝会造成空定义域的恒零限制；非零常数限制不进入排除集合；定义域输入列表被复制，避免外部修改污染对象。
- [x] 新增 `RationalFunctionModelTest.kt`，覆盖自动加入分母、继承限制合并 / 去重、常数限制、零分母、空定义域与输入列表隔离。
- [x] C4.2.1 对应 Android Build #366（Run ID `42444984226`）完整成功：JUnit、androidTest Kotlin 源码编译、Debug APK 组装与 Artifact 上传通过；Artifact ID `13603028718`。
- [ ] C4.2.2 加减乘除 / 幂的约束传播，以及 C4.2.3 定义域判定与有理函数求值尚未实现；本阶段没有接入 Parser、Solver 或 Compose。


### 27.27 C4.2.1 CI 验收与 C4.2.2 有理式运算传播（2026-11-08）

- [x] C4.2.1 提交 `cb04270c297bf9e5596351974034dd40f1e634b2` 的 Android Build #366（Run ID `42444984226`）结果为 `success`。
- [x] Artifact `zhixingapp-debug`，ID `13603028718`，ZIP 大小 12,414,498 bytes；SHA-256 digest `cea4caba2eda9e42d5786b781069ffd9b60f72caf8d17eef614ae0547706c2b7`。
- [x] C4.2.1 模型测试通过：当前分母自动加入排除集合、上游限制合并去重、非零常数不增加排除点、零分母与空定义域拒绝、输入列表防外部修改。
- [x] 新增 `domain/math/algebra/RationalFunctionArithmetic.kt`：实现有理函数加、减、乘、除和非负整数幂；通分与乘除使用纯多项式算法。
- [x] 所有运算合并两个操作数的原始定义域限制；除法额外把除数分子作为非零限制，拒绝恒零有理函数作为除数。
- [x] 幂运算使用平方求幂；指数 0 会把当前分式变成 1/1，但保留底式原始限制，因此 `(1/x)^0` 仍排除 x=0。
- [x] 不执行最大公因式约分，避免在域层尚未完成时抹掉原式定义域；多项式溢出映射为有理函数类型化错误。
- [x] 扩展 `RationalFunctionModelTest.kt`：增加加减通分、乘法限制继承、除法分子限制、恒零除数、零次幂定义域保留、正次幂和非法 / 溢出指数测试。
- [x] 增加 `x/x` 回归：即使分子与分母完全相同，也保留原式的 `x != 0` 限制，不在本阶段消去公共因子。
- [x] Build #367（Run ID `42445399522`）失败原因已定位为误导入 `domain.math.model.PolynomialArithmetic`，实际类位于 `domain.math.algebra` 同包；修正提交 `8913a3b` 已删除错误导入。
- [x] Build #368（Run ID `42445421203`）发现新增运算用例调用了未定义的 `rational(...)` 测试工厂函数；已补充工厂函数，并移除测试中同包冗余导入。
- [ ] Build #369 验证修复后的基础运算测试；增加 `x/x` 专项回归后的 Build #370 将作为本小步最终验收依据。
- [ ] C4.2.3 代入求值与定义域判定尚未实现；本轮仍未接入 Parser / Solver / Compose。

### 27.29 C4.2.3 首个子步骤：有理函数单点求值与原式定义域检查（2026-11-08）

- [x] 新增 `RationalFunctionEvaluationResult`，沿用 `RationalFunctionError` / `RationalFunctionErrorCode` 类型化错误体系。
- [x] `RationalFunctionArithmetic.evaluate(function, x)` 先检查所有原始 `domainRestrictions`，再计算分子 / 分母；显式防御分母为零与最终商溢出。
- [x] 新增测试：普通代入、多条继承限制、`x/x` 在零点仍未定义、零次幂保留排除点、NaN / Infinity 输入、多项式中间溢出、最终除法溢出。
- [x] Android Build #372（Run ID `42453575710`）成功：JUnit、androidTest Kotlin 源码编译、Debug APK 组装与 Artifact 上传通过；Artifact ID `13606083998`，12,418,755 bytes。未执行模拟器或真机仪器测试。
- [ ] 后续还需评估定义域近似零点策略、与 AST / parser 集成及边界行为；本子步骤只使用现有 Double 求值器的精确 `0.0` 判断。
- [ ] 下一小步：定义限制多项式的数值零点判定策略，并补充接近零但非零值的回归测试。

### 27.30 C4.2.3 精确零点判定与近零回归（2026-11-08）

- [x] `PolynomialArithmetic.isExactlyZeroAt` 将有限 Double 系数和自变量解释为精确 IEEE-754 二进制有理数，以 `BigInteger` 执行精确 Horner 运算，判断多项式值是否严格为零。
- [x] 常规 `PolynomialArithmetic.evaluate` 仍先执行，保持非有限输入 / 中间值的既有类型化错误；精确判零只决定定义域零点，不替换最终数值求值。
- [x] 增加近零但非零输入测试 `x=1e-300`；增加消去误差回归：Double Horner 对 `x² + 10¹⁶x − 10¹⁶` 在 `x=1` 得到 0，但存储系数所定义的精确值为 1，不能将该点误判为定义域外。
- [x] Android Build #379（Run ID `42454420363`）完整成功：资源检查、JUnit、androidTest Kotlin 源码编译、Debug APK 组装与 Artifact 上传均通过。Artifact `zhixingapp-debug` ID `13607222086`，12,421,460 bytes，SHA-256 `a1ce35b340731cd6a4eba9cd362b423973a0a9171c9f21a6988ef61f9a08ab5e`。
- [ ] 未运行模拟器或真机仪器测试。
- [x] 分母 Double Horner 结果为 0、精确二进制值非零时返回 `NUMERIC_PRECISION_LOSS`，覆盖下溢与消去误差两种情况。

### 27.31 C4.2.3 分母精度损失分类（2026-11-08）

- [x] 新增 `RationalFunctionErrorCode.NUMERIC_PRECISION_LOSS`。
- [x] 若分母 Double Horner 求值为 `0.0`，再次使用精确二进制判零：精确值为零时保留 `ZERO_DENOMINATOR_AT_INPUT` 防御性错误；精确值非零时返回 `NUMERIC_PRECISION_LOSS`，不误报为分母真实为零。
- [x] 增加两类回归：`x²` 在 `x=1e-200` 时下溢为零；`x² + 10¹⁶x − 10¹⁶` 在 `x=1` 时 Double Horner 消去为零。两者的精确二进制值均非零。
- [x] Android Build #381（Run ID `42454900909`）完整成功：资源校验、JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传全部通过。Artifact `zhixingapp-debug` ID `13607088513`，12,421,567 bytes，SHA-256 `a542ca67e6923cbd143b4dc7fa24472e4f0be5a1e7f9ff282b4f82186d44fe94`。
- [ ] 未执行模拟器或真机仪器测试。
- [x] 本节原定的分子精度边界已在 §27.32 完成并通过 Build #383；当前下一步为复核 C4.2.3 整体验收。


### 27.32 C4.2.3 分子精度损失保护（2026-11-08）

- [x] 在分母零值与精度检查完成后，检查分子 Double Horner 结果是否为 `0.0`；仅当 `PolynomialArithmetic.isExactlyZeroAt` 证明精确二进制值非零时，返回 `NUMERIC_PRECISION_LOSS`，不把下溢 / 消去误差伪装成有效的数值 0。
- [x] 增加回归：`x²` 在 `x=1e-200` 处下溢；`x² + 10¹⁶x − 10¹⁶` 在 `x=1` 处发生 Double Horner 消去。
- [x] 增加真正零分子回归：精确零多项式仍成功返回 `0.0`。
- [x] 实现与测试提交：`924f4958c2132b09ff016938ba4070c099c18a52`。
- [x] Android Build #383（Run ID `42455402733`）完整成功：资源校验、JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传全部通过。Artifact `zhixingapp-debug`，ID `13607311903`，12,421,616 bytes；SHA-256 `54dfd80e7956ebefa7f1ea5a4ae9bd59c28d9a7ba03140a103d1e9e9c494f058`。
- [ ] 本步骤未执行模拟器或真机仪器测试；CI 也不代表设备端验收。
- [ ] 下一步复核 C4.2.3 全部边界与整体验收定义；本项通过不代表 C4.2.3 整体验收完成。


### 27.33 C4.2.3 整体验收与 C4.3.1 一次实根基础（2026-11-08）

**C4.2.3 CI 验收完成：**
- [x] 普通求值、NaN / Infinity 拒绝、Horner 中间非有限值与最终商溢出有回归测试。
- [x] 所有原始 `domainRestrictions` 在求值前逐项检查；`x/x` 的可去间断点与 `(1/x)^0` 的原始排除点不因代数形式化简而消失。
- [x] `PolynomialArithmetic.isExactlyZeroAt` 以 BigInteger 精确二进制有理数 Horner 判零，不使用 epsilon；覆盖近零非零值和 Double Horner 消去。
- [x] 分子 / 分母 Double 求值舍入为 `0.0`、但精确值非零时均返回 `NUMERIC_PRECISION_LOSS`；精确零分子仍正常返回 0。
- [x] Build #372、#379、#381、#383 全部成功；其中 Build #383 Run ID `42455402733` 完整通过 JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传。
- [ ] 未执行模拟器或真机仪器测试；C4.2.3 的 CI 验收完成不代表设备端验收完成。

**C4.3.1：**
- [x] 新增 `PolynomialRootsResult`，明确区分全体实数、无实根、有限根、暂不支持次数和类型化失败。
- [x] `solveRealRoots` 支持零多项式、非零常数和一次多项式；线性根附带归一化残差。
- [x] 根超出 Double 范围时返回 `NON_FINITE_RESULT`；非零根下溢为零或残差超限时返回 `NUMERIC_PRECISION_LOSS`。
- [x] 二次及以上次数返回 `UnsupportedDegree`，不会输出未经实现的近似结果。
- [x] 首版提交 `860429b699492d0471cf4ac5deb53e7906b0440c` 的 Build #387 因新增错误码遗漏 `RationalFunctionArithmetic.mapFailure` 分支而编译失败；修正提交 `c6f59062cd89765d4affa3fa4dd1952ffed8c332` 补齐映射后，Build #388（Run ID `42456084109`）完整成功。
- [x] Build #388 Artifact `zhixingapp-debug` ID `13606854310`，12,423,470 bytes，SHA-256 `47c53f359a01803891be43029b17db0489460b55d6561fcc092db2a79ac8d157`。
- [ ] 未执行模拟器 / 真机测试；下一步 C4.3.2 实现二次多项式实根，重点处理判别式符号、稳定求根公式、根去重和残差边界。

### 27.34 C4.3.2 二次多项式实根（2026-11-09）

**实现范围：** 在现有 `PolynomialRootsResult` 与 `PolynomialError` 类型模型上扩展 `PolynomialArithmetic.solveRealRoots`，不增加 UI、输入键盘、图像或 AI。

- [x] 使用 `ExactDyadic` / `BigInteger` 将有限 Double 系数解释为精确二进制有理数，精确计算判别式符号；不使用固定 epsilon 判定重根或无实根。
- [x] 使用二进制缩放数表示中间值，避免直接计算 `b*b` 和 `4*a*c` 时溢出 / 下溢；正判别式采用稳定 q 公式，两个根分别由 `q/a` 与 `c/q` 得到，避免小根相减消去；零判别式返回单个重根，负判别式返回 `NoRealRoots`。
- [x] 对最终 Double 根执行精确二进制项求和的归一化残差检查 `|a*r²+b*r+c|/(|a*r²|+|b*r|+|c|)`，阈值保持 `1e-12`；根溢出映射 `NON_FINITE_RESULT`，非零根下溢、两个不同根无法区分或残差不合格映射 `NUMERIC_PRECISION_LOSS`。
- [x] JUnit 覆盖常规两个不同实根、`x²-1e16x+1` 的稳定小根、重根、无实根、判别式近抵消两侧、根溢出、根下溢和三次多项式仍返回 `UnsupportedDegree`。
- [x] 初次实现提交 `9a955bd2b3b61fb9fdc4980b14ebf9abd6ceb886` 对应 Build #390（Run ID `42544038191`）失败；日志定位为 `ROOT_RESIDUAL_TOLERANCE` 与 `rootsFailure` 被重复声明。未删除测试断言；修复提交 `9db9d21c9506b401959654bc6ecefc3ec7c043ff` 删除重复定义并复用既有类型化错误辅助函数。
- [x] Android Build #391（Run ID `42544184281`）完整成功：资源校验、JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传通过。Artifact `zhixingapp-debug`，ID `13642063693`，12,428,103 bytes；SHA-256 `e3708be725e4f8adced35b6c09801c4e150a3ce686dba559111242440510cc3f`。
- [ ] 未启动模拟器或执行真机仪器测试；CI 成功不等于设备端验收。
- [ ] 下一阶段 C5 方程求解；先单独建立方程结果模型与常数 / 一次方程基础测试，不接 Compose 页面。

### 27.35 C5.1 常数与一次多项式方程（2026-11-13）

- [x] 新增 `PolynomialEquation(left, right)`，保留等式两侧的多项式，不要求调用方提前手动移项。
- [x] 新增 `EquationSolutionResult`，区分 `AllRealNumbers`、`NoSolutions`、`FiniteSolutions`、`UnsupportedDegree` 和类型化 `Failure`；有限解携带根值和归一化残差。
- [x] 新增 `PolynomialEquationSolver.solve`：用现有多项式减法化为 `left(x) - right(x) = 0`，并复用已验证的零 / 常数 / 一次实根引擎。
- [x] 本步限定最高次数为一次；二次及以上返回 `UnsupportedDegree`，尽管 C4.3.2 已有二次根引擎，也不在本小步提前开放。
- [x] JUnit 覆盖恒等式、常数无解、一次方程移项、等式两边均含 x、零根、高次暂缓和移项相减溢出。
- [x] 代码提交：`9fe617a3ad1283341bde78b80d56fb775c2615bf`。
- [x] Android Build #393（Run ID `43394051873`）完整成功：资源校验、JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传均通过。Artifact `zhixingapp-debug` ID `14022361217`，12,434,313 bytes，SHA-256 `322d8591154a23e71bbb9b62a7d8a6ffd71cd888a503224c81ff5805b6fe3d84`。
- [ ] 未运行模拟器 / 真机仪器测试。
- [ ] 下一步 C5.2：在方程求解层开放二次方程，复用 C4.3.2 稳定求根结果，并验证方程结果映射、重根和无实根。


### 27.36 C5.2 二次多项式方程（2026-11-13）

- [x] 将 `PolynomialEquationSolver` 支持最高次数从 1 扩展到 2；仍以 `left(x)-right(x)=0` 规约，不复制或重写 C4.3.2 二次根算法。
- [x] 方程层映射覆盖两个不同实根、一个重根和无实根；有限解继续携带底层根的归一化残差。
- [x] 新增稳定小根回归：`x²-10¹⁶x+1=0`，验证方程层仍能保留约 `1e-16` 的小根及大根。
- [x] 新增错误映射回归：底层二次根求解返回 `NON_FINITE_RESULT` 时，方程结果保留相同类型化错误码。
- [x] 三次多项式仍返回 `UnsupportedDegree(3)`，没有提前启用 C5.3。
- [x] Build #395（Run ID `43394483739`）发现旧测试仍断言二次次数暂不支持；该断言与 C5.2 已开放二次的范围冲突。修正提交 `3123bc6cf72c9a93f2933df894dd8dfe1ac7a29e` 将该旧用例收窄为只验证三次暂缓，没有删去三次边界测试。
- [x] Android Build #396（Run ID `43394734621`）完整成功：JUnit、androidTest Kotlin 源码编译、Debug APK 组装与 Artifact 上传均通过。Artifact `zhixingapp-debug` ID `14022780460`，12,434,313 bytes；SHA-256 `28bae4de45a8f27b4690c707bff19f054099113c959f7021b19bf540706aabeb`。
- [ ] 未执行模拟器 / 真机仪器测试。
- [ ] 下一步 C5.3：新增三次方程求解与实根数量 / 重根分类，独立测试三次方程根与残差，不提前处理分式方程。


### 27.37 C5.3 三次多项式方程与 C5.4 分式方程（2026-11-13）

- [x] 三次实根分支使用精确二进制有理数判别式，分类单实根、三个不同实根、二重根和三重根；不使用固定 epsilon 猜判别式符号。
- [x] 数值分支使用缩放 Cardano / 三角公式；单实根分支增加 Newton 修正以处理小根消去；最终根检查 Double 范围、根区分和归一化残差。
- [x] 方程层最高支持次数提升到 3；四次及更高次数继续返回明确的 `UnsupportedDegree`。
- [x] 新增分式方程模型和 solver，复用有理式减法；左右原始分母及除法定义域限制完整传播。
- [x] 对候选根使用 `isExactlyZeroAt` 精确检查每个原式限制，剔除分母零点增根；恒等式结果以 `AllRealNumbersInDomain` 携带原式定义域，不返回无条件全体实数。
- [x] JUnit 新增三次方程与分式方程场景；覆盖三实根、单实根、二重根、三重根、小根近消去、大根、`1/x=2`、`x/x=1`、`x/x=0`、类型化错误和四次暂缓。
- [x] 功能实现提交 `933ba61f47ae4f40a138ff653ba4428f9e0b12f4`；Build #399（Run ID `43396736449`）发现唯一失败是旧测试仍断言三次不支持。修正提交 `e51fd565b8333e06aae8f88215ae9dca1f9bc5cb` 将旧用例改为四次边界，没有删除或放宽有效测试。
- [x] Android Build #400（Run ID `43397103889`）通过全部 JUnit、仪器测试 Kotlin 源码编译、Debug APK 组装和 Artifact 上传；Artifact ID `14022809017`，12,440,699 bytes。
- [ ] 未执行模拟器 / 真机仪器测试；算法在真实设备上的耗时和交互响应仍待后续整合时验证。
- [ ] 下一步 C5.5 绝对值方程；C5.6 根式方程；C5.7 指数 / 对数方程。


### 27.38 C5.5 线性内部绝对值方程（2026-11-13）

- [x] 新增 `AbsoluteValueEquation` 与 `AbsoluteValueEquationSolver`，支持 `|ax+b|=c`，其中内部为常数 / 一次多项式，右侧为有限常数。
- [x] 负目标直接判无解；常数内部式分类恒等式 / 矛盾式；一次内部式拆为 `ax+b=c` 与 `ax+b=-c` 两支。
- [x] 零目标只求一次分支，避免同根重复；有限根有序输出并保留根引擎的残差和类型化错误。
- [x] JUnit 覆盖双根、零目标单根、负目标、常数内部式恒等 / 无解、二次内部式暂缓及 NaN 输入。
- [x] 实现提交 `4d26a8d8b2d5e0c37454272bcf8af65cdf980cb1`；Android Build #401（Run ID `43398026099`）完整成功，资源检查、全部 JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传均通过；Artifact ID `14023737491`，12,443,101 bytes。
- [ ] 暂未支持变量右侧、多重 / 嵌套绝对值或二次及更高次数内部式；未执行模拟器 / 真机仪器测试。
- [ ] 下一步 C5.6 根式方程，再处理 C5.7 指数 / 对数方程。


### 27.39 C5.6 平方根方程与增根剔除（2026-11-13）

- [x] 新增 `RadicalEquation`、`RadicalEquationSolutionResult` 与 `RadicalEquationSolver`，范围为 `√P(x)=Q(x)`，被开方式次数≤2、右侧次数≤1。
- [x] 平方仅用于构造 `P-Q²=0` 的候选根；对候选逐个执行精确符号判定，要求 P≥0、Q≥0，再代回原根式并检查归一化残差。
- [x] 新增 `PolynomialArithmetic.signAt`，将有限 Double 看作精确二进制有理数，通过 BigInteger Horner 方式求符号，避免浮点舍入导致的定义域误判。
- [x] 若平方后的多项式为零，返回带非负条件列表的 `AllRealNumbersOnDomain`；如 `√(x²)=x` 会保存 `x²≥0` 与 `x≥0`，不误报全体实数。
- [x] JUnit 覆盖平方增根剔除、原式残差、负右侧、受限恒等式、边界根、全体实数恒等式、高次暂缓与平方溢出。
- [x] 实现提交 `9cc09a1e049ba9bb177aefb429adcd83127e872e`；Android Build #402（Run ID `43398997674`）完整成功，资源检查、JUnit、androidTest Kotlin 源码编译、Debug APK 组装与 Artifact 上传通过；Artifact ID `14023194525`，12,448,771 bytes。
- [ ] 未执行模拟器 / 真机仪器测试；立方根、多重 / 嵌套根式、任意高次被开方式和二次及更高次数右侧尚未支持。
- [ ] 下一步 C5.7 指数 / 对数方程。


### 27.40 C5.7 受限指数 / 对数方程（2026-11-13）

- [x] 新增指数 / 对数方程模型、结果模型与 solver，使用自然对数变换求解常底、线性指数 / 真数形式。
- [x] 支持 `a^P(x)=c`，`a^P(x)=b^Q(x)`，`log_a(P(x))=c` 和同底 `log_a(P(x))=log_a(Q(x))`；P、Q 为常数或一次多项式。
- [x] 校验指数底数为正、对数底数为正且不等于 1、指数式常数右侧为正；目标真数上溢 / 下溢或近 1 舍入不可分辨时返回类型化数值错误。
- [x] 所有有限候选根均回代检查原指数 / 对数关系；同底对数根逐个检查两个真数均严格为正。
- [x] 恒等式结果保留对数真数的严格正值域条件；指数恒等式只在结构上可证明时才返回全体实数。
- [x] 新增 22 个 JUnit 场景，覆盖换底、底数小于 1 / 等于 1、常数式、对数真数限制、严格定义域、无解、非法底数、数值精度和高次暂缓。
- [x] 功能实现提交 `88a9d18f8603d0388223e2196d0b8189b53e9692`；Build #403（Run ID `43400220986`）完整 success：全部 JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过；Artifact ID `14023699329`，12,465,463 bytes。
- [ ] 未执行模拟器 / 真机仪器测试。
- [ ] 下一阶段 C6 不等式求解：区间端点、分母零点、奇偶重根与严格 / 非严格不等式。


### 27.41 C6.1 一至三次多项式不等式（2026-11-13）

- [x] 新增 `PolynomialInequality`、`PolynomialComparison`、`RealInterval`、`PolynomialInequalitySolutionResult` 和 `PolynomialInequalitySolver`。
- [x] 支持多项式最高次数 3，覆盖 `<`、`≤`、`>`、`≥`；复用既有多项式实根引擎，不重复实现求根。
- [x] 通过根分割实数轴；有限根之间用中点 + `PolynomialArithmetic.signAt` 精确判符号，无界区间按首项符号和次数奇偶计算。
- [x] 正确处理偶数重根不变号、奇数重根变号；严格比较排除根点，非严格比较保留根点，并按闭端点条件合并区间。
- [x] 解集统一使用 `RealInterval`；无穷端点开，单点集用相同的闭端点表示。常数式 / 零多项式分类正确，四次及更高次数明确暂缓。
- [x] 对相邻根不可表示内部 Double 采样点或根列表与区间符号冲突的情形返回 `NUMERIC_PRECISION_LOSS`，不猜解集。
- [x] 新增 14 个 JUnit 测试方法，覆盖一次 / 二次 / 三次、两根 / 三根、二重 / 三重根、严格 / 非严格、常数、零多项式和高次暂缓。
- [x] 实现提交 `f23400eea148776895f7fccfac0c5c262e46ab29`；Android Build #404（Run ID `43401527163`）完整 success，JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传及 Actions 清理均通过；Artifact ID `14025750621`，12,472,155 bytes。
- [ ] 未执行模拟器 / 真机仪器测试；分式不等式、原式定义域与分母零点尚未接入。
- [ ] 下一步 C6.2 分式不等式，重点保护分母零点和原式历史定义域限制。


### 27.42 C6.2 分式不等式与原式定义域保护（2026-11-13）

- [x] 新增 `RationalFunctionInequality`、结果模型与 `RationalFunctionInequalitySolver`，支持有理函数与 0 的四种严格 / 非严格比较。
- [x] 复用多项式实根内核，合并分子零点、当前分母零点和所有 `domainRestrictions` 的实根边界；相关多项式次数上限为 3。
- [x] 符号区间按分子符号乘以分母符号确定，避免显式构造 `N·D` 导致多项式次数翻倍或中间溢出；无界区间通过首项符号和次数奇偶确定。
- [x] 非严格比较允许取分子零点，但只在原式定义域有效时加入；分母零点、原式历史限制零点与除法操作引入的分子限制零点始终排除。
- [x] 约分空洞保持为开点；`x/x` 不会被错误判为全体实数，`(x²−1)/(x−1)` 保留 `x=1` 的未定义点。
- [x] 零分子严格比较无解，非严格比较返回定义域内的区间集合；常数分式按符号分类。
- [x] 对相邻边界无法在 Double 中可靠区分的情况返回 `NUMERIC_PRECISION_LOSS`；对分子、分母或任一原式限制的四次及以上结构返回 `UnsupportedDegree`。
- [x] 新增 18 个 JUnit 测试方法，覆盖分式正负区间、分子 / 分母根、可去间断点、重根、继承定义域、除法限制、零分子、常数分式、无理公共零点和数值精度边界。
- [x] 实现提交 `fd9c6ef1bf9266dd5ac7be3e86a18a1825cde60e`；Build #405（Run ID `43403410088`）完整 success，JUnit、androidTest 源码编译、Debug APK 组装、Artifact 上传与 Actions 清理均通过；Artifact ID `14026112216`，12,478,669 bytes。
- [ ] 未执行模拟器 / 真机仪器测试。
- [ ] 下一阶段 C7 函数工具内核：坐标点数据模型、输入校验、一次 / 二次拟合，再扩展三次 / 反比例拟合与采样。


### 27.43 C7.1 坐标点模型与输入校验（2026-11-13）

- [x] 新增纯 Kotlin `CoordinatePoint`、`CoordinatePointSet` 与校验结果模型；点集保存独立快照，并通过只读列表阻止调用方在校验后修改输入。
- [x] 新增 `CoordinatePointSetValidator`：检查必需点数配置、空输入、x/y 非有限数、重复横坐标和最少点数，返回带点索引及坐标轴信息的类型化结果。
- [x] 重复 x 按 Double 精确相等判断；`-0.0` 与 `0.0` 统一为同一横坐标，近似但不同的有限值不强行合并，病态拟合判断留给后续拟合器。
- [x] 新增 11 个 JUnit 测试方法，覆盖有效点集、输入快照、空输入、非有限 x/y、重复 x、正负零、相邻可表示 x、点数不足、非法最少点数与错误优先级。
- [x] 功能提交 `bb5fb6d822d18d0c19f51e4f67eb1254daea7761`；Android Build #406（Run ID `43404794011`）完整 success：资源检查、全部 JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过。Artifact ID `14027365163`，12,485,602 bytes。
- [ ] 未执行模拟器 / 真机仪器测试；本步不包含拟合算法、函数表达式解析、定义域断点或图像采样。
- [x] C7.2 一次函数拟合已在后续记录完成；当前下一步为 C7.3 二次函数拟合。


### 27.44 C7.2 一次函数拟合（2026-11-13）

- [x] 新增 `LinearFunction` 与 `LinearFunctionFitResult`，以斜率 / 截距表示 `y=mx+b`；无效点集通过既有 Validator 返回类型化输入原因。
- [x] 两个横坐标不同的点确定唯一插值直线；三个及以上点使用普通最小二乘，最小化纵向残差平方和。输入顺序不影响解。
- [x] x、y 分别按最大绝对值归一化，并使用 Welford 在线中心矩算法累积方差与协方差，减少大数求和和均值偏移造成的溢出 / 消去。
- [x] 最终尺度恢复使用 `BigDecimal` 高精度中间转换，区分真正超出 Double 上限的 `NON_FINITE_RESULT` 与下溢 / 归一化碰撞导致的 `NUMERIC_PRECISION_LOSS`；不会返回非有限拟合系数。
- [x] `fit(List)` 与 `fit(CoordinatePointSet)` 使用同一校验语义；空列表和空点集都返回 `EmptyInput`。
- [x] 新增 13 个 JUnit 测试方法，覆盖两点插值、多点共线、带噪最小二乘、水平 / 零函数、负坐标和乱序输入、空输入、点数不足、重复 x、极端对称数、相邻可表示数、归一化碰撞及斜率溢出。
- [x] 初始实现提交 `3a2e754769280037f3a7434924e1af0a74e65faf` 的首次 Build #407 有 1 项错误分类断言失败（286 个总测试中 1 项失败）；读取日志后，以 `3b9f2a490a558a43e80b8bc9453d2efbd9d7b5f1` 区分溢出与下溢，Build #408 完整通过。
- [x] 随后修正空 `CoordinatePointSet` 与空列表返回结果不一致的问题，最终功能提交 `c167cb304f90e85c2caedfeaf271af48cf3146ef`；Build #409（Run ID `43410045585`）完整 success，资源检查、全部 JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过。Artifact ID `14029077035`，12,491,526 bytes。
- [ ] 未执行模拟器 / 真机仪器测试；本步不包含二次 / 三次 / 反比例拟合、函数表达式解析、定义域断点或图像采样。

**下一步：** C7.4 三次函数拟合。C7.1–C7.3 已完成，后续处理四点插值、多点最小二乘、矩阵秩与精度边界。


### 27.45 C7.3 二次函数拟合（2026-11-13）

- [x] 新增 `QuadraticFunction`、`QuadraticFunctionFitResult` 与 `QuadraticFunctionFitter`，输出 `y=ax²+bx+c` 三个有限系数；复用 C7.1 点集校验，要求至少三个有限点且 x 唯一。
- [x] 三点使用唯一二次插值；四点及以上采用普通最小二乘，最小化纵向残差平方和；`List` 与 `CoordinatePointSet` 两种入口执行相同校验。
- [x] 设计矩阵按归一化横坐标构造 `[t²,t,1]`，采用列主元 Householder QR 求解，不使用正规方程；通过相对秩阈值拒绝数值秩不足的点集。
- [x] 坐标中心化 / 缩放并使用 `BigDecimal` DECIMAL128 恢复原始系数；对矩阵秩不足、归一化碰撞 / 下溢返回 `NUMERIC_PRECISION_LOSS`，系数真正超出 Double 范围返回 `NON_FINITE_RESULT`。
- [x] 极端横坐标尺度下对精确共线数据使用 BigDecimal 交叉乘积确认，直线按二次项为零返回；截距通过先构造精确分子再除法，修复极大坐标下中间舍入引发的灾难性消去。
- [x] 新增 15 个 JUnit 测试方法，覆盖三点插值、多点共线、精确二次数据、带噪最小二乘、负 / 乱序坐标、零函数 / 常值函数、输入边界、精度损失和系数溢出。
- [x] 初始实现提交 `dde20ea19fc5beb3fba6ca44846d856584bf6032` 对应 Build #410 success；加入极端尺度共线性分支后的 Build #411 因极端对称点的截距恢复精度失败。读取日志后修正精确截距分子，最终提交 `e073b6ef750df72b2831e0cdd24459c17476129f`；Build #412（Run ID `43411748705`）完整 success，资源检查、全部 JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过。Artifact ID `14029827428`，12,503,320 bytes。
- [ ] 未执行模拟器 / 真机仪器测试；androidTest 仅源码编译。本步未接函数表达式解析、定义域断点、图像采样或 Compose 页面。

**下一步：** C7.5 反比例函数拟合。C7.1–C7.4 已通过完整 CI；后续处理 x=0 定义域排除、参数拟合、多点拟合语义与数值精度。


### 27.46 C7.4 三次函数拟合（2026-11-13）

- [x] 新增 `CubicFunction`、`CubicFunctionFitResult` 与 `CubicFunctionFitter`，输出 `y=ax³+bx²+cx+d` 四个有限系数；复用 C7.1 点集校验，要求至少四个有限点且 x 唯一。
- [x] 四个不同横坐标的点确定唯一三次插值；五个及以上点采用普通最小二乘，最小化纵向残差平方和。
- [x] 设计矩阵使用中心化 / 缩放后的 `[t³,t²,t,1]`，通过列主元 Householder QR 分解求解，不构造正规方程；列主元、相对秩阈值及回代检查用于识别数值秩不足。
- [x] 原始系数使用 BigDecimal 中间值恢复；对非零系数下溢与归一化碰撞返回 `NUMERIC_PRECISION_LOSS`，真正超出 Double 范围返回 `NON_FINITE_RESULT`。
- [x] 极端 x 量级下尝试精确二次或更低次结构分支，通过公共分母 Lagrange 系数与精确交叉乘积验证所有数据点；验证通过时保留真实低次模型而不制造虚假三次项，不通过则继续 QR 拟合。
- [x] 新增 17 个 JUnit 方法，覆盖四点插值、五点精确三次、带噪 OLS、线性 / 二次退化数据、零 / 常值函数、负 / 乱序坐标、空输入、点数不足、重复 x、非有限 y、横坐标下溢、系数溢出 / 下溢及极端对称坐标。
- [x] 初始代码提交 `c5167a45cdb76878e1faac5091e5a85f0a3f2f8b` 对应 Build #413 有 1 项测试断言失败：输入为非有限 y，却错误期待 x 轴错误；算法返回 y 轴错误是正确行为。修正测试期望后最终提交 `7059dae72c741158dc071c89d54100f75fc19537`；Build #414（Run ID `43413083875`）完整 success，资源检查、全部 JUnit、androidTest 源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过。Artifact ID `14030506504`，12,510,326 bytes。
- [ ] 未执行模拟器 / 真机仪器测试；androidTest 仅源码编译。本步未接表达式解析、函数定义域断点、图像采样或 Compose 页面。

**下一步：** C7.5 反比例函数拟合，继续使用纯 Kotlin domain，不接函数图像 UI。


### 27.47 C7.5 反比例函数拟合（2026-11-13）

C7.5 新增纯 Kotlin `InverseProportionFunction`、`InverseProportionFunctionFitResult`、`InverseProportionFitInputError` 与 `InverseProportionFunctionFitter`，拟合目标为 `y=k/x`。至少一个有效点即可拟合；单点唯一确定 `k=x·y`，两点及以上按普通最小二乘最小化纵向残差平方和 `Σ(yi-k/xi)²`。

- 点集输入继续复用 C7.1 `CoordinatePointSetValidator`，要求有限坐标与唯一 x；拟合器对 List 和 CoordinatePointSet 两种入口使用一致校验，并在点集入口再次检查快照。
- x=0（含正负零）不属于反比例函数定义域。拟合器返回独立的 `InverseProportionFitInputError.ZeroXCoordinate`，携带点索引与原始 x；没有把这一规则塞进通用 Validator，避免误伤多项式函数拟合。
- 数值算法取最小绝对横坐标作为 x 尺度，最大绝对纵坐标作为 y 尺度，构造有界归一化变量 `u=sX/x`、`v=y/sY`，求解 `q=Σ(uv)/Σ(u²)`，再用 `k=sX·sY·q` 恢复系数。分子与分母使用补偿求和，最终尺度恢复使用 BigDecimal DECIMAL128。
- 若倒数横坐标或纵坐标归一化发生下溢 / 碰撞，返回 `NUMERIC_PRECISION_LOSS`；系数真实超出 Double 范围返回 `NON_FINITE_RESULT`，非零系数下溢则返回精度损失。所有 y 均为零时直接得到合法的零反比例函数，但 x=0 仍先被拒绝。
- 新增 16 个 JUnit 测试方法，覆盖单点定参、负 x 符号、精确多点、带噪 OLS、乱序与正负坐标、零函数、x=0（List / PointSet）、空输入、重复 x、非有限坐标、倒数 / y 归一化下溢、系数溢出 / 下溢和极端但可表示的系数。
- 功能提交：`361e7f3389931acddb775db830dc085b10a85000`。
Android Build #415（Run ID `43414570879`）完整 success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传及 Actions 清理全部通过。Artifact `zhixingapp-debug`，ID `14031401965`，12,517,040 bytes；Artifact ZIP SHA-256：`81c1712ed567052f0e4fdcbd6145c6ce3a52af7e47e200554e10dbe479102055`。没有执行模拟器 / 真机仪器测试；Instrumentation 测试仅完成 Kotlin 源码编译，不能作为设备端运行验收。

**C7.6 已通过 Build #416。下一步：** C8 单位换算纯 Kotlin 内核。函数图表 UI 仍未开始。


### 27.48 C7.6 统一函数定义域与安全图像采样（2026-11-13）

C7.6 新增 `FittedFunction` 统一封装一次、二次、三次与反比例函数；`FunctionDomainResolver` 将多项式函数定义域统一为全体实数，将反比例函数定义域统一为 `R\\{0}`。`FunctionEvaluator` 先检查 x 是否有限、再检查定义域与系数有限性，使用 Horner 形式计算多项式，并对非有限结果返回类型化错误。

### 安全采样规则

- `FunctionGraphSampler` 接收有限且严格递增的 x 范围，以及每个连续定义域分段的采样数；每段至少 2 点、最多 512 点，最多 16 段、总样本数不超过 2,048。
- 先用统一定义域中的排除点切分 x 范围。每个连续区间输出独立的 `FunctionGraphSegment` 折线点列；反比例函数跨过 x=0 时返回左右两段，绝不将两侧点列合并成一条折线。
- 对开区间端点按该段采样间距留出安全内缩量，不在 x=0 或紧贴渐近线的位置求值；只在 x=0 为范围端点时生成对应的一侧分段。
- x 范围插值采用加权插值 `start·(1-t)+end·t`，不直接计算可能溢出的 `end-start`，因此可安全处理 `[-Double.MAX_VALUE, Double.MAX_VALUE]` 一类有限端点。
- 如果连续采样横坐标因 Double 精度而重复 / 倒退、开端点内缩后无可表示区间，或函数采样值溢出，则返回 `NUMERIC_PRECISION_LOSS` / `NON_FINITE_FUNCTION_VALUE` 等类型化结果；不输出部分错误曲线。
- 本步只交付 domain 模型、求值与采样数据，不接函数表达式解析、Compose 图表、坐标轴绘制、自适应像素采样或交互缩放。

### JUnit 与 CI

- 新增 18 个 JUnit 测试方法：8 个求值测试覆盖四种函数类型、非有限 x、反比例零点、非有限系数与多项式溢出；10 个采样测试覆盖连续多项式分段、x=0 左右断开、范围端点位于零、远离零时不误分段、非法范围、采样数边界、相邻 Double 精度不足、极端异号边界与函数值溢出。
- 功能提交：`668a7e55a2400e46f6bb217d1e389b239bdcec97`。
- Android Build #416（Run ID `43415412088`）完整 success：资源完整性检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传及 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 52s`。
- Artifact：`zhixingapp-debug`，ID `14031292575`，12,528,905 bytes；Artifact ZIP SHA-256：`7a73dcb404d7c9511c842c2b1c0159602432fb9698c354e48f0be06c0d9054f7`。
- 未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。函数图表 UI 与真机视觉 / 性能仍未验收。

**下一步：** C8 单位换算纯 Kotlin 内核，按单位定义、维度校验、换算规则和温度偏移等小步推进。C7 的拟合、定义域和安全采样数据层已完成，但仍未接入 Compose 图表界面。


### 27.49 C8.1 单位类别与物理量纲目录（2026-11-13）

C8.1 新增纯 Kotlin `UnitDimension`、`UnitCategory`、`UnitConversionKind` 与 `UnitCategoryCatalog`，先建立旧版单位换算九类的稳定目录和量纲基础，尚未加入具体单位条目或数值换算。

- 维度采用整数指数向量：长度 L、质量 M、时间 T、热力学温度 Θ、数据量 D、角度 A。面积为 L²、体积为 L³、速度为 L·T⁻¹；数据存储和角度各自有显式维度，避免误与普通数值 / 物理量混用。
- 保留旧版类别 ID 和顺序：`length`、`area`、`volume`、`mass`、`temp`、`speed`、`time`、`data`、`angle`。目录通过不可修改列表暴露，按 ID 查找不做大小写模糊匹配。
- 类别是否可换算由量纲向量相等判断，不以显示名称或类别相近作为依据。
- 温度类别标记为 `AFFINE`，其他八类为 `LINEAR`。这一步只标明转换语义，不实现摄氏 / 华氏 / 开尔文的偏移公式；温度转换将在独立小步实现，不能套用普通比例系数。
- 新增 7 个 JUnit 测试方法，覆盖九类目录顺序、唯一 ID / 名称、旧 ID 查找、基础 / 派生量纲、兼容性、温度仿射标记和目录不可变性。
- 功能提交：`736f308436868e929625ba5428aa6a97abe36dd7`。
- Android Build #417（Run ID `43416648667`）完整 success：资源完整性检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传及 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 1m 6s`。
- Artifact：`zhixingapp-debug`，ID `14031703877`，12,536,513 bytes；Artifact ZIP SHA-256：`d0d92b5af941ef73601fc1d9e98d5474ecf0c91af82e8e1a1e2c290c451c5c1c`。
- 未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。本步没有实现实际单位条目、转换数值、输入解析或 Compose 界面。

**下一步：** C8.2 建立长度、面积、体积的具体单位定义与线性基准换算；本步不实现温度偏移或 Compose 单位页面。


### 27.50 C8.2 长度、面积与体积单位定义（2026-11-13）

新增纯 Kotlin `UnitDefinition`、`UnitCategoryUnitSet` 和 `UnitDefinitionCatalog`，从旧版 `calculator.html` 迁移长度、面积、体积三类共 36 个具体单位。换算基准分别为 m、m²、m³，精确保留旧版比例因子、单位 ID、标签、排列顺序和默认选中单位（m / ft、m2 / ft2、l / ml）。

数据模型拒绝空 ID / 标签、非有限或非正比例因子、重复单位 ID、缺失默认单位，并限制比例模型只接受 `LINEAR` 类别，避免温度 `AFFINE` 误用。类别及单位清单均暴露不可修改快照，查找同时限定类别与单位 ID。

新增 9 个 JUnit 测试方法，覆盖完整 36 项因子表、历史标识与默认值、量纲匹配、类别隔离、列表不可变性和非法定义拒绝。代码提交 `2740027e666dc7725f1850d822d42c13a580adcf`。
- Android Build #418（Run ID `43424886269`）完整 CI success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 42s`。Artifact `zhixingapp-debug`，ID `14036736039`，12,540,001 bytes；Artifact ZIP SHA-256：`66f9803e6636dd1c88c2b5d18e4b32567522996f25969705270bffc70f5e08c3`。未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。

**下一步：** C8.3 质量、速度、时间单位目录；本步尚未实现通用转换计算、输入解析或 Compose 页面。


### 27.51 C8.3 质量、速度与时间单位定义（2026-11-13）

C8.3 在现有 `UnitDefinitionCatalog` 中增加质量、速度、时间三类，共 24 个条目，和 C8.2 已完成的长度、面积、体积合计 60 个线性单位。基准单位分别为 kg、m/s、s。保留旧版单位 ID、显示标签、顺序、比例因子和默认单位对（kg / lb、kmh / ms、h / min）。

速度 `kmh` 继续使用旧因子 `1 / 3.6`；时间 `yr` 按旧版 365 天定义为 31,536,000 秒。新增回归测试覆盖完整 60 项比例因子、六类目录顺序和默认单位、物理量纲、类别隔离及目录不变性。C8.2 既有 36 项没有更改。本步代码提交：`0d268424c554178ce60a5905d85ea7dca8dace95`。Android Build #419（Run ID `43426203477`）完整 CI success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 1m 7s`。Artifact `zhixingapp-debug`，ID `14036937441`，12,540,674 bytes；Artifact ZIP SHA-256：`df7ed7b6c3e427e3a16c7ce182c545f594c03800d4011ac692874e5f48837140`。未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。

**下一步：** C8.4 数据存储与角度单位定义；温度仿射转换与通用换算入口另行实现。


### 27.52 C8.4 数据存储与角度单位定义（2026-11-13）

C8.4 为 `UnitDefinitionCatalog` 增加数据存储 10 项和角度 6 项，当前包含八类、76 个线性单位。数据存储以字节 B 为基准，明确区分 KiB / MiB / GiB / TiB 的 1024 进位与 kB / MB / GB / TB 的 1000 进位，并保留 bit → 0.125 B。角度以弧度 rad 为基准，六项比例因子使用 `Math.PI` 公式。保留旧版 ID、显示标签、顺序及默认单位（mb / gb、deg / rad）；数据量 D 和角度 A 继续由显式伪量纲隔离。

测试现已逐项精确核对 76 个单位因子，覆盖八类顺序、默认值、标签、显式量纲、分类查找、速度 / 时间共有 ID `ms` 的独立解析，以及目录不可变性。C8.2 / C8.3 的旧条目未改动。代码提交 `32a288cccfc76211b0ef25a4ee803e98c87cbff6`，测试修正提交 `d93e9278fe5e99ae9a4619daf22bcfb20ef8feca`。Android Build #420（Run ID `43427982663`）完整 CI success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 1m 6s`。Artifact `zhixingapp-debug`，ID `14037119258`，12,541,011 bytes；Artifact ZIP SHA-256：`09e44420aef6fb08acfeed47b6a173f20da0c979c49f52147056157019cb71e5`。未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。

**下一步：** C8.5 温度仿射转换，之后再实现通用线性 / 仿射转换器与类型化输入 / 数值错误。


### 27.53 C8.5 温度仿射定义与转换（2026-11-13）

新增 `AffineUnitDefinition`、`AffineUnitCategoryUnitSet`、`TemperatureUnitCatalog` 和 `TemperatureConverter`，独立处理温度 AFFINE 语义，不修改 76 项 LINEAR 目录。统一以开尔文为基准：摄氏度 `C + 273.15`，华氏度 `(F + 459.67) × 5/9`，开尔文不变；偏移先于比例应用，使各输入单位的绝对零度都映射为精确 0 K。

转换 API 返回类型化结果，分别报告非有限输入、低于绝对零度、结果非有限 / 溢出、未知源单位和未知目标单位。严格检查 Kelvin 下界，不以 epsilon 放行低于绝对零度的输入；允许绝对零度以上的负温度。

新增 9 个 JUnit 测试方法覆盖正常互转、负温度、三种绝对零度、`Math.nextDown` 紧邻下界值、NaN / ±Infinity、输出溢出、未知单位和仿射目录元数据 / 不可变性。功能提交 `935d59982f5cce2992f2903d536459e6a22d5444`；测试整理提交 `a399d2d2c55dd330f35a3afdde0d7d1b5509a2fd`。Android Build #421（Run ID `43429397488`）完整 CI success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 48s`。Artifact `zhixingapp-debug`，ID `14038965866`，12,546,736 bytes；Artifact ZIP SHA-256：`9104003229349ce033b11224b55a08637935b07836b60f46f4ce6be1ef9116c3`。未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。

**下一步：** C8.6 统一线性 / 仿射换算入口，并集中处理量纲、类别匹配、未知单位和数值精度边界。


### 27.54 C8.6 统一线性 / 仿射单位转换入口（2026-11-13）

新增 `UnitConverter`，为全部八类 LINEAR 单位及 TEMPERATURE AFFINE 单位提供统一调用和统一类型化结果。调用分别接收源类别 / 单位和目标类别 / 单位 ID，校验类别存在、物理量纲兼容、同类别约束和单位归属后才执行转换；长度转质量、数据转角度、速度转时间等跨类别请求会被拒绝。

线性分支使用 `BigDecimal` + `DECIMAL128`，不因中间 Double 基准值可能溢出而错误拒绝最终可表示的值；检测最终 Double 溢出及非零结果下溢为零。温度分支委托 C8.5 仿射转换器，并把绝对零度、非有限中间值、非有限结果和未知单位错误映射为统一错误类型。

新增 9 个 JUnit 测试方法，覆盖九类换算、全部跨类别配对拒绝、量纲不兼容、同名单位 ID 隔离、未知类别 / 单位、非有限输入、溢出 / 下溢、中间值保护和温度错误映射。代码提交：`8a04403c895cf88784d1803032e992c7cef32457`；测试修正提交：`21f91b43de60f93b83f5ad15c0c808672c214c5e`、`a580ebc688ec1249e38080b6ac42b07f557f8472`。Android Build #422（Run ID `43431479631`）完整 CI success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 52s`。Artifact `zhixingapp-debug`，ID `14039917793`，12,553,718 bytes。未执行模拟器 / 真机仪器测试。

**下一步：** C8.7 旧版单位换算兼容回归与统一入口验收。


### 27.55 C8.7 旧版单位换算兼容回归与迁移验收（2026-11-13）

已完成旧版端到端转换行为回归，并新增 [UNIT_CONVERSION_API.md](UNIT_CONVERSION_API.md) 作为后续 Compose 页面和输入适配层的稳定契约。

- `LegacyUnitConversionCompatibilityTest` 使用旧版 `calculator.html` 公式作对照，逐一遍历 76 个线性单位形成的 780 组同类别有序配对，每对 5 个输入，共 3,900 次转换比对；温度 3 × 3 × 5，共 45 次比对。
- 核验九类目录顺序与所有默认源 / 目标单位选择；验证 `speed/ms` 与 `time/ms` 的类别隔离以及跨类别拒绝。
- 原有的 NaN / Infinity、低于绝对零度、溢出和精度下溢保护保持为明确的类型化失败；这些是相较旧版更严格的安全行为。
- 提交：`428425641266374898159cdf469e0b145c7e34bc`。Android Build #423（Run ID `43435614443`）完整 success：资源检查、全部 JVM 单元测试、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传及 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 46s`。Artifact `zhixingapp-debug`，ID `14042138313`，12,553,719 bytes。
- 没有模拟器 / 真机仪器测试执行记录；androidTest 仅完成 Kotlin 源码编译。本步不涉及 Compose UI 或真机交互验收。

**下一步：** C8.8 原生单位换算页面与输入层接入，统一使用已验收的 `UnitConverter`。


### 27.56 C8.8.1 原生单位换算页面首版（2026-11-13）

单位换算已从 domain 内核接入原生工具页面。工具 Tab 显示已实现的单位换算入口；二级页采用现有 Backdrop / Liquid Glass 表面和页面转场。

- 页面支持九类类别切换、读取每类历史默认单位、空白输入、十进制输入、源 / 目标单位下拉选择、双向交换、即时结果和当前类别全部单位一览。
- 新增 `UnitConversionPresenter` 与 `UnitValueFormatter`，输入文本解析、错误解释、数值格式化和 UI 行数据都留在表现层；主结果和列表结果统一通过 `UnitConverter` 获取。
- 新增 6 个 presenter / formatting JUnit 测试方法，覆盖类别与默认值、空 / 非法输入、线性与温度一览、类型化错误和科学计数法。
- 页面接入提交：`c9c0be200bddf9672798b93dac6a0becc328af55`。Android Build #424（Run ID `43436816915`）完整 success：资源检查、全部 JVM 单元测试、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传和 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 46s`。Artifact `zhixingapp-debug`，ID `14043848306`，12,601,245 bytes。
- 没有执行设备 instrumentation tests；页面还需要目标设备视觉 / 输入法 / 触控和窄屏验收。

**下一步：** C8.8.2 增加复制等高频交互、完善输入与无障碍细节，并进行目标真机验收。


### 27.57 恢复高中题型 / 输入 / 函数图像规划文档（2026-11-13）

恢复 `docs/CALCULATOR_INPUT_SOLVER_GRAPH_PLAN.md`。原文件由提交 `7d4638a22a2fd3b9ff7860be4dc8e835a4b0985d` 新增，后在功能提交 `0aafab9f00b7b7b0d4e2e593dc5d67d72b69c4ff` 中被误删。恢复正文来自原始 Git blob `f66eec4b01d255964b7740ac680e345ad4990727`；保留题型范围、QuestionDocument / Block / Revision、专用输入、函数图像采样、AI 复杂度分流和验收清单。

为避免旧规划误导当前开发，在文档顶部明确标注创建日期和状态权威来源；§7 的 C4.2.2 当前进度句改为引用最新核心迁移计划；§7.1 不再指定过期的 C4.2.2 / C4.2.3 为当前优先级；§10 改为 2026-11-08 创建时历史快照。README 的本地链接因此恢复有效。

本步仅恢复和标注规划文件，不改变任何功能代码或阶段完成结论。


### 27.58 C8.8.2 单位换算交互、输入边界与无障碍完善（2026-11-14）

本步按用户要求跳过 iQOO Neo5 活力版真机验收，只完成代码、JVM 测试、Android 编译与 APK 构建。真机检查清单保留未勾选，不作为本步 CI 的替代结论。

- 新增完整等式复制：复制文本形如 `1.250 m = 4.101049869 ft`，通过 Android ClipboardManager 写入系统剪贴板；复制按钮使用“已复制”状态反馈，输入 / 类别 / 单位变化后状态自动失效。
- 输入交互：增加“清除”操作、IME Done 收起键盘；输入长度限制为 64 字符，粘贴超长内容时进入专门错误状态。
- 输入解析区分空白、未完成、非法、超长和有效数字；`-`、`.`、`1e-` 等输入中间态不显示错误，支持正负数、小数和科学计数法；非有限转换仍由 `UnitConverter` 类型化拒绝。
- 窄屏适配：可用宽度小于 340dp 时，源 / 目标单位选择由横排改为纵排，并显示“交换单位”文字按钮；较宽布局保留横向排列。
- 无障碍：单位选择按钮提供当前单位描述，交换与复制提供明确动作名称和复制状态；同类单位列表行合并朗读，并标记当前目标单位。
- `UnitConversionPresenterTest` 共 10 个测试方法，覆盖类别 / 默认值、空 / 非法 / 中间 / 超长输入、正负数 / 小数 / 科学计数法、线性 / 温度结果、类型化错误、复制文本和格式化边界。
- 功能提交：`64f2ce0dd7f09f985e98d8af8e5d4b8075072d40`。Build #427 首次编译曾因 Compose semantics 的两个错误引用而失败；修复提交：`2b62e75f6f20224b50550d0f655d3d511ccc2cd3`。
- Android Build #428（Run ID `43545850658`）完整 success：Markdown 链接检查、资源检查、全部 JVM 单元测试、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传和 Actions 清理通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 50s`。Artifact `zhixingapp-debug`，ID `14093618375`，12,612,565 bytes。
- 未执行模拟器 / 真机 instrumentation tests；用户明确暂缓真机验收。本步不得标记为设备交互验收通过。

**下一步：** 回到高中方程题型扩展，先为 C5.8 选择一个边界明确、可单独验证的新增题型切片；继续保持 domain 纯 Kotlin、先测试后接 UI。


### 27.59 C5.8 二次内部式绝对值方程（2026-11-14）

在 C5.5 的线性内部式绝对值方程基础上，扩展到常数、一次或二次内部多项式；本步仍限定右侧为有限常数，不增加变量右侧、多重绝对值或嵌套绝对值。

- [x] 将 AbsoluteValueEquationSolver 的内部式最高支持次数从 1 提高到 2；复用现有 PolynomialArithmetic.solveRealRoots，不在绝对值 solver 中复制二次求根算法。
- [x] 对 |P(x)|=d（d>0）求解 P(x)−d=0 与 P(x)+d=0，合并并排序两支实根；d=0 时只求一次，避免重复分支结果；负目标仍直接无解。
- [x] 保留常数 / 一次内部式行为、类型化非有限输入错误，以及三次及以上内部式的 UnsupportedDegree 边界。
- [x] 新增二次内部式测试：|x²−1|=3 的双根、|x²−1|=1 的三解分支合并、|x²−1|=0 的零目标去重；三次内部式仍明确暂缓。AbsoluteValueEquationSolverTest 当前共 9 个测试方法。
- [x] 代码提交：f6c08f91d946c09bae4662dd714b1a810e51c457。
- [x] Android Build #429（Run ID 43550028977，代码 SHA f6c08f91d946c09bae4662dd714b1a810e51c457）完整 success：Markdown 本地链接检查、资源检查、全部 JVM 单元测试、:app:compileDebugAndroidTestKotlin、:app:assembleDebug、Artifact 上传和 Actions 清理全部通过。Debug Artifact 14095509294，12,612,599 bytes。
- [x] Build #429 后续 Post Set up Gradle / JDK / Checkout 收尾全部成功，workflow 最终状态 completed / success。
- [ ] 未执行模拟器 / 真机 instrumentation tests；目标设备验收继续按用户要求暂缓。
- [x] 保持纯 Kotlin domain，不改 Parser、Compose、输入键盘或 UI；不进入 C8.7。

**边界与后续：** C5.8 仍只支持一个绝对值表达式与有限常数目标；变量右侧、多重 / 嵌套绝对值、高于二次的内部式不在本步范围。下一步继续检查方程求解覆盖缺口，另选可独立验证的高中题型切片。


### 27.60 C5.9 二元一次方程组（2026-11-14）

新增纯 Kotlin 二元一次方程组模型与求解器，输入两条形如 ax + by = c 的线性方程。当前只覆盖 2 个未知数、2 条一次方程，不包含三元系统、参数系统或方程组 UI。

- [x] 新增 LinearEquation2D 与 LinearEquationSystem 数据模型；系数和常数项统一按 Double 输入，由 solver 负责有限性校验。
- [x] 新增 LinearSystemSolutionResult，明确区分 AllRealPairs（任意实数对均满足）、NoSolutions、UniqueSolution（x、y 与归一化残差）和 Failure。
- [x] 新增 LinearEquationSystemSolver。使用 BigDecimal 计算行列式、克拉默法则分子和增广矩阵秩关系；不以固定 epsilon 判断行列式是否为零，避免把近奇异但仍线性独立的方程误判为比例方程。
- [x] 唯一解转换回 Double 后检查非有限结果、非零解下溢，并对两条原方程计算最大归一化残差；超出范围或残差不合格时返回既有类型化数值错误。
- [x] 新增 10 个 JUnit 测试方法，覆盖唯一解、负系数、比例方程无穷多解、比例左侧但常数矛盾、零系数行、全零行恒等 / 矛盾、近奇异独立方程、非有限输入、解溢出和非零解下溢。
- [x] 代码提交：16b6840fc8d3ca561d04449c726f47248f2d00b5。
- [x] Android Build #431（Run ID 43550576403，代码 SHA 16b6840fc8d3ca561d04449c726f47248f2d00b5）完整 success：Markdown 本地链接检查、资源检查、全部 JVM 单元测试、:app:compileDebugAndroidTestKotlin、:app:assembleDebug、Artifact 上传和 Actions 清理全部通过；主 Gradle 步骤 BUILD SUCCESSFUL in 1m 2s。Debug Artifact 14096161115，12,617,943 bytes。
- [ ] 未执行模拟器 / 真机 instrumentation tests；目标设备验收仍按用户要求暂缓。
- [x] 保持纯 Kotlin domain，不接 Parser、Compose、专用键盘或方程组 UI。

**边界与后续：** C5.9 仅对两条二元一次方程分类和求解；不代表已支持三元方程组、含参数方程组、非线性系统或通用矩阵求解。下一步继续从高中方程题型目录中选择独立切片，先完善真实题型与解集边界，再接输入 / UI。


### 27.61 C5.10 实数立方根方程（2026-11-14）

新增独立实数立方根方程模型、解集结果与纯 Kotlin solver。当前形式限制为 ∛P(x)=Q(x)，其中 deg(P)≤3、deg(Q)≤1；不把它扩展成多个 / 嵌套根式或其他指数根式系统。

- [x] 新增 CubeRootEquation 与 CubeRootEquationSolutionResult，区分 AllRealNumbers、NoSolutions、FiniteSolutions、UnsupportedStructure 和 Failure。
- [x] 利用实数立方函数在全体实数上的一一对应，将 ∛P(x)=Q(x) 等价为 P(x)=Q(x)³；对 Q 复用 PolynomialArithmetic.multiply 两次，对候选多项式复用现有 1–3 次实根引擎，不复制根算法。
- [x] 有限候选根逐个代回原式：用 Math.cbrt(P(x)) 与 Q(x) 比较归一化残差；负数 radicand / 负数解合法，不引入平方根方程的非负定义域筛选。
- [x] 多项式乘法或求值产生非有限中间结果时保留既有类型化错误；原式回代残差不合格时返回 NUMERIC_PRECISION_LOSS。
- [x] 新增 9 个 JUnit 测试方法，覆盖三次 radicand + 一次右侧双根、负解、三次恒等式、负常数恒等式、常数矛盾、无根、二次 radicand 双根、结构次数限制与立方溢出。
- [x] 代码提交：61186992e4bee37cabad1016002d4ed38b06a587。
- [x] Android Build #433（Run ID 43551030029，代码 SHA 61186992e4bee37cabad1016002d4ed38b06a587）完整 success：Markdown 本地链接检查、资源检查、全部 JVM 单元测试、:app:compileDebugAndroidTestKotlin、:app:assembleDebug、Artifact 上传和 Actions 清理全部通过；主 Gradle 步骤 BUILD SUCCESSFUL in 39s。Debug Artifact 14095917816，12,620,182 bytes。
- [ ] 未执行模拟器 / 真机 instrumentation tests；目标设备验收继续按用户要求暂缓。
- [x] 保持纯 Kotlin domain；不改 Parser、Compose、专用键盘或页面输入。

**边界与后续：** C5.10 仅支持实数立方根、三次以内 radicand 和一次以内右侧多项式；不表示支持任意 n 次根、嵌套根式或高次 / 非线性右侧。下一步进入 C5.11 变量右侧绝对值方程，继续从高中题型规划选择一个独立切片。

 
### 27.62 C5.11 变量右侧线性绝对值方程（2026-11-14）

新增 `|P(x)|=Q(x)` 的独立 solver，限定 deg(P)≤2、deg(Q)≤1。和 C5.5 / C5.8 的常数右侧形式分开建模，不改变已有 `AbsoluteValueEquationSolver` 的原有契约。

- [x] 新增 `AbsoluteValueEquationWithLinearRightSide`，明确表达绝对值内部多项式 P 与线性 / 常数右侧 Q。
- [x] 新增 `AbsoluteValueLinearRightEquationResult`：有限解、无解、全体实数 / 带非负域条件、结构不支持和类型化失败分别表达。
- [x] 新增 `AbsoluteValueLinearRightEquationSolver`：求解两个分支 P−Q=0、P+Q=0，并对有限候选根通过 `PolynomialArithmetic.signAt` 精确检查 Q(x)≥0；对原方程做归一化残差回代。
- [x] 当任一分支为恒等式时，返回 Q(x)≥0 的解域条件；若 Q 是负常数则返回无解，若 Q 是非负常数或零多项式则返回全体实数。两个分支重合的根去重并排序。
- [x] 明确拒绝 deg(P)>2 或 deg(Q)>1；多项式运算溢出、求值失败与符号 / 数值不一致均保留类型化错误。
- [x] 新增 11 个 JUnit 测试方法，覆盖双分支双解、负右侧候选剔除、正 / 负恒等分支的非负域、零恒等、正常数右侧双解、零右侧根去重、负常数无解、结构限制、运算溢出及常数恒等分支右侧正负分类。
- [x] 功能提交：`f0cc86f70a982d8a2d7fd17e0d3ca03b1939cbe1`；最终 solver 修正：`57413373e491ec115c5e0af3ec39f9d3ae5dfd30`；常数恒等边界测试：`133923abe41744b62bb71db56385d43c20966c72`。
- [x] Build #434（Run ID `43551347228`）初始功能实现完整 success；Build #435（Run ID `43551389610`）的一次中间重构因正分支候选变量重复声明而编译失败，已在 `57413373e491ec115c5e0af3ec39f9d3ae5dfd30` 修正；Build #436（Run ID `43551441415`）修正后的 solver 完整 success。
- [x] Build #437（Run ID `43551712552`，最终测试提交 SHA `133923abe41744b62bb71db56385d43c20966c72`）完整 success：Markdown 链接检查、资源检查、全部 JVM 单元测试、:app:compileDebugAndroidTestKotlin、:app:assembleDebug、Artifact 上传和 Actions 清理通过；主 Gradle 步骤 BUILD SUCCESSFUL in 49s。Debug Artifact `14095997284`，12,623,582 bytes。
- [ ] 未执行模拟器 / 真机 instrumentation tests；按用户要求，设备验收继续暂缓。
- [x] 仅新增纯 Kotlin domain 模型 / solver / 单元测试；未改 Parser、Compose、专用键盘或界面。

**边界与后续：** C5.11 仅支持 `|P(x)|=Q(x)` 且 deg(P)≤2、deg(Q)≤1；未支持二次及更高次数右侧、多重绝对值、嵌套绝对值或含参分类。下一步进入 C5.13，继续选择有独立解集语义的高中方程结构。


### 27.63 C5.12 弧度制正弦周期方程（2026-11-14）

新增纯 Kotlin 正弦方程内核，支持 `sin(a*x+b)=c`，所有角度均采用弧度制。当前只处理线性角表达式和常数目标，不包含复合三角函数、多角组合、含参区间综合或反三角函数方程。

- [x] 新增 `SineEquation` 输入模型，明确系数 a、相位 b、目标 c 均为 Double 且角度单位为弧度。
- [x] 新增 `PeriodicSolutionFamily`，用 `x = firstSolution + k * period`（k 为任意整数）表达一个无限周期解族；基准值归一化至 `[0, period)`，周期固定为正数。
- [x] 新增 `SineEquationSolutionResult`，将全体实数、无解、一个或两个周期解族、类型化数值错误分开表示；不把无限解截断成有限根列表。
- [x] 对 a≠0 使用 `α=asin(c)`，分别生成 `a*x+b=α+2kπ` 和 `a*x+b=π−α+2kπ` 两支；当 c=±1 时两支重合，只返回一个解族；周期为 `2π/|a|`，负系数仍得到正周期。
- [x] 对 a=0 直接判断常数等式 `sin(b)=c`，分类为 AllRealNumbers 或 NoSolutions；c 超出 [-1,1] 时直接无解。
- [x] 对非有限输入返回 NON_FINITE_INPUT；周期溢出返回 NON_FINITE_RESULT；非零基准解下溢、相位周期数超出可靠归一化范围或原式回代残差不合格时返回 NUMERIC_PRECISION_LOSS。
- [x] 新增 11 个 JUnit 测试方法，覆盖 `sin(x)=0` 两解族、c=±1 边界合并、`sin(2x+π/6)=1/2`、负系数、目标越界、a=0 恒等 / 矛盾、非有限输入、周期溢出和超大相位精度边界。
- [x] 代码提交：初版 `1d2924b5d28f3013c8dfdc29b3b640c526c11635`；解族归一化结果改为调用内局部类型、移除共享可变错误状态的修正提交 `b049e2ee6880524d5698c1d19df3966925821a0a`。
- [x] Build #438（Run ID `43552419920`，初版 SHA `1d2924b5d28f3013c8dfdc29b3b640c526c11635`）完整 success；Build #439（Run ID `43552472439`，最终 SHA `b049e2ee6880524d5698c1d19df3966925821a0a`）完整 success。最终主 Gradle 步骤 `BUILD SUCCESSFUL in 46s`，另有清理构建成功；Markdown 链接检查、资源检查、全部 JVM 测试、Android 测试源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过。
- [x] 最新 Debug Artifact：`14096494162`，12,629,769 bytes。
- [ ] 未执行模拟器 / 真机 instrumentation tests；按用户要求，真机验收继续暂缓。
- [x] 保持纯 Kotlin domain；未改 Parser、Compose、专用键盘或计算器 UI。

**边界与后续：** C5.12 仅支持 `sin(ax+b)=c`，以弧度为单位，周期解族适用于全体实数 x；不代表已经支持 cos / tan 方程、复合三角式、多个三角函数混合方程或区间内解筛选。下一步进入 C5.13 余弦周期方程。


### 27.64 C5.13 弧度制余弦周期方程（2026-11-14）

新增纯 Kotlin 余弦方程内核，支持 `cos(a*x+b)=c`，所有角度均采用弧度制；周期解族复用 C5.12 的 `PeriodicSolutionFamily`。

- [x] 新增 `CosineEquation` 与 `CosineEquationSolutionResult`，输入系数、相位、目标均使用 Double。
- [x] 对 a≠0 使用 `α=acos(c)`，生成 `a*x+b=α+2kπ` 与 `a*x+b=−α+2kπ` 两个分支；周期为 `2π/|a|`，基准值归一化到 `[0, period)`。
- [x] 当 c=±1 时两个角度分支重合，只返回一个周期解族；负系数仍返回正周期。
- [x] 对 a=0 直接分类 `cos(b)=c`，返回 AllRealNumbers 或 NoSolutions；目标超出 [-1,1] 则无解。
- [x] 与正弦 solver 一样检查非有限输入、周期溢出、相位归一化精度和候选解回代残差，复用既有 PolynomialErrorCode。
- [x] 新增 11 个 JUnit 测试方法，覆盖 `cos(x)=0` 两解族、c=±1 单解族、`cos(2x+π/3)=1/2`、负系数、目标越界、a=0 恒等 / 矛盾、非有限输入、周期溢出和超大相位精度边界。
- [x] 代码提交：`5a930bd1f58f465cbbaf8392c1877338496fa669`。
- [x] Build #440（Run ID `43552940320`，代码 SHA `5a930bd1f58f465cbbaf8392c1877338496fa669`）完整 success：Markdown 链接检查、资源检查、全部 JVM 单元测试、Android 测试源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 48s`，清理构建 `BUILD SUCCESSFUL in 5s`。
- [x] Debug Artifact：`14097190828`，12,633,478 bytes。
- [ ] 未执行模拟器 / 真机 instrumentation tests；按用户要求，真机验收继续暂缓。
- [x] 保持纯 Kotlin domain；未改 Parser、Compose、专用键盘或计算器 UI。

**边界与后续：** C5.13 仅支持 `cos(ax+b)=c`，全体实数 x 上的周期解族；不代表已经支持正切方程、复合三角式、混合三角方程、区间筛选或参数分类。下一步进入 C5.14，继续选择边界明确的高中方程独立切片。
