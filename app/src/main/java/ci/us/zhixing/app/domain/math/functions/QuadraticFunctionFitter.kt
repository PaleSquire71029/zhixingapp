package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.CoordinatePointSet
import ci.us.zhixing.app.domain.math.model.CoordinatePointSetValidationResult
import ci.us.zhixing.app.domain.math.model.FunctionFitError
import ci.us.zhixing.app.domain.math.model.FunctionFitErrorCode
import ci.us.zhixing.app.domain.math.model.QuadraticFunction
import ci.us.zhixing.app.domain.math.model.QuadraticFunctionFitResult
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs

/**
 * Fits y = ax^2 + bx + c.
 *
 * Three points with distinct x coordinates determine an interpolating
 * quadratic. Larger point sets use ordinary least squares. The normalized
 * Vandermonde system is solved with column-pivoted Householder QR rather than
 * normal equations, avoiding the condition-number squaring of X'X.
 */
internal object QuadraticFunctionFitter {
    private val conversionContext = MathContext.DECIMAL128

    fun fit(points: List<CoordinatePoint>): QuadraticFunctionFitResult {
        val validation = CoordinatePointSetValidator.validate(
            points = points,
            requiredPointCount = MINIMUM_POINT_COUNT,
        )
        val pointSet = when (validation) {
            is CoordinatePointSetValidationResult.Valid -> validation.pointSet
            else -> return QuadraticFunctionFitResult.InvalidInput(validation)
        }
        return fit(pointSet)
    }

    fun fit(pointSet: CoordinatePointSet): QuadraticFunctionFitResult {
        // Recheck invariants at this API boundary rather than trusting that an
        // internal point-set instance was necessarily produced by the validator.
        val validation = CoordinatePointSetValidator.validate(
            points = pointSet.points,
            requiredPointCount = MINIMUM_POINT_COUNT,
        )
        if (validation !is CoordinatePointSetValidationResult.Valid) {
            return QuadraticFunctionFitResult.InvalidInput(validation)
        }
        return fitValidated(validation.pointSet)
    }

