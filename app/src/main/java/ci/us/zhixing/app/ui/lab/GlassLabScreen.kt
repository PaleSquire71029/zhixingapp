package ci.us.zhixing.app.ui.lab

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.drawscope.drawImage
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.roundToInt
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import kotlinx.coroutines.launch

/**
 * Glass Lab 第 3 阶段：受控 Composable 背景快照。
 *
 * GraphicsLayer 录制本实验画布的背景绘制，再导出为 ImageBitmap；
 * 玻璃样本只显示它覆盖区域的裁剪图像，并在其上应用模糊与浅色叠层。
 * 快照不会自动捕获窗口或其他 Composable，也不是连续实时采样。
 */
@Composable
internal fun GlassLabScreen(onBack: () -> Unit) {
    val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    var blurEnabled by rememberSaveable { mutableStateOf(true) }
    val backdropLayer = rememberGraphicsLayer()
    var backdropImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var captureStatus by remember { mutableStateOf("正在准备背景快照…") }
    val coroutineScope = rememberCoroutineScope()

    suspend fun captureBackdrop() {
        captureStatus = "正在采集背景…"
        val result = runCatching { backdropLayer.toImageBitmap() }
        backdropImage = result.getOrNull()
        captureStatus = if (result.isSuccess) {
            "已采集局部背景快照"
        } else {
            "采集失败，可重新尝试"
        }
    }

    LaunchedEffect(backdropLayer) {
        // Give Compose time to record the first background draw before snapshotting.
        withFrameNanos { }
        withFrameNanos { }
        captureBackdrop()
    }

    val density = LocalDensity.current
    val panelInsetPx = with(density) { 20.dp.roundToPx() }
    val panelHeightPx = with(density) { 188.dp.roundToPx() }
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
                text = "GLASS LAB / 03",
                modifier = Modifier.weight(1f),
                style = ZhixingTheme.type.brandCaption,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
            Text(
                text = "局部背景采样",
                style = ZhixingTheme.type.screenTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = "从实验画布实际绘制结果生成快照，再裁出玻璃面板下方的区域。",
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

        TextButton(
            onClick = { coroutineScope.launch { captureBackdrop() } },
        ) {
            Text(
                text = "重新采样背景",
                style = ZhixingTheme.type.featureTitle,
                color = ZhixingTheme.colors.primary,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(panelShape)
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

        CapturedGlassPanel(
            image = backdropImage,
            blurEnabled = blurEnabled && blurSupported,
            panelInsetPx = panelInsetPx,
            panelHeightPx = panelHeightPx,
            shape = panelShape,
        ) {
            Text(
                text = "LIGHT / CAPTURE 03",
                style = ZhixingTheme.type.brandCaption,
                color = ZhixingTheme.colors.secondary,
            )
            Text(
                text = "背景进入玻璃",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = "前景保留清晰，背景像素来自上方实际绘制的彩色形状。",
                style = ZhixingTheme.type.body,
                color = ZhixingTheme.colors.onSurface,
            )
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
                text = if (blurSupported) {
                    "当前 Android 版本支持 10 dp 图层模糊。快照只在页面初次显示及点击“重新采样背景”时更新。"
                } else {
                    "当前 Android 版本低于 12：仍会显示局部背景快照，但不启用系统模糊。"
                },
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Text(
                text = "范围限制：仅采集 Glass Lab 指定的背景 Composable，不读取其他页面、兄弟组件或系统窗口；它是手动刷新的局部快照，不是连续实时 backdrop。",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CapturedGlassPanel(
    image: ImageBitmap?,
    blurEnabled: Boolean,
    panelInsetPx: Int,
    panelHeightPx: Int,
    shape: androidx.compose.ui.graphics.Shape,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
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
                    val srcLeft = panelInsetPx.coerceIn(0, (image.width - 1).coerceAtLeast(0))
                    val srcTop = ((image.height - panelHeightPx) / 2)
                        .coerceIn(0, (image.height - 1).coerceAtLeast(0))
                    val srcWidth = (image.width - panelInsetPx * 2)
                        .coerceAtLeast(1)
                        .coerceAtMost(image.width - srcLeft)
                    val srcHeight = panelHeightPx
                        .coerceAtLeast(1)
                        .coerceAtMost(image.height - srcTop)
                    drawImage(
                        image = image,
                        srcOffset = IntOffset(srcLeft, srcTop),
                        srcSize = IntSize(srcWidth, srcHeight),
                        dstSize = IntSize(
                            width = size.width.roundToInt().coerceAtLeast(1),
                            height = size.height.roundToInt().coerceAtLeast(1),
                        ),
                    )
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
