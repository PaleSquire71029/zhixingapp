# 知行计算器原生核心迁移计划

> 版本：1.0  
> 更新日期：2026-11-13  
> 目标仓库：`PaleSquire71029/zhixingapp`  
> 旧逻辑基线：`PaleSquire71029/calculator` 的 `calculator.html`、`js/chemistry-balancer.js`、`js/chemistry-predictor.js`

## 1. 本阶段目标

把旧网页中的数学与化学计算能力逐步迁移到 Android 原生 Kotlin。先保证结果和边界条件正确，再制作计算器操作界面；不使用 WebView 整页承载旧计算器，也不把算法写进 Activity / Composable。

本阶段先建立**纯 Kotlin、可单元测试、无 Android / Compose 依赖**的计算内核。第一步不做大页面，不引入新计算库，不改变旧仓库生产代码。

### 当前事实基线

- 新 App 目前只有一个 Gradle 模块 `:app`，继续在该模块内按包分层，暂不拆分多个 Gradle module。
- 一级导航现为「计算 / 知行 AI / 工具 / 我的」；「我的」页已有历史空状态和材质设置入口，历史仍无本地持久化记录。计算 Tab 当前仍未接入真正的计算器页面，C0–C2 数学内核已在纯 Kotlin 层分步迁移。
- 旧计算器的数学解析器位于 `calculator.html`：`zxTokenize`、`zxParseExpr/Term/Unary/Power/Primary`、`zxParseRational`、`zxEvalArithmetic`。
- 旧方程 / 不等式求解、实根与区间运算也在同一个文件中；多项式表达式使用系数数组，有理表达式使用 numerator / denominator 多项式表示。
- 旧化学配平与反应预测分别位于 `js/chemistry-balancer.js` 和 `js/chemistry-predictor.js`。
- 旧仓库现有化学测试页可作为回归样例来源；数学表达式 / 方程 / 不等式需要先建立独立黄金样例，不能只凭 UI 截图验证。

## 2. 设计边界

1. **计算域与界面分离。** `domain/math` 不得依赖 Android、Compose、DataStore、View 或 Activity。
2. **先保留语义，再逐步增强。** 旧行为先用测试锁定；已知错误或用户此前指出的错误优先写成测试，再修实现。
3. **解析和求值分离。** 输入字符串先 Tokenize，再构建 AST，最后求值；方程 / 不等式使用独立的代数表达层。
4. **不混淆数值类型。** 普通数值表达式首轮按旧引擎的 Double 语义对照；多项式 / 有理函数是代数表达式，不要误称为任意精度有理数。化学配平继续使用精确整数 / 分数算法，独立于普通计算器浮点引擎。
5. **定义域必须保留。** 有理式约分不能丢掉原始分母零点；方程增根、不等式开闭区间与分母排除必须可测试。
6. **不提前接 UI。** 内核每个小步通过 JUnit 后，再做最小计算页接入；键盘、动画、玻璃材质不与解析器开发混在同一 PR。
7. **不依赖 JavaScript 运行时。** Kotlin 代码不得用 WebView、`eval`、脚本桥接或调用旧网页作为计算后端。

## 3. 目标代码结构

先保持单一 `:app` 模块，按职责创建包：

```text
app/src/main/java/ci/us/zhixing/app/
  domain/
    math/
      model/       // Token、AST、结果、错误类型
      parser/      // Tokenizer、ExpressionParser
      evaluator/   // 数值求值与运算规则
      algebra/     // Polynomial、RationalFunction、RealRoots
      solver/      // EquationSolver、InequalitySolver
      functions/   // 点集解析、函数拟合、定义域与采样
      units/       // 单位定义、转换纯函数
    chemistry/
      formula/     // 化学式解析
      balancing/   // 精确配平
      prediction/  // 规则候选
  ui/
    calculator/    // 后续计算页与状态层
```

包可以随小步调整，但算法不得迁入 `MainActivity.kt` 或某个超大 Composable。

### 3.1 长期产品路线与 C0–C12 的映射 / 依赖

仓库中有两种不同目的的编号体系：C0–C12 是计算核心与工具 / 主页面接入的工程路线；P0、Q、M、S、G、AI、R 是高中题目产品路线、输入体验、函数图像和 AI 的长期路线。二者可以并行，但不能互相替代，也不要求一对一编号。

| 产品路线 | 主要内容 | 关联 C 阶段 | 依赖与边界 |
|---|---|---|---|
| P0 | 产品规则、离线优先、AI 仅由用户主动触发、数据隐私 | 全部阶段的横向约束 | 不是独立代码阶段，不对应单一 C 编号 |
| Q1 | `QuestionDocument / Block / Revision` 与序列化 | 无直接 C 编号 | 输入数据模型；需能承载文本、公式、矩阵、分段、化学式、单位和版本 |
| Q2 | 多行编辑器、光标 / 选区、撤销 / 重做、系统键盘切换 | 可复用 C1 / C2 的解析接口 | 依赖 Q1；编辑器先保存用户原文，解析失败不能丢文本 |
| Q3–Q4 | 专用键盘、公式模板、学科符号、符号搜索 | 与 C1 / C2 tokenizer / parser adapter 衔接 | 依赖 Q1 / Q2 的编辑模型；键盘是编辑命令层，不是可输入内容的上限 |
| M1 | 本地数学底座整体能力 | 横跨 C0–C7 | 是一组能力的总称，不是需要重复实现的额外 C 阶段；具体进度看账本 |
| S1 | `ProblemType / SolverCapability` 注册表和 Unsupported 分类 | 复用 C5 / C6 / C7，以及后续 C9 / C10 的能力结果 | 可先定义模型；注册项必须对应真实已实现能力，不能把未支持误报为无解 |
| S2 | 高中数学题型扩展 | 与 C5 / C6 / C7 重叠，并可能新增后续 C 小步 | C5.1–C5.13 已覆盖一元基础方程、二元一次方程组、受限实数立方根、变量右侧绝对值方程和基础正弦 / 余弦周期方程；正切、多角、复合三角与区间筛选、更复杂根式结构及参数系统等仍需各自算法和回归，不能视为已完成 |
| G1 | 函数求值与定义域 | C7.6 | 当前 C7.6 的 evaluator / domain 面向已建模的拟合函数；一般函数表达式解析与适配仍是独立工作 |
| G2 | 自适应采样、断点检测、视口 / 坐标轴 | 扩展 C7.6 | C7.6 已有拟合函数安全采样基础；完整表达式曲线、视口算法和 Compose 图表仍未完成 |
| G3 | 探针、数值表、多函数、参数滑块和图像交互 | 建立在 G1 / G2 上 | 依赖稳定求值与采样；需要独立触控、手势仲裁和性能验收 |
| C8 | 单位目录、转换内核与独立单位工具 | C8.1–C8.8 | 单位换算是独立工具线；C8.8.1 单位工具页不等于 C12 主计算器页 |
| S3 | 化学 / 物理专项迁移 | 化学部分关联 C9 / C10；物理需独立规划 | C9 是化学式解析 / 配平，C10 是化学反应预测；物理量模型不应假装已包含在这两个编号内 |
| C11 | 本地计算历史与持久化 | C11 | Room / 数据迁移 / 删除筛选等持久化能力；没有直接的 Q / G 编号替代它 |
| C12 | 最小主计算器页面 | 接入已验收的 C0–C7 等能力，并按需连接 Q1 / Q2 | 这是计算主工作区；独立工具页（例如 C8.8.1）可按自己的阶段提前接入，不代表 C12 已完成 |
| AI-0 | 本地复杂度判断与条件式 AI 入口 | 依赖 S1 能力分类及本地 solver 状态 | 简单任务不能请求 AI；本地 Unsupported 与程序异常必须区分 |
| AI-1–AI-2 | Worker 原生安全通道和 Android 网络层 | 不属于 C0–C12 数学阶段 | AI-1 的服务端认证 / 限流是 AI-2 网络客户端的前置条件；不能伪造网页 Origin 或把密钥放入 APK |
| AI-3–AI-4 | 复杂题解题面板、多题型提示词和核验 | 依赖 Q1 快照、S1 / S2 题型信息和本地结果 | AI 解释不能覆盖本地结果；必须与题目 revision 绑定 |
| R1 | 键盘、图像、AI 面板和主壳综合真机体验 | 横跨 Q / G / AI 和主壳 | 最终集成验收，不是替代各阶段单测或专项验收的编号 |

**依赖关系的读法：**

- C0–C12 是本仓库数学 / 工具工程计划；当前阶段、已完成项和下一步以 `IMPLEMENTATION_STATUS.md` 为准。
- Q1–Q4 可以先做设计和独立模型，但实现顺序为 Q1 → Q2 → Q3 → Q4；与 parser 的连接需要基于 C1 / C2 稳定接口。
- M1 是数学基础的产品概括；S1 / S2 是基于基础 solver 扩展能力目录和更多高中题型，不等于把 C5.1–C5.7 重新做一遍。
- G1 / G2 / G3 逐步依赖：先保证定义域与求值，再做采样 / 视口，最后做探针与手势交互。现有 C7.6 不能直接推导为完整图像 UI 已完成。
- C8 独立于 C12；单位工具可先有可用页面，但主计算器仍按 C12 计划接入。
- AI-1 必须先于真实 Android AI 请求；AI UI / 提示词不能成为本地 solver 的依赖。
- 本手册早期“第 0–9 步”是早期操作清单，不属于另一套产品阶段，也不是现行任务顺序；具体操作方法见 `PROJECT_STATUS_AND_GUIDE.md`。

## 4. 分批实施路线

