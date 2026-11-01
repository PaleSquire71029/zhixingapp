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
- [x] 记录 CI 环境限制：当前 Runner SDK 仓库无法找到 \`platforms;android-37\`；API 36 安装成功后，AAR Metadata 检查因 minCompileSdk=37 失败。
- [x] 已找到 Backdrop 1.0.6 作为更适合当前工程的候选：上游 Android 项目使用 AndroidX Compose 1.10.3 和 compileSdk 36。
- [x] Backdrop 1.0.6 的依赖解析、Kotlin 编译、单元测试和 Debug 构建已通过 PR #4 的 CI Run #39（2026-10-31）：https://github.com/PaleSquire71029/zhixingapp/actions/runs/41226739564。
- [ ] 将已验证的 Backdrop 1.0.6 与 Kotlin Compose 插件 2.4.21 基线正式合并到 main（PR #4）。
- [ ] 未修改 MainActivity/生产导航；未向 main 提交 APK。

### 实时 Backdrop 阶段进度（2026-11-01）

- [x] 修正 Android Build 工作流：日常 push/PR 仍执行测试和构建，但只有手动触发 \`workflow_dispatch\` 才会把新 APK 提交到 \`artifacts/zhixingapp-preview.apk\`。
- [x] 已确认 PR #4 合并后的既有 push 工作流 Run #40 曾自动更新仓库预览 APK（提交 \`57f63a382207e3d34284f48e214829c0b9cbca6a\`）；之后的工作流已更改为手动更新，避免常规源码提交重写 APK。
- [x] 已在 \`feature/live-backdrop-host\` 分支将 GlassLab 由每秒一次 Bitmap 快照改为 Backdrop 1.0.6 的共享实时背景层。
- [x] 已移除 GlassLab 的 \`toImageBitmap()\`、1 秒定时采样循环和旧位图绑定 Shader 路径；背景与玻璃面板改为同一 Compose 绘制链路。
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
