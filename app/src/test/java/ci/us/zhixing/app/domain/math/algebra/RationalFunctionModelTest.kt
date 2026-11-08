package ci.us.zhixing.app.domain.math.algebra

import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.RationalFunction
import ci.us.zhixing.app.domain.math.model.RationalFunctionErrorCode
import ci.us.zhixing.app.domain.math.model.RationalFunctionOperationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RationalFunctionModelTest {
    @Test
    fun creationAlwaysKeepsItsCurrentNonConstantDenominatorAsRestriction() {
        val x = Polynomial.variable()
        val result = RationalFunction.create(
            numerator = Polynomial.constant(1.0),
            denominator = x,
        )

        val rational = success(result)
        assertEquals(x, rational.denominator)
        assertEquals(listOf(x), rational.domainRestrictions)
    }

    @Test
    fun creationMergesAndDeduplicatesInheritedRestrictions() {
        val x = Polynomial.variable()
        val xMinusOne = Polynomial.of(-1.0, 1.0)
        val result = RationalFunction.create(
            numerator = Polynomial.of(2.0, 1.0),
            denominator = xMinusOne,
            domainRestrictions = listOf(x, xMinusOne, x),
        )

        val rational = success(result)
        assertEquals(listOf(x, xMinusOne), rational.domainRestrictions)
    }

    @Test
    fun nonZeroConstantDenominatorsDoNotCreatePointExclusions() {
        val result = RationalFunction.create(
            numerator = Polynomial.constant(3.0),
            denominator = Polynomial.constant(2.0),
            domainRestrictions = listOf(Polynomial.constant(-4.0)),
        )

        assertTrue(success(result).domainRestrictions.isEmpty())
    }

    @Test
    fun rejectsZeroPolynomialAsCurrentDenominator() {
        val result = RationalFunction.create(
            numerator = Polynomial.constant(1.0),
            denominator = Polynomial.of(0.0),
        )

        assertFailureCode(
            RationalFunctionErrorCode.ZERO_DENOMINATOR_POLYNOMIAL,
            result,
        )
    }

    @Test
    fun rejectsAnInheritedZeroPolynomialRestrictionAsEmptyDomain() {
        val result = RationalFunction.create(
            numerator = Polynomial.constant(1.0),
            denominator = Polynomial.variable(),
            domainRestrictions = listOf(Polynomial.of(0.0)),
        )

        assertFailureCode(RationalFunctionErrorCode.EMPTY_DOMAIN, result)
    }

    @Test
    fun copiesInheritedRestrictionsInsteadOfRetainingMutableInputList() {
        val x = Polynomial.variable()
        val original = mutableListOf(x)
        val result = RationalFunction.create(
            numerator = Polynomial.constant(1.0),
            denominator = Polynomial.of(-1.0, 1.0),
            domainRestrictions = original,
        )

        val rational = success(result)
        original.clear()
        assertEquals(listOf(x, Polynomial.of(-1.0, 1.0)), rational.domainRestrictions)
    }

    private fun success(result: RationalFunctionOperationResult): RationalFunction {
        assertTrue(
            "Expected rational-function creation to succeed",
            result is RationalFunctionOperationResult.Success,
        )
        return (result as RationalFunctionOperationResult.Success).value
    }

    private fun assertFailureCode(
        expected: RationalFunctionErrorCode,
        result: RationalFunctionOperationResult,
    ) {
        assertTrue(
            "Expected rational-function creation to fail",
            result is RationalFunctionOperationResult.Failure,
        )
        assertEquals(
            expected,
            (result as RationalFunctionOperationResult.Failure).error.code,
        )
    }
}
