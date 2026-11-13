# 知行 Android App 项目现状、操作指南与后续开发手册

> **核对日期：2026-11-13（UTC）**  
> **仓库：** PaleSquire71029/zhixingapp  
> **分支：** main  
> **文档目的：** 让新对话、开发者或未来的维护者不依赖聊天记录，也能准确知道项目做到哪里、哪些只是代码落地、如何构建 APK，以及下一步应该怎么做。

---

## 1. 先看结论

目前项目已从原生 Compose 工程骨架推进到包含「我的」页面、历史空状态、材质设置入口和浮动液态玻璃底部导航的 0.1.2 版本基线。一级导航为 **计算 / 知行 AI / 工具 / 我的**；普通本地计算不强制登录。导航图标已调整为圆润、带填色的 Apple 风格面性图标，底部 Dock 使用轻透玻璃材质，二级页面有淡入、轻微横移和缩放转场。

计算器核心正在进行纯 Kotlin 迁移：C0.1 黄金样例、C1 Tokenizer、C2 AST / Parser、C3 常数表达式求值、C4.1 多项式基础运算、C4.2 有理函数 / 原式定义域与数值精度保护、C4.3 一次 / 二次实根、C5.1–C5.7 受限方程、C6.1–C6.2 多项式 / 分式不等式，以及 C7.1–C7.3 坐标点校验和一次 / 二次函数拟合均已实现。Build #405 验收分式不等式；C7.1 通过 Build #406，C7.2 通过 Build #409，C7.3 二次函数拟合通过 Build #412（Run ID `43411748705`），Artifact ID `14029827428`，12,503,320 bytes。二次拟合使用三点插值和多点最小二乘，采用列主元 Householder QR、坐标归一化和 BigDecimal 尺度恢复。当前函数内核仍在纯 Kotlin domain，尚未接入 Compose 函数页面、函数表达式解析、定义域断点或图像采样；三次 / 反比例拟合、单位 / 化学、AI 客户端、原生账号与云历史也未完成。没有执行模拟器 / 真机仪器测试。

必须区分三种状态：

- **已实现并提交：** 仓库中能找到对应代码或资源。
- **代码已提交，待验证：** 代码已经写入，但尚无成功构建、自动化测试或目标真机结果作为依据。
- **尚未开始：** 只有规划或未来步骤，没有对应实现。

不要把“提交成功”“GitHub Actions 已启动”“模拟器能打开”混写成“功能验收通过”。

**当前接续顺序（2026-11-13）：**

1. 先阅读 [计算器核心迁移计划](./CALCULATOR_CORE_MIGRATION_PLAN.md) 和 [计算器原生页面设计](./CALCULATOR_NATIVE_UI_DESIGN.md)；核心迁移与 UI 实现分开处理。
2. C2 已通过 Android Build #350（Run ID `42443214070`）；仪器测试只完成源码编译，没有实际启动模拟器 / 真机。
3. C3、C4.1、C4.2.1、C4.2.2、C4.2.3 已完成对应 CI 验收；C4.3.1 一次实根通过 Build #388，C4.3.2 二次实根通过 Build #391（Run ID `42544184281`）。
4. C5.1–C5.7 方程内核、C6.1–C6.2 多项式 / 分式不等式已分别完成 CI（Build #403–#405）。C7.1 坐标点校验通过 Build #406，C7.2 一次函数拟合通过 Build #409，C7.3 二次函数拟合通过 Build #412（Run ID `43411748705`），Artifact ID `14029827428`，12,503,320 bytes。C7.3 使用三点插值 / 多点最小二乘与列主元 Householder QR。当前继续纯 Kotlin domain，不接 Compose 页面、专用键盘、函数图像 UI 或 AI；下一步 C7.4 三次函数拟合。PR #88 的材质滑块 / 开关手势与 120Hz 窗口偏好继续作为独立主壳事项。

**液态玻璃执行基准：** 官方 Backdrop Catalog 1.0.6 是移植来源；当前主分支已有浮动玻璃 Dock 与圆润填色图标。主界面大面积玻璃扩展和真机光学验收按 [液态玻璃专项计划](./LIQUID_GLASS_REDESIGN_PLAN.md) 执行，但不能因此打乱计算内核的小步顺序。



## 2. 当前仓库与项目配置

### 2.1 仓库

- GitHub：<https://github.com/PaleSquire71029/zhixingapp>
- 默认分支：`main`
- Android applicationId：`ci.us.zhixing.app`
- App 显示名称：`知行`
- 当前工程版本配置：`0.1.2`，versionCode 为 `3`；是否已有同版本仓库归档 APK，以最新成功构建 / 归档记录为准。
- 当前 Gradle 工程只有一个 `:app` 模块；暂时不拆成很多独立 Gradle module，避免早期维护成本。

### 2.2 已核对的构建配置

| 项目 | 当前值 | 说明 |
|---|---|---|
| 语言 | Kotlin | 原生 Android |
| UI | Jetpack Compose + Material 3 | 组件样式由知行自行设计 |
| Android Gradle Plugin | 9.1.1 | 根目录 build.gradle.kts |
| Gradle Wrapper | 9.3.1 | gradle/wrapper/gradle-wrapper.properties |
| Kotlin Compose 插件 | 2.4.21 | 根目录 build.gradle.kts |
| Compose BOM | 2026.06.00 | 与当前 API 36 编译目标匹配 |
| compileSdk | 36 | 编译 SDK |
| targetSdk | 36 | 当前目标 SDK |
| minSdk | 26 | 最低 Android API 暂定 26 |
| Java 编译目标 | 17 | CI 使用 Temurin JDK 17 |
| CI | GitHub Actions | 测试 + Debug APK 构建 + Artifact 上传 |

注意：这里是仓库中当前配置的事实，不等于已在所有 Android 版本和设备上验证兼容。

### 2.3 重要文件位置

| 文件 | 当前作用 | 后续维护提示 |
|---|---|---|
| `settings.gradle.kts` | Gradle 工程名和模块声明 | 新模块加入时同步修改 |
| `build.gradle.kts` | AGP / Kotlin 插件版本 | 升级前先查兼容矩阵 |
| `app/build.gradle.kts` | Android SDK、版本号、Compose 依赖 | 依赖版本变更需跑完整 CI |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 9.3.1 下载地址 | 始终使用 Wrapper |
| `gradlew` / `gradlew.bat` | Linux/macOS 与 Windows 构建入口 | CI 和本地统一使用 |
| `app/src/main/AndroidManifest.xml` | App 声明、启动 Activity、键盘窗口模式 | 新增权限前说明理由 |
| `app/src/main/java/ci/us/zhixing/app/MainActivity.kt` | 当前主界面、系统栏与页面布局 | 颜色方案已移动到 ui/theme/ColorTokens.kt |
| `app/src/main/java/ci/us/zhixing/app/ui/theme/ColorTokens.kt` | 知行浅色 Material 3 配色方案 | 统一管理浅色配色 |
| `app/src/main/java/ci/us/zhixing/app/ui/theme/SpacingTokens.kt` | 首页常用间距 | 统一管理既有 dp 间距，不改数值 |
| `app/src/main/java/ci/us/zhixing/app/ui/theme/ShapeTokens.kt` | 首页圆角形状 | 统一管理 28.dp / 18.dp 两种圆角 |
| `app/src/main/java/ci/us/zhixing/app/ui/theme/TypeTokens.kt` | 首页文字样式角色 | 统一管理字号、行高、字重和字距；不绑定额外字体文件 |
| `app/src/main/java/ci/us/zhixing/app/ui/theme/ZhixingTheme.kt` | 知行统一主题入口 | 暴露 colors / type / spacing / shapes，并应用浅色 Material 3 主题 |
| `app/src/main/java/ci/us/zhixing/app/ui/lab/GlassLabScreen.kt` | Glass Lab 局部背景快照实验 | GraphicsLayer 捕获受控背景 Composable；局部裁切、模糊与手动刷新，不采集窗口内容 |
| `app/src/main/java/ci/us/zhixing/app/ui/lab/BackdropCrop.kt` | 背景快照源区域计算 | 纯函数计算居中面板裁切区域，非法尺寸返回 null |
| `app/src/test/java/ci/us/zhixing/app/ui/lab/GlassBackdropCropTest.kt` | 背景快照裁切单元测试 | 正常、极小和无效输入覆盖；当前待执行 |
| `app/src/main/java/ci/us/zhixing/app/navigation/AppDestination.kt` | 四个底部导航目的地及占位描述 | 未来与真实页面路由连接 |
| `app/src/test/java/ci/us/zhixing/app/navigation/AppDestinationTest.kt` | 导航数据契约的 JVM 单元测试 | 未来逐步增加状态和导航测试 |
| `app/src/main/res/drawable-nodpi/zhixing_logo.webp` | 用户提供的蓝紫渐变图标 | 不随意裁切、重绘或改变比例 |
| `app/src/main/res/drawable-nodpi/zhixing_logo.webp` 与 launcher mipmap XML | 品牌图片与自适应桌面图标资源 | 检查不同启动器遮罩效果 |
| `.github/workflows/android-build.yml` | 自动测试、打包和上传 Debug APK | 每次主分支 push 会触发 |