| 编号 | 小步 | 范围 | 本步完成条件 |
|---|---|---|---|
| C0 | 旧逻辑清点与黄金样例 | 只新增测试夹具 / 迁移说明 | 每一类语法与异常都有明确输入、预期值和来源 |
| C1 | Tokenizer | 数字、小数点、运算符、括号、x、Unicode 运算符归一化 | Token 顺序、非法字符、非法数字格式测试通过 |
| C2 | AST 与递归下降解析器 | 加减、乘除、隐式乘法、一元正负、幂、后缀百分号、括号 | 运算优先级和结合性测试通过；解析错误有位置 |
| C3 | 常数表达式求值 | Double 数值、除零、非有限值、格式化前的原始结果 | 基础计算样例与旧实现逐项一致 |
| C4.1 | 多项式基础运算 | 升序系数模型、规范化、加减乘、缩放、Horner 求值 | 系数、次数、运算、零多项式与非有限边界测试通过 |
| C4.2 | 有理函数与定义域约束 | 分子 / 分母结构、加减乘除、幂、原式分母零点约束传播 | 约分前定义域不丢失；除以零函数拒绝；定义域约束合并测试通过 |
| C4.3 | 多项式实根基础 | 次数 / 零多项式判定、低次实根求解与残差检查 | 一次 / 二次根、重根、无实根和数值边界测试通过 |
| C5.1 | 常数 / 一次多项式方程 | 两侧多项式分别建模，移项为 L(x)-R(x)=0 | 恒等式、无解、唯一解、数值失败和高次暂缓测试通过 |
| C5.2 | 二次多项式方程 | 复用 C4.3.2 根引擎，在方程结果模型中映射两个根 / 重根 / 无实根 | 方程层结果映射、根数量、重根和残差测试通过 |
| C5.3 | 三次多项式方程 | 精确判别式分类、缩放 Cardano / 三角分支、二重根 / 三重根 | 单实根、三个不同实根、多重根、近消去小根、溢出与残差测试通过 |
| C5.4 | 分式方程 | 左右有理式相减，保留原始分母 / 除法定义域，剔除增根 | `x/x=1`、`1/x=2`、`x/x=0`、恒等式定义域和分母零点剔除测试通过 |
| C5.5 | 线性内部绝对值方程 | 将 `|ax+b|=c` 拆成 `ax+b=±c`；处理负目标、零目标及常数内部式 | 双根、零目标去重、负目标、恒等式 / 无解、非有限输入和高次暂缓测试通过 |
| C5.6 | 平方根方程 | 支持 `√P(x)=Q(x)`，deg(P)≤2、deg(Q)≤1；平方仅生成候选根，再检查 P≥0、Q≥0 和原式残差 | 平方增根剔除、根号定义域、边界根、受限恒等式、数值失败与高次暂缓测试通过 |
| C5.7 | 受限指数 / 对数方程 | `a^P(x)=c`、`a^P(x)=b^Q(x)`、`log_a(P(x))=c`、同底 `log_a(P(x))=log_a(Q(x))`；P/Q 为常数或一次式 | 换底、底数限制、真数严格为正、受限恒等式、回代残差和数值范围测试通过 |
| C5.8 | 二次内部式绝对值方程 | 将 `\|P(x)\|=d` 扩展到 `deg(P)≤2`，且 `d` 为有限常数；复用二次根引擎求解 `P(x)=±d`，零目标只计算一次 | 二次双根 / 多分支合并 / 零目标去重、负目标无解、三次内部式明确暂缓测试通过 |
| C5.9 | 二元一次方程组 | 两条方程 `a*x+b*y=c`，使用 BigDecimal 计算行列式 / 克拉默分子和秩分类，Double 输出检查范围与两式归一化残差 | 唯一解、比例方程无穷多解、矛盾无解、零系数、近奇异独立、溢出 / 下溢与非有限输入测试通过 |
| C5.10 | 实数立方根方程 | `∛P(x)=Q(x)`，`deg(P)≤3`、`deg(Q)≤1`；等价为 `P(x)=Q(x)³`，生成候选后代回原式 | 负解合法、有限根排序、恒等式 / 无解、次数边界、运算溢出与回代残差测试通过 |
| C5.11 | 变量右侧绝对值方程 | `\|P(x)\|=Q(x)`，`deg(P)≤2`、`deg(Q)≤1`；求解 `P−Q=0` 和 `P+Q=0`，两支均检查 `Q(x)≥0` | 双分支有限解、负右侧候选剔除、恒等分支的非负定义域、常数正负分类、次数边界与运算溢出测试通过 |
| C5.12 | 正弦周期方程 | `sin(a*x+b)=c`，弧度制；用 `α=asin(c)` 得到两组角度分支，按 `2π/|a|` 表示周期解族，`a=0` 单独分类 | 两解族 / 边界单族、负系数、常数方程、目标越界、非有限输入、周期溢出和相位精度测试通过 |
| C5.13 | 余弦周期方程 | `cos(a*x+b)=c`，弧度制；用 `α=acos(c)` 得到 `±α` 两组角度分支，周期为 `2π/|a|`，`a=0` 单独分类 | 两解族 / c=±1 单族、相位偏移、负系数、常数方程、目标越界、周期溢出和相位精度测试通过 |
| C6.1 | 多项式不等式 | 一至三次多项式实根分割符号区间，严格 / 非严格比较、偶数 / 奇数重根、无根与恒等式 | 一至三次符号区间、端点开闭、重根不变号、空集 / 全体实数和高次暂缓测试通过 |
| C6.2 | 分式不等式 | 复用有理函数分子 / 分母与原式定义域限制；分子零点可按比较关系取舍，分母及历史限制零点永远排除 | 分母零点开点、可去间断点、恒等式定义域、分子 / 分母多重根及符号区间测试通过（Build #405） |
| C7 | 函数工具内核 | 坐标解析、一次 / 二次 / 三次 / 反比例拟合、图像采样 | 重复 x、退化点集、定义域断点和采样边界测试通过 |
| C8 | 单位换算 | 长度、面积、体积、质量、温度、速度、时间、数据、角度 | 转换方向、温度偏移、空输入和单位定义测试通过 |
| C9 | 化学式解析与配平 | 将独立 JS 模块迁移为 Kotlin 精确整数 / 分数算法 | 元素守恒、电荷守恒、括号 / 水合物 / 离子样例通过 |
| C10 | 化学反应预测 | 迁移规则库，候选、条件、置信度与不确定性提示 | 不凭空生成候选；条件敏感反应明确标识 |
| C11 | 本地计算历史 | Room / 本地持久化设计，按计算类型记录并复用 | 重启恢复、删除、筛选与数据迁移测试通过 |
| C12 | 最小计算页接入 | 表达式区、结果区、错误状态、键盘输入状态 | 只接入已完成的内核能力；未实现项不展示为可用 |

## 5. C0 黄金样例范围

第一轮至少覆盖以下样例；具体输出按旧逻辑逐项核验后固化，不凭记忆猜值。

### 基础表达式 / 优先级

- `2 + 3 × 4`
- `8 / 4 / 2`（确认乘除结合方向）
- `-2^2`
- `(-2)^2`
- `-(2^2)`
- `2^3^2`（幂的结合性）
- `2x`、`3(x+1)`、`(x+1)(x-2)`（代数解析模式）
- `50%`、`200 + 10%`（旧百分号语义必须有测试）
- Unicode 乘除号、负号、全角括号与空白归一化。

### 错误与边界

- 空表达式、孤立小数点、多个小数点、未知字符。
- 不完整表达式、括号不配对、多余右括号。
- 除数为零、结果非有限值。
- 指数为负数、非整数、超过旧引擎限制。
- 常数计算误输入变量 x 时给出明确提示。

### 方程 / 不等式

- 一次方程、二次方程的两个实根 / 重根 / 无实根。
- 三次方程三个实根、一个实根和重根场景。
- 恒等式、矛盾式、无解。
- 分式方程产生的分母零点增根必须剔除。
- 严格与非严格不等式、重根符号不变、分母零点开点排除。
- 有理式恒等于零时，原式定义域仍需保留。

## 6. 120Hz 与视觉工作边界

当前材质 / 手势修复和 120Hz 窗口偏好属于 App 主壳质量工作，不等于已经实测稳定 120fps。进入计算器 UI 阶段后：

- 计算内核必须在后台纯计算，不把解析、求根或历史写库放在 Compose 每帧重组路径。
- 按键反馈动画与输入状态更新解耦，连续按键不得丢失或重复。
- 图像采样、函数图表重绘和公式排版不得阻塞主线程。
- 真实刷新率与帧时间必须在目标 iQOO 设备上分别测量；Android 的 120Hz 窗口偏好不代表系统必然授予 120Hz。
- 首个计算器 UI PR 不叠加复杂液态动画，先验证输入响应、布局和键盘高度，再逐项接入玻璃组件。

## 7. 质量门槛

每个小步必须满足：

1. 只修改本步相关文件；
2. 至少新增 / 更新对应 JUnit 测试，并做静态代码检查；
3. 单个小步骤以针对性 JVM 单元测试为默认验证，不要求每一步都构建 APK；多个紧密相关步骤可合并到阶段检查点统一执行完整 CI；
4. 需要验证 Android 依赖 / 平台 API 时执行对应编译；仪器测试源码编译通过不等于模拟器 / 真机已真正执行；
5. `:app:assembleDebug` 仅在阶段检查点、发布预览或用户明确需要 APK 时执行；
6. 更新 `docs/IMPLEMENTATION_STATUS.md`，记录文件、commit、测试命令和未验证项；
7. 不修改旧 `calculator` 仓库生产文件，不接触 Worker / Supabase；
8. 不允许为了 CI 变绿而删除或放宽失败断言。

## 8. 本阶段当前入口

**C0.1–C4.3 与 C5.1–C5.4 已完成对应纯 Kotlin 数学内核 CI 验收：** C0.1 黄金样例、C1 Tokenizer、C2 AST / Parser、C3 常数表达式求值、C4.1 多项式基础运算、C4.2 有理函数 / 原式定义域与数值精度保护、C4.3 一次 / 二次实根，以及 C5 常数至三次多项式方程与分式方程均已取得对应 CI 记录。

C5.3 / C5.4 功能代码提交 `933ba61f47ae4f40a138ff653ba4428f9e0b12f4`，次数边界测试修正提交 `e51fd565b8333e06aae8f88215ae9dca1f9bc5cb`；Android Build #400（Run ID `43397103889`）完整成功。资源检查、JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传均通过；未执行模拟器或真机仪器测试。

**C5.7 指数 / 对数方程与 C6.1、C6.2 不等式求解均已通过完整 CI 验收；下一阶段进入 C7 函数工具内核。** C5.7 仅支持常底、常数右侧或同底比较、变量指数 / 真数最高一次的受限形式；不同底对数相等、含变量底数和非线性指数 / 真数尚未支持。C5.6 当前支持单个平方根等式 `√P(x)=Q(x)`，其中 P 不超过二次、Q 不超过一次；立方根、多重根式、嵌套根式和变量次数更高的右侧尚未支持。C5.5 后续仍可扩展变量右侧和多个绝对值；继续只做纯 Kotlin domain，不接 Compose、专用键盘、函数图像或 AI。每个小步骤先做代码检查和针对性测试，多个紧密相关的小步可在阶段检查点统一回归，不要求每步都构建 APK；不放宽有效断言。

## 9. C2 首轮 CI 问题与修正记录（2026-11-08）

- C2 实现提交：`ea711e0e712fd748699ad23a55392bb498592e60`。
- Android Build Run `42443068196` 已通过 Kotlin 编译；失败发生在右结合幂链的 AST 测试断言。
- 对表达式 `2^3^2`，语法树按数学约定构造为 `2^(3^2)`。原测试误把内层幂节点左操作数写成 2，现已修正为 3，并显式检查右操作数为 2。
- 修正提交：`782dd334f40d4ab17e6c87dd5962e3f3be65475f`。
- 当前判定：Parser 代码与断言修复均已提交，但修正提交尚未取得完整 CI 成功记录，暂不勾选 C2 CI 通过。
- 下一项：C2 完整 CI 通过后再开始 C3；C3 仍只做纯 Kotlin 常数表达式求值，不接 Compose 页面。


## 10. C3：常数表达式求值（2026-11-08）

### 本步范围

- 输入流程固定为 Tokenizer → AST Parser → Evaluator；三个阶段保持独立，错误需要标出发生阶段和原始输入偏移。
- 普通数值首轮沿用旧计算器的 IEEE-754 `Double` 语义，不在 evaluator 内做舍入、分数化简、千分位或科学计数法格式化。
- 支持 AST 已定义的数字、显式分组、一元正负、加减乘除、整数幂和后缀百分号；隐式乘法由 Parser 生成的同一 Multiply AST 节点参与求值。
- 百分号按旧逻辑逐层乘以 `0.01`，所以 `50% = 0.5`、`200 + 10% = 200.1`、`50%% = 0.005`。
- 对正零、负零除数都返回“除数不能为 0”；溢出、Infinity 与 NaN 不允许作为成功结果。
- 常数计算遇到变量 `x` 时返回旧版提示“含 x 的式子请用「方程求解」”，不返回部分数值。
- 保留 Tokenizer / Parser 的原始错误消息和输入偏移；不要把格式化、UI 状态或代数求解混入本步。

### 本步实现文件

- `domain/math/model/MathEvaluation.kt`
- `domain/math/evaluator/MathExpressionEvaluator.kt`
- `domain/math/evaluator/MathArithmeticCalculator.kt`
- `test/domain/math/evaluator/MathExpressionEvaluatorTest.kt`

### C3 完成条件

