# 知行 Android App 项目现状、操作指南与后续开发手册

> **核对日期：2026-10-31（UTC）**  
> **仓库：** PaleSquire71029/zhixingapp  
> **分支：** main  
> **文档目的：** 让新对话、开发者或未来的维护者不依赖聊天记录，也能准确知道项目做到哪里、哪些只是代码落地、如何构建 APK，以及下一步应该怎么做。

---

## 1. 先看结论

目前项目已经从空仓库变成一个**原生 Android + Kotlin + Jetpack Compose 的最小可启动工程骨架**。用户提供的蓝紫渐变知行图标已经进入 App 资源，并配置为应用图标和首页品牌标识。首页具有四个底部导航入口，系统栏和键盘窗口的基础适配代码也已经写入。

但目前还**不是一个能完成计算的计算器 App**。四个入口主要是占位页面，尚未迁移旧计算器算法、数学键盘、AI 对话、账号、云端历史、函数图像、化学工具或真实液态玻璃折射效果。

必须区分三种状态：

- **已实现并提交：** 仓库中能找到对应代码或资源。
- **代码已提交，待验证：** 代码已经写入，但尚无成功构建、自动化测试或目标真机结果作为依据。
- **尚未开始：** 只有规划或未来步骤，没有对应实现。

不要把“提交成功”“GitHub Actions 已启动”“模拟器能打开”混写成“功能验收通过”。

## 2. 当前仓库与项目配置

### 2.1 仓库

- GitHub：<https://github.com/PaleSquire71029/zhixingapp>
- 默认分支：`main`
- Android applicationId：`ci.us.zhixing.app`
- App 显示名称：`知行`
- 当前版本：`0.1.0`，versionCode 为 `1`
- 当前 Gradle 工程只有一个 `:app` 模块；暂时不拆成很多独立 Gradle module，避免早期维护成本。

### 2.2 已核对的构建配置

| 项目 | 当前值 | 说明 |
|---|---|---|
| 语言 | Kotlin | 原生 Android |
| UI | Jetpack Compose + Material 3 | 组件样式由知行自行设计 |
| Android Gradle Plugin | 9.1.1 | 根目录 build.gradle.kts |
| Gradle Wrapper | 9.3.1 | gradle/wrapper/gradle-wrapper.properties |
| Kotlin Compose 插件 | 2.2.10 | 根目录 build.gradle.kts |
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
| `app/src/main/java/ci/us/zhixing/app/MainActivity.kt` | 当前主界面、主题、系统栏基础设置 | 文件目前偏集中，设计系统阶段再拆分 |
| `app/src/main/java/ci/us/zhixing/app/navigation/AppDestination.kt` | 四个底部导航目的地及占位描述 | 未来与真实页面路由连接 |
| `app/src/test/java/ci/us/zhixing/app/navigation/AppDestinationTest.kt` | 导航数据契约的 JVM 单元测试 | 未来逐步增加状态和导航测试 |
| `app/src/main/res/drawable-nodpi/zhixing_logo.webp` | 用户提供的蓝紫渐变图标 | 不随意裁切、重绘或改变比例 |
| `app/src/main/res/drawable-nodpi/zhixing_logo.webp` 与 launcher mipmap XML | 品牌图片与自适应桌面图标资源 | 检查不同启动器遮罩效果 |
| `.github/workflows/android-build.yml` | 自动测试、打包和上传 Debug APK | 每次主分支 push 会触发 |

品牌图标具体资源文件名以仓库实际文件为准；不要把旧网页的书本图标或其他项目图标替换进来。

## 3. 已经做了什么

### 3.1 原生工程骨架

**状态：已实现并提交。**

- 已建立 Android Gradle 工程。
- 有单一 `:app` 模块和启动 Activity。
- Kotlin 与 Jetpack Compose 已接入。
- 建立四个主入口：计算、知行AI、工具、历史。
- 底部导航可以切换当前选中目的地。
- 当前页面内容从导航目的地数据读取，属于演示占位，不是业务功能。

相关文件：

- `app/src/main/java/ci/us/zhixing/app/MainActivity.kt`
- `app/src/main/java/ci/us/zhixing/app/navigation/AppDestination.kt`

### 3.2 知行品牌图标

