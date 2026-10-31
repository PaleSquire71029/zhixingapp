# 知行 Android App 实施状态

> 最后更新：2026-10-31  
> 当前仓库：PaleSquire71029/zhixingapp  
> 当前分支：main  
> 总规划：[docs/ANDROID_APP_MASTER_PLAN.md](./ANDROID_APP_MASTER_PLAN.md)

本文件是项目状态账本。每次开发结束必须更新，不能只在聊天中报告进度。

---

## 1. 当前项目状态

**第 1 小步已提交：原生 Compose 工程骨架与最小首页。CI 构建结果待确认。**

| 项目 | 状态 | 说明 |
|---|---|---|
| 新建 App 仓库 | 已完成 | zhixingapp / main |
| 旧项目审计 | 已完成（2026-10-31） | 检查 calculator、zhixing_ai、zhixingapp |
| 旧 AI 交接文档 | 已阅读 | 项目总览、遗漏需求补充 |
| 技术路线 | 已确定 | Android 原生 Kotlin + Jetpack Compose |
| Android 工程配置 | 已提交 | AGP 9.1.1、Gradle 9.3.1、JDK 17 |
| 最小原生首页 | 已提交 | 四个主入口可切换，当前均为占位内容 |
| 浅色主题基线 | 已提交 | 仅浅色，不含深色主题 |
| 导航契约单元测试 | 已提交 | 四个入口、路由唯一性和文案完整性 |
| GitHub Actions 构建 | 已配置 | 首次工作流执行结果待核实 |
| Gradle Wrapper | 待补齐 | 当前 CI 使用 setup-gradle 安装 Gradle；后续补充标准 wrapper 文件 |
| 液态玻璃渲染 | 未开始 | 下一小步单独建立 Glass Lab |
| 计算迁移 | 未开始 | 尚无旧算法迁移 |
| AI 接入 | 未开始 | 尚无 Worker 客户端 |
| 账号与云历史 | 未开始 | 尚无原生认证与历史层 |
| APK / AAB | 待 CI | 工作流成功后可下载 Debug APK artifact |

## 2. 已完成提交

### 第 1 小步：原生 Compose 工程骨架

- 目标：让仓库成为一个清晰、最小、可持续扩展的原生 Android 项目。
- 文件：settings.gradle.kts、build.gradle.kts、gradle.properties、app/build.gradle.kts、AndroidManifest.xml、strings.xml、MainActivity.kt、AppDestination.kt、AppDestinationTest.kt、.gitignore、GitHub Actions workflow。
- 技术：AGP 9.1.1、Gradle 9.3.1、JDK 17、Compose BOM 2026.09.00、compileSdk/targetSdk 36、minSdk 26。
- UI：浅色知行首页，四个导航入口：计算、知行AI、工具、历史。此时页面只是原生骨架，不代表功能已迁移。
- 测试：配置两个导航契约单元测试。首次 CI 在 Android SDK 初始化阶段失败，原因是 setup-android@v3 请求已下架的 tools 包；尚未运行 Gradle 编译。
- Commit：`7d20f87ec4cbd34be3ad3e28eafacd427760116c`。
- 未完成：标准 Gradle Wrapper 文件尚未加入；需要在后续小步骤补齐。

## 3. 下一步拆分（一次只做一个小项）

### 第 2 小步：补齐 Gradle Wrapper

- [ ] 加入 wrapper properties、gradlew、gradlew.bat 和 wrapper JAR。
- [ ] 将 CI 切换为 ./gradlew。
- [ ] 验证 wrapper 与 Gradle 9.3.1 一致。

### 第 3 小步：整理系统窗口与安全区

- [ ] 设置浅色状态栏图标。
- [ ] 检查导航栏和手势区域的颜色。
- [ ] 处理键盘弹出时的页面高度。
- [ ] 不加入玻璃效果。

### 第 4 小步：把首页占位样式拆成设计组件

- [ ] 抽取知行颜色常量。
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

- [ ] 修正 setup-android 版本后，重新确认 GitHub Actions Android 构建成功。
- [ ] 标准 Gradle Wrapper 待补齐。
- [ ] 目标真机和 Android API 版本待确认/实测。
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
