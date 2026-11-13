package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinateAxis
import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.CoordinatePointSet
import ci.us.zhixing.app.domain.math.model.CoordinatePointSetValidationResult

/**
 * Validates x/y data before it is passed to a function-fitting algorithm.
 *
 * Duplicate x values are rejected because the supported fitting targets are
 * functions y = f(x). Equality is exact; near-equal x values are left for a
 * fitting algorithm to assess for numerical conditioning.
 */
internal object CoordinatePointSetValidator {
    fun validate(
        points: List<CoordinatePoint>,
        requiredPointCount: Int = 1,
    ): CoordinatePointSetValidationResult {
        if (requiredPointCount < 1) {
            return CoordinatePointSetValidationResult.InvalidRequiredPointCount(
                requiredPointCount,
            )
        }
        if (points.isEmpty()) {
            return CoordinatePointSetValidationResult.EmptyInput
        }

        points.forEachIndexed { index, point ->
            if (!point.x.isFinite()) {
                return CoordinatePointSetValidationResult.NonFiniteCoordinate(
                    index,
                    CoordinateAxis.X,
                )
            }
            if (!point.y.isFinite()) {
                return CoordinatePointSetValidationResult.NonFiniteCoordinate(
                    index,
                    CoordinateAxis.Y,
                )
            }
        }

        val firstIndexByX = HashMap<Double, Int>(points.size)
        points.forEachIndexed { index, point ->
            // Canonicalize signed zero so -0.0 and +0.0 represent the same x.
            val key = if (point.x == 0.0) 0.0 else point.x
            val firstIndex = firstIndexByX[key]
            if (firstIndex != null) {
                return CoordinatePointSetValidationResult.DuplicateXCoordinate(
                    point.x,
                    firstIndex,
                    index,
                )
            }
            firstIndexByX[key] = index
        }

        if (points.size < requiredPointCount) {
            return CoordinatePointSetValidationResult.InsufficientPointCount(
                actualPointCount = points.size,
                requiredPointCount = requiredPointCount,
            )
        }

        return CoordinatePointSetValidationResult.Valid(CoordinatePointSet(points))
    }
}
