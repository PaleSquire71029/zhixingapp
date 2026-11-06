/*
 * Copyright 2025 Kyant
 *
 * Ported from Kyant0/AndroidLiquidGlass, Catalog tag 1.0.6
 * (commit 896a94a3ade1cc1a940b92365f942a34971fecda).
 *
 * Modified for Zhixing: package namespace and helper references are adapted
 * to the isolated backdrop106 package. Upstream layout and gesture physics are
 * retained; material settings are routed through Zhixing's tested parameter map.
 * Source: https://github.com/Kyant0/AndroidLiquidGlass/tree/1.0.6/catalog
 * SPDX-License-Identifier: Apache-2.0
 */

package ci.us.zhixing.app.ui.glass.backdrop106.components

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import ci.us.zhixing.app.ui.glass.GlassMaterialState
import ci.us.zhixing.app.ui.glass.drawEnvironmentReflection
import ci.us.zhixing.app.ui.glass.resolveCatalogBottomBarMaterialParameters
import ci.us.zhixing.app.ui.glass.backdrop106.DampedDragAnimation
import ci.us.zhixing.app.ui.glass.backdrop106.InteractiveHighlight
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

internal const val LIQUID_BOTTOM_TABS_SELECTION_TAG = "liquid-bottom-tabs-selection"

