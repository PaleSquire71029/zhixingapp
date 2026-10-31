package ci.us.zhixing.app.ui.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens

/**
 * Compile-time compatibility probe for the upstream Backdrop API.
 *
 * It is deliberately not wired into the production UI. The probe verifies
 * the Android dependency variant and the core background/surface API without
 * altering MainActivity or changing the app's visible navigation.
 */
@Composable
internal fun BackdropCompatibilityProbe() {
    val backdrop = rememberLayerBackdrop()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE8F2FF),
                                Color(0xFFF5EDFF),
                                Color(0xFFFFF4E8),
                            ),
                        ),
                    ),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(width = 220.dp, height = 64.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedCornerShape(32.dp) },
                    effects = {
                        blur(8.dp.toPx())
                        lens(
                            refractionHeight = 20.dp.toPx(),
                            refractionAmount = 18.dp.toPx(),
                        )
                    },
                    onDrawSurface = {
                        drawRect(Color.White.copy(alpha = 0.28f))
                    },
                ),
        )
    }
}