品牌图标具体资源文件名以仓库实际文件为准；不要把旧网页的书本图标或其他项目图标替换进来。

## 3. 已经做了什么

**液态玻璃专项执行路线已于 2026-11-01 重置：** 以用户上传的 Backdrop Catalog APK 为视觉 / 交互基准，锁定上游 tag 1.0.6，直接移植官方 LiquidBottomTabs、DampedDragAnimation、InteractiveHighlight、LiquidButton、LiquidSlider、LiquidToggle 等源码；不升级 Backdrop 2.x，不复制整套 Demo App。详细步骤见 [LIQUID_GLASS_REDESIGN_PLAN.md 第 25 节](./LIQUID_GLASS_REDESIGN_PLAN.md)。

### 3.1 原生工程与主导航

**状态：原生壳、我的页和导航底座已实现；真正业务页面仍在逐步迁移。**

- 已建立单一 `:app` 原生 Android 工程，使用 Kotlin + Jetpack Compose。
- 一级导航为「计算 / 知行 AI / 工具 / 我的」；原来的「历史」不再单独占用一级 Tab。
- 「我的」原生页面已包含轻量身份区、最近使用空状态、历史记录入口、材质设置入口与动态应用版本信息。
- 历史页面当前只呈现真实空状态；尚无 Room 本地计算历史存储，不允许显示虚构记录。
- 材质设置从首页入口迁入「我的」；进入历史 / 材质二级页面有淡入、轻微水平位移和缩放转场，返回时保留主页面状态。
- 底部导航已采用浮动液态玻璃 Dock 和圆润填色图标；玻璃透出页面背景，内容需为 Dock 和系统手势区域留足安全空间。
- 计算、AI、工具 Tab 的业务页面仍未接入；当前展示内容仍有占位性质，不能把导航切换视为功能已经完成。

相关文件：
- `app/src/main/java/ci/us/zhixing/app/MainActivity.kt`
- `app/src/main/java/ci/us/zhixing/app/navigation/AppDestination.kt`
- `app/src/main/java/ci/us/zhixing/app/ui/my/MyScreen.kt`
- `app/src/main/java/ci/us/zhixing/app/ui/history/HistoryScreen.kt`
- `app/src/main/java/ci/us/zhixing/app/ui/glass/` 下的导航玻璃适配组件


### 3.2 知行品牌图标

**0.1.1 状态：图标资源已修复并通过 CI；真机启动待用户验收。**

已确认首版 APK 内 WebP 资源被截断：文件实际 5,193 bytes，而 RIFF 头部声明总长度 9,614 bytes。首页和启动器图标共用该资源，因此可能导致首页启动解码崩溃，同时使桌面图标无法正常显示。修复已合并至 main（PR #3）：替换为 160×160 透明 WebP。GitHub 远端文件实际 4,728 bytes，与 RIFF 声明长度一致，WebP 分块完整。版本为 0.1.1 / versionCode 2。Android Build #32 已通过资源校验、单元测试和 APK 构建，且已将修复版自动归档到仓库 `artifacts/zhixingapp-preview.apk`。真机启动仍待用户验收。

**原始资源的设计内容与用户图标一致；仅重新编码，不重画图形。**

- 用户提供的蓝紫渐变抽象标志已经纳入 Android 资源。
- 已配置 launcher adaptive icon 的普通和圆形入口。
- 首页品牌区也展示同一标志。
- 不需要重新生成图标；后续应沿用这一资源。

### 3.3 浅色视觉基线

**状态：基础版本已实现，正式设计系统尚未建立。**

- 当前使用 `lightColorScheme`。
- 页面背景、文字、基础边框与导航选中状态已经设置。
- 未创建深色主题切换。
- 当前仍然是简单 Surface / NavigationBar 组合；没有实现 iOS 26 风格的实时折射、背景采样或流体玻璃材质。

### 3.4 Gradle Wrapper

**状态：已实现并提交。**

- Gradle Wrapper 版本为 9.3.1。
- 已有 Wrapper properties、JAR、POSIX 启动脚本和 Windows 启动脚本。
- 本地或 CI 都应使用 `./gradlew`，Windows 使用 `gradlew.bat`，而不是依赖机器上不确定的系统 Gradle 版本。

### 3.5 系统栏、安全区和键盘窗口

**状态：代码已提交，构建与真机验收待完成。**

- Activity 使用 AndroidX Activity 的 `enableEdgeToEdge`。
- 状态栏与导航栏使用适合浅色背景的系统图标样式。
- Android 10+ 关闭系统自动对比度 scrim。
- Activity 和 Manifest 配置了 `adjustResize`。
- 页面通过 Material 3 Scaffold 组织内容和底部导航。

需要特别检查：不同系统版本的手势导航、三键导航、状态栏图标、底部导航与键盘是否重叠。代码配置存在不代表这些设备表现已确认。

### 3.6 单元测试与 CI

**状态：测试代码、CI 工作流和首次 Debug APK 构建均已成功。真机验收仍未执行。**

- 已有两个导航契约测试，检查入口数量、路由唯一性和文案非空。
- GitHub Actions 配置为安装 JDK 17 和 Android SDK API 36。
- 使用 Gradle Wrapper 执行：
  - `./gradlew --no-daemon --stacktrace :app:testDebugUnitTest :app:assembleDebug`
- 成功后上传名为 `zhixingapp-debug` 的工作流 Artifact。
- 先运行 `python3 scripts/verify_android_resources.py`，验证 PNG / WebP / JPEG 资源结构和 launcher 引用，防止 Gradle 将损坏图片静默打包。
- 首个工程骨架构建：Android Build #15（Run ID 41206049511）成功。
- 图标修复版构建：Android Build #32（Run ID 41208283410）成功；资源完整性、导航单测、assembleDebug、Artifact 上传与仓库 APK 归档全部成功。
- 修复版 Artifact：`zhixingapp-debug`；APK 名称：`app-debug.apk`；APK 大小：11,490,765 bytes。
- 修复版 APK SHA-256：`d20ff37aaa0b6eb54d19dff1d2bcb5ea3c9f69bd17ffc6d38572bf0e2643d9ba`。
- [查看修复版成功构建](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41208283410)。
- 这只证明构建与当前自动化测试成功，不证明真实设备的安装、渲染和交互已经验收。

## 4. 明确还没有做什么

下面这些能力目前**没有完成原生迁移**，不能在测试 APK 中期待它们已经能用。

### 4.1 计算器核心

**历史阶段记录（2026-11-06）：** C0.1 黄金样例、C1 Tokenizer 已通过 CI；当时 C2 AST / Parser 的右结合幂链测试断言已于 `782dd334` 修正。该阶段后续已由 Build #350、#363 等记录更新，不应将这一旧段落视为当前接续状态。详见 [CALCULATOR_CORE_MIGRATION_PLAN.md](./CALCULATOR_CORE_MIGRATION_PLAN.md)。



- [ ] 可输入并计算完整表达式
- [ ] 自定义数字与数学键盘
- [ ] 科学计算和常用函数
- [ ] 幂、负号与运算符优先级边界
- [ ] 有理数与精确数值处理
- [x] 方程求解纯 Kotlin 内核：多项式至三次、分式定义域与增根剔除、常数右侧线性内部绝对值方程（未接入页面）
- [ ] 方程输入 UI、其它类型方程及步骤展示
- [ ] 不等式求解与分母零点分支
- [ ] 多项式符号区间
- [ ] 线性方程组
- [x] 函数拟合纯 Kotlin 内核：坐标点输入校验、一次 / 二次拟合（尚未接入 Compose 页面）
- [ ] 函数表达式、函数图像与坐标交互
- [ ] 单位换算
- [ ] 本地计算历史与收藏

