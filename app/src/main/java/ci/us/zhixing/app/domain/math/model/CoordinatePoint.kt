package ci.us.zhixing.app.domain.math.model

/**
 * A Cartesian point used by function fitting.
 *
 * Finiteness is checked when a list of points is validated, rather than in
 * this value constructor, so invalid external input can be represented as a
 * typed validation result.
 */
internal data class CoordinatePoint(
    val x: Double,
    val y: Double,
)
