package ci.us.zhixing.app.ui.lab

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import ci.us.zhixing.app.ui.glass.BackdropHost
import ci.us.zhixing.app.ui.glass.DEFAULT_HOME_GLASS_MATERIAL
import ci.us.zhixing.app.ui.glass.GlassMaterialState
import ci.us.zhixing.app.ui.glass.ZhixingGlassSurface
import ci.us.zhixing.app.ui.glass.ZhixingLiquidButton
import ci.us.zhixing.app.ui.glass.ZhixingLiquidSlider
import ci.us.zhixing.app.ui.glass.ZhixingLiquidToggle
import ci.us.zhixing.app.ui.theme.ZhixingGlassShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Material settings use a grouped, light system-settings layout.
 * Controls keep their original state, ranges and persistence behaviour.
 */
@Composable
internal fun GlassLabScreen(
    onBack: () -> Unit,
    initialMaterial: GlassMaterialState,
    onMaterialChange: (GlassMaterialState) -> Unit,
    onMaterialCommit: (GlassMaterialState) -> Unit,
    onMaterialReset: () -> Unit,
) {
    val apiLevel = Build.VERSION.SDK_INT
    val blurSupported = apiLevel >= Build.VERSION_CODES.S
    val refractionSupported = apiLevel >= Build.VERSION_CODES.TIRAMISU

    var selectedTier by rememberSaveable {
        mutableStateOf(if (initialMaterial.enabled) GlassPerformanceTier.CUSTOM else GlassPerformanceTier.OFF)
    }
    var blurRadiusDp by rememberSaveable { mutableFloatStateOf(initialMaterial.blurRadiusDp) }
    var refractionEnabled by rememberSaveable { mutableStateOf(initialMaterial.refractionEnabled) }
    var refractionStrength by rememberSaveable { mutableFloatStateOf(initialMaterial.refractionStrengthDp) }
    var chromaticDispersionEnabled by rememberSaveable {
        mutableStateOf(initialMaterial.chromaticDispersionEnabled)
    }
    var edgeHighlightEnabled by rememberSaveable { mutableStateOf(initialMaterial.edgeHighlightEnabled) }
    var environmentReflectionEnabled by rememberSaveable {
        mutableStateOf(initialMaterial.environmentReflectionEnabled)
    }
    var touchFeedbackEnabled by rememberSaveable {
        mutableStateOf(initialMaterial.touchFeedbackEnabled)
    }
    var transparencyPercent by rememberSaveable {
        mutableFloatStateOf(initialMaterial.transparencyPercent.toFloat())
    }

    val backdropEnabled = selectedTier != GlassPerformanceTier.OFF

    fun currentMaterial(): GlassMaterialState = GlassMaterialState(
        transparencyPercent = transparencyPercent.roundToInt(),
        blurRadiusDp = blurRadiusDp,
        enabled = backdropEnabled,
        refractionEnabled = refractionEnabled,
        refractionStrengthDp = refractionStrength,
        chromaticDispersionEnabled = chromaticDispersionEnabled,
        edgeHighlightEnabled = edgeHighlightEnabled,
        environmentReflectionEnabled = environmentReflectionEnabled,
        touchFeedbackEnabled = touchFeedbackEnabled,
    )

    fun publishMaterial(commit: Boolean = false) {
        val material = currentMaterial()
        onMaterialChange(material)
        if (commit) onMaterialCommit(material)
    }

    fun resetMaterial() {
        selectedTier = GlassPerformanceTier.CUSTOM
        transparencyPercent = DEFAULT_HOME_GLASS_MATERIAL.transparencyPercent.toFloat()
        blurRadiusDp = DEFAULT_HOME_GLASS_MATERIAL.blurRadiusDp
        refractionEnabled = DEFAULT_HOME_GLASS_MATERIAL.refractionEnabled
        refractionStrength = DEFAULT_HOME_GLASS_MATERIAL.refractionStrengthDp
        chromaticDispersionEnabled = DEFAULT_HOME_GLASS_MATERIAL.chromaticDispersionEnabled
        edgeHighlightEnabled = DEFAULT_HOME_GLASS_MATERIAL.edgeHighlightEnabled
        environmentReflectionEnabled = DEFAULT_HOME_GLASS_MATERIAL.environmentReflectionEnabled
        touchFeedbackEnabled = DEFAULT_HOME_GLASS_MATERIAL.touchFeedbackEnabled
        onMaterialChange(DEFAULT_HOME_GLASS_MATERIAL)
        onMaterialReset()
    }

    fun applyPerformanceTier(tier: GlassPerformanceTier) {
        if (tier == GlassPerformanceTier.OFF) {
            // Off only changes the enabled flag; retain the user's optical values.
            selectedTier = GlassPerformanceTier.OFF
        } else {
            val config = resolveGlassPerformanceConfig(tier, apiLevel)
            selectedTier = tier
            blurRadiusDp = config.blurRadiusDp
            refractionEnabled = config.refractionEnabled
            refractionStrength = config.refractionStrengthPx
            chromaticDispersionEnabled = config.chromaticDispersionEnabled
            edgeHighlightEnabled = config.edgeHighlightEnabled
            environmentReflectionEnabled = tier != GlassPerformanceTier.LITE
            touchFeedbackEnabled = config.touchFeedbackEnabled
            transparencyPercent = ((1f - config.panelOverlayAlpha) * 100f).coerceIn(0f, 100f)
        }
        publishMaterial(commit = true)
    }

    fun markCustom() {
        selectedTier = GlassPerformanceTier.CUSTOM
    }

    // Keep the settings controls optically calm and text perfectly sharp.
    val settingsMaterial = currentMaterial().copy(
        refractionEnabled = false,
        chromaticDispersionEnabled = false,
    )

    BackdropHost(modifier = Modifier.fillMaxSize()) { backdrop ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ZhixingLiquidButton(
                    backdrop = backdrop,
                    onClick = onBack,
                    touchFeedbackEnabled = touchFeedbackEnabled,
                    shape = ZhixingGlassShapes.pill,
                ) {
                    Text(
                        text = "‹  返回",
                        style = ZhixingTheme.type.featureTitle,
                        color = ZhixingTheme.colors.primary,
                    )
                }
                ZhixingLiquidButton(
                    backdrop = backdrop,
                    onClick = { resetMaterial() },
                    touchFeedbackEnabled = touchFeedbackEnabled,
                    shape = ZhixingGlassShapes.pill,
                ) {
                    Text(
                        text = "恢复默认",
                        style = ZhixingTheme.type.supporting,
                        color = ZhixingTheme.colors.primary,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "材质设置",
                    style = ZhixingTheme.type.screenTitle,
                    color = ZhixingTheme.colors.onBackground,
                )
                Text(
                    text = "调整玻璃表面的通透度、光学效果与触摸反馈。",
                    style = ZhixingTheme.type.body,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }

            SettingsSection(
                title = "性能模式",
                trailing = tierLabel(selectedTier),
                backdrop = backdrop,
                material = settingsMaterial,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    PerformanceTierChip(
                        backdrop = backdrop,
                        label = "高性能",
                        selected = selectedTier == GlassPerformanceTier.PREMIUM,
                        touchFeedbackEnabled = touchFeedbackEnabled,
                        modifier = Modifier.weight(1f),
                    ) { applyPerformanceTier(GlassPerformanceTier.PREMIUM) }
                    PerformanceTierChip(
                        backdrop = backdrop,
                        label = "标准",
                        selected = selectedTier == GlassPerformanceTier.STANDARD,
                        touchFeedbackEnabled = touchFeedbackEnabled,
                        modifier = Modifier.weight(1f),
                    ) { applyPerformanceTier(GlassPerformanceTier.STANDARD) }
                    PerformanceTierChip(
                        backdrop = backdrop,
                        label = "轻量",
                        selected = selectedTier == GlassPerformanceTier.LITE,
                        touchFeedbackEnabled = touchFeedbackEnabled,
                        modifier = Modifier.weight(1f),
                    ) { applyPerformanceTier(GlassPerformanceTier.LITE) }
                    PerformanceTierChip(
                        backdrop = backdrop,
                        label = "关闭",
                        selected = selectedTier == GlassPerformanceTier.OFF,
                        touchFeedbackEnabled = touchFeedbackEnabled,
                        modifier = Modifier.weight(1f),
                    ) { applyPerformanceTier(GlassPerformanceTier.OFF) }
                }
                Text(
                    text = "按设备性能选择效果强度；关闭后使用普通浅色表面。",
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }

            SettingsSection(
                title = "表面外观",
                backdrop = backdrop,
                material = settingsMaterial,
            ) {
                SliderSetting(
                    title = "透明程度",
                    value = transparencyPercent,
                    valueLabel = { String.format(Locale.US, "%.0f%%", it) },
                    description = "数值越高，背景越清晰；始终保留轻微浅色底，保证内容易读。",
                    onValueCommit = { next ->
                        markCustom()
                        transparencyPercent = next.coerceIn(0f, 100f)
                        publishMaterial(commit = true)
                    },
                ) { liveValueReader, onLiveValueChange, onFinished ->
                    ZhixingLiquidSlider(
                        backdrop = backdrop,
                        value = liveValueReader,
                        onValueChange = onLiveValueChange,
                        onValueChangeFinished = onFinished,
                        valueRange = 0f..100f,
                        steps = 19,
                        label = "透明程度",
                        touchFeedbackEnabled = touchFeedbackEnabled,
                    )
                }

                SettingsDivider()

                SliderSetting(
                    title = "背景模糊",
                    value = blurRadiusDp,
                    valueLabel = { String.format(Locale.US, "%.1f dp", it) },
                    description = if (blurSupported) {
                        "控制玻璃下方背景的柔化程度。"
                    } else {
                        "当前 Android 版本不支持实时背景模糊，将使用浅色透明材质。"
                    },
                    onValueCommit = { next ->
                        markCustom()
                        blurRadiusDp = next.coerceIn(0f, 24f)
                        publishMaterial(commit = true)
                    },
                ) { liveValueReader, onLiveValueChange, onFinished ->
                    ZhixingLiquidSlider(
                        backdrop = backdrop,
                        value = liveValueReader,
                        onValueChange = onLiveValueChange,
                        onValueChangeFinished = onFinished,
                        valueRange = 0f..24f,
                        steps = 23,
                        label = "背景模糊",
                        enabled = blurSupported && backdropEnabled,
                        touchFeedbackEnabled = touchFeedbackEnabled,
                    )
                }
            }

            SettingsSection(
                title = "光学效果",
                backdrop = backdrop,
                material = settingsMaterial,
            ) {
                GlassToggleRow(
                    backdrop = backdrop,
                    title = "边缘折射",
                    description = if (refractionSupported) {
                        "在玻璃边缘产生轻微透镜位移。"
                    } else {
                        "需要 Android 13 或更新版本。"
                    },
                    checked = refractionEnabled,
                    enabled = refractionSupported && backdropEnabled,
                    touchFeedbackEnabled = touchFeedbackEnabled,
                    label = "边缘折射",
                ) { checked ->
                    markCustom()
                    refractionEnabled = checked
                    publishMaterial(commit = true)
                }

                SettingsDivider()

                SliderSetting(
                    title = "折射强度",
                    value = refractionStrength,
                    valueLabel = { String.format(Locale.US, "%.1f dp", it) },
                    description = "调整边缘透镜的位移幅度。",
                    onValueCommit = { next ->
                        markCustom()
                        refractionStrength = next.coerceIn(0f, 8f)
                        publishMaterial(commit = true)
                    },
                ) { liveValueReader, onLiveValueChange, onFinished ->
                    ZhixingLiquidSlider(
                        backdrop = backdrop,
                        value = liveValueReader,
                        onValueChange = onLiveValueChange,
                        onValueChangeFinished = onFinished,
                        valueRange = 0f..8f,
                        steps = 15,
                        label = "折射强度",
                        enabled = refractionSupported && refractionEnabled && backdropEnabled,
                        touchFeedbackEnabled = touchFeedbackEnabled,
                    )
                }

                SettingsDivider()

                GlassToggleRow(
                    backdrop = backdrop,
                    title = "色散折射",
                    description = "只在玻璃边缘产生轻微色彩分离，避免影响正文。",
                    checked = chromaticDispersionEnabled,
                    enabled = refractionSupported && refractionEnabled && backdropEnabled,
                    touchFeedbackEnabled = touchFeedbackEnabled,
                    label = "色散折射",
                ) { checked ->
                    markCustom()
                    chromaticDispersionEnabled = checked
                    publishMaterial(commit = true)
                }

                SettingsDivider()

                GlassToggleRow(
                    backdrop = backdrop,
                    title = "边缘高光",
                    description = "强化玻璃轮廓的细腻反射，不使用粗白描边。",
                    checked = edgeHighlightEnabled,
                    enabled = backdropEnabled,
                    touchFeedbackEnabled = touchFeedbackEnabled,
                    label = "边缘高光",
                ) { checked ->
                    markCustom()
                    edgeHighlightEnabled = checked
                    publishMaterial(commit = true)
                }

                SettingsDivider()

                GlassToggleRow(
                    backdrop = backdrop,
                    title = "环境反射",
                    description = "叠加柔和环境光，让玻璃表面更有空间层次。",
                    checked = environmentReflectionEnabled,
                    enabled = backdropEnabled,
                    touchFeedbackEnabled = touchFeedbackEnabled,
                    label = "环境反射",
                ) { checked ->
                    markCustom()
                    environmentReflectionEnabled = checked
                    publishMaterial(commit = true)
                }
            }

            SettingsSection(
                title = "触摸反馈",
                backdrop = backdrop,
                material = settingsMaterial,
            ) {
                GlassToggleRow(
                    backdrop = backdrop,
                    title = "触摸光效与形变",
                    description = "按压时高光跟随指尖，控件轻微压缩并自然回弹。",
                    checked = touchFeedbackEnabled,
                    enabled = backdropEnabled,
                    touchFeedbackEnabled = touchFeedbackEnabled,
                    label = "触摸光效与形变",
                ) { checked ->
                    markCustom()
                    touchFeedbackEnabled = checked
                    publishMaterial(commit = true)
                }
            }

            Text(
                text = "部分光学效果受 Android 系统版本限制；不可用的选项会自动停用。",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    backdrop: Backdrop,
    material: GlassMaterialState,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.secondary,
                )
            }
        }
        ZhixingGlassSurface(
            backdrop = backdrop,
            material = material,
            modifier = Modifier.fillMaxWidth(),
            shape = ZhixingGlassShapes.card,
            glassEnabled = material.enabled,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun SliderSetting(
    title: String,
    value: Float,
    valueLabel: (Float) -> String,
    description: String,
    onValueCommit: (Float) -> Unit,
    slider: @Composable (
        valueReader: () -> Float,
        onLiveValueChange: (Float) -> Unit,
        onFinished: () -> Unit,
    ) -> Unit,
) {
    // The settings row itself never reads the changing value. The numeric
    // label is an isolated restart scope; the liquid slider receives a stable
    // reader and animates its GPU layer without rebuilding the Backdrop subtree.
    val liveValue = remember(value) { mutableFloatStateOf(value) }
    val liveValueReader = remember(liveValue) { { liveValue.floatValue } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = ZhixingTheme.type.featureTitle,
                color = ZhixingTheme.colors.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            SliderValueLabel(
                value = liveValue,
                formatter = valueLabel,
            )
        }
        slider(
            liveValueReader,
            { next -> liveValue.floatValue = next },
            { onValueCommit(liveValue.floatValue) },
        )
        Text(
            text = description,
            style = ZhixingTheme.type.supporting,
            color = ZhixingTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SliderValueLabel(
    value: MutableFloatState,
    formatter: (Float) -> String,
) {
    Text(
        text = formatter(value.floatValue),
        style = ZhixingTheme.type.supporting,
        color = ZhixingTheme.colors.secondary,
    )
}

@Composable
private fun GlassToggleRow(
    backdrop: Backdrop,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    touchFeedbackEnabled: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = ZhixingTheme.type.featureTitle,
                color = if (enabled) ZhixingTheme.colors.onSurface
                else ZhixingTheme.colors.onSurfaceVariant,
            )
            Text(
                text = description,
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }
        ZhixingLiquidToggle(
            backdrop = backdrop,
            checked = checked,
            enabled = enabled,
            touchFeedbackEnabled = touchFeedbackEnabled,
            label = label,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ZhixingTheme.colors.outlineVariant.copy(alpha = 0.48f)),
    )
}

@Composable
private fun PerformanceTierChip(
    backdrop: Backdrop,
    label: String,
    selected: Boolean,
    touchFeedbackEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ZhixingLiquidButton(
        backdrop = backdrop,
        onClick = onClick,
        modifier = modifier.height(40.dp),
        touchFeedbackEnabled = touchFeedbackEnabled,
        selected = selected,
        shape = ZhixingGlassShapes.pill,
    ) {
        Text(
            text = label,
            style = ZhixingTheme.type.supporting,
            color = if (selected) ZhixingTheme.colors.primary
            else ZhixingTheme.colors.onSurfaceVariant,
        )
    }
}

private fun tierLabel(tier: GlassPerformanceTier): String = when (tier) {
    GlassPerformanceTier.PREMIUM -> "高性能"
    GlassPerformanceTier.STANDARD -> "标准"
    GlassPerformanceTier.LITE -> "轻量"
    GlassPerformanceTier.OFF -> "关闭"
    GlassPerformanceTier.CUSTOM -> "自定义"
}
