package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialComparison
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult
import ci.us.zhixing.app.domain.math.model.RationalFunction
import ci.us.zhixing.app.domain.math.model.RationalFunctionInequality
import ci.us.zhixing.app.domain.math.model.RationalFunctionInequalitySolutionResult
import ci.us.zhixing.app.domain.math.model.RealInterval
import kotlin.math.max

/**
 * Solves real rational-function inequalities N(x)/D(x) comparison 0.
 *
 * The current denominator and every restriction inherited from the original
 * expression tree are always excluded, even when a common factor could be
 * cancelled algebraically. Numerator roots may be included only for a
 * non-strict comparison and only when they are inside the original domain.
 */
internal object RationalFunctionInequalitySolver {
    fun solve(
        inequality: RationalFunctionInequality,
    ): RationalFunctionInequalitySolutionResult {
        val function = inequality.expression
        val restrictedPolynomials = (function.domainRestrictions + function.denominator).distinct()
        val allPolynomials = listOf(function.numerator) + restrictedPolynomials
        val unsupported = allPolynomials.maxOfOrNull { it.degree }
            ?: 0
        if (unsupported > MAX_SUPPORTED_DEGREE) {
            return RationalFunctionInequalitySolutionResult.UnsupportedDegree(unsupported)
        }

        // 0 / D(x) is zero at every point of the original domain.
        if (function.numerator.isZero() && !inequality.comparison.includesEquality) {
            return RationalFunctionInequalitySolutionResult.NoSolutions
        }

        val boundaries = linkedMapOf<Double, Boundary>()
        when (val roots = collectRoots(function.numerator)) {
            is RootCollection.Unsupported -> return unsupported(roots.degree)
            is RootCollection.Failure -> return failure(roots.error)
            is RootCollection.Success -> roots.values.forEach { root ->
                boundaryAt(boundaries, root).numeratorZero = true
            }
        }

        for (restriction in restrictedPolynomials) {
            when (val roots = collectRoots(restriction)) {
                is RootCollection.Unsupported -> return unsupported(roots.degree)
                is RootCollection.Failure -> return failure(roots.error)
                is RootCollection.Success -> roots.values.forEach { root ->
                    boundaryAt(boundaries, root).excludedByDomain = true
                }
            }
        }

        val orderedBoundaries = boundaries.values.sortedBy { it.value }
        if (containsUnresolvableNeighboringRoots(orderedBoundaries)) {
            return precisionFailure(
                "分子零点与原式定义域边界过于接近，无法可靠区分",
            )
        }

        val cells = mutableListOf<RealInterval>()
        for (index in 0..orderedBoundaries.size) {
            val lower = if (index == 0) null else orderedBoundaries[index - 1].value
            val upper = if (index == orderedBoundaries.size) {
                null
            } else {
                orderedBoundaries[index].value
            }

            val sign = when {
                orderedBoundaries.isEmpty() -> signAtPositiveInfinity(function)
                index == 0 -> signAtNegativeInfinity(function)
                index == orderedBoundaries.size -> signAtPositiveInfinity(function)
                else -> signInsideInterval(
                    function = function,
                    restrictions = restrictedPolynomials,
                    lower = lower!!,
                    upper = upper!!,
                ) ?: return precisionFailure(
                    "两个相邻有理式边界之间无法找到可靠的符号采样点",
                )
            }

            if (sign == null) {
                return precisionFailure(
                    "有理式在符号区间内出现未列入边界的零点",
                )
            }

            if (inequality.comparison.acceptsSign(sign)) {
                cells += RealInterval(
                    lowerBound = lower,
                    lowerInclusive = false,
                    upperBound = upper,
                    upperInclusive = false,
                )
            }

            if (index < orderedBoundaries.size) {
                val boundary = orderedBoundaries[index]
                if (boundary.numeratorZero &&
                    !boundary.excludedByDomain &&
                    inequality.comparison.includesEquality
                ) {
                    cells += RealInterval(
                        lowerBound = boundary.value,
                        lowerInclusive = true,
                        upperBound = boundary.value,
                        upperInclusive = true,
                    )
                }
            }
        }

        val merged = mergeAdjacentCells(cells)
        return when {
            merged.isEmpty() -> RationalFunctionInequalitySolutionResult.NoSolutions
            merged.size == 1 &&
                merged[0].lowerBound == null &&
                merged[0].upperBound == null ->
                RationalFunctionInequalitySolutionResult.AllRealNumbers
            else -> RationalFunctionInequalitySolutionResult.Intervals(merged)
        }
    }

