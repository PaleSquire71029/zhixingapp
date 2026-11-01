package ci.us.zhixing.app.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * A single static light background source shared by foreground glass surfaces.
 *
 * The host owns exactly one LayerBackdrop. Its background is deliberately
 * static in this first step: soft light gradients provide something to refract
 * without introducing another animation clock or per-frame bitmap snapshots.
 *
 * Place glass surfaces in [content] and pass the supplied Backdrop to them.
 * This host does not change navigation, page content, or system insets.
 */
@Composable
internal fun BackdropHost(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable BoxScope.(Backdrop) -> Unit,
) {
    val backdrop = rememberLayerBackdrop()

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .layerBackdrop(backdrop),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val minDimension = size.minDimension
                        val baseGradient = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF2F8FC),
                                Color(0xFFF3F1FA),
                                Color(0xFFFFF7EF),
                            ),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height),
                        )
                        val upperGlow = Brush.radialGradient(
                            colors = listOf(
                                Color(0x38A9D6EE),
                                Color(0x00A9D6EE),
                            ),
                            center = Offset(size.width * 0.16f, size.height * 0.12f),
                            radius = minDimension * 0.78f,
                        )
                        val lowerGlow = Brush.radialGradient(
                            colors = listOf(
                                Color(0x25C5B9E9),
                                Color(0x00C5B9E9),
                            ),
                            center = Offset(size.width * 0.88f, size.height * 0.82f),
                            radius = minDimension * 0.72f,
                        )
                        val warmGlow = Brush.radialGradient(
                            colors = listOf(
                                Color(0x1EDDC6A8),
                                Color(0x00DDC6A8),
                            ),
                            center = Offset(size.width * 0.72f, size.height * 0.28f),
                            radius = minDimension * 0.58f,
                        )

                        onDrawBehind {
                            drawRect(baseGradient)
                            drawCircle(
                                brush = upperGlow,
                                radius = minDimension * 0.78f,
                                center = Offset(size.width * 0.16f, size.height * 0.12f),
                            )
                            drawCircle(
                                brush = lowerGlow,
                                radius = minDimension * 0.72f,
                                center = Offset(size.width * 0.88f, size.height * 0.82f),
                            )
                            drawCircle(
                                brush = warmGlow,
                                radius = minDimension * 0.58f,
                                center = Offset(size.width * 0.72f, size.height * 0.28f),
                            )
                        }
                    },
            )
        }

        content(backdrop)
    }
}
