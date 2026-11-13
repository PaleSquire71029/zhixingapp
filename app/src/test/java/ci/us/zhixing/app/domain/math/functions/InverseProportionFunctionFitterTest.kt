package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinateAxis
import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.CoordinatePointSet
import ci.us.zhixing.app.domain.math.model.CoordinatePointSetValidationResult
import ci.us.zhixing.app.domain.math.model.FunctionFitErrorCode
import ci.us.zhixing.app.domain.math.model.InverseProportionFitInputError
import ci.us.zhixing.app.domain.math.model.InverseProportionFunctionFitResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InverseProportionFunctionFitterTest {
    @Test
    fun onePointUniquelyDeterminesTheInverseProportionConstant() {
        assertFit(
            fit(CoordinatePoint(3.0, 4.0)),
            constant = 12.0,
            pointCount = 1,
        )
    }

    @Test
    fun onePointWithNegativeXPreservesTheSignOfTheConstant() {
        assertFit(
            fit(CoordinatePoint(-3.0, 4.0)),
            constant = -12.0,
            pointCount = 1,
        )
    }

    @Test
    fun exactMultiplePointsRecoverTheSameInverseProportionFunction() {
        assertFit(
            fit(
                CoordinatePoint(-4.0, -3.0),
                CoordinatePoint(1.0, 12.0),
                CoordinatePoint(3.0, 4.0),
                CoordinatePoint(-2.0, -6.0),
            ),
            constant = 12.0,
            pointCount = 4,
        )
    }

    @Test
    fun noisyPointsUseVerticalOrdinaryLeastSquares() {
        // For x=[1,2,4], y=[1,3,1], the OLS coefficient is 44/21.
        assertFit(
            fit(
                CoordinatePoint(1.0, 1.0),
                CoordinatePoint(2.0, 3.0),
                CoordinatePoint(4.0, 1.0),
            ),
            constant = 44.0 / 21.0,
            pointCount = 3,
        )
    }

    @Test
    fun unsortedNegativeAndPositiveCoordinatesAreSupported() {
        assertFit(
            fit(
                CoordinatePoint(4.0, 3.0),
                CoordinatePoint(-2.0, -6.0),
                CoordinatePoint(1.0, 12.0),
                CoordinatePoint(-4.0, -3.0),
            ),
            constant = 12.0,
            pointCount = 4,
        )
    }

    @Test
    fun allZeroYValuesProduceTheZeroInverseProportionFunction() {
        assertFit(
            fit(
                CoordinatePoint(-3.0, 0.0),
                CoordinatePoint(1.0, 0.0),
                CoordinatePoint(5.0, 0.0),
            ),
            constant = 0.0,
            pointCount = 3,
        )
    }

    @Test
    fun zeroXIsRejectedAsAnInverseProportionDomainError() {
        val result = fit(CoordinatePoint(-0.0, 2.0))

        assertEquals(
            InverseProportionFitInputError.ZeroXCoordinate(0, -0.0),
            (result as InverseProportionFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun zeroXIsRejectedForPointSetInputToo() {
        val result = InverseProportionFunctionFitter.fit(
            CoordinatePointSet(
                listOf(
                    CoordinatePoint(2.0, 1.0),
                    CoordinatePoint(0.0, 3.0),
                ),
            ),
        )

        assertEquals(
            InverseProportionFitInputError.ZeroXCoordinate(1, 0.0),
            (result as InverseProportionFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun emptyListAndEmptyPointSetReturnTypedValidationErrors() {
        val listResult = InverseProportionFunctionFitter.fit(emptyList())
        val setResult = InverseProportionFunctionFitter.fit(CoordinatePointSet(emptyList()))

        assertEquals(
            InverseProportionFitInputError.PointSetValidation(
                CoordinatePointSetValidationResult.EmptyInput,
            ),
            (listResult as InverseProportionFunctionFitResult.InvalidInput).reason,
        )
        assertEquals(
            InverseProportionFitInputError.PointSetValidation(
                CoordinatePointSetValidationResult.EmptyInput,
            ),
            (setResult as InverseProportionFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun duplicateXReturnsTheSharedTypedValidationError() {
        val result = fit(
            CoordinatePoint(2.0, 1.0),
            CoordinatePoint(2.0, 5.0),
        )

        assertEquals(
            InverseProportionFitInputError.PointSetValidation(
                CoordinatePointSetValidationResult.DuplicateXCoordinate(2.0, 0, 1),
            ),
            (result as InverseProportionFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun nonFiniteCoordinatesRetainTheirAxisAndPointIndex() {
        val xResult = fit(CoordinatePoint(Double.NaN, 1.0))
        val yResult = fit(CoordinatePoint(1.0, Double.POSITIVE_INFINITY))

        assertEquals(
            InverseProportionFitInputError.PointSetValidation(
                CoordinatePointSetValidationResult.NonFiniteCoordinate(0, CoordinateAxis.X),
            ),
            (xResult as InverseProportionFunctionFitResult.InvalidInput).reason,
        )
        assertEquals(
            InverseProportionFitInputError.PointSetValidation(
                CoordinatePointSetValidationResult.NonFiniteCoordinate(0, CoordinateAxis.Y),
            ),
            (yResult as InverseProportionFunctionFitResult.InvalidInput).reason,
        )
    }

    @Test
    fun reciprocalNormalizationUnderflowReturnsPrecisionFailure() {
        val result = fit(
            CoordinatePoint(Double.MIN_VALUE, 1.0),
            CoordinatePoint(Double.MAX_VALUE, 2.0),
        )

        assertEquals(
            FunctionFitErrorCode.NUMERIC_PRECISION_LOSS,
            (result as InverseProportionFunctionFitResult.Failure).error.code,
        )
    }

    @Test
    fun normalizedYUnderflowReturnsPrecisionFailure() {
        val result = fit(
            CoordinatePoint(1.0, Double.MIN_VALUE),
            CoordinatePoint(2.0, Double.MAX_VALUE),
        )

        assertEquals(
            FunctionFitErrorCode.NUMERIC_PRECISION_LOSS,
            (result as InverseProportionFunctionFitResult.Failure).error.code,
        )
    }

    @Test
    fun unrepresentableConstantReturnsNonFiniteResult() {
        val result = fit(CoordinatePoint(Double.MAX_VALUE, 2.0))

        assertEquals(
            FunctionFitErrorCode.NON_FINITE_RESULT,
            (result as InverseProportionFunctionFitResult.Failure).error.code,
        )
    }

    @Test
    fun nonzeroConstantUnderflowReturnsPrecisionFailure() {
        val result = fit(CoordinatePoint(Double.MIN_VALUE, Double.MIN_VALUE))

        assertEquals(
            FunctionFitErrorCode.NUMERIC_PRECISION_LOSS,
            (result as InverseProportionFunctionFitResult.Failure).error.code,
        )
    }

    @Test
    fun extremeButRepresentableConstantIsPreserved() {
        assertFit(
            fit(CoordinatePoint(1e200, 1e-100)),
            constant = 1e100,
            pointCount = 1,
            tolerance = 1e-10,
        )
    }

    private fun fit(vararg points: CoordinatePoint) =
        InverseProportionFunctionFitter.fit(points.toList())

    private fun assertFit(
        result: InverseProportionFunctionFitResult,
        constant: Double,
        pointCount: Int,
        tolerance: Double = 1e-10,
    ) {
        assertTrue("Expected success but got $result", result is InverseProportionFunctionFitResult.Success)
        val success = result as InverseProportionFunctionFitResult.Success
        assertEquals(pointCount, success.pointCount)
        assertEquals(constant, success.function.constant, tolerance)
    }
}
