package ci.us.zhixing.app.ui.glass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TouchRefractionParametersTest {
    @Test
    fun idleStateKeepsBaseLensParameters() {
        val parameters = resolveTouchRefractionParameters(
            baseHeightDp = 22f,
            baseAmountDp = 1.8f,
            pressProgress = 0f,
        )

        assertEquals(22f, parameters.heightDp, 0f)
        assertEquals(1.8f, parameters.amountDp, 0f)
    }

    @Test
    fun fullPressAddsSmallLensHeightAndAmount() {
        val parameters = resolveTouchRefractionParameters(
            baseHeightDp = 22f,
            baseAmountDp = 1.8f,
            pressProgress = 1f,
        )

        assertEquals(26f, parameters.heightDp, 0f)
        assertEquals(2.088f, parameters.amountDp, 0.0001f)
    }

    @Test
    fun partialPressInterpolatesLensParameters() {
        val parameters = resolveTouchRefractionParameters(
            baseHeightDp = 22f,
            baseAmountDp = 2f,
            pressProgress = 0.5f,
        )

        assertEquals(24f, parameters.heightDp, 0f)
        assertEquals(2.16f, parameters.amountDp, 0.0001f)
    }

    @Test
    fun invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            resolveTouchRefractionParameters(0f, 1f, 0f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            resolveTouchRefractionParameters(22f, -1f, 0f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            resolveTouchRefractionParameters(22f, 1f, Float.NaN)
        }
        assertThrows(IllegalArgumentException::class.java) {
            resolveTouchRefractionParameters(22f, 1f, 1.01f)
        }
    }
}
