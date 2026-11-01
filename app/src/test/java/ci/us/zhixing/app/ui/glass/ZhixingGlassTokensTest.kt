package ci.us.zhixing.app.ui.glass

import ci.us.zhixing.app.ui.theme.ZhixingGlassMotion
import ci.us.zhixing.app.ui.theme.ZhixingGlassRadii
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZhixingGlassTokensTest {
    @Test
    fun cornerRadiusScaleMatchesComponentHierarchy() {
        assertEquals(12f, ZhixingGlassRadii.chip.value, 0f)
        assertEquals(16f, ZhixingGlassRadii.compactControl.value, 0f)
        assertEquals(20f, ZhixingGlassRadii.control.value, 0f)
        assertEquals(24f, ZhixingGlassRadii.card.value, 0f)
        assertEquals(28f, ZhixingGlassRadii.hero.value, 0f)
        assertEquals(32f, ZhixingGlassRadii.floatingPanel.value, 0f)
    }

    @Test
    fun pressScalesStayWithinTheIntendedHierarchy() {
        assertTrue(ZhixingGlassMotion.keypadPressScale < ZhixingGlassMotion.buttonPressScale)
        assertTrue(ZhixingGlassMotion.buttonPressScale < ZhixingGlassMotion.cardPressScale)
        assertTrue(ZhixingGlassMotion.cardPressScale < 1f)
        assertTrue(ZhixingGlassMotion.keypadPressScale > 0.9f)
    }

    @Test
    fun springAndTransitionTokensArePositiveAndOrdered() {
        assertTrue(ZhixingGlassMotion.pressSpringDampingRatio > 0f)
        assertTrue(ZhixingGlassMotion.pressSpringDampingRatio < 1f)
        assertTrue(ZhixingGlassMotion.pressSpringStiffness > 0f)
        assertTrue(ZhixingGlassMotion.controlStateTransitionMillis < ZhixingGlassMotion.pageTransitionMillis)
        assertTrue(ZhixingGlassMotion.pageTransitionMillis < ZhixingGlassMotion.sheetTransitionMillis)
    }

    @Test
    fun spatialMotionRemainsSubtle() {
        assertTrue(ZhixingGlassMotion.cardLiftDp in 0f..2f)
        assertTrue(ZhixingGlassMotion.pageEnterOffsetDp <= 8f)
        assertTrue(ZhixingGlassMotion.itemEnterOffsetDp <= 8f)
        assertTrue(ZhixingGlassMotion.sheetEnterOffsetDp <= 12f)
    }
}
