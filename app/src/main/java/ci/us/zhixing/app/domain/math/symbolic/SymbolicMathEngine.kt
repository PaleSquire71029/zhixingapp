package ci.us.zhixing.app.domain.math.symbolic

import org.matheclipse.core.basic.Config
import org.matheclipse.core.eval.ExprEvaluator
import org.matheclipse.core.expression.F
import org.matheclipse.core.interfaces.IExpr
import java.util.concurrent.TimeUnit

internal enum class SymbolicMathErrorCode {
    EMPTY_INPUT,
    INPUT_TOO_LONG,
    INVALID_TIMEOUT,
    INVALID_EXPRESSION,
    UNSUPPORTED_CONSTRUCT,
    EXPRESSION_TOO_COMPLEX,
    TIMEOUT,
    ENGINE_FAILURE,
}

internal data class SymbolicMathError(
    val code: SymbolicMathErrorCode,
    val message: String,
)

internal sealed interface SymbolicMathResult {
    data class Success(
        val expression: String,
        val domainRestrictions: List<SymbolicDomainRestriction> = emptyList(),
    ) : SymbolicMathResult

    data class Failure(val error: SymbolicMathError) : SymbolicMathResult
}

/**
 * Restrictions are extracted from the user's original AST before CAS
 * simplification, so operations such as Simplify(x/x) do not erase the
 * original denominator hole from the result contract.
 */
internal sealed interface SymbolicDomainRestriction {
    val expression: String

    data class NonZero(override val expression: String) : SymbolicDomainRestriction
    data class Positive(override val expression: String) : SymbolicDomainRestriction
    data class NonNegative(override val expression: String) : SymbolicDomainRestriction
    data class NotEqual(
        override val expression: String,
        val value: String,
    ) : SymbolicDomainRestriction
    data class ClosedInterval(
        override val expression: String,
        val lowerBound: String,
        val upperBound: String,
    ) : SymbolicDomainRestriction
    data class OpenInterval(
        override val expression: String,
        val lowerBound: String,
        val upperBound: String,
    ) : SymbolicDomainRestriction
}

/**
 * A narrowly-scoped adapter around Symja's LGPL core.
 *
 * The adapter parses first and validates the complete AST before evaluation.
 * Only explicitly listed mathematical heads and symbols are accepted; file,
 * network, process, reflection and arbitrary scripting heads are rejected.
 *
 * Calls are serialized because Symja keeps some process-wide and thread-local
 * evaluation state. Call this API from a worker thread, never from Compose UI.
 */
internal object SymbolicMathEngine {
    private val engineLock = Any()
    @Volatile
    private var initialized = false

    fun evaluate(
        source: String,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    ): SymbolicMathResult = synchronized(engineLock) {
        if (source.isBlank()) {
            return@synchronized failure(
                SymbolicMathErrorCode.EMPTY_INPUT,
                "表达式不能为空",
            )
        }
        if (source.length > MAX_INPUT_CHARS) {
            return@synchronized failure(
                SymbolicMathErrorCode.INPUT_TOO_LONG,
                "表达式长度不能超过 $MAX_INPUT_CHARS 个字符",
            )
        }
        if (timeoutMillis !in MIN_TIMEOUT_MILLIS..MAX_TIMEOUT_MILLIS) {
            return@synchronized failure(
                SymbolicMathErrorCode.INVALID_TIMEOUT,
                "计算超时必须在 $MIN_TIMEOUT_MILLIS 到 $MAX_TIMEOUT_MILLIS 毫秒之间",
            )
        }

        try {
            initializeIfNeeded()
            val evaluator = ExprEvaluator()
            val parsed = evaluator.parse(source)
                ?: return@synchronized failure(
                    SymbolicMathErrorCode.INVALID_EXPRESSION,
                    "表达式无法解析",
                )

            val validation = validate(parsed)
            if (validation != null) {
                return@synchronized validation
            }

            // Collect restrictions from the original input before symbolic
            // rewriting, cancellation or simplification can remove them.
            val originalDomainRestrictions = collectDomainRestrictions(parsed)

            val result = evaluator.evaluateWithTimeout(
                source,
                timeoutMillis,
                TimeUnit.MILLISECONDS,
                true,
                null,
            ) ?: return@synchronized failure(
                SymbolicMathErrorCode.ENGINE_FAILURE,
                "数学引擎没有返回结果",
            )

            when (result.toString()) {
                ABORTED_MARKER -> failure(
                    SymbolicMathErrorCode.TIMEOUT,
                    "计算超时，请简化表达式后重试",
                )
                NULL_MARKER, FAILED_MARKER -> failure(
                    SymbolicMathErrorCode.ENGINE_FAILURE,
                    "数学引擎无法完成该计算",
                )
                else -> SymbolicMathResult.Success(
                    expression = result.toString(),
                    domainRestrictions = originalDomainRestrictions,
                )
            }
        } catch (exception: Exception) {
            failure(
                SymbolicMathErrorCode.INVALID_EXPRESSION,
                exception.message?.take(MAX_ERROR_MESSAGE_CHARS)
                    ?: "表达式解析或计算失败",
            )
        }
    }

