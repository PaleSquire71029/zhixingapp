package ci.us.zhixing.app.domain.math.model

/**
 * One linear equation in two real variables:
 * xCoefficient * x + yCoefficient * y = constant.
 *
 * Finiteness is validated by the solver so invalid user input is represented
 * as a typed result instead of throwing while constructing the model.
 */
internal data class LinearEquation2D(
    val xCoefficient: Double,
    val yCoefficient: Double,
    val constant: Double,
)

/** A system containing exactly two linear equations in x and y. */
internal data class LinearEquationSystem(
    val first: LinearEquation2D,
    val second: LinearEquation2D,
)