### 4.2 化学工具

- [ ] 化学式解析
- [ ] 化学方程式配平
- [ ] 元素搜索与化学专用键盘
- [ ] 反应条件、气体和沉淀标记
- [ ] 化学反应预测
- [ ] 净离子方程式
- [ ] 原有化学回归测试迁移

### 4.3 知行AI

- [ ] Worker 连通性检查
- [ ] 模型列表读取与模型选择
- [ ] 普通对话请求
- [ ] SSE 流式输出、停止生成与取消
- [ ] 错误恢复、重试与防止重复消息
- [ ] Markdown 与 LaTeX 渲染
- [ ] 图片识题与图片选择
- [ ] 文件选择和附件状态
- [ ] 自动构建计算器上下文
- [ ] 多轮追问与最近对话

### 4.4 账号、云历史和数据层

- [ ] 原生登录与登出界面
- [ ] 用户名登录和会话恢复
- [ ] Android Keystore 保护 Token
- [ ] Room 本地计算历史
- [ ] DataStore 偏好设置
- [ ] Worker / Supabase 云历史
- [ ] 历史删除、同步状态与错误恢复
- [ ] 用户数据隔离和权限回归测试

普通计算不能强制登录。服务端密钥、管理员密钥、Supabase service-role key 和数据库密码不能进入 APK 或仓库。

### 4.5 液态玻璃效果

- [ ] Glass Lab 实验入口
- [ ] 背景采样
- [ ] 模糊半径对比
- [ ] AGSL / RuntimeShader 单通道折射
- [ ] 边缘高光
- [ ] 轻微色散
- [ ] 触摸高光和按压反馈
- [ ] Premium / Standard / Lite / Off 性能档位
- [ ] 在目标手机上测量帧率、GPU 成本、发热和耗电

**因此，当前 APK 的正确定位是“原生 UI 框架预览版”，不是功能完整的计算器或 AI 测试版。**

## 5. APK 构建、获取和安装

### 5.1 仓库已经配置的自动构建方式

工作流文件：

`.github/workflows/android-build.yml`

触发条件：

- push 到 `main`
- 针对 `main` 的 pull request
- 在 GitHub Actions 页面手动运行（workflow_dispatch）

构建任务：

1. 检出仓库；
2. 安装 JDK 17；
3. 配置 Android SDK；
4. 安装 `platforms;android-36`、`build-tools;36.0.0` 和 platform-tools；
5. 运行导航单元测试；
6. 运行 `:app:assembleDebug`；
7. 将 `app/build/outputs/apk/debug/app-debug.apk` 上传为 `zhixingapp-debug` Artifact。
8. 主分支 push 构建成功后，将 APK 复制到仓库 `artifacts/zhixingapp-preview.apk` 并提交；Artifact 路径被设置为 push 触发忽略项，避免自动提交 APK 后再次触发构建循环。

**已验证：** Android Build #22 已成功执行上述归档，仓库 Git Tree 中存在 `artifacts/zhixingapp-preview.apk`（11,491,233 bytes）。日常直接下载入口：<https://raw.githubusercontent.com/PaleSquire71029/zhixingapp/main/artifacts/zhixingapp-preview.apk>。

### 5.2 如何在 GitHub 手机上找到 APK

优先直接打开仓库中的 APK：<https://raw.githubusercontent.com/PaleSquire71029/zhixingapp/main/artifacts/zhixingapp-preview.apk>。该文件现已存在于主分支，是 CI 自动归档的最新 Debug 包；后续主分支构建成功会自动更新它。

1. 打开仓库：<https://github.com/PaleSquire71029/zhixingapp>。
2. 进入 **Actions**。
3. 打开名称为 **Android Build** 的最新一次运行。
4. 先看运行是否显示绿色成功。若红色失败，进入 `Compile and test` 作业查看第一个真正失败的步骤。
5. 成功运行页面底部找到 **Artifacts**。
6. 下载 `zhixingapp-debug` ZIP。
7. 用手机文件管理器解压，找到 `app-debug.apk`。
8. 点击 APK 安装；Android 可能要求允许当前浏览器或文件管理器“安装未知应用”。
9. 安装完成后打开“知行”。

不要把 Artifact ZIP 当作 APK 安装；需要先解压。若 GitHub 运行没有 Artifact，通常表示构建或测试没有成功到上传步骤。

Debug APK 现已直接存入仓库：`artifacts/zhixingapp-preview.apk`。建议通过仓库 README 的下载入口获取最新文件；也可以打开该文件的 Raw 链接。每次主分支构建成功后，CI 会自动更新该文件。

### 5.3 Debug APK 的限制

- 这是调试构建，适合预览、测试，不是 Play Store 正式发布包。
- Debug APK 由 Gradle 的 debug signing 自动签名，不需要将正式签名密钥放入仓库。
- 不同 CI 机器生成的 debug keystore 可能不同；后续更新安装失败时，可能需要先卸载旧的预览版，再安装新 APK。
- 不能把 Debug APK 当作稳定的正式升级渠道。

### 5.4 本地 Android Studio 构建（有电脑时）

1. 安装 Android Studio 与 JDK 17。
2. 安装 Android SDK Platform 36、Build Tools 36.0.0。
3. 克隆仓库并用 Android Studio 打开仓库根目录。
4. 等待 Gradle Sync 完成。
5. 在项目根目录运行：

```bash
./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

Windows 命令：

```powershell
.\gradlew.bat --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

生成路径：

`app/build/outputs/apk/debug/app-debug.apk`

构建完成后，建议再运行：

```bash
./gradlew --no-daemon lintDebug
```

Lint 当前不属于现有 CI 的既定验收步骤，运行结果需单独记录。

## 6. 构建失败时的排查指引

| 现象 | 先检查什么 | 不要怎么做 |
|---|---|---|
| 找不到 Android SDK Platform 36 | CI 的 sdkmanager 安装步骤与 SDK 仓库状态 | 不要直接升级一批依赖掩盖问题 |
| Compose 报 compileSdk 版本不足 | Compose BOM 与 compileSdk 的官方兼容要求 | 不要只提高 compileSdk 而不确认 SDK 包能安装 |
| Wrapper 无法启动 | gradlew 文件、JAR、Gradle 9.3.1 URL、JDK | 不要改成依赖系统 Gradle |
| Kotlin 编译错误 | 第一条 Kotlin compiler error 和对应源码行 | 不要一次改很多文件 |
| 单元测试失败 | 失败测试名称、断言值和对应改动 | 不要删除失败测试让 CI 变绿 |
| APK 没有上传 | 先看测试与 assemble 是否成功，再看 upload-artifact 步骤 | 不要把源码 ZIP 误当 APK |
| 安装时提示签名不一致 | 是否安装过另一个 CI 生成的同包名 APK | 预览版可先卸载旧版，再安装新版本 |

修复规则：一次只修第一个确定原因，重新运行构建；通过后再继续下一项。

## 7. 下一步应该怎么做（严格按顺序）

每个小步骤应独立提交，并做源代码检查和针对性 JVM 单元测试，同时更新 `docs/IMPLEMENTATION_STATUS.md`。不要求每个小步骤都构建 APK；多个紧密相关步骤可在阶段检查点统一执行完整 CI，只有需要验证 Android 平台编译或用户明确需要 APK 时才执行 APK 构建。不要把一轮塞进多个大功能。

**当前数学内核接续入口：C5.3–C5.7、C6.1–C6.2 与 C7.1–C7.3 已完成相应 CI 验收；当前下一步为 C7.4 三次函数拟合。** 旧手册下面的历史分步记录用于追溯，不应覆盖 `CALCULATOR_CORE_MIGRATION_PLAN.md` 的最新阶段状态。

### 第 0 步：修复损坏图标并重新验收

**状态：修复版 CI 已通过，真机待验收。**

**目标：** 确认修复后的 APK 可以在真机启动，桌面图标能够显示。GitHub Actions Run #32 已完成资源校验、单元测试和 APK 打包，真实设备安装与界面验收仍待进行。

操作：

1. 打开 GitHub Actions 的最新 Android Build 运行；
2. 检查 API 36 / Compose BOM 2026.06.00 是否成功解析依赖；
3. 确认两个导航测试通过；
4. 确认 Debug APK 成功生成并上传；
5. 在至少一台 Android 真机安装；
6. 确认应用可以启动、首页图标能正常显示、桌面图标出现；
7. 若仍闪退，记录 Android 系统弹出的错误或从开发者选项/Logcat 获取 FATAL EXCEPTION；
8. 截图记录首页、四个导航、系统状态栏和底部区域。

