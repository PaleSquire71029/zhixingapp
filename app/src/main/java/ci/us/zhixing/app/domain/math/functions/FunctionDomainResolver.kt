package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.FittedFunction
import ci.us.zhixing.app.domain.math.model.FunctionDomain

/** Resolves the x-domain shared by function evaluation and graph sampling. */
internal object FunctionDomainResolver {
    fun resolve(function: FittedFunction): FunctionDomain =
        when (function) {
            is FittedFunction.Linear,
            is FittedFunction.Quadratic,
            is FittedFunction.Cubic ->
                FunctionDomain(emptyList())

            is FittedFunction.InverseProportion ->
                FunctionDomain(listOf(0.0))
        }
}
