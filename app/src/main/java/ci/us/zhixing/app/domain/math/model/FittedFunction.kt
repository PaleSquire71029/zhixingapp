package ci.us.zhixing.app.domain.math.model

import java.util.Collections

/** A typed wrapper around the polynomial and inverse-proportion models that can be graphed. */
internal sealed interface FittedFunction {
    data class Linear(val function: LinearFunction) : FittedFunction
    data class Quadratic(val function: QuadraticFunction) : FittedFunction
    data class Cubic(val function: CubicFunction) : FittedFunction
    data class InverseProportion(val function: InverseProportionFunction) : FittedFunction
}

/**
 * The real x-domain of a supported function, represented by exact excluded
 * x-values. The list is an immutable snapshot.
 */
internal class FunctionDomain(
    excludedXValues: List<Double>,
) {
    val excludedXValues: List<Double> =
        Collections.unmodifiableList(excludedXValues.distinct().sorted())

    fun contains(x: Double): Boolean =
        x.isFinite() && excludedXValues.none { it == x }
}