完成条件：CI 绿灯、有 Debug Artifact、真机可安装启动；系统栏无严重遮挡。未完成前不引入新依赖。

### 第 1 步：建立知行设计 Tokens

**当前进度：颜色、间距、形状与文字 Tokens 已完成抽取；ZhixingTheme 统一入口已建立，编译和 UI 验证待安排。**

**目标：** 让颜色、字体、圆角和间距有单一来源。

建议新增：

- `ui/theme/ColorTokens.kt`
- `ui/theme/ShapeTokens.kt`
- `ui/theme/SpacingTokens.kt`
- `ui/theme/TypeTokens.kt`
- `ui/theme/ZhixingTheme.kt`

操作指引：

1. [x] 已从 `MainActivity.kt` 将 `ZhixingLightColors` 抽取到 `ui/theme/ColorTokens.kt`；颜色值保持不变；
2. [x] 已将首页常用留白和内容间距抽取到 `ui/theme/SpacingTokens.kt`；原有 dp 数值未改变；
3. [x] 已将首页两种既有圆角抽取到 `ui/theme/ShapeTokens.kt`；28.dp 与 18.dp 数值未改变；
4. [x] 已定义品牌标题、页面标题、正文、辅助文字、徽标与导航符号样式角色；
5. [x] 暂不绑定字体文件，沿用 Compose / Android 默认字体回退；不把 22.7 MB 的 OPPO Sans 完整文件打进 APK；
6. [x] 已建立 `ZhixingTheme`，统一暴露颜色、文字、形状与间距，并接入 Material 3 浅色主题。

剩余操作：安排一次构建、导航单测与 UI 验证。完成条件：外观没有意外变化；首页和导航测试通过；所有页面只从统一 Tokens 读取基础样式。

本次颜色、间距、形状、文字 Tokens 与统一主题入口的提交均有意跳过 CI，避免生成 APK；因此编译和测试状态保持“待验证”。

### 第 2 步：Glass Lab 静态实验页

**状态：静态页面代码已提交，构建与真机验收待进行。**

已完成：

1. [x] 新建独立实验页面，不覆盖主首页和四个主导航入口；
2. [x] 添加浅色渐变画布、彩色背景形状与半透明样本容器；
3. [x] 提供首页入口、页面内返回和 Android 系统返回键；
4. [x] 明确标注当前仅是静态透明叠层示意，不代表真实 Liquid Glass。

尚未实现：实时背景采样、模糊、折射、色散、动态高光和触摸形变。

完成条件：后续编译通过；真机可进入和退出实验页；主导航仍正常；窄屏滚动无裁切；不出现深色面板。

本次按要求跳过 CI，不生成 APK，以上状态仅代表代码已提交。

### 第 3 步：背景采样和模糊

**当前进度：GraphicsLayer 局部背景快照、面板区域裁切、10.dp 模糊 / 清晰对照及手动重新采样代码已提交；构建与真机验收待进行。**

已完成：

1. [x] 使用 `rememberGraphicsLayer()` 和 `drawWithContent` 录制实验背景 Composable 的实际绘制内容；
2. [x] 使用 `toImageBitmap()` 自动生成初始快照，并支持手动重新采样；
3. [x] 按面板在背景画布中的位置裁切快照，再放入玻璃面板；
4. [x] 支持 10.dp 模糊 / 清晰切换，文字与浅色叠层独立保持清晰；
5. [x] API 31 以下显示原始快照但关闭系统模糊。

技术边界：这是真实的局部 Composable 绘制快照，但它不会读取整个窗口、任意其他页面或任意兄弟组件，也不会随背景每帧自动刷新。后续如需动态 backdrop，需要另行设计可控的共享绘制树或平台渲染方案。AGSL 折射另作为后续独立实验，不应与快照采样能力混为一谈。

已补充裁切源区域纯函数和 JVM 单元测试，覆盖常规尺寸、极小尺寸与无效尺寸；但测试尚未执行。真机还需检查图像裁切坐标、圆角、滚动性能、截图内存、系统栏与设备温度；本轮跳过 CI，不生成 APK。

### 第 4 步：单通道 AGSL 折射

**目标：** 先实现一条可解释的背景位移，不追求复杂视觉堆叠。

操作：

1. 将 Shader 放在独立 glass 包；
2. 第一次只实现单通道坐标位移；
3. 折射仅作用于 Glass Lab 的单个样本；
4. API 33 以下提供简化替代材质；
5. 添加关闭开关，便于做有/无效果的对照。

完成条件：Shader 无崩溃、无明显边缘撕裂，低版本能降级，性能数据已记录。

### 第 5 步：边缘高光、色散和触摸

按小项拆分：先高光，再轻微色散，再按压反馈，最后拖动高光。每次只加一种效果，检查发热和掉帧。不要先做液体融合动画。

### 第 6 步：正式计算器界面（先 UI，再算法）

1. 先定义表达式显示区、结果区、输入区和数学键盘的布局；
2. 确保窄屏、横屏和系统字体放大时不挤压；
3. 自定义键盘先实现按键布局和输入状态，不假装有正确的计算引擎；
4. 算法先从测试样例和 tokenizer 开始迁移；
5. parser、基础求值、科学函数逐项迁移；
6. 专门测试 `-2^2`、`(-2)^2` 和 `-(2^2)`；
7. 再迁移方程、不等式、分母零点和函数图像。

完成条件：数学行为与旧实现的已确认规范一致，关键边界用单元测试覆盖。不得为了视觉进度先接一个不可靠的计算内核。

### 第 7 步：工具模块

按顺序迁移单位换算、方程组、函数工具、化学方程式配平、反应预测等。每个工具独立页面、独立测试、独立验收；不要把所有工具塞进一个页面。

### 第 8 步：知行AI原生客户端

1. 先验证 Worker 的健康检查和真实返回结构；
2. 验证模型列表接口和错误状态；
3. 实现非流式聊天；
4. 实现 SSE 的分段、取消和异常中断；
5. 再加 Markdown / LaTeX；
6. 再加图片和文件；
7. 最后接入计算器上下文。

注意旧项目已发现 `calculator-ai` 的产品 ID 可能被 Worker 归一化成 `learning`，且历史表约束没有该 ID。接入前必须先设计后端和数据库迁移；不可仅在 App 请求里写一个字符串就认为已隔离成功。

### 第 9 步：账号、历史和安全

- 先画登录状态图；
- 再验证用户名登录 API；
- 使用 Keystore 保护会话凭据；
- 本地计算历史用 Room；
- 设置使用 DataStore；
- 云端消息按用户和产品身份隔离；
- 测试删除、登出、Token 过期、断网重试和重复提交。

严禁把管理员密钥、上游模型密钥或 Supabase service-role key 写进 Android 客户端。

## 8. 每一步如何验收

每次合并或提交前至少检查：

- [ ] 修改文件只有本步骤相关内容；
- [ ] 没有引入不必要的大型依赖；
- [ ] 没有深色主题或大面积深色块；
- [ ] 数学逻辑有对应单元测试；
- [ ] 页面在窄屏下不会横向溢出；
- [ ] 输入法弹出时输入框和底部按钮不被遮挡；
- [ ] 动画可取消并尊重系统减少动态效果设置；
- [ ] 未提交 API Key、Token、密码或签名文件；
- [ ] 构建和测试结果真实记录；
- [ ] `docs/IMPLEMENTATION_STATUS.md` 已同步更新。

### 每个步骤的状态写法

- **未开始：** 没有实现代码。
- **进行中：** 有未完成代码或依赖实验。
- **代码已提交，待验证：** 提交存在，但构建、测试或真机验证未全部完成。
- **已验收：** 代码、构建、自动测试和对应设备验证均完成。

## 9. 新对话接续时怎么做

接手本项目时按以下顺序：

1. 打开本文件，了解真实范围；
2. 打开 `docs/IMPLEMENTATION_STATUS.md`，确定最新完成项；
3. 打开 `docs/ANDROID_APP_MASTER_PLAN.md`，查看长期要求和技术边界；
4. 检查 GitHub `main` 最新提交，不使用旧聊天里留下的 SHA 当作最新版本；
5. 查看最近一次 Android Build 的状态；
6. 只选择最靠前的一个未完成步骤；
7. 修改后运行构建与测试；
8. 更新实施状态文档，再结束本轮工作。

