package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.CoordinatePointSet
import ci.us.zhixing.app.domain.math.model.CoordinatePointSetValidationResult
import ci.us.zhixing.app.domain.math.model.CubicFunction
import ci.us.zhixing.app.domain.math.model.CubicFunctionFitResult
import ci.us.zhixing.app.domain.math.model.FunctionFitError
import ci.us.zhixing.app.domain.math.model.FunctionFitErrorCode
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs

/**
 * Fits y = ax^3 + bx^2 + cx + d.
 *
 * Four distinct-x points determine an interpolating cubic. Larger point sets
 * use ordinary least squares. The normalized Vandermonde system is solved by
 * column-pivoted Householder QR, never by forming X'X.
 */
internal object CubicFunctionFitter {
    private val conversionContext = MathContext.DECIMAL128

    fun fit(points: List<CoordinatePoint>): CubicFunctionFitResult {
        val validation = CoordinatePointSetValidator.validate(
            points = points,
            requiredPointCount = MINIMUM_POINT_COUNT,
        )
        val pointSet = when (validation) {
            is CoordinatePointSetValidationResult.Valid -> validation.pointSet
            else -> return CubicFunctionFitResult.InvalidInput(validation)
        }
        return fit(pointSet)
    }

    fun fit(pointSet: CoordinatePointSet): CubicFunctionFitResult {
        // Revalidate at the public fitting boundary so an internally created
        // point set cannot bypass finiteness, uniqueness, or minimum-count checks.
        val validation = CoordinatePointSetValidator.validate(
            points = pointSet.points,
            requiredPointCount = MINIMUM_POINT_COUNT,
        )
        if (validation !is CoordinatePointSetValidationResult.Valid) {
            return CubicFunctionFitResult.InvalidInput(validation)
        }
        return fitValidated(validation.pointSet)
    }

