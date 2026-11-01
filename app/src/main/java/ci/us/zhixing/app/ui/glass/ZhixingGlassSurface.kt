package ci.us.zhixing.app.ui.glass

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.highlight.Highlight

/**
 * A single reusable light glass surface backed by a shared live Backdrop.
 *
 * The parent owns and provides the Backdrop so multiple surfaces can sample
 * the same background instead of creating independent recording layers.
 * A null Backdrop uses a lightweight light-surface fallback for disabled or
 * unavailable glass rendering.
 *
 * Transparency and blur are independent: transparency controls only the
 * white surface overlay, while blur controls only the backdrop blur effect.
 */
@Composable
internal fun ZhixingGlassSurface(
    backdrop: Backdrop?,
    material: GlassMaterialState,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val overlayAlpha = (100 - material.transparencyPercent) / 100f

    if (backdrop != null) {
        Box(
            modifier = modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        material.blurRadiusDp > 0f
                    ) {
                        blur(material.blurRadiusDp.dp.toPx())
                    }
                },
                highlight = {
                    Highlight.Default.copy(alpha = 0.42f)
                },
                onDrawSurface = {
                    drawRect(Color.White.copy(alpha = overlayAlpha))
                },
            ),
            content = content,
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(Color.White.copy(alpha = overlayAlpha))
                .border(1.dp, Color.White.copy(alpha = 0.88f), shape),
            content = content,
        )
    }
}
