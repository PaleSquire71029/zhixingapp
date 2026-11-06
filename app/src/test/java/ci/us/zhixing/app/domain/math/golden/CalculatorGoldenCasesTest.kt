package ci.us.zhixing.app.domain.math.golden

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorGoldenCasesTest {
    @Test
    fun everyGoldenCaseHasAUniqueStableId() {
        val ids = CalculatorGoldenCases.tokenizer.map { it.id } +
            CalculatorGoldenCases.arithmetic.map { it.id } +
            CalculatorGoldenCases.equations.map { it.id } +
            CalculatorGoldenCases.inequalities.map { it.id }

        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { it.isNotBlank() })
    }

    @Test
    fun tokenizerCasesDeclareExactlyOneExpectedOutcome() {
        CalculatorGoldenCases.tokenizer.forEach { case ->
            assertTrue(
                "Tokenizer case ${case.id} must define tokens or an error, but not both",
                (case.expectedTokens != null) xor (case.expectedError != null),
            )
        }
    }

    @Test
    fun arithmeticCasesDeclareExactlyOneExpectedOutcome() {
        CalculatorGoldenCases.arithmetic.forEach { case ->
            assertTrue(
                "Arithmetic case ${case.id} must define a value or an error, but not both",
                (case.expectedValue != null) xor (case.expectedError != null),
            )
            case.expectedValue?.let { assertTrue("${case.id} must be finite", it.isFinite()) }
        }
    }

    @Test
    fun equationCasesKeepRootAndDomainExpectationsSeparate() {
        CalculatorGoldenCases.equations.forEach { case ->
            assertTrue(case.expectedRealRoots.all { it.isFinite() })
            assertTrue(case.expectedExcludedDomainPoints.all { it.isFinite() })
            assertFalse(
                "A case with an excluded point must carry the domain-pole tag: ${case.id}",
                case.expectedExcludedDomainPoints.isNotEmpty() && "domain-pole" !in case.tags,
            )
            assertTrue(
                "A case with a finite root and an error is contradictory: ${case.id}",
                case.expectedError == null || case.expectedRealRoots.isEmpty(),
            )
        }
    }

    @Test
    fun inequalityIntervalsHaveValidBoundsAndDomainMetadata() {
        CalculatorGoldenCases.inequalities.forEach { case ->
            case.expectedIntervals.forEach { interval ->
                interval.lower?.let { assertTrue(it.isFinite()) }
                interval.upper?.let { assertTrue(it.isFinite()) }
                if (interval.lower == null) assertFalse(interval.lowerInclusive)
                if (interval.upper == null) assertFalse(interval.upperInclusive)
                if (interval.lower != null && interval.upper != null) {
                    assertTrue(interval.lower <= interval.upper)
                    if (interval.lower == interval.upper) {
                        assertTrue(interval.lowerInclusive && interval.upperInclusive)
                    }
                }
            }
            assertTrue(case.expectedExcludedDomainPoints.all { it.isFinite() })
            assertFalse(
                "A case with an excluded point must carry the domain-pole tag: ${case.id}",
                case.expectedExcludedDomainPoints.isNotEmpty() && "domain-pole" !in case.tags,
            )
            assertTrue(case.expectedError == null || case.expectedIntervals.isEmpty())
        }
    }

    @Test
    fun initialCorpusCoversTheKnownLegacySemantics() {
        val allTags = (CalculatorGoldenCases.tokenizer.flatMap { it.tags } +
            CalculatorGoldenCases.arithmetic.flatMap { it.tags } +
            CalculatorGoldenCases.equations.flatMap { it.tags } +
            CalculatorGoldenCases.inequalities.flatMap { it.tags }).toSet()

        val requiredTags = setOf(
            "precedence",
            "associativity",
            "implicit-multiplication",
            "percent",
            "unicode",
            "domain-pole",
            "strict",
            "nonstrict",
            "high-degree",
        )
        assertTrue("Missing tags: ${requiredTags - allTags}", allTags.containsAll(requiredTags))
        assertNotNull(CalculatorGoldenCases.tokenizer)
        assertTrue(CalculatorGoldenCases.arithmetic.isNotEmpty())
        assertTrue(CalculatorGoldenCases.equations.isNotEmpty())
        assertTrue(CalculatorGoldenCases.inequalities.isNotEmpty())
    }
}