    private fun initializeIfNeeded() {
        if (initialized) return

        Config.JAVA_AWT_DESKTOP_AVAILABLE = false
        Config.DISABLE_JMX = true

        // Initialize Symja's own built-in symbol/AST registry before applying
        // per-session limits; a tiny user-expression limit here breaks startup.
        F.initSymbols()

        // The adapter separately enforces the small user-input AST budget in
        // validate(). These global limits must still leave room for CAS internals.
        Config.MAX_INPUT_LEAVES = MAX_INTERNAL_AST_SIZE.toLong()
        Config.MAX_AST_SIZE = MAX_INTERNAL_AST_SIZE
        Config.MAX_OUTPUT_SIZE = MAX_OUTPUT_CHARS
        Config.MAX_MATRIX_DIMENSION_SIZE = MAX_MATRIX_DIMENSION
        Config.MAX_BIT_LENGTH = MAX_INTEGER_BIT_LENGTH
        initialized = true
    }

    /**
     * Returns a typed failure when a node is not in the small safe mathematics
     * language. Parsing alone is not treated as permission to evaluate a head.
     */
    private fun validate(root: IExpr): SymbolicMathResult.Failure? {
        val nodeCounter = intArrayOf(0)

        fun visit(expression: IExpr, depth: Int): SymbolicMathResult.Failure? {
            nodeCounter[0]++
            if (nodeCounter[0] > MAX_AST_NODES || depth > MAX_AST_DEPTH) {
                return failure(
                    SymbolicMathErrorCode.EXPRESSION_TOO_COMPLEX,
                    "表达式结构过于复杂",
                )
            }

            if (expression.isNumber()) return null

            if (expression.isSymbol()) {
                return if (expression.toString() in ALLOWED_SYMBOLS) {
                    null
                } else {
                    failure(
                        SymbolicMathErrorCode.UNSUPPORTED_CONSTRUCT,
                        "暂不允许使用符号：${expression}",
                    )
                }
            }

            if (!expression.isAST()) {
                return failure(
                    SymbolicMathErrorCode.UNSUPPORTED_CONSTRUCT,
                    "暂不支持该表达式节点",
                )
            }

            val head = expression.head().toString()
            if (head !in ALLOWED_HEADS) {
                return failure(
                    SymbolicMathErrorCode.UNSUPPORTED_CONSTRUCT,
                    "暂不允许使用该运算：$head",
                )
            }

            val argumentCount = expression.argSize()
            if (argumentCount > MAX_ARGUMENTS) {
                return failure(
                    SymbolicMathErrorCode.EXPRESSION_TOO_COMPLEX,
                    "单个运算的参数数量过多",
                )
            }

            if (head == "Power" && argumentCount == 2) {
                val exponent = expression.get(2).toString().toDoubleOrNull()
                if (exponent != null && exponent.isFinite() &&
                    kotlin.math.abs(exponent) > MAX_ABSOLUTE_LITERAL_EXPONENT
                ) {
                    return failure(
                        SymbolicMathErrorCode.EXPRESSION_TOO_COMPLEX,
                        "指数绝对值不能超过 $MAX_ABSOLUTE_LITERAL_EXPONENT",
                    )
                }
            }

            if ((head == "Factorial" || head == "Factorial2") && argumentCount == 1) {
                val argument = expression.get(1).toString().toDoubleOrNull()
                if (argument != null && argument.isFinite() && argument > MAX_FACTORIAL_ARGUMENT) {
                    return failure(
                        SymbolicMathErrorCode.EXPRESSION_TOO_COMPLEX,
                        "阶乘参数不能超过 $MAX_FACTORIAL_ARGUMENT",
                    )
                }
            }

            for (index in 1..argumentCount) {
                val childFailure = visit(expression.get(index), depth + 1)
                if (childFailure != null) return childFailure
            }
            return null
        }

        return visit(root, 0)
    }

    /**
     * Captures common real-domain constraints from the unmodified source AST.
     * This list travels with simplified/solved output so a removable
     * discontinuity is not silently turned into an admissible point.
     */
    private fun collectDomainRestrictions(root: IExpr): List<SymbolicDomainRestriction> {
        val restrictions = linkedSetOf<SymbolicDomainRestriction>()

        fun visit(expression: IExpr) {
            if (!expression.isAST()) return

            val head = expression.head().toString()
            val argumentCount = expression.argSize()
            if (argumentCount >= 1) {
                val first = expression.get(1).toString()
                when (head) {
                    "Power" -> {
                        if (argumentCount == 2 &&
                            expression.get(2).toString().trim().startsWith("-")
                        ) {
                            restrictions += SymbolicDomainRestriction.NonZero(first)
                        }
                    }
                    "Sqrt" -> restrictions += SymbolicDomainRestriction.NonNegative(first)
                    "Log" -> {
                        if (argumentCount == 1) {
                            restrictions += SymbolicDomainRestriction.Positive(first)
                        } else if (argumentCount == 2) {
                            restrictions += SymbolicDomainRestriction.Positive(first)
                            restrictions += SymbolicDomainRestriction.NotEqual(first, "1")
                            restrictions += SymbolicDomainRestriction.Positive(
                                expression.get(2).toString(),
                            )
                        }
                    }
                    "Tan", "Sec" ->
                        restrictions += SymbolicDomainRestriction.NonZero("Cos($first)")
                    "Cot", "Csc" ->
                        restrictions += SymbolicDomainRestriction.NonZero("Sin($first)")
                    "ArcSin", "ArcCos" ->
                        restrictions += SymbolicDomainRestriction.ClosedInterval(first, "-1", "1")
                    "ArcTanh" ->
                        restrictions += SymbolicDomainRestriction.OpenInterval(first, "-1", "1")
                }
            }

            for (index in 1..argumentCount) visit(expression.get(index))
        }

        visit(root)
        return restrictions.toList()
    }

