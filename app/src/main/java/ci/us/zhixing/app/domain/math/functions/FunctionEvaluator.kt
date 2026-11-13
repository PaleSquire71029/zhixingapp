package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.FunctionEvaluationErrorCode
import ci.us.zhixing.app.domain.math.model.FunctionEvaluationInputError
import ci.us.zhixing.app.domain.math.model.FunctionEvaluationResult
import ci.us.zhixing.app.domain.math.model.FittedFunction

/** Evaluates supported fitted-function models without leaking NaN or infinity. */
internal object FunctionEvaluator {
    fun evaluate(
        function: FittedFunction,
        x: Double,
    ): FunctionEvaluationResult {
        if (!x.isFinite()) {
            return FunctionEvaluationResult.InvalidInput(
                FunctionEvaluationInputError.NON_FINITE_X,
            )
        }

        val domain = FunctionDomainResolver.resolve(function)
        val excludedX = domain.excludedXValues.firstOrNull { it == x }
        if (excludedX != null) {
            return FunctionEvaluationResult.OutsideDomain(excludedX)
        }

        val coefficientsAreFinite = when (function) {
            is FittedFunction.Linear ->
                function.function.slope.isFinite() &&
                    function.function.intercept.isFinite()

            is FittedFunction.Quadratic ->
                function.function.quadraticCoefficient.isFinite() &&
                    function.function.linearCoefficient.isFinite() &&
                    function.function.intercept.isFinite()

            is FittedFunction.Cubic ->
                function.function.cubicCoefficient.isFinite() &&
                    function.function.quadraticCoefficient.isFinite() &&
                    function.function.linearCoefficient.isFinite() &&
                    function.function.intercept.isFinite()

            is FittedFunction.InverseProportion ->
                function.function.constant.isFinite()
        }
        if (!coefficientsAreFinite) {
            return FunctionEvaluationResult.Failure(
                FunctionEvaluationErrorCode.NON_FINITE_COEFFICIENT,
                x,
            )
        }

        val y = when (function) {
            is FittedFunction.Linear -> {
                val model = function.function
                model.slope * x + model.intercept
            }

            is FittedFunction.Quadratic -> {
                val model = function.function
                (model.quadraticCoefficient * x + model.linearCoefficient) * x +
                    model.intercept
            }

            is FittedFunction.Cubic -> {
                val model = function.function
                ((model.cubicCoefficient * x + model.quadraticCoefficient) * x +
                    model.linearCoefficient) * x + model.intercept
            }

            is FittedFunction.InverseProportion ->
                function.function.constant / x
        }

        return if (y.isFinite()) {
            FunctionEvaluationResult.Success(y)
        } else {
            FunctionEvaluationResult.Failure(
                FunctionEvaluationErrorCode.NON_FINITE_RESULT,
                x,
            )
        }
    }
}
