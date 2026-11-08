package ci.us.zhixing.app.domain.math.parser

import ci.us.zhixing.app.domain.math.golden.CalculatorGoldenCases
import ci.us.zhixing.app.domain.math.model.MathTokenKind
import ci.us.zhixing.app.domain.math.model.MathTokenizerErrorCode
import ci.us.zhixing.app.domain.math.model.TokenizationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathTokenizerTest {
    @Test
    fun matchesEveryTokenizerGoldenCase() {
        CalculatorGoldenCases.tokenizer.forEach { golden ->
            when (val result = MathTokenizer.tokenize(golden.input)) {
                is TokenizationResult.Success -> {
                    assertTrue(
                        "${golden.id}: expected an error but tokenization succeeded",
                        golden.expectedError == null,
                    )
                    assertEquals(
                        golden.id,
                        golden.expectedTokens,
                        result.tokens.map { it.toGoldenLabel() },
                    )
                }

                is TokenizationResult.Failure -> {
                    assertTrue(
                        "${golden.id}: unexpected tokenizer error ${result.error.message}",
                        golden.expectedTokens == null,
                    )
                    assertEquals(golden.id, golden.expectedError, result.error.message)
                }
            }
        }
    }

    @Test
    fun stripsWhitespaceWithoutLosingOriginalOffsets() {
        val result = MathTokenizer.tokenize("  2 × 3  ")
        assertTrue(result is TokenizationResult.Success)
        val tokens = (result as TokenizationResult.Success).tokens

        assertEquals(listOf("num:2.0", "*", "num:3.0"), tokens.map { it.toGoldenLabel() })
        assertEquals(2, tokens[0].startOffset)
        assertEquals(3, tokens[0].endOffset)
        assertEquals(4, tokens[1].startOffset)
        assertEquals(5, tokens[1].endOffset)
        assertEquals(6, tokens[2].startOffset)
        assertEquals(7, tokens[2].endOffset)
    }

    @Test
    fun superscriptExpansionMapsBothTokensToTheOriginalCharacter() {
        val result = MathTokenizer.tokenize("x²")
        assertTrue(result is TokenizationResult.Success)
        val tokens = (result as TokenizationResult.Success).tokens

        assertEquals(
            listOf(MathTokenKind.VARIABLE_X, MathTokenKind.POWER, MathTokenKind.NUMBER),
            tokens.map { it.kind },
        )
        assertEquals(1 to 2, tokens[1].startOffset to tokens[1].endOffset)
        assertEquals(1 to 2, tokens[2].startOffset to tokens[2].endOffset)
    }

    @Test
    fun reportsUnknownCharacterAtItsOriginalOffset() {
        val result = MathTokenizer.tokenize(" 2 @ 3")
        assertTrue(result is TokenizationResult.Failure)
        val error = (result as TokenizationResult.Failure).error

        assertEquals(MathTokenizerErrorCode.UNKNOWN_CHARACTER, error.code)
        assertEquals("不认识的字符：@", error.message)
        assertEquals(3, error.offset)
        assertEquals("@", error.lexeme)
    }

    @Test
    fun reportsMalformedNumberAtTheStartOfItsOriginalSpan() {
        val result = MathTokenizer.tokenize("  1..2")
        assertTrue(result is TokenizationResult.Failure)
        val error = (result as TokenizationResult.Failure).error

        assertEquals(MathTokenizerErrorCode.INVALID_NUMBER, error.code)
        assertEquals("数字格式不对：1..2", error.message)
        assertEquals(2, error.offset)
        assertEquals("1..2", error.lexeme)
    }

    @Test
    fun emptyInputProducesAnEmptyTokenStream() {
        val result = MathTokenizer.tokenize(" \n\t")
        assertTrue(result is TokenizationResult.Success)
        assertTrue((result as TokenizationResult.Success).tokens.isEmpty())
    }

    @Test
    fun keepsLegacyAsciiUppercaseXBehavior() {
        val result = MathTokenizer.tokenize("X+1")
        assertTrue(result is TokenizationResult.Failure)
        assertEquals("不认识的字符：X", (result as TokenizationResult.Failure).error.message)
    }

    @Test
    fun supportsAllLegacyDashAndMultiplicationAliases() {
        val result = MathTokenizer.tokenize("1✕2✖3–4—5ー6")
        assertTrue(result is TokenizationResult.Success)
        assertEquals(
            listOf(
                "num:1.0", "*", "num:2.0", "*", "num:3.0",
                "-", "num:4.0", "-", "num:5.0", "-", "num:6.0",
            ),
            (result as TokenizationResult.Success).tokens.map { it.toGoldenLabel() },
        )
    }
}
