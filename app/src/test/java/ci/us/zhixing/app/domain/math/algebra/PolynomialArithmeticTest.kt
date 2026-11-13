package ci.us.zhixing.app.domain.math.algebra

import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialEvaluationResult
import ci.us.zhixing.app.domain.math.model.PolynomialOperationResult
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PolynomialArithmeticTest {
    @Test
    fun storesCoefficientsFromConstantTermUpward() {
        val polynomial = Polynomial.of(3.0, -2.0, 5.0)
        assertEquals(listOf(3.0, -2.0, 5.0), polynomial.coefficients)
        assertEquals(2, polynomial.degree)
    }

    @Test
    fun removesOnlyTrailingNearZeroCoefficients() {
        val polynomial = Polynomial.of(1.0, 2.0, 1e-13, -1e-14)
        assertEquals(listOf(1.0, 2.0), polynomial.coefficients)
        assertEquals(1, polynomial.degree)

        val zero = Polynomial.of(0.0, 1e-13, -1e-13)
        assertTrue(zero.isZero())
        assertEquals(listOf(0.0), zero.coefficients)
        assertEquals(0, zero.degree)

        // A polynomial whose every coefficient is within tolerance must use [0].
        assertEquals(listOf(0.0), Polynomial.of(1e-13, -1e-13).coefficients)
        assertEquals(listOf(0.0), Polynomial.constant(-1e-13).coefficients)
        assertEquals(listOf(0.0), Polynomial.of(-0.0).coefficients)

        // The tolerance is strict: a coefficient exactly at the threshold is retained.
        assertEquals(
            listOf(Polynomial.ZERO_TOLERANCE),
            Polynomial.constant(Polynomial.ZERO_TOLERANCE).coefficients,
        )
    }

    @Test
    fun zeroAndConstantPolynomialsHaveStableRepresentation() {
        assertEquals(listOf(0.0), Polynomial.of().coefficients)
        assertTrue(Polynomial.of(0.0).isZero())
        assertTrue(Polynomial.constant(7.0).isConstant())
        assertEquals(listOf(0.0, 1.0), Polynomial.variable().coefficients)
    }

    @Test
    fun addsAndSubtractsDifferentDegrees() {
        val left = Polynomial.of(1.0, 2.0, 3.0)
        val right = Polynomial.of(4.0, -2.0)

        assertPolynomial(listOf(5.0, 0.0, 3.0), PolynomialArithmetic.add(left, right))
        assertPolynomial(listOf(-3.0, 4.0, 3.0), PolynomialArithmetic.subtract(left, right))
        assertPolynomial(listOf(0.0), PolynomialArithmetic.subtract(left, left))
    }

    @Test
    fun multipliesPolynomialsAndKeepsCoefficientOrder() {
        // (1 + 2x + 3x²)(2 - x) = 2 + 3x + 4x² - 3x³
        val result = PolynomialArithmetic.multiply(
            Polynomial.of(1.0, 2.0, 3.0),
            Polynomial.of(2.0, -1.0),
        )
        assertPolynomial(listOf(2.0, 3.0, 4.0, -3.0), result)
    }

    @Test
    fun scalesAndEvaluatesUsingHornerMethod() {
        val polynomial = Polynomial.of(1.0, -2.0, 1.0)
        assertPolynomial(
            listOf(-3.0, 6.0, -3.0),
            PolynomialArithmetic.scale(polynomial, -3.0),
        )

        val result = PolynomialArithmetic.evaluate(polynomial, 3.0)
        assertTrue(result is PolynomialEvaluationResult.Success)
        assertEquals(4.0, (result as PolynomialEvaluationResult.Success).value, 0.0)
    }

    @Test
    fun rejectsNonFiniteInputsAndOverflowResults() {
        val invalidInput = runCatching {
            Polynomial.of(1.0, Double.NaN)
        }
        assertTrue(invalidInput.isFailure)

        val overflow = PolynomialArithmetic.multiply(
            Polynomial.of(Double.MAX_VALUE),
            Polynomial.of(2.0),
        )
        assertTrue(overflow is PolynomialOperationResult.Failure)

        val invalidScale = PolynomialArithmetic.scale(
            Polynomial.of(1.0),
            Double.POSITIVE_INFINITY,
        )
        assertTrue(invalidScale is PolynomialOperationResult.Failure)
    }

    @Test
    fun evaluationRejectsNonFiniteArgumentsAndResults() {
        val invalidInput = PolynomialArithmetic.evaluate(
            Polynomial.variable(),
            Double.NaN,
        )
        assertTrue(invalidInput is PolynomialEvaluationResult.Failure)

        val finiteBoundary = PolynomialArithmetic.evaluate(
            Polynomial.of(0.0, 1.0),
            Double.MAX_VALUE,
        )
        assertTrue(finiteBoundary is PolynomialEvaluationResult.Success)
        assertEquals(
            Double.MAX_VALUE,
            (finiteBoundary as PolynomialEvaluationResult.Success).value,
            0.0,
        )

        val overflow = PolynomialArithmetic.evaluate(
            Polynomial.of(Double.MAX_VALUE, Double.MAX_VALUE),
            2.0,
        )
        assertTrue(overflow is PolynomialEvaluationResult.Failure)
    }

    @Test
    fun exactZeroDetectionDoesNotUseAnEpsilonOrRoundedHornerResult() {
        val tinyNonZeroAtInput = Polynomial.variable()
        assertFalse(PolynomialArithmetic.isExactlyZeroAt(tinyNonZeroAtInput, 1e-300))
        assertTrue(PolynomialArithmetic.isExactlyZeroAt(Polynomial.of(-1.0, 1.0), 1.0))

        // Double Horner loses the +1 while adding it to 1e16, then returns 0.
        // The exact polynomial represented by these Double coefficients has value 1.
        val cancellation = Polynomial.of(-1e16, 1e16, 1.0)
        val rounded = PolynomialArithmetic.evaluate(cancellation, 1.0)
        assertTrue(rounded is PolynomialEvaluationResult.Success)
        assertEquals(0.0, (rounded as PolynomialEvaluationResult.Success).value, 0.0)
        assertFalse(PolynomialArithmetic.isExactlyZeroAt(cancellation, 1.0))
    }

    @Test
    fun zeroPolynomialHasEveryRealNumberAsARoot() {
        assertTrue(
            PolynomialArithmetic.solveRealRoots(Polynomial.of(0.0)) ===
                PolynomialRootsResult.AllRealNumbers,
        )
    }

    @Test
    fun nonZeroConstantPolynomialHasNoRealRoots() {
        assertTrue(
            PolynomialArithmetic.solveRealRoots(Polynomial.constant(3.0)) ===
                PolynomialRootsResult.NoRealRoots,
        )
        assertTrue(
            PolynomialArithmetic.solveRealRoots(Polynomial.constant(Polynomial.ZERO_TOLERANCE)) ===
                PolynomialRootsResult.NoRealRoots,
        )
    }

    @Test
    fun solvesLinearPolynomialsAndReportsNormalizedResidual() {
        val cases = listOf(
            Polynomial.of(3.0, 2.0) to -1.5,
            Polynomial.of(0.0, 4.0) to 0.0,
            Polynomial.of(-Double.MAX_VALUE, 1.0) to Double.MAX_VALUE,
        )

        cases.forEach { (polynomial, expectedRoot) ->
            val result = PolynomialArithmetic.solveRealRoots(polynomial)
            assertTrue(result is PolynomialRootsResult.FiniteRoots)
            val root = (result as PolynomialRootsResult.FiniteRoots).roots.single()
            assertEquals(expectedRoot, root.value, 0.0)
            assertTrue(root.normalizedResidual.isFinite())
            assertTrue(root.normalizedResidual <= 1e-12)
        }
    }

    @Test
    fun rejectsLinearRootsOutsideDoubleRange() {
        val polynomial = Polynomial.of(Double.MAX_VALUE, Polynomial.ZERO_TOLERANCE)
        val result = PolynomialArithmetic.solveRealRoots(polynomial)

        assertTrue(result is PolynomialRootsResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (result as PolynomialRootsResult.Failure).error.code,
        )
    }

    @Test
    fun rejectsLinearRootsThatUnderflowToZero() {
        val polynomial = Polynomial.of(1e-300, 1e25)
        val result = PolynomialArithmetic.solveRealRoots(polynomial)

        assertTrue(result is PolynomialRootsResult.Failure)
        assertEquals(
            PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
            (result as PolynomialRootsResult.Failure).error.code,
        )
    }

    @Test
    fun solvesQuadraticsWithTwoDistinctRealRootsAndStableSmallRoot() {
        val simple = PolynomialArithmetic.solveRealRoots(Polynomial.of(2.0, -3.0, 1.0))
        assertTrue(simple is PolynomialRootsResult.FiniteRoots)
        val simpleRoots = (simple as PolynomialRootsResult.FiniteRoots).roots
        assertEquals(listOf(1.0, 2.0), simpleRoots.map { it.value })
        assertTrue(simpleRoots.all { it.normalizedResidual <= 1e-12 })

        // The direct (-b - sqrt(D)) / (2a) formula loses the small root here.
        val illConditioned = PolynomialArithmetic.solveRealRoots(
            Polynomial.of(1.0, -1e16, 1.0),
        )
        assertTrue(illConditioned is PolynomialRootsResult.FiniteRoots)
        val roots = (illConditioned as PolynomialRootsResult.FiniteRoots).roots
        assertEquals(2, roots.size)
        assertTrue(kotlin.math.abs(roots[0].value - 1e-16) <= 1e-28)
        assertEquals(1e16, roots[1].value, 2.0)
        assertTrue(roots.all { it.normalizedResidual <= 1e-12 })
    }

    @Test
    fun classifiesRepeatedAndNonRealQuadraticRootsExactly() {
        val repeated = PolynomialArithmetic.solveRealRoots(Polynomial.of(1.0, -2.0, 1.0))
        assertTrue(repeated is PolynomialRootsResult.FiniteRoots)
        val repeatedRoots = (repeated as PolynomialRootsResult.FiniteRoots).roots
        assertEquals(1, repeatedRoots.size)
        assertEquals(1.0, repeatedRoots.single().value, 0.0)
        assertTrue(repeatedRoots.single().normalizedResidual <= 1e-12)

        val noRealRoots = PolynomialArithmetic.solveRealRoots(Polynomial.of(1.0, 0.0, 1.0))
        assertTrue(noRealRoots === PolynomialRootsResult.NoRealRoots)
    }

    @Test
    fun discriminantSignSurvivesNearCancellation() {
        val negative = PolynomialArithmetic.solveRealRoots(
            Polynomial.of(Math.nextUp(1.0), -2.0, 1.0),
        )
        assertTrue(negative === PolynomialRootsResult.NoRealRoots)

        val positive = PolynomialArithmetic.solveRealRoots(
            Polynomial.of(Math.nextDown(1.0), -2.0, 1.0),
        )
        assertTrue(positive is PolynomialRootsResult.FiniteRoots)
        assertEquals(2, (positive as PolynomialRootsResult.FiniteRoots).roots.size)
    }

    @Test
    fun reportsQuadraticRootOverflowAndUnderflowWithTypedErrors() {
        val overflow = PolynomialArithmetic.solveRealRoots(
            Polynomial.of(1.0, Double.MAX_VALUE, Polynomial.ZERO_TOLERANCE),
        )
        assertTrue(overflow is PolynomialRootsResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (overflow as PolynomialRootsResult.Failure).error.code,
        )

        val underflow = PolynomialArithmetic.solveRealRoots(
            Polynomial.of(1e-300, 1e300, 1e25),
        )
        assertTrue(underflow is PolynomialRootsResult.Failure)
        assertEquals(
            PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
            (underflow as PolynomialRootsResult.Failure).error.code,
        )
    }

    @Test
    fun reportsQuarticAsUnsupportedAfterCubicSolverIsAdded() {
        val result = PolynomialArithmetic.solveRealRoots(
            Polynomial.of(1.0, 2.0, 3.0, 4.0, 5.0),
        )

        assertEquals(PolynomialRootsResult.UnsupportedDegree(4), result)
    }

    private fun assertPolynomial(
        expected: List<Double>,
        actual: PolynomialOperationResult,
    ) {
        assertTrue("Expected polynomial operation to succeed", actual is PolynomialOperationResult.Success)
        assertEquals(expected, (actual as PolynomialOperationResult.Success).polynomial.coefficients)
    }
}
