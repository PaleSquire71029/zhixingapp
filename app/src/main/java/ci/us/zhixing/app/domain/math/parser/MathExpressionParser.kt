package ci.us.zhixing.app.domain.math.parser

import ci.us.zhixing.app.domain.math.model.BinaryExpression
import ci.us.zhixing.app.domain.math.model.BinaryMathOperator
import ci.us.zhixing.app.domain.math.model.ExpressionParseResult
import ci.us.zhixing.app.domain.math.model.GroupExpression
import ci.us.zhixing.app.domain.math.model.MathExpression
import ci.us.zhixing.app.domain.math.model.MathParserError
import ci.us.zhixing.app.domain.math.model.MathParserErrorCode
import ci.us.zhixing.app.domain.math.model.MathSourceSpan
import ci.us.zhixing.app.domain.math.model.MathToken
import ci.us.zhixing.app.domain.math.model.MathTokenKind
import ci.us.zhixing.app.domain.math.model.NumberExpression
import ci.us.zhixing.app.domain.math.model.PercentExpression
import ci.us.zhixing.app.domain.math.model.UnaryExpression
import ci.us.zhixing.app.domain.math.model.UnaryMathOperator
import ci.us.zhixing.app.domain.math.model.VariableExpression
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

/**
 * Recursive-descent parser for arithmetic/algebra expressions.
 *
 * Parsing is separate from evaluation: this class only creates an AST and
 * reports syntax errors. It has no Android, Compose or JavaScript dependency.
 *
 * Grammar (highest precedence at the bottom):
 * expression := term (('+' | '-') term)*
 * term       := unary (('*' | '/') unary | implicitFactor)*
 * unary      := ('+' | '-') unary | power
 * power      := primary ('^' exponentChain)? ('%')*
 * exponentChain := unsignedInteger ('^' exponentChain)?
 * primary    := number | x | '(' expression ')'
 */
internal object MathExpressionParser {
    fun parse(
        tokens: List<MathToken>,
        sourceLength: Int,
    ): ExpressionParseResult = Session(tokens, sourceLength).parse()