如果聊天上下文与仓库文档冲突，以用户最新明确要求和仓库实际代码为准，并把差异写回文档。

## 10. 当前提交与核对边界

本文件创建时，已核对的最新代码基线为：

- 系统栏与输入法代码提交：`ba4eb6f8c8ed69f75641f3309694db6027ea28a8`
- Manifest 键盘窗口配置提交：`4fab50db2a64b9eb0a8736381bf44f92d363ed9d`
- 成功构建验证：Android Build #15 / `41206049511`
- APK 测试清单已合并到 main：`docs/APK_TEST_CHECKLIST.md`
- 文档与 APK 验收记录已继续提交；接手时仍应重新检查 main 最新 SHA。

以上 SHA 是核对时的参考点；后续开发必须重新检查 main 分支的最新 SHA。构建是否通过、APK 是否生成、真机是否启动，需要以对应的 Actions 运行记录和实际设备验证为准。

---

**一句话交接：现在有原生 Android 项目骨架、品牌图标、四个占位导航、浅色基础主题和颜色与间距 Tokens 文件；原有数值均未改变。本轮未创建 APK、未运行 CI。接下来只应继续抽取一个小项（例如字体角色 Tokens），完成后再统一安排编译验证。完整计算器、AI、化学、历史、账号、云同步和真实液态玻璃效果仍未迁移。**


### Glass Lab 动态采样实验（2026-11-01）

**状态：代码已提交，编译与真机性能验收待进行。**

- 动态采样默认关闭，用户手动开启后背景形状轻微移动，局部快照最多每秒更新一次。
- Activity 发出 `ON_STOP` 时暂停采样；回到 `ON_START` 时允许继续。实验页被移出 Composition 时，`LaunchedEffect` 与组合级协程自动取消。
- 手动刷新和动态刷新共享互斥锁，避免同一图形层快照重叠执行。
- 页面显示当前快照次数及采样暂停状态。
- 快照只覆盖 Glass Lab 自己的背景子层，不采集其他页面、兄弟 Composable 或系统窗口；刷新频率约 1 Hz，属于离散快照。
- 本轮仅代码提交，跳过 CI，不生成 APK；尚未在真机上测量耗电、温度、内存或帧时间。

后续验收重点：连续运行 2–5 分钟、切换后台再返回、反复进出实验页、关闭动态采样、比较清晰与模糊状态，并观察快照是否与移动背景同步。


### Glass Lab AGSL 单通道折射实验（2026-11-01）

**状态：代码已提交，Android 编译与真机验收待进行。**

- API 33 及以上使用 Android `RuntimeShader`；Compose 通过 `ShaderBrush` 将 AGSL Shader 绘制进面板背景区域。
- Shader 输入来自 Glass Lab 自己的局部 GraphicsLayer 快照，使用 `BitmapShader` 采样，不读取系统窗口或其他页面。
- AGSL 只沿玻璃轮廓方向轻微位移采样坐标，RGB 通道共用相同坐标；没有色散、双边色差或彩虹边缘。
- 折射强度 0–8 px 可调，默认约 3.5 px；可单独关闭折射，并保留背景模糊开关。
- API 26–32 不调用 RuntimeShader；回退为快照与平台支持的模糊。
- Shader 编译或输入绑定失败时，面板退回普通快照绘制；运行时显示降级状态。
- 已编译 RuntimeShader 实例复用；快照更新时只替换 BitmapShader 输入，不每秒重建 AGSL 程序。
- 本轮跳过 CI，不生成 APK；Android 实际编译、Shader 编译和真机 GPU 性能尚未验证。

参考 Android 官方文档：RuntimeShader 要求 API 33+；Compose 支持通过 ShaderBrush 绘制 RuntimeShader。真机验证时重点看边缘是否自然、强度滑块是否产生连续变化、低版本是否无崩溃、采样更新时是否掉帧。



### Glass Lab 折射方向几何修正（2026-11-01）

在单通道折射原型基础上修正了非正方形面板的位移方向计算：

- 边缘强度继续由按宽、高分别归一化的坐标计算，因此折射强度区域仍随面板边界变化。
- 位移方向改用像素坐标中“当前像素指向面板中心”的单位向量，避免横纵缩放比例影响实际像素位移方向。
- 新增纯 Kotlin 数学镜像函数与 JVM 测试，覆盖中心零位移、内部静止区、非正方形面板方向、最大位移与非法输入。
- 该 Kotlin 函数仅用于独立验证公式，不参与 AGSL 实时渲染。
- 本轮跳过 CI、不生成 APK；JUnit、Android 编译、AGSL 运行与真机 GPU 性能仍未验证。


### Glass Lab 方向性边缘高光实验（2026-11-01）

- 在当前 RuntimeShader 内增加 rim 高光计算，继续复用同一背景 BitmapShader 输入与单通道折射采样，不创建第二个 Shader。
- 高光通过边缘距离带生成：上沿亮度最高，左缘次之，右缘和下沿保留极弱反射；边缘宽度与柔和过渡按 dp 转换成 px。
- 高光输出是白色轻混合，强度最大 0.46；独立开关与折射开关分离，关闭折射时仍可单独看高光。
- Compose 原有圆角裁切决定最终边缘形状。它不是精确的圆角法线光照模型，真机仍需检查角落效果及边缘均匀性。
- 低于 API 33 时不使用 RuntimeShader，按钮不可用，面板继续走快照 / 模糊路径。
- 新增 Kotlin 数学镜像与 JVM 测试，不代表 AGSL 编译或渲染已经通过设备验收。
- 本轮跳过 CI、不生成 APK；编译、JVM 测试、真机观感和 GPU 性能仍待验证。


### Glass Lab 微量 RGB 色散实验（2026-11-01）

- 在单一 AGSL RuntimeShader 中加入可选子像素 RGB 采样偏移，不新增 RuntimeShader 或 BitmapShader 输入。
- 使用同一背景快照采样源：红通道向径向外侧轻微偏移，蓝通道向相反方向偏移，绿通道保持当前折射采样位置。
- 色散受边缘 mask 控制，中心区域偏移为 0；最大偏移限制为 0.8 px，默认预设 0.25 px，开关默认关闭。
- 采样偏移由面板本地像素换算到源快照像素，使用 sourceSize / resolution 比例，保持截图缩放变化时的位移幅度稳定。AGSL 的 child shader 通过 eval() 读取，坐标为 Shader 的局部 Canvas 坐标；实现参考 Android RuntimeShader API 文档。
- 关闭色散时走单次背景 eval() 分支，避免不必要的三路颜色采样。
- 新增纯 Kotlin 偏移镜像函数和 JVM 测试；测试不能证明 RuntimeShader 语法、采样结果或手机 GPU 表现已验证。
- 本轮跳过 CI、不生成 APK；Android 编译、AGSL 编译、真机视觉与性能验收待后续进行。


### Glass Lab 指尖高光与按压反馈实验（2026-11-01）

- 只在 CapturedGlassPanel 样本上增加单指触摸追踪；以局部归一化坐标作为 Shader 输入，避免绑定到屏幕绝对坐标。
- 自定义指针监听仅观察按下、移动和抬起，不调用 consume()；设计目标是让外层纵向滚动仍保持工作。真机仍需验证手势竞争与取消路径。
- 同一 AGSL RuntimeShader 叠加半径约 68 dp 的柔和径向触摸光斑，强度通过 Compose 动画在按下和松开时渐入渐出。
- 按下时样本面板 scale 约 0.992，松开后以 180 ms 级动画恢复；无持续呼吸或循环动画。
- 触摸光斑与边缘高光、折射、色散的 uniforms 独立，触摸不会触发新的背景快照。
- 新增纯 Kotlin 高光衰减镜像与 5 项 JVM 测试；这些测试不验证 Compose PointerInput 的运行行为，也不验证 AGSL 编译或设备帧率。
- 本轮跳过 CI、不生成 APK；Android 编译、JUnit 和真机滚动/触摸验收仍待完成。


### Glass Lab 四档性能策略（2026-11-01）

| 档位 | 背景采样 | 模糊 | Shader | 触摸反馈 | 面板叠层 |
|---|---|---:|---|---|---|
| Premium | 前台最多 1 次 / 秒 | 10 dp（API 31+） | 折射 3.5 px + 边缘高光 | 开 | 0.48 |
| Standard | 进入时一次性采样 | 6 dp（API 31+） | 折射 1.8 px + 边缘高光 | 开 | 0.60 |
| Lite | 进入时一次性采样 | 关闭 | 关闭 | 关 | 0.78 |
| Off | 不采样，隐藏背景快照 | 关闭 | 关闭 | 关 | 0.96 |

