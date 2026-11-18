package ci.us.zhixing.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignTokenContractTest {
    @Test
    fun lightPalettePreservesExistingColorValues() {
        assertEquals(Color(0xFF4D7187), ZhixingColorTokens.primary)
        assertEquals(Color.White, ZhixingColorTokens.onPrimary)
        assertEquals(Color(0xFFDCEAF1), ZhixingColorTokens.primaryContainer)
        assertEquals(Color(0xFF1E3948), ZhixingColorTokens.onPrimaryContainer)
        assertEquals(Color(0xFF92775B), ZhixingColorTokens.secondary)
        assertEquals(Color.White, ZhixingColorTokens.onSecondary)
        assertEquals(Color(0xFFF5F8FB), ZhixingColorTokens.background)
        assertEquals(Color(0xFF202D37), ZhixingColorTokens.onBackground)
        assertEquals(Color.White, ZhixingColorTokens.surface)
        assertEquals(Color(0xFF202D37), ZhixingColorTokens.onSurface)
        assertEquals(Color(0xFFEDF2F6), ZhixingColorTokens.surfaceVariant)
        assertEquals(Color(0xFF687985), ZhixingColorTokens.onSurfaceVariant)
        assertEquals(Color(0xFFD6E0E7), ZhixingColorTokens.outline)
        assertEquals(ZhixingLightColors.error, ZhixingColorTokens.error)
        assertEquals(ZhixingLightColors.onError, ZhixingColorTokens.onError)
        assertEquals(ZhixingLightColors.tertiary, ZhixingColorTokens.tertiary)
        assertEquals(ZhixingColorTokens.primary.copy(alpha = 0.12f), ZhixingColorTokens.selectedContainer)
        assertEquals(ZhixingLightColors.primary, ZhixingTheme.colors.primary)
    }

    @Test
    fun typographyPreservesSizesWeightsAndDefaultFontFallback() {
        assertEquals(32f, ZhixingTypographyTokens.brandTitle.fontSize.value, 0f)
        assertEquals(40f, ZhixingTypographyTokens.brandTitle.lineHeight.value, 0f)
        assertEquals(FontWeight.Bold, ZhixingTypographyTokens.brandTitle.fontWeight)
        assertEquals(11f, ZhixingTypographyTokens.brandCaption.fontSize.value, 0f)
        assertEquals(1.5f, ZhixingTypographyTokens.brandCaption.letterSpacing.value, 0f)
        assertEquals(24f, ZhixingTypographyTokens.screenTitle.fontSize.value, 0f)
        assertEquals(16f, ZhixingTypographyTokens.body.fontSize.value, 0f)
        assertEquals(24f, ZhixingTypographyTokens.body.lineHeight.value, 0f)
        assertNull(ZhixingTypographyTokens.body.fontFamily)
        assertEquals(ZhixingTypographyTokens.screenTitle, ZhixingType.screenTitle)
        assertEquals(ZhixingTypographyTokens.body, ZhixingTheme.type.body)
    }

    @Test
    fun spacingPreservesEveryExistingDimensionAndLegacyAccessor() {
        assertEquals(22f, ZhixingSpacingTokens.pageHorizontal.value, 0f)
        assertEquals(28f, ZhixingSpacingTokens.pageVertical.value, 0f)
        assertEquals(20f, ZhixingSpacingTokens.section.value, 0f)
        assertEquals(12f, ZhixingSpacingTokens.headerGap.value, 0f)
        assertEquals(3f, ZhixingSpacingTokens.tight.value, 0f)
        assertEquals(13f, ZhixingSpacingTokens.badgeHorizontal.value, 0f)
        assertEquals(8f, ZhixingSpacingTokens.badgeVertical.value, 0f)
        assertEquals(24f, ZhixingSpacingTokens.cardPadding.value, 0f)
        assertEquals(14f, ZhixingSpacingTokens.cardContentGap.value, 0f)
        assertEquals(2f, ZhixingSpacingTokens.hairlineGap.value, 0f)
        assertEquals(10f, ZhixingSpacingTokens.groupGap.value, 0f)
        assertEquals(16f, ZhixingSpacingTokens.featureHorizontal.value, 0f)
        assertEquals(13f, ZhixingSpacingTokens.featureVertical.value, 0f)
        assertEquals(ZhixingSpacingTokens.pageHorizontal, ZhixingSpacing.pageHorizontal)
        assertEquals(ZhixingSpacingTokens.cardPadding, ZhixingTheme.spacing.cardPadding)
    }

    @Test
    fun shapeScaleMatchesExistingGlassRadii() {
        assertEquals(12f, ZhixingShapeTokens.chip.value, 0f)
        assertEquals(16f, ZhixingShapeTokens.compactControl.value, 0f)
        assertEquals(18f, ZhixingShapeTokens.feature.value, 0f)
        assertEquals(20f, ZhixingShapeTokens.control.value, 0f)
        assertEquals(24f, ZhixingShapeTokens.card.value, 0f)
        assertEquals(28f, ZhixingShapeTokens.hero.value, 0f)
        assertEquals(32f, ZhixingShapeTokens.floatingPanel.value, 0f)
        assertEquals(RoundedCornerShape(28.dp), ZhixingShapes.hero)
        assertEquals(RoundedCornerShape(18.dp), ZhixingShapes.feature)
        assertEquals(ZhixingShapes.card, ZhixingGlassShapes.card)
        assertEquals(CircleShape, ZhixingGlassShapes.pill)
        assertEquals(ZhixingShapeTokens.hero, ZhixingGlassRadii.hero)
    }

    @Test
    fun elevationScaleCapturesExistingShadowRadii() {
        assertEquals(0f, ZhixingElevationTokens.flat.value, 0f)
        assertEquals(4f, ZhixingElevationTokens.glassControlShadow.value, 0f)
        assertEquals(6f, ZhixingElevationTokens.contentShadow.value, 0f)
        assertEquals(10f, ZhixingElevationTokens.heroShadow.value, 0f)
    }

    @Test
    fun materialRolesKeepReadingContentSeparateFromGlass() {
        assertEquals(ZhixingMaterialRole.CANVAS, ZhixingMaterialTokens.canvasRole)
        assertEquals(ZhixingMaterialRole.SOFT_SURFACE, ZhixingMaterialTokens.contentRole)
        assertEquals(ZhixingMaterialRole.FLOW_GLASS, ZhixingMaterialTokens.floatingRole)
        assertEquals(ZhixingMaterialRole.OVERLAY, ZhixingMaterialTokens.overlayRole)
        assertEquals(ZhixingColorTokens.background, ZhixingMaterialTokens.canvasColor)
        assertEquals(ZhixingColorTokens.surface, ZhixingMaterialTokens.softSurfaceColor)
        assertFalse(ZhixingMaterialTokens.regularContentUsesFlowGlass)
        assertFalse(ZhixingMaterialTokens.mathContentAllowsRefraction)
        assertTrue(ZhixingMaterialTokens.sharedBackdropHostPerPage)
        assertTrue(ZhixingMaterialTokens.transparencyAndBlurAreIndependent)
    }

    @Test
    fun motionTokensRetainExistingGlassResponse() {
        assertEquals(0.965f, ZhixingMotionTokens.buttonPressScale, 0f)
        assertEquals(0.95f, ZhixingMotionTokens.keypadPressScale, 0f)
        assertEquals(0.985f, ZhixingMotionTokens.cardPressScale, 0f)
        assertEquals(0.78f, ZhixingMotionTokens.pressSpringDampingRatio, 0f)
        assertEquals(520f, ZhixingMotionTokens.pressSpringStiffness, 0f)
        assertEquals(120, ZhixingMotionTokens.microTransitionMillis)
        assertEquals(180, ZhixingMotionTokens.controlStateTransitionMillis)
        assertEquals(220, ZhixingMotionTokens.contentTransitionMillis)
        assertEquals(220, ZhixingMotionTokens.navigationTransitionMillis)
        assertEquals(220, ZhixingMotionTokens.pageTransitionMillis)
        assertEquals(280, ZhixingMotionTokens.sheetTransitionMillis)
        assertEquals(8f, ZhixingMotionTokens.pageEnterOffsetDp, 0f)
        assertEquals(ZhixingMotionTokens.buttonPressScale, ZhixingGlassMotion.buttonPressScale, 0f)
    }

    @Test
    fun interactionTokensKeepTouchTargetsAndSelectedTintStable() {
        assertEquals(48f, ZhixingInteractionTokens.minimumTouchTarget.value, 0f)
        assertEquals(2f, ZhixingInteractionTokens.focusOutlineWidth.value, 0f)
        assertEquals(0.12f, ZhixingInteractionTokens.pressedStateLayerAlpha, 0f)
        assertEquals(0.12f, ZhixingInteractionTokens.selectedContainerAlpha, 0f)
        assertEquals(0.38f, ZhixingInteractionTokens.disabledContentAlpha, 0f)
        assertEquals(7, ZhixingInteractionState.entries.size)
        assertTrue(ZhixingInteractionTokens.touchFeedbackCanBeDisabled)
    }

    @Test
    fun layoutTokensRecordFixedDockAndViewportBoundary() {
        assertEquals(22f, ZhixingLayoutTokens.pageHorizontalPadding.value, 0f)
        assertEquals(28f, ZhixingLayoutTokens.pageVerticalPadding.value, 0f)
        assertEquals(68f, ZhixingLayoutTokens.dockHeight.value, 0f)
        assertTrue(ZhixingLayoutTokens.dockOutsideScrollViewport)
        assertTrue(ZhixingLayoutTokens.systemBarsOutsideScrollViewport)
        assertTrue(ZhixingLayoutTokens.imeOutsideViewportTransform)
        assertTrue(ZhixingLayoutTokens.pageOverscrollUsesSingleViewportLayer)
    }

    @Test
    fun themeExposesAllTokenGroupsAndKeepsLegacyEntryPoints() {
        assertEquals(ZhixingLightColors, ZhixingTheme.colors)
        assertEquals(ZhixingType.body, ZhixingTheme.type.body)
        assertEquals(ZhixingSpacing.pageHorizontal, ZhixingTheme.spacing.pageHorizontal)
        assertEquals(ZhixingShapes.hero, ZhixingTheme.shapes.hero)
        assertEquals(ZhixingColorTokens.primary, ZhixingTheme.palette.primary)
        assertEquals(ZhixingTypographyTokens.body, ZhixingTheme.typography.body)
        assertEquals(ZhixingSpacingTokens.section, ZhixingTheme.spacingTokens.section)
        assertEquals(ZhixingElevationTokens.heroShadow, ZhixingTheme.elevation.heroShadow)
        assertEquals(ZhixingMaterialRole.FLOW_GLASS, ZhixingTheme.material.floatingRole)
        assertEquals(ZhixingMotionTokens.pageTransitionMillis, ZhixingTheme.motion.pageTransitionMillis)
        assertEquals(ZhixingInteractionTokens.minimumTouchTarget, ZhixingTheme.interaction.minimumTouchTarget)
        assertEquals(ZhixingLayoutTokens.dockHeight, ZhixingTheme.layout.dockHeight)
    }
}