    private class Session(
        private val tokens: List<MathToken>,
        private val sourceLength: Int,
    ) {
        private var position = 0
        private var firstError: MathParserError? = null

        fun parse(): ExpressionParseResult {
            if (tokens.isEmpty()) {
                return failure(
                    MathParserError(
                        code = MathParserErrorCode.EMPTY_EXPRESSION,
                        message = "表达式是空的",
                        offset = 0,
                    ),
                )
            }

            val expression = parseExpression()
            firstError?.let { return ExpressionParseResult.Failure(it) }
            if (expression == null) {
                return failure(
                    MathParserError(
                        code = MathParserErrorCode.INCOMPLETE_EXPRESSION,
                        message = "表达式不完整",
                        offset = sourceLength,
                    ),
                )
            }

            if (position < tokens.size) {
                val trailing = tokens[position]
                return failure(
                    MathParserError(
                        code = MathParserErrorCode.TRAILING_TOKENS,
                        message = "表达式后半段没写完",
                        offset = trailing.startOffset,
                        lexeme = trailing.lexeme,
                    ),
                )
            }

            return ExpressionParseResult.Success(expression)
        }

        private fun parseExpression(): MathExpression? {
            var left = parseTerm() ?: return null
            while (
                currentKind() == MathTokenKind.PLUS ||
                currentKind() == MathTokenKind.MINUS
            ) {
                val operator = current() ?: return incomplete()
                position++
                val right = parseTerm() ?: return null
                left = BinaryExpression(
                    operator = if (operator.kind == MathTokenKind.PLUS) {
                        BinaryMathOperator.ADD
                    } else {
                        BinaryMathOperator.SUBTRACT
                    },
                    left = left,
                    right = right,
                    span = span(left.span.startOffset, right.span.endOffset),
                )
            }
            return left
        }

        private fun parseTerm(): MathExpression? {
            var left = parseUnary() ?: return null
            while (true) {
                val token = current() ?: return left
                when (token.kind) {
                    MathTokenKind.MULTIPLY,
                    MathTokenKind.DIVIDE -> {
                        position++
                        val right = parseUnary() ?: return null
                        left = BinaryExpression(
                            operator = if (token.kind == MathTokenKind.MULTIPLY) {
                                BinaryMathOperator.MULTIPLY
                            } else {
                                BinaryMathOperator.DIVIDE
                            },
                            left = left,
                            right = right,
                            span = span(left.span.startOffset, right.span.endOffset),
                        )
                    }

                    MathTokenKind.NUMBER,
                    MathTokenKind.VARIABLE_X,
                    MathTokenKind.LEFT_PAREN -> {
                        // Legacy behavior: adjacent number/x/group factors imply multiplication.
                        val right = parseUnary() ?: return null
                        left = BinaryExpression(
                            operator = BinaryMathOperator.MULTIPLY,
                            left = left,
                            right = right,
                            isImplicitMultiplication = true,
                            span = span(left.span.startOffset, right.span.endOffset),
                        )
                    }

                    else -> return left
                }
            }
        }

        private fun parseUnary(): MathExpression? {
            val token = current() ?: return incomplete()
            val operator = when (token.kind) {
                MathTokenKind.PLUS -> UnaryMathOperator.PLUS
                MathTokenKind.MINUS -> UnaryMathOperator.MINUS
                else -> null
            } ?: return parsePower()

            position++
            val operand = parseUnary() ?: return null
            return UnaryExpression(
                operator = operator,
                operand = operand,
                span = span(token.startOffset, operand.span.endOffset),
            )
        }

        private fun parsePower(): MathExpression? {
            var base = parsePrimary() ?: return null

            if (currentKind() == MathTokenKind.POWER) {
                position++
                val exponent = parseExponentChain() ?: return null
                base = BinaryExpression(
                    operator = BinaryMathOperator.POWER,
                    left = base,
                    right = exponent,
                    span = span(base.span.startOffset, exponent.span.endOffset),
                )
            }

            while (currentKind() == MathTokenKind.PERCENT) {
                val percent = current() ?: break
                position++
                base = PercentExpression(
                    operand = base,
                    span = span(base.span.startOffset, percent.endOffset),
                )
            }
            return base
        }

        /**
         * The legacy engine accepts a chain of unsigned integer exponent
         * literals (2^3^2), not an arbitrary expression after '^'. It also
         * caps each recursively computed exponent value at 64.
         */
        private fun parseExponentChain(): MathExpression? {
            val token = current()
            if (token?.kind != MathTokenKind.NUMBER) {
                return invalidExponent(token)
            }

            val value = token.numericValue
            if (
                value == null ||
                !value.isFinite() ||
                value < 0.0 ||
                abs(value - round(value)) > INTEGER_TOLERANCE
            ) {
                return invalidExponent(token)
            }

            position++
            var exponent: MathExpression = NumberExpression(
                value = value,
                lexeme = token.lexeme,
                span = span(token.startOffset, token.endOffset),
            )

            if (currentKind() == MathTokenKind.POWER) {
                position++
                val right = parseExponentChain() ?: return null
                val combined = BinaryExpression(
                    operator = BinaryMathOperator.POWER,
                    left = exponent,
                    right = right,
                    span = span(exponent.span.startOffset, right.span.endOffset),
                )
                val computed = exponentValue(combined)
                if (!computed.isFinite() || computed > MAX_EXPONENT_VALUE) {
                    return exponentTooLarge(token)
                }
                exponent = combined
            } else if (value > MAX_EXPONENT_VALUE) {
                return exponentTooLarge(token)
            }

            return exponent
        }

        private fun exponentValue(expression: MathExpression): Double = when (expression) {
            is NumberExpression -> expression.value
            is BinaryExpression -> {
                if (expression.operator != BinaryMathOperator.POWER) {
                    Double.NaN
                } else {
                    exponentValue(expression.left).pow(exponentValue(expression.right))
                }
            }
            else -> Double.NaN
        }

        private fun parsePrimary(): MathExpression? {
            val token = current() ?: return incomplete()
            return when (token.kind) {
                MathTokenKind.NUMBER -> {
                    position++
                    val value = token.numericValue
                    if (value == null) {
                        fail(
                            MathParserErrorCode.UNEXPECTED_TOKEN,
                            "这里不该出现「${token.lexeme}」",
                            token,
                        )
                    } else {
                        NumberExpression(
                            value = value,
                            lexeme = token.lexeme,
                            span = span(token.startOffset, token.endOffset),
                        )
                    }
                }

                MathTokenKind.VARIABLE_X -> {
                    position++
                    VariableExpression(span(token.startOffset, token.endOffset))
                }

                MathTokenKind.LEFT_PAREN -> {
                    position++
                    val inner = parseExpression() ?: return null
                    val closing = current()
                    if (closing?.kind != MathTokenKind.RIGHT_PAREN) {
                        return fail(
                            code = MathParserErrorCode.UNMATCHED_LEFT_PARENTHESIS,
                            message = "括号没有配对",
                            offset = sourceLength,
                        )
                    }
                    position++
                    GroupExpression(
                        expression = inner,
                        span = span(token.startOffset, closing.endOffset),
                    )
                }

                MathTokenKind.RIGHT_PAREN -> fail(
                    code = MathParserErrorCode.EXTRA_RIGHT_PARENTHESIS,
                    message = "多了一个右括号",
                    token = token,
                )

                else -> fail(
                    code = MathParserErrorCode.UNEXPECTED_TOKEN,
                    message = "这里不该出现「${token.lexeme}」",
                    token = token,
                )
            }
        }

        private fun invalidExponent(token: MathToken?): Nothing? = fail(
            code = MathParserErrorCode.INVALID_EXPONENT,
            message = "指数需要是非负整数",
            offset = token?.startOffset ?: sourceLength,
            lexeme = token?.lexeme.orEmpty(),
        )

        private fun exponentTooLarge(token: MathToken): Nothing? = fail(
            code = MathParserErrorCode.EXPONENT_TOO_LARGE,
            message = "指数计算结果必须是不超过 64 的非负整数",
            offset = token.startOffset,
            lexeme = token.lexeme,
        )

        private fun incomplete(): Nothing? = fail(
            code = MathParserErrorCode.INCOMPLETE_EXPRESSION,
            message = "表达式不完整",
            offset = sourceLength,
        )

        private fun fail(
            code: MathParserErrorCode,
            message: String,
            token: MathToken,
        ): Nothing? = fail(code, message, token.startOffset, token.lexeme)

        private fun fail(
            code: MathParserErrorCode,
            message: String,
            offset: Int,
            lexeme: String = "",
        ): Nothing? {
            if (firstError == null) {
                firstError = MathParserError(
                    code = code,
                    message = message,
                    offset = offset,
                    lexeme = lexeme,
                )
            }
            return null
        }

        private fun failure(error: MathParserError): ExpressionParseResult.Failure =
            ExpressionParseResult.Failure(error)

        private fun current(): MathToken? = tokens.getOrNull(position)

        private fun currentKind(): MathTokenKind? = current()?.kind

        private fun span(start: Int, end: Int) = MathSourceSpan(start, end)
    }

    private const val INTEGER_TOLERANCE = 1e-9
    private const val MAX_EXPONENT_VALUE = 64.0
}
