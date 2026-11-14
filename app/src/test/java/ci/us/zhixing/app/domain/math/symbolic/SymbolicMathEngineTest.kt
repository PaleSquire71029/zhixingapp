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
    fun rejectsUnknownSymbolsAndOversizedInput() {
        assertFailure("SecretVariable+1", SymbolicMathErrorCode.UNSUPPORTED_CONSTRUCT)
        assertFailure("x+".padEnd(513, '1'), SymbolicMathErrorCode.INPUT_TOO_LONG)
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
