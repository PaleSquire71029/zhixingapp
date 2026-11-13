package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CoordinateAxis
import ci.us.zhixing.app.domain.math.model.CoordinatePoint
import ci.us.zhixing.app.domain.math.model.CoordinatePointSetValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoordinatePointSetValidatorTest {
    @Test
    fun acceptsFinitePointsWhenMinimumCountIsMet() {
        val result = CoordinatePointSetValidator.validate(
            listOf(
                CoordinatePoint(-2.0, 3.0),
                CoordinatePoint(0.0, 1.0),
                CoordinatePoint(4.0, -5.0),
            ),
            requiredPointCount = 3,
        )

        assertTrue(result is CoordinatePointSetValidationResult.Valid)
        val pointSet = (result as CoordinatePointSetValidationResult.Valid).pointSet
        assertEquals(3, pointSet.size)
        assertEquals(
            listOf(
                CoordinatePoint(-2.0, 3.0),
                CoordinatePoint(0.0, 1.0),
                CoordinatePoint(4.0, -5.0),
            ),
            pointSet.points,
        )
    }

    @Test
    fun validPointSetIsASnapshotOfTheInputList() {
        val source = mutableListOf(CoordinatePoint(1.0, 2.0))
        val result = CoordinatePointSetValidator.validate(source)
            as CoordinatePointSetValidationResult.Valid

        source[0] = CoordinatePoint(9.0, 9.0)

        assertEquals(CoordinatePoint(1.0, 2.0), result.pointSet.points.single())
    }

    @Test
    fun rejectsEmptyInput() {
        assertEquals(
            CoordinatePointSetValidationResult.EmptyInput,
            CoordinatePointSetValidator.validate(emptyList()),
        )
    }

    @Test
    fun rejectsNonFiniteXCoordinateWithItsPointIndex() {
        assertEquals(
            CoordinatePointSetValidationResult.NonFiniteCoordinate(1, CoordinateAxis.X),
            CoordinatePointSetValidator.validate(
                listOf(
                    CoordinatePoint(0.0, 1.0),
                    CoordinatePoint(Double.POSITIVE_INFINITY, 2.0),
                ),
            ),
        )
    }

    @Test
    fun rejectsNonFiniteYCoordinateWithItsPointIndex() {
        assertEquals(
            CoordinatePointSetValidationResult.NonFiniteCoordinate(0, CoordinateAxis.Y),
            CoordinatePointSetValidator.validate(
                listOf(CoordinatePoint(2.0, Double.NaN)),
            ),
        )
    }

    @Test
    fun rejectsDuplicateXAndReportsBothPointIndexes() {
        assertEquals(
            CoordinatePointSetValidationResult.DuplicateXCoordinate(
                x = 2.0,
                firstPointIndex = 0,
                duplicatePointIndex = 2,
            ),
            CoordinatePointSetValidator.validate(
                listOf(
                    CoordinatePoint(2.0, 1.0),
                    CoordinatePoint(3.0, 4.0),
                    CoordinatePoint(2.0, 9.0),
                ),
            ),
        )
    }

    @Test
    fun treatsPositiveAndNegativeZeroAsTheSameXCoordinate() {
        assertEquals(
            CoordinatePointSetValidationResult.DuplicateXCoordinate(
                x = 0.0,
                firstPointIndex = 0,
                duplicatePointIndex = 1,
            ),
            CoordinatePointSetValidator.validate(
                listOf(
                    CoordinatePoint(-0.0, 1.0),
                    CoordinatePoint(0.0, 2.0),
                ),
            ),
        )
    }

    @Test
    fun keepsDistinctNearbyXValuesSeparate() {
        val result = CoordinatePointSetValidator.validate(
            listOf(
                CoordinatePoint(1.0, 2.0),
                CoordinatePoint(Math.nextUp(1.0), 3.0),
            ),
            requiredPointCount = 2,
        )

        assertTrue(result is CoordinatePointSetValidationResult.Valid)
    }

    @Test
    fun rejectsInsufficientPointCountAfterCoordinateValidation() {
        assertEquals(
            CoordinatePointSetValidationResult.InsufficientPointCount(
                actualPointCount = 1,
                requiredPointCount = 2,
            ),
            CoordinatePointSetValidator.validate(
                listOf(CoordinatePoint(1.0, 2.0)),
                requiredPointCount = 2,
            ),
        )
    }

    @Test
    fun rejectsZeroOrNegativeRequiredPointCount() {
        assertEquals(
            CoordinatePointSetValidationResult.InvalidRequiredPointCount(0),
            CoordinatePointSetValidator.validate(
                listOf(CoordinatePoint(1.0, 2.0)),
                requiredPointCount = 0,
            ),
        )
        assertEquals(
            CoordinatePointSetValidationResult.InvalidRequiredPointCount(-1),
            CoordinatePointSetValidator.validate(
                listOf(CoordinatePoint(1.0, 2.0)),
                requiredPointCount = -1,
            ),
        )
    }

    @Test
    fun reportsNonFiniteCoordinatesBeforeDuplicateOrCountErrors() {
        assertEquals(
            CoordinatePointSetValidationResult.NonFiniteCoordinate(1, CoordinateAxis.Y),
            CoordinatePointSetValidator.validate(
                listOf(
                    CoordinatePoint(1.0, 1.0),
                    CoordinatePoint(1.0, Double.NEGATIVE_INFINITY),
                ),
                requiredPointCount = 3,
            ),
        )
    }
}
