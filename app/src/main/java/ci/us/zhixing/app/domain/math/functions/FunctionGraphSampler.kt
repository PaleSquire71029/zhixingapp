package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.FunctionEvaluationResult
import ci.us.zhixing.app.domain.math.model.FunctionGraphSamplingError
import ci.us.zhixing.app.domain.math.model.FunctionGraphSamplingErrorCode
import ci.us.zhixing.app.domain.math.model.FunctionGraphSamplingInputError
import ci.us.zhixing.app.domain.math.model.FunctionGraphSamplingRequest
import ci.us.zhixing.app.domain.math.model.FunctionGraphSamplingResult
import ci.us.zhixing.app.domain.math.model.FunctionGraphSegment
import ci.us.zhixing.app.domain.math.model.FittedFunction

/**
 * Samples each connected piece of a function's domain into a separate polyline.
 *
 * Open domain boundaries are inset by a fraction of the requested local sample
 * spacing. This avoids evaluating immediately beside a vertical asymptote,
 * where the function can exceed Double even though ordinary visible samples
 * remain representable. It also guarantees that samples never include an
 * excluded x value.
 */
internal object FunctionGraphSampler {
    fun sample(
        function: FittedFunction,
        request: FunctionGraphSamplingRequest,
    ): FunctionGraphSamplingResult {
        validate(request)?.let {
            return FunctionGraphSamplingResult.InvalidInput(it)
        }

        val domain = FunctionDomainResolver.resolve(function)
        val cuts = domain.excludedXValues
            .filter { it >= request.minX && it <= request.maxX }
            .sorted()

        val intervals = buildIntervals(
            minX = request.minX,
            maxX = request.maxX,
            cuts = cuts,
        )
        if (intervals.isEmpty()) {
            return FunctionGraphSamplingResult.Failure(
                FunctionGraphSamplingError(
                    FunctionGraphSamplingErrorCode.NUMERIC_PRECISION_LOSS,
                ),
            )
        }

        if (intervals.size > MAX_SEGMENTS ||
            intervals.size.toLong() * request.samplesPerSegment > MAX_TOTAL_SAMPLES
        ) {
            return FunctionGraphSamplingResult.Failure(
                FunctionGraphSamplingError(
                    FunctionGraphSamplingErrorCode.SAMPLE_BUDGET_EXCEEDED,
                ),
            )
        }

        val segments = ArrayList<FunctionGraphSegment>(intervals.size)
        for (interval in intervals) {
            val safeRange = createSafeRange(
                interval = interval,
                samplesPerSegment = request.samplesPerSegment,
            ) ?: return precisionFailure()

            val points = ArrayList<CoordinatePoint>(request.samplesPerSegment)
            var previousX: Double? = null

            for (index in 0 until request.samplesPerSegment) {
                val fraction = index.toDouble() / (request.samplesPerSegment - 1).toDouble()
                val x = interpolate(safeRange.first, safeRange.second, fraction)
                if (!x.isFinite()) {
                    return precisionFailure(x)
                }
                if (previousX != null && x <= previousX) {
                    return precisionFailure(x)
                }

                val y = when (val evaluation = FunctionEvaluator.evaluate(function, x)) {
                    is FunctionEvaluationResult.Success -> evaluation.y
                    is FunctionEvaluationResult.InvalidInput,
                    is FunctionEvaluationResult.OutsideDomain ->
                        return precisionFailure(x)

                    is FunctionEvaluationResult.Failure ->
                        return FunctionGraphSamplingResult.Failure(
                            FunctionGraphSamplingError(
                                FunctionGraphSamplingErrorCode.NON_FINITE_FUNCTION_VALUE,
                                evaluation.x,
                            ),
                        )
                }

                points += CoordinatePoint(x, y)
                previousX = x
            }

            segments += FunctionGraphSegment(points)
        }

        return FunctionGraphSamplingResult.Success(
            segments = segments,
            excludedXValues = domain.excludedXValues,
        )
    }