1. 所有 `CalculatorGoldenCases.arithmetic` 成功样例按数值容差通过；
2. 所有算术错误样例保留预期错误消息；
3. 覆盖除负零、溢出 / 非有限数、百分号连用、0^0、变量拒绝和错误阶段 / 偏移；
4. `:app:testDebugUnitTest`、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug` 在 CI 成功；
5. 不接 Compose 页面、系统输入法、玻璃键盘或方程 / 不等式 solver。

C3 已通过 Android Build #363。当前先完成 C4.1 多项式基础运算的边界复核与最新 CI 验收；通过后再做 C4.2 有理函数与原式定义域约束，最后 C4.3 才进入实根求解。不在 C4.1–C4.3 完成前开始方程 UI。


## 11. C4.1：多项式基础运算（2026-11-08）

本小步使用升序系数：`[c0, c1, c2]` 表示 `c0 + c1*x + c2*x²`，沿用旧引擎约 `1e-12` 的尾部系数裁剪阈值。零多项式保持单元素 `[0]`，次数按当前旧引擎约定返回 0。

只实现多项式模型、加减乘、实数常数缩放与 Horner 求值。任何非有限输入、溢出系数或非有限求值结果都返回类型化错误；本步不实现求根、最大次数策略、有理式通分或定义域管理。

CI 通过后下一步为 C4.2：有理函数表示与定义域限制传播。`RationalFunction` 需要同时保留当前分母和所有来源分母 / 除法约束，不能通过约分把原式的不可定义点抹掉。


### C4.1 复核补充：零多项式规范化

C4.1 的多项式模型约定零多项式只使用 `[0.0]` 表示。构造时需先将带符号零统一为正零，按严格阈值裁剪尾部近零系数；若剩余的全部系数都严格小于 `ZERO_TOLERANCE`，整体收敛到 `[0.0]`。恰好等于阈值的系数必须保留。该规则已加入 JUnit，最新补丁的 Android Build 通过前不得开始 C4.2。


## 12. C4.2.1：有理函数模型与定义域不变量（2026-11-08）

本小步只建立数据模型，不实现有理函数运算或数值求值。

- `RationalFunction` 保存 numerator、denominator，以及独立的 `domainRestrictions`。
- 每个限制多项式表达一个必须满足的条件：`p(x) != 0`。
- 创建时自动把当前非恒定分母加入限制集合；同时保留所有上游传入的原始分母 / 除法限制。
- 限制集合按多项式值去重；非零常数限制不排除任何实数，因此不存储。
- 零多项式分母必须拒绝；上游限制中若出现恒零多项式，表示定义域为空，必须返回类型化错误。
- 不在本步进行最大公因式约分，也不把分子分母的公共因子消去。
- 下一小步 C4.2.2 才实现加、减、乘、除和非负整数幂，并按运算规则合并原始限制；除法必须额外加入除数分子的非零限制。
- C4.2.3 的单点求值与原式定义域检查首步已实现，详见第 14 节。


## 13. C4.2.2：有理函数运算与定义域传播（2026-11-08）

本步实现有理函数的代数运算，但不进行约分、实根求解或代入求值。

- 加法：`(a/b)+(c/d)=(ad+bc)/(bd)`；减法对应 `(ad-bc)/(bd)`。
- 乘法：`(a/b)(c/d)=ac/bd`。
- 除法：`(a/b)/(c/d)=ad/bc`；除数分子 `c(x)` 必须额外加入限制 `c(x) != 0`。若 c 是零多项式，直接返回“不能除以恒为零的有理函数”错误。
- 非负整数幂：分别对分子与分母求幂，最大保护指数为 64；语法层仍需按旧版规则限制含 x 的表达式次数与展开后的分子 / 分母次数。
- 所有操作数的 `domainRestrictions` 必须并集传播；新生成的当前分母由 `RationalFunction.create` 自动添加。
- 指数 0 返回 `1/1`，但底式所有原始定义域限制必须保留；例如 `(1/x)^0` 在代数形式上是 1，原式仍然排除 x=0。
- 暂不做最大公因式约分。以后即使加入化简，也只能改变 numerator / denominator，不能删除原式限制集合。
- 多项式中间结果溢出必须变为类型化错误，不能把 Infinity / NaN 继续传入有理函数。

C4.2.3 首个单点求值子步骤已通过 Android Build #372（Run ID `42453575710`）。

## 14. C4.2.3：有理函数单点求值与定义域检查（2026-11-08）

实现 `RationalFunctionArithmetic.evaluate(function, x)`，返回独立的 `RationalFunctionEvaluationResult`。

- 先拒绝 NaN / Infinity 输入。
- 逐项检查所有原始 `domainRestrictions`；限制多项式在该点为零时返回 `DOMAIN_RESTRICTION_VIOLATION`。因此 `x/x` 在 `x=0` 仍未定义，`(1/x)^0` 也保留 `x=0` 排除点。
- 再用现有 Horner 求值器计算分子 / 分母，传播 `NON_FINITE_INPUT` / `NON_FINITE_RESULT` 类型化错误。
- 对分母零值做防御性检查，并拒绝最终商溢出。
- 定义域零点判定不直接比较 Double Horner 结果或 epsilon。`PolynomialArithmetic.isExactlyZeroAt` 将有限 Double 系数与自变量解释为精确 IEEE-754 二进制有理数，以 BigInteger 精确 Horner 运算判零；常规 Double Horner 仍先执行以检查非有限输入 / 中间值，不改变最终数值求值语义。

回归测试覆盖普通代入、多个继承限制、`x/x` 可去间断点、零次幂保留限制、NaN / Infinity、Horner 中间溢出和最终商溢出。

CI：单点求值 Build #372、精确零点判定 Build #379、分母精度损失分类 Build #381（Run ID `42454900909`）均成功。Build #381 的资源校验、JUnit、androidTest 源码编译、Debug APK 组装和 Artifact 上传通过；Artifact ID `13607088513`，12,421,567 bytes，digest `sha256:a542ca67e6923cbd143b4dc7fa24472e4f0be5a1e7f9ff282b4f82186d44fe94`。未执行模拟器 / 真机仪器测试。

分母和分子 Double Horner 结果为 0、但精确二进制值非零时，现均返回 `NUMERIC_PRECISION_LOSS`；精确零点、近零非零值、原式定义域空洞、非有限中间值和最终商溢出均有回归测试。Build #372、#379、#381、#383 全部成功，故 C4.2.3 已完成 CI 验收；未执行模拟器 / 真机仪器测试。当前进入 C4.3.1，零 / 常数多项式分类与一次实根已通过 Build #388。


### 27.32 C4.2.3 分子求值精度损失保护（2026-11-08）

- [x] 在有理函数分母检查通过后，增加分子下溢 / 消去保护：当 Double Horner 结果为 `0.0`，但 `PolynomialArithmetic.isExactlyZeroAt` 判定精确 IEEE-754 二进制多项式值非零时，返回 `NUMERIC_PRECISION_LOSS`。
- [x] 只在 Double 结果恰为零时额外执行精确判定；普通非零数值仍沿用现有 Double Horner 路径，不引入固定 epsilon。
- [x] 测试覆盖 `x²` 在 `1e-200` 处下溢、多项式 `x² + 10¹⁶x − 10¹⁶` 在 `x=1` 处消去，以及精确零分子仍返回 0。
- [x] 源码和 JUnit 回归提交：`924f4958c2132b09ff016938ba4070c099c18a52`。
- [x] Android Build #383（Run ID `42455402733`）完整成功：JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传通过。Artifact ID `13607311903`，12,421,616 bytes，SHA-256 `54dfd80e7956ebefa7f1ea5a4ae9bd59c28d9a7ba03140a103d1e9e9c494f058`。
- [ ] 未运行模拟器或真机仪器测试。
- [ ] 下一步复核分子 / 分母的舍入零值、精确零值、非有限中间值、最终商溢出与原始定义域约束，再决定 C4.2.3 是否满足整体验收标准。


## 15. C4.3.1：零 / 常数多项式分类与一次多项式实根（2026-11-08）

本步把多项式求根从有理函数求值中独立出来，采用明确的根集结果模型：
- `AllRealNumbers`：零多项式，所有实数均为根。
- `NoRealRoots`：非零常数多项式，没有实根。
- `FiniteRoots`：有限实根列表，每个 `PolynomialRealRoot` 同时携带 `value` 与 `normalizedResidual`。
- `UnsupportedDegree`：当前子步骤未实现的次数；本步对二次及以上次数明确返回，不伪造结果。
- `Failure`：根超出 Double 表示范围或数值精度不足等类型化失败。

在 C4.3.1 首次落地时，`PolynomialArithmetic.solveRealRoots` 先只解决零多项式、非零常数和一次多项式；C4.3.2 后续扩展到二次，C5.3 再扩展到三次。一次根按 `-c0/c1` 计算；计算结果必须有限，非零常数项对应的根不得下溢成 0，并检查归一化残差 `|c0/scale + c1*r/scale|`（`scale=max(|c0|, |c1*r|)`），阈值为 `1e-12`。超范围返回 `NON_FINITE_RESULT`；根下溢或残差不合格返回 `NUMERIC_PRECISION_LOSS`。

测试覆盖零多项式、非零常数、常规一次根、零根、接近 Double 上界的有限根、根溢出、根下溢，以及二次输入明确标记为暂不支持。

代码提交：首版 `860429b699492d0471cf4ac5deb53e7906b0440c`；错误映射修正 `c6f59062cd89765d4affa3fa4dd1952ffed8c332`。Build #387（Run ID `42456067278`）因新错误码缺少一个 exhaustive `when` 分支而失败，日志已确认；修正后的 Build #388（Run ID `42456084109`）完整成功，JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传全部通过。Artifact ID `13606854310`，12,423,470 bytes，SHA-256 `47c53f359a01803891be43029b17db0489460b55d6561fcc092db2a79ac8d157`。未执行模拟器 / 真机仪器测试。

**C4.3.2 已完成 CI 验收，详见第 16 节。下一阶段进入 C5 方程求解，先定义方程结果模型并实现常数 / 一次方程最小切片；二次、三次与分式方程增根剔除仍按独立小步推进。

## 16. C4.3.2：二次多项式实根（2026-11-09）

在 C4.3.1 的零 / 常数 / 一次根基础上，扩展 `PolynomialArithmetic.solveRealRoots` 支持二次多项式，沿用 `PolynomialRootsResult`、`PolynomialRealRoot` 与 `PolynomialError`，不引入新 UI 或新依赖。

- 判别式 `D=b²−4ac` 的符号使用 `ExactDyadic` / `BigInteger` 对 Double 的精确 IEEE-754 二进制值计算，不用固定 epsilon。
- 数值求根使用二进制缩放中间表示；当 `D>0` 时采用 `q=-0.5*(b+copySign(sqrt(D),b))`，根为 `q/a` 与 `c/q`，避免朴素公式的小根灾难性消去；`D=0` 返回一个重根，`D<0` 返回 `NoRealRoots`。
- 最终根使用精确二进制项计算归一化残差 `|a*r²+b*r+c|/(|a*r²|+|b*r|+|c|)`，阈值为 `1e-12`。根超出 Double 范围返回 `NON_FINITE_RESULT`；非零根下溢为零、不同根无法在 Double 中区分或残差超限返回 `NUMERIC_PRECISION_LOSS`。
- JUnit 覆盖两个不同实根、稳定小根、重根、无实根、判别式近抵消的正负两侧、根溢出 / 下溢、归一化残差及三次输入继续标记 `UnsupportedDegree(3)`。

提交与 CI：
- 实现首版提交：`9a955bd2b3b61fb9fdc4980b14ebf9abd6ceb886`；Build #390（Run ID `42544038191`）因重复声明 `ROOT_RESIDUAL_TOLERANCE` / `rootsFailure` 编译失败。
- 读取失败日志后，修复提交：`9db9d21c9506b401959654bc6ecefc3ec7c043ff`；没有删除或放宽测试断言。
- Android Build #391（Run ID `42544184281`）完整成功：JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传均通过。Artifact ID `13642063693`，12,428,103 bytes，SHA-256 `e3708be725e4f8adced35b6c09801c4e150a3ce686dba559111242440510cc3f`。

本步状态：**源码已提交，CI / JVM 单元测试 / androidTest 源码编译 / Debug APK 构建通过；未运行模拟器，也未进行真机验收。** 下一阶段为 C5 方程求解。

## 17. C5.1：常数与一次多项式方程（2026-11-13）

本步开始方程 solver，但仍只在纯 Kotlin domain 中实现；不接入 Compose 页面、方程输入键盘或 AI。

### 结果与输入模型
- `PolynomialEquation(left, right)` 分别保存等式左右两侧的多项式，不把“移项”职责压给调用方。
- `EquationSolutionResult` 表达方程语义：`AllRealNumbers`（恒等式）、`NoSolutions`（矛盾式）、`FiniteSolutions`（有限实数解）、`UnsupportedDegree`（当前阶段未实现次数）和 `Failure`（类型化数值错误）。
- `EquationSolution` 携带解值及归一化残差，便于后续二次 / 三次方程和步骤展示复用。

### 求解规则
- 先计算 `L(x)-R(x)`；多项式相减溢出时保留 `PolynomialError`，不把 Infinity / NaN 继续传给求根器。
- 约化结果为零多项式时返回 `AllRealNumbers`；非零常数时返回 `NoSolutions`；一次式复用 `PolynomialArithmetic.solveRealRoots` 并映射为 `FiniteSolutions`。
- 本小步显式限制最高次数为 1。二次及以上暂时返回 `UnsupportedDegree`，即使底层 C4.3.2 已支持二次根，也要等 C5.2 验证方程层映射后再开放。
- 本步只接受多项式等式。原式分母 / 除法的定义域限制尚未进入该模型；分式方程在 C5.4 独立实现并剔除增根。

### 测试与 CI
- JUnit 覆盖恒等式 `3=3`、无解 `5=3`、一次移项 `2x+3=7`、两侧均有 x 的 `x+1=3x-3`、零根 `4x=0`、二 / 三次暂缓及多项式相减溢出。
- 提交 `9fe617a3ad1283341bde78b80d56fb775c2615bf`；Android Build #393（Run ID `43394051873`）完整成功。资源校验、JUnit、androidTest Kotlin 源码编译、Debug APK 组装与 Artifact 上传全部通过。
- Artifact `zhixingapp-debug` ID `14022361217`，12,434,313 bytes；SHA-256 `322d8591154a23e71bbb9b62a7d8a6ffd71cd888a503224c81ff5805b6fe3d84`。
- 未执行模拟器或真机仪器测试。

**C5.2 已通过 Build #396。下一步 C5.3：** 在现有方程结果模型上增加三次方程求解，验证一个实根、三个不同实根和重根组合，保留归一化残差与类型化数值错误；分式方程定义域与增根剔除继续留在 C5.4。


## 18. C5.2：二次多项式方程（2026-11-13）

在 C5.1 方程模型上开放二次次数；继续复用 `PolynomialArithmetic.solveRealRoots` 的 C4.3.2 稳定二次求根算法，不在 equation solver 中复制判别式、q 公式或残差计算。

- `PolynomialEquationSolver` 的最高支持次数提升至 2；左右两侧仍先相减，随后将 `PolynomialRootsResult` 映射为 `EquationSolutionResult`。
- JUnit 覆盖两个不同实根（`x²-3x+2=0`）、重根（`(x-1)²=0`）、无实根（`x²+1=0`）、稳定小根（`x²-10¹⁶x+1=0`）、底层根错误码透传，以及三次继续 `UnsupportedDegree(3)`。
- Build #395（Run ID `43394483739`）唯一失败用例为旧 C5.1 测试仍断言二次暂不支持；二次新用例与根引擎本身没有被报告为失败。修正提交 `3123bc6cf72c9a93f2933df894dd8dfe1ac7a29e` 只将旧测试收窄为验证三次暂缓。
- Build #396（Run ID `43394734621`）完整成功：资源校验、JUnit、androidTest 源码编译、Debug APK 组装及 Artifact 上传均通过。Artifact ID `14022780460`，12,434,313 bytes，SHA-256 `28bae4de45a8f27b4690c707bff19f054099113c959f7021b19bf540706aabeb`。
- 未执行模拟器 / 真机仪器测试。

**下一步 C5.3：** 实现三次方程解集映射与实根求解，单独覆盖一个实根、三个不同实根、二重根 / 三重根、重根合并与归一化残差；分式方程及原式定义域剔除仍保留给 C5.4。


## 19. C5.3 与 C5.4：三次多项式及分式方程（2026-11-13）

### C5.3 三次多项式方程

- `PolynomialArithmetic.solveRealRoots` 新增三次分支，方程层最高支持次数提升至 3；四次及以上仍返回 `UnsupportedDegree`。
- 使用 `ExactDyadic` / BigInteger 对输入 Double 的精确二进制值计算三次判别式，避免依靠 epsilon 判断单实根、三个不同实根或重根。
- 判别式为零时，用精确系数关系分辨三重根与一重根 + 一重二重根；判别式为负时使用二进制缩放的 Cardano 分支，判别式为正时使用三角函数分支求三个不同实根。
- 单实根 Cardano 分支增加缩放 Newton 修正以处理近消去的小根；根计算采用 `ScaledDouble` 中间表示，最终执行 Double 可表示性、不同根区分和归一化残差检查。精度不足时返回类型化错误。
- JUnit 覆盖三个不同实根、单实根、二重根、三重根、Cardano 近消去小根、可表示的大根，以及四次方程明确暂缓。

### C5.4 分式方程

- 新增 `RationalEquation`、`RationalEquationSolutionResult` 和 `RationalEquationSolver`，输入两侧均为已有 `RationalFunction` 模型。
- 通过有理式减法构造差值分子求候选根，并沿用 `RationalFunctionArithmetic` 的定义域传播，保留左右两侧的原始分母 / 除法限制。
- 每个候选根使用 BigInteger 精确二进制求值检查所有原式限制；任何使原式限制多项式为零的候选根均被剔除。
- 恒等式结果返回 `AllRealNumbersInDomain` 并携带限制多项式，不会被错误表示为无条件的全体实数；有限解、无解、不支持次数和类型化错误均有独立结果。
- JUnit 覆盖 `1/x=2`、`1/x=1`、`x/x=1` 定义域保留、`x/x=0` 增根剔除、四次分式方程暂缓和类型化运算错误。

### 提交与验证

- 功能实现提交：`933ba61f47ae4f40a138ff653ba4428f9e0b12f4`。
- 首轮 Build #399（Run ID `43396736449`）共执行 194 个 JUnit，唯一失败为旧 `PolynomialArithmeticTest` 仍断言三次不支持；新增三次 / 分式方程测试均通过。修正提交 `e51fd565b8333e06aae8f88215ae9dca1f9bc5cb` 只把该边界测试改为四次仍不支持，没有删除或放宽有效断言。
- Android Build #400（Run ID `43397103889`）完整成功：资源校验、全部 JUnit、androidTest Kotlin 源码编译、Debug APK 组装及 Artifact 上传均通过；Artifact ID `14022809017`，12,440,699 bytes。
- 未启动模拟器、未执行真机仪器测试；本阶段只验收 domain 算法与 CI 构建。

**下一步：** C5.6 根式方程；C5.7 指数 / 对数方程。C5.5 当前仅覆盖 `|ax+b|=c` 且 `c` 为有限常数；变量右侧、多重 / 嵌套绝对值仍未实现。


## 20. C5.5：线性内部绝对值方程（2026-11-13）

- 新增 `AbsoluteValueEquation(expression, target)` 与 `AbsoluteValueEquationSolver`，当前明确限定为 `|P(x)|=c`：内部多项式为常数或一次式，右侧 `c` 为有限常数。
- 对 `c<0` 返回无解；常数内部式直接分类为恒等式或矛盾式；一次内部式通过求解 `P(x)-c=0` 与 `P(x)+c=0` 生成两侧候选根。
- `c=0` 时只计算一次分支，避免重复根；有限解按数值升序返回，并沿用多项式求根器的归一化残差及类型化错误。
- JUnit 覆盖 `|2x-1|=3` 的两个解、`|2x+1|=0` 的零目标单根、负目标、常数式恒等 / 无解、二次内部式明确暂缓，以及 NaN 右侧类型化输入错误。
- 实现提交：`4d26a8d8b2d5e0c37454272bcf8af65cdf980cb1`。
- Android Build #401（Run ID `43398026099`）完整成功：资源校验、JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传均通过；Artifact ID `14023737491`，12,443,101 bytes。
- 未执行模拟器 / 真机仪器测试。本步不支持变量右侧、多重 / 嵌套绝对值或高于一次的内部多项式。

**下一步：** C7 函数工具内核。C6.1 多项式不等式与 C6.2 分式不等式已分别通过 Android Build #404 / #405 完整 CI。C5.6 当前范围仅为 `√P(x)=Q(x)`，deg(P)≤2、deg(Q)≤1；其它根式结构仍需独立扩展。


## 21. C5.6：平方根方程与增根剔除（2026-11-13）

- 新增 `RadicalEquation(radicand, right)`、`RadicalEquationSolutionResult` 和 `RadicalEquationSolver`，当前范围严格限定为 `√P(x)=Q(x)`，其中被开方式 P 的次数不超过 2，右侧 Q 的次数不超过 1。
- 求解器先计算 `P-Q²=0` 以生成候选根；平方不视为等价变换的最终结果。对每个候选根，使用 `PolynomialArithmetic.signAt` 的精确二进制有理数 Horner 计算检查 `P(x)≥0` 与 `Q(x)≥0`，再用 Double 代回原根式并检查归一化残差；非法候选根剔除，数值符号与浮点求值不一致时返回 `NUMERIC_PRECISION_LOSS`。
- 若 `P-Q²` 为零多项式，返回 `AllRealNumbersOnDomain` 并携带 P、Q 的非负条件，避免把 `√(x²)=x` 错误地报告成全体实数。
- JUnit 覆盖 `√x=x−1` 的平方增根、`√(x+1)=x−1`、负右侧候选排除、`√(x²)=x` 的受限恒等式、负常数右侧、零边界根、全体实数恒等式、高次暂缓和平方溢出。
- 源码提交：`9cc09a1e049ba9bb177aefb429adcd83127e872e`。
- Android Build #402（Run ID `43398997674`）完整成功：资源校验、JUnit、androidTest Kotlin 源码编译、Debug APK 组装和 Artifact 上传均通过；Artifact ID `14023194525`，12,448,771 bytes。
- 未执行模拟器 / 真机仪器测试。本步不支持立方根、多个 / 嵌套根式、任意高次 P 或二次及更高次数 Q。

**下一步：** C5.7 指数 / 对数方程。


## 22. C5.7：受限指数与对数方程（2026-11-13）

新增 `ExponentialLogarithmicEquation`、`ExponentialLogarithmicEquationSolutionResult` 与 `ExponentialLogarithmicEquationSolver`。当前支持以下四种形式，所有变量指数 / 真数最多为一次多项式：

1. `a^P(x)=c`，要求 `a>0`、`c>0`；底数为 1 时按常数式直接分类。
2. `a^P(x)=b^Q(x)`，要求两个底数均为正，允许底数为 1；两边取自然对数后化为一次方程。
3. `log_a(P(x))=c`，要求 `a>0` 且 `a≠1`；先计算目标真数 `a^c`，再解一次方程。
4. `log_a(P(x))=log_a(Q(x))`，要求同一底数 `a>0` 且 `a≠1`；化为 `P(x)=Q(x)`，并逐个检查两个真数都严格大于零。

### 精度与定义域保护

- 对数方程底数校验严格执行 `a>0` 且 `a≠1`；指数方程底数要求 `a>0`；指数式等于常数时右侧要求 `c>0`。
- 对数目标真数若上溢、下溢为 0 或因舍入无法与 1 区分，返回类型化数值错误，不把无表示范围的数值当作候选根。
- 指数 / 对数变形产生候选根后，使用原方程的自然对数值回代，检查归一化残差；多项式线性系数相差超过 Double 可可靠处理的范围时返回 `NUMERIC_PRECISION_LOSS`。
- 同底对数等式的恒等式结果返回 `AllRealNumbersOnDomain`，携带两个真数的严格正值条件；例如 `log_a(x+1)=log_a(x+1)` 不被错误地报告成无条件全体实数。
- 指数恒等式只有在可由结构直接确认时才返回全体实数；近似对数系数相等但无法证明恒等时返回精度错误。

### 测试与验证

- JUnit 新增 22 个场景，覆盖同底指数变形、不同底换底、底数小于 1、底数为 1、常数指数恒等 / 矛盾、对数等于常数、真数为正的验证、同底对数等式、真数零点剔除、严格正值域保留、非法底数、目标真数超范围、近 1 精度边界和高次结构暂缓。
- 功能提交：`88a9d18f8603d0388223e2196d0b8189b53e9692`。
- Android Build #403（Run ID `43400220986`）完整成功：JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传与所有 Actions 清理步骤均通过；Artifact ID `14023699329`，12,465,463 bytes。
- 未执行模拟器 / 真机仪器测试；未支持不同底数对数直接相等、变量底数、非线性指数或二次及以上次数的指数 / 真数表达式。

**下一阶段：** C6 一至三次不等式、分式不等式的符号区间和端点开闭规则。


## 23. C6.1：一至三次多项式不等式（2026-11-13）

新增 `PolynomialInequality`、`PolynomialInequalitySolutionResult`、`RealInterval` 和 `PolynomialInequalitySolver`。当前支持 `P(x)<0`、`P(x)≤0`、`P(x)>0`、`P(x)≥0`，且多项式次数不超过 3。

### 符号区间算法

- 复用 `PolynomialArithmetic.solveRealRoots` 取得有序实根边界；求根器已有的一次 / 二次 / 三次实根与残差检查继续作为唯一根源，不复制 Cardano 或二次求根算法。
- 根边界之间的有界区间使用溢出安全的中点采样，并调用 `PolynomialArithmetic.signAt` 的精确二进制有理数 Horner 符号判断；两侧无界区间按多项式次数奇偶与首项系数符号确定。
- 这种做法不假定“每过一个根必变号”，因此偶数重根两侧符号一致；三重根等奇数重根则由相邻区间实际符号自然体现。
- 严格不等式不把根点加入解集；非严格不等式把根点作为闭点，再按公共端点是否包含合并相邻区间。解集以 `RealInterval` 表示，无穷端点固定为开端点，单点解用两个相同且闭合的端点表示。
- 相邻浮点根之间若不存在可表示的 Double 内部采样点，或采样发现额外零点与根列表不一致，返回 `NUMERIC_PRECISION_LOSS`，不猜区间符号。
- 常数多项式和零多项式直接分类；四次及更高次数返回 `UnsupportedDegree`。

### JUnit 与 CI

- 新增 14 个测试方法，覆盖一次不等式端点、二次两个外区间 / 中间闭区间、无实根恒正 / 恒负、二重根不变号、二重根单点解、三次三个根、三次一个简单根加一个二重根、三重根、零多项式、正负常数和四次暂缓。
- 源码提交：`f23400eea148776895f7fccfac0c5c262e46ab29`。
- Android Build #404（Run ID `43401527163`）完整 success：JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传及所有 Actions 清理步骤均通过；Artifact ID `14025750621`，12,472,155 bytes。
- 未执行模拟器 / 真机仪器测试。本步未处理分式不等式、原式分母 / 除法定义域和可去间断点。

**下一步：** C7 函数工具内核，从坐标输入、点集校验和一次 / 二次拟合开始，继续保持纯 Kotlin 与分层测试。


## 24. C6.2：分式不等式与原式定义域保护（2026-11-13）

新增 `RationalFunctionInequality`、`RationalFunctionInequalitySolutionResult` 和 `RationalFunctionInequalitySolver`，求解 `N(x)/D(x)` 与 0 的 `<`、`≤`、`>`、`≥` 比较。最高支持分子、分母和每条原式定义域限制次数不超过 3；任一相关多项式超过三次均明确返回 `UnsupportedDegree`。

### 算法与定义域规则

- 使用当前分子、分母以及 `RationalFunction.domainRestrictions` 的并集构造全部实数轴边界；当前分母再次显式加入，防止调用方或未来模型调整意外漏掉分母定义域。
- 对每个参与边界的多项式复用 `PolynomialArithmetic.solveRealRoots`；不自行重写多项式求根算法。有限边界按数值排序，完全相同的 Double 根合并并合并其来源标记。
- 在无界区间用首项符号和次数奇偶确定各多项式符号；有限相邻边界之间用溢出安全中点，并以 `PolynomialArithmetic.signAt` 对分子、分母和原式限制进行精确符号检查。分式符号按 `sign(N) * sign(D)` 计算，不直接构造可能导致次数升高或溢出的 N·D 多项式。
- 分子零点仅在非严格不等式且原式定义域允许时加入；分母零点、历史定义域限制零点和除法操作引入的限制零点始终排除。排除点作为开端点保留，即使它同时也是分子零点。
- 零分子在严格比较时直接无解；非严格比较返回原式定义域，所有有限定义域空洞仍被分割保留。
- 共享或相邻边界若在 Double 精度下无法可靠区分，或者区间采样与零点边界不一致，返回 `NUMERIC_PRECISION_LOSS`，不生成不可靠的极窄区间。
- 解集沿用 `RealInterval` 表示，单点闭区间、半无限区间、并集与定义域孔洞均能表达；所有端点依照根点归属与原式限制独立决定开闭。

### JUnit 与 CI

- 新增 18 个测试方法，覆盖 `1/x` 正负区间、分子根可取 / 严格排除、分母根永不取、`(x²−1)/(x−1)` 可去间断点、`x/x` 空洞、分母二重根、继承的非分母限制、除法新增除数分子限制、零分子、常数分式、无理公共零点 `±√2`、相邻不可分辨根、四次分子 / 定义域限制暂缓。
- 功能提交：`fd9c6ef1bf9266dd5ac7be3e86a18a1825cde60e`。
- Android Build #405（Run ID `43403410088`）完整 success：JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传和所有 Actions 清理步骤均通过；Artifact ID `14026112216`，12,478,669 bytes。
- 未执行模拟器 / 真机仪器测试。

**下一阶段：** C7 函数工具内核，先实现坐标点输入校验与一次 / 二次函数拟合，再处理三次 / 反比例拟合和图像采样。


## 25. C7.1：坐标点数据模型与输入校验（2026-11-13）

新增纯 Kotlin `CoordinatePoint`、`CoordinatePointSet`、`CoordinatePointSetValidationResult` 和 `CoordinatePointSetValidator`，作为一次 / 二次 / 三次 / 反比例函数拟合前的统一输入层。本步只负责点集结构与校验，不提前实现任何拟合或绘图行为。

### 模型与校验规则

- `CoordinatePoint` 用 Double 保存 x、y。模型本身允许承载外部错误输入；有限性由 Validator 检查并转换为类型化结果，避免构造对象时直接抛异常。
- `CoordinatePointSet` 复制输入列表，并以不可修改视图暴露；通过校验后，调用方不能从原列表或结果列表侧更改点集。
- 最少点数由调用方传入，必须大于零；默认值为 1。空列表、非法点数参数分别返回独立结果。
- x 与 y 都必须为有限数；错误包含点索引和坐标轴。
- 对 `y=f(x)` 型拟合，x 必须唯一。重复 x 返回首个位置与重复位置；`-0.0` 与 `0.0` 按同一个横坐标处理。
- 重复判断使用精确 Double 相等，不用任意 epsilon 合并近邻 x；数值条件数与近退化数据在拟合器中另行检测。

### 测试与 CI

- 新增 11 个 JUnit 测试方法，覆盖有效点集、输入快照、空输入、x/y 非有限、重复 x、正负零、相邻 Double、点数不足、无效点数要求和校验优先顺序。
- 功能提交：`bb5fb6d822d18d0c19f51e4f67eb1254daea7761`。
- Android Build #406（Run ID `43404794011`）完整成功：资源检查、JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传及 Actions 清理均通过；Artifact ID `14027365163`，12,485,602 bytes。
- 未执行模拟器 / 真机仪器测试；本步没有函数拟合、函数解析、定义域处理或图像采样。

**下一步：** C7.2 一次函数拟合已完成。当前进入 C7.3 二次函数拟合，再扩展三次 / 反比例拟合和图像采样。


## 26. C7.2：一次函数拟合（2026-11-13）

新增 `LinearFunction`、`LinearFunctionFitResult` 与 `LinearFunctionFitter`。拟合目标为 `y=mx+b`，输出有限斜率和截距。

### 拟合语义与数值算法

- 输入继续复用 C7.1 `CoordinatePointSetValidator`，要求至少两个有限点，且 x 唯一；若传入点集对象，则在拟合边界再次校验其快照，避免内部直接构造的非法数据绕过规则。
- 恰好两个不同 x 的点唯一确定一条插值直线。三个或更多点采用普通最小二乘，最小化 `Σ(yi-(mxi+b))²`，不做加权或稳健回归。
- x、y 分别除以各自最大绝对值，控制原始数据极端量级；对归一化数据使用 Welford 在线更新均值、中心平方和与中心协方差，避免直接求和与大数均值相减引起的溢出 / 消去。
- 归一化过程中如果不同的原始坐标被压成相同 Double，或非零值下溢为零，则停止并返回 `NUMERIC_PRECISION_LOSS`；若中心方差不能在 Double 中区分，同样不猜测斜率。
- 用 `BigDecimal` + DECIMAL128 仅完成最终尺度恢复，避免中间比例先溢出 / 下溢；恢复后的斜率 / 截距若超出 Double 上限，返回 `NON_FINITE_RESULT`，若非零值下溢为零则返回精度损失。
- `fit(List)` 和 `fit(CoordinatePointSet)` 使用同样的错误分类：空列表和空点集均返回 `EmptyInput`，不足两个点、重复 x 等保留原验证类型。

### JUnit 与 CI

- 新增 13 个 JUnit 测试方法，覆盖两点插值、多点共线、带噪数据的最小二乘、水平 / 零函数、负坐标及乱序输入、空列表 / 空点集、点数不足、重复 x、极端对称坐标、相邻可表示坐标、归一化碰撞和斜率溢出。
- 初始功能提交 `3a2e754769280037f3a7434924e1af0a74e65faf`；Build #407 暴露 1 项溢出结果类型断言失败。修正提交 `3b9f2a490a558a43e80b8bc9453d2efbd9d7b5f1` 后 Build #408 完整成功。随后在 `c167cb304f90e85c2caedfeaf271af48cf3146ef` 统一空 `CoordinatePointSet` 结果，Build #409（Run ID `43410045585`）完整成功：资源检查、全部 JUnit、androidTest Kotlin 源码编译、Debug APK 组装、Artifact 上传与 Actions 清理均通过；Artifact ID `14029077035`，12,491,526 bytes。
- 未执行模拟器 / 真机仪器测试；不代表已在目标手机运行验收。

**下一步：** C7.5 反比例函数拟合。C7.1–C7.4 的坐标点校验与一次 / 二次 / 三次拟合均已完成并通过 CI；接下来处理反比例函数的定义域排除点、参数拟合及多点误差，再进入统一函数定义域与安全图像采样。


## 27. C7.3：二次函数拟合（2026-11-13）

新增纯 Kotlin `QuadraticFunction`、`QuadraticFunctionFitResult` 与 `QuadraticFunctionFitter`，拟合目标为 `y=ax²+bx+c`。拟合器继续复用 C7.1 坐标点校验，要求至少三个有限点，且所有横坐标互异；`fit(List)` 与 `fit(CoordinatePointSet)` 使用相同校验规则，并在拟合入口重新验证点集快照。

### 拟合语义与数值算法

- 恰好三个不同横坐标的点确定唯一二次插值函数；四个及以上点使用普通最小二乘，最小化纵向残差平方和。输入顺序不影响结果。
- 构建归一化 Vandermonde 设计矩阵 `[t²,t,1]`，横坐标先按量级缩放，再中心化和缩放至安全区间；纵坐标同步缩放，减小大数累积溢出。
- 使用**列主元 Householder QR 分解**直接求解最小二乘系统，不构造 `XᵀX` 正规方程，避免把矩阵条件数平方。列主元重排、相对秩阈值和回代检查用于发现退化 / 数值秩不足的点集。
- 将归一化系数恢复为原始坐标系的 a、b、c 时使用 `BigDecimal` DECIMAL128 中间运算，避免恢复尺度时的临时溢出；非零系数下溢或矩阵秩不足返回 `NUMERIC_PRECISION_LOSS`，真实超出 Double 范围返回 `NON_FINITE_RESULT`。
- 极端横坐标尺度下增加精确共线性分支：以输入 Double 的精确十进制展开进行交叉乘积验证，若全部点严格共线，则返回二次项为零的直线，避免极端尺度舍入制造虚假的极小二次项。截距通过精确分子 `y₀Δx−Δy·x₀` 后再除以 `Δx` 恢复，避免先舍入斜率再与极大坐标相减所导致的灾难性消去。
- 输入异常仍沿用类型化点集校验结果；不会把非有限或秩不足拟合伪装成成功函数。

### JUnit 与 CI

- 新增 15 个 JUnit 测试方法，覆盖三点插值、更多点共线 / 精确二次数据、带噪最小二乘、负坐标与乱序输入、零 / 常值函数、空列表 / 空点集、点数不足、重复 x、非有限坐标、归一化下溢、系数溢出和极端对称坐标。
- 初始功能提交 `dde20ea19fc5beb3fba6ca44846d856584bf6032` 对应 Build #410 完整成功；新增极端尺度共线分支后的 Build #411 暴露一项大坐标下截距精度断言失败。读取日志后修正截距恢复方式，最终提交 `e073b6ef750df72b2831e0cdd24459c17476129f` 对应 Build #412（Run ID `43411748705`）完整 success：资源检查、全部 JUnit、`compileDebugAndroidTestKotlin`、`assembleDebug`、Artifact 上传及 Actions 清理全部通过。
- Artifact：`zhixingapp-debug`，ID `14029827428`，12,503,320 bytes。
- 未执行模拟器 / 真机仪器测试；Instrumentation 测试仅完成 Kotlin 源码编译，不能作为设备端运行验收。

**下一步：** C7.4 三次函数拟合。处理四点插值、多点最小二乘、秩退化和数值稳定性；不接 Compose 函数页面或图像绘制。


## 28. C7.4：三次函数拟合（2026-11-13）

新增纯 Kotlin `CubicFunction`、`CubicFunctionFitResult` 与 `CubicFunctionFitter`，拟合目标为 `y=ax³+bx²+cx+d`。拟合器复用 C7.1 `CoordinatePointSetValidator`，要求至少四个有限点、横坐标互异；`fit(List)` 与 `fit(CoordinatePointSet)` 使用同一校验语义，并在拟合入口重新检查点集快照。

### 拟合语义与数值算法

- 恰好四个不同横坐标的点确定唯一三次插值函数；五个及以上的点采用普通最小二乘，最小化纵向残差平方和，不做加权或稳健回归。
- x 先按最大绝对值缩放，再中心化并映射到安全区间；y 同步归一化。构造三次 Vandermonde 设计矩阵 `[t³,t²,t,1]`。
- 使用**列主元 Householder QR 分解**直接求解最小二乘系统，不构造 `XᵀX` 正规方程；列主元按剩余列范数选择，配合相对秩阈值和回代检查，数值秩不足 / 浮点坐标归一化碰撞时返回 `NUMERIC_PRECISION_LOSS`。
- 将归一化的四个系数恢复为原始 x 坐标下的 a、b、c、d，乘法与抵消先保留在 BigDecimal 中，最后以 DECIMAL128 做除法转换；非零系数下溢返回 `NUMERIC_PRECISION_LOSS`，真正超出 Double 范围返回 `NON_FINITE_RESULT`。
- 当 x 量级极大或极小时，额外尝试精确低次多项式分支：用输入 Double 的精确 BigDecimal 展开，通过公共分母的 Lagrange 系数与交叉乘积验证全部点是否严格位于同一二次或更低次曲线上。通过时将三次项设为零并输出精确恢复的二次 / 一次 / 常数函数，避免极端归一化把低次函数误判为虚假三次项；不满足精确关系的噪声数据会继续走 QR，不被错误归类。
- 输入有限性、重复 x、点数不足沿用原 Validator 的类型化结果；拟合器不返回 NaN / Infinity 系数。

### JUnit 与 CI

- 新增 17 个 JUnit 测试方法，覆盖四点插值、五点精确三次数据、带噪数据最小二乘、线性 / 二次退化数据、零函数 / 常值函数、负坐标与乱序输入、空列表 / 空点集、点数不足、重复 x、非有限 y、横坐标归一化下溢、三次系数溢出 / 下溢及极端对称坐标下的精确线性结构。
- 功能提交：`c5167a45cdb76878e1faac5091e5a85f0a3f2f8b`。
- Build #413 暴露 1 项测试期望写错：输入在第 3 个点的 y 坐标上提供 Infinity，断言却预期 x 轴错误；Validator 正确返回 y 轴错误。修正断言为 `CoordinateAxis.Y` 后，最终测试修正提交 `7059dae72c741158dc071c89d54100f75fc19537`。
- Android Build #414（Run ID `43413083875`）完整 success：资源完整性检查、全部 JUnit、`compileDebugAndroidTestKotlin`、`assembleDebug`、Artifact 上传及 Actions 清理均通过。Artifact `zhixingapp-debug`，ID `14030506504`，12,510,326 bytes。
- 未执行模拟器 / 真机仪器测试；Instrumentation 测试仅完成 Kotlin 源码编译，不能作为设备端运行验收。

**C7.5 已通过 Build #415。下一步：** C7.6 统一函数定义域与安全图像采样；仍不接 Compose 函数页面或图像绘制。


## 29. C7.5：反比例函数拟合（2026-11-13）

C7.5 新增纯 Kotlin `InverseProportionFunction`、`InverseProportionFunctionFitResult`、`InverseProportionFitInputError` 与 `InverseProportionFunctionFitter`，拟合目标为 `y=k/x`。至少一个有效点即可拟合；单点唯一确定 `k=x·y`，两点及以上按普通最小二乘最小化纵向残差平方和 `Σ(yi-k/xi)²`。

- 点集输入继续复用 C7.1 `CoordinatePointSetValidator`，要求有限坐标与唯一 x；拟合器对 List 和 CoordinatePointSet 两种入口使用一致校验，并在点集入口再次检查快照。
- x=0（含正负零）不属于反比例函数定义域。拟合器返回独立的 `InverseProportionFitInputError.ZeroXCoordinate`，携带点索引与原始 x；没有把这一规则塞进通用 Validator，避免误伤多项式函数拟合。
- 数值算法取最小绝对横坐标作为 x 尺度，最大绝对纵坐标作为 y 尺度，构造有界归一化变量 `u=sX/x`、`v=y/sY`，求解 `q=Σ(uv)/Σ(u²)`，再用 `k=sX·sY·q` 恢复系数。分子与分母使用补偿求和，最终尺度恢复使用 BigDecimal DECIMAL128。
- 若倒数横坐标或纵坐标归一化发生下溢 / 碰撞，返回 `NUMERIC_PRECISION_LOSS`；系数真实超出 Double 范围返回 `NON_FINITE_RESULT`，非零系数下溢则返回精度损失。所有 y 均为零时直接得到合法的零反比例函数，但 x=0 仍先被拒绝。
- 新增 16 个 JUnit 测试方法，覆盖单点定参、负 x 符号、精确多点、带噪 OLS、乱序与正负坐标、零函数、x=0（List / PointSet）、空输入、重复 x、非有限坐标、倒数 / y 归一化下溢、系数溢出 / 下溢和极端但可表示的系数。
- 功能提交：`361e7f3389931acddb775db830dc085b10a85000`。
Android Build #415（Run ID `43414570879`）完整 success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传及 Actions 清理全部通过。Artifact `zhixingapp-debug`，ID `14031401965`，12,517,040 bytes；Artifact ZIP SHA-256：`81c1712ed567052f0e4fdcbd6145c6ce3a52af7e47e200554e10dbe479102055`。没有执行模拟器 / 真机仪器测试；Instrumentation 测试仅完成 Kotlin 源码编译，不能作为设备端运行验收。

C7.6 已通过 Build #416，函数图表 UI 仍留待后续原生页面阶段。C8.1 单位类别与量纲模型已通过 Build #417。**下一步：** C8.2 建立长度 / 面积 / 体积具体单位定义与线性换算基准。


## 30. C7.6：统一函数定义域与安全图像采样（2026-11-13）

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


## 31. C8.1：单位类别与物理量纲目录（2026-11-13）

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

**下一步：** C8.2 先实现长度、面积、体积单位定义与线性基准尺度；之后分批实现质量 / 速度 / 时间、数据存储 / 角度，最后单独处理温度仿射转换与通用换算入口。


## 32. C8.2：长度、面积与体积单位定义（2026-11-13）

C8.2 新增纯 Kotlin `UnitDefinition`、`UnitCategoryUnitSet` 和 `UnitDefinitionCatalog`，将旧版 `calculator.html` 中长度、面积、体积的具体单位目录迁入 Android domain 层。本步只登记单位元数据与到类别基准单位的比例因子，不执行输入解析或数值换算。

- 长度 15 项，以米 m 为基准；面积 11 项，以平方米 m² 为基准；体积 10 项，以立方米 m³ 为基准，共 36 项。
- 保留旧版单位 ID、中文标签、列表顺序和默认单位对：长度 m → ft、面积 m2 → ft2、体积 l → ml。包括市里 / 市尺 / 市寸、亩、公顷、美制 / 英制加仑等本地或英制条目。
- 每个比例因子必须是有限正数；单位 ID 和标签不能为空；同一类别内 ID 不允许重复；默认源 / 目标单位必须确实存在。
- 类别单位集只允许 `LINEAR` 类别，温度 `AFFINE` 不得进入只含单一比例因子的模型。目录和单位列表均复制快照并以不可修改列表暴露；按类别及 ID 查找，禁止跨类别误命中。
- 新增 9 个 JUnit 测试方法，逐项校验 36 个历史比例因子，并覆盖 ID / 标签 / 默认值兼容、类别量纲、类别隔离、不可变性和非法元数据拒绝。
- 功能提交：`2740027e666dc7725f1850d822d42c13a580adcf`。
- Android Build #418（Run ID `43424886269`）完整 CI success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 42s`。Artifact `zhixingapp-debug`，ID `14036736039`，12,540,001 bytes；Artifact ZIP SHA-256：`66f9803e6636dd1c88c2b5d18e4b32567522996f25969705270bffc70f5e08c3`。未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。

