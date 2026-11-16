package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.PeriodicSolutionFamily
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionInterval
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionIntervalErrorCode
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionIntervalResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class PeriodicSolutionIntervalFilterTest {
    @Test
    fun closedIntervalIncludesBothEndpointSolutions() {
        val result = filter(
            listOf(PeriodicSolutionFamily(0.0, 2.0 * PI)),
            PeriodicSolutionInterval(0.0, 4.0 * PI),
        )

        assertValues(result, listOf(0.0, 2.0 * PI, 4.0 * PI))
    }

    @Test
    fun openAndHalfOpenIntervalsExcludeOnlyTheirOpenEndpoints() {
        val family = PeriodicSolutionFamily(0.0, 2.0 * PI)

        assertValues(
            filter(listOf(family), PeriodicSolutionInterval(0.0, 4.0 * PI, false, true)),
            listOf(2.0 * PI, 4.0 * PI),
        )
        assertValues(
            filter(listOf(family), PeriodicSolutionInterval(0.0, 4.0 * PI, true, false)),
            listOf(0.0, 2.0 * PI),
        )
        assertValues(
            filter(listOf(family), PeriodicSolutionInterval(0.0, 4.0 * PI, false, false)),
            listOf(2.0 * PI),
        )
    }

    @Test
    fun mergesAndSortsMultipleFamiliesWithoutDuplicatingSharedRoots() {
        val result = filter(
            listOf(
                PeriodicSolutionFamily(PI, 2.0 * PI),
                PeriodicSolutionFamily(0.0, 2.0 * PI),
                PeriodicSolutionFamily(0.0, 2.0 * PI),
            ),
            PeriodicSolutionInterval(0.0, 2.0 * PI),
        )

        assertValues(result, listOf(0.0, PI, 2.0 * PI))
    }

    @Test
    fun supportsNegativeIntervalsAndNegativePeriodIndices() {
        val result = filter(
            listOf(PeriodicSolutionFamily(0.5, 2.0)),
            PeriodicSolutionInterval(-3.5, 2.5),
        )

        assertValues(result, listOf(-3.5, -1.5, 0.5, 2.5))
    }

    @Test
    fun closedSingletonReturnsRootOnlyWhenItMatchesExactly() {
        assertValues(
            filter(
                listOf(PeriodicSolutionFamily(1.0, 2.0)),
                PeriodicSolutionInterval(5.0, 5.0),
            ),
            listOf(5.0),
        )
        assertTrue(
            filter(
                listOf(PeriodicSolutionFamily(0.0, 2.0)),
                PeriodicSolutionInterval(5.0, 5.0),
            ) === PeriodicSolutionIntervalResult.NoSolutions,
        )
    }

    @Test
    fun equalOpenOrHalfOpenIntervalIsEmpty() {
        val result = filter(
            listOf(PeriodicSolutionFamily(1.0, 2.0)),
            PeriodicSolutionInterval(1.0, 1.0, includeLowerBound = false),
        )

        assertTrue(result === PeriodicSolutionIntervalResult.NoSolutions)
    }

    @Test
    fun intervalWithoutAnyPeriodicRootReturnsNoSolutions() {
        val result = filter(
            listOf(PeriodicSolutionFamily(0.0, 10.0)),
            PeriodicSolutionInterval(1.0, 2.0),
        )

        assertTrue(result === PeriodicSolutionIntervalResult.NoSolutions)
    }

    @Test
    fun invalidIntervalOrderReturnsTypedFailure() {
        val result = filter(
            listOf(PeriodicSolutionFamily(0.0, 1.0)),
            PeriodicSolutionInterval(2.0, 1.0),
        )

        assertFailure(result, PeriodicSolutionIntervalErrorCode.INVALID_INTERVAL)
    }

    @Test
    fun nonFiniteIntervalBoundsReturnTypedFailure() {
        val result = filter(
            listOf(PeriodicSolutionFamily(0.0, 1.0)),
            PeriodicSolutionInterval(Double.NEGATIVE_INFINITY, 1.0),
        )

        assertFailure(result, PeriodicSolutionIntervalErrorCode.NON_FINITE_INPUT)
    }

    @Test
    fun invalidFamilyReturnsTypedFailure() {
        val result = filter(
            listOf(PeriodicSolutionFamily(1.0, 1.0)),
            PeriodicSolutionInterval(0.0, 2.0),
        )

        assertFailure(result, PeriodicSolutionIntervalErrorCode.INVALID_FAMILY)
    }

    @Test
    fun nonPositiveMaximumReturnsTypedFailure() {
        val result = PeriodicSolutionIntervalFilter.filter(
            listOf(PeriodicSolutionFamily(0.0, 1.0)),
            PeriodicSolutionInterval(0.0, 1.0),
            maxSolutions = 0,
        )

        assertFailure(result, PeriodicSolutionIntervalErrorCode.INVALID_MAX_SOLUTIONS)
    }

    @Test
    fun tooManySolutionsAreRejectedBeforeMaterializingAnUnboundedList() {
        val result = PeriodicSolutionIntervalFilter.filter(
            listOf(PeriodicSolutionFamily(0.0, 1.0)),
            PeriodicSolutionInterval(0.0, 10.0),
            maxSolutions = 5,
        )

        assertFailure(result, PeriodicSolutionIntervalErrorCode.TOO_MANY_SOLUTIONS)
    }

    @Test
    fun exactBigIntegerPeriodIndexSupportsLargeButRepresentableOffset() {
        val result = filter(
            listOf(PeriodicSolutionFamily(0.0, 1.0)),
            PeriodicSolutionInterval(1.0e16, 1.0e16),
        )

        assertValues(result, listOf(1.0e16))
    }

    @Test
    fun distinctRootsThatCollapseToOneDoubleReturnPrecisionFailure() {
        val result = filter(
            listOf(PeriodicSolutionFamily(0.0, 1.0)),
            PeriodicSolutionInterval(1.0e16, 1.0e16 + 4.0),
        )

        assertFailure(result, PeriodicSolutionIntervalErrorCode.NUMERIC_PRECISION_LOSS)
    }

    @Test
    fun rejectsSubnormalPeriodWhenTheIntervalContainsTooManyRoots() {
        val result = PeriodicSolutionIntervalFilter.filter(
            listOf(PeriodicSolutionFamily(0.0, Double.MIN_VALUE)),
            PeriodicSolutionInterval(1.0, 1.0 + 1.0e-15),
            maxSolutions = 100,
        )

        assertFailure(result, PeriodicSolutionIntervalErrorCode.TOO_MANY_SOLUTIONS)
    }

    private fun filter(
        families: List<PeriodicSolutionFamily>,
        interval: PeriodicSolutionInterval,
    ) = PeriodicSolutionIntervalFilter.filter(families, interval)

    private fun assertValues(
        result: PeriodicSolutionIntervalResult,
        expected: List<Double>,
    ) {
        assertTrue("Expected finite solutions, got $result", result is PeriodicSolutionIntervalResult.FiniteSolutions)
        assertEquals(expected, (result as PeriodicSolutionIntervalResult.FiniteSolutions).values)
    }

    private fun assertFailure(
        result: PeriodicSolutionIntervalResult,
        expected: PeriodicSolutionIntervalErrorCode,
    ) {
        assertTrue("Expected $expected, got $result", result is PeriodicSolutionIntervalResult.Failure)
        assertEquals(expected, (result as PeriodicSolutionIntervalResult.Failure).error.code)
    }
}