- Premium 和 Standard 默认不启用 RGB 色散；用户可在实验页单独开启色散并调整强度。
- 设备 API 能力在解析预设时自动应用：RuntimeShader 要求 API 33；模糊能力按 API 31+ 判断。低版本不创建或调用对应图形能力。
- 手动更改某一项时，模式标题变成“自定义”；再次选中某个预设即可一次性恢复该档参数。
- Off 档不导出背景图层快照，降低无用图形处理；切换离开 Off 后会按需要重新生成首次快照。
- 档位只代表预设资源配置，不是设备性能检测或帧率承诺。必须在目标手机分别实测运行时间、帧率、发热和耗电后，才能决定正式产品默认档位。
- 本轮跳过 CI、不生成 APK；档位解析单测已添加但未执行，Android UI / Gradle 编译也待验证。


 
## 11. 2026-11-06：下一阶段接续点

本节为本手册旧“下一步”清单的最新接续说明；若旧步骤与当前仓库代码不一致，以 main 最新代码和 `docs/IMPLEMENTATION_STATUS.md` 为准。

### 当前阶段判断

- 原生 App 目前是 Kotlin + Jetpack Compose，只有 `:app` 一个 Gradle 模块。
- 首页四个 Tab 仍是导航占位，不代表计算、AI、工具、历史已迁移。
- 材质设置页已完成浅色分组重设计，液态玻璃组件已接入。
- PR #88 仍未合并，包含滑块连续跟手、开关长按滚动仲裁、按压 / 释放竞态修复、速度采样优化和 120Hz 窗口刷新率偏好。
- 最新仪器测试失败属于云端模拟器 ADB device offline / no compatible devices，不能据此判断手势断言通过，也不能声称真机 120fps 已验证。

### 下一阶段：计算器原生核心

详细计划：[CALCULATOR_CORE_MIGRATION_PLAN.md](./CALCULATOR_CORE_MIGRATION_PLAN.md)。

**第一项任务 C0.1：** 建立旧计算器表达式、方程和不等式黄金样例。只整理样例与测试接口，不写 parser、不做 UI，也不修改旧 calculator 仓库。

完成 C0 后的顺序是：Tokenizer → AST / 递归下降解析器 → 常数表达式求值 → 多项式 / 有理函数 → 方程 → 不等式 → 函数工具 → 单位换算 → 化学工具 → 本地历史 → 原生计算页接入。

### 继续开发前的边界

1. PR #88 的材质 / 120Hz 工作单独收尾，不与计算器算法迁移混成一个 PR。
2. 数学内核先做纯 Kotlin domain，不能依赖 Compose、Android View 或 WebView。
3. 每个 C 小步至少有对应 JUnit；构建通过后再继续下一项。
4. 旧仓库只作为参考和测试基线，不在本阶段修改其生产页面。
5. 先保证计算正确和错误边界，再做按键动效与玻璃层，不用 UI 遮掩计算功能缺失。


### 12. 2026-11-06：一级导航修订

用户已确定一级导航为 **计算 / 知行 AI / 工具 / 我的**。原「历史」不再作为一级 Tab，历史记录和设置统一放进「我的」。导航图标采用圆润、带填色的 Apple 风格面性设计，不再使用默认空心线稿；底部导航背景采用适度模糊的浮动液态玻璃 Dock，允许页面背景透出；进入历史、材质等二级页面时使用淡入、轻微位移和缩放转场。

上述导航与「我的」页面已在主分支落地：MainActivity.kt 接入 MyScreen、历史 / 材质二级页面和 AnimatedContent 转场；导航 Dock 使用 Backdrop 玻璃组件。当前仍需目标真机验收玻璃采样、遮挡、安全区、图标视觉平衡与切页连续性。详细视觉基准见 [CALCULATOR_NATIVE_UI_DESIGN.md](./CALCULATOR_NATIVE_UI_DESIGN.md) 第 9–11 节。


### 11.1 C4.2.3 有理函数求值首步（2026-11-08）

已提交单点求值入口 `RationalFunctionArithmetic.evaluate(function, x)`：先检查全部原始定义域限制，再用 Horner 求值分子与分母，拒绝非有限输入 / 中间值 / 最终商，并保留 `x/x` 在零点的原式定义域空洞。新增测试覆盖普通值、继承限制、零次幂、非有限输入和溢出。该提交仍需 GitHub Actions 验证；CI 只编译 androidTest 源码，不等于启动模拟器或真机验收。

### 11.2 C4.2.3 精确零点判定验收（2026-11-08）

`PolynomialArithmetic.isExactlyZeroAt` 使用 BigInteger 精确处理有限 Double 的二进制有理数，避免 epsilon 误判，也避免 Double Horner 消去误差把非零多项式值错误判为零。Build #379（Run ID `42454420363`）成功，JUnit、androidTest 源码编译、Debug APK 组装与 Artifact 上传均通过；未运行模拟器 / 真机。下一小步是区分分母真实为零与 Double 求值舍入为零。

### 11.3 C4.2.3 分母精度损失分类验收（2026-11-08）

新增 `NUMERIC_PRECISION_LOSS`：分母 Double Horner 结果为零、但 BigInteger 精确二进制求值证明其非零时，返回数值精度错误。测试覆盖 `x²` 在 `1e-200` 处下溢以及高消去多项式。Build #381（Run ID `42454900909`）成功，资源检查、JUnit、androidTest 源码编译、Debug APK 组装和 Artifact 上传全部通过；未运行模拟器 / 真机。C4.2.3 的定义域、精确零点、分子 / 分母精度保护、非有限与溢出边界均已完成 CI 验收（Build #372 / #379 / #381 / #383）；设备端验收仍未执行。C4.3.1 的一次根首步已通过 Build #388（Run ID `42456084109`），下一步处理二次多项式实根。


### 11.4 C4.2.3 分子精度损失保护（2026-11-08）

已在 `RationalFunctionArithmetic.evaluate` 中补充分子数值精度检查：当分子 Double Horner 结果为 `0.0`，但 `PolynomialArithmetic.isExactlyZeroAt` 证明精确二进制值非零时，返回 `NUMERIC_PRECISION_LOSS`；真正精确为零的分子仍返回数值 0。测试覆盖 `x²` 在 `1e-200` 处下溢、高消去多项式以及真正零分子。

代码提交 `924f4958c2132b09ff016938ba4070c099c18a52`。Android Build #383（Run ID `42455402733`）已完整成功：JUnit、androidTest 源码编译、Debug APK 组装与 Artifact 上传通过；Artifact ID `13607311903`，12,421,616 bytes。该步已通过 CI，但未执行模拟器 / 真机测试。C4.2.3 已完成 CI 整体验收；当前进入 C4.3.1 / C4.3.2，设备端测试仍待单独执行。


### 11.5 C4.3.1 零 / 常数 / 一次多项式实根（2026-11-08）

已新增 `PolynomialRootsResult` 根集模型，并在 `PolynomialArithmetic.solveRealRoots` 中支持零多项式（全体实数）、非零常数（无实根）和一次多项式。有限根携带归一化残差；根溢出返回 `NON_FINITE_RESULT`，非零根下溢或残差检查失败返回 `NUMERIC_PRECISION_LOSS`；二次及以上目前返回 `UnsupportedDegree`。

首版提交 `860429b699492d0471cf4ac5deb53e7906b0440c` 在 Build #387 中暴露出新错误码漏加 exhaustive `when` 映射，已在 `c6f59062cd89765d4affa3fa4dd1952ffed8c332` 修复。Build #388（Run ID `42456084109`）完整成功；Artifact ID `13606854310`，12,423,470 bytes。未执行模拟器 / 真机测试。二次多项式实根已由后续 C4.3.2 完成，详见 11.6。

### 11.6 C4.3.2 二次多项式实根（2026-11-09）

已在 `PolynomialArithmetic.solveRealRoots` 中扩展二次实根求解，复用现有 `PolynomialRootsResult` / `PolynomialError` 类型模型。判别式符号由 BigInteger 精确二进制有理数计算，不使用 epsilon；中间值使用二进制缩放表示，正判别式采用稳定 q 公式，根由 `q/a` 与 `c/q` 得到；重根只返回一个根，无实根返回 `NoRealRoots`。每个有限根均检查精确归一化残差；溢出返回 `NON_FINITE_RESULT`，下溢 / 精度不足返回 `NUMERIC_PRECISION_LOSS`。

