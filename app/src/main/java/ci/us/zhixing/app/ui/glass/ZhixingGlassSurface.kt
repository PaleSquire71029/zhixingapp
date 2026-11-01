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
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight

/**
 * A single reusable light glass surface backed by a shared live Backdrop.
 *
 * The parent owns and provides the Backdrop so multiple surfaces can sample
 * the same background instead of creating independent recording layers.
 * A null Backdrop or glassEnabled = false uses a lightweight light-surface
 * fallback. The disabled path does not attach a drawBackdrop sampling node.
 * If the whole host is Off, its owner should also avoid creating the shared
 * Backdrop layer when there are no other consumers.
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
    glassEnabled: Boolean = true,
    refractionPressProgress: Float = 0f,
    content: @Composable BoxScope.() -> Unit,
) {
    val overlayAlpha = (100 - material.transparencyPercent) / 100f
    val capabilities = resolveGlassSurfaceCapabilities(
        apiLevel = Build.VERSION.SDK_INT,
        enabled = glassEnabled && material.enabled,
        blurRadiusDp = material.blurRadiusDp,
        refractionEnabled = material.refractionEnabled,
        refractionStrengthDp = material.refractionStrengthDp,
    )

    if (backdrop != null && capabilities.useBackdropSampling) {
        Box(
            modifier = modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    if (capabilities.useBlur) {
                        blur(material.blurRadiusDp.dp.toPx())
                    }
                    if (capabilities.useRefraction) {
                        val lensParameters = resolveTouchRefractionParameters(
                            baseHeightDp = 22f,
                            baseAmountDp = material.refractionStrengthDp,
                            pressProgress = if (material.touchFeedbackEnabled) {
                                refractionPressProgress.coerceIn(0f, 1f)
                            } else {
                                0f
                            },
                        )
                        lens(
                            refractionHeight = lensParameters.heightDp.dp.toPx(),
                            refractionAmount = lensParameters.amountDp.dp.toPx(),
                            chromaticAberration = material.chromaticDispersionEnabled,
                        )
                    }
                },
                highlight = {
                    if (material.edgeHighlightEnabled) {
                        Highlight.Default.copy(alpha = 0.42f)
                    } else {
                        null
                    }
                },
                onDrawSurface = {
                    drawRect(Color.White.copy(alpha = overlayAlpha))
                    if (material.environmentReflectionEnabled) {
                        drawEnvironmentReflection()
                    }
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