**下一步：** C8.3 迁移质量、速度和时间单位表；数据存储与角度随后单独处理，温度继续作为仿射转换独立实现。


## 33. C8.3：质量、速度与时间单位定义（2026-11-13）

C8.3 在 C8.2 的只读单位目录上追加质量、速度、时间三类，共 24 项；六类线性单位合计 60 项。换算基准分别为千克 kg、米每秒 m/s、秒 s。本步仍只保存元数据和线性比例，不执行数值换算或输入解析。

- 质量 9 项：吨、千克、克、毫克、市斤、市两、磅、盎司、英石。
- 速度 6 项：米每秒、千米每小时、英里每小时、节、英尺每秒、马赫；`kmh` 因子保持旧值 `1 / 3.6`。
- 时间 9 项：纳秒、微秒、毫秒、秒、分、小时、天、周、年（365 天）；`yr` 保持旧值 31,536,000 秒，不改成闰年或天文年。
- 保留旧版 ID、标签、顺序与默认单位对：kg → lb、kmh → ms、h → min。
- 原 C8.2 的长度、面积、体积条目未改动。新增测试扩展为对全部 60 项因子逐项精确比较，并覆盖六类默认值、量纲与类别隔离；测试方法合计 9 个。

代码提交：`0d268424c554178ce60a5905d85ea7dca8dace95`。Android Build #419（Run ID `43426203477`）完整 CI success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 1m 7s`。Artifact `zhixingapp-debug`，ID `14036937441`，12,540,674 bytes；Artifact ZIP SHA-256：`df7ed7b6c3e427e3a16c7ce182c545f594c03800d4011ac692874e5f48837140`。未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。

