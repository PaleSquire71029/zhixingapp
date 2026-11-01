package ci.us.zhixing.app.ui.lab

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import java.util.Locale

/**
 * Glass Lab stage 2:
 * - one shared Backdrop layer records the live animated background;
 * - glass surfaces sample that layer during Compose drawing;
 * - no ImageBitmap snapshots, timer loop, or per-second refresh.
 */
@Composable
internal fun GlassLabScreen(
    onBack: () -> Unit,
    lifecycle: Lifecycle,
) {
    val apiLevel = Build.VERSION.SDK_INT
    val blurSupported = apiLevel >= Build.VERSION_CODES.S
    val refractionSupported = apiLevel >= Build.VERSION_CODES.TIRAMISU
    val initialConfig = remember {
        resolveGlassPerformanceConfig(GlassPerformanceTier.STANDARD, apiLevel)
    }

    var selectedTier by rememberSaveable { mutableStateOf(GlassPerformanceTier.STANDARD) }
    var blurRadiusDp by rememberSaveable { mutableFloatStateOf(initialConfig.blurRadiusDp) }
    var refractionEnabled by rememberSaveable { mutableStateOf(initialConfig.refractionEnabled) }
    var refractionStrength by rememberSaveable { mutableFloatStateOf(initialConfig.refractionStrengthPx) }
    var edgeHighlightEnabled by rememberSaveable { mutableStateOf(initialConfig.edgeHighlightEnabled) }
    var touchFeedbackEnabled by rememberSaveable { mutableStateOf(initialConfig.touchFeedbackEnabled) }
    var transparencyPercent by rememberSaveable { mutableFloatStateOf(78f) }
    var isLifecycleStarted by remember(lifecycle) {
        mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }

    val backdropEnabled = selectedTier != GlassPerformanceTier.OFF

    fun applyPerformanceTier(tier: GlassPerformanceTier) {
        val config = resolveGlassPerformanceConfig(tier, apiLevel)
        selectedTier = tier
        blurRadiusDp = config.blurRadiusDp
        refractionEnabled = config.refractionEnabled
        refractionStrength = config.refractionStrengthPx
        edgeHighlightEnabled = config.edgeHighlightEnabled
        touchFeedbackEnabled = config.touchFeedbackEnabled
        transparencyPercent = ((1f - config.panelOverlayAlpha) * 100f).coerceIn(0f, 100f)
    }

    fun markCustom() {
        selectedTier = GlassPerformanceTier.CUSTOM
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> isLifecycleStarted = true
                Lifecycle.Event.ON_STOP -> isLifecycleStarted = false
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val panelShape = ZhixingTheme.shapes.hero
    val surfaceOverlayAlpha = (1f - transparencyPercent / 100f).coerceIn(0.08f, 0.96f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZhixingTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ZhixingTheme.spacing.pageHorizontal)
            .padding(vertical = ZhixingTheme.spacing.pageVertical),
        verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.section),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "← 返回主界面",
                    style = ZhixingTheme.type.featureTitle,
                    color = ZhixingTheme.colors.primary,
                )
            }
            Text(
                text = "GLASS LAB / LIVE",
                modifier = Modifier.weight(1f),
                style = ZhixingTheme.type.brandCaption,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
            Text(
                text = "实时液态玻璃",
                style = ZhixingTheme.type.screenTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = "共享 Backdrop 背景层 · 连续绘制 · 不再使用每秒一次的图片快照",
                style = ZhixingTheme.type.body,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
            Text(
                text = "性能模式 · \${tierLabel(selectedTier)}",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PerformanceTierChip(
                    label = "Premium",
                    selected = selectedTier == GlassPerformanceTier.PREMIUM,
                    modifier = Modifier.weight(1f),
                ) { applyPerformanceTier(GlassPerformanceTier.PREMIUM) }
                PerformanceTierChip(
                    label = "Standard",
                    selected = selectedTier == GlassPerformanceTier.STANDARD,
                    modifier = Modifier.weight(1f),
                ) { applyPerformanceTier(GlassPerformanceTier.STANDARD) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PerformanceTierChip(
                    label = "Lite",
                    selected = selectedTier == GlassPerformanceTier.LITE,
                    modifier = Modifier.weight(1f),
                ) { applyPerformanceTier(GlassPerformanceTier.LITE) }
                PerformanceTierChip(
                    label = "Off",
                    selected = selectedTier == GlassPerformanceTier.OFF,
                    modifier = Modifier.weight(1f),
                ) { applyPerformanceTier(GlassPerformanceTier.OFF) }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
            Text(
                text = "透明程度 · \${transparencyPercent.toInt()}%",
                style = ZhixingTheme.type.sectionLabel,
                color = ZhixingTheme.colors.secondary,
            )
            Slider(
                value = transparencyPercent,
                onValueChange = {
                    markCustom()
                    transparencyPercent = it.coerceIn(0f, 100f)
                },
                valueRange = 0f..100f,
                steps = 19,
            )
            Text(
                text = "数值越高，背景越清晰；为保证文字可读，始终保留极轻的浅色底。",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
            Text(
                text = "背景模糊 · ${String.format(Locale.US, "%.1f", blurRadiusDp)} dp",
                style = ZhixingTheme.type.sectionLabel,
                color = ZhixingTheme.colors.secondary,
            )
            Slider(
                value = blurRadiusDp,
                onValueChange = {
                    markCustom()
                    blurRadiusDp = it.coerceIn(0f, 24f)
                },
                valueRange = 0f..24f,
                steps = 23,
                enabled = blurSupported && backdropEnabled,
            )
            if (!blurSupported) {
                Text(
                    text = "当前 Android 版本不支持实时背景模糊，将使用浅色透明材质。",
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (refractionEnabled) "边缘折射 · 开启" else "边缘折射 · 关闭",
                    style = ZhixingTheme.type.sectionLabel,
                    color = ZhixingTheme.colors.secondary,
                )
                Text(
                    text = if (refractionSupported) {
                        "轻微放大边缘背景，形成柔和玻璃光学感"
                    } else {
                        "需要 Android 13 及以上版本"
                    },
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }
            TextButton(
                enabled = refractionSupported && backdropEnabled,
                onClick = {
                    markCustom()
                    refractionEnabled = !refractionEnabled
                },
            ) {
                Text(
                    text = if (refractionEnabled) "关闭" else "开启",
                    style = ZhixingTheme.type.featureTitle,
                    color = ZhixingTheme.colors.primary,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
            Text(
                text = "折射强度 · ${String.format(Locale.US, "%.1f", refractionStrength)} dp",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Slider(
                value = refractionStrength,
                onValueChange = {
                    markCustom()
                    refractionStrength = it.coerceIn(0f, 8f)
                },
                valueRange = 0f..8f,
                steps = 15,
                enabled = refractionSupported && refractionEnabled && backdropEnabled,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (edgeHighlightEnabled) "边缘高光 · 开启" else "边缘高光 · 关闭",
                    style = ZhixingTheme.type.sectionLabel,
                    color = ZhixingTheme.colors.secondary,
                )
                Text(
                    text = "高光集中在上沿和左缘，保持轻薄，不使用粗白边",
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }
            TextButton(
                enabled = backdropEnabled,
                onClick = {
                    markCustom()
                    edgeHighlightEnabled = !edgeHighlightEnabled
                },
            ) {
                Text(
                    text = if (edgeHighlightEnabled) "关闭" else "开启",
                    style = ZhixingTheme.type.featureTitle,
                    color = ZhixingTheme.colors.primary,
                )
            }
        }

        GlassPreviewCard(
            backdropEnabled = backdropEnabled,
            lifecycleStarted = isLifecycleStarted,
            blurRadiusDp = if (blurSupported) blurRadiusDp else 0f,
            refractionEnabled = refractionEnabled && refractionSupported,
            refractionStrength = if (refractionSupported) refractionStrength else 0f,
            edgeHighlightEnabled = edgeHighlightEnabled,
            touchFeedbackEnabled = touchFeedbackEnabled,
            surfaceOverlayAlpha = surfaceOverlayAlpha,
            shape = panelShape,
        )

        Text(
            text = when {
                !isLifecycleStarted -> "应用已进入后台 · 动态光斑已停止"
                !backdropEnabled -> "Off 模式 · 普通浅色表面，不执行玻璃背景采样"
                !blurSupported && !refractionSupported -> "实时背景层已连接 · 当前设备使用浅色透明降级"
                !refractionSupported -> "实时背景层已连接 · 模糊可用，折射按系统版本降级"
                else -> "实时 Backdrop 已连接 · 玻璃面板与动态光斑在同一绘制链路中更新"
            },
            style = ZhixingTheme.type.supporting,
            color = ZhixingTheme.colors.onSurfaceVariant,
        )
    }
}

private fun tierLabel(tier: GlassPerformanceTier): String = when (tier) {
    GlassPerformanceTier.PREMIUM -> "Premium"
    GlassPerformanceTier.STANDARD -> "Standard"
    GlassPerformanceTier.LITE -> "Lite"
    GlassPerformanceTier.OFF -> "Off"
    GlassPerformanceTier.CUSTOM -> "自定义"
}

@Composable
private fun PerformanceTierChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (selected) ZhixingTheme.colors.primary.copy(alpha = 0.14f)
                else Color.White.copy(alpha = 0.72f),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = ZhixingTheme.type.supporting,
            color = if (selected) ZhixingTheme.colors.primary
            else ZhixingTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun GlassPreviewCard(
    backdropEnabled: Boolean,
    lifecycleStarted: Boolean,
    blurRadiusDp: Float,
    refractionEnabled: Boolean,
    refractionStrength: Float,
    edgeHighlightEnabled: Boolean,
    touchFeedbackEnabled: Boolean,
    surfaceOverlayAlpha: Float,
    shape: Shape,
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && touchFeedbackEnabled) 0.992f else 1f,
        animationSpec = tween(if (pressed) 90 else 180),
        label = "liveGlassPressScale",
    )

    val backdrop = if (backdropEnabled) rememberLayerBackdrop() else null
    val backdropBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFE6F1F8),
                Color(0xFFEDEAF8),
                Color(0xFFF8EEE5),
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(shape),
    ) {
        if (backdrop != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(backdrop),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backdropBrush),
                )
                if (lifecycleStarted) AnimatedBackdropShapes() else StaticBackdropShapes()
            }
        } else {
            // Off mode keeps a static light canvas and does not record a Backdrop layer.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backdropBrush),
            ) {
                StaticBackdropShapes()
            }
        }

        if (backdropEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(188.dp)
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                    }
                    .pointerInput(touchFeedbackEnabled) {
                        if (!touchFeedbackEnabled) return@pointerInput
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            pressed = true
                            try {
                                waitForUpOrCancellation()
                            } finally {
                                pressed = false
                            }
                        }
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            if (blurRadiusDp > 0f) blur(blurRadiusDp.dp.toPx())
                            if (refractionEnabled && refractionStrength > 0f) {
                                lens(
                                    refractionHeight = 22.dp.toPx(),
                                    refractionAmount = refractionStrength.dp.toPx(),
                                )
                            }
                        },
                        highlight = {
                            if (edgeHighlightEnabled) Highlight.Default.copy(alpha = 0.46f)
                            else null
                        },
                        onDrawSurface = {
                            drawRect(Color.White.copy(alpha = surfaceOverlayAlpha))
                        },
                    ),
            ) {
                PreviewCardContent()
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(188.dp)
                    .clip(shape)
                    .background(Color.White.copy(alpha = surfaceOverlayAlpha))
                    .border(1.dp, Color.White.copy(alpha = 0.9f), shape)
                    .padding(ZhixingTheme.spacing.cardPadding),
            ) {
                PreviewCardContent()
            }
        }
    }
}

