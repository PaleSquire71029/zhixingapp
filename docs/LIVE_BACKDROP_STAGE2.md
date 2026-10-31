# 实时 Backdrop 阶段 2 实施记录

> 对应专项计划：\`LIQUID_GLASS_REDESIGN_PLAN.md\`
> 对应 PR：#5
> 当前状态：等待 Android CI

## 本阶段目标

移除 GlassLab 中每秒刷新一次 Bitmap 的动态背景方案，让动态光斑和玻璃面板通过 Backdrop 1.0.6 的 GraphicsLayer 记录/绘制链路保持同步。

## 实现结构

1. 页面创建唯一的 \`LayerBackdrop\`（\`rememberLayerBackdrop()\`）。
2. 演示背景区域通过 \`Modifier.layerBackdrop(backdrop)\` 录制当前背景绘制内容。
3. 动态光斑仍由 Compose 无限过渡动画驱动；应用进入后台后显示静态光斑，避免后台持续动画。
4. 玻璃区域通过 \`Modifier.drawBackdrop(backdrop = ...)\` 读取背景。
5. 模糊由 Backdrop 的 \`blur()\` 效果控制；折射由 \`lens()\` 控制；高光通过 \`Highlight\` 提供。
6. 不再把背景导出为 \`ImageBitmap\`，不使用定时器刷新快照，也不单独创建旧版 Bitmap Shader。

## 参数

- 透明程度：0–100%，独立调节。为保护文字可读性，最终浅色遮罩保留少量最低 Alpha。
- 背景模糊：0–24 dp，独立调节。
- 折射强度：0–8 dp。
- 边缘高光：独立开关。
- 触摸反馈：轻微缩放和回弹。
- 性能档位：
  - Premium：较高模糊、折射、高光和触摸反馈；
  - Standard：适度模糊、折射与高光；
  - Lite：实时背景但不执行模糊/折射；
  - Off：普通浅色表面，不启用 Backdrop。

## Android API 降级

- API 26–30：Backdrop 作为背景源可用，模糊和折射关闭，采用浅色透明表面。
- API 31–32：允许使用系统 RenderEffect 模糊，折射关闭。
- API 33+：允许模糊与 RuntimeShader lens 折射。

## 验收清单

- [ ] \`:app:compileDebugKotlin\` 通过。
- [ ] \`:app:testDebugUnitTest\` 通过。
- [ ] \`:app:assembleDebug\` 通过。
- [ ] GlassLab 中动态光斑不再每秒跳变。
- [ ] 关闭 App / 页面后台后动画停止。
- [ ] 透明程度与模糊程度独立生效。
- [ ] Off 档不采样玻璃背景。
- [ ] 真机上检查触控、滚动、发热与帧流畅度。

CI 只能验证构建和单元测试，无法替代用户目标设备上的实时动画验收。通过 CI 后仍需进行下一阶段的真机验证和 UI 微调。