**下一步：** C8.4 迁移数据存储与角度单位；之后再独立实现温度仿射换算和通用线性换算入口。


## 34. C8.4：数据存储与角度单位定义（2026-11-13）

C8.4 在现有线性单位目录中追加数据存储和角度两个类别，共 16 项；目录现已包含八类、76 个 LINEAR 单位。本步继续只登记类别内单位和相对基准因子，不实现数值换算。

- 数据存储 10 项，以字节 B 为基准：bit、B、KiB、MiB、GiB、TiB、kB、MB、GB、TB。保留旧版 ID 和标签；二进制前缀按 1024 进位，十进制前缀按 1000 进位，1 bit = 0.125 B。
- 角度 6 项，以弧度 rad 为基准：度、弧度、百分度、圈、角分、角秒。比例因子使用旧版 `Math.PI` 公式，不把角度当作普通无量纲数。
- 保留默认单位对：数据存储 MB → GB（`mb` → `gb`），角度 degree → radian（`deg` → `rad`）。
- 旧 C8.2 / C8.3 的 60 项单位不变；现有测试扩展为全部 76 项因子精确核对，并覆盖数据 / 角度显式量纲、默认值、标签、类别隔离、同名 ID（速度与时间均有 `ms`）和不可变性。

代码提交：`32a288cccfc76211b0ef25a4ee803e98c87cbff6`；测试修正提交：`d93e9278fe5e99ae9a4619daf22bcfb20ef8feca`。Android Build #420（Run ID `43427982663`）完整 CI success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 1m 6s`。Artifact `zhixingapp-debug`，ID `14037119258`，12,541,011 bytes；Artifact ZIP SHA-256：`09e44420aef6fb08acfeed47b6a173f20da0c979c49f52147056157019cb71e5`。未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。

