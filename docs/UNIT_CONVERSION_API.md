# 单位换算 Domain API 契约

**状态：** C8.7 已完成兼容性回归与迁移验收  
**适用代码：** `app/src/main/java/ci/us/zhixing/app/domain/units`  
**旧版基准：** `PaleSquire71029/calculator/calculator.html` 中的 `ZX_UNITS` / `convertUnit`

## 1. 统一调用入口

单位换算页面和后续输入适配层应调用同一个 domain 入口：

```kotlin
UnitConverter.convert(
    value = parsedFiniteDouble,
    sourceCategoryId = "length",
    sourceUnitId = "m",
    targetCategoryId = "length",
    targetUnitId = "ft",
)
```

调用方必须提供已解析的有限数值，以及源 / 目标类别 ID 和单位 ID。类别与单位 ID 都是稳定标识符，不应使用中文标签、数组下标或经过模糊匹配的字符串代替。

返回值为 `UnitConversionResult`：

- `Success(value)`：成功的 Double 数值。Domain 层不负责小数位格式化、千分位、科学计数法文本或 UI 标签拼接。
- `Failure(error)`：明确的 `UnitConversionError`，调用方应按错误类型展示或恢复，不要把失败转换成 0。

## 2. 兼容的类别、单位与默认选择

类别顺序沿用旧版对象键顺序：

| ID | 类别 | 默认源 → 目标 | 算法 |
|---|---|---|---|
| `length` | 长度 | `m → ft` | LINEAR |
| `area` | 面积 | `m2 → ft2` | LINEAR |
| `volume` | 体积 | `l → ml` | LINEAR |
| `mass` | 质量 | `kg → lb` | LINEAR |
| `temp` | 温度 | `c → f` | AFFINE |
| `speed` | 速度 | `kmh → ms` | LINEAR |
| `time` | 时间 | `h → min` | LINEAR |
| `data` | 数据存储 | `mb → gb` | LINEAR |
| `angle` | 角度 | `deg → rad` | LINEAR |

当前目录是 76 个线性单位定义 + 3 个温度仿射单位定义。每个类别中的单位 ID、标签、顺序、比例 / 偏移元数据和默认选择均以旧版 `ZX_UNITS` 为兼容基准。单位 ID 只在类别范围内唯一；例如 `speed/ms` 是米每秒，`time/ms` 是毫秒。

## 3. 数值语义

### 线性单位

旧版算法为：

```javascript
b.fromBase(a.toBase(v))
```

等价于 `value * sourceScale / targetScale`。Android 使用 `BigDecimal` 与 `DECIMAL128` 计算中间表达式，避免仅因中间基准值超出 Double 范围就拒绝最终仍可表示的结果。对普通有限值，回归测试按旧版算法比较所有类别内有序单位对。

### 温度仿射单位

旧版以摄氏度为基准；Android 以开尔文为基准。对物理有效输入，两种基准表达应得到相同的转换结果。Android 仍允许绝对零度以上的负摄氏 / 华氏温度。

以下是有意加强的安全边界，而不是兼容性回归失败：

- NaN 与正负 Infinity 返回 `NonFiniteInput`，不再返回旧版的 NaN。
- 低于绝对零度的输入返回 `BelowAbsoluteZero`；旧版没有这项物理约束。
- 非有限中间值、溢出结果和非零值下溢为 0 分别返回类型化错误。
- 未知类别 / 单位和跨类别转换均返回类型化错误；不会静默地产生 NaN。
- 对线性转换，BigDecimal 中间计算保留可表示的最终值，不因浮点中间溢出过早失败。

## 4. 错误处理契约

`UnitConversionError` 当前包含：

- `NonFiniteInput`
- `UnknownSourceCategory` / `UnknownTargetCategory`
- `IncompatibleDimensions` / `CategoryMismatch`
- `UnsupportedCategory`
- `UnknownSourceUnit` / `UnknownTargetUnit`
- `BelowAbsoluteZero`
- `NonFiniteIntermediateValue` / `NonFiniteResult`
- `PrecisionUnderflow`

跨类别换算必须拒绝，即使类别含义看起来相近也不能绕过物理量纲和类别验证。错误类型是调用方需要处理的稳定边界；新增错误类型时同步补齐测试与本契约。

## 5. C8.7 验收范围

`LegacyUnitConversionCompatibilityTest` 以旧版 `calculator.html` 公式作为独立参照：

- 76 个线性单位形成 780 个类别内有序源 / 目标单位对；每对使用 5 个数值样例，共 3,900 次端到端转换比对。
- 温度 3 个源单位 × 3 个目标单位 × 5 个物理有效样例，共 45 次转换比对。
- 核验九类历史顺序和全部默认单位对。
- 再次核验重复 ID `ms` 在速度和时间类别内各自解析；跨类别混用必须拒绝。

单位系数的逐项黄金值仍由 `UnitDefinitionCatalogTest` 固定，温度目录和绝对零度边界由 `TemperatureConverterTest` 固定。本回归测试验证端到端算法路径，避免仅靠目录元数据测试推断换算功能正确。

## 6. UI 接入边界

C8.7 不创建 Compose 页面，也不迁移旧版 HTML / JavaScript。后续原生单位换算页面应：

1. 用类别 ID 查询目录，并依据原顺序显示类别。
2. 用单位 ID 保存当前源 / 目标选择，默认值读取目录的 `defaultFromUnitId` / `defaultToUnitId`。
3. 将输入文本解析为有限 Double 后调用 `UnitConverter`；文本解析失败由输入层处理。
4. 对成功结果统一格式化；旧版 `zxDec` 的显示策略属于表现层，不属于本 Domain API。
5. 对 `Failure` 按具体错误类型显示反馈；不要把所有失败都展示成“未定义”或数值 0。
6. “同类单位一览”可对当前类别所有单位重复调用同一入口，禁止绕过统一换算服务。

**迁移验收结论：** 旧版九类单位的目录身份、类别顺序、默认选择和有效输入转换规则均已通过回归；新版同时提供了更严格的数值 / 物理约束和类型化错误。该结论不代表原生单位换算 UI、输入体验或真机交互已经完成。


## 7. C8.8.1 原生页面首版接入

已新增原生 `ToolsDirectoryScreen` 与 `UnitConverterScreen`：

- 从「工具」Tab 进入单位换算二级页面，沿用主壳的 `AnimatedContent` 转场和 Backdrop 材质。
- 类别选择严格使用目录顺序和稳定类别 ID；选择类别时恢复目录中的默认源 / 目标单位。
- 输入框以空字符串启动，输入有效数值后即时计算；空输入不会自动变成零，无法解析的字符串显示输入错误。
- 源 / 目标单位以目录 ID 保存，可分别下拉选择或交换。
- 主结果及「同类单位一览」均通过 `UnitConversionPresenter` → `UnitConverter` 产生；表现层不另写转换公式。
- `UnitValueFormatter` 处理 10 位有效数字与极大 / 极小科学计数文本；错误依据 `UnitConversionError` 显示具体原因。
- 页面暂不写入历史记录；历史持久化尚未接入。复制结果和更完整的输入 / 无障碍交互列入后续 C8.8.2。

Build #424（Run ID `43436816915`）已通过全部 JVM 单测、Android 测试源码编译和 Debug APK 构建。当前没有设备运行记录；构建成功不代表真机视觉或触控验收完成。
