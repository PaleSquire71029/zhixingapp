# 知行 Android App 实施状态

> 最后更新：2026-10-31  
> 当前仓库：PaleSquire71029/zhixingapp  
> 当前分支：main  
> 总规划：[docs/ANDROID_APP_MASTER_PLAN.md](./ANDROID_APP_MASTER_PLAN.md)

本文件是项目状态账本。每次开发结束必须更新，不能只在聊天中报告进度。

---

## 1. 当前项目状态

**规划已保存，工程尚未开始。**

| 项目 | 状态 | 说明 |
|---|---|---|
| 新建 App 仓库 | 已完成 | zhixingapp 已建立，默认分支 main |
| 仓库审计 | 已完成（2026-10-31） | 检查 calculator、zhixing_ai、zhixingapp |
| 旧 AI 交接文档 | 已阅读 | 已阅读项目总览和遗漏需求补充 |
| Android 技术路线 | 已规划 | Kotlin + Jetpack Compose |
| 液态玻璃候选 | 已初步调查 | Backdrop 第一候选；Prismal / QWEA0 为辅助研究对象 |
| 数学公式候选 | 已初步调查 | huarangmeng/latex 第一候选测试对象；RaTeX-CMP 备选 |
| App 主体设计 | 已规划 | 四个入口：计算、知行AI、工具、历史 |
| App 工程 | 未开始 | 当前还没有 Gradle 工程 |
| Glass Lab | 未开始 | 尚未编写 Android Shader |
| 原生 UI | 未开始 | 尚未建立 Compose 页面 |
| 计算迁移 | 未开始 | 尚未迁移 Kotlin 算法 |
| AI 接入 | 未开始 | 尚未对接原生客户端 |
| 账号与云历史 | 未开始 | 尚未编写原生认证与历史层 |
| APK / AAB | 未开始 | 尚未构建 |

## 2. 本次已完成的规划工作

- [x] 确认 zhixingapp 的仓库、默认分支和当前文件结构。
- [x] 确认新仓库当前没有 Android 工程，只存在 LICENSE。
- [x] 检查 calculator 的计算器、AI 计算器、化学算法、测试、Worker 和 SQL。
- [x] 检查 zhixing_ai 的聊天页面、云历史、账号认证、后台和 docs。
- [x] 阅读知行AI项目总览文档。
- [x] 阅读知行AI遗漏需求补充文档。
- [x] 记录普通计算器不强制登录、全浅色、原生 UI、逐步迁移等硬约束。
- [x] 初步检查 AGSL、液态玻璃和原生公式渲染开源候选。
- [x] 将详细主规划写入 docs/ANDROID_APP_MASTER_PLAN.md。

规划文档提交：

- Commit：f9b17884a28b10bda1f837933ad216f7cd24ee6d
- 文件：docs/ANDROID_APP_MASTER_PLAN.md
- 变更：新增详细 Android App 主规划
- 代码实现：无

## 3. 下一步执行顺序

### 阶段 0：迁移前核验

- [ ] 重新检查旧仓库最新版本和 SHA。
- [ ] 建立数学表达式测试集。
- [ ] 建立方程、不等式和函数图像测试集。
- [ ] 将化学配平测试样例转为可复用数据。
- [ ] 整理化学反应预测测试样例和条件边界。
- [ ] 检查线上 Worker 主机和当前部署版本。
- [ ] 修正并验证 Worker 的 calculator-ai app_id 分流问题。
- [ ] 检查云历史 SQL 的 app_id 约束和 RLS。
- [ ] 核对用户名登录规则。
- [ ] 核对字体授权和实际体积。

### 阶段 1：原生工程骨架

- [ ] 初始化 Kotlin + Jetpack Compose 项目。
- [ ] 添加 Gradle Wrapper 和 Version Catalog。
- [ ] 建立 app / core / domain / feature 基础目录。
- [ ] 配置基础浅色设计系统。
- [ ] 建立最小 Compose 主界面和导航。
- [ ] 添加 GitHub Actions 构建与测试。
- [ ] 真机安装和启动验证。

### 阶段 2：Liquid Glass Lab

- [ ] 试接 Backdrop。
- [ ] 对比自研 RuntimeShader 最小样例。
- [ ] 实现背景采样。
- [ ] 实现边缘折射。
- [ ] 实现高光和受控色散。
- [ ] 实现触摸形变。
- [ ] 实现 Lite / Off 降级。
- [ ] 在目标手机验证帧率、温度、GPU 和耗电。

### 阶段 3：设计系统与主导航

- [ ] 确定色彩变量和字体规范。
- [ ] 实现 GlassSurface 抽象。
- [ ] 实现 GlassNavigationBar。
- [ ] 实现工具栏、按钮、输入表面和弹窗。
- [ ] 实现四个主入口页面骨架。
- [ ] 验证小屏、键盘、系统返回和无障碍。