**下一步：** C8.5 独立实现摄氏度、华氏度、开尔文之间的仿射转换；随后建立带类型化结果的通用线性 / 仿射转换入口，并覆盖非法输入、溢出与精度边界。


## 35. C8.5：温度仿射定义与转换（2026-11-13）

C8.5 新增 `AffineUnitDefinition`、`AffineUnitCategoryUnitSet`、`TemperatureUnitCatalog` 和 `TemperatureConverter`，将温度单独建模为仿射类别，不进入只含比例因子的 `UnitDefinitionCatalog`。

- 内部统一使用开尔文 K 为基准。摄氏度映射为 `C + 273.15`；华氏度映射为 `(F + 459.67) × 5/9`；开尔文保持不变。偏移在缩放前应用，三种单位的绝对零度输入均精确映射到 0 K。
- 保留旧版 ID、标签和默认选择：`c` / `f` / `k`，默认摄氏度 → 华氏度。
- 转换器返回 sealed typed result；错误区分非有限输入、低于绝对零度、结果溢出、未知源单位和未知目标单位。
- 绝对零度采用严格 Kelvin 下界检查，不用宽松 epsilon 把低于绝对零度的值放行；支持绝对零度以上的负摄氏 / 华氏温度。
- 新增 9 个 JUnit 测试方法，覆盖常用转换、负温度、三种绝对零度、紧邻下界的 `Math.nextDown` 输入、NaN / ±Infinity、转换结果溢出、未知单位、仿射目录不可变性及元数据约束。
- 功能提交：`935d59982f5cce2992f2903d536459e6a22d5444`；测试整理提交：`a399d2d2c55dd330f35a3afdde0d7d1b5509a2fd`。Android Build #421（Run ID `43429397488`）完整 CI success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 48s`。Artifact `zhixingapp-debug`，ID `14038965866`，12,546,736 bytes；Artifact ZIP SHA-256：`9104003229349ce033b11224b55a08637935b07836b60f46f4ce6be1ef9116c3`。未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。

**下一步：** C8.6 实现统一线性 / 仿射换算入口，验证类别量纲、单位归属和数值安全；再补齐不同类别的跨类别拒绝测试及完整转换矩阵。


## 36. C8.6：统一线性 / 仿射单位转换入口（2026-11-13）

