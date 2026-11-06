package ci.us.zhixing.app.domain.math.golden

/**
 * Frozen behavior examples audited against PaleSquire71029/calculator:
 * calculator.html on main, 2026-11-06.
 *
 * These are input/output contracts for the upcoming Kotlin engine. They are
 * deliberately test-only: no parser, evaluator, solver, or UI is implemented
 * in this step. Error strings and semantic metadata describe the legacy
 * behavior; equation/inequality expectations avoid coupling to prose layout.
 */
internal data class TokenGoldenCase(
    val id: String,
    val input: String,
    val expectedTokens: List<String>? = null,
    val expectedError: String? = null,
    val tags: Set<String> = emptySet(),
)

internal data class ArithmeticGoldenCase(
    val id: String,
    val input: String,
    val expectedValue: Double? = null,
    val expectedError: String? = null,
    val tags: Set<String> = emptySet(),
)

internal data class EquationGoldenCase(
    val id: String,
    val input: String,
    val expectedRealRoots: List<Double> = emptyList(),
    val expectedAllRealExcept: List<Double> = emptyList(),
    val expectedExcludedDomainPoints: List<Double> = emptyList(),
    val expectedError: String? = null,
    val tags: Set<String> = emptySet(),
)

internal data class ExpectedInterval(
    /** null means negative infinity. */
    val lower: Double?,
    val lowerInclusive: Boolean,
    /** null means positive infinity. */
    val upper: Double?,
    val upperInclusive: Boolean,
)

internal data class InequalityGoldenCase(
    val id: String,
    val input: String,
    val expectedIntervals: List<ExpectedInterval> = emptyList(),
    val expectedExcludedPoints: List<Double> = emptyList(),
    val expectedError: String? = null,
    val tags: Set<String> = emptySet(),
)

internal object CalculatorGoldenCases {
    val tokenizer = listOf(
        TokenGoldenCase(
            id = "tokenize-ascii-and-multiply-glyph",
            input = "2 + 3 × 4",
            expectedTokens = listOf("num:2.0", "+", "num:3.0", "*", "num:4.0"),
            tags = setOf("unicode", "operators", "whitespace"),
        ),
        TokenGoldenCase(
            id = "tokenize-fullwidth-parentheses-and-minus",
            input = "（2−3）×4",
            expectedTokens = listOf("(", "num:2.0", "-", "num:3.0", ")", "*", "num:4.0"),
            tags = setOf("unicode", "normalization"),
        ),
        TokenGoldenCase(
            id = "tokenize-fullwidth-x-and-superscript",
            input = "ｘ²",
            expectedTokens = listOf("x", "^", "num:2.0"),
            tags = setOf("unicode", "normalization", "power"),
        ),
        TokenGoldenCase(
            id = "tokenize-division-glyph",
            input = "−3 ÷ 2",
            expectedTokens = listOf("-", "num:3.0", "/", "num:2.0"),
            tags = setOf("unicode", "operators"),
        ),
        TokenGoldenCase(
            id = "tokenize-leading-decimal",
            input = ".5",
            expectedTokens = listOf("num:0.5"),
            tags = setOf("decimal"),
        ),
        TokenGoldenCase(
            id = "tokenize-trailing-decimal",
            input = "1.",
            expectedTokens = listOf("num:1.0"),
            tags = setOf("decimal"),
        ),
        TokenGoldenCase(
            id = "tokenize-lone-decimal-error",
            input = ".",
            expectedError = "数字格式不对：.",
            tags = setOf("invalid-number"),
        ),
        TokenGoldenCase(
            id = "tokenize-multiple-decimal-error",
            input = "1..2",
            expectedError = "数字格式不对：1..2",
            tags = setOf("invalid-number"),
        ),
        TokenGoldenCase(
            id = "tokenize-unknown-character-error",
            input = "2@3",
            expectedError = "不认识的字符：@",
            tags = setOf("invalid-character"),
        ),
    )

