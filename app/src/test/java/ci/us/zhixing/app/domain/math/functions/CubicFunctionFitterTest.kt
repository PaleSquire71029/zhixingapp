package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinateAxis
import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.CoordinatePointSet
import ci.us.zhixing.app.domain.math.model.CoordinatePointSetValidationResult
import ci.us.zhixing.app.domain.math.model.CubicFunctionFitResult
import ci.us.zhixing.app.domain.math.model.FunctionFitErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CubicFunctionFitterTest {
    @Test
    fun fourPointsDetermineTheirUniqueInterpolatingCubic() {
        val result = fit(
            CoordinatePoint(-2.0, -16.0),
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(1.0, 0.5),
            CoordinatePoint(3.0, 11.5),
        )

        assertFit(result, a = 1.0, b = -2.0, c = 0.5, d = 1.0)
    }

    @Test
    fun moreThanFourExactCubicPointsRecoverTheOriginalFunction() {
        val result = fit(
            CoordinatePoint(-3.0, -45.5),
            CoordinatePoint(-1.0, -2.5),
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(2.0, 2.0),
            CoordinatePoint(4.0, 35.0),
        )

        assertFit(result, a = 1.0, b = -2.0, c = 0.5, d = 1.0)
    }

    @Test
    fun noisyPointsUseOrdinaryLeastSquares() {
        // The residual vector [1,-4,6,-4,1] is orthogonal to every
        // polynomial of degree <= 3 on x=-2,-1,0,1,2.
        val result = fit(
            CoordinatePoint(-2.0, -40.0),
            CoordinatePoint(-1.0, -18.0),
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(1.0, -6.0),
            CoordinatePoint(2.0, 8.0),
        )

        assertFit(result, a = 2.0, b = -3.0, c = 4.0, d = -5.0)
    }

    @Test
    fun linearDataAreRepresentedWithZeroHigherOrderTerms() {
        val result = fit(
            CoordinatePoint(-3.0, -5.0),
            CoordinatePoint(-1.0, -1.0),
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(2.0, 5.0),
            CoordinatePoint(4.0, 9.0),
        )

        assertFit(result, a = 0.0, b = 0.0, c = 2.0, d = 1.0)
    }

    @Test
    fun quadraticDataAreRepresentedWithZeroCubicTerm() {
        val result = fit(
            CoordinatePoint(-2.0, 11.0),
            CoordinatePoint(-1.0, 5.0),
            CoordinatePoint(0.0, 3.0),
            CoordinatePoint(1.0, 5.0),
            CoordinatePoint(2.0, 11.0),
        )

        assertFit(result, a = 0.0, b = 2.0, c = 0.0, d = 3.0)
    }

    @Test
    fun allZeroYValuesProduceTheZeroFunction() {
        val result = fit(
            CoordinatePoint(-2.0, 0.0),
            CoordinatePoint(-1.0, 0.0),
            CoordinatePoint(1.0, 0.0),
            CoordinatePoint(3.0, 0.0),
        )

        assertFit(result, a = 0.0, b = 0.0, c = 0.0, d = 0.0)
    }

    @Test
    fun aConstantFunctionIsRepresentedWithoutInventingHigherTerms() {
        val result = fit(
            CoordinatePoint(-2.0, 5.0),
            CoordinatePoint(-1.0, 5.0),
            CoordinatePoint(1.0, 5.0),
            CoordinatePoint(3.0, 5.0),
            CoordinatePoint(8.0, 5.0),
        )

        assertFit(result, a = 0.0, b = 0.0, c = 0.0, d = 5.0)
    }

    @Test
    fun negativeAndUnsortedCoordinatesAreSupported() {
        val result = fit(
            CoordinatePoint(3.0, 11.5),
            CoordinatePoint(-2.0, -16.0),
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(1.0, 0.5),
        )

        assertFit(result, a = 1.0, b = -2.0, c = 0.5, d = 1.0)
    }

    @Test
    fun emptyInputReturnsTypedEmptyInput() {
        val result = CubicFunctionFitter.fit(emptyList())

        assertEquals(
            CoordinatePointSetValidationResult.EmptyInput,
            (result as CubicFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun pointSetInputUsesTheSameValidationRules() {
        val result = CubicFunctionFitter.fit(CoordinatePointSet(emptyList()))

        assertEquals(
            CoordinatePointSetValidationResult.EmptyInput,
            (result as CubicFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun fewerThanFourPointsReturnTypedInsufficientInput() {
        val result = fit(
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(1.0, 2.0),
            CoordinatePoint(2.0, 3.0),
        )

        assertEquals(
            CoordinatePointSetValidationResult.InsufficientPointCount(3, 4),
            (result as CubicFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun duplicateXReturnsTypedInvalidInput() {
        val result = fit(
            CoordinatePoint(0.0, 0.0),
            CoordinatePoint(1.0, 1.0),
            CoordinatePoint(1.0, 2.0),
            CoordinatePoint(2.0, 3.0),
        )

        assertTrue(result is CubicFunctionFitResult.InvalidInput)
        assertTrue(
            (result as CubicFunctionFitResult.InvalidInput).reason is
                CoordinatePointSetValidationResult.DuplicateXCoordinate,
        )
    }

    @Test
    fun nonFiniteCoordinateReturnsTypedInvalidInput() {
        val result = fit(
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(1.0, 2.0),
            CoordinatePoint(2.0, Double.POSITIVE_INFINITY),
            CoordinatePoint(3.0, 4.0),
        )

        assertEquals(
            CoordinatePointSetValidationResult.NonFiniteCoordinate(2, CoordinateAxis.Y),
            (result as CubicFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun normalizedXUnderflowReturnsPrecisionFailure() {
        val result = fit(
            CoordinatePoint(Double.MIN_VALUE, 1.0),
            CoordinatePoint(2.0 * Double.MIN_VALUE, 2.0),
            CoordinatePoint(3.0 * Double.MIN_VALUE, 3.0),
            CoordinatePoint(Double.MAX_VALUE, 4.0),
        )

        assertEquals(
            FunctionFitErrorCode.NUMERIC_PRECISION_LOSS,
            (result as CubicFunctionFitResult.Failure).error.code,
        )
    }

    @Test
    fun unrepresentableCubicCoefficientReturnsNonFiniteResult() {
        val result = fit(
            CoordinatePoint(0.0, 0.0),
            CoordinatePoint(1e-200, 0.0),
            CoordinatePoint(2e-200, 0.0),
            CoordinatePoint(3e-200, 1.0),
        )

        assertEquals(
            FunctionFitErrorCode.NON_FINITE_RESULT,
            (result as CubicFunctionFitResult.Failure).error.code,
        )
    }

    @Test
    fun cubicCoefficientUnderflowReturnsPrecisionFailure() {
        val result = fit(
            CoordinatePoint(0.0, 0.0),
            CoordinatePoint(1e200, 0.0),
            CoordinatePoint(2e200, 0.0),
            CoordinatePoint(3e200, 1.0),
        )

        assertEquals(
            FunctionFitErrorCode.NUMERIC_PRECISION_LOSS,
            (result as CubicFunctionFitResult.Failure).error.code,
        )
    }

    @Test
    fun extremeSymmetricLinearCoordinatesArePreservedExactly() {
        val result = fit(
            CoordinatePoint(-Double.MAX_VALUE, -Double.MAX_VALUE),
            CoordinatePoint(-Double.MAX_VALUE / 2.0, -Double.MAX_VALUE / 2.0),
            CoordinatePoint(0.0, 0.0),
            CoordinatePoint(Double.MAX_VALUE / 2.0, Double.MAX_VALUE / 2.0),
            CoordinatePoint(Double.MAX_VALUE, Double.MAX_VALUE),
        )

        assertFit(
            result,
            a = 0.0,
            b = 0.0,
            c = 1.0,
            d = 0.0,
            tolerance = 1e-12,
        )
    }

    private fun fit(vararg points: CoordinatePoint) =
        CubicFunctionFitter.fit(points.toList())

    private fun assertFit(
        result: CubicFunctionFitResult,
        a: Double,
        b: Double,
        c: Double,
        d: Double,
        tolerance: Double = 1e-8,
    ) {
        assertTrue("Expected success but got $result", result is CubicFunctionFitResult.Success)
        val function = (result as CubicFunctionFitResult.Success).function
        assertEquals(a, function.cubicCoefficient, tolerance)
        assertEquals(b, function.quadraticCoefficient, tolerance)
        assertEquals(c, function.linearCoefficient, tolerance)
        assertEquals(d, function.intercept, tolerance)
    }
}