C8.6 新增 `UnitConverter`、`UnitConversionResult` 和 `UnitConversionError`，将八类线性单位与温度仿射单位接入统一纯 Kotlin API。调用同时传入源 / 目标类别 ID 和单位 ID，必须先通过类别查找、量纲兼容性检查和同类别约束，之后才进入对应数值路径。

- 八类 LINEAR 目录仍由 `UnitDefinitionCatalog` 提供；温度 AFFINE 路径继续委托 `TemperatureConverter`，避免偏移公式混入乘法转换。
- 结果统一为 Success / Failure；错误覆盖非有限输入、未知源 / 目标类别、物理量纲不兼容、同量纲但不同类别、未支持类别、未知源 / 目标单位、低于绝对零度、中间值非有限、结果非有限和精度下溢。
- 线性换算使用 `BigDecimal` + `DECIMAL128` 中间计算，避免仅因中间基准量超出 Double 范围就误拒绝最终仍可表示的换算结果；最终 Double 非有限返回溢出错误，非零十进制结果转换为 Double 后归零则返回精度下溢。
- 温度 converter 将基准值非有限与目标结果非有限拆成不同错误，统一入口映射到同一错误模型。
- 新增 9 个 JUnit 测试方法，覆盖九类各自转换、全部跨类别配对拒绝、长度 / 质量和数据 / 角度维度不兼容、同 ID 的速度 / 时间隔离、未知类别 / 单位、非有限输入、线性溢出 / 下溢、BigDecimal 中间值保护和温度仿射错误映射。
- 功能提交：`8a04403c895cf88784d1803032e992c7cef32457`；边界测试修正：`21f91b43de60f93b83f5ad15c0c808672c214c5e`；全量跨类别配对测试：`a580ebc688ec1249e38080b6ac42b07f557f8472`。Android Build #422（Run ID `43431479631`）完整 CI success：资源检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 52s`。Artifact `zhixingapp-debug`，ID `14039917793`，12,553,718 bytes；Artifact ZIP SHA-256：`b5ab3a891a097e621b42d6dfc079d9845f2c68c135b1402d2ea7d69484d4cd91`。未执行模拟器 / 真机仪器测试；androidTest 仅完成 Kotlin 源码编译。

**下一步：** C8.7 已完成旧版单位换算兼容回归并明确稳定 Domain API；下一阶段进入原生单位换算页面 / 输入层接入。契约见 [UNIT_CONVERSION_API.md](UNIT_CONVERSION_API.md)。


## 37. C8.7：旧版单位换算兼容回归与迁移验收（2026-11-13）

C8.7 不增加新的单位类别或转换因子，而是将 C8.1–C8.6 形成的九类统一入口与旧版 `calculator.html` 的真实算法做端到端回归，并明确原生页面可依赖的 Domain API 契约。

- 新增 `LegacyUnitConversionCompatibilityTest`，把旧版线性算法 `b.fromBase(a.toBase(v))` 作为参照；76 个线性单位形成 780 组同类别有序源 / 目标配对，每组使用 -123.456、-1、0、1、123.456 五个样例，共 3,900 次转换结果比对。
- 温度对照旧版摄氏度基准算法（C 原值、F → C 使用 `(F - 32) × 5/9`、K → C 使用 `K - 273.15`），覆盖 3 个源单位 × 3 个目标单位 × 5 个有效样例，共 45 次结果比对。
- 核对九类历史顺序与所有默认单位对：`m → ft`、`m2 → ft2`、`l → ml`、`kg → lb`、`c → f`、`kmh → ms`、`h → min`、`mb → gb`、`deg → rad`。
- 增加重复单位 ID `ms` 的端到端隔离回归：`speed/ms` 表示米每秒，`time/ms` 表示毫秒；混用类别必须失败。
- 原有安全增强属于有意行为，不作为兼容性错误：拒绝 NaN / ±Infinity、低于绝对零度的温度、结果溢出和精度下溢；错误通过 `UnitConversionError` 明确表达，不返回旧版的 NaN 占位。
- 新增 [UNIT_CONVERSION_API.md](UNIT_CONVERSION_API.md)，明确统一入口参数、返回类型、稳定类别 / 单位 ID、数值语义、错误类型与 UI 接入边界。格式化、文本解析和 Compose 页面仍归输入 / 表现层，不混入纯 Kotlin 换算内核。
- 测试与 API 契约提交：`428425641266374898159cdf469e0b145c7e34bc`。
- Android Build #423（Run ID `43435614443`）完整 success：资源完整性检查、全部 JUnit、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传与 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 46s`。Artifact `zhixingapp-debug`，ID `14042138313`，12,553,719 bytes。
- 本步未改动 Compose UI；instrumentation 测试源码完成 Kotlin 编译，但未执行模拟器 / 真机仪器测试。

**下一步：** C8.8.2 已完成代码与 CI 验收；目标真机验收按用户要求暂缓，不阻塞后续计算器功能开发。接下来回到高中方程题型扩展，选择 C5.8 的最小独立测试切片。


## 38. C8.8.1：原生单位换算页面首版接入（2026-11-13）

在 C8.7 稳定接口之上，完成第一个可操作的原生单位换算页面，并接入现有「工具」一级 Tab 与二级页面转场。

- 新增 `ToolsDirectoryScreen`，工具目录只展示已经有实际功能的「单位换算」入口，不把尚未实现的方程、函数或化学入口做成空按钮。
- 新增 `UnitConverterScreen`，包括九类单位横向类别选择、空白初始输入框、源 / 目标单位下拉选择、双向交换、即时换算主结果、同类全部单位一览和类型化错误提示。
- 页面复用 `BackdropHost`、`ZhixingGlassSurface`、`ZhixingLiquidButton` 与现有二级页动画；适配 safe drawing / IME insets，输入框使用系统十进制键盘。没有引入 WebView 或网页资源。
- 新增纯 Kotlin `UnitConversionPresenter`：将输入文本状态、默认单位、目录映射和展示行整理为 UI model；所有主结果及同类单位结果都调用 `UnitConverter`，不复制线性或温度公式。
- 新增 `UnitValueFormatter`：有限值采用 10 位有效数字，极大 / 极小值使用科学计数法；空输入维持空状态，不自动转换为 0。
- 新增 `UnitConversionPresenterTest`，覆盖九类顺序 / 默认单位、空 / 非法输入、线性单位一览、温度仿射一览、低于绝对零度 / 非有限输入错误，以及常规 / 科学计数格式。
- 页面与导航接入提交：`c9c0be200bddf9672798b93dac6a0becc328af55`。
- Android Build #424（Run ID `43436816915`）完整 success：资源完整性检查、全部 JVM 单元测试、`:app:compileDebugAndroidTestKotlin`、`:app:assembleDebug`、Artifact 上传和 Actions 清理全部通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 46s`。Artifact `zhixingapp-debug`，ID `14043848306`，12,601,245 bytes。
- 本步没有执行模拟器 / 真机 instrumentation tests，也尚未完成 iQOO Neo5 活力版页面视觉、IME、触控和滚动验收。Build #424 的 androidTest 是源码编译，不是设备执行。

**下一步：** C8.8.2 已完成代码与 CI；真机验收按用户要求暂缓，后续不作为其他计算器功能的前置条件。


## 39. C8.8.2：单位换算交互、输入边界与无障碍完善（2026-11-14）

在 C8.8.1 原生页面首版上补齐高频交互和表现层输入边界；不修改 C8.1–C8.7 的转换算法，也不增加新的转换因子。

- **复制结果：** 增加复制完整等式操作，剪贴板内容包含原始输入、源单位、格式化后的目标值与目标单位；复制状态只对当前输入 / 类别 / 单位组合有效。
- **输入体验：** 支持清除输入；IME Done 收起系统键盘。输入解析区分 Empty、Incomplete、Invalid、InputTooLong 与 Converted，允许用户自然输入 `-`、`.`、`1e-` 等中间状态；数字最多 64 字符。
- **数值安全：** 继续由 `UnitConverter` 处理非有限输入、溢出、绝对零度和精度下溢；Presenter 不把失败转换为 0，也不自行复制公式。
- **窄屏布局：** 可用宽度小于 340dp 时，源 / 目标单位控件纵向排列并显示“交换单位”；宽屏保持横排。类别区保留横向滚动。
- **无障碍：** 源 / 目标选择具备当前单位描述；交换和复制按钮有清晰动作名称；复制成功与当前目标单位提供状态描述；同类单位行合并朗读。
- **测试：** `UnitConversionPresenterTest` 共 10 个测试方法，覆盖九类默认值、空 / 非法 / 中间 / 超长输入、正负数 / 小数 / 科学计数法、线性 / 温度结果、类型化错误、复制文本与格式化边界。
- **提交：** 页面 / Presenter / 测试提交 `64f2ce0dd7f09f985e98d8af8e5d4b8075072d40`；Compose semantics 修正提交 `2b62e75f6f20224b50550d0f655d3d511ccc2cd3`。Build #427 的首次编译失败已如实保留为历史；Build #428（Run ID `43545850658`，代码 SHA `2b62e75f6f20224b50550d0f655d3d511ccc2cd3`）完整 success，主 Gradle 步骤 `BUILD SUCCESSFUL in 50s`，Artifact ID `14093618375`，12,612,565 bytes。
- **设备状态：** 没有模拟器 / 真机仪器测试记录；按用户要求，本步不执行 iQOO 真机验收。待执行清单继续保留未勾选状态。

**下一步：** 回到 C5 高中方程求解扩展，先定义 C5.8 的最小题型范围和黄金样例，再独立实现。


## 40. C5.8：二次内部式绝对值方程（2026-11-14）

在 C5.5 的线性内部式绝对值方程基础上，将绝对值内部多项式扩展至二次。支持范围为：

\|P(x)\|=d，其中 deg(P)≤2，d 为有限实常数。右侧仍为常数，不隐含支持 \|P(x)\|=Q(x)、多个绝对值或嵌套绝对值。

- 负目标直接返回 NoSolutions；常数内部式仍按恒等式 / 矛盾式分类。
- 当 d>0 时，分别求解 P(x)−d=0 与 P(x)+d=0，复用 C4.3 的二次实根引擎；合并、排序两支实根。
- 当 d=0 时只求解 P(x)=0 一次，避免同一根由正负两个分支重复出现。
- 二次以上的内部式明确返回 UnsupportedDegree；非有限目标继续返回类型化 NON_FINITE_INPUT。
- AbsoluteValueEquationSolverTest 增加二次两解、正负分支三解合并、零目标去重，以及三次仍暂缓用例；共 9 个测试方法。
- 代码提交：f6c08f91d946c09bae4662dd714b1a810e51c457。
- Build #429（Run ID 43550028977）完整 success：JVM 单元测试、Android instrumentation 测试源码编译、Debug APK 组装、Artifact 上传与 Post Set up Gradle / JDK / Checkout 清理步骤全部通过；Artifact ID 14095509294，12,612,599 bytes。
- 未运行模拟器或真机仪器测试；按用户要求，设备验收仍暂缓。
- 不涉及 Compose、解析器、专用键盘、函数图像或 AI。

**下一步：** 继续从高中方程题型目录中选择一个边界明确的独立切片，优先补充真实常用题型与可验证的解集语义；仍先做 domain 与测试，不提前接 UI。


## 41. C5.9：二元一次方程组（2026-11-14）

新增二元一次方程组最小切片。每条方程表示为 a*x + b*y = c；系统包含两条方程、两个未知数。该模型面向未来 EquationSystemBlock 的本地 solver 接入，但本步不做文本解析或 UI。

- 解集结果分为 AllRealPairs、NoSolutions、UniqueSolution 和 Failure；不把所有退化情形合并为“无解”。
- 对输入进行非有限检查后，使用 BigDecimal 表示有限 Double 的十进制往返值，计算行列式 a*e−b*d 及克拉默法则分子，避免中间乘法溢出。
- 行列式为零时，通过系数行是否全零，以及两个增广 2×2 子式是否为零，精确区分比例 / 相关系统与矛盾系统；不使用固定 epsilon 进行秩判断。
- 行列式非零时计算唯一解；转换回 Double 后检查 Infinity / NaN 与非零结果下溢为零。对两个原始方程分别计算归一化残差，取较大值作为最终误差指标。
- LinearEquationSystemSolverTest 新增 10 个 JUnit 测试方法，覆盖唯一解、负系数、无穷多解、无解、零系数行、全零系统恒等 / 矛盾、近奇异独立系统、非有限输入、解溢出和非零解下溢。
- 代码提交：16b6840fc8d3ca561d04449c726f47248f2d00b5。
- Android Build #431（Run ID 43550576403）完整 success；主 Gradle 步骤 BUILD SUCCESSFUL in 1m 2s，全部 JVM 单测、Android 测试源码编译、Debug APK 组装、Artifact 上传和 Actions 清理通过。Artifact ID 14096161115，12,617,943 bytes。
- 未执行模拟器 / 真机仪器测试；按用户要求，设备验收暂缓。
- 仍未支持三元或更高维系统、含参数系统、非线性方程组、通用矩阵求解，也未接入 EquationSystemBlock / Compose 输入。

