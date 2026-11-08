package ci.us.zhixing.app.domain.math.evaluator

import ci.us.zhixing.app.domain.math.model.ExpressionParseResult
import ci.us.zhixing.app.domain.math.model.MathCalculationError
import ci.us.zhixing.app.domain.math.model.MathCalculationResult
import ci.us.zhixing.app.domain.math.model.MathCalculationStage
import ci.us.zhixing.app.domain.math.model.MathEvaluationResult
import ci.us.zhixing.app.domain.math.model.TokenizationResult
import ci.us.zhixing.app.domain.math.parser.MathExpressionParser
import ci.us.zhixing.app.domain.math.parser.MathTokenizer

/** Pure-Kotlin source -> tokenizer -> AST parser -> evaluator entry point. */
internal object MathArithmeticCalculator {
    fun calculate(source: String): MathCalculationResult {
        val tokens = when (val result = MathTokenizer.tokenize(source)) {
            is TokenizationResult.Failure -> {
                val error = result.error
                return failure(
                    MathCalculationStage.TOKENIZER,
                    error.message,
                    error.offset,
                    error.lexeme,
                )
            }
            is TokenizationResult.Success -> result.tokens
        }

        val expression = when (
            val result = MathExpressionParser.parse(tokens = tokens, sourceLength = source.length)
        ) {
            is ExpressionParseResult.Failure -> {
                val error = result.error
                return failure(
                    MathCalculationStage.PARSER,
                    error.message,
                    error.offset,
                    error.lexeme,
                )
            }
            is ExpressionParseResult.Success -> result.expression
        }

        return when (val result = MathExpressionEvaluator.evaluate(expression)) {
            is MathEvaluationResult.Success -> MathCalculationResult.Success(result.value)
            is MathEvaluationResult.Failure -> {
                val error = result.error
                failure(MathCalculationStage.EVALUATOR, error.message, error.offset)
            }
        }
    }

    private fun failure(
        stage: MathCalculationStage,
        message: String,
        offset: Int,
        lexeme: String = "",
    ) = MathCalculationResult.Failure(
        MathCalculationError(
            stage = stage,
            message = message,
            offset = offset,
            lexeme = lexeme,
        ),
    )
}
