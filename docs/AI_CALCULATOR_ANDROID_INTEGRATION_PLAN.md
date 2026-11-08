# 知行计算器 AI 原生接入方案

> 版本：0.1  
> 日期：2026-11-08  
> 仓库：`PaleSquire71029/zhixingapp`  
> 状态：架构与产品规划；未实现、未部署、未通过真机验收

## 1. 产品目标与边界

计算器 AI 是本地计算能力的解释层、复核层和扩展层，不是另一个要求用户重新输入题目的通用聊天框。

核心原则：

1. 本地计算优先。普通算式、方程、不等式和单位等本地能力即使离线也必须工作。
2. AI 主动携带上下文。用户不需要复制算式、结果、定义域或当前页面信息。
3. AI 不篡改本地结果。AI 输出用于解释、复核与扩展；若与本地结果不同，明确并列并提示核对，不静默替换本地计算结果。
4. 普通计算器不强制登录。不得为了接入 AI 把账号登录塞进普通计算器；网络 AI 单独应用限流与滥用控制。
5. 上游 API Key 永远留在 Cloudflare Worker Secrets / 服务端配置中，不放进 APK、BuildConfig、资源文件或客户端请求头。
6. AI 失败不能影响本地计算；断网、未配置、限流或模型不可用时，显示可恢复的错误状态。

## 2. 检查过的旧版能力与协议

参考代码：`PaleSquire71029/calculator` 的 `ai-calculator.html`、`ai-proxy/src/index.js`、`ai-proxy/README.md`。

旧版已经有以下适合迁移的产品能力：

- 基础计算、方程、不等式、函数 / 绘图、单位、化学等题型分别使用分析模式。
- 分离「AI 分析」与「AI 扩展」：前者复核当前问题和本地结果，后者尝试本地工具暂未覆盖的相关推导。
- 核心结论与详细分析分阶段生成：先请求 `analysisStage=core)，用户展开时再请求 `details`；后续追问使用 `followup`。
- 流式返回采用 SSE，前端逐段呈现；后端提示词定义了核心结论、推导依据、核验限制与扩展能力等输出职责。
- 上下文不是任意 HTML，而是有限字段的结构化文本：`title`、`view`、`inputs`、`outputs`、`details`、`selection`。
- Worker 对分析任务设置了题型模式、分析动作、上下文长度边界，并记录调用元数据；README 说明不会主动记录问题正文 / 回答正文，但内容仍会传给配置的模型服务商。

旧版网页的典型请求形态：

```json
{
  "app": "calculator-ai",
  "task": "analysis",
  "action": "analyze",
  "mode": "equation",
  "context": {
    "title": "方程求解",
    "view": "equation",
    "inputs": "原始方程：...",
    "outputs": "本地求解结果：...",
    "details": "定义域与中间步骤：..."
  },
  "analysisStage": "core",
  "stream": true
}
```

详情请求再发送 `analysisStage=details` 和刚才已经展示的 `coreAnswer`；追问请求使用 `analysisStage=followup` 与 `question`。这是协议示意，不代表 Android 客户端已经接通。

**需要特别确认的服务端现状：**旧 Worker 的 `normalizeAppId` 当前只将 `zhixing-ai` 映射到单独命名空间，其余值归为 `learning`，所以旧网页提交的 `calculator-ai` 会按当前代码映射到 `learning` 设置。迁移前必须检查部署 Worker / 后台提示词配置，决定是继续沿用共享设置，还是在服务端安全迁移配置后单独命名空间；不得在 Android 端擅自把它改成另一个 app ID。

## 3. Android 计算器内的交互形态

### 3.1 入口位置

- 在计算结果区域附近放一个轻量的「AI 分析」入口。
- 本地结果仍是视觉主层级；AI 分析入口不应抢过等号结果，也不能把每次输入都自动发送给 AI。
- 对支持扩展的题型，再提供一个次级动作，例如「AI 解复杂方程」「AI 深度函数分析」「AI 预测反应」。
- 用户点按后，在当前计算页面上打开原生 Compose 的上下文分析面板 / Bottom Sheet；不切走到独立聊天页面。
- 面板采用 App 已有的浅色玻璃体系，公式内容区域保持清晰、低模糊，避免折射影响数学可读性。

### 3.2 分阶段结果

1. 点击「AI 分析」后只请求核心结论，优先显示 1–4 句关键判断或主要结论。
2. 用户主动展开后，再流式请求详细步骤、适用条件、核验和扩展内容。
3. 底部提供当前问题的追问输入；追问自动引用同一个计算快照和前文 AI 结果。
4. 「AI 扩展」可单独启动，与基础分析分开标记，避免用户误以为扩展推导已由本地计算器验证。
5. 若题目 / 输入在请求过程中改变，当前回答标记为“基于上一版输入”，不覆盖新结果；可由用户重新分析。

### 3.3 题型模式

首批模式沿用旧 Worker 提示词契约：

| 本地页面 | Worker mode | 首批 AI 任务 |
|---|---|---|
| 基础算式 | `basic` | 检查优先级、百分数、负数与幂，补充化简 / 推导 |
| 方程 | `equation` | 复核定义域、增根，扩展复杂方程 |
| 不等式 | `inequality` | 复核分母零点、临界点、区间端点与符号方向 |
| 函数 / 绘图 | `fit` | 分析定义域、值域、奇偶性、单调性、零点、极值和图像 |
| 单位 | `units` | 量纲、复合单位和温度偏移换算 |
| 化学 | `chemistry` | 产物候选、条件、守恒、现象与离子方程式 |

知识讲解 / 历史复习可以留到后续，不放进首个 AI 面板版本。

## 4. 自动上下文设计

客户端建立不可变的 `CalculatorContextSnapshot`，每次点击时捕获：

- `calculationId`：本次本地计算的稳定 ID，仅客户端用于状态绑定，不必发送可识别个人的信息。
- `revision`：本次输入版本；输入发生变化即递增。
- `title / view`：页面名称与题型。
- `inputs`：原始表达式、方程、点集、单位 / 数值、化学反应物等。
- `outputs`：本地结果、精确值 / 近似值、解集或配平方程。
- `details`：必要的中间步骤、定义域约束、排除点、区间端点、单位和条件。
- `selection`：用户要求重点分析的某一项。

上下文必须是可读、有限、最小化的文本，不直接发送整个 Compose 树、屏幕截图、完整历史、账户资料或调试日志。第一版不自动上传图片；未来支持图片识题时，单独做大小限制、用户确认和隐私说明。

客户端应比 Worker 的既有限额更严格：按题型只发送必要字段；超长步骤摘要化；不把无关历史混进请求。对单次快照进行哈希或 revision 绑定，确保流式回答对应正确题目。

## 5. Android 技术分层

建议结构：

```text
domain/ai/
  model/
    CalculatorContextSnapshot.kt
    AnalysisRequest.kt
    AnalysisStage.kt
    AiStreamEvent.kt
  repository/
    CalculatorAiRepository.kt

