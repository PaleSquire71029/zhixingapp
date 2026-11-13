package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.CoordinatePointSet
import ci.us.zhixing.app.domain.math.model.CoordinatePointSetValidationResult
import ci.us.zhixing.app.domain.math.model.FunctionFitErrorCode
import ci.us.zhixing.app.domain.math.model.LinearFunctionFitResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinearFunctionFitterTest {
    @Test
    fun twoPointsDetermineTheirUniqueInterpolatingLine() {
        val result = fit(
            CoordinatePoint(3.0, 7.0),
            CoordinatePoint(-1.0, -1.0),
        )

        assertFit(result, slope = 2.0, intercept = 1.0)
    }

    @Test
    fun moreThanTwoCollinearPointsReturnTheSameLine() {
        val result = fit(
            CoordinatePoint(-2.0, -3.0),
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(3.0, 7.0),
            CoordinatePoint(5.0, 11.0),
        )

        assertFit(result, slope = 2.0, intercept = 1.0)
    }

    @Test
    fun noisyPointsUseOrdinaryLeastSquares() {
        // For (1,1), (2,2), (3,2), OLS gives y = 0.5x + 2/3.
        val result = fit(
            CoordinatePoint(1.0, 1.0),
            CoordinatePoint(2.0, 2.0),
            CoordinatePoint(3.0, 2.0),
        )

        assertFit(result, slope = 0.5, intercept = 2.0 / 3.0)
    }

    @Test
    fun horizontalAndConstantZeroDataAreHandled() {
        val horizontal = fit(
            CoordinatePoint(-2.0, 5.0),
            CoordinatePoint(0.0, 5.0),
            CoordinatePoint(3.0, 5.0),
        )
        val zero = fit(
            CoordinatePoint(-1.0, 0.0),
            CoordinatePoint(2.0, 0.0),
        )

        assertFit(horizontal, slope = 0.0, intercept = 5.0)
        assertFit(zero, slope = 0.0, intercept = 0.0)
    }

    @Test
    fun negativeCoordinatesAndUnsortedInputAreSupported() {
        val result = fit(
            CoordinatePoint(4.0, -7.0),
            CoordinatePoint(-2.0, 5.0),
        )

        assertFit(result, slope = -2.0, intercept = 1.0)
    }

    @Test
    fun emptyPointSetReturnsTheSameTypedErrorAsEmptyListInput() {
        val result = LinearFunctionFitter.fit(CoordinatePointSet(emptyList()))

        assertEquals(
            CoordinatePointSetValidationResult.EmptyInput,
            (result as LinearFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun onePointReturnsTypedInsufficientInput() {
        val result = fit(CoordinatePoint(1.0, 2.0))

        assertEquals(
            CoordinatePointSetValidationResult.InsufficientPointCount(1, 2),
            (result as LinearFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun emptyInputReturnsTypedEmptyInput() {
        val result = LinearFunctionFitter.fit(emptyList())

        assertEquals(
            CoordinatePointSetValidationResult.EmptyInput,
            (result as LinearFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun duplicateXReturnsTypedInvalidInput() {
        val result = fit(
            CoordinatePoint(2.0, 1.0),
            CoordinatePoint(2.0, 9.0),
        )

        assertTrue(result is LinearFunctionFitResult.InvalidInput)
        assertTrue(
            (result as LinearFunctionFitResult.InvalidInput).reason is
                CoordinatePointSetValidationResult.DuplicateXCoordinate,
        )
    }

    @Test
    fun extremeSymmetricCoordinatesRemainFinite() {
        val result = fit(
            CoordinatePoint(-Double.MAX_VALUE, -Double.MAX_VALUE),
            CoordinatePoint(Double.MAX_VALUE, Double.MAX_VALUE),
        )

        assertFit(result, slope = 1.0, intercept = 0.0)
    }

    @Test
    fun adjacentRepresentableCoordinatesRemainDistinguishable() {
        val nextX = Math.nextUp(1.0)
        val result = fit(
            CoordinatePoint(1.0, 1.0),
            CoordinatePoint(nextX, nextX),
        )

        assertFit(result, slope = 1.0, intercept = 0.0, tolerance = 1e-12)
    }

    @Test
    fun normalizationCollisionReturnsPrecisionFailureInsteadOfGuessing() {
        val result = fit(
            CoordinatePoint(1e-300, 1.0),
            CoordinatePoint(2e-300, 2.0),
            CoordinatePoint(Double.MAX_VALUE, 3.0),
        )

        assertEquals(
            FunctionFitErrorCode.NUMERIC_PRECISION_LOSS,
            (result as LinearFunctionFitResult.Failure).error.code,
        )
    }

    @Test
    fun unrepresentableSlopeReturnsNonFiniteResult() {
        val result = fit(
            CoordinatePoint(0.0, -Double.MAX_VALUE),
            CoordinatePoint(1.0, Double.MAX_VALUE),
        )

        assertEquals(
            FunctionFitErrorCode.NON_FINITE_RESULT,
            (result as LinearFunctionFitResult.Failure).error.code,
        )
    }

    private fun fit(vararg points: CoordinatePoint) =
        LinearFunctionFitter.fit(points.toList())

    private fun assertFit(
        result: LinearFunctionFitResult,
        slope: Double,
        intercept: Double,
        tolerance: Double = 1e-10,
    ) {
        assertTrue("Expected success but got $result", result is LinearFunctionFitResult.Success)
        val function = (result as LinearFunctionFitResult.Success).function
        assertEquals(slope, function.slope, tolerance)
        assertEquals(intercept, function.intercept, tolerance)
    }
}
