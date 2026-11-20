# Zhixing Flow 页面迁移与行为保留矩阵

> 基线：main 165a0ca2ff77aeae0868d8dbd2087c09cd9e6260  
> 更新：2026-11-21  
> 原则：先建立框架，再逐页迁移；只迁 UI，不重写 domain。F1 / F2 foundations 与组件已落地，但现有页面仍未迁移；旧页面只有在替代页通过回归后才清理。

## 1. 页面映射总表

| 当前页面 / 入口 | 当前真实状态 | 目标落点（建议） | 迁移阶段 |
|---|---|---|---|
| 计算 Tab | MainActivity 内的介绍型占位布局 | ui/screens/calculator/CalculatorScreen | F6 |
| 知行 AI Tab | MainActivity 内的介绍型占位布局 | ui/screens/ai/AiScreen | F6 |
| 工具 Tab | 当前只有单位换算入口 | ui/screens/tools/ToolsScreen | F5 |
| 我的 Tab | 最近使用空状态 + 历史 / 材质入口 + 版本信息 | ui/screens/my/MyScreen | F5 |
| 历史二级页 | 当前真实空状态；无本地历史存储 | ui/screens/history/HistoryScreen | F5 |
| 材质设置二级页 | Glass Lab，可调材质参数并持久化 | ui/screens/settings/material/MaterialSettingsScreen | F5 |
| 单位换算二级页 | 已实现输入、选择、交换、结果、复制 / 清除 | ui/screens/units/UnitConverterScreen | F5 |
| 浮动玻璃 Dock | 已有独立实现和用户验收记录 | ui/shell/navigation/ZhixingDock facade；底层继续复用现有 glass 文件 | F4（先适配，不重写） |
| Backdrop / glass host | 共享背景采样、surface、按钮、滑块、开关 | ui/glass adapter + ui/foundation/material token | F1–F5 渐进封装 |

目标包名是迁移落点建议，不要求 F1 就创建空文件。按页面交付、测试覆盖、再移除旧路径，避免同时改完所有页面。

## 2. 必须保留的页面行为

### 2.1 计算 Tab

当前只有品牌信息与介绍卡片，不存在可迁移的计算器输入状态、计算按键、结果区或历史联动。F6 应直接使用已建立的数学 domain / evaluator 设计真正工作区，不要把占位卡片当业务需求照搬。

### 2.2 知行 AI Tab

当前只有标题与介绍文案，没有 AI 输入框、消息列表、网络调用或会话状态。F6 单独建立 AI 页面；不要在计算器页面塞入一个常驻聊天面板，也不要虚构已有对话历史。

### 2.3 工具目录

- 当前只列出单位换算一项。
- 不能提前展示未交付的方程、不等式、函数、化学、物理入口，让用户误以为已能使用。
- 新功能 UI 和最小真实能力都通过验收后，再开放对应目录行。
- 目录应使用统一 ListRow / Section Pattern，不复制卡片布局。

### 2.4 我的 / 历史

- My 页面现有版本信息、最近使用空状态和两个入口全部保留。
- 历史页目前是空状态，说明文案明确承认尚未接入存储。
- 不以演示数据伪造历史，不把 AI 云端历史与本地计算历史混为一类。
- 本轮不提前引入 Room / 数据迁移；历史存储单独规划。

### 2.5 材质设置

保留全部 9 个独立材质设置：透明度、模糊、总开关、折射、折射强度、色散、边缘高光、环境反射和触摸反馈。

- DataStore 名称继续为 glass_material_settings。
- 预览态即时反馈；提交 / 松手后写盘，不在 pointer move 每帧写盘。
- 重置只恢复默认设置，不清除其他无关数据。
- 玻璃关闭时仍保留各项 optical preferences，重新开启可恢复。
- PREMIUM / STANDARD / LITE / OFF / CUSTOM 和 Android API 能力降级需继续有效。
- 材质页面自身可滚动控件必须在 F3 的整页回弹手势仲裁中回归测试。

### 2.6 单位换算

单位换算是当前最完整的可用页面，迁移时必须保持：