data/ai/
  worker/
    WorkerAiDataSource.kt
    WorkerRequestSigner.kt
    SseEventParser.kt

ui/calculator/ai/
  CalculatorAiSheet.kt
  CalculatorAiViewModel.kt
  CalculatorAiUiState.kt
```

分工：

- `CalculatorAiRepository`：根据题型和快照构建任务，不依赖 Compose、Activity 或 Worker URL。
- `WorkerAiDataSource`：HTTP、超时、SSE 字节流、错误映射、取消和连接状态。
- `WorkerRequestSigner`：使用 Android Keystore 内不可导出的安装密钥生成签名；不保存静态共享密钥。
- `SseEventParser`：正确处理分块、UTF-8 跨块字符、CRLF / LF、多个 data 行、`[DONE]`、断流和取消。
- `CalculatorAiViewModel`：按计算 revision 管理请求状态，处理核心结论、详细分析和追问。
- `CalculatorAiSheet`：只负责展示，不直接拼装 Worker 请求或处理 API Key。

Android 当前 `AndroidManifest.xml` 尚未声明 `android.permission.INTERNET`；原生 AI 接入前需要在网络任务阶段增加最小必需网络权限，并为网络层添加超时、生命周期取消和失败测试。是否引入 OkHttp / Ktor 等依赖，应在实现时评估；首轮优先避免不必要的大型网络依赖。

## 6. Worker 原生请求安全：首个技术阻塞点

检查过的旧 Worker `/api/chat` 会强制要求请求携带且匹配允许列表中的网页 `Origin`；原生 Android HTTP 请求通常没有浏览器 Origin。不能为了让 App 连通就伪造网页 Origin，也不能删除 Origin 检查或把 Worker Key 暴露给 APK。

建议新增独立的移动端请求入口，不与浏览器 CORS 逻辑混用：

1. App 首次启用 AI 时，在 Android Keystore 生成 P-256 ECDSA 密钥对；私钥不可导出。
2. App 向 Worker 的移动端 bootstrap / challenge 路由提交公钥和客户端版本；Worker 对注册按 IP 限流，并使用一次性随机 challenge 防止简单重放。
3. Worker 将公钥与匿名安装 ID 绑定，发放短期安装会话 / 设备 ID；不要求用户账号登录。
4. 每次请求时，App 签署规范化内容：HTTP method、path、timestamp、nonce、request body SHA-256；Worker 校验签名、时钟窗口和 nonce 单次使用。
5. Worker 仍以 Cloudflare 看到的真实 IP 执行 IP 限流，并以安装 ID 执行设备限流；不能相信 App 自报 IP。
6. 注册和轮换都限频；支持吊销安装密钥、服务端限制单设备并发、每日总 Token / 成本上限，以及异常模型失败熔断。
7. 可在正式商店分发后再评估 Play Integrity；它不能替代服务端限流，且不应成为当前侧载 / 国内测试的唯一验证路径。

P-256 签名提高了密钥提取和简单脚本滥用门槛，但不等于能证明设备一定运行官方 APK。服务端仍必须按匿名、可滥用的公共接口设计。

**必须在移动端 Worker 路由部署前验证：** API 主机 / DNS / TLS、Cloudflare Durable Object 绑定、密钥轮换、无 Origin 请求、签名失败、重放、设备更换、IP 与设备限流、超时和成本阈值。旧网页的 `DEFAULT_PROXY` 指向 `workers.dev` 历史地址，不自动等于新 App 的正式地址；`api.zhixing.us.ci` 是否指向同一个 Worker 需要实测，不在 Android 源码中猜测。

## 7. 隐私与数据边界

- 第一次使用前明确说明：本次输入、计算结果和必要步骤会发送到知行 Worker，再由 Worker 转发给当前配置的模型服务商。
- AI 是用户主动操作，不在每次等号或键盘输入时自动请求。
- 提供本次关闭 / 不发送上下文入口；取消后中止客户端请求，并尽可能取消 Worker 上游流。
- Worker 统计只保留必要元数据（时间、场景、模型、状态、耗时、可用 Token 统计）；不把完整题目、AI 回答、图片写进请求日志。
- UI 不展示上游 API Key、内部模型服务密钥或 Worker 管理接口。
- 不要求普通计算登录；未来的账号能力不得影响本地计算的离线可用性。
- 隐私说明必须准确描述“Worker 会转发给当前模型服务商”；不能承诺第三方绝不记录或绝不用于训练，应以实际服务商政策为准。

## 8. 请求状态与故障体验

建议显式状态：

- `Idle`
- `LoadingCore`
- `CoreStreaming`
- `CoreReady`
- `LoadingDetails`
- `DetailsStreaming`
- `FollowupStreaming`
- `Cancelled`
- `Failed(retryable, userMessage)`

每个 UI 请求有唯一 request ID，并与 calculation revision 绑定。重复点击不创建重复请求；同一个流只允许一个消费者；成功、异常、超时、取消、页面离开、Activity 重建等路径都必须在 finally 中清理 loading 状态。不能出现“回答重复”或失败后残留“正在思考”。

错误应分为：网络离线、Worker 未配置、签名失败 / 会话过期、IP / 设备限流、上游 401 / 403、模型不存在、请求过大、服务商超时、流中断、响应格式错误。给用户的是可操作的中文提示，不回显密钥、Authorization 头或不可信上游 HTML。

## 9. 分阶段实施顺序

| 阶段 | 交付 | 验收门槛 |
|---|---|---|
| AI-0 | 核对 Worker 实际部署地址、`calculator-ai` 配置命名空间与提示词 | 在线健康检查、模型列表和现有网页分析实测；不改线上设置 |
| AI-1 | Worker 移动端 bootstrap / challenge / 签名校验 / IP 与设备限流 | 无 Origin 合法签名可请求；无效签名、过期、重放、超限全部拒绝 |
| AI-2 | Kotlin AI DTO、上下文快照、SSE Parser、请求签名器与 Repository | JVM 单测覆盖 JSON 字段、分块、断流、取消、重试、重复请求 |
| AI-3 | 计算结果旁的 AI 分析面板 | 核心结论首屏、详情展开、取消、追问、旧 revision 标记和错误恢复 |
| AI-4 | AI 扩展动作与题型提示词适配 | 基础算式 / 方程 / 不等式 / 函数 / 单位 / 化学六类场景验收 |
| AI-5 | 通用「知行 AI」Tab 与计算上下文跳转 | 普通聊天、会话状态、Markdown/LaTeX 和从计算器带上下文继续聊 |
| AI-6 | 图片识题与更丰富的结构化输出 | 明确授权、附件限额、错误处理和视觉 / 数学 / 化学回归测试 |

不把 AI-1 与 AI-2 混在同一提交；先让 Worker 的信任边界正确，再写客户端。AI-3 之前，计算器本地页面仍可独立使用，不因网络层未完成而阻塞。

## 10. 第一版界面示意（文字结构）

```text
┌──────────── 当前算式 / 方程 ────────────┐
│ 本地结果：x = 2                        │
│ [ AI 分析 ]        [ AI 扩展 ▾ ]        │
└───────────────────────────────────────┘