    private fun fitValidated(pointSet: CoordinatePointSet): QuadraticFunctionFitResult {
        val points = pointSet.points
        val xScale = points.maxOf { abs(it.x) }
        val yScale = points.maxOf { abs(it.y) }

        if (xScale == 0.0) {
            return precisionFailure("横坐标缩放后无法区分不同点")
        }

        // Very large / tiny abscissas can make an otherwise exact straight
        // line appear to have a tiny spurious quadratic term after QR. For
        // these extreme scales, detect exact collinearity using the exact
        // decimal expansions of the input Doubles before normalizing.
        if (xScale > EXTREME_SCALE_THRESHOLD || xScale < 1.0 / EXTREME_SCALE_THRESHOLD) {
            exactLinearFit(points)?.let { return it }
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

        // Center and scale normalized x to [-1, 1]. Halving before addition
        // and subtraction avoids overflow for opposite-sign extreme values.
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

        val coefficients = when (
            val solution = solveWithPivotedHouseholderQr(normalizedT, normalizedY)
        ) {
            is QrResult.Success -> solution.coefficients
            is QrResult.PrecisionFailure -> return precisionFailure(solution.message)
        }

        val normalizedA = coefficients[0]
        val normalizedB = coefficients[1]
        val normalizedC = coefficients[2]
        if (coefficients.any { !it.isFinite() }) {
            return precisionFailure("归一化二次拟合系数超出可靠数值范围")
        }

        val converted = convertCoefficients(
            xScale = xScale,
            yScale = yScale,
            midpoint = midpoint,
            halfRange = halfRange,
            normalizedA = normalizedA,
            normalizedB = normalizedB,
            normalizedC = normalizedC,
        )
        val a = when (val conversion = converted.a) {
            is CoefficientConversion.Success -> conversion.value
            CoefficientConversion.Overflow -> return nonFiniteFailure("二次项系数超出 Double 可表示范围")
            CoefficientConversion.Underflow -> return precisionFailure("二次项系数下溢，无法可靠表示")
        }
        val b = when (val conversion = converted.b) {
            is CoefficientConversion.Success -> conversion.value
            CoefficientConversion.Overflow -> return nonFiniteFailure("一次项系数超出 Double 可表示范围")
            CoefficientConversion.Underflow -> return precisionFailure("一次项系数下溢，无法可靠表示")
        }
        val c = when (val conversion = converted.c) {
            is CoefficientConversion.Success -> conversion.value
            CoefficientConversion.Overflow -> return nonFiniteFailure("常数项系数超出 Double 可表示范围")
            CoefficientConversion.Underflow -> return precisionFailure("常数项系数下溢，无法可靠表示")
        }

        if (!a.isFinite() || !b.isFinite() || !c.isFinite()) {
            return nonFiniteFailure("二次函数拟合系数不是有限数")
        }

        return QuadraticFunctionFitResult.Success(
            function = QuadraticFunction(
                quadraticCoefficient = a,
                linearCoefficient = b,
                intercept = c,
            ),
            pointCount = points.size,
        )
    }

    /**
     * Returns an exact linear fit only when every supplied Double point lies
     * on the same mathematical line. BigDecimal(Double) preserves each input
     * binary value exactly; cross multiplication avoids a rounded slope in
     * the collinearity decision.
     */
    private fun exactLinearFit(
        points: List<CoordinatePoint>,
    ): QuadraticFunctionFitResult? {
        val first = points[0]
        val second = points[1]
        val x0 = BigDecimal(first.x)
        val y0 = BigDecimal(first.y)
        val deltaX = BigDecimal(second.x).subtract(x0)
        val deltaY = BigDecimal(second.y).subtract(y0)
        if (deltaX.signum() == 0) return null

        for (point in points.drop(2)) {
            val left = BigDecimal(point.y).subtract(y0).multiply(deltaX)
            val right = deltaY.multiply(BigDecimal(point.x).subtract(x0))
            if (left.compareTo(right) != 0) return null
        }

        val slope = deltaY.divide(deltaX, conversionContext)
        // Form the intercept numerator before division. Multiplying the
        // rounded DECIMAL128 slope by a huge x0 and then subtracting y0 can
        // destroy all low-order digits through catastrophic cancellation.
        val interceptNumerator = y0.multiply(deltaX).subtract(deltaY.multiply(x0))
        val intercept = interceptNumerator.divide(deltaX, conversionContext)
        val convertedSlope = convertCoefficient(slope)
        val convertedIntercept = convertCoefficient(intercept)
        val b = when (convertedSlope) {
            is CoefficientConversion.Success -> convertedSlope.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("拟合斜率超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("拟合斜率下溢，无法可靠表示")
        }
        val c = when (convertedIntercept) {
            is CoefficientConversion.Success -> convertedIntercept.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("拟合截距超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("拟合截距下溢，无法可靠表示")
        }
        return QuadraticFunctionFitResult.Success(
            function = QuadraticFunction(
                quadraticCoefficient = 0.0,
                linearCoefficient = b,
                intercept = c,
            ),
            pointCount = points.size,
        )
    }

    /**
     * Column-pivoted Householder QR for the normalized design matrix
     * [t^2, t, 1]. Pivoting chooses the largest remaining column norm;
     * a relative diagonal threshold rejects numerically rank-deficient data.
     */
    private fun solveWithPivotedHouseholderQr(
        tValues: List<Double>,
        yValues: List<Double>,
    ): QrResult {
        val rowCount = tValues.size
        val matrix = Array(rowCount) { row ->
            val t = tValues[row]
            doubleArrayOf(t * t, t, 1.0)
        }
        val right = yValues.toDoubleArray()
        val permutation = intArrayOf(0, 1, 2)
        val initialNorm = (0..2).maxOf { columnNorm(matrix, 0, it) }
        if (!initialNorm.isFinite() || initialNorm == 0.0) {
            return QrResult.PrecisionFailure("二次拟合矩阵没有有效列")
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
                return QrResult.PrecisionFailure("坐标点分布退化，二次拟合矩阵数值秩不足")
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
            reflector.forEach { value -> reflectorNormSquared += value * value }
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
                return QrResult.PrecisionFailure("二次拟合矩阵在回代时数值秩不足")
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
     * Convert y = yScale*(A*t^2+B*t+C), t=(x/xScale-midpoint)/halfRange,
     * into coefficients in the original x-coordinate using DECIMAL128
     * intermediate arithmetic so scale products cannot overflow first.
     */
    private fun convertCoefficients(
        xScale: Double,
        yScale: Double,
        midpoint: Double,
        halfRange: Double,
        normalizedA: Double,
        normalizedB: Double,
        normalizedC: Double,
    ): ConvertedCoefficients {
        if (yScale == 0.0) {
            val zero = CoefficientConversion.Success(0.0)
            return ConvertedCoefficients(zero, zero, zero)
        }

        val x = BigDecimal.valueOf(xScale)
        val y = BigDecimal.valueOf(yScale)
        val m = BigDecimal.valueOf(midpoint)
        val h = BigDecimal.valueOf(halfRange)
        val hSquared = h.multiply(h, conversionContext)
        val xSquared = x.multiply(x, conversionContext)
        val aN = BigDecimal.valueOf(normalizedA)
        val bN = BigDecimal.valueOf(normalizedB)
        val cN = BigDecimal.valueOf(normalizedC)

        val a = if (normalizedA == 0.0) {
            CoefficientConversion.Success(0.0)
        } else {
            val numerator = y.multiply(aN, conversionContext)
            val denominator = xSquared.multiply(hSquared, conversionContext)
            convertCoefficient(numerator.divide(denominator, conversionContext))
        }

        val bNumeratorFactor = bN.multiply(h, conversionContext)
            .subtract(
                BigDecimal.valueOf(2.0).multiply(aN, conversionContext)
                    .multiply(m, conversionContext),
                conversionContext,
            )
        val b = if (bNumeratorFactor.signum() == 0) {
            CoefficientConversion.Success(0.0)
        } else {
            val numerator = y.multiply(bNumeratorFactor, conversionContext)
            val denominator = x.multiply(hSquared, conversionContext)
            convertCoefficient(numerator.divide(denominator, conversionContext))
        }

        val cNumeratorFactor = cN.multiply(hSquared, conversionContext)
            .subtract(
                bN.multiply(m, conversionContext).multiply(h, conversionContext),
                conversionContext,
            )
            .add(
                aN.multiply(m, conversionContext).multiply(m, conversionContext),
                conversionContext,
            )
        val c = if (cNumeratorFactor.signum() == 0) {
            CoefficientConversion.Success(0.0)
        } else {
            val numerator = y.multiply(cNumeratorFactor, conversionContext)
            convertCoefficient(numerator.divide(hSquared, conversionContext))
        }
        return ConvertedCoefficients(a, b, c)
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
        QuadraticFunctionFitResult.Failure(
            FunctionFitError(FunctionFitErrorCode.NUMERIC_PRECISION_LOSS, message),
        )

    private fun nonFiniteFailure(message: String) =
        QuadraticFunctionFitResult.Failure(
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
    )

    private const val MINIMUM_POINT_COUNT = 3
    private const val COLUMN_COUNT = 3
    private const val RANK_RELATIVE_TOLERANCE = 1e-13
    private const val EXTREME_SCALE_THRESHOLD = 1e150
}
