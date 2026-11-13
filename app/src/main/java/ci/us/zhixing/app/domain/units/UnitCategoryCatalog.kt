package ci.us.zhixing.app.domain.units

import java.util.Collections

/** Read-only catalog for the unit categories supported by the legacy calculator. */
internal object UnitCategoryCatalog {
    val all: List<UnitCategory> =
        Collections.unmodifiableList(UnitCategory.values().toList())

    private val byId: Map<String, UnitCategory> =
        all.associateBy(UnitCategory::id)

    fun findById(id: String): UnitCategory? = byId[id]

    fun areDimensionallyCompatible(
        source: UnitCategory,
        target: UnitCategory,
    ): Boolean = source.dimension == target.dimension
}
