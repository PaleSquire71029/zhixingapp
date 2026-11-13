package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.CoordinatePointSet
import ci.us.zhixing.app.domain.math.model.CoordinatePointSetValidationResult
import ci.us.zhixing.app.domain.math.model.FunctionFitError
import ci.us.zhixing.app.domain.math.model.FunctionFitErrorCode
import ci.us.zhixing.app.domain.math.model.InverseProportionFitInputError
import ci.us.zhixing.app.domain.math.model.InverseProportionFunction
import ci.us.zhixing.app.domain.math.model.InverseProportionFunctionFitResult
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs

/**
 * Fits y = k / x.
 *
 * One point with x != 0 determines k uniquely. For multiple points, ordinary
 * least squares minimizes vertical residuals: sum((yi - k / xi)^2).
 *
 * Let sX be the smallest absolute x and sY the largest absolute y. The
 * normalized predictor u = sX / x is bounded by 1, and v = y / sY is bounded
 * by 1. The least-squares coefficient q in v ~= q * u is
 * sum(u*v) / sum(u*u), and k = sX * sY * q. Compensated sums and BigDecimal
 * scale restoration avoid avoidable overflow and underflow.
 */
internal object InverseProportionFunctionFitter {
    private val conversionContext = MathContext.DECIMAL128

    fun fit(points: List<CoordinatePoint>): InverseProportionFunctionFitResult {
        val validation = CoordinatePointSetValidator.validate(
            points = points,
            requiredPointCount = MINIMUM_POINT_COUNT,
        )
        val pointSet = when (validation) {
            is CoordinatePointSetValidationResult.Valid -> validation.pointSet
            else -> return invalidPointSet(validation)
        }
        return fit(pointSet)
    }

    fun fit(pointSet: CoordinatePointSet): InverseProportionFunctionFitResult {
        // Revalidate at this boundary so an internally constructed point set
        // cannot bypass the same checks as a list input.
        val validation = CoordinatePointSetValidator.validate(
            points = pointSet.points,
            requiredPointCount = MINIMUM_POINT_COUNT,
        )
        val validatedSet = when (validation) {
            is CoordinatePointSetValidationResult.Valid -> validation.pointSet
            else -> return invalidPointSet(validation)
        }

        val zeroXIndex = validatedSet.points.indexOfFirst { it.x == 0.0 }
        if (zeroXIndex >= 0) {
            val point = validatedSet.points[zeroXIndex]
            return InverseProportionFunctionFitResult.InvalidInput(
                InverseProportionFitInputError.ZeroXCoordinate(
                    pointIndex = zeroXIndex,
                    x = point.x,
                ),
            )
        }

        return fitValidated(validatedSet)
    }

    private fun fitValidated(
        pointSet: CoordinatePointSet,
    ): InverseProportionFunctionFitResult {
        val points = pointSet.points
        val xScale = points.minOf { abs(it.x) }
        val yScale = points.maxOf { abs(it.y) }

        // Every x has already been checked as finite and nonzero.
        if (xScale <= 0.0 || !xScale.isFinite()) {
            return precisionFailure("横坐标无法建立可靠的反比例缩放")
        }

        if (yScale == 0.0) {
            return InverseProportionFunctionFitResult.Success(
                function = InverseProportionFunction(constant = 0.0),
                pointCount = points.size,
            )
        }

        val seenInverseX = HashMap<Double, Double>(points.size)
        val seenY = HashMap<Double, Double>(points.size)
        val numerator = CompensatedSum()
        val denominator = CompensatedSum()

        points.forEach { point ->
            val inverseX = canonicalZero(xScale / point.x)
            val normalizedY = canonicalZero(point.y / yScale)

            if (!inverseX.isFinite() || !normalizedY.isFinite()) {
                return nonFiniteFailure("反比例拟合归一化产生了非有限值")
            }
            if (inverseX == 0.0) {
                return precisionFailure("倒数横坐标归一化时发生下溢")
            }
            if (point.y != 0.0 && normalizedY == 0.0) {
                return precisionFailure("纵坐标归一化时发生下溢")
            }

            val previousInverseX = seenInverseX.putIfAbsent(inverseX, point.x)
            if (previousInverseX != null && previousInverseX != point.x) {
                return precisionFailure("不同横坐标的倒数在归一化后无法区分")
            }
            val previousY = seenY.putIfAbsent(normalizedY, point.y)
            if (previousY != null && previousY != point.y) {
                return precisionFailure("不同纵坐标在归一化后无法区分")
            }

            numerator.add(inverseX * normalizedY)
            denominator.add(inverseX * inverseX)
        }

        val sumProducts = numerator.value
        val sumSquares = denominator.value
        if (!sumProducts.isFinite() || !sumSquares.isFinite() || sumSquares <= 0.0) {
            return precisionFailure("反比例最小二乘累积超出可靠数值范围")
        }

        val normalizedConstant = sumProducts / sumSquares
        if (!normalizedConstant.isFinite()) {
            return precisionFailure("归一化反比例系数超出可靠数值范围")
        }

        val constant = when (
            val conversion = scaledConstantToDouble(
                xScale = xScale,
                yScale = yScale,
                normalizedConstant = normalizedConstant,
            )
        ) {
            is CoefficientConversion.Success -> conversion.value
            CoefficientConversion.Overflow ->
                return nonFiniteFailure("反比例系数超出 Double 可表示范围")
            CoefficientConversion.Underflow ->
                return precisionFailure("反比例系数下溢，无法可靠表示")
        }

        if (!constant.isFinite()) {
            return nonFiniteFailure("反比例系数不是有限数")
        }

        return InverseProportionFunctionFitResult.Success(
            function = InverseProportionFunction(constant = constant),
            pointCount = points.size,
        )
    }

    private fun scaledConstantToDouble(
        xScale: Double,
        yScale: Double,
        normalizedConstant: Double,
    ): CoefficientConversion {
        if (normalizedConstant == 0.0 || yScale == 0.0) {
            return CoefficientConversion.Success(0.0)
        }
        val scaled = BigDecimal.valueOf(xScale)
            .multiply(BigDecimal.valueOf(yScale), conversionContext)
            .multiply(BigDecimal.valueOf(normalizedConstant), conversionContext)
        val converted = scaled.toDouble()
        return when {
            !converted.isFinite() -> CoefficientConversion.Overflow
            converted == 0.0 && scaled.signum() != 0 -> CoefficientConversion.Underflow
            else -> CoefficientConversion.Success(canonicalZero(converted))
        }
    }

    private fun invalidPointSet(
        reason: CoordinatePointSetValidationResult,
    ) = InverseProportionFunctionFitResult.InvalidInput(
        InverseProportionFitInputError.PointSetValidation(reason),
    )

    private class CompensatedSum {
        private var sum = 0.0
        private var compensation = 0.0

        val value: Double
            get() = sum

        fun add(value: Double) {
            val adjusted = value - compensation
            val next = sum + adjusted
            compensation = (next - sum) - adjusted
            sum = next
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
        InverseProportionFunctionFitResult.Failure(
            FunctionFitError(
                FunctionFitErrorCode.NUMERIC_PRECISION_LOSS,
                message,
            ),
        )

    private fun nonFiniteFailure(message: String) =
        InverseProportionFunctionFitResult.Failure(
            FunctionFitError(
                FunctionFitErrorCode.NON_FINITE_RESULT,
                message,
            ),
        )

    private const val MINIMUM_POINT_COUNT = 1
}