（用户点击 AI 分析后展开的原生底部面板）

┌──────────── 知行 AI 分析 ─────────────┐
│ 核心结论                               │
│ 方程有唯一实根 x = 2。                 │
│                                       │
│ [展开推导与核验]                      │
│                                       │
│ 追问当前结果…                   [发送] │
└───────────────────────────────────────┘
```

这是交互结构草案，不是当前 APK 中已存在的 UI。

## 11. 当前明确不做

- 不在 APK 中保存任何模型 API Key。
- 不给普通计算器增加强制账号登录。
- 不把全部计算历史自动发给 AI。
- 不在每次输入 / 计算时自动调用模型。
- 不让 AI 替换本地计算结果。
- 不用 WebView 承载 AI 页面。
- 不把 API 地址、模型 ID、用户上下文或管理凭据硬编码到密钥字段。
- 不在本地计算公式、定义域或方程 solver 尚未稳定时，让 AI 输出被当作精确算法结果。



## 12. 复杂题 AI 策略与高中题型覆盖修订（2026-11-08）

**重要产品决策：简单运算不接入 AI。**

- 简单算式、基础科学计算、已支持的常见本地计算全部走确定性 Kotlin 内核；不请求 Worker、不上传输入、不显示显眼 AI 提示。
- 只有本地复杂度规则识别为复杂、多条件综合、需要证明 / 文字建模，或 solver 明确标记 Unsupported / NeedsReasoning 时，才提供 AI 入口。
- AI 入口是可选动作；第一版必须由用户点击确认发送，不能因按等号或输入结束自动向云端发送。
- AI 需要面向复杂高中题给出题型识别、已知条件、思路、完整必要推导、结论、验算、易错点和条件讨论；用户要求简洁时缩短步骤。
- 学科优先级为高中数学（代数、方程、不等式、函数、三角、数列、几何、向量、概率统计、导数、综合应用），其后化学，最后物理综合题。各题型使用独立 mode / prompt / context adapter。
- AI 上下文来源为通用 `QuestionDocument` 与本地 solver 执行摘要；不上传整个界面、所有历史或未选择图片。
- AI 不能替换本地结果；具备本地验证器的结果需交叉核验；不能自动证明的内容要标记为 AI 推理、未自动验证。

输入法与图像不是本 AI 客户端内部功能，统一规划于 [CALCULATOR_INPUT_SOLVER_GRAPH_PLAN.md](./CALCULATOR_INPUT_SOLVER_GRAPH_PLAN.md)：题目文档、App 内专用输入键盘、自由文本 / 系统键盘兜底、符号搜索、自适应函数采样、断点检测、交互图像和数值表。

AI 开发顺序重新确定为：

1. 本地题型 / 复杂度注册表；
2. Worker 原生安全通道（不能伪造网页 Origin）；
3. Android 网络 DTO、SSE、取消 / 超时；
4. QuestionDocument 上下文 adapter；
5. 只在复杂题展示的 AI 分析 / 解题面板；
6. 高中数学、化学、物理 prompt 与本地核验适配。

AI 的完整题型、触发规则和验收见 [CALCULATOR_INPUT_SOLVER_GRAPH_PLAN.md](./CALCULATOR_INPUT_SOLVER_GRAPH_PLAN.md)。