@Composable
private fun PreviewCardContent() {
    Column(
        modifier = Modifier.padding(ZhixingTheme.spacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.cardContentGap),
    ) {
        Text(
            text = "ZHIXING / LIVE GLASS",
            style = ZhixingTheme.type.brandCaption,
            color = ZhixingTheme.colors.secondary,
        )
        Text(
            text = "背景持续变化",
            style = ZhixingTheme.type.sectionTitle,
            color = ZhixingTheme.colors.onBackground,
        )
        Text(
            text = "玻璃直接读取实时背景层，动画不会再每隔一秒跳变。",
            style = ZhixingTheme.type.body,
            color = ZhixingTheme.colors.onSurface,
        )
    }
}

@Composable
private fun BoxScope.StaticBackdropShapes() {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = (-32).dp, y = 26.dp)
            .size(188.dp)
            .background(Color(0xFF86BFE3).copy(alpha = 0.92f), CircleShape),
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 34.dp, y = 22.dp)
            .size(214.dp)
            .background(Color(0xFFA99AE5).copy(alpha = 0.88f), CircleShape),
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 22.dp, y = (-48).dp)
            .size(128.dp)
            .background(Color(0xFFE9B995).copy(alpha = 0.94f), CircleShape),
    )
}

@Composable
private fun BoxScope.AnimatedBackdropShapes() {
    val transition = rememberInfiniteTransition(label = "liveGlassBackdrop")
    val drift by transition.animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "liveBackdropDrift",
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = (-32 + drift).dp, y = 26.dp)
            .size(188.dp)
            .background(Color(0xFF86BFE3).copy(alpha = 0.92f), CircleShape),
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = (34 - drift).dp, y = (22 - drift / 2).dp)
            .size(214.dp)
            .background(Color(0xFFA99AE5).copy(alpha = 0.88f), CircleShape),
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = (22 + drift / 2).dp, y = (-48 - drift / 2).dp)
            .size(128.dp)
            .background(Color(0xFFE9B995).copy(alpha = 0.94f), CircleShape),
    )
}
