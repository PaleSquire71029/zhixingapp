package ci.us.zhixing.app.domain.math.parser

import ci.us.zhixing.app.domain.math.golden.CalculatorGoldenCases
import ci.us.zhixing.app.domain.math.model.BinaryExpression
import ci.us.zhixing.app.domain.math.model.BinaryMathOperator
import ci.us.zhixing.app.domain.math.model.ExpressionParseResult
import ci.us.zhixing.app.domain.math.model.GroupExpression
import ci.us.zhixing.app.domain.math.model.MathParserErrorCode
import ci.us.zhixing.app.domain.math.model.NumberExpression
import ci.us.zhixing.app.domain.math.model.PercentExpression
import ci.us.zhixing.app.domain.math.model.TokenizationResult
import ci.us.zhixing.app.domain.math.model.UnaryExpression
import ci.us.zhixing.app.domain.math.model.UnaryMathOperator
import ci.us.zhixing.app.domain.math.model.VariableExpression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathExpressionParserTest {
    @Test
    fun parsesAllWellFormedArithmeticGoldenInputs() {
        val syntaxErrors = setOf("incomplete-expression", "unclosed-parenthesis")
        CalculatorGoldenCases.arithmetic
            .filterNot { it.id in syntaxErrors }
            .forEach { golden ->
                val result = parse(golden.input)
                assertTrue(
                    "${golden.id}: expected valid syntax, got ${result}",
                    result is ExpressionParseResult.Success,
                )
            }
    }

    @Test
    fun unaryMinusHasLowerPrecedenceThanPower() {
        val result = parseSuccess("-2^2")
        val unary = result as UnaryExpression
        assertEquals(UnaryMathOperator.MINUS, unary.operator)

        val power = unary.operand as BinaryExpression
        assertEquals(BinaryMathOperator.POWER, power.operator)
        assertEquals(2.0, (power.left as NumberExpression).value, 0.0)
        assertEquals(2.0, (power.right as NumberExpression).value, 0.0)
    }

    @Test
    fun parenthesesChangeThePowerBaseGrouping() {
        val result = parseSuccess("(-2)^2")
        val power = result as BinaryExpression
        assertEquals(BinaryMathOperator.POWER, power.operator)

        val group = power.left as GroupExpression
        val unary = group.expression as UnaryExpression
        assertEquals(UnaryMathOperator.MINUS, unary.operator)
        assertEquals(2.0, (unary.operand as NumberExpression).value, 0.0)
    }

    @Test
    fun unaryMinusCanWrapAParenthesizedPower() {
        val result = parseSuccess("-(2^2)")
        val unary = result as UnaryExpression
        val group = unary.operand as GroupExpression
        val power = group.expression as BinaryExpression
        assertEquals(BinaryMathOperator.POWER, power.operator)
    }

    @Test
    fun powerChainsAreRightAssociative() {
        val result = parseSuccess("2^3^2") as BinaryExpression
        assertEquals(BinaryMathOperator.POWER, result.operator)
        assertTrue(result.right is BinaryExpression)
        assertEquals(BinaryMathOperator.POWER, (result.right as BinaryExpression).operator)
        assertEquals(
            2.0,
            ((result.right as BinaryExpression).left as NumberExpression).value,
            0.0,
        )
    }

    @Test
    fun divisionChainsAreLeftAssociative() {
        val result = parseSuccess("8/4/2") as BinaryExpression
        assertEquals(BinaryMathOperator.DIVIDE, result.operator)
        assertTrue(result.left is BinaryExpression)
        assertEquals(BinaryMathOperator.DIVIDE, (result.left as BinaryExpression).operator)
    }

    @Test
    fun implicitMultiplicationIsMarkedExplicitlyInTheAst() {
        listOf("2x", "3(x+1)", "(x+1)(x-2)").forEach { input ->
            val result = parseSuccess(input) as BinaryExpression
            assertEquals(BinaryMathOperator.MULTIPLY, result.operator)
            assertTrue("$input must be marked as implicit multiplication", result.isImplicitMultiplication)
        }
    }

    @Test
    fun explicitMultiplicationIsNotMarkedAsImplicit() {
        val result = parseSuccess("2*3") as BinaryExpression
        assertEquals(BinaryMathOperator.MULTIPLY, result.operator)
        assertTrue(!result.isImplicitMultiplication)
    }

    @Test
    fun postfixPercentWrapsThePrimaryExpression() {
        val result = parseSuccess("50%%") as PercentExpression
        assertTrue(result.operand is PercentExpression)
        assertEquals(0, result.span.startOffset)
        assertEquals(4, result.span.endOffset)
    }

    @Test
    fun explicitGroupingIsRetainedWithSourceSpan() {
        val result = parseSuccess("(1+2)")
        assertTrue(result is GroupExpression)
        assertEquals(0, result.span.startOffset)
        assertEquals(5, result.span.endOffset)
    }

    @Test
    fun variableXIsRepresentedAsItsOwnNode() {
        val result = parseSuccess("x")
        assertTrue(result is VariableExpression)
    }

    @Test
    fun malformedExpressionsReturnStructuredErrors() {
        assertError("", MathParserErrorCode.EMPTY_EXPRESSION, "表达式是空的")
        assertError("2+", MathParserErrorCode.INCOMPLETE_EXPRESSION, "表达式不完整")
        assertError("(2+3", MathParserErrorCode.UNMATCHED_LEFT_PARENTHESIS, "括号没有配对")
        assertError(")", MathParserErrorCode.EXTRA_RIGHT_PARENTHESIS, "多了一个右括号")
        assertError("2)", MathParserErrorCode.TRAILING_TOKENS, "表达式后半段没写完")
        assertError("2+*3", MathParserErrorCode.UNEXPECTED_TOKEN, "这里不该出现「*」")
    }

    @Test
    fun exponentMustBeAnUnsignedIntegerLiteralChain() {
        assertError("2^", MathParserErrorCode.INVALID_EXPONENT, "指数需要是非负整数")
        assertError("2^-2", MathParserErrorCode.INVALID_EXPONENT, "指数需要是非负整数")
        assertError("2^2.5", MathParserErrorCode.INVALID_EXPONENT, "指数需要是非负整数")
        assertError("2^(3)", MathParserErrorCode.INVALID_EXPONENT, "指数需要是非负整数")
    }

    @Test
    fun exponentChainRetainsLegacySixtyFourLimit() {
        assertError(
            "2^3^4",
            MathParserErrorCode.EXPONENT_TOO_LARGE,
            "指数计算结果必须是不超过 64 的非负整数",
        )
        assertTrue(parse("2^2^6") is ExpressionParseResult.Success)
    }

    @Test
    fun sourceSpansReferToOriginalTextAfterUnicodeNormalization() {
        val result = parseSuccess("  −2²  ") as UnaryExpression
        assertEquals(2, result.span.startOffset)
        assertEquals(5, result.span.endOffset)

        val power = result.operand as BinaryExpression
        assertEquals(3, power.left.span.startOffset)
        assertEquals(4, power.left.span.endOffset)
        assertEquals(4, power.right.span.startOffset)
        assertEquals(5, power.right.span.endOffset)
    }

    private fun parseSuccess(input: String) =
        (parse(input) as ExpressionParseResult.Success).expression

    private fun parse(input: String): ExpressionParseResult =
        when (val tokenized = MathTokenizer.tokenize(input)) {
            is TokenizationResult.Success -> MathExpressionParser.parse(
                tokens = tokenized.tokens,
                sourceLength = input.length,
            )
            is TokenizationResult.Failure -> throw AssertionError(
                "Tokenizer rejected parser test input '$input': ${tokenized.error.message}",
            )
        }

    private fun assertError(
        input: String,
        expectedCode: MathParserErrorCode,
        expectedMessage: String,
    ) {
        val result = parse(input)
        assertTrue("$input should fail", result is ExpressionParseResult.Failure)
        val error = (result as ExpressionParseResult.Failure).error
        assertEquals(input, expectedCode, error.code)
        assertEquals(input, expectedMessage, error.message)
    }
}