**下一步：** 继续选择一个真实高中常见题型的最小独立范围，优先检查三角方程或更完整的绝对值 / 根式题型；保持 domain 纯 Kotlin、结果分类和数值边界先行。


## 42. C5.10：实数立方根方程（2026-11-14）

新增实数立方根方程最小切片，支持 ∛P(x)=Q(x)，其中 deg(P)≤3、deg(Q)≤1。与平方根不同，实数立方函数在整个实数域上严格单调且一一对应，因此等式可等价变形为 P(x)=Q(x)³，不需要额外的非负定义域约束。

- 新增 CubeRootEquation 与 CubeRootEquationSolutionResult，区分全体实数、无解、有限解、结构暂缓和类型化失败。
- 对 Q 先计算 Q²，再计算 Q³；候选多项式为 P−Q³。复用 PolynomialArithmetic 的乘法、减法和一至三次实根引擎。
- 对每个有限候选根，重新计算原始 P(x)、Q(x)，使用 Math.cbrt(P(x)) 回代 ∛P(x)=Q(x)，并检查归一化残差。负 radicand 和负解必须允许，不套用平方根 solver 的 Q(x)≥0 条件。
- 乘法 / 求值的非有限中间结果保持类型化失败；回代误差超过阈值则返回 NUMERIC_PRECISION_LOSS。
- CubeRootEquationSolverTest 新增 9 个 JUnit 测试方法：三次 radicand 与一次右侧双根、负数解、三次多项式恒等式、负常数恒等式、常数矛盾、无解、二次 radicand 解集、次数边界，以及立方溢出。
- 代码提交：61186992e4bee37cabad1016002d4ed38b06a587。
- Android Build #433（Run ID 43551030029）完整 success；主 Gradle 步骤 BUILD SUCCESSFUL in 39s，全部 JVM 单元测试、Android 测试源码编译、Debug APK 组装、Artifact 上传和 Actions 清理通过。Artifact ID 14095917816，12,620,182 bytes。
- 未执行模拟器 / 真机仪器测试；按用户要求，设备验收继续暂缓。
- 未接入 Parser、Compose、专用键盘或独立页面。

**下一步：** 继续围绕高中方程题型补齐真实高频结构，选择 C5.12 的独立范围并明确候选根、解集语义和数值边界；先完成纯 Kotlin domain 和回归测试。

## 43. C5.11：变量右侧线性绝对值方程（2026-11-14）

新增方程形式 |P(x)|=Q(x)，其中 deg(P)≤2、deg(Q)≤1。该范围覆盖常数或一次右侧，内部式保持常数 / 一次 / 二次，不扩展成多个或嵌套绝对值。

- 新增模型 AbsoluteValueEquationWithLinearRightSide 与结果 AbsoluteValueLinearRightEquationResult；结果类型包含 AllRealNumbersOnDomain、NoSolutions、FiniteSolutions、UnsupportedStructure 和 Failure。
- 分别求解 P(x)−Q(x)=0 与 P(x)+Q(x)=0；候选根仍须满足 Q(x)≥0。使用 PolynomialArithmetic.signAt 精确判断右侧符号，再通过原始表达式计算归一化残差。
- 当任一分支为恒等式时，解集不是无条件的全体实数，而是所有满足 Q(x)≥0 的 x。若 Q 为负常数则为空集；若 Q 为非负常数 / 零多项式则为全体实数；若 Q 为非恒定一次式则将其作为非负域条件返回。
- 当两个分支在 Q(x)=0 处出现相同根时，最终按根值去重、排序；右侧负值产生的增根必须剔除。
- 通过 11 个 JUnit 测试方法覆盖双分支两解、负右侧候选剔除、正 / 负恒等分支域条件、零恒等、正常数右侧、零右侧去重、负常数无解、次数边界、非有限分支运算，以及常数恒等分支右侧正负分类。
- 功能提交 f0cc86f70a982d8a2d7fd17e0d3ca03b1939cbe1；中间重构 b7da36655c06d2d7c35ceaee59abeb80ce7af992 曾造成重复局部声明并使 Build #435 失败；修正提交 57413373e491ec115c5e0af3ec39f9d3ae5dfd30 后 Build #436 完整 success。补充常数恒等测试提交 133923abe41744b62bb71db56385d43c20966c72 后 Build #437 完整 success。
- Build #437（Run ID 43551712552）完整 success；主 Gradle 步骤 BUILD SUCCESSFUL in 49s，全部 JVM 单元测试、Android 测试源码编译、Debug APK 组装、Artifact 上传和 Actions 清理通过。Artifact ID 14095997284，12,623,582 bytes。
- 未执行模拟器 / 真机仪器测试；按用户要求，设备验收继续暂缓。未接入 Parser、Compose、专用输入键盘或 UI。

**下一步：** 继续评估 C5.13 的高中常见方程形式，优先选择边界可封闭、解集易验证的独立 domain 切片；目前仅实现基础正弦周期方程，不能把规划中的其它三角能力标成已完成。
## 44. C5.12：弧度制正弦周期方程（2026-11-14）

新增纯 Kotlin 正弦方程内核，支持 `sin(a*x+b)=c`，所有角度采用弧度制。当前只处理线性角表达式和常数目标，不包含复合三角函数、多角组合、含参区间综合或反三角函数方程。

- 新增 `SineEquation` 输入模型，明确系数 a、相位 b、目标 c 均为 Double 且角度单位为弧度。
- 新增 `PeriodicSolutionFamily`，用 `x = firstSolution + k * period`（k 为任意整数）表达一个无限周期解族；基准值归一化至 `[0, period)`，周期固定为正数。
- 新增 `SineEquationSolutionResult`，将全体实数、无解、一个或两个周期解族、类型化数值错误分开表示；不把无限解截断成有限根列表。
- 对 a≠0 使用 α=asin(c)，分别生成 `a*x+b=α+2kπ` 和 `a*x+b=π−α+2kπ` 两支；当 c=±1 时两支重合，只返回一个解族；周期为 `2π/|a|`，负系数仍得到正周期。
- 对 a=0 直接判断常数等式 `sin(b)=c`，分类为 AllRealNumbers 或 NoSolutions；c 超出 [-1,1] 时直接无解。
- 对非有限输入返回 NON_FINITE_INPUT；周期溢出返回 NON_FINITE_RESULT；非零基准解下溢、相位周期数超出可靠归一化范围或原式回代残差不合格时返回 NUMERIC_PRECISION_LOSS。
- 新增 11 个 JUnit 测试方法，覆盖 `sin(x)=0` 两解族、c=±1 边界合并、`sin(2x+π/6)=1/2`、负系数、目标越界、a=0 恒等 / 矛盾、非有限输入、周期溢出和超大相位精度边界。
- 代码提交：初版 `1d2924b5d28f3013c8dfdc29b3b640c526c11635`；将解族归一化改为调用内局部结果类型、移除共享可变错误状态的修正提交 `b049e2ee6880524d5698c1d19df3966925821a0a`。
- Build #438（Run ID `43552419920`，初版 SHA `1d2924b5d28f3013c8dfdc29b3b640c526c11635`）完整 success；Build #439（Run ID `43552472439`，最终 SHA `b049e2ee6880524d5698c1d19df3966925821a0a`）完整 success。最终主 Gradle 步骤 BUILD SUCCESSFUL in 46s；Markdown 链接检查、资源检查、全部 JVM 测试、Android 测试源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过。
- 最新 Debug Artifact：`14096494162`，12,629,769 bytes。
- 未执行模拟器 / 真机 instrumentation tests；按用户要求，真机验收继续暂缓。
- 保持纯 Kotlin domain；未改 Parser、Compose、专用键盘或计算器 UI。

**下一步：** 进入 C5.13，继续选择一个高中常见方程形式，优先关注方程区间条件、解集表达与可验证的原式残差；其它三角函数仍未实现。
## 45. C5.13：弧度制余弦周期方程（2026-11-14）

新增纯 Kotlin 余弦方程内核，支持 `cos(a*x+b)=c`，所有角度采用弧度制；周期解族复用 C5.12 的 `PeriodicSolutionFamily`。

- 新增 `CosineEquation` 与 `CosineEquationSolutionResult`，输入系数、相位、目标均使用 Double。
- 对 a≠0 使用 α=acos(c)，生成 `a*x+b=α+2kπ` 与 `a*x+b=−α+2kπ` 两个分支；周期为 `2π/|a|`，基准值归一化到 `[0, period)`。
- 当 c=±1 时两个角度分支重合，只返回一个周期解族；负系数仍返回正周期。
- 对 a=0 直接分类 `cos(b)=c`，返回 AllRealNumbers 或 NoSolutions；目标超出 [-1,1] 则无解。
- 与正弦 solver 一样检查非有限输入、周期溢出、相位归一化精度和候选解回代残差，复用既有 PolynomialErrorCode。
- 新增 11 个 JUnit 测试方法，覆盖 `cos(x)=0` 两解族、c=±1 单解族、`cos(2x+π/3)=1/2`、负系数、目标越界、a=0 恒等 / 矛盾、非有限输入、周期溢出和超大相位精度边界。
- 代码提交：`5a930bd1f58f465cbbaf8392c1877338496fa669`。
- Build #440（Run ID `43552940320`，代码 SHA `5a930bd1f58f465cbbaf8392c1877338496fa669`）完整 success：Markdown 链接检查、资源检查、全部 JVM 单元测试、Android 测试源码编译、Debug APK 组装、Artifact 上传和 Actions 清理均通过；主 Gradle 步骤 `BUILD SUCCESSFUL in 48s`，清理构建 `BUILD SUCCESSFUL in 5s`。
- Debug Artifact：`14097190828`，12,633,478 bytes。
- 未执行模拟器 / 真机 instrumentation tests；按用户要求，真机验收继续暂缓。
- 保持纯 Kotlin domain；未改 Parser、Compose、专用键盘或计算器 UI。

**下一步：** C5.14 正切周期方程已实现。进入 C5.15 时优先补充周期解族在给定区间内的有限解筛选；其它复合三角式、混合三角方程和参数分类仍未实现。

## 46. C5.14：弧度制正切周期方程（2026-11-14）

新增纯 Kotlin 正切方程内核，支持 `tan(a*x+b)=c`，全部角度采用弧度制。范围限制为线性角表达式和有限实数目标；不包含混合三角式、复合角结构或区间内筛选。

- 新增 `TangentEquation` 与 `TangentEquationSolutionResult`，明确区分全体实数、无解、一个周期解族和类型化失败。
- 对 a≠0，使用 α=atan(c) 得到 `a*x+b=α+kπ`，周期为 `π/|a|`；将基准解归一化到 `[0, period)`，复用既有 `PeriodicSolutionFamily`。
- 对 a=0，先检查常数角是否位于正切定义域；若落在 `π/2+kπ` 极点则判无解，否则比较 `tan(b)=c`，分类为恒等或矛盾。
- 极点判断使用可表示的规范极点重建和精确匹配，避免用宽松的 cos 容差把紧邻极点、但仍有有限正切值的角度误判为无定义。
- 检查非有限输入、周期溢出、周期数超出 Double 可靠整数范围、基准值下溢、回代角度 / 正切值非有限，以及原式归一化残差；复用既有 `PolynomialErrorCode`。
- 新增 13 个 JUnit 测试方法，覆盖 `tan(x)=0`、正负目标、线性角与相位、负系数、a=0 恒等 / 矛盾 / 极点、紧邻极点的有限角度、非有限输入、周期溢出、超大相位精度和超大目标回代精度。
- 代码提交：`a027ee7d6b0de507277630cb6c98cc543db097cd`。
- Android Build #441（Run ID `43578324526`）已触发，当前状态为 in progress；CI 完成前不标记为通过。
- 未执行模拟器 / 真机 instrumentation tests；按用户要求，真机验收继续暂缓。
- 保持纯 Kotlin domain；未改 Parser、Compose、专用键盘或计算器 UI。

**边界与后续：** C5.14 只支持 `tan(ax+b)=c` 的全体实数周期解族；不代表支持正切函数复合式、混合三角方程或区间内筛选。下一步 C5.15 优先对现有正弦 / 余弦 / 正切周期解族实施给定区间内的有限解筛选。
