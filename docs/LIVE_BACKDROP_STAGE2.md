# 实时 Backdrop 阶段 2 实施记录

> 对应专项计划：\`LIQUID_GLASS_REDESIGN_PLAN.md\`
> 对应 PR：#5
> 当前状态：等待 Android CI

## 实现概要

- 通过 \`rememberLayerBackdrop()\` 创建唯一的共享实时背景源。
- \`Modifier.layerBackdrop()\` 录制渐变底色和动态光斑。
- \`Modifier.drawBackdrop()\` 直接从同一层读取背景，替代每秒一次的 ImageBitmap 快照。
- 删除 \`toImageBitmap()\`、1 秒延时循环、快照计数和旧位图 Shader 绑定。
- 透明程度和背景模糊分别使用独立滑块。
- 保留轻微折射、边缘高光和触摸缩放反馈。

## 性能模式

| 模式 | 背景 | 模糊 | 折射 | 交互反馈 |
|---|---|---|---|---|
| Premium | 实时 | 较高 | 开 | 开 |
| Standard | 实时 | 适中 | 开 | 开 |
| Lite | 实时 | 关 | 关 | 关 |
| Off | 普通浅色面 | 关 | 关 | 关 |

## Android API 降级

- API 26–30：使用浅色透明表面，不启用模糊和折射。
- API 31–32：允许系统 RenderEffect 模糊，关闭折射。
- API 33+：允许模糊与 RuntimeShader lens 折射。

## CI 验收

- [ ] \`:app:compileDebugKotlin\`
- [ ] \`:app:testDebugUnitTest\`
- [ ] \`:app:assembleDebug\`

CI 通过只证明代码能编译和现有单元测试通过。动态背景是否丝滑、设备发热和滚动表现仍需目标真机验证。
