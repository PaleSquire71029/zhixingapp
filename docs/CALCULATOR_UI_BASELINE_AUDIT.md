# Calculator UI 基线审计（F6.1 / UI-0）

> 审计日期：2026-11-27（UTC+8）  
> 审计代码基线：PR #92 `docs/ui-first-calculator-roadmap`，commit `b4dac02bbea9f2190898911138e3da27bbdba5e6`  
> 对照基线：main `165a0ca2ff77aeae0868d8dbd2087c09cd9e6260`  
> 本文性质：只读代码审计与下一阶段交接；没有改动数学算法或应用页面代码。

## 1. 审计结论

1. **计算 Tab 仍是占位页。** `MainActivity` 已把页面工作交给 `ZhixingAppRoot`；`AppRootRoute.CALCULATOR` 是 index 0、route `calculator`、Saveable key `root-calculator`。根 Shell 当前没有 `CalculatorScreen`、表达式编辑器、应用内键盘或 Calculator ViewModel；计算与 AI 共用通用介绍型页面分支。
2. **标准四则已有可直接复用的本地入口。** `MathArithmeticCalculator.calculate(source)` 已串联 Tokenizer → Parser → Evaluator，输出 `MathCalculationResult.Success(Double)` 或带 stage / message / offset / lexeme 的 `Failure`。标准模式 UI 不应复制 tokenizer、AST parser 或数值求值逻辑。
3. **题型 Solver 并不是原始字符串入口。** 多项式 / 有理方程、不等式 solver 接收已构造的 Domain 模型；当前 `MathExpressionParser` 只解析单条算术表达式，不识别等号 / 比较符，也未发现统一的原始字符串 → 方程 / 不等式模型 adapter。首版计算器不应把这些尚未接好的 solver 宣称为可输入功能。
4. **函数数值内核已存在，图形 UI 不存在。** 已有拟合、求值、定义域和安全采样模型，可供后续函数工作区接入；不属于 UI-1。
5. **有可复用的基础组件和测试范式。** 主题九类 Tokens、`ZhixingButton` / `ZhixingInput` / `ZhixingSurface` / `ZhixingResultPanel` / `ZhixingSegmentedControl` 已存在；`UnitConversionPresentation.kt` 体现纯 Kotlin Presenter + Presentation State 的做法，可作为分层参考。

## 2. 真实页面入口与 Shell 边界

| 位置 | 现状 | F6 使用约束 |
|---|---|---|
| `app/src/main/java/ci/us/zhixing/app/MainActivity.kt` | Window / edge-to-edge / adjustResize / setContent；注入 `GlassMaterialSettingsStore` | 不把计算业务逻辑放回 Activity |
| `app/src/main/java/ci/us/zhixing/app/ui/shell/ZhixingAppRoot.kt` | 持有 root / secondary Saveable 状态、Backdrop、转场和 Dock；普通 Calculator route 仍进通用占位分支 | 在现有 CALCULATOR 分支替换占位内容；保留 Saveable key、Backdrop Host、PageViewport、Dock sibling |
| `app/src/main/java/ci/us/zhixing/app/navigation/AppDestination.kt` | `AppRootRoute.CALCULATOR(0, "calculator", "root-calculator")`；四个入口顺序稳定 | 不改 route / index / key |
| `app/src/main/java/ci/us/zhixing/app/ui/shell/ZhixingPageViewport.kt` | 根页面 viewport 与 Dock overlay 为 sibling | 计算页不拥有导航 Dock，也不将 Dock 放入可滚动内容 |
| `app/src/main/java/ci/us/zhixing/app/ui/foundation/scroll/ZhixingRigidOverscrollEffect.kt` | 包含统一页面滚动物理 / 容器实现 | 页面内容需要滚动时复用既有刚性整页效果；不再自建第二套回弹 |

当前尚不存在 `app/src/main/java/ci/us/zhixing/app/ui/screens/calculator/CalculatorScreen.kt` 或 Calculator 输入 / 键盘文件。

## 3. 标准算术 Domain API

### 3.1 单一入口

`app/src/main/java/ci/us/zhixing/app/domain/math/evaluator/MathArithmeticCalculator.kt`：

```kotlin
MathArithmeticCalculator.calculate(source: String): MathCalculationResult
```

处理链：

1. `MathTokenizer.tokenize(source)` → `TokenizationResult.Success(tokens)` 或 `Failure`。
2. `MathExpressionParser.parse(tokens, sourceLength)` → AST / `ExpressionParseResult.Failure`。
3. `MathExpressionEvaluator.evaluate(expression)` → `MathEvaluationResult.Success(value)` 或 `Failure`。
4. `MathArithmeticCalculator` 将其统一为 `MathCalculationResult`。

成功返回 `Double`。当前接口本身不负责格式化、精度展示、光标编辑、表达式选择或 Compose 状态。

### 3.2 语法范围

`MathTokenKind` 目前包括：

- NUMBER、VARIABLE_X；
- PLUS、MINUS、MULTIPLY、DIVIDE、POWER；
- LEFT_PAREN、RIGHT_PAREN、PERCENT。

