package ci.us.zhixing.app.domain.units

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitCategoryCatalogTest {
    @Test
    fun catalogContainsTheNineLegacyCategoriesInStableOrder() {
        assertEquals(
            listOf(
                "length",
                "area",
                "volume",
                "mass",
                "temp",
                "speed",
                "time",
                "data",
                "angle",
            ),
            UnitCategoryCatalog.all.map(UnitCategory::id),
        )
    }

    @Test
    fun everyCategoryHasAUniqueNonBlankIdAndDisplayName() {
        val categories = UnitCategoryCatalog.all

        assertEquals(categories.size, categories.map(UnitCategory::id).toSet().size)
        assertEquals(categories.size, categories.map(UnitCategory::displayName).toSet().size)
        assertTrue(categories.all { it.id.isNotBlank() && it.displayName.isNotBlank() })
    }

    @Test
    fun categoryLookupUsesTheLegacyStableIds() {
        assertSame(
            UnitCategory.LENGTH,
            UnitCategoryCatalog.findById("length"),
        )
        assertSame(
            UnitCategory.TEMPERATURE,
            UnitCategoryCatalog.findById("temp"),
        )
        assertNull(UnitCategoryCatalog.findById("Length"))
        assertNull(UnitCategoryCatalog.findById("unknown"))
    }

    @Test
    fun dimensionVectorsRepresentBaseAndDerivedQuantities() {
        assertEquals(
            UnitDimension(lengthExponent = 1),
            UnitCategory.LENGTH.dimension,
        )
        assertEquals(
            UnitDimension(lengthExponent = 2),
            UnitCategory.AREA.dimension,
        )
        assertEquals(
            UnitDimension(lengthExponent = 3),
            UnitCategory.VOLUME.dimension,
        )
        assertEquals(
            UnitDimension(lengthExponent = 1, timeExponent = -1),
            UnitCategory.SPEED.dimension,
        )
        assertEquals(
            UnitDimension(temperatureExponent = 1),
            UnitCategory.TEMPERATURE.dimension,
        )
    }

    @Test
    fun categoriesAreCompatibleOnlyWhenTheirDimensionsMatch() {
        assertTrue(
            UnitCategoryCatalog.areDimensionallyCompatible(
                UnitCategory.LENGTH,
                UnitCategory.LENGTH,
            ),
        )
        assertFalse(
            UnitCategoryCatalog.areDimensionallyCompatible(
                UnitCategory.LENGTH,
                UnitCategory.AREA,
            ),
        )
        assertFalse(
            UnitCategoryCatalog.areDimensionallyCompatible(
                UnitCategory.SPEED,
                UnitCategory.LENGTH,
            ),
        )
        assertFalse(
            UnitCategoryCatalog.areDimensionallyCompatible(
                UnitCategory.ANGLE,
                UnitCategory.LENGTH,
            ),
        )
        assertFalse(
            UnitCategoryCatalog.areDimensionallyCompatible(
                UnitCategory.DATA_STORAGE,
                UnitCategory.MASS,
            ),
        )
    }

    @Test
    fun temperatureIsMarkedAsAffineWhileOtherCategoriesAreLinear() {
        assertEquals(
            UnitConversionKind.AFFINE,
            UnitCategory.TEMPERATURE.conversionKind,
        )
        assertTrue(
            UnitCategoryCatalog.all
                .filterNot { it == UnitCategory.TEMPERATURE }
                .all { it.conversionKind == UnitConversionKind.LINEAR },
        )
    }

    @Test
    fun categoryListIsAnImmutableSnapshot() {
        @Suppress("UNCHECKED_CAST")
        val mutableView = UnitCategoryCatalog.all as MutableList<UnitCategory>

        var rejected = false
        try {
            mutableView.add(UnitCategory.LENGTH)
        } catch (_: UnsupportedOperationException) {
            rejected = true
        }

        assertTrue("Expected the catalog to reject mutation", rejected)
        assertEquals(9, UnitCategoryCatalog.all.size)
    }
}
