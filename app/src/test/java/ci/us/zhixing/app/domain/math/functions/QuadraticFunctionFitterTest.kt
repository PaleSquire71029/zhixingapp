package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinateAxis
import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.CoordinatePointSet
import ci.us.zhixing.app.domain.math.model.CoordinatePointSetValidationResult
import ci.us.zhixing.app.domain.math.model.FunctionFitErrorCode
import ci.us.zhixing.app.domain.math.model.QuadraticFunctionFitResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuadraticFunctionFitterTest {
    @Test
    fun threePointsDetermineTheirUniqueInterpolatingQuadratic() {
        val result = fit(
            CoordinatePoint(-2.0, 18.0),
            CoordinatePoint(0.0, 4.0),
            CoordinatePoint(3.0, 13.0),
        )

        assertFit(result, a = 2.0, b = -3.0, c = 4.0)
    }

    @Test
    fun moreThanThreeCollinearPointsProduceAZeroQuadraticTerm() {
        val result = fit(
            CoordinatePoint(-3.0, 11.0),
            CoordinatePoint(-1.0, 7.0),
            CoordinatePoint(0.0, 5.0),
            CoordinatePoint(2.0, 1.0),
            CoordinatePoint(4.0, -3.0),
        )

        assertFit(result, a = 0.0, b = -2.0, c = 5.0)
    }

    @Test
    fun moreThanThreeExactQuadraticPointsRecoverTheOriginalFunction() {
        val result = fit(
            CoordinatePoint(-3.0, 31.0),
            CoordinatePoint(-1.0, 9.0),
            CoordinatePoint(0.0, 4.0),
            CoordinatePoint(2.0, 6.0),
            CoordinatePoint(4.0, 24.0),
        )

        assertFit(result, a = 2.0, b = -3.0, c = 4.0)
    }

    @Test
    fun noisyPointsUseOrdinaryLeastSquares() {
        // Residuals at x=-1 and x=1 are both +1.
        // The least-squares solution is 13/7*x^2 + 3*x + 59/35.
        val result = fit(
            CoordinatePoint(-2.0, 3.0),
            CoordinatePoint(-1.0, 1.0),
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(1.0, 7.0),
            CoordinatePoint(2.0, 15.0),
        )

        assertFit(result, a = 13.0 / 7.0, b = 3.0, c = 59.0 / 35.0)
    }

    @Test
    fun negativeAndUnsortedCoordinatesAreSupported() {
        val result = fit(
            CoordinatePoint(3.0, 13.0),
            CoordinatePoint(-2.0, 18.0),
            CoordinatePoint(0.0, 4.0),
        )

        assertFit(result, a = 2.0, b = -3.0, c = 4.0)
    }

    @Test
    fun allZeroYValuesProduceTheZeroFunction() {
        val result = fit(
            CoordinatePoint(-2.0, 0.0),
            CoordinatePoint(0.0, 0.0),
            CoordinatePoint(3.0, 0.0),
        )

        assertFit(result, a = 0.0, b = 0.0, c = 0.0)
    }

    @Test
    fun aConstantFunctionIsRepresentedWithoutInventingHigherTerms() {
        val result = fit(
            CoordinatePoint(-2.0, 5.0),
            CoordinatePoint(0.0, 5.0),
            CoordinatePoint(3.0, 5.0),
            CoordinatePoint(8.0, 5.0),
        )

        assertFit(result, a = 0.0, b = 0.0, c = 5.0)
    }

    @Test
    fun emptyInputReturnsTypedEmptyInput() {
        val result = QuadraticFunctionFitter.fit(emptyList())

        assertEquals(
            CoordinatePointSetValidationResult.EmptyInput,
            (result as QuadraticFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun pointSetInputUsesTheSameValidationRules() {
        val result = QuadraticFunctionFitter.fit(CoordinatePointSet(emptyList()))

        assertEquals(
            CoordinatePointSetValidationResult.EmptyInput,
            (result as QuadraticFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun fewerThanThreePointsReturnTypedInsufficientInput() {
        val result = fit(
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(1.0, 2.0),
        )

        assertEquals(
            CoordinatePointSetValidationResult.InsufficientPointCount(2, 3),
            (result as QuadraticFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun duplicateXReturnsTypedInvalidInput() {
        val result = fit(
            CoordinatePoint(1.0, 2.0),
            CoordinatePoint(1.0, 3.0),
            CoordinatePoint(2.0, 4.0),
        )

        assertTrue(result is QuadraticFunctionFitResult.InvalidInput)
        assertTrue(
            (result as QuadraticFunctionFitResult.InvalidInput).reason is
                CoordinatePointSetValidationResult.DuplicateXCoordinate,
        )
    }

    @Test
    fun nonFiniteCoordinateReturnsTypedInvalidInput() {
        val result = fit(
            CoordinatePoint(0.0, 1.0),
            CoordinatePoint(1.0, Double.NaN),
            CoordinatePoint(2.0, 3.0),
        )

        assertEquals(
            CoordinatePointSetValidationResult.NonFiniteCoordinate(1, CoordinateAxis.Y),
            (result as QuadraticFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun normalizedXUnderflowReturnsPrecisionFailure() {
        val result = fit(
            CoordinatePoint(Double.MIN_VALUE, 1.0),
            CoordinatePoint(2.0 * Double.MIN_VALUE, 2.0),
            CoordinatePoint(Double.MAX_VALUE, 3.0),
        )

        assertEquals(
            FunctionFitErrorCode.NUMERIC_PRECISION_LOSS,
            (result as QuadraticFunctionFitResult.Failure).error.code,
        )
    }

    @Test
    fun unrepresentableQuadraticCoefficientReturnsNonFiniteResult() {
        val result = fit(
            CoordinatePoint(0.0, 0.0),
            CoordinatePoint(1e-200, 0.0),
            CoordinatePoint(2e-200, 1.0),
        )

        assertEquals(
            FunctionFitErrorCode.NON_FINITE_RESULT,
            (result as QuadraticFunctionFitResult.Failure).error.code,
        )
    }

    @Test
    fun extremeSymmetricCoordinatesRecoverTheLinearFunction() {
        val result = fit(
            CoordinatePoint(-Double.MAX_VALUE, -Double.MAX_VALUE),
            CoordinatePoint(0.0, 0.0),
            CoordinatePoint(Double.MAX_VALUE, Double.MAX_VALUE),
        )

        assertFit(result, a = 0.0, b = 1.0, c = 0.0, tolerance = 1e-12)
    }

    private fun fit(vararg points: CoordinatePoint) =
        QuadraticFunctionFitter.fit(points.toList())

    private fun assertFit(
        result: QuadraticFunctionFitResult,
        a: Double,
        b: Double,
        c: Double,
        tolerance: Double = 1e-9,
    ) {
        assertTrue("Expected success but got $result", result is QuadraticFunctionFitResult.Success)
        val function = (result as QuadraticFunctionFitResult.Success).function
        assertEquals(a, function.quadraticCoefficient, tolerance)
        assertEquals(b, function.linearCoefficient, tolerance)
        assertEquals(c, function.intercept, tolerance)
    }
}
