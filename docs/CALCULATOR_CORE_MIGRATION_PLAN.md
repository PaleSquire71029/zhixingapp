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
