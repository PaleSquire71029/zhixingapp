package ci.us.zhixing.app.domain.math.parser

import ci.us.zhixing.app.domain.math.model.MathToken
import ci.us.zhixing.app.domain.math.model.MathTokenKind
import ci.us.zhixing.app.domain.math.model.MathTokenizerError
import ci.us.zhixing.app.domain.math.model.MathTokenizerErrorCode
import ci.us.zhixing.app.domain.math.model.TokenizationResult

/**
 * Pure Kotlin port of the legacy zxTokenize lexical contract.
 *
 * It intentionally does not parse expressions or evaluate values. Every token
 * retains offsets into the original UTF-16 input so later parser diagnostics
 * can point at the characters the user actually typed, even after Unicode
 * normalization (including superscript expansion).
 */
internal object MathTokenizer {
    fun tokenize(source: String): TokenizationResult {
        val input = normalize(source)
        val tokens = ArrayList<MathToken>()
        var index = 0

        while (index < input.size) {
            val current = input[index]
            if (current.value.isAsciiDigit() || current.value == '.') {
                val start = index
                while (
                    index < input.size &&
                    (input[index].value.isAsciiDigit() || input[index].value == '.')
                ) {
                    index++
                }

                val numberText = input.subList(start, index).joinToString("") { it.value.toString() }
                if (numberText == "." || numberText.count { it == '.' } > 1) {
                    return TokenizationResult.Failure(
                        MathTokenizerError(
                            code = MathTokenizerErrorCode.INVALID_NUMBER,
                            message = "数字格式不对：$numberText",
                            offset = input[start].sourceStart,
                            lexeme = numberText,
                        ),
                    )
                }

                val numericValue = numberText.toDoubleOrNull()
                    ?: return TokenizationResult.Failure(
                        MathTokenizerError(
                            code = MathTokenizerErrorCode.INVALID_NUMBER,
                            message = "数字格式不对：$numberText",
                            offset = input[start].sourceStart,
                            lexeme = numberText,
                        ),
                    )

                tokens += MathToken(
                    kind = MathTokenKind.NUMBER,
                    lexeme = numberText,
                    numericValue = numericValue,
                    startOffset = input[start].sourceStart,
                    endOffset = input[index - 1].sourceEnd,
                )
                continue
            }

            val kind = MathTokenKind.fromSymbol(current.value)
            if (kind == null) {
                return TokenizationResult.Failure(
                    MathTokenizerError(
                        code = MathTokenizerErrorCode.UNKNOWN_CHARACTER,
                        message = "不认识的字符：${current.value}",
                        offset = current.sourceStart,
                        lexeme = current.value.toString(),
                    ),
                )
            }

            tokens += MathToken(
                kind = kind,
                lexeme = current.value.toString(),
                numericValue = null,
                startOffset = current.sourceStart,
                endOffset = current.sourceEnd,
            )
            index++
        }

        return TokenizationResult.Success(tokens)
    }

    private fun normalize(source: String): List<NormalizedChar> {
        val output = ArrayList<NormalizedChar>(source.length)
        source.forEachIndexed { index, original ->
            if (original.isWhitespace() || original == '\uFEFF') return@forEachIndexed

            val replacement = when (original) {
                '×', '✕', '✖' -> "*"
                '÷', '／' -> "/"
                '−', '–', '—', 'ー' -> "-"
                '（' -> "("
                '）' -> ")"
                '²' -> "^2"
                '³' -> "^3"
                'Ｘ', 'ｘ' -> "x"
                else -> original.toString()
            }

            replacement.forEach { normalized ->
                output += NormalizedChar(
                    value = normalized,
                    sourceStart = index,
                    sourceEnd = index + 1,
                )
            }
        }
        return output
    }

    private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'

    private data class NormalizedChar(
        val value: Char,
        val sourceStart: Int,
        val sourceEnd: Int,
    )
}
