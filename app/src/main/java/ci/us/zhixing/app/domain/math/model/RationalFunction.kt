package ci.us.zhixing.app.domain.math.model

/**
 * A rational expression together with every polynomial restriction inherited
 * from the original expression tree.
 *
 * [numerator] / [denominator] stores the current algebraic form. The
 * [domainRestrictions] list is deliberately separate: later operations may
 * change or simplify the displayed fraction, but must not erase points where
 * an original sub-expression was undefined.
 *
 * Restrictions mean p(x) != 0. Non-zero constant restrictions are omitted
 * because they exclude no real input. The current non-constant denominator
 * is always included.
 */
internal class RationalFunction private constructor(
    val numerator: Polynomial,
    val denominator: Polynomial,
    domainRestrictions: List<Polynomial>,
) {
    val domainRestrictions: List<Polynomial> = domainRestrictions.toList()

    companion object {
        /**
         * Creates a rational function and establishes its domain invariant.
         *
         * The supplied restrictions are inherited from earlier expression
         * nodes. The current denominator is always added, and duplicate or
         * non-zero constant restrictions are removed.
         */
        fun create(
            numerator: Polynomial,
            denominator: Polynomial,
            domainRestrictions: List<Polynomial> = emptyList(),
        ): RationalFunctionOperationResult {
            if (denominator.isZero()) {
                return failure(
                    RationalFunctionErrorCode.ZERO_DENOMINATOR_POLYNOMIAL,
                    "有理式的分母不能是零多项式",
                )
            }

            if (domainRestrictions.any { it.isZero() }) {
                return failure(
                    RationalFunctionErrorCode.EMPTY_DOMAIN,
                    "原式定义域为空：存在恒为零的定义域限制",
                )
            }

            val restrictions = (domainRestrictions + denominator)
                .filterNot { it.isConstant() }
                .distinct()

            return RationalFunctionOperationResult.Success(
                RationalFunction(
                    numerator = numerator,
                    denominator = denominator,
                    domainRestrictions = restrictions,
                ),
            )
        }

        private fun failure(
            code: RationalFunctionErrorCode,
            message: String,
        ) = RationalFunctionOperationResult.Failure(
            RationalFunctionError(code, message),
        )
    }
}

internal enum class RationalFunctionErrorCode {
    ZERO_DENOMINATOR_POLYNOMIAL,
    EMPTY_DOMAIN,
    ZERO_DIVISOR_FUNCTION,
    INVALID_EXPONENT,
    NON_FINITE_INPUT,
    NON_FINITE_RESULT,
    DOMAIN_RESTRICTION_VIOLATION,
    ZERO_DENOMINATOR_AT_INPUT,
}

internal data class RationalFunctionError(
    val code: RationalFunctionErrorCode,
    val message: String,
)

internal sealed interface RationalFunctionOperationResult {
    data class Success(val value: RationalFunction) : RationalFunctionOperationResult
    data class Failure(val error: RationalFunctionError) : RationalFunctionOperationResult
}
