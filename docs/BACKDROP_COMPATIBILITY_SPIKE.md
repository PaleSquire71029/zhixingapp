# Backdrop 兼容性验证记录

> 验证状态：通过（CI Run #39）
> 验证分支：\`experiment/backdrop-2.0.1-compat\`
> 关联 PR：#4
> 当前采用候选：\`io.github.kyant0:backdrop:1.0.6\`
> 此记录只证明依赖和公开 API 可编译，不代表 UI 与真机性能验收完成。

## 最终验证组合

| 组件 | 验证版本 |
|---|---|
| Backdrop | 1.0.6 |
| Android Gradle Plugin | 9.1.1 |
| Kotlin Compose 插件 | 2.4.21 |
| Gradle Wrapper | 9.3.1 |
| compileSdk / targetSdk | 36 / 36 |
| minSdk | 26 |
| CI JDK | 17 |

Backdrop 1.0.6 上游 Android 工程采用 AndroidX Compose 1.10.3、compileSdk 36 和 Apache-2.0 许可证。该版本暴露了本项目后续封装所需的公开 API：\`rememberLayerBackdrop\`、\`Modifier.layerBackdrop\`、\`Modifier.drawBackdrop\`、\`blur\`、\`lens\`。

## CI 结果

**Run #39：通过**

https://github.com/PaleSquire71029/zhixingapp/actions/runs/41226739564

实际通过的步骤：

- Android SDK 安装与图片资源校验；
- Gradle 配置；
- \`:app:compileDebugKotlin\`；
- \`:app:testDebugUnitTest\`；
- \`:app:assembleDebug\`。

最终日志：\`BUILD SUCCESSFUL in 1m 53s\`。

CI 将 Debug APK 作为 PR workflow artifact 临时上传用于构建验证；没有执行把 APK 提交回 main 的步骤，也没有生成正式发布包。

## 被排除的候选：Backdrop 2.0.1

2.0.1 的 Android artifact / Compose 1.12.0 依赖声明要求 \`compileSdk 37\`。当时 CI Runner 的 SDK 仓库无法找到 \`platforms;android-37\`。回退到 API 36 后，AAR metadata 明确报告多个 AndroidX Compose 1.12.0 组件要求 API 37。

因此，本项目当前阶段不采用 2.0.1，不为它单独升级整个 SDK 环境。该判断针对当前构建流水线和当前 App 基线；未来等 API 37 稳定可用并完成兼容性验证后，可以重新评估。

## 试验过程中修正的问题

- 第一次 CI 停在 Android SDK 包安装：Runner 找不到 API 37 平台包。
- 第二次 CI 在 AAR metadata 检查失败：2.0.1 依赖要求 API 37。
- 第三次 CI 使用 1.0.6 时，失败原因是探针文件误导入了 \`matchParentSize\`；改为 \`fillMaxSize\` 后解决。
- 第四次 CI 全部通过。

上述试验未修改 MainActivity 的生产导航，也未更改 GlassLab 的现有效果；仅在试验分支添加依赖与编译探针。

## 结论与后续边界

Backdrop 1.0.6 可作为当前 App 的液态玻璃底层渲染依赖。正式集成时仍需：

1. 将背景层与玻璃层放到正确的绘制顺序中，确认动态内容可连续采样；
2. 检查生命周期退出时的渲染开销；
3. 处理 API 26–30 的模糊降级，以及 API 33 以下的折射降级；
4. 在目标真机检查帧连续性、滚动、触控、发热和内存；
5. 再实现知行自有的 GlassSurface、底部导航和材质参数设置。

**当前阶段完成项：** 依赖解析、公开 API 引用编译、既有单元测试和 Debug 构建通过。

**未完成项：** 实时渲染行为、主界面底栏、透明度/模糊设置、其他玻璃控件和真机性能验证。
