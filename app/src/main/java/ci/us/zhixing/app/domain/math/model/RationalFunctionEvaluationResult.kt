package ci.us.zhixing.app.domain.math.model

/** Result of evaluating a rational function at one real input. */
internal sealed interface RationalFunctionEvaluationResult {
    data class Success(val value: Double) : RationalFunctionEvaluationResult
    data class Failure(val error: RationalFunctionError) : RationalFunctionEvaluationResult
}