### 阶段 4：计算功能迁移

- [ ] 表达式解析与求值。
- [ ] 基础 / 科学计算。
- [ ] 方程求解。
- [ ] 不等式求解。
- [ ] 函数绘图。
- [ ] 单位换算。
- [ ] 化学方程式配平。
- [ ] 化学反应预测。
- [ ] Room 本地历史。
- [ ] 自定义数学键盘。
- [ ] JSON 历史导入 / 导出。

### 阶段 5：知行AI

- [ ] 原生聊天 UI。
- [ ] Worker 健康检查。
- [ ] 模型列表。
- [ ] SSE 流式对话。
- [ ] 停止、取消、重试和错误恢复。
- [ ] Markdown 与 LaTeX。
- [ ] 图片与文件输入。
- [ ] 计算器上下文传递。
- [ ] 结论优先、详情折叠。

### 阶段 6：账号与数据同步

- [ ] 原生登录。
- [ ] Keystore 加密保存会话 Token。
- [ ] Access Token 刷新。
- [ ] 登出与会话过期处理。
- [ ] Worker 的 calculator-ai 场景支持。
- [ ] Supabase app_id 数据库迁移。
- [ ] 本地和云端最近对话合并。
- [ ] 消息幂等去重。
- [ ] 90 天保留策略验证。
- [ ] 多账号数据隔离测试。

### 阶段 7：正式发布准备

- [ ] API 版本兼容矩阵。
- [ ] 多台 Android 真机测试。
- [ ] GPU / 帧率 / 温度 / 内存测试。
- [ ] 安全和隐私审计。
- [ ] 依赖许可证审计。
- [ ] Secrets 扫描。
- [ ] Debug APK 构建。
- [ ] Release APK / AAB 构建。
- [ ] 安装包签名验证。
- [ ] 发布说明与已知限制。

---

## 4. 必须优先处理的阻断项

### A. Worker 产品 ID 分流

旧 Worker 中 normalizeAppId 当前只保留 zhixing-ai，其余场景映射成 learning；但计算器 AI 前端发送 calculator-ai。接入前必须修复并测试，不应在客户端绕过。

状态：**待修复 / 待验证**

### B. 云历史数据库约束

旧 SQL 的 app_id 仅允许 zhixing-ai 和 learning。若新 App 要独立同步 calculator-ai，需要单独数据库迁移，并测试 RLS、旧数据兼容和会话隔离。

状态：**待设计 / 待迁移**

### C. Native Android 网络认证

旧网页有 Cookie 行为。原生 App 应使用服务端验证的 Bearer Token 流程和安全本地存储，不可把 CORS、User-Agent 或客户端自报 user_id 当成身份认证。

状态：**待实现 / 待安全测试**

### D. 液态玻璃真实设备效果

AGSL RuntimeShader 需要 API 33+。API 33 以下必须优雅降级，不能用高成本 CPU 贴图冒充完整实时折射。

状态：**待真机实验**

### E. 原生公式渲染

先对照真实知行题目、AI 回复和化学方程式测试候选库，不可只依赖仓库 README 的命令覆盖数。

状态：**待评估**

---

## 5. 当前明确的产品决策

| 决策 | 当前值 |
|---|---|
| 平台 | 仅 Android |
| 语言 | Kotlin |
| UI | Jetpack Compose |
| 视觉 | 知行 Light Liquid |
| 主题 | 仅浅色 |
| 玻璃 | AGSL / RuntimeShader 为核心 |
| 玻璃候选 | Kyant0 Backdrop 第一候选 |
| 数学公式候选 | huarangmeng/latex 优先测试 |
| 导航 | 计算 / 知行AI / 工具 / 历史 |
| 迁移方式 | 全新原生 App，逐步迁移 |
| 普通计算登录 | 不强制 |
| AI 后端 | 继续使用受保护的 Worker |
| 本地数据 | Room |
| 云端 AI 历史 | Worker + Supabase |
| 管理后台 | 继续独立保护，不放入普通 App |
| 首版是否包含学习版 | 不包含，后续需单独确认 |

---

## 6. 开发记录模板

以后每次开发完成后追加记录：

### YYYY-MM-DD / 阶段 / 功能名称

- 目标：
- 修改文件：
- 旧项目参考：
- Commit SHA：
- 测试命令：
- 自动化测试结果：
- 真机与 Android API：
- UI / 性能验证：
- 安全检查：
- 未完成项：
- 是否允许进入下一阶段：

---

## 7. 维护要求

- 每次有实质性实现，都更新本文件。
- 如果主规划中的技术决策变化，先修改主规划，再继续实施。
- 未执行的测试保持未勾选。
- 只通过模拟器的测试不能记作真机通过。
- 不得把“已编写”记为“已验证”。
- 如果新对话继续项目，先读取本文件和主规划，再检查仓库当前 commit。