- 9 个单位类别。
- 76 个线性单位与 3 个温度仿射单位。
- 数值输入、未完成数字的中间态与 64 字符限制。
- 原单位 / 目标单位选择、交换按钮、即时换算。
- 复制完整等式、清除输入。
- 无效输入、量纲不兼容、绝对零度、非有限中间值与精度边界错误提示。
- TalkBack contentDescription / stateDescription 语义。
- 窄屏下可纵向滚动；类别横向 Chips 保持横向手势。
- rememberSaveable 保存 category / source unit / target unit / input / copied key；页面切回恢复状态。
- 业务逻辑继续委托 UnitConversionPresenter 与 domain/units，不在 Composable 复制换算算法。

## 3. F2 核心组件目录（已实现，尚未迁移现有页面）

| 组件 | 职责 | 约束 / 状态 |
|---|---|---|
| `ZhixingSurface` | 普通浅色内容 Surface | 不创建 Backdrop，不做模糊 / 折射 |
| `ZhixingButton` | 主要 / 次要 / 文字操作 | 按压、禁用、加载、选中、焦点描边与状态语义 |
| `ZhixingInput` | 统一文本输入 | Label、Supporting、Error、Disabled、焦点交由 TextField 处理 |
| `ZhixingListRow` | 列表 / 菜单行 | 标题、辅助信息、选中与禁用状态 |
| `ZhixingSegmentedControl` | 同级单选切换 | selectableGroup / RadioButton 语义 |
| `ZhixingSheet` | 内容面板容器 | 只负责 Surface / 标题；显隐与拖拽由上层负责 |
| `ZhixingResultPanel` | 结果 / 错误输出 | 统一结果层级和状态描述 |
| `DesignGalleryScreen` | 组件展示页 | 390dp / 720dp Preview，600dp 单列 / 双列断点；不注册导航 |

新增测试：2 项 JVM 布局阈值测试、5 项 Compose UI 交互 / 语义测试。当前 CI 会执行 JVM 测试并编译 androidTest 源码，但不会启动模拟器执行 instrumentation tests。页面迁移必须等 F5，不得把 Gallery 当成现有业务页面入口。

## 4. App Shell 与系统行为保留

F4 对照当前实现逐项回归：

- 四个一级入口和选中状态。
- 一级 Tab 切换与 Dock 胶囊移动。
- 二级页进入 / 退出转场。
- 返回键和系统返回手势。
- 根 Tab / 二级页面的 Saveable state。
- 系统栏浅色图标、edge-to-edge、safeDrawing 和 adjustResize。
- 浮动 Dock 不参与内容滚动 / overscroll。
- IME 出现时页面可用高度变化和底部操作区避让。
- Backdrop 共享层的绘制顺序与前后景关系。

## 5. 迁移依赖顺序

1. F1 Foundations：所有后续页面可引用一致的 tokens；不动旧页。
2. F2 Components（已完成）：七个基础 primitives / patterns 与独立 gallery 已建立；现有页面仍保持原样。
3. F3 Overscroll：独立完成整页回弹原型和目标设备验收，再封装 ScrollContainer。
4. F4 App Shell：拆分 MainActivity、typed routes、AppState、Dock overlay / page viewport。
5. F5 Existing screens：工具目录 → 我的 / 历史 / 材质设置 → 单位换算。
6. F6 Core experience：计算工作区 → AI 工作区 → 方程 / 不等式 / 函数等工具页。
7. F7 Stabilization：无障碍、窄屏、IME、60Hz / 120Hz、滚动、性能、状态恢复与旧代码清理。

## 6. 页面迁移完成定义

页面不能仅以“看起来相似”判定完成，必须同时满足：

- 所有当前可用交互都有明确的对应组件和行为。
- 状态在切 Tab / 进入二级页 / 返回时按设计恢复。
- 不出现 Dock 遮挡、IME 遮挡、重复 Insets 或最后一项不可见。
- 页面不拥有自己的自定义 overscroll 物理；统一使用 ZhixingScrollContainer。
- 自动化测试覆盖核心状态、语义和交互；设备验收记录手势与视觉表现。
- 旧页在新页完成验收前不删除。
- 业务 domain 的结果、错误模型、单位换算与数学能力保持原样。
