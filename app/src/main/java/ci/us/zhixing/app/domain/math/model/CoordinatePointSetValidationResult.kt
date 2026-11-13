package ci.us.zhixing.app.domain.math.model

internal enum class CoordinateAxis {
    X,
    Y,
}

/** Typed outcomes for validating coordinate input before fitting a function. */
internal sealed interface CoordinatePointSetValidationResult {
    data class Valid(
        val pointSet: CoordinatePointSet,
    ) : CoordinatePointSetValidationResult

    object EmptyInput : CoordinatePointSetValidationResult

    data class InvalidRequiredPointCount(
        val requiredPointCount: Int,
    ) : CoordinatePointSetValidationResult

    data class NonFiniteCoordinate(
        val pointIndex: Int,
        val axis: CoordinateAxis,
    ) : CoordinatePointSetValidationResult

    data class DuplicateXCoordinate(
        val x: Double,
        val firstPointIndex: Int,
        val duplicatePointIndex: Int,
    ) : CoordinatePointSetValidationResult

    data class InsufficientPointCount(
        val actualPointCount: Int,
        val requiredPointCount: Int,
    ) : CoordinatePointSetValidationResult
}