    private fun validate(
        request: FunctionGraphSamplingRequest,
    ): FunctionGraphSamplingInputError? {
        if (!request.minX.isFinite()) {
            return FunctionGraphSamplingInputError.NON_FINITE_MIN_X
        }
        if (!request.maxX.isFinite()) {
            return FunctionGraphSamplingInputError.NON_FINITE_MAX_X
        }
        if (request.minX >= request.maxX) {
            return FunctionGraphSamplingInputError.MIN_X_NOT_LESS_THAN_MAX_X
        }
        if (request.samplesPerSegment < MIN_SAMPLES_PER_SEGMENT) {
            return FunctionGraphSamplingInputError.SAMPLE_COUNT_BELOW_MINIMUM
        }
        if (request.samplesPerSegment > MAX_SAMPLES_PER_SEGMENT) {
            return FunctionGraphSamplingInputError.SAMPLE_COUNT_ABOVE_MAXIMUM
        }
        return null
    }

    private fun buildIntervals(
        minX: Double,
        maxX: Double,
        cuts: List<Double>,
    ): List<SamplingInterval> {
        if (cuts.isEmpty()) {
            return listOf(
                SamplingInterval(
                    start = minX,
                    end = maxX,
                    startOpen = false,
                    endOpen = false,
                ),
            )
        }

        val intervals = ArrayList<SamplingInterval>(cuts.size + 1)
        var currentStart = minX
        var currentStartOpen = cuts.firstOrNull() == minX

        cuts.forEach { cut ->
            if (cut > currentStart) {
                intervals += SamplingInterval(
                    start = currentStart,
                    end = cut,
                    startOpen = currentStartOpen,
                    endOpen = true,
                )
            }
            currentStart = cut
            currentStartOpen = true
        }

        if (currentStart < maxX) {
            intervals += SamplingInterval(
                start = currentStart,
                end = maxX,
                startOpen = currentStartOpen,
                endOpen = cuts.lastOrNull() == maxX,
            )
        }

        return intervals
    }

    private fun createSafeRange(
        interval: SamplingInterval,
        samplesPerSegment: Int,
    ): Pair<Double, Double>? {
        if (interval.start >= interval.end) return null

        val insetFraction = if (interval.startOpen && interval.endOpen) {
            minOf(0.25, 0.5 / (samplesPerSegment - 1).toDouble())
        } else {
            0.5 / (samplesPerSegment - 1).toDouble()
        }

        val safeStart = if (interval.startOpen) {
            interpolate(interval.start, interval.end, insetFraction)
        } else {
            interval.start
        }
        val safeEnd = if (interval.endOpen) {
            interpolate(interval.start, interval.end, 1.0 - insetFraction)
        } else {
            interval.end
        }

        if (!safeStart.isFinite() || !safeEnd.isFinite()) return null
        if (interval.startOpen && safeStart <= interval.start) return null
        if (interval.endOpen && safeEnd >= interval.end) return null
        if (safeStart >= safeEnd) return null
        return safeStart to safeEnd
    }

    /**
     * Weighted interpolation avoids forming (end - start), which can overflow
     * when the finite endpoints have opposite signs near Double.MAX_VALUE.
     */
    private fun interpolate(
        start: Double,
        end: Double,
        fraction: Double,
    ): Double = when (fraction) {
        0.0 -> start
        1.0 -> end
        else -> start * (1.0 - fraction) + end * fraction
    }

    private fun precisionFailure(x: Double? = null) =
        FunctionGraphSamplingResult.Failure(
            FunctionGraphSamplingError(
                FunctionGraphSamplingErrorCode.NUMERIC_PRECISION_LOSS,
                x,
            ),
        )

    private data class SamplingInterval(
        val start: Double,
        val end: Double,
        val startOpen: Boolean,
        val endOpen: Boolean,
    )

    private const val MIN_SAMPLES_PER_SEGMENT = 2
    private const val MAX_SAMPLES_PER_SEGMENT = 512
    private const val MAX_SEGMENTS = 16
    private const val MAX_TOTAL_SAMPLES = 2_048
}