**状态：已实现并提交。**

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
- 已实际完成一次完整 CI：Android Build #15（Run ID 41206049511）结果为 success；导航单元测试、assembleDebug 和 Artifact 上传步骤均成功。
- Artifact 名称：`zhixingapp-debug`；APK 名称：`app-debug.apk`；APK 大小：11,491,233 bytes。
- APK SHA-256：`218fe484bffe249e39b02006184512f4a4a837ee37809b527aed712b0fde7785`。
- [查看本次成功构建](https://github.com/PaleSquire71029/zhixingapp/actions/runs/41206049511)。
- 这只证明构建与当前自动化测试成功，不证明真实设备的安装、渲染和交互已经验收。

## 4. 明确还没有做什么

下面这些能力目前**没有完成原生迁移**，不能在测试 APK 中期待它们已经能用。

### 4.1 计算器核心

- [ ] 可输入并计算完整表达式
- [ ] 自定义数字与数学键盘
- [ ] 科学计算和常用函数
- [ ] 幂、负号与运算符优先级边界
- [ ] 有理数与精确数值处理
- [ ] 方程求解
- [ ] 不等式求解与分母零点分支
- [ ] 多项式符号区间
- [ ] 线性方程组
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

### 5.2 如何在 GitHub 手机上找到 APK

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

本次已成功生成首份 APK。GitHub Actions Artifact 中的 `app-debug.apk` 与本会话提供的预览副本内容一致，SHA-256 为 `218fe484bffe249e39b02006184512f4a4a837ee37809b527aed712b0fde7785`。长期获取时请从对应 Actions 运行下载 Artifact，并按上面的 ZIP 解压步骤取得 APK。

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

每个小步骤都要独立提交、执行构建，并更新 `docs/IMPLEMENTATION_STATUS.md`。不要跳过前面的构建门槛，也不要把一轮塞进多个大功能。

### 第 0 步：确认当前基线能构建

**状态：CI 已通过；真机安装待验收。**

**目标：** 先让最小工程通过 CI，并产出第一份预览 APK。GitHub Actions Run #15 已完成这一部分，真实手机安装与界面验收仍待进行。

操作：

1. 打开 GitHub Actions 的最新 Android Build 运行；
2. 检查 API 36 / Compose BOM 2026.06.00 是否成功解析依赖；
3. 确认两个导航测试通过；
4. 确认 Debug APK 成功生成并上传；
5. 在至少一台 Android 真机安装；
6. 截图记录首页、四个导航、系统状态栏和底部区域。

完成条件：CI 绿灯、有 Debug Artifact、真机可安装启动；系统栏无严重遮挡。未完成前不引入新依赖。

### 第 1 步：建立知行设计 Tokens

**目标：** 让颜色、字体、圆角和间距有单一来源。

建议新增：

- `ui/theme/ColorTokens.kt`
- `ui/theme/ShapeTokens.kt`
- `ui/theme/SpacingTokens.kt`
- `ui/theme/TypeTokens.kt`
- `ui/theme/ZhixingTheme.kt`

操作指引：

1. 从现有 `ZhixingLightColors` 抽取颜色，先保持现有界面外观，不趁机大改页面；
2. 把常用圆角、间距提取为常量；
3. 定义中文正文、标题、辅助文字和等宽数字的字体角色；
4. 字体先用可靠系统回退，不要直接把 22.7 MB 的 OPPO Sans 完整文件打进 APK；
5. 主 Activity 只保留调用 `ZhixingTheme`，不再自己定义整套颜色。

完成条件：外观没有意外变化；首页和导航测试通过；所有页面只从统一 Tokens 读取基础样式。

### 第 2 步：Glass Lab 静态实验页

**目标：** 只验证实验入口和基础构图，不马上做折射。

操作：

1. 新建独立实验页面，不覆盖主首页；
2. 添加一块浅色画布和一个半透明样本容器；
3. 放置简单彩色背景图形，便于观察后续采样；
4. 增加关闭/返回入口；
5. 明确标注实验页面，不让实验效果冒充已经完成的正式组件。

完成条件：可以进入和退出实验页；主导航仍正常；没有深色面板或复杂动画。

### 第 3 步：背景采样和模糊

**目标：** 分开验证材质，而不是把模糊、折射和色散一次性叠加。

操作顺序：

1. 先验证玻璃容器能否正确采样它后面的内容；
2. 再增加单一 blur 参数；
3. 对比不同半径下的文字可读性；
4. 检查滚动、裁切、圆角边缘和系统栏；
5. 记录目标设备 GPU 帧时间和温度变化。

完成条件：采样区域正确、文字清晰、滚动不闪烁；无特效降级时仍能清楚显示。

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

**一句话交接：现在有原生 Android 项目骨架、品牌图标、四个占位导航、浅色基础主题、Gradle Wrapper、系统栏与键盘窗口适配代码以及 CI 工作流；没有完整计算器、AI、化学、历史、账号、云同步或真实液态玻璃效果。下一件事是先确认 CI 产出 Debug APK 并在真机安装，再抽取设计 Tokens。**