JUnit 覆盖两个不同实根、`x²-1e16x+1` 稳定小根、重根、无实根、判别式近抵消正负两侧、根溢出 / 下溢和三次输入仍不支持。首版提交 `9a955bd2b3b61fb9fdc4980b14ebf9abd6ceb886` 的 Build #390（Run ID `42544038191`）因重复声明 `ROOT_RESIDUAL_TOLERANCE` / `rootsFailure` 失败；读取日志后在 `9db9d21c9506b401959654bc6ecefc3ec7c043ff` 删除重复定义并复用原有辅助函数。没有删减有效断言。Build #391（Run ID `42544184281`）完整成功，Artifact ID `13642063693`，12,428,103 bytes，SHA-256 `e3708be725e4f8adced35b6c09801c4e150a3ce686dba559111242440510cc3f`。

本步已通过源码提交、CI / JUnit、androidTest 源码编译、Debug APK 组装与 Artifact 上传；未启动模拟器，也未进行真机验收。下一阶段 C5 方程求解，先建立方程结果模型与常数 / 一次方程基础测试。

### 11.7 C5.1 常数 / 一次多项式方程（2026-11-13）

已增加 `PolynomialEquation(left, right)` 与 `EquationSolutionResult`，并实现 `PolynomialEquationSolver.solve`。求解器内部先计算左侧减右侧，再把零多项式映射为恒等式、非零常数映射为无解、一次式复用已验证根引擎；有限解保留归一化残差。当前明确只支持到一次，二次及以上返回 `UnsupportedDegree`；分式方程的原始定义域限制与增根剔除留到 C5.4。

提交 `9fe617a3ad1283341bde78b80d56fb775c2615bf` 的 Build #393（Run ID `43394051873`）完整成功：JUnit、androidTest 源码编译、Debug APK 组装和 Artifact 上传通过。Artifact ID `14022361217`，12,434,313 bytes，SHA-256 `322d8591154a23e71bbb9b62a7d8a6ffd71cd888a503224c81ff5805b6fe3d84`。未执行模拟器 / 真机测试。下一步 C5.2 为二次方程层接入。


### 11.8 C5.2 二次多项式方程（2026-11-13）

已把 `PolynomialEquationSolver` 的支持次数扩展到二次，直接复用 C4.3.2 `PolynomialArithmetic.solveRealRoots`。方程层回归覆盖两个不同实根、重根、无实根、`x²-10¹⁶x+1=0` 的稳定小根、底层数值错误码透传，并确认三次仍返回 `UnsupportedDegree(3)`。

Build #395（Run ID `43394483739`）暴露旧 C5.1 测试仍要求二次不支持；修正提交 `3123bc6cf72c9a93f2933df894dd8dfe1ac7a29e` 把旧断言收窄为三次暂缓。Build #396（Run ID `43394734621`）完整成功，JUnit、androidTest 源码编译、Debug APK 组装和 Artifact 上传通过；Artifact ID `14022780460`，12,434,313 bytes。未执行模拟器 / 真机测试。下一步 C5.3 为三次方程。


### 11.9 C5.3 三次多项式方程与 C5.4 分式方程（2026-11-13）

三次多项式方程已在 `PolynomialArithmetic.solveRealRoots` 与 `PolynomialEquationSolver` 中开放。求根分支以精确二进制判别式分类，缩放 Cardano 处理一个实根，三角函数分支处理三个不同实根，精确系数关系识别二重根 / 三重根；最终结果经过可表示性、根区分与归一化残差检查。方程层当前最高支持三次，四次及以上明确暂缓。

分式方程使用新 `RationalEquation` / `RationalEquationSolver`。左右有理函数相减时继承双方所有原始定义域限制；求出候选根后，逐一使用 BigInteger 精确二进制零点判断排除原式分母为零的点。恒等式不会被误报成无条件的全体实数，而是携带定义域限制返回。

源码提交 `933ba61f47ae4f40a138ff653ba4428f9e0b12f4`；首轮 Build #399 发现的唯一失败是旧测试仍断言三次不支持，修正提交 `e51fd565b8333e06aae8f88215ae9dca1f9bc5cb` 只更新为四次边界。Build #400（Run ID `43397103889`）全部 JUnit、androidTest 源码编译、Debug APK 组装与 Artifact 上传成功；Artifact ID `14022809017`，12,440,699 bytes。未执行模拟器 / 真机仪器测试。

接续顺序：C5.5 绝对值方程、C5.6 根式方程、C5.7 指数 / 对数方程。后续步骤保持小步推进，单测 / 代码检查不强制伴随 APK 构建。


### 11.10 C5.5 线性内部绝对值方程（2026-11-13）

新增 `AbsoluteValueEquation` / `AbsoluteValueEquationSolver`，求解范围限定为 `|ax+b|=c`：内部为常数或一次多项式，右侧为有限常数。负目标直接无解；常数内部表达式分类恒等式 / 矛盾式；一次内部表达式拆分成两条一次方程求解。零目标只计算一支，避免重复根。测试覆盖两根、零目标单根、负目标、常数恒等 / 无解、二次内部式暂缓及 NaN 输入。

提交 `4d26a8d8b2d5e0c37454272bcf8af65cdf980cb1`；Android Build #401（Run ID `43398026099`）完整成功，资源检查、JUnit、androidTest 源码编译、Debug APK 组装和 Artifact 上传均通过；Artifact ID `14023737491`，12,443,101 bytes。未执行模拟器 / 真机仪器测试。变量右侧、多重 / 嵌套绝对值及高次内部式尚未实现。

下一步为 C5.6 根式方程，再处理 C5.7 指数 / 对数方程。


### 11.11 C5.6 平方根方程（2026-11-13）

新增 `RadicalEquation` / `RadicalEquationSolver`，当前支持 `√P(x)=Q(x)`，其中 P 最多二次、Q 最多一次。求解器先平方得到候选多项式 P−Q²，但不会直接把平方后的根作为答案；每个候选根都通过精确二进制有理数符号计算检查被开方式 P 与右侧 Q 均非负，再计算原根式并检查归一化残差。`P−Q²` 为零多项式时，结果会返回 `AllRealNumbersOnDomain` 和非负条件列表，例如 `√(x²)=x` 保留 `x≥0` 条件。

测试覆盖平方增根、负右侧候选、原式代回、受限恒等式、边界根、全体实数恒等式、次数暂缓与平方溢出。提交 `9cc09a1e049ba9bb177aefb429adcd83127e872e`；Build #402（Run ID `43398997674`）完整成功，资源检查、JUnit、androidTest 源码编译、Debug APK 组装和 Artifact 上传均通过；Artifact ID `14023194525`，12,448,771 bytes。未执行模拟器 / 真机仪器测试。立方根、多重 / 嵌套根式和高次结构仍未支持。

下一步为 C5.7 指数 / 对数方程。


### 11.12 C5.7 受限指数与对数方程（2026-11-13）

本步新增 `ExponentialLogarithmicEquation` / `ExponentialLogarithmicEquationSolver`，当前支持四种形式：`a^P(x)=c`、`a^P(x)=b^Q(x)`、`log_a(P(x))=c`、同底 `log_a(P(x))=log_a(Q(x))`。所有变量指数 / 真数均限定为常数或一次多项式；对数底数必须大于零且不等于 1，所有真数必须严格大于零。指数式底数必须大于零，指数式等于常数时右侧必须为正。

求解使用自然对数换底后得到线性候选方程。有限候选根会回代检查原指数 / 对数式；同底对数逐个精确判断两侧真数的符号。常数对数目标的真数若超出 Double 正数范围，或过于接近 1 以至于无法区分，则返回类型化数值精度错误。对数恒等式保留严格正值条件；指数恒等式要求结构上可证明，近似系数相等而无法证明的情况不会直接报告全体实数。

测试覆盖常底指数式、不同底换底、底数小于 / 等于 1、常数指数恒等 / 矛盾、对数等于常数、同底对数、真数零点剔除、严格正值域保留、非法底数、目标真数溢出与近 1 精度边界、高次结构暂缓，共 22 个测试场景。