    private fun failure(
        code: SymbolicMathErrorCode,
        message: String,
    ) = SymbolicMathResult.Failure(
        SymbolicMathError(code = code, message = message),
    )

    private val ALLOWED_SYMBOLS = ('a'..'z').map { it.toString() }.toSet() + setOf(
        "Pi",
        "E",
        "I",
        "True",
        "False",
        "Infinity",
        "ComplexInfinity",
        "Reals",
        "Integers",
        "Rationals",
        "Complexes",
    )

    private val ALLOWED_HEADS = setOf(
        // Arithmetic and elementary functions.
        "Plus", "Times", "Power", "Rational", "Complex",
        "Sin", "Cos", "Tan", "Cot", "Sec", "Csc",
        "ArcSin", "ArcCos", "ArcTan", "ArcCot", "ArcSec", "ArcCsc",
        "Sinh", "Cosh", "Tanh", "Coth", "Sech", "Csch",
        "ArcSinh", "ArcCosh", "ArcTanh", "ArcCoth", "ArcSech", "ArcCsch",
        "Exp", "Log", "Sqrt", "Abs", "Sign", "Floor", "Ceiling", "Round",
        "Min", "Max", "Factorial", "Factorial2",
        "Re", "Im", "Conjugate", "ComplexExpand",

        // Algebra, equations, inequalities and polynomial analysis.
        "Equal", "Unequal", "Less", "Greater", "LessEqual", "GreaterEqual",
        "And", "Or", "Not", "List", "Rule", "Element",
        "Simplify", "FullSimplify", "Expand", "ExpandAll", "Factor", "FactorTerms",
        "Together", "Apart", "Cancel", "Collect", "TrigExpand", "TrigReduce",
        "TrigToExp", "ExpToTrig", "PolynomialGCD", "PolynomialQuotient",
        "PolynomialRemainder", "Coefficient", "CoefficientList", "Exponent", "Variables",
        "Solve", "SolveValues", "NSolve", "Reduce", "FindRoot", "Roots",
        "FunctionDomain", "FunctionRange", "Minimize", "Maximize", "ArgMin", "ArgMax",

        // Sequences, summation, products and combinatorics.
        "Sum", "Product", "RSolve", "RecurrenceTable",
        "Binomial", "Multinomial", "Pochhammer",

        // Coordinate geometry, vectors and linear algebra.
        "Dot", "Cross", "Norm", "Det", "Inverse", "Transpose", "MatrixRank",
        "LinearSolve", "RowReduce", "NullSpace", "IdentityMatrix", "DiagonalMatrix",
        "Eigenvalues", "Eigenvectors", "CharacteristicPolynomial", "Tr",
        "Distance", "EuclideanDistance",

        // Probability and descriptive statistics.
        "Mean", "Median", "Variance", "StandardDeviation", "Covariance",
        "Correlation", "Quantile", "Quartiles", "Total", "Count", "Counts", "Tally",

        // Piecewise and conditional function definitions.
        "Piecewise", "ConditionalExpression", "UnitStep", "HeavisideTheta", "Boole",
        "Interval", "IntervalUnion", "IntervalIntersection", "Union", "Intersection",
        "Complement",

        // Calculus and numerical evaluation.
        "D", "Integrate", "Limit", "Series", "SeriesCoefficient", "Sum", "N",
    )

    private const val DEFAULT_TIMEOUT_MILLIS = 1_500L
    private const val MIN_TIMEOUT_MILLIS = 100L
    private const val MAX_TIMEOUT_MILLIS = 5_000L
    private const val MAX_INPUT_CHARS = 512
    private const val MAX_AST_NODES = 128
    private const val MAX_INTERNAL_AST_SIZE = 100_000
    private const val MAX_AST_DEPTH = 24
    private const val MAX_ARGUMENTS = 16
    private const val MAX_OUTPUT_CHARS = 4_096
    private const val MAX_MATRIX_DIMENSION = 32
    private const val MAX_INTEGER_BIT_LENGTH = 4_096
    private const val MAX_ABSOLUTE_LITERAL_EXPONENT = 256.0
    private const val MAX_FACTORIAL_ARGUMENT = 256.0
    private const val MAX_ERROR_MESSAGE_CHARS = 240
    private const val ABORTED_MARKER = "\$Aborted"
    private const val NULL_MARKER = "\$Null"
    private const val FAILED_MARKER = "\$Failed"
}