    private fun collectRoots(polynomial: Polynomial): RootCollection {
        if (polynomial.isZero() || polynomial.isConstant()) {
            return RootCollection.Success(emptyList())
        }
        return when (val roots = PolynomialArithmetic.solveRealRoots(polynomial)) {
            PolynomialRootsResult.AllRealNumbers ->
                RootCollection.Success(emptyList())

            PolynomialRootsResult.NoRealRoots ->
                RootCollection.Success(emptyList())

            is PolynomialRootsResult.FiniteRoots -> {
                val values = roots.roots.map { root ->
                    if (root.value == 0.0) 0.0 else root.value
                }
                if (values.any { !it.isFinite() }) {
                    RootCollection.Failure(
                        PolynomialError(
                            PolynomialErrorCode.NON_FINITE_RESULT,
                            "有理式不等式的零点不是有限数",
                        ),
                    )
                } else {
                    RootCollection.Success(values)
                }
            }

            is PolynomialRootsResult.UnsupportedDegree ->
                RootCollection.Unsupported(roots.degree)

            is PolynomialRootsResult.Failure ->
                RootCollection.Failure(roots.error)
        }
    }

    private fun boundaryAt(
        boundaries: MutableMap<Double, Boundary>,
        root: Double,
    ): Boundary = boundaries.getOrPut(root) { Boundary(root) }

    private fun signAtNegativeInfinity(
        function: RationalFunction,
    ): Int? {
        val numeratorSign = signAtInfinity(function.numerator, negative = true)
        val denominatorSign = signAtInfinity(function.denominator, negative = true)
        return if (denominatorSign == 0) null else numeratorSign * denominatorSign
    }

    private fun signAtPositiveInfinity(
        function: RationalFunction,
    ): Int? {
        val numeratorSign = signAtInfinity(function.numerator, negative = false)
        val denominatorSign = signAtInfinity(function.denominator, negative = false)
        return if (denominatorSign == 0) null else numeratorSign * denominatorSign
    }

    private fun signAtInfinity(
        polynomial: Polynomial,
        negative: Boolean,
    ): Int {
        if (polynomial.isZero()) return 0
        val leadingSign = polynomial.coefficients.last().compareTo(0.0)
        return if (negative && polynomial.degree % 2 != 0) {
            -leadingSign
        } else {
            leadingSign
        }
    }

    /**
     * Returns null when sampling is not representable or when a supposedly
     * root-free interval contains an uncollected zero / domain hole.
     */
    private fun signInsideInterval(
        function: RationalFunction,
        restrictions: List<Polynomial>,
        lower: Double,
        upper: Double,
    ): Int? {
        val sample = midpoint(lower, upper) ?: return null
        if (restrictions.any { PolynomialArithmetic.signAt(it, sample) == 0 }) {
            return null
        }

        val numeratorSign = PolynomialArithmetic.signAt(function.numerator, sample)
        val denominatorSign = PolynomialArithmetic.signAt(function.denominator, sample)
        if (denominatorSign == 0 ||
            (numeratorSign == 0 && !function.numerator.isZero())
        ) {
            return null
        }
        return numeratorSign * denominatorSign
    }

    /**
     * If roots from different source polynomials round to neighboring Double
     * values only a few ULPs apart, they may be one shared algebraic root.
     * Do not fabricate a tiny interval between those approximations.
     */
    private fun containsUnresolvableNeighboringRoots(
        boundaries: List<Boundary>,
    ): Boolean {
        for (index in 1 until boundaries.size) {
            val left = boundaries[index - 1].value
            val right = boundaries[index].value
            val ulp = max(Math.ulp(left), Math.ulp(right))
            if (right - left <= ulp * ROOT_SEPARATION_ULPS) return true
        }
        return false
    }

    private fun midpoint(left: Double, right: Double): Double? {
        val value = left / 2.0 + right / 2.0
        return value.takeIf { it.isFinite() && value > left && value < right }
    }

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

    private fun PolynomialComparison.acceptsSign(sign: Int): Boolean = when (this) {
        PolynomialComparison.LESS_THAN -> sign < 0
        PolynomialComparison.LESS_THAN_OR_EQUAL -> sign <= 0
        PolynomialComparison.GREATER_THAN -> sign > 0
        PolynomialComparison.GREATER_THAN_OR_EQUAL -> sign >= 0
    }

    private val PolynomialComparison.includesEquality: Boolean
        get() = this == PolynomialComparison.LESS_THAN_OR_EQUAL ||
            this == PolynomialComparison.GREATER_THAN_OR_EQUAL

    private fun unsupported(degree: Int) =
        RationalFunctionInequalitySolutionResult.UnsupportedDegree(degree)

    private fun failure(error: PolynomialError) =
        RationalFunctionInequalitySolutionResult.Failure(error)

    private fun precisionFailure(message: String) =
        failure(
            PolynomialError(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                message,
            ),
        )

    private data class Boundary(
        val value: Double,
        var numeratorZero: Boolean = false,
        var excludedByDomain: Boolean = false,
    )

    private sealed interface RootCollection {
        data class Success(val values: List<Double>) : RootCollection
        data class Unsupported(val degree: Int) : RootCollection
        data class Failure(val error: PolynomialError) : RootCollection
    }

    private const val MAX_SUPPORTED_DEGREE = 3
    private const val ROOT_SEPARATION_ULPS = 8.0
}
