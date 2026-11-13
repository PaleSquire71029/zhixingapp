package ci.us.zhixing.app.domain.math.model

import java.util.Collections

/**
 * A stable snapshot of points accepted by [CoordinatePointSetValidator].
 *
 * The backing list is copied and exposed as an unmodifiable view so a caller
 * cannot mutate the input set after validation.
 */
internal class CoordinatePointSet(
    points: List<CoordinatePoint>,
) {
    val points: List<CoordinatePoint> = Collections.unmodifiableList(points.toList())

    val size: Int
        get() = points.size
}
