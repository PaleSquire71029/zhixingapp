package ci.us.zhixing.app.ui.lab

import android.os.Build
import java.util.Locale
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToInt
import ci.us.zhixing.app.ui.theme.ZhixingTheme

private const val BACKDROP_SAMPLE_INTERVAL_MS = 1_000L

/**
 * Glass Lab 第 4 阶段：前台限频动态快照实验。
 *
 * GraphicsLayer 只录制指定背景子层。动态模式默认关闭，启用后每秒最多导出一次
 * 局部快照，并在 Activity 进入 STOPPED 状态或页面离开组合时停止。
 */
@Composable
internal fun GlassLabScreen(
    onBack: () -> Unit,
    lifecycle: Lifecycle,
) {
    val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val refractionSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    var blurEnabled by rememberSaveable { mutableStateOf(blurSupported) }
    var refractionEnabled by rememberSaveable { mutableStateOf(refractionSupported) }
    var refractionStrength by rememberSaveable { mutableFloatStateOf(3.5f) }
    var dynamicSamplingEnabled by rememberSaveable { mutableStateOf(false) }
    var isLifecycleStarted by remember(lifecycle) {
        mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    val backdropLayer = rememberGraphicsLayer()
    var backdropImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var captureStatus by remember { mutableStateOf("正在准备背景快照…") }
    var refractionStatus by remember { mutableStateOf("AGSL 初始化中…") }
    var refractionReady by remember { mutableStateOf(false) }
    val refraction = remember(refractionSupported) {
        if (refractionSupported) runCatching { SingleChannelRefraction.create() }.getOrNull() else null
    }
    var captureCount by remember { mutableIntStateOf(0) }
    var manualRefreshPending by remember { mutableStateOf(false) }
    val captureMutex = remember { Mutex() }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> isLifecycleStarted = true
                Lifecycle.Event.ON_STOP -> isLifecycleStarted = false
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    suspend fun captureBackdrop() {
        captureMutex.withLock {
            if (!isLifecycleStarted) return@withLock
            captureStatus = "正在采集背景…"
            val result = try {
                Result.success(backdropLayer.toImageBitmap())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                Result.failure<ImageBitmap>(IllegalStateException("Backdrop snapshot failed"))
            }
            val image = result.getOrNull()
            if (image != null) {
                backdropImage = image
                captureCount += 1
                captureStatus = "已更新局部背景快照"
            } else {
                captureStatus = "采集失败，可重新尝试"
            }
        }
    }

    LaunchedEffect(backdropLayer, isLifecycleStarted) {
        if (isLifecycleStarted) {
            // Allow the background draw node to record its first frame.
            withFrameNanos { }
            withFrameNanos { }
            if (backdropImage == null) captureBackdrop()
        }
    }

    LaunchedEffect(dynamicSamplingEnabled, isLifecycleStarted, backdropLayer) {
        if (dynamicSamplingEnabled && isLifecycleStarted) {
            captureStatus = "动态采样已开启 · 每秒最多更新一次"
            while (true) {
                delay(BACKDROP_SAMPLE_INTERVAL_MS)
                if (isLifecycleStarted) captureBackdrop()
            }
        } else if (dynamicSamplingEnabled) {
            captureStatus = "应用已暂停 · 动态采样已停止"
        }
    }

    val density = LocalDensity.current
    val panelInsetPx = with(density) { 20.dp.roundToPx() }
    val panelHeightPx = with(density) { 188.dp.roundToPx() }
    val backdropCrop = backdropImage?.let { image ->
        calculateBackdropCrop(
            imageWidth = image.width,
            imageHeight = image.height,
            panelInset = panelInsetPx,
            panelHeight = panelHeightPx,
        )
    }

    LaunchedEffect(backdropImage, backdropCrop, refraction) {
        refractionReady = false
        if (refraction != null && backdropImage != null && backdropCrop != null) {
            val result = runCatching {
                refraction.bindBackdrop(backdropImage!!, backdropCrop!!)
            }
            refractionReady = result.isSuccess
            refractionStatus = if (result.isSuccess) {
                "AGSL 单通道折射已启用"
            } else {
                "AGSL 初始化失败，已回退为快照显示"
            }
        } else {
            refractionStatus = when {
                !refractionSupported -> "系统低于 Android 13，使用快照与模糊降级"
                refraction == null -> "AGSL 初始化失败，使用快照与模糊降级"
                else -> "等待背景快照"
            }
        }
    }

    LaunchedEffect(manualRefreshPending, isLifecycleStarted) {
        if (manualRefreshPending && isLifecycleStarted) {
            captureBackdrop()
            manualRefreshPending = false
        }
    }

    val panelShape = ZhixingTheme.shapes.hero

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
                text = "GLASS LAB / 04",
                modifier = Modifier.weight(1f),
                style = ZhixingTheme.type.brandCaption,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
            Text(
                text = "前台动态背景采样",
                style = ZhixingTheme.type.screenTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = "背景图形轻微移动，快照按固定间隔更新；动态采样默认关闭。",
                style = ZhixingTheme.type.body,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (blurEnabled && blurSupported) "模糊 · 10 dp" else "清晰快照",
                style = ZhixingTheme.type.sectionLabel,
                color = ZhixingTheme.colors.secondary,
            )
            TextButton(
                onClick = { blurEnabled = !blurEnabled },
                enabled = blurSupported,
            ) {
                Text(
                    text = if (blurEnabled) "关闭模糊" else "开启模糊",
                    style = ZhixingTheme.type.featureTitle,
                    color = ZhixingTheme.colors.primary,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
                Text(
                    text = if (dynamicSamplingEnabled) "动态采样 · 开启" else "动态采样 · 关闭",
                    style = ZhixingTheme.type.sectionLabel,
                    color = ZhixingTheme.colors.secondary,
                )
                Text(
                    text = "每秒最多刷新 1 次",
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = { dynamicSamplingEnabled = !dynamicSamplingEnabled },
                enabled = isLifecycleStarted,
            ) {
                Text(
                    text = if (dynamicSamplingEnabled) "停止动态" else "开启动态",
                    style = ZhixingTheme.type.featureTitle,
                    color = ZhixingTheme.colors.primary,
                )
            }
        }

        TextButton(
            onClick = { manualRefreshPending = true },
            enabled = isLifecycleStarted && !manualRefreshPending,
        ) {
            Text(
                text = "重新采样背景",
                style = ZhixingTheme.type.featureTitle,
                color = ZhixingTheme.colors.primary,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
                Text(
                    text = if (refractionEnabled && refractionReady) "单通道折射 · 开启" else "单通道折射 · 关闭",
                    style = ZhixingTheme.type.sectionLabel,
                    color = ZhixingTheme.colors.secondary,
                )
                Text(
                    text = "RGB 共用采样坐标 · 不做色散",
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = { refractionEnabled = !refractionEnabled },
                enabled = refractionReady,
            ) {
                Text(
                    text = if (refractionEnabled) "关闭折射" else "开启折射",
                    style = ZhixingTheme.type.featureTitle,
                    color = ZhixingTheme.colors.primary,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
            Text(
                text = "折射强度 · ${String.format(Locale.US, "%.1f", clampRefractionStrength(refractionStrength))} px",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Slider(
                value = refractionStrength,
                onValueChange = { refractionStrength = clampRefractionStrength(it) },
                valueRange = 0f..8f,
                steps = 15,
                enabled = refractionReady && refractionEnabled,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(panelShape),
        ) {
            // The glass panel remains a sibling and is never included in this recording layer.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        backdropLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(backdropLayer)
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE6F1F8),
                                    Color(0xFFEDEAF8),
                                    Color(0xFFF8EEE5),
                                ),
                            ),
                        ),
                )
                if (dynamicSamplingEnabled && isLifecycleStarted) {
                    AnimatedBackdropShapes()
                } else {
                    StaticBackdropShapes()
                }
            }

            CapturedGlassPanel(
                modifier = Modifier.align(Alignment.Center),
                image = backdropImage,
                blurEnabled = blurEnabled && blurSupported,
                refraction = refraction,
                refractionReady = refractionReady,
                refractionEnabled = refractionEnabled,
                refractionStrength = refractionStrength,
                panelInsetPx = panelInsetPx,
                panelHeightPx = panelHeightPx,
                shape = panelShape,
            ) {
                Text(
                    text = "LIGHT / DYNAMIC 04",
                    style = ZhixingTheme.type.brandCaption,
                    color = ZhixingTheme.colors.secondary,
                )
                Text(
                    text = "背景持续变化",
                    style = ZhixingTheme.type.sectionTitle,
                    color = ZhixingTheme.colors.onBackground,
                )
                Text(
                    text = "快照更新受频率限制，前景文字保持清晰。",
                    style = ZhixingTheme.type.body,
                    color = ZhixingTheme.colors.onSurface,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.groupGap)) {
            Text(
                text = "采样状态",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = captureStatus,
                style = ZhixingTheme.type.body,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Text(
                text = "成功快照：$captureCount 次",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Text(
                text = refractionStatus,
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Text(
                text = if (isLifecycleStarted) {
                    "当前页面处于前台。离开实验页或 App 进入后台后，自动采样协程会停止。"
                } else {
                    "当前 App 不在前台，采样已暂停。"
                },
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Text(
                text = "范围限制：仅采集 Glass Lab 指定的背景 Composable，不读取其他页面、兄弟组件或系统窗口；每秒一次的快照仍是离散采样，不是原生连续实时 backdrop。",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }
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
    val transition = rememberInfiniteTransition(label = "glassLabBackdrop")
    val drift by transition.animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "backdropDrift",
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

@Composable
private fun CapturedGlassPanel(
    modifier: Modifier = Modifier,
    image: ImageBitmap?,
    blurEnabled: Boolean,
    refraction: SingleChannelRefraction?,
    refractionReady: Boolean,
    refractionEnabled: Boolean,
    refractionStrength: Float,
    panelInsetPx: Int,
    panelHeightPx: Int,
    shape: Shape,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(188.dp),
        color = Color.Transparent,
        shape = shape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.96f)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (image != null) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (blurEnabled) {
                                Modifier.blur(
                                    radius = 10.dp,
                                    edgeTreatment = BlurredEdgeTreatment(shape),
                                )
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    val crop = calculateBackdropCrop(
                        imageWidth = image.width,
                        imageHeight = image.height,
                        panelInset = panelInsetPx,
                        panelHeight = panelHeightPx,
                    )
                    if (crop != null && refractionEnabled && refractionReady && refraction != null) {
                        refraction.updateUniforms(
                            widthPx = size.width,
                            heightPx = size.height,
                            strengthPx = refractionStrength,
                        )
                        drawRect(brush = refraction.brush)
                    } else if (crop != null) {
                        drawImage(
                            image = image,
                            srcOffset = androidx.compose.ui.unit.IntOffset(crop.left, crop.top),
                            srcSize = androidx.compose.ui.unit.IntSize(crop.width, crop.height),
                            dstSize = androidx.compose.ui.unit.IntSize(
                                width = size.width.roundToInt().coerceAtLeast(1),
                                height = size.height.roundToInt().coerceAtLeast(1),
                            ),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.54f)),
            )

            Column(
                modifier = Modifier.padding(ZhixingTheme.spacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.cardContentGap),
                content = content,
            )
        }
    }
}