    val arithmetic = listOf(
        ArithmeticGoldenCase("precedence-multiply-before-add", "2 + 3 × 4", 14.0, tags = setOf("precedence", "unicode")),
        ArithmeticGoldenCase("division-left-associative", "8 / 4 / 2", 1.0, tags = setOf("associativity")),
        ArithmeticGoldenCase("unary-minus-after-power", "-2^2", -4.0, tags = setOf("precedence", "unary-minus")),
        ArithmeticGoldenCase("parenthesized-negative-base", "(-2)^2", 4.0, tags = setOf("precedence", "parentheses")),
        ArithmeticGoldenCase("negative-parenthesized-power", "-(2^2)", -4.0, tags = setOf("precedence", "unary-minus")),
        ArithmeticGoldenCase("power-right-associative", "2^3^2", 512.0, tags = setOf("associativity", "power")),
        ArithmeticGoldenCase("postfix-percent", "50%", 0.5, tags = setOf("percent")),
        ArithmeticGoldenCase("percent-is-scaled-before-addition", "200 + 10%", 200.1, tags = setOf("percent", "legacy-semantics")),
        ArithmeticGoldenCase("implicit-multiplication", "3(2+5)", 21.0, tags = setOf("implicit-multiplication")),
        ArithmeticGoldenCase("unicode-superscript", "3² + 1", 10.0, tags = setOf("unicode", "power")),
        ArithmeticGoldenCase("divide-by-zero", "1/0", expectedError = "除数不能为 0", tags = setOf("division-by-zero")),
        ArithmeticGoldenCase("variable-is-not-arithmetic", "2x+1", expectedError = "含 x 的式子请用「方程求解」", tags = setOf("variable-mode")),
        ArithmeticGoldenCase("incomplete-expression", "2+", expectedError = "表达式不完整", tags = setOf("parse-error")),
        ArithmeticGoldenCase("unclosed-parenthesis", "(2+3", expectedError = "括号没有配对", tags = setOf("parse-error", "parentheses")),
    )

    val equations = listOf(
        EquationGoldenCase("linear-equation", "2x+3=7", expectedRealRoots = listOf(2.0), tags = setOf("linear")),
        EquationGoldenCase("quadratic-two-roots", "x^2-5x+6=0", expectedRealRoots = listOf(2.0, 3.0), tags = setOf("quadratic", "two-roots")),
        EquationGoldenCase("quadratic-no-real-root", "x^2+1=0", tags = setOf("quadratic", "no-real-root")),
        EquationGoldenCase("cubic-three-roots", "x^3-6x^2+11x-6=0", expectedRealRoots = listOf(1.0, 2.0, 3.0), tags = setOf("cubic", "three-roots")),
        EquationGoldenCase("fraction-equation-extraneous-root", "(x^2-1)/(x-1)=0", expectedRealRoots = listOf(-1.0), tags = setOf("rational-equation", "domain-pole", "extraneous-root")),
        EquationGoldenCase("identity-keeps-original-hole", "(x-1)/(x-1)=1", expectedAllRealExcept = listOf(1.0), tags = setOf("identity", "domain-pole")),
        EquationGoldenCase("nonzero-constant-over-zero-point", "1/(x-1)=0", expectedExcludedDomainPoints = listOf(1.0), tags = setOf("rational-equation", "domain-pole", "no-solution")),
    )

    val inequalities = listOf(
        InequalityGoldenCase(
            "quadratic-strict-outside",
            "x^2-1>0",
            expectedIntervals = listOf(
                ExpectedInterval(null, false, -1.0, false),
                ExpectedInterval(1.0, false, null, false),
            ),
            tags = setOf("quadratic", "strict", "interval-union"),
        ),
        InequalityGoldenCase(
            "quadratic-nonstrict-outside",
            "x^2-1>=0",
            expectedIntervals = listOf(
                ExpectedInterval(null, false, -1.0, true),
                ExpectedInterval(1.0, true, null, false),
            ),
            tags = setOf("quadratic", "nonstrict", "closed-endpoint"),
        ),
        InequalityGoldenCase(
            "rational-strict-excludes-pole",
            "1/(x-1)>0",
            expectedIntervals = listOf(ExpectedInterval(1.0, false, null, false)),
            expectedExcludedPoints = listOf(1.0),
            tags = setOf("rational-inequality", "strict", "domain-pole"),
        ),
        InequalityGoldenCase(
            "rational-nonstrict-still-excludes-pole",
            "1/(x-1)>=0",
            expectedIntervals = listOf(ExpectedInterval(1.0, false, null, false)),
            expectedExcludedPoints = listOf(1.0),
            tags = setOf("rational-inequality", "nonstrict", "domain-pole"),
        ),
        InequalityGoldenCase(
            "identity-nonstrict-excludes-original-hole",
            "(x-1)/(x-1)>=1",
            expectedIntervals = listOf(
                ExpectedInterval(null, false, 1.0, false),
                ExpectedInterval(1.0, false, null, false),
            ),
            expectedExcludedPoints = listOf(1.0),
            tags = setOf("identity", "nonstrict", "domain-pole"),
        ),
        InequalityGoldenCase(
            "identity-strict-is-empty",
            "(x-1)/(x-1)>1",
            tags = setOf("identity", "strict", "empty-set"),
        ),
        InequalityGoldenCase(
            "quartic-result-has-explicit-limit",
            "1/(x^4+1)>0",
            expectedError = "整理后是 4 次不等式，目前最高支持三次",
            tags = setOf("high-degree", "solver-limit"),
        ),
        InequalityGoldenCase(
            "seventh-degree-denominator-is-rejected",
            "1/(x^7+1)>0",
            expectedError = "左边：式子太复杂了，展开次数过高",
            tags = setOf("high-degree", "denominator-limit", "parser-limit"),
        ),
    )
}
