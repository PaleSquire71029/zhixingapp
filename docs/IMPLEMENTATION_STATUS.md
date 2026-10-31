# 知行 Android App 实施状态

> 最后更新：2026-11-01  
> 当前仓库：PaleSquire71029/zhixingapp  
> 当前分支：main  
> 项目接手指南：[docs/PROJECT_STATUS_AND_GUIDE.md](./PROJECT_STATUS_AND_GUIDE.md)  
> 总规划：[docs/ANDROID_APP_MASTER_PLAN.md](./ANDROID_APP_MASTER_PLAN.md)

本文件是项目状态账本。每次开发结束必须更新，不能只在聊天中报告进度。详细操作步骤、APK 获取和排错方法见 [项目现状与操作指南](./PROJECT_STATUS_AND_GUIDE.md)。

---

## 1. 当前项目状态

**截至 2026-10-31：首版 APK 的图标截断问题已在 PR #3 修复；0.1.1 已通过 Android Build #32 并归档。用户真机安装与启动仍待验收。设计系统的颜色、间距、形状与文字 Tokens 已集中到 ZhixingTheme 统一入口；Glass Lab 已加入 GraphicsLayer 局部背景快照、默认关闭的前台动态采样（每秒最多一次）和 API 33+ AGSL 单通道边缘折射实验；低版本使用快照/模糊回退。折射只作用于实验样本，不含 RGB 色散或窗口级 backdrop。编译与真机观感仍待验收。本轮按要求未触发 CI、未创建新 APK。计算器、AI 等业务功能仍未迁移。**

| 项目 | 状态 | 说明 |
|---|---|---|
| 新建 App 仓库 | 已完成 | zhixingapp / main |
| 旧项目审计 | 已完成（2026-10-31） | 检查 calculator、zhixing_ai、zhixingapp |
| 旧 AI 交接文档 | 已阅读 | 项目总览、遗漏需求补充 |
| 技术路线 | 已确定 | Android 原生 Kotlin + Jetpack Compose |
| Android 工程配置 | 已提交 | AGP 9.1.1、Gradle 9.3.1、JDK 17 |
| 最小原生首页 | 已提交；修复版待真机验收 | 四个主入口仍为占位内容；首页与桌面图标引用重新编码并经 CI 验证的知行透明资源 |
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
| 液态玻璃光学渲染 | 未开始 | 后续先单独验证背景采样与模糊，再分步加入折射等效果 |
| 计算迁移 | 未开始 | 尚无旧算法迁移 |
| AI 接入 | 未开始 | 尚无 Worker 客户端 |
| 账号与云历史 | 未开始 | 尚无原生认证与历史层 |
| Debug APK | 0.1.1 已归档 | `artifacts/zhixingapp-preview.apk`，11,490,765 bytes；后续主分支成功构建会自动更新 |
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