Tokenizer 已归一化 `×` / `✕` / `✖`、`÷`、全角斜线、不同 Unicode 减号、全角括号、全角 x，以及上标 `²` / `³`；会保留原始 UTF-16 source offset。Parser 支持显式乘除、相邻因子隐式乘法、括号、一元正负、百分号和幂。指数限定为无符号整数链，计算上限为 64。

**当前不支持** 函数标识符（如 `sin` / `log`）、常数符号键、等号 / 比较符、任意表达式指数或多行方程输入。数字键盘不能因为视觉上提供某个符号，就把它当作已有 Parser 能力。

### 3.3 AST 与错误

`MathExpression.kt` 定义 `MathExpression` sealed AST：Number / Variable / Unary / Binary / Percent / Group；节点带 `MathSourceSpan`。Parser 错误 code 包括 EMPTY_EXPRESSION、INCOMPLETE_EXPRESSION、UNMATCHED_LEFT_PARENTHESIS、EXTRA_RIGHT_PARENTHESIS、UNEXPECTED_TOKEN、TRAILING_TOKENS、INVALID_EXPONENT、EXPONENT_TOO_LARGE。

`MathEvaluationErrorCode` 包含 VARIABLE_NOT_ALLOWED、DIVISION_BY_ZERO、NON_FINITE_RESULT。Tokenization 错误 code 包含 INVALID_NUMBER、UNKNOWN_CHARACTER。

**已发现的表现层边界：** `MathCalculationError` 只保留 `stage` / `message` / `offset` / `lexeme`，顶层 wrapper 没有继续保留底层具体 error code。首屏可以先展示现有错误文案和定位；若 UI 需要把除零、非法变量、非有限结果做独立样式，需另行增加最小 typed cause 透传，不允许通过匹配中文 message 猜错误类别。

## 4. 其他 Domain 能力与 UI 接入前提

| 能力 | 模型 / API 入口 | 当前 UI 接入状态 |
|---|---|---|
| 多项式运算 / 实根 | `Polynomial`（系数按常数项起升序）；`PolynomialArithmetic` 的 add / subtract / multiply / evaluate / solveRealRoots | 无通用表达式 → Polynomial UI adapter |
| 多项式方程 | `PolynomialEquation(left, right)` → `PolynomialEquationSolver.solve(...)`；结果区分全体实数 / 无解 / 有限根 / 不支持次数 / Failure | 需要构造方程两侧 Polynomial；Parser 不识别 `=` |
| 有理方程 / 分式约束 | `RationalFunction` 携带原始 `domainRestrictions`；`RationalEquationSolver.solve(RationalEquation)` | 不可仅从约分后分子分母推导原式定义域；需显式 AST → rational adapter |
| 多项式不等式 | `PolynomialInequality(expression, comparison)` → `PolynomialInequalitySolver.solve(...)`；结果为区间、全体实数、空集、不支持次数或 Failure | Parser 当前没有 `<` / `>` / `≤` / `≥` token |
| 函数拟合 | Linear / Quadratic / Cubic / InverseProportion fitters；坐标点集校验 | 无 Compose 函数输入 / 拟合工作区 |
| 函数求值与图像采样 | `FunctionEvaluator.evaluate(function, x)`；`FunctionGraphSampler.sample(function, request)`，返回分段 polyline 与排除点 | 数值内核存在，图表、坐标轴、视口、手势 UI 未实现 |
| 三角 / 绝对值 / 根式 / 指数对数方程 | 多个独立 model + solver + typed result | 不是统一字符串服务；后续按独立题型 adapter 与 UI 计划接入 |

## 5. 测试资产

已有主要 JVM 测试路径：

- `app/src/test/java/ci/us/zhixing/app/domain/math/parser/MathTokenizerTest.kt`
- `app/src/test/java/ci/us/zhixing/app/domain/math/parser/MathExpressionParserTest.kt`
- `app/src/test/java/ci/us/zhixing/app/domain/math/evaluator/MathExpressionEvaluatorTest.kt`
- `app/src/test/java/ci/us/zhixing/app/domain/math/golden/CalculatorGoldenCases.kt` 与 `CalculatorGoldenCasesTest.kt`（tokenizer / arithmetic / equations / inequalities 共用黄金夹具）
- `app/src/test/java/ci/us/zhixing/app/domain/math/algebra/`、`solver/`、`functions/` 与 `symbolic/` 下各专项测试。

UI-1 新增测试建议：

1. `CalculatorEditorStateTest`：光标 / 选区插入、替换、退格、清除、括号和按键 reducer；不依赖 Android。
2. `CalculatorPresenterTest`：标准算术成功、空 / 未完成 / parse error、除零、变量、不有限结果映射；复用 Golden Cases，不在测试中重新算公式。
3. `CalculatorScreenTest`（androidTest 源码）：数字键按序输入、括号 / 运算符 / AC / 退格 / 等号、结果语义、Dock 与安全区；当前 CI 只编译这类源码，不会自动运行模拟器。