    private fun fitValidated(pointSet: CoordinatePointSet): CubicFunctionFitResult {
        val points = pointSet.points
        val xScale = points.maxOf { abs(it.x) }
        val yScale = points.maxOf { abs(it.y) }
        if (xScale == 0.0) {
            return precisionFailure("横坐标缩放后无法区分不同点")
        }

        // At extreme x magnitudes, normalized Double coordinates can hide
        // exact low-degree structure. Preserve exact linear / quadratic data
        // using the input Doubles' exact decimal expansions before QR fitting.
        if (xScale > EXTREME_SCALE_THRESHOLD || xScale < 1.0 / EXTREME_SCALE_THRESHOLD) {
            exactQuadraticOrLowerFit(points)?.let { return it }
        }

        val normalizedX = ArrayList<Double>(points.size)
        val normalizedY = ArrayList<Double>(points.size)
        val seenX = HashMap<Double, Double>(points.size)
        val seenY = HashMap<Double, Double>(points.size)

        points.forEach { point ->
            val x = canonicalZero(point.x / xScale)
            val y = if (yScale == 0.0) 0.0 else canonicalZero(point.y / yScale)
            if (!x.isFinite() || !y.isFinite()) {
                return nonFiniteFailure("坐标归一化产生了非有限值")
            }
            if (point.x != 0.0 && x == 0.0) {
                return precisionFailure("横坐标归一化时发生下溢")
            }
            if (point.y != 0.0 && y == 0.0) {
                return precisionFailure("纵坐标归一化时发生下溢")
            }

            val previousX = seenX.putIfAbsent(x, point.x)
            if (previousX != null && previousX != point.x) {
                return precisionFailure("不同横坐标在归一化后无法区分")
            }
            val previousY = seenY.putIfAbsent(y, point.y)
            if (previousY != null && previousY != point.y) {
                return precisionFailure("不同纵坐标在归一化后无法区分")
            }
            normalizedX += x
            normalizedY += y
        }

        // Center and scale x/xScale into [-1, 1], reducing the spread of
        // magnitudes in the cubic Vandermonde columns.
        val minX = normalizedX.minOrNull() ?: return precisionFailure("缺少横坐标数据")
        val maxX = normalizedX.maxOrNull() ?: return precisionFailure("缺少横坐标数据")
        val midpoint = minX / 2.0 + maxX / 2.0
        val halfRange = maxX / 2.0 - minX / 2.0
        if (!midpoint.isFinite() || !halfRange.isFinite() || halfRange <= 0.0) {
            return precisionFailure("横坐标中心化后无法可靠区分")
        }

        val normalizedT = ArrayList<Double>(points.size)
        val seenT = HashSet<Double>(points.size)
        normalizedX.forEach { x ->
            val t = canonicalZero((x - midpoint) / halfRange)
            if (!t.isFinite()) {
                return precisionFailure("横坐标中心化产生了非有限值")
            }
            if (!seenT.add(t)) {
                return precisionFailure("不同横坐标在中心化后无法区分")
            }
            normalizedT += t
        }

        val normalizedCoefficients = when (
            val solution = solveWithPivotedHouseholderQr(normalizedT, normalizedY)
        ) {
            is QrResult.Success -> solution.coefficients
            is QrResult.PrecisionFailure -> return precisionFailure(solution.message)
        }
        if (normalizedCoefficients.any { !it.isFinite() }) {
            return precisionFailure("归一化三次拟合系数超出可靠数值范围")
        }

        val converted = convertCoefficients(
            xScale = xScale,
            yScale = yScale,
            midpoint = midpoint,
            halfRange = halfRange,
            normalizedA = normalizedCoefficients[0],
            normalizedB = normalizedCoefficients[1],
            normalizedC = normalizedCoefficients[2],
            normalizedD = normalizedCoefficients[3],
        )
        val a = when (val coefficient = converted.a) {
            is CoefficientConversion.Success -> coefficient.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("三次项系数超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("三次项系数下溢，无法可靠表示")
        }
        val b = when (val coefficient = converted.b) {
            is CoefficientConversion.Success -> coefficient.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("二次项系数超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("二次项系数下溢，无法可靠表示")
        }
        val c = when (val coefficient = converted.c) {
            is CoefficientConversion.Success -> coefficient.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("一次项系数超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("一次项系数下溢，无法可靠表示")
        }
        val d = when (val coefficient = converted.d) {
            is CoefficientConversion.Success -> coefficient.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("常数项超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("常数项下溢，无法可靠表示")
        }

        if (!a.isFinite() || !b.isFinite() || !c.isFinite() || !d.isFinite()) {
            return nonFiniteFailure("三次函数拟合系数不是有限数")
        }
        return CubicFunctionFitResult.Success(
            function = CubicFunction(a, b, c, d),
            pointCount = points.size,
        )
    }

    /**
     * Exact low-degree fallback for extreme x scales.
     *
     * A quadratic through the first three points is represented by one common
     * denominator. Every remaining point is checked by exact cross
     * multiplication, so this branch never classifies noisy data as exact.
     */
    private fun exactQuadraticOrLowerFit(
        points: List<CoordinatePoint>,
    ): CubicFunctionFitResult? {
        val x0 = BigDecimal(points[0].x)
        val x1 = BigDecimal(points[1].x)
        val x2 = BigDecimal(points[2].x)
        val y0 = BigDecimal(points[0].y)
        val y1 = BigDecimal(points[1].y)
        val y2 = BigDecimal(points[2].y)

        val d0 = x0.subtract(x1).multiply(x0.subtract(x2))
        val d1 = x1.subtract(x0).multiply(x1.subtract(x2))
        val d2 = x2.subtract(x0).multiply(x2.subtract(x1))
        val commonDenominator = d0.multiply(d1).multiply(d2)
        if (commonDenominator.signum() == 0) return null

        val numeratorA = y0.multiply(d1).multiply(d2)
            .add(y1.multiply(d0).multiply(d2))
            .add(y2.multiply(d0).multiply(d1))
        val numeratorB = y0.multiply(x1.add(x2)).negate().multiply(d1).multiply(d2)
            .add(y1.multiply(x0.add(x2)).negate().multiply(d0).multiply(d2))
            .add(y2.multiply(x0.add(x1)).negate().multiply(d0).multiply(d1))
        val numeratorC = y0.multiply(x1).multiply(x2).multiply(d1).multiply(d2)
            .add(y1.multiply(x0).multiply(x2).multiply(d0).multiply(d2))
            .add(y2.multiply(x0).multiply(x1).multiply(d0).multiply(d1))

        for (point in points.drop(3)) {
            val x = BigDecimal(point.x)
            val y = BigDecimal(point.y)
            val expectedNumerator = numeratorA.multiply(x).multiply(x)
                .add(numeratorB.multiply(x))
                .add(numeratorC)
            if (y.multiply(commonDenominator).compareTo(expectedNumerator) != 0) {
                return null
            }
        }

        val a = numeratorA.divide(commonDenominator, conversionContext)
        val b = numeratorB.divide(commonDenominator, conversionContext)
        val c = numeratorC.divide(commonDenominator, conversionContext)
        val convertedA = convertCoefficient(a)
        val convertedB = convertCoefficient(b)
        val convertedC = convertCoefficient(c)
        val coefficientA = when (convertedA) {
            is CoefficientConversion.Success -> convertedA.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("二次项系数超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("二次项系数下溢，无法可靠表示")
        }
        val coefficientB = when (convertedB) {
            is CoefficientConversion.Success -> convertedB.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("一次项系数超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("一次项系数下溢，无法可靠表示")
        }
        val coefficientC = when (convertedC) {
            is CoefficientConversion.Success -> convertedC.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("常数项系数超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("常数项系数下溢，无法可靠表示")
        }
        return CubicFunctionFitResult.Success(
            function = CubicFunction(
                cubicCoefficient = 0.0,
                quadraticCoefficient = coefficientA,
                linearCoefficient = coefficientB,
                intercept = coefficientC,
            ),
            pointCount = points.size,
        )
    }

    /** Column-pivoted Householder QR for [t^3, t^2, t, 1]. */
    private fun solveWithPivotedHouseholderQr(
        tValues: List<Double>,
        yValues: List<Double>,
    ): QrResult {
        val rowCount = tValues.size
        val matrix = Array(rowCount) { row ->
            val t = tValues[row]
            doubleArrayOf(t * t * t, t * t, t, 1.0)
        }
        val right = yValues.toDoubleArray()
        val permutation = IntArray(COLUMN_COUNT) { it }
        val initialNorm = (0 until COLUMN_COUNT).maxOf { columnNorm(matrix, 0, it) }
        if (!initialNorm.isFinite() || initialNorm == 0.0) {
            return QrResult.PrecisionFailure("三次拟合矩阵没有有效列")
        }
        val rankThreshold = initialNorm * RANK_RELATIVE_TOLERANCE

        for (pivotIndex in 0 until COLUMN_COUNT) {
            var selectedColumn = pivotIndex
            var selectedNorm = columnNorm(matrix, pivotIndex, pivotIndex)
            for (column in pivotIndex + 1 until COLUMN_COUNT) {
                val candidateNorm = columnNorm(matrix, pivotIndex, column)
                if (candidateNorm > selectedNorm) {
                    selectedNorm = candidateNorm
                    selectedColumn = column
                }
            }
            if (!selectedNorm.isFinite() || selectedNorm <= rankThreshold) {
                return QrResult.PrecisionFailure("坐标点分布退化，三次拟合矩阵数值秩不足")
            }

            if (selectedColumn != pivotIndex) {
                for (row in 0 until rowCount) {
                    val temporary = matrix[row][pivotIndex]
                    matrix[row][pivotIndex] = matrix[row][selectedColumn]
                    matrix[row][selectedColumn] = temporary
                }
                val temporary = permutation[pivotIndex]
                permutation[pivotIndex] = permutation[selectedColumn]
                permutation[selectedColumn] = temporary
            }

            val norm = columnNorm(matrix, pivotIndex, pivotIndex)
            val first = matrix[pivotIndex][pivotIndex]
            val alpha = if (first >= 0.0) -norm else norm
            val denominator = first - alpha
            if (!denominator.isFinite() || denominator == 0.0) {
                return QrResult.PrecisionFailure("QR 分解的反射向量无法可靠计算")
            }

            val reflector = DoubleArray(rowCount - pivotIndex)
            reflector[0] = 1.0
            for (row in pivotIndex + 1 until rowCount) {
                reflector[row - pivotIndex] = matrix[row][pivotIndex] / denominator
            }
            var reflectorNormSquared = 0.0
            reflector.forEach { reflectorNormSquared += it * it }
            if (!reflectorNormSquared.isFinite() || reflectorNormSquared == 0.0) {
                return QrResult.PrecisionFailure("QR 分解的反射向量失去数值精度")
            }
            val beta = 2.0 / reflectorNormSquared

            for (column in pivotIndex + 1 until COLUMN_COUNT) {
                var dot = 0.0
                for (row in pivotIndex until rowCount) {
                    dot += reflector[row - pivotIndex] * matrix[row][column]
                }
                val factor = beta * dot
                for (row in pivotIndex until rowCount) {
                    matrix[row][column] -= factor * reflector[row - pivotIndex]
                }
            }

            var rightDot = 0.0
            for (row in pivotIndex until rowCount) {
                rightDot += reflector[row - pivotIndex] * right[row]
            }
            val rightFactor = beta * rightDot
            for (row in pivotIndex until rowCount) {
                right[row] -= rightFactor * reflector[row - pivotIndex]
            }

            matrix[pivotIndex][pivotIndex] = alpha
            for (row in pivotIndex + 1 until rowCount) {
                matrix[row][pivotIndex] = 0.0
            }
        }

        val orderedCoefficients = DoubleArray(COLUMN_COUNT)
        for (row in COLUMN_COUNT - 1 downTo 0) {
            var value = right[row]
            for (column in row + 1 until COLUMN_COUNT) {
                value -= matrix[row][column] * orderedCoefficients[column]
            }
            val diagonal = matrix[row][row]
            if (!diagonal.isFinite() || abs(diagonal) <= rankThreshold) {
                return QrResult.PrecisionFailure("三次拟合矩阵在回代时数值秩不足")
            }
            orderedCoefficients[row] = value / diagonal
            if (!orderedCoefficients[row].isFinite()) {
                return QrResult.PrecisionFailure("QR 回代产生了非有限系数")
            }
        }

        val originalOrder = DoubleArray(COLUMN_COUNT)
        for (column in 0 until COLUMN_COUNT) {
            originalOrder[permutation[column]] = orderedCoefficients[column]
        }
        return QrResult.Success(originalOrder)
    }

    private fun columnNorm(
        matrix: Array<DoubleArray>,
        startRow: Int,
        column: Int,
    ): Double {
        var norm = 0.0
        for (row in startRow until matrix.size) {
            norm = Math.hypot(norm, matrix[row][column])
        }
        return norm
    }

    /**
     * Convert y=yScale*(A*t^3+B*t^2+C*t+D),
     * t=(x/xScale-midpoint)/halfRange, into original-coordinate coefficients.
     * Products / cancellation are kept exact in BigDecimal until the final
     * DECIMAL128 division, then each coefficient is range-checked.
     */
    private fun convertCoefficients(
        xScale: Double,
        yScale: Double,
        midpoint: Double,
        halfRange: Double,
        normalizedA: Double,
        normalizedB: Double,
        normalizedC: Double,
        normalizedD: Double,
    ): ConvertedCoefficients {
        if (yScale == 0.0) {
            val zero = CoefficientConversion.Success(0.0)
            return ConvertedCoefficients(zero, zero, zero, zero)
        }

        val x = BigDecimal.valueOf(xScale)
        val y = BigDecimal.valueOf(yScale)
        val m = BigDecimal.valueOf(midpoint)
        val h = BigDecimal.valueOf(halfRange)
        val h2 = h.multiply(h)
        val h3 = h2.multiply(h)
        val x2 = x.multiply(x)
        val x3 = x2.multiply(x)
        val a = BigDecimal.valueOf(normalizedA)
        val b = BigDecimal.valueOf(normalizedB)
        val c = BigDecimal.valueOf(normalizedC)
        val d = BigDecimal.valueOf(normalizedD)

        val numeratorA = a
        val numeratorB = b.multiply(h).subtract(BigDecimal.valueOf(3.0).multiply(a).multiply(m))
        val numeratorC = c.multiply(h2)
            .subtract(BigDecimal.valueOf(2.0).multiply(b).multiply(m).multiply(h))
            .add(BigDecimal.valueOf(3.0).multiply(a).multiply(m).multiply(m))
        val numeratorD = d.multiply(h3)
            .subtract(c.multiply(m).multiply(h2))
            .add(b.multiply(m).multiply(m).multiply(h))
            .subtract(a.multiply(m).multiply(m).multiply(m))

        fun convert(numerator: BigDecimal, denominator: BigDecimal): CoefficientConversion {
            if (numerator.signum() == 0) return CoefficientConversion.Success(0.0)
            return convertCoefficient(y.multiply(numerator).divide(denominator, conversionContext))
        }

        return ConvertedCoefficients(
            a = convert(numeratorA, x3.multiply(h3)),
            b = convert(numeratorB, x2.multiply(h3)),
            c = convert(numeratorC, x.multiply(h3)),
            d = convert(numeratorD, h3),
        )
    }

    private fun convertCoefficient(value: BigDecimal): CoefficientConversion {
        val converted = value.toDouble()
        return when {
            !converted.isFinite() -> CoefficientConversion.Overflow
            converted == 0.0 && value.signum() != 0 -> CoefficientConversion.Underflow
            else -> CoefficientConversion.Success(converted)
        }
    }

    private fun canonicalZero(value: Double): Double =
        if (value == 0.0) 0.0 else value

    private fun precisionFailure(message: String) =
        CubicFunctionFitResult.Failure(
            FunctionFitError(FunctionFitErrorCode.NUMERIC_PRECISION_LOSS, message),
        )

    private fun nonFiniteFailure(message: String) =
        CubicFunctionFitResult.Failure(
            FunctionFitError(FunctionFitErrorCode.NON_FINITE_RESULT, message),
        )

    private sealed interface QrResult {
        data class Success(val coefficients: DoubleArray) : QrResult
        data class PrecisionFailure(val message: String) : QrResult
    }

    private sealed interface CoefficientConversion {
        data class Success(val value: Double) : CoefficientConversion
        object Overflow : CoefficientConversion
        object Underflow : CoefficientConversion
    }

    private data class ConvertedCoefficients(
        val a: CoefficientConversion,
        val b: CoefficientConversion,
        val c: CoefficientConversion,
        val d: CoefficientConversion,
    )

    private const val MINIMUM_POINT_COUNT = 4
    private const val COLUMN_COUNT = 4
    private const val RANK_RELATIVE_TOLERANCE = 1e-13
    private const val EXTREME_SCALE_THRESHOLD = 1e150
}
