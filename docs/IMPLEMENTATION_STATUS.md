# 知行 Android App 实施状态

> 最后更新：2026-10-31  
> 当前仓库：PaleSquire71029/zhixingapp  
> 当前分支：main  
> 项目接手指南：[docs/PROJECT_STATUS_AND_GUIDE.md](./PROJECT_STATUS_AND_GUIDE.md)  
> 总规划：[docs/ANDROID_APP_MASTER_PLAN.md](./ANDROID_APP_MASTER_PLAN.md)

本文件是项目状态账本。每次开发结束必须更新，不能只在聊天中报告进度。详细操作步骤、APK 获取和排错方法见 [项目现状与操作指南](./PROJECT_STATUS_AND_GUIDE.md)。

---

## 1. 当前项目状态

**截至 2026-10-31：首版 APK 的图标截断问题已在 PR #3 修复；0.1.1 已通过 Android Build #32 并归档。用户真机安装与启动仍待验收。设计系统的颜色 Tokens 与间距 Tokens 已完成代码抽取，原有配色、间距数值均保持不变；本轮按要求未触发 CI、未创建新 APK。计算器、AI 等业务功能仍未迁移。**

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
| 系统栏与安全区 | 代码已提交，待验收 | Edge-to-edge、浅色系统图标、Scaffold 安全区与 adjustResize 已配置 |
| 导航契约单元测试 | 已提交 | 四个入口、路由唯一性和文案完整性 |
| GitHub Actions 构建 | 已通过（Run #32） | 图片资源完整性检查、导航单测、assembleDebug、Artifact 上传、APK 自动归档全部成功；真机待验收 |
| Gradle Wrapper | 已完成 | 已添加 Gradle 9.3.1 wrapper JAR、properties、POSIX 与 Windows 启动脚本 |
| 液态玻璃渲染 | 未开始 | 下一小步单独建立 Glass Lab |
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


### 3.8 设计系统第 2 小项：抽取间距 Tokens（2026-10-31）

- [x] 新建 `app/src/main/java/ci/us/zhixing/app/ui/theme/SpacingTokens.kt`。
- [x] 将首页页面边距、区块间距、标题间距、徽标内边距、主内容区内边距、功能条内边距和微小间隔集中到 `ZhixingSpacing`。
- [x] `MainActivity.kt` 改为引用间距 Tokens；未改变任何既有 dp 数值或布局顺序。
- [x] 图标高度、圆角半径、边框宽度、导航栏 elevation 等非间距尺寸仍留在原处，避免扩大本轮范围。
- [x] 本轮提交使用 `[skip ci]`，不触发构建、不生成 APK。
- [ ] 后续统一安排一次构建验证；在用户允许生成 APK 前不执行构建。

本项只集中管理现有间距；颜色、字体、形状和页面视觉效果不变。
