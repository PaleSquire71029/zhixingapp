package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.CoordinatePointSet
import ci.us.zhixing.app.domain.math.model.CoordinatePointSetValidationResult
import ci.us.zhixing.app.domain.math.model.FunctionFitError
import ci.us.zhixing.app.domain.math.model.FunctionFitErrorCode
import ci.us.zhixing.app.domain.math.model.LinearFunction
import ci.us.zhixing.app.domain.math.model.LinearFunctionFitResult
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs

/**
 * Fits y = mx + b.
 *
 * Two distinct-x points uniquely determine an interpolating line. For three
 * or more points, the same least-squares solution minimizes the sum of squared
 * vertical residuals. Coordinate normalization and Welford's online centered
 * moments keep the accumulation stable; BigDecimal is used only for the final
 * scale conversion so a temporary scale ratio does not overflow or underflow.
 */
internal object LinearFunctionFitter {
    private val conversionContext = MathContext.DECIMAL128

    fun fit(points: List<CoordinatePoint>): LinearFunctionFitResult {
        val validation = CoordinatePointSetValidator.validate(
            points = points,
            requiredPointCount = MINIMUM_POINT_COUNT,
        )
        val pointSet = when (validation) {
            is CoordinatePointSetValidationResult.Valid -> validation.pointSet
            else -> return LinearFunctionFitResult.InvalidInput(validation)
        }
        return fit(pointSet)
    }

    fun fit(pointSet: CoordinatePointSet): LinearFunctionFitResult {
        if (pointSet.size < MINIMUM_POINT_COUNT) {
            return LinearFunctionFitResult.InvalidInput(
                CoordinatePointSetValidationResult.InsufficientPointCount(
                    actualPointCount = pointSet.size,
                    requiredPointCount = MINIMUM_POINT_COUNT,
                ),
            )
        }
        // Recheck invariants at this boundary because the model constructor is
        // internal and deliberately does not perform validation itself.
        val validation = CoordinatePointSetValidator.validate(
            points = pointSet.points,
            requiredPointCount = MINIMUM_POINT_COUNT,
        )
        if (validation !is CoordinatePointSetValidationResult.Valid) {
            return LinearFunctionFitResult.InvalidInput(validation)
        }
        return fitValidated(validation.pointSet)
    }

    private fun fitValidated(pointSet: CoordinatePointSet): LinearFunctionFitResult {
        val points = pointSet.points
        val xScale = points.maxOf { abs(it.x) }
        val yScale = points.maxOf { abs(it.y) }

        if (xScale == 0.0) {
            return precisionFailure("横坐标缩放后无法区分不同点")
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

        // Welford's online algorithm for centered variance and covariance.
        var meanX = 0.0
        var meanY = 0.0
        var sumXX = 0.0
        var sumXY = 0.0

        for (index in normalizedX.indices) {
            val x = normalizedX[index]
            val y = normalizedY[index]
            val count = (index + 1).toDouble()
            val deltaX = x - meanX
            val deltaY = y - meanY
            meanX += deltaX / count
            meanY += deltaY / count
            sumXX += deltaX * (x - meanX)
            sumXY += deltaX * (y - meanY)

            if (!meanX.isFinite() || !meanY.isFinite() ||
                !sumXX.isFinite() || !sumXY.isFinite()
            ) {
                return precisionFailure("最小二乘累积超出可靠数值范围")
            }
        }

        if (sumXX <= 0.0) {
            return precisionFailure("横坐标方差在 Double 精度下不可分辨")
        }

        val normalizedSlope = sumXY / sumXX
        val normalizedIntercept = meanY - normalizedSlope * meanX
        if (!normalizedSlope.isFinite() || !normalizedIntercept.isFinite()) {
            return precisionFailure("归一化拟合系数超出可靠数值范围")
        }

        val slope = when (
            val conversion = scaledSlopeToDouble(
                yScale = yScale,
                normalizedSlope = normalizedSlope,
                xScale = xScale,
            )
        ) {
            is CoefficientConversion.Success -> conversion.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("拟合斜率超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("拟合斜率下溢，无法可靠表示")
        }

        val intercept = when (
            val conversion = scaledProductToDouble(
                scale = yScale,
                normalizedValue = normalizedIntercept,
            )
        ) {
            is CoefficientConversion.Success -> conversion.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("拟合截距超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("拟合截距下溢，无法可靠表示")
        }

        if (!slope.isFinite() || !intercept.isFinite()) {
            return nonFiniteFailure("拟合系数不是有限数")
        }

        return LinearFunctionFitResult.Success(
            function = LinearFunction(slope = slope, intercept = intercept),
            pointCount = points.size,
        )
    }

    private fun scaledSlopeToDouble(
        yScale: Double,
        normalizedSlope: Double,
        xScale: Double,
    ): CoefficientConversion {
        if (normalizedSlope == 0.0 || yScale == 0.0) {
            return CoefficientConversion.Success(0.0)
        }
        val exactScaled = BigDecimal.valueOf(yScale)
            .multiply(BigDecimal.valueOf(normalizedSlope))
            .divide(BigDecimal.valueOf(xScale), conversionContext)
        return convertCoefficient(exactScaled)
    }

    private fun scaledProductToDouble(
        scale: Double,
        normalizedValue: Double,
    ): CoefficientConversion {
        if (normalizedValue == 0.0 || scale == 0.0) {
            return CoefficientConversion.Success(0.0)
        }
        val exactScaled = BigDecimal.valueOf(scale)
            .multiply(BigDecimal.valueOf(normalizedValue))
        return convertCoefficient(exactScaled)
    }

    private fun convertCoefficient(value: BigDecimal): CoefficientConversion {
        val converted = value.toDouble()
        return when {
            !converted.isFinite() -> CoefficientConversion.Overflow
            converted == 0.0 && value.signum() != 0 -> CoefficientConversion.Underflow
            else -> CoefficientConversion.Success(converted)
        }
    }

    private sealed interface CoefficientConversion {
        data class Success(val value: Double) : CoefficientConversion
        object Overflow : CoefficientConversion
        object Underflow : CoefficientConversion
    }

    private fun canonicalZero(value: Double): Double =
        if (value == 0.0) 0.0 else value

    private fun precisionFailure(message: String) =
        LinearFunctionFitResult.Failure(
            FunctionFitError(
                FunctionFitErrorCode.NUMERIC_PRECISION_LOSS,
                message,
            ),
        )

    private fun nonFiniteFailure(message: String) =
        LinearFunctionFitResult.Failure(
            FunctionFitError(
                FunctionFitErrorCode.NON_FINITE_RESULT,
                message,
            ),
        )

    private const val MINIMUM_POINT_COUNT = 2
}