测试基础是 JUnit 4.13.2 与 Compose UI test。当前没有 ViewModel / navigation compose 专用依赖；不为首屏无实际需要地增加依赖。

## 6. 可复用的 Zhixing Flow UI 资产

- Theme：`ui/theme/ZhixingTheme.kt`，暴露 Color / Typography / Spacing / Shape / Elevation / Material / Motion / Interaction / Layout 九类 Tokens。
- 基础组件：`ui/components/ZhixingButton.kt`、`ZhixingInput.kt`、`ZhixingSurface.kt`、`ZhixingResultPanel.kt`、`ZhixingSegmentedControl.kt`、`ZhixingListRow.kt`、`ZhixingSheet.kt`。
- 纯 Kotlin Presentation 范例：`ui/units/UnitConversionPresentation.kt`，把输入状态 / 不完整输入 / 格式错误 / 类型化 Domain Failure / Converted 结果组织成 Presentation State；可参照职责划分，不复用单位换算逻辑。
- 页面层级：root Shell 继续持有 Backdrop / Dock / secondary transition；计算页只实现内容，不自建 Dock 或额外 `NavHost`。
- 标准模式使用应用内数字键盘；默认不主动弹出系统 IME。系统键盘入口、科学函数和增强符号模板另行切片，不在首步全部塞入。

## 7. F6.2 / UI-1 最小实施边界

### 做

- 新增 `ui/screens/calculator/CalculatorScreen.kt`，在 `ZhixingAppRoot` 的 CALCULATOR 分支挂载；根 route / SaveableStateProvider key / Dock / Backdrop 保持原样。
- 新增纯 Kotlin `CalculatorEditorState` + reducer，描述表达式文本与光标 / 选区；实现数字、十进制点、加减乘除、幂、括号、百分号、正负号、退格和 AC 的最小编辑规则。
- 新增纯 Kotlin `CalculatorPresenter`，唯一求值入口委托 `MathArithmeticCalculator.calculate(source)`，并由独立 formatter 输出结果文本；不复制算法。
- 页面提供表达式编辑区、主要结果区、四列五行标准应用内键盘；等号提交计算，实时预览不得覆盖表达式输入。
- 分离空输入、未完成输入、成功、Tokenizer / Parser / Evaluator Failure；优先忠实呈现当前 typed result 能提供的信息。

### 不做

- 不做科学函数面板 / `sin` / `cos` / `log` / 任意指数输入；当前 Parser 不支持。
- 不做方程、不等式、函数图像、化学或 AI 面板；这些是后续独立工作区。
- 不改 Solver 数值算法，不复制 Domain 公式，不新增 C5.19，不加无必要 Gradle 依赖。
- 不重构 Dock / Backdrop / 导航栈，不改变单位换算、历史、材质设置页面。

### 首步验收

- 仅开放当前 Parser 已支持的语法；基本四则、括号、负数、小数、幂、百分号、隐式乘法可从输入到结果端到端完成。
- `-2^2`、连续运算、括号内光标、选区替换、退格、AC、输入中间态要有明确规则和测试。
- 空 / 未完成 / 语法错误 / 除零 / 含 x / 非有限结果不能误显示为 0 或普通成功值。
- 360dp 窄屏、IME / 安全区、Dock 遮挡、TalkBack 语义和连续点按要有验收；instrumentation 源码编译成功不能代替真机操作。

## 8. 下一步

F6.1 / UI-0 基线审计完成。当前进入 F6.2 / UI-1：先提交纯 Kotlin 编辑状态和 Presenter 测试，再实现 `CalculatorScreen` 与标准应用内键盘。

## 9. 审计后的实现进度（2026-11-27）

本节记录审计完成后的后续变化；第 1–8 节的“当前不存在 CalculatorScreen”均描述审计代码基线 `b4dac02bbea9f2190898911138e3da27bbdba5e6`，不是最新代码状态。

- F6.2a 已新增 `ui/calculator/CalculatorEditorState.kt`、`CalculatorPresenter.kt` 及两组 JVM 测试。状态 reducer 覆盖插入 / 替换、选区、退格 / 前删、AC、括号和 UTF-16 surrogate pair 删除；Presenter 继续唯一调用 `MathArithmeticCalculator.calculate(source)`。Build #546（Run ID `46009853613`）通过。
- F6.2b 已新增 `ui/screens/calculator/CalculatorScreen.kt` 并挂载到 `AppRootRoute.CALCULATOR`；保留根 Shell 的路由 / Saveable key / Backdrop / PageViewport / Dock 归属。首版有五行标准应用内键盘、表达式 / 选区保存、结果预览 / 提交与失败反馈。
- Build #549（Run ID `46011425977`）是五行键盘最终布局版本的完整验证：JVM 测试、androidTest 源码编译、Debug APK 组装和 Artifact 上传通过；Artifact 15203873016（31,483,195 bytes）。
- 新增 3 项 Activity instrumentation 测试源码覆盖键盘求值、AC / 退格和不完整表达式错误，但当前 CI 只编译这些测试源码，没有运行模拟器。选区拖动、长表达式、TalkBack、360dp 窄屏和真机视觉仍待验收。
