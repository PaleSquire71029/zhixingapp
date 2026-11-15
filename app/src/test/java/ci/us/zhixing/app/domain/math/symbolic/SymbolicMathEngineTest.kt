package ci.us.zhixing.app.domain.math.symbolic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SymbolicMathEngineTest {
    @Test
    fun evaluatesArithmeticAndTrigonometricExpressions() {
        assertSuccess("2+3*4", "14")
        assertSuccess("Sin(Pi/2)", "1")
        assertSuccess("Sqrt(9)", "3")
    }

    @Test
    fun differentiatesAFunctionSymbolically() {
        val result = SymbolicMathEngine.evaluate("D(x^3+Sin(x),x)")
        assertTrue(result.toString(), result is SymbolicMathResult.Success)
        val expression = (result as SymbolicMathResult.Success).expression
        assertTrue(expression, expression.contains("3*x^2") || expression.contains("x^2*3"))
        assertTrue(expression, expression.contains("Cos(x)"))
    }

    @Test
    fun solvesAQuadraticEquationExactly() {
        val result = SymbolicMathEngine.evaluate("Solve(x^2-1==0,x)")
        assertTrue(result.toString(), result is SymbolicMathResult.Success)
        val expression = (result as SymbolicMathResult.Success).expression
        assertTrue(expression, expression.contains("-1"))
        assertTrue(expression, expression.contains("1"))
        assertTrue(expression, expression.contains("x"))
    }

    @Test
    fun rejectsProcessAndFileSystemFunctionsBeforeEvaluation() {
        assertFailure("Run(\"echo unsafe\")", SymbolicMathErrorCode.UNSUPPORTED_CONSTRUCT)
        assertFailure("Import(\"/etc/passwd\")", SymbolicMathErrorCode.UNSUPPORTED_CONSTRUCT)
        assertFailure("Clear(x)", SymbolicMathErrorCode.UNSUPPORTED_CONSTRUCT)
    }

    @Test
    fun acceptsCommonHighSchoolParameterSymbolsAndRejectsOversizedInput() {
        val result = SymbolicMathEngine.evaluate("a*x+b*m+n")
        assertTrue(result.toString(), result is SymbolicMathResult.Success)
        assertFailure("x+".padEnd(513, '1'), SymbolicMathErrorCode.INPUT_TOO_LONG)
    }

    @Test
    fun solvesLinearSystemsAndReducesPolynomialInequalities() {
        val system = SymbolicMathEngine.evaluate("Solve({x+y==3,x-y==1},{x,y})")
        assertTrue(system.toString(), system is SymbolicMathResult.Success)
        val systemSuccess = requireSuccess(system, "Solve({x+y==3,x-y==1},{x,y})")
        assertTrue(systemSuccess.expression.contains("x"))
        assertTrue(systemSuccess.expression.contains("y"))

        // The bundled Symja implementation accepts Reduce(condition, variable);
        // unlike Wolfram Language, this version does not take a third domain argument.
        val inequality = SymbolicMathEngine.evaluate("Reduce(x^2<4,x)")
        val inequalitySuccess = requireSuccess(inequality, "Reduce(x^2<4,x)")
        assertTrue(inequalitySuccess.expression, inequalitySuccess.expression.contains("-2"))
        assertTrue(inequalitySuccess.expression, inequalitySuccess.expression.contains("2"))
    }

    @Test
    fun supportsSequencesCombinatoricsAndStatistics() {
        assertSuccess("Sum(k,{k,1,5})", "15")
        assertSuccess("Binomial(5,2)", "10")
        assertSuccess("Mean({1,2,3,4})", "5/2")
    }

    @Test
    fun supportsVectorAndMatrixOperations() {
        assertSuccess("Dot({1,2},{3,4})", "11")
        assertSuccess("Det({{1,2},{3,4}})", "-2")
    }

    @Test
    fun supportsFactorizationIntegrationAndLimits() {
        val factor = SymbolicMathEngine.evaluate("Factor(x^2-1)")
        val factorSuccess = requireSuccess(factor, "Factor(x^2-1)")
        assertTrue(factorSuccess.expression, factorSuccess.expression.contains("x"))

        val integral = SymbolicMathEngine.evaluate("Integrate(x^2,x)")
        val integralSuccess = requireSuccess(integral, "Integrate(x^2,x)")
        assertTrue(integralSuccess.expression, integralSuccess.expression.contains("x^3"))

        val limit = SymbolicMathEngine.evaluate("Limit(Sin(x)/x,x->0)")
        val limitSuccess = requireSuccess(limit, "Limit(Sin(x)/x,x->0)")
        assertEquals("1", limitSuccess.expression)
    }

    @Test
    fun retainsOriginalDomainHolesAfterSymbolicSimplification() {
        val result = SymbolicMathEngine.evaluate("Simplify(x/x)")
        assertTrue(result.toString(), result is SymbolicMathResult.Success)
        val success = result as SymbolicMathResult.Success
        assertEquals("1", success.expression)
        assertTrue(
            success.domainRestrictions.toString(),
            success.domainRestrictions.any {
                it == SymbolicDomainRestriction.NonZero("x")
            },
        )
    }

    @Test
    fun recordsCommonRootLogAndTrigonometricDomainRestrictions() {
        val result = SymbolicMathEngine.evaluate(
            "Sqrt(x)+x^(1/2)+x^(-1/2)+Log(x)+Tan(x)",
        )
        assertTrue(result.toString(), result is SymbolicMathResult.Success)
        val restrictions = (result as SymbolicMathResult.Success).domainRestrictions
        assertTrue(restrictions.any { it == SymbolicDomainRestriction.NonNegative("x") })
        assertTrue(restrictions.any { it == SymbolicDomainRestriction.Positive("x") })
        assertTrue(restrictions.any { it == SymbolicDomainRestriction.NonZero("Cos(x)") })
    }

    @Test
    fun boundsExponentAndFactorialWork() {
        assertFailure("x^999", SymbolicMathErrorCode.EXPRESSION_TOO_COMPLEX)
        assertFailure("999!", SymbolicMathErrorCode.EXPRESSION_TOO_COMPLEX)
    }

    @Test
    fun rejectsInvalidTimeoutAndEmptyInput() {
        assertFailure("", SymbolicMathErrorCode.EMPTY_INPUT)
        assertFailure("x+1", SymbolicMathErrorCode.INVALID_TIMEOUT, timeoutMillis = 50)
    }

    private fun assertSuccess(input: String, expected: String) {
        val result = SymbolicMathEngine.evaluate(input)
        assertTrue("Expected success for $input, got $result", result is SymbolicMathResult.Success)
        assertEquals(expected, (result as SymbolicMathResult.Success).expression)
    }

    private fun requireSuccess(
        result: SymbolicMathResult,
        input: String,
    ): SymbolicMathResult.Success {
        assertTrue("Expected success for $input, got $result", result is SymbolicMathResult.Success)
        return result as SymbolicMathResult.Success
    }

    private fun assertFailure(
        input: String,
        expected: SymbolicMathErrorCode,
        timeoutMillis: Long = 1_500L,
    ) {
        val result = SymbolicMathEngine.evaluate(input, timeoutMillis)
        assertTrue("Expected failure for $input, got $result", result is SymbolicMathResult.Failure)
        assertEquals(expected, (result as SymbolicMathResult.Failure).error.code)
    }
}
