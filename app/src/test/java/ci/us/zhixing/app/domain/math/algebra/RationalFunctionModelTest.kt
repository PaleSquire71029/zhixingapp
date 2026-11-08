package ci.us.zhixing.app.domain.math.algebra

import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.RationalFunction
import ci.us.zhixing.app.domain.math.model.RationalFunctionErrorCode
import ci.us.zhixing.app.domain.math.model.RationalFunctionEvaluationResult
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

    @Test
    fun evaluatesOrdinaryRationalValues() {
        val function = rational(Polynomial.of(1.0, 1.0), Polynomial.of(-1.0, 1.0))
        val result = RationalFunctionArithmetic.evaluate(function, 3.0)
        assertTrue(result is RationalFunctionEvaluationResult.Success)
        assertEquals(2.0, (result as RationalFunctionEvaluationResult.Success).value, 0.0)
    }

    @Test
    fun rejectsEveryInheritedDomainRestriction() {
        val x = Polynomial.variable()
        val xMinusOne = Polynomial.of(-1.0, 1.0)
        val function = success(RationalFunction.create(
            numerator = Polynomial.constant(1.0),
            denominator = Polynomial.constant(1.0),
            domainRestrictions = listOf(x, xMinusOne),
        ))
        assertEvaluationFailureCode(RationalFunctionErrorCode.DOMAIN_RESTRICTION_VIOLATION, RationalFunctionArithmetic.evaluate(function, 0.0))
        assertEvaluationFailureCode(RationalFunctionErrorCode.DOMAIN_RESTRICTION_VIOLATION, RationalFunctionArithmetic.evaluate(function, 1.0))
        val valid = RationalFunctionArithmetic.evaluate(function, 2.0)
        assertTrue(valid is RationalFunctionEvaluationResult.Success)
        assertEquals(1.0, (valid as RationalFunctionEvaluationResult.Success).value, 0.0)
    }

    @Test
    fun xOverXRemainsUndefinedAtTheRemovableHole() {
        val x = Polynomial.variable()
        val variable = rational(x, Polynomial.constant(1.0))
        val xOverX = success(RationalFunctionArithmetic.divide(variable, variable))
        assertEvaluationFailureCode(RationalFunctionErrorCode.DOMAIN_RESTRICTION_VIOLATION, RationalFunctionArithmetic.evaluate(xOverX, 0.0))
        val defined = RationalFunctionArithmetic.evaluate(xOverX, 2.0)
        assertTrue(defined is RationalFunctionEvaluationResult.Success)
        assertEquals(1.0, (defined as RationalFunctionEvaluationResult.Success).value, 0.0)
    }

    @Test
    fun zeroPowerStillRejectsTheOriginalExcludedPoint() {
        val x = Polynomial.variable()
        val reciprocal = rational(Polynomial.constant(1.0), x)
        val zeroPower = success(RationalFunctionArithmetic.power(reciprocal, 0))
        assertEvaluationFailureCode(RationalFunctionErrorCode.DOMAIN_RESTRICTION_VIOLATION, RationalFunctionArithmetic.evaluate(zeroPower, 0.0))
        val defined = RationalFunctionArithmetic.evaluate(zeroPower, 2.0)
        assertTrue(defined is RationalFunctionEvaluationResult.Success)
        assertEquals(1.0, (defined as RationalFunctionEvaluationResult.Success).value, 0.0)
    }

    @Test
    fun rejectsNonFiniteInputsAndPolynomialIntermediateOverflow() {
        val identity = rational(Polynomial.variable(), Polynomial.constant(1.0))
        assertEvaluationFailureCode(RationalFunctionErrorCode.NON_FINITE_INPUT, RationalFunctionArithmetic.evaluate(identity, Double.NaN))
        assertEvaluationFailureCode(RationalFunctionErrorCode.NON_FINITE_INPUT, RationalFunctionArithmetic.evaluate(identity, Double.POSITIVE_INFINITY))
        val square = rational(Polynomial.of(0.0, 0.0, 1.0), Polynomial.constant(1.0))
        assertEvaluationFailureCode(RationalFunctionErrorCode.NON_FINITE_RESULT, RationalFunctionArithmetic.evaluate(square, Double.MAX_VALUE))
    }

    @Test
    fun rejectsOverflowInFinalDivision() {
        val function = rational(Polynomial.constant(Double.MAX_VALUE), Polynomial.constant(0.5))
        assertEvaluationFailureCode(RationalFunctionErrorCode.NON_FINITE_RESULT, RationalFunctionArithmetic.evaluate(function, 4.0))
    }
    @Test
    fun nearZeroButNonZeroRestrictionValueRemainsInDomain() {
        val variable = Polynomial.variable()
        val function = success(RationalFunction.create(
            numerator = Polynomial.constant(1.0),
            denominator = Polynomial.constant(1.0),
            domainRestrictions = listOf(variable),
        ))

        val result = RationalFunctionArithmetic.evaluate(function, 1e-300)
        assertTrue(result is RationalFunctionEvaluationResult.Success)
        assertEquals(1.0, (result as RationalFunctionEvaluationResult.Success).value, 0.0)
    }

    @Test
    fun cancellationToZeroInDoubleHornerDoesNotCreateFalseDomainHole() {
        val cancellation = Polynomial.of(-1e16, 1e16, 1.0)
        val function = success(RationalFunction.create(
            numerator = Polynomial.constant(1.0),
            denominator = Polynomial.constant(1.0),
            domainRestrictions = listOf(cancellation),
        ))

        val result = RationalFunctionArithmetic.evaluate(function, 1.0)
        assertTrue(result is RationalFunctionEvaluationResult.Success)
        assertEquals(1.0, (result as RationalFunctionEvaluationResult.Success).value, 0.0)
    }
    @Test
    fun reportsPrecisionLossWhenNonZeroDenominatorRoundsToZero() {
        val cases = listOf(
            // x² is exactly non-zero at 1e-200, but Double underflows to zero.
            Polynomial.of(0.0, 0.0, 1.0) to 1e-200,
            // Exact value is 1; Double Horner loses it during cancellation.
            Polynomial.of(-1e16, 1e16, 1.0) to 1.0,
        )

        cases.forEach { (denominator, x) ->
            assertTrue(!PolynomialArithmetic.isExactlyZeroAt(denominator, x))
            val function = rational(Polynomial.constant(1.0), denominator)
            assertEvaluationFailureCode(
                RationalFunctionErrorCode.NUMERIC_PRECISION_LOSS,
                RationalFunctionArithmetic.evaluate(function, x),
            )
        }
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

    private fun assertEvaluationFailureCode(
        expected: RationalFunctionErrorCode,
        result: RationalFunctionEvaluationResult,
    ) {
        assertTrue("Expected rational-function evaluation to fail", result is RationalFunctionEvaluationResult.Failure)
        assertEquals(expected, (result as RationalFunctionEvaluationResult.Failure).error.code)
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
