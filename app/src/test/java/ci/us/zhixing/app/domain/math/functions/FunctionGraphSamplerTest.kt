package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.FittedFunction
import ci.us.zhixing.app.domain.math.model.FunctionGraphSamplingErrorCode
import ci.us.zhixing.app.domain.math.model.FunctionGraphSamplingInputError
import ci.us.zhixing.app.domain.math.model.FunctionGraphSamplingRequest
import ci.us.zhixing.app.domain.math.model.FunctionGraphSamplingResult
import ci.us.zhixing.app.domain.math.model.InverseProportionFunction
import ci.us.zhixing.app.domain.math.model.LinearFunction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctionGraphSamplerTest {
    @Test
    fun polynomialFunctionProducesOneContinuousSegment() {
        val result = sample(
            FittedFunction.Linear(LinearFunction(2.0, 1.0)),
            minX = -2.0,
            maxX = 2.0,
            samples = 5,
        )

        val success = assertSuccess(result)
        assertEquals(1, success.segments.size)
        assertEquals(5, success.totalPointCount)
        assertEquals(-2.0, success.segments.single().points.first().x, 0.0)
        assertEquals(2.0, success.segments.single().points.last().x, 0.0)
    }

    @Test
    fun inverseProportionAcrossZeroProducesTwoIndependentSegments() {
        val result = sample(
            FittedFunction.InverseProportion(InverseProportionFunction(1.0)),
            minX = -2.0,
            maxX = 2.0,
            samples = 5,
        )

        val success = assertSuccess(result)
        assertEquals(2, success.segments.size)
        assertEquals(10, success.totalPointCount)
        assertEquals(listOf(0.0), success.excludedXValues)
        assertTrue(success.segments[0].points.all { it.x < 0.0 && it.y.isFinite() })
        assertTrue(success.segments[1].points.all { it.x > 0.0 && it.y.isFinite() })
        assertTrue(success.segments[0].points.last().x < 0.0)
        assertTrue(success.segments[1].points.first().x > 0.0)
    }

    @Test
    fun rangeStartingAtZeroOnlySamplesThePositiveSideAwayFromTheBoundary() {
        val success = assertSuccess(
            sample(
                FittedFunction.InverseProportion(InverseProportionFunction(2.0)),
                minX = 0.0,
                maxX = 2.0,
                samples = 5,
            ),
        )

        assertEquals(1, success.segments.size)
        assertTrue(success.segments.single().points.all { it.x > 0.0 })
        assertEquals(2.0, success.segments.single().points.last().x, 0.0)
    }

    @Test
    fun rangeEndingAtZeroOnlySamplesTheNegativeSide() {
        val success = assertSuccess(
            sample(
                FittedFunction.InverseProportion(InverseProportionFunction(2.0)),
                minX = -2.0,
                maxX = 0.0,
                samples = 5,
            ),
        )

        assertEquals(1, success.segments.size)
        assertTrue(success.segments.single().points.all { it.x < 0.0 })
        assertEquals(-2.0, success.segments.single().points.first().x, 0.0)
    }

    @Test
    fun rangeAwayFromZeroDoesNotCreateAnUnnecessaryBreak() {
        val success = assertSuccess(
            sample(
                FittedFunction.InverseProportion(InverseProportionFunction(6.0)),
                minX = 1.0,
                maxX = 3.0,
                samples = 4,
            ),
        )

        assertEquals(1, success.segments.size)
        assertEquals(4, success.totalPointCount)
    }

    @Test
    fun nonFiniteRangeAndInvalidOrderReturnTypedInputErrors() {
        assertEquals(
            FunctionGraphSamplingInputError.NON_FINITE_MIN_X,
            (sample(
                FittedFunction.Linear(LinearFunction(1.0, 0.0)),
                Double.NaN,
                1.0,
                5,
            ) as FunctionGraphSamplingResult.InvalidInput).reason,
        )
        assertEquals(
            FunctionGraphSamplingInputError.NON_FINITE_MAX_X,
            (sample(
                FittedFunction.Linear(LinearFunction(1.0, 0.0)),
                0.0,
                Double.POSITIVE_INFINITY,
                5,
            ) as FunctionGraphSamplingResult.InvalidInput).reason,
        )
        assertEquals(
            FunctionGraphSamplingInputError.MIN_X_NOT_LESS_THAN_MAX_X,
            (sample(
                FittedFunction.Linear(LinearFunction(1.0, 0.0)),
                1.0,
                1.0,
                5,
            ) as FunctionGraphSamplingResult.InvalidInput).reason,
        )
    }

    @Test
    fun sampleCountIsBounded() {
        val function = FittedFunction.Linear(LinearFunction(1.0, 0.0))
        assertEquals(
            FunctionGraphSamplingInputError.SAMPLE_COUNT_BELOW_MINIMUM,
            (sample(function, 0.0, 1.0, 1) as FunctionGraphSamplingResult.InvalidInput).reason,
        )
        assertEquals(
            FunctionGraphSamplingInputError.SAMPLE_COUNT_ABOVE_MAXIMUM,
            (sample(function, 0.0, 1.0, 513) as FunctionGraphSamplingResult.InvalidInput).reason,
        )
    }

    @Test
    fun adjacentDoubleBoundsThatCannotFitRequestedSamplesReturnPrecisionFailure() {
        val result = sample(
            FittedFunction.Linear(LinearFunction(1.0, 0.0)),
            minX = 1.0,
            maxX = Math.nextUp(1.0),
            samples = 3,
        )

        assertEquals(
            FunctionGraphSamplingErrorCode.NUMERIC_PRECISION_LOSS,
            (result as FunctionGraphSamplingResult.Failure).error.code,
        )
    }

    @Test
    fun extremeOppositeFiniteBoundsAreInterpolatedWithoutRangeOverflow() {
        val success = assertSuccess(
            sample(
                FittedFunction.Linear(LinearFunction(1.0, 0.0)),
                minX = -Double.MAX_VALUE,
                maxX = Double.MAX_VALUE,
                samples = 3,
            ),
        )

        val points = success.segments.single().points
        assertEquals(-Double.MAX_VALUE, points[0].x, 0.0)
        assertEquals(0.0, points[1].x, 0.0)
        assertEquals(Double.MAX_VALUE, points[2].x, 0.0)
    }

    @Test
    fun nonFiniteFunctionSampleReturnsTypedFailure() {
        val result = sample(
            FittedFunction.Cubic(
                ci.us.zhixing.app.domain.math.model.CubicFunction(1.0, 0.0, 0.0, 0.0),
            ),
            minX = 1e200,
            maxX = 2e200,
            samples = 3,
        )

        assertEquals(
            FunctionGraphSamplingErrorCode.NON_FINITE_FUNCTION_VALUE,
            (result as FunctionGraphSamplingResult.Failure).error.code,
        )
    }

    private fun sample(
        function: FittedFunction,
        minX: Double,
        maxX: Double,
        samples: Int,
    ) = FunctionGraphSampler.sample(
        function,
        FunctionGraphSamplingRequest(
            minX = minX,
            maxX = maxX,
            samplesPerSegment = samples,
        ),
    )

    private fun assertSuccess(
        result: FunctionGraphSamplingResult,
    ): FunctionGraphSamplingResult.Success {
        assertTrue("Expected success but got $result", result is FunctionGraphSamplingResult.Success)
        return result as FunctionGraphSamplingResult.Success
    }
}
