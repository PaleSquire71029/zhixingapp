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


    @Test
    fun additionAndSubtractionMergeBothOriginalDenominatorRestrictions() {
        val x = Polynomial.variable()
        val xMinusOne = Polynomial.of(-1.0, 1.0)
        val left = rational(Polynomial.constant(1.0), x)
        val right = rational(Polynomial.constant(1.0), xMinusOne)

        val sum = success(RationalFunctionArithmetic.add(left, right))
        assertEquals(Polynomial.of(-1.0, 2.0), sum.numerator)
        assertEquals(Polynomial.of(0.0, -1.0, 1.0), sum.denominator)
        assertTrue(sum.domainRestrictions.contains(x))
        assertTrue(sum.domainRestrictions.contains(xMinusOne))

        val difference = success(RationalFunctionArithmetic.subtract(left, right))
        assertEquals(Polynomial.constant(-1.0), difference.numerator)
        assertEquals(Polynomial.of(0.0, -1.0, 1.0), difference.denominator)
        assertTrue(difference.domainRestrictions.contains(x))
        assertTrue(difference.domainRestrictions.contains(xMinusOne))
    }

    @Test
    fun multiplicationPreservesEveryOperandRestriction() {
        val x = Polynomial.variable()
        val xMinusOne = Polynomial.of(-1.0, 1.0)
        val left = rational(Polynomial.constant(1.0), x)
        val right = rational(Polynomial.of(1.0, 1.0), xMinusOne)

        val product = success(RationalFunctionArithmetic.multiply(left, right))
        assertEquals(Polynomial.of(1.0, 1.0), product.numerator)
        assertEquals(Polynomial.of(0.0, -1.0, 1.0), product.denominator)
        assertTrue(product.domainRestrictions.contains(x))
        assertTrue(product.domainRestrictions.contains(xMinusOne))
    }

    @Test
    fun divisionAddsTheDivisorNumeratorAsANewRestriction() {
        val xMinusTwo = Polynomial.of(-2.0, 1.0)
        val xPlusOne = Polynomial.of(1.0, 1.0)
        val dividend = rational(Polynomial.constant(1.0), Polynomial.constant(1.0))
        val divisor = rational(xPlusOne, xMinusTwo)

        val quotient = success(RationalFunctionArithmetic.divide(dividend, divisor))
        assertEquals(xMinusTwo, quotient.numerator)
        assertEquals(xPlusOne, quotient.denominator)
        assertTrue(quotient.domainRestrictions.contains(xMinusTwo))
        assertTrue(quotient.domainRestrictions.contains(xPlusOne))
    }

    @Test
    fun xOverXRetainsTheExcludedZeroWithoutCancellingTheOriginalExpression() {
        val x = Polynomial.variable()
        val variable = rational(x, Polynomial.constant(1.0))

        val result = success(RationalFunctionArithmetic.divide(variable, variable))

        assertEquals(x, result.numerator)
        assertEquals(x, result.denominator)
        assertEquals(listOf(x), result.domainRestrictions)
    }

    @Test
    fun divisionRejectsAnIdenticallyZeroRationalFunction() {
        val nonZero = rational(Polynomial.constant(1.0), Polynomial.constant(1.0))
        val zero = rational(Polynomial.of(0.0), Polynomial.variable())

        assertFailureCode(
            RationalFunctionErrorCode.ZERO_DIVISOR_FUNCTION,
            RationalFunctionArithmetic.divide(nonZero, zero),
        )
    }

    @Test
    fun zeroPowerPreservesTheOriginalExcludedPoints() {
        val x = Polynomial.variable()
        val base = rational(Polynomial.constant(1.0), x)

        val result = success(RationalFunctionArithmetic.power(base, 0))
        assertEquals(Polynomial.constant(1.0), result.numerator)
        assertEquals(Polynomial.constant(1.0), result.denominator)
        assertEquals(listOf(x), result.domainRestrictions)
    }

    @Test
    fun positivePowerRaisesNumeratorAndDenominatorWithoutDroppingRestrictions() {
        val x = Polynomial.variable()
        val xMinusOne = Polynomial.of(-1.0, 1.0)
        val base = rational(xMinusOne, x)

        val result = success(RationalFunctionArithmetic.power(base, 2))
        assertEquals(Polynomial.of(1.0, -2.0, 1.0), result.numerator)
        assertEquals(Polynomial.of(0.0, 0.0, 1.0), result.denominator)
        assertTrue(result.domainRestrictions.contains(x))
    }

    @Test
    fun invalidPowersAndNonFiniteResultsReturnTypedFailures() {
        val x = Polynomial.variable()
        val base = rational(Polynomial.constant(1.0), x)

        assertFailureCode(
            RationalFunctionErrorCode.INVALID_EXPONENT,
            RationalFunctionArithmetic.power(base, -1),
        )
        assertFailureCode(
            RationalFunctionErrorCode.INVALID_EXPONENT,
            RationalFunctionArithmetic.power(base, RationalFunctionArithmetic.MAX_EXPONENT + 1),
        )

        val large = rational(
            Polynomial.constant(Double.MAX_VALUE),
            Polynomial.constant(1.0),
        )
        assertFailureCode(
            RationalFunctionErrorCode.NON_FINITE_RESULT,
            RationalFunctionArithmetic.power(large, 2),
        )
    }

    private fun rational(
        numerator: Polynomial,
        denominator: Polynomial,
    ): RationalFunction = success(
        RationalFunction.create(
            numerator = numerator,
            denominator = denominator,
        ),
    )

    private fun success(result: RationalFunctionOperationResult): RationalFunction {
        assertTrue(
            "Expected rational-function operation to succeed",
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
