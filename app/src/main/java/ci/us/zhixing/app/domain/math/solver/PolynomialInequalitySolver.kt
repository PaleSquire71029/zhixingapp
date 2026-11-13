package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialComparison
import ci.us.zhixing.app.domain.math.model.PolynomialInequality
import ci.us.zhixing.app.domain.math.model.PolynomialInequalitySolutionResult
import ci.us.zhixing.app.domain.math.model.PolynomialRealRoot
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult
import ci.us.zhixing.app.domain.math.model.RealInterval

/**
 * Solves real polynomial inequalities through degree three.
 *
 * Real roots partition the line into open intervals. Signs inside bounded
 * intervals are evaluated with exact binary-rational Horner arithmetic;
 * signs on the two unbounded intervals follow from degree parity and the
 * leading coefficient. This means an even-multiplicity root does not
 * automatically flip the sign.
 */
internal object PolynomialInequalitySolver {
    fun solve(
        inequality: PolynomialInequality,
    ): PolynomialInequalitySolutionResult {
        val polynomial = inequality.expression
        if (polynomial.degree > MAX_SUPPORTED_DEGREE) {
            return PolynomialInequalitySolutionResult.UnsupportedDegree(polynomial.degree)
        }

        if (polynomial.isZero()) {
            return if (inequality.comparison.includesEquality) {
                PolynomialInequalitySolutionResult.AllRealNumbers
            } else {
                PolynomialInequalitySolutionResult.NoSolutions
            }
        }

        if (polynomial.isConstant()) {
            return if (inequality.comparison.acceptsSign(
                    polynomial.coefficients[0].compareTo(0.0),
                )
            ) {
                PolynomialInequalitySolutionResult.AllRealNumbers
            } else {
                PolynomialInequalitySolutionResult.NoSolutions
            }
        }

        return when (val roots = PolynomialArithmetic.solveRealRoots(polynomial)) {
            PolynomialRootsResult.AllRealNumbers ->
                PolynomialInequalitySolutionResult.AllRealNumbers

            PolynomialRootsResult.NoRealRoots -> {
                val sign = polynomial.coefficients.last().compareTo(0.0)
                if (inequality.comparison.acceptsSign(sign)) {
                    PolynomialInequalitySolutionResult.AllRealNumbers
                } else {
                    PolynomialInequalitySolutionResult.NoSolutions
                }
            }

            is PolynomialRootsResult.UnsupportedDegree ->
                PolynomialInequalitySolutionResult.UnsupportedDegree(roots.degree)

            is PolynomialRootsResult.Failure ->
                PolynomialInequalitySolutionResult.Failure(roots.error)

            is PolynomialRootsResult.FiniteRoots ->
                buildSolutionFromRoots(polynomial, inequality.comparison, roots.roots)
        }
    }

    private fun buildSolutionFromRoots(
        polynomial: Polynomial,
        comparison: PolynomialComparison,
        roots: List<PolynomialRealRoot>,
    ): PolynomialInequalitySolutionResult {
        val orderedRoots = roots.map { it.value }.distinct().sorted()
        if (orderedRoots.size != roots.size || orderedRoots.any { !it.isFinite() }) {
            return precisionFailure(
                "不等式的实根边界无法在 Double 中可靠区分",
            )
        }

        val leadingSign = polynomial.coefficients.last().compareTo(0.0)
        val degreeIsEven = polynomial.degree % 2 == 0
        val cells = mutableListOf<RealInterval>()

        for (index in 0..orderedRoots.size) {
            val lower = if (index == 0) null else orderedRoots[index - 1]
            val upper = if (index == orderedRoots.size) null else orderedRoots[index]

            val sign = when {
                index == 0 -> if (degreeIsEven) leadingSign else -leadingSign
                index == orderedRoots.size -> leadingSign
                else -> {
                    val sample = midpoint(lower!!, upper!!)
                        ?: return precisionFailure(
                            "两个相邻实根过于接近，Double 中不存在可靠的区间采样点",
                        )
                    val sampledSign = PolynomialArithmetic.signAt(polynomial, sample)
                    if (sampledSign == 0) {
                        return precisionFailure(
                            "符号区间内部出现额外零点，无法可靠构造不等式解集",
                        )
                    }
                    sampledSign
                }
            }

            if (comparison.acceptsSign(sign)) {
                cells += RealInterval(
                    lowerBound = lower,
                    lowerInclusive = false,
                    upperBound = upper,
                    upperInclusive = false,
                )
            }

            if (index < orderedRoots.size && comparison.includesEquality) {
                val root = orderedRoots[index]
                cells += RealInterval(
                    lowerBound = root,
                    lowerInclusive = true,
                    upperBound = root,
                    upperInclusive = true,
                )
            }
        }

        val merged = mergeAdjacentCells(cells)
        return if (merged.isEmpty()) {
            PolynomialInequalitySolutionResult.NoSolutions
        } else if (merged.size == 1 &&
            merged[0].lowerBound == null &&
            merged[0].upperBound == null
        ) {
            PolynomialInequalitySolutionResult.AllRealNumbers
        } else {
            PolynomialInequalitySolutionResult.Intervals(merged)
        }
    }

    /**
     * Adjacent true cells merge only when their shared boundary is included.
     * Thus a strict inequality keeps a hole at every root, while a non-strict
     * inequality joins sign-compatible intervals through included root points.
     */
    private fun mergeAdjacentCells(
        cells: List<RealInterval>,
    ): List<RealInterval> {
        val result = mutableListOf<RealInterval>()
        for (cell in cells) {
            val previous = result.lastOrNull()
            val canMerge = previous != null &&
                previous.upperBound != null &&
                previous.upperBound == cell.lowerBound &&
                (previous.upperInclusive || cell.lowerInclusive)

            if (canMerge) {
                result[result.lastIndex] = RealInterval(
                    lowerBound = previous!!.lowerBound,
                    lowerInclusive = previous.lowerInclusive,
                    upperBound = cell.upperBound,
                    upperInclusive = cell.upperInclusive,
                )
            } else {
                result += cell
            }
        }
        return result
    }

    /** Overflow-safe midpoint; null means no representable interior Double. */
    private fun midpoint(left: Double, right: Double): Double? {
        val value = left / 2.0 + right / 2.0
        return value.takeIf { it.isFinite() && it > left && it < right }
    }

    private fun precisionFailure(message: String) =
        PolynomialInequalitySolutionResult.Failure(
            ci.us.zhixing.app.domain.math.model.PolynomialError(
                ci.us.zhixing.app.domain.math.model.PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                message,
            ),
        )

    private val PolynomialComparison.includesEquality: Boolean
        get() = this == PolynomialComparison.LESS_THAN_OR_EQUAL ||
            this == PolynomialComparison.GREATER_THAN_OR_EQUAL

    private fun PolynomialComparison.acceptsSign(sign: Int): Boolean = when (this) {
        PolynomialComparison.LESS_THAN -> sign < 0
        PolynomialComparison.LESS_THAN_OR_EQUAL -> sign <= 0
        PolynomialComparison.GREATER_THAN -> sign > 0
        PolynomialComparison.GREATER_THAN_OR_EQUAL -> sign >= 0
    }

    private const val MAX_SUPPORTED_DEGREE = 3
}