功能提交 `88a9d18f8603d0388223e2196d0b8189b53e9692`；Build #403（Run ID `43400220986`）完整 success，JUnit、androidTest 源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过，Artifact ID `14023699329`，12,465,463 bytes。未执行模拟器 / 真机仪器测试。

下一阶段进入 C6：一至三次多项式不等式、分式不等式、符号区间、端点开闭与原式定义域边界。


### 11.13 C6.1 一至三次多项式不等式（2026-11-13）

新增纯 Kotlin `PolynomialInequality` / `PolynomialInequalitySolver`，把 `P(x)<0`、`P(x)≤0`、`P(x)>0`、`P(x)≥0` 转为实数轴上的符号区间解集，最高支持三次多项式。

求解器复用现有一次至三次实根引擎确定边界；相邻有限根之间取溢出安全的中点，使用 `PolynomialArithmetic.signAt` 精确判断符号；两端无界区间根据首项系数符号和次数奇偶确定。因此不能简单按根的个数轮流变号，二重根等偶数重根两侧仍可保持同号。严格不等式不含零点，非严格不等式含零点；结果按照端点是否闭合决定是否合并相邻区间。若浮点分辨率不足以在根之间找到可靠采样点，返回 `NUMERIC_PRECISION_LOSS`。

JUnit 新增 14 个场景，覆盖一次端点、二次两外区间 / 中间区间、无实根恒正、双根不变号与单点解、三次三个简单根、三次双根 + 单根、三重根、常数 / 零多项式及四次暂缓。实现提交 `f23400eea148776895f7fccfac0c5c262e46ab29`；Android Build #404（Run ID `43401527163`）完整 success：JUnit、androidTest 源码编译、Debug APK 组装、Artifact 上传及 Actions 清理全部通过，Artifact ID `14025750621`，12,472,155 bytes。未执行模拟器 / 真机仪器测试。当前尚未处理分式不等式及原式分母限制。

下一步 C7 函数工具内核：从坐标点集模型、输入校验和一次 / 二次函数拟合开始，之后再扩展三次 / 反比例拟合与图像采样。


### 11.14 C6.2 分式不等式（2026-11-13）

新增纯 Kotlin `RationalFunctionInequality` / `RationalFunctionInequalitySolver`。输入复用已有 `RationalFunction`，不等式为有理式与 0 的 `<`、`≤`、`>`、`≥` 比较。当前分子、分母及所有原式定义域限制的次数均不超过三次；更高次数明确暂缓。

求解器分别收集分子零点、分母零点和 `domainRestrictions` 的全部实根，形成统一实数轴分割。区间内按 `sign(N) * sign(D)` 判断符号，避免构造乘积多项式造成次数增加；无界区间根据次数奇偶与首项符号确定。分子零点仅在非严格比较且原式定义域允许时可取；当前分母零点、所有继承限制零点和除法时引入的限制零点始终排除。即使代数上可约分，空洞也作为开端点保留。

测试覆盖倒数正负半轴、分子零点开闭、分母零点排除、`(x²−1)/(x−1)`、`x/x`、分母二重根、继承定义域限制、除法新增限制、零分子、常数分式、无理公共零点 `±√2`、相邻根精度损失和四次边界，共 18 个测试方法。实现提交 `fd9c6ef1bf9266dd5ac7be3e86a18a1825cde60e`；Build #405（Run ID `43403410088`）完整 success：JUnit、androidTest 源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过；Artifact ID `14026112216`，12,478,669 bytes。未执行模拟器 / 真机仪器测试。

C7.1 坐标点模型 / 输入校验、C7.2 一次函数拟合和 C7.3 二次函数拟合已完成；下一步 C7.4 三次拟合，随后扩展反比例拟合及定义域安全图像采样。


### 11.15 C7.1 坐标点模型与输入校验（2026-11-13）

C7 函数工具内核已开始。新增纯 Kotlin `CoordinatePoint(x, y)` 和 `CoordinatePointSet`，点集在创建时复制输入，并以不可修改列表暴露，避免调用方在通过校验后更改数据。`CoordinatePointSetValidator` 按确定顺序检查：最少点数参数必须大于零、输入不能为空、所有 x/y 必须为有限 Double、所有点的 x 必须互异、最后检查点数是否达到当前拟合器要求。

重复横坐标按精确 Double 相等判定；`-0.0` 和 `0.0` 视为同一个 x，临近但不相等的 x 不会被误合并。重复 x 必须拒绝，因为本阶段目标是 `y=f(x)` 的函数拟合；多个不同 x 对应相同 y 仍是合法数据。校验结果会指出非有限值位于第几个点及 x/y 轴，或指出重复 x 的首个与重复点索引。最小点数由调用者提供，为下一步一次函数 / 二次函数拟合保留扩展空间。

新增 11 个 JUnit 方法。Android Build #406（Run ID `43404794011`）已完整通过：资源检查、全部 JVM 单元测试、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传和 Actions 清理；Artifact ID `14027365163`，12,485,602 bytes。未执行模拟器 / 真机仪器测试，也尚未实现拟合或图像采样。

C7.2 一次函数拟合已完成并通过 Build #409；C7.3 二次函数拟合已通过 Build #412。


### 11.16 C7.2 一次函数拟合（2026-11-13）

新增纯 Kotlin `LinearFunction` / `LinearFunctionFitter`。模型以斜率与截距表示 `y=mx+b`。两个横坐标不同的点唯一确定一条插值直线；输入达到三个或更多点时，采用普通最小二乘，使所有点的纵向残差平方和最小。

拟合器先复用 C7.1 点集校验，要求至少两个有限点且横坐标唯一；`fit(List)` 与 `fit(CoordinatePointSet)` 对空输入、点数不足、重复 x 的结果一致。算法将 x、y 分别按最大绝对值缩放到安全范围，再用 Welford 在线中心矩方法累积方差 / 协方差，避免直接累加原始大数。恢复原始尺度时用 `BigDecimal` 中间值，避免仅仅因为先计算比例而产生临时溢出。不同坐标归一化后碰撞、横坐标方差不可分辨、系数下溢返回 `NUMERIC_PRECISION_LOSS`；斜率或截距真正超出 Double 表示范围返回 `NON_FINITE_RESULT`，不把错误系数当成功结果。

新增 13 个 JUnit 方法，覆盖两点插值、多点共线、带噪最小二乘、水平线、零函数、负坐标、乱序输入、空列表 / 空点集、点数不足、重复 x、极端对称坐标、相邻可表示坐标、归一化碰撞与斜率溢出。首次 Build #407 暴露 1 项错误分类断言，按日志区分溢出与精度下溢后 Build #408 完整通过；空点集语义统一后，最终 Build #409（Run ID `43410045585`）完整 success：全部 JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传及 Actions 清理通过。Artifact ID `14029077035`，12,491,526 bytes。未执行模拟器 / 真机仪器测试。

下一步 C7.3：二次函数拟合。先区分三点插值与多点最小二乘，并重点处理 x 分布退化、矩阵条件数与数值精度。


### 11.17 C7.3 二次函数拟合（2026-11-13）

新增纯 Kotlin `QuadraticFunction` / `QuadraticFunctionFitter`，输出 `y=ax²+bx+c`。继续复用 C7.1 坐标点校验，至少需要三个有限点且横坐标互异；三点决定唯一插值函数，四点及以上采用普通最小二乘。

算法将横坐标缩放、中心化到安全范围，纵坐标同步归一化，构造 `[t²,t,1]` 设计矩阵，并用列主元 Householder QR 求解，不构造正规方程。列主元和相对秩阈值识别退化点集；恢复原始 a/b/c 时使用 BigDecimal DECIMAL128，区分精度损失和真正系数溢出。极端 x 尺度下增加精确共线性识别，并用精确截距分子再除以 Δx，避免极大坐标相消丢失低位。

新增 15 个 JUnit 测试方法，覆盖三点插值、多点共线 / 精确二次 / 带噪 OLS、负坐标与乱序输入、零 / 常值函数、输入边界、归一化精度和系数溢出。Build #411 暴露一项极端对称数据截距断言失败，按日志修复后最终提交 `e073b6ef750df72b2831e0cdd24459c17476129f`；Build #412（Run ID `43411748705`）完整成功：资源检查、全部 JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过。Artifact ID `14029827428`，12,503,320 bytes。未执行模拟器 / 真机仪器测试，androidTest 仅源码编译。

下一步 C7.4 三次函数拟合，仍先在纯 Kotlin domain 内实现，不接函数图像 UI。
