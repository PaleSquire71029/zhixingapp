package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.PeriodicSolutionFamily
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionInterval
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionIntervalError
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionIntervalErrorCode
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionIntervalResult
import java.math.BigDecimal
import java.math.BigInteger
import java.util.TreeMap

/**
 * Materializes one or more infinite periodic solution families inside a
 * finite interval. Integer period indices are calculated with BigDecimal /
 * BigInteger so large offsets do not silently lose an integer during ceil/floor.
 */
internal object PeriodicSolutionIntervalFilter {
    fun filter(
        families: List<PeriodicSolutionFamily>,
        interval: PeriodicSolutionInterval,
        maxSolutions: Int = DEFAULT_MAX_SOLUTIONS,
    ): PeriodicSolutionIntervalResult {
        if (!interval.lowerBound.isFinite() || !interval.upperBound.isFinite()) {
            return failure(
                PeriodicSolutionIntervalErrorCode.NON_FINITE_INPUT,
                "区间端点必须是有限数",
            )
        }
        if (interval.lowerBound > interval.upperBound) {
            return failure(
                PeriodicSolutionIntervalErrorCode.INVALID_INTERVAL,
                "区间下界不能大于上界",
            )
        }
        if (maxSolutions <= 0) {
            return failure(
                PeriodicSolutionIntervalErrorCode.INVALID_MAX_SOLUTIONS,
                "解数量上限必须是正整数",
            )
        }
        if (families.isEmpty()) return PeriodicSolutionIntervalResult.NoSolutions

        if (interval.lowerBound == interval.upperBound &&
            !(interval.includeLowerBound && interval.includeUpperBound)
        ) {
            return PeriodicSolutionIntervalResult.NoSolutions
        }

        val lower = BigDecimal.valueOf(interval.lowerBound)
        val upper = BigDecimal.valueOf(interval.upperBound)
        // Exact mathematical solutions are keyed separately from their Double
        // representation. This lets us distinguish genuine duplicate families
        // from two distinct roots that collapse to the same Double.
        val exactByValue = mutableMapOf<Double, BigDecimal>()

        for (family in families.distinct()) {
            if (!family.firstSolution.isFinite() ||
                !family.period.isFinite() ||
                family.period <= 0.0 ||
                family.firstSolution < 0.0 ||
                family.firstSolution >= family.period
            ) {
                return failure(
                    PeriodicSolutionIntervalErrorCode.INVALID_FAMILY,
                    "周期解族必须具有有限基准值、正周期，且基准值位于 [0, period)",
                )
            }

            val base = BigDecimal.valueOf(family.firstSolution)
            val period = BigDecimal.valueOf(family.period)
            val lowerOffset = lower.subtract(base)
            val upperOffset = upper.subtract(base)

            val firstIndex = if (interval.includeLowerBound) {
                ceilQuotient(lowerOffset, period)
            } else {
                floorQuotient(lowerOffset, period).add(BigInteger.ONE)
            }
            val lastIndex = if (interval.includeUpperBound) {
                floorQuotient(upperOffset, period)
            } else {
                ceilQuotient(upperOffset, period).subtract(BigInteger.ONE)
            }

            if (firstIndex > lastIndex) continue

            val count = lastIndex.subtract(firstIndex).add(BigInteger.ONE)
            if (count > BigInteger.valueOf(maxSolutions.toLong())) {
                return failure(
                    PeriodicSolutionIntervalErrorCode.TOO_MANY_SOLUTIONS,
                    "区间内解的数量超过上限 $maxSolutions",
                )
            }

            var index = firstIndex
            while (index <= lastIndex) {
                val exactValue = base.add(period.multiply(BigDecimal(index)))
                val value = exactValue.toDouble()
                if (!value.isFinite()) {
                    return failure(
                        PeriodicSolutionIntervalErrorCode.NON_FINITE_RESULT,
                        "区间内的周期解超出 Double 可表示范围",
                    )
                }

                val normalizedValue = if (value == 0.0) 0.0 else value
                if (!isInside(normalizedValue, interval)) {
                    return failure(
                        PeriodicSolutionIntervalErrorCode.NUMERIC_PRECISION_LOSS,
                        "周期解转换为 Double 后越过区间边界，无法可靠判断端点",
                    )
                }

                val previousExact = exactByValue[normalizedValue]
                if (previousExact == null) {
                    exactByValue[normalizedValue] = exactValue
                    if (exactByValue.size > maxSolutions) {
                        return failure(
                            PeriodicSolutionIntervalErrorCode.TOO_MANY_SOLUTIONS,
                            "区间内解的数量超过上限 $maxSolutions",
                        )
                    }
                } else if (previousExact.compareTo(exactValue) != 0) {
                    return failure(
                        PeriodicSolutionIntervalErrorCode.NUMERIC_PRECISION_LOSS,
                        "两个不同的周期解舍入为同一个 Double，无法可靠区分",
                    )
                }

                index = index.add(BigInteger.ONE)
            }
        }

        if (exactByValue.isEmpty()) return PeriodicSolutionIntervalResult.NoSolutions
        val sorted = TreeMap<Double, BigDecimal>()
        exactByValue.forEach { (value, exact) -> sorted[value] = exact }
        return PeriodicSolutionIntervalResult.FiniteSolutions(sorted.keys.toList())
    }

    private fun floorQuotient(
        numerator: BigDecimal,
        positiveDenominator: BigDecimal,
    ): BigInteger {
        val truncated = numerator.divideToIntegralValue(positiveDenominator).toBigIntegerExact()
        val remainder = numerator.remainder(positiveDenominator)
        return if (numerator.signum() < 0 && remainder.signum() != 0) {
            truncated.subtract(BigInteger.ONE)
        } else {
            truncated
        }
    }

    private fun ceilQuotient(
        numerator: BigDecimal,
        positiveDenominator: BigDecimal,
    ): BigInteger {
        val truncated = numerator.divideToIntegralValue(positiveDenominator).toBigIntegerExact()
        val remainder = numerator.remainder(positiveDenominator)
        return if (numerator.signum() > 0 && remainder.signum() != 0) {
            truncated.add(BigInteger.ONE)
        } else {
            truncated
        }
    }

    private fun isInside(
        value: Double,
        interval: PeriodicSolutionInterval,
    ): Boolean {
        val aboveLower = value > interval.lowerBound ||
            (interval.includeLowerBound && value == interval.lowerBound)
        val belowUpper = value < interval.upperBound ||
            (interval.includeUpperBound && value == interval.upperBound)
        return aboveLower && belowUpper
    }

    private fun failure(
        code: PeriodicSolutionIntervalErrorCode,
        message: String,
    ) = PeriodicSolutionIntervalResult.Failure(
        PeriodicSolutionIntervalError(code, message),
    )

    private const val DEFAULT_MAX_SOLUTIONS = 1_000
}
