package ci.us.zhixing.app.domain.math.model

import java.util.Collections

internal enum class FunctionGraphSamplingInputError {
    NON_FINITE_MIN_X,
    NON_FINITE_MAX_X,
    MIN_X_NOT_LESS_THAN_MAX_X,
    SAMPLE_COUNT_BELOW_MINIMUM,
    SAMPLE_COUNT_ABOVE_MAXIMUM,
}

internal enum class FunctionGraphSamplingErrorCode {
    NON_FINITE_FUNCTION_VALUE,
    NUMERIC_PRECISION_LOSS,
    SAMPLE_BUDGET_EXCEEDED,
}

internal data class FunctionGraphSamplingError(
    val code: FunctionGraphSamplingErrorCode,
    val x: Double? = null,
)

/** Requested finite x-range and number of samples in each connected domain segment. */
internal data class FunctionGraphSamplingRequest(
    val minX: Double,
    val maxX: Double,
    val samplesPerSegment: Int,
)

/** An independently drawable polyline. Segments must never be joined implicitly. */
internal class FunctionGraphSegment(
    points: List<CoordinatePoint>,
) {
    val points: List<CoordinatePoint> =
        Collections.unmodifiableList(points.toList())

    val size: Int
        get() = points.size
}

/** Result of splitting a function's domain and safely sampling each segment. */
internal sealed interface FunctionGraphSamplingResult {
    class Success(
        segments: List<FunctionGraphSegment>,
        excludedXValues: List<Double>,
    ) : FunctionGraphSamplingResult {
        val segments: List<FunctionGraphSegment> =
            Collections.unmodifiableList(segments.toList())

        val excludedXValues: List<Double> =
            Collections.unmodifiableList(excludedXValues.toList())

        val totalPointCount: Int
            get() = segments.sumOf { it.size }
    }

    data class InvalidInput(
        val reason: FunctionGraphSamplingInputError,
    ) : FunctionGraphSamplingResult

    data class Failure(
        val error: FunctionGraphSamplingError,
    ) : FunctionGraphSamplingResult
}