@Composable
internal fun LiquidBottomTabs(
    selectedTabIndex: Int,
    onTabSelected: (index: Int) -> Unit,
    backdrop: Backdrop,
    tabsCount: Int,
    material: GlassMaterialState = GlassMaterialState(),
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    // Zhixing intentionally uses a light material regardless of system theme.
    val materialParameters = resolveCatalogBottomBarMaterialParameters(
        material = material,
        apiLevel = Build.VERSION.SDK_INT,
    )
    val accentColor = Color(0xFF0088FF)
    val glassTintAlpha = materialParameters.overlayAlpha
    val containerColor = Color(0xFFF4F9FD).copy(alpha = glassTintAlpha * 0.58f)
    val containerGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = glassTintAlpha * 0.92f),
            Color(0xFFEAF4FB).copy(alpha = glassTintAlpha * 0.66f),
            Color(0xFFFCF9FF).copy(alpha = glassTintAlpha * 0.78f),
        ),
    )
    val glassRim = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.98f),
            Color(0xFFD8EAF5).copy(alpha = 0.72f),
            Color.White.copy(alpha = 0.90f),
        ),
    )

    val tabsBackdrop = if (materialParameters.useRefraction) {
        rememberLayerBackdrop()
    } else {
        null
    }
    val selectionBackdrop = if (tabsBackdrop != null) {
        rememberCombinedBackdrop(backdrop, tabsBackdrop)
    } else {
        backdrop
    }

    BoxWithConstraints(
        modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val tabWidth = with(density) {
            (constraints.maxWidth.toFloat() - 8f.dp.toPx()) / tabsCount
        }

        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by remember(density) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / constraints.maxWidth).fastCoerceIn(-1f, 1f)
                with(density) {
                    4f.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        // This state tracks the last index already handed to the animation
        // controller. It is independent of recomposition and callback identity.
        var currentIndex by remember {
            mutableIntStateOf(selectedTabIndex)
        }
        val dampedDragAnimation = remember(animationScope, materialParameters.touchFeedbackEnabled) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedTabIndex.toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = if (materialParameters.touchFeedbackEnabled) 78f / 56f else 1f,
                touchFeedbackEnabled = materialParameters.touchFeedbackEnabled,
                onDragStarted = {},
                onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                    currentIndex = targetIndex
                    onTabSelected(targetIndex)
                    animateToValue(targetIndex.toFloat())
                    animationScope.launch {
                        offsetAnimation.animateTo(
                            0f,
                            spring(1f, 300f, 0.5f)
                        )
                    }
                },
                onDrag = { _, dragAmount ->
                    updateValue(
                        (targetValue + dragAmount.x / tabWidth * if (isLtr) 1f else -1f)
                            .fastCoerceIn(0f, (tabsCount - 1).toFloat())
                    )
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }
            )
        }
        // A tap or external navigation update changes the caller-owned index.
        // Animate from the existing capsule value instead of recreating the
        // controller or routing the update through a second snapshotFlow loop.
        LaunchedEffect(selectedTabIndex, dampedDragAnimation) {
            if (currentIndex != selectedTabIndex) {
                currentIndex = selectedTabIndex
                dampedDragAnimation.animateToValue(selectedTabIndex.toFloat())
            }
        }

        val interactiveHighlight = if (materialParameters.touchFeedbackEnabled) {
            remember(animationScope) {
                InteractiveHighlight(
                    animationScope = animationScope,
                    position = { size, offset ->
                        Offset(
                            if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset
                            else size.width - (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset,
                            size.height / 2f
                        )
                    }
                )
            }
        } else {
            null
        }

        val pressProgress = if (materialParameters.touchFeedbackEnabled) {
            dampedDragAnimation.pressProgress
        } else {
            0f
        }
        // Keep a restrained optical response while idle; pressing brings the
        // capsule to the full Catalog lens and depth response.
        val selectionOpticalProgress = lerp(0.24f, 1f, pressProgress)
        val selectionDepthAlpha = 0.12f + 0.28f * pressProgress
        val highlightModifier = interactiveHighlight?.modifier ?: Modifier
        val highlightGestureModifier = interactiveHighlight?.gestureModifier ?: Modifier

        val backgroundSurfaceModifier = if (materialParameters.useBackdropSampling) {
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    if (materialParameters.useVibrancy) vibrancy()
                    if (materialParameters.useBlur) {
                        blur(materialParameters.blurRadiusDp.dp.toPx())
                    }
                    if (materialParameters.useRefraction) {
                        val lensAmount = 24f.dp.toPx() * materialParameters.refractionScale
                        lens(lensAmount, lensAmount)
                    }
                },
                highlight = {
                    if (materialParameters.edgeHighlightEnabled) {
                        Highlight.Default.copy(alpha = 0.30f + 0.12f * pressProgress)
                    } else {
                        null
                    }
                },
                shadow = {
                    Shadow(alpha = 0.13f + 0.06f * pressProgress)
                },
                innerShadow = {
                    InnerShadow(radius = 3f.dp, alpha = 0.10f + 0.04f * pressProgress)
                },
                highlight = {
                    if (materialParameters.edgeHighlightEnabled) {
                        Highlight.Default.copy(alpha = 0.62f + pressProgress * 0.18f)
                    } else {
                        null
                    }
                },
                shadow = {
                    Shadow(alpha = 0.10f + pressProgress * 0.04f)
                },
                innerShadow = {
                    InnerShadow(
                        radius = 5f.dp,
                        alpha = 0.10f + pressProgress * 0.05f,
                    )
                },
                layerBlock = {
                    val scale = lerp(1f, 1f + 16f.dp.toPx() / size.width, pressProgress)
                    scaleX = scale
                    scaleY = scale
                },
                onDrawSurface = {
                    drawRect(containerColor)
                    drawRect(containerGradient)
                    if (materialParameters.environmentReflectionEnabled) {
                        drawEnvironmentReflection()
                    }
                },
            )
        } else {
            Modifier
                .clip(Capsule())
                .background(containerGradient)
                .border(1.dp, glassRim, Capsule())
        }

        Row(
            Modifier
                .graphicsLayer { translationX = panelOffset }
                .then(backgroundSurfaceModifier)
                .then(highlightModifier)
                .height(64f.dp)
                .fillMaxWidth()
                .padding(4f.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )

        if (materialParameters.useRefraction) {
            val activeTabsBackdrop = checkNotNull(tabsBackdrop)
            CompositionLocalProvider(
                LocalLiquidBottomTabScale provides {
                    lerp(1f, 1.2f, pressProgress)
                },
            ) {
                Row(
                    Modifier
                        .clearAndSetSemantics {}
                        .alpha(0f)
                        .layerBackdrop(activeTabsBackdrop)
                        .graphicsLayer { translationX = panelOffset }
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { Capsule() },
                            effects = {
                                if (materialParameters.useVibrancy) vibrancy()
                                if (materialParameters.useBlur) {
                                    blur(materialParameters.blurRadiusDp.dp.toPx())
                                }
                                val lensAmount =
                                    24f.dp.toPx() * materialParameters.refractionScale * pressProgress
                                lens(lensAmount, lensAmount)
                            },
                            highlight = {
                                if (materialParameters.edgeHighlightEnabled) {
                                    Highlight.Default.copy(alpha = pressProgress)
                                } else {
                                    null
                                }
                            },
                            onDrawSurface = {
                                drawRect(containerColor)
                                if (materialParameters.environmentReflectionEnabled) {
                                    drawEnvironmentReflection()
                                }
                            },
                        )
                        .then(highlightModifier)
                        .height(56f.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 4f.dp)
                        .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content,
                )
            }
        }

        val selectionSurfaceModifier = if (materialParameters.useBackdropSampling) {
            Modifier.drawBackdrop(
                backdrop = selectionBackdrop,
                shape = { Capsule() },
                effects = {
                    if (materialParameters.useRefraction) {
                        lens(
                            10f.dp.toPx() * selectionOpticalProgress * materialParameters.refractionScale,
                            14f.dp.toPx() * selectionOpticalProgress * materialParameters.refractionScale,
                            chromaticAberration = materialParameters.chromaticDispersionEnabled,
                        )
                    }
                },
                highlight = {
                    if (materialParameters.edgeHighlightEnabled) {
                        Highlight.Default.copy(alpha = 0.18f + 0.82f * pressProgress)
                    } else {
                        null
                    }
                },
                shadow = {
                    Shadow(alpha = selectionDepthAlpha)
                },
                innerShadow = {
                    InnerShadow(
                        radius = (2f + 6f * pressProgress).dp,
                        alpha = selectionDepthAlpha,
                    )
                },
                layerBlock = {
                    if (materialParameters.touchFeedbackEnabled) {
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                        val velocity = dampedDragAnimation.velocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    } else {
                        scaleX = 1f
                        scaleY = 1f
                    }
                },
                onDrawSurface = {
                    drawRect(
                        Color(0xFFDCECF8).copy(
                            alpha = 0.34f * materialParameters.overlayAlpha,
                        ),
                        alpha = 1f - pressProgress,
                    )
                    drawRect(
                        Color.White.copy(alpha = 0.12f * pressProgress),
                    )
                    if (materialParameters.environmentReflectionEnabled) {
                        drawEnvironmentReflection()
                    }
                },
            )
        } else {
            Modifier
                .clip(Capsule())
                .background(Color.White.copy(alpha = materialParameters.overlayAlpha * 0.72f))
                .border(0.75.dp, Color.White.copy(alpha = 0.92f), Capsule())
        }

        Box(
            Modifier
                .padding(horizontal = 4f.dp)
                .graphicsLayer {
                    translationX =
                        if (isLtr) dampedDragAnimation.value * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 1f) * tabWidth + panelOffset
                }
                .then(highlightGestureModifier)
                .then(dampedDragAnimation.modifier)
                .then(selectionSurfaceModifier)
                .height(56f.dp)
                .fillMaxWidth(1f / tabsCount)
                .testTag(LIQUID_BOTTOM_TABS_SELECTION_TAG),
        )
    }
}
