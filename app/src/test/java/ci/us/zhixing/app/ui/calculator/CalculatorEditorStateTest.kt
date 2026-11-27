package ci.us.zhixing.app.ui.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEditorStateTest {
    @Test
    fun insertsAtCaretAndMovesCaretAfterInsertedText() {
        val state = CalculatorEditorState(text = "23", selectionStart = 1)

        val result = CalculatorEditorReducer.reduce(
            state,
            CalculatorEditorAction.InsertText(" + "),
        )

        assertEquals("2 + 3", result.text)
        assertEquals(4, result.selectionStart)
        assertEquals(result.selectionStart, result.selectionEnd)
    }

    @Test
    fun insertionReplacesForwardOrReversedSelection() {
        listOf(
            CalculatorEditorState("1234", 1, 3),
            CalculatorEditorState("1234", 3, 1),
        ).forEach { state ->
            val result = CalculatorEditorReducer.reduce(
                state,
                CalculatorEditorAction.InsertText("x"),
            )

            assertEquals("1x4", result.text)
            assertEquals(2, result.selectionStart)
            assertEquals(2, result.selectionEnd)
        }
    }

    @Test
    fun backspaceDeletesPreviousCharacterOrSelectedRange() {
        val previous = CalculatorEditorReducer.reduce(
            CalculatorEditorState(text = "123", selectionStart = 2),
            CalculatorEditorAction.Backspace,
        )
        assertEquals("13", previous.text)
        assertEquals(1, previous.selectionStart)

        val selected = CalculatorEditorReducer.reduce(
            CalculatorEditorState(text = "12345", selectionStart = 1, selectionEnd = 4),
            CalculatorEditorAction.Backspace,
        )
        assertEquals("15", selected.text)
        assertEquals(1, selected.selectionStart)
    }

    @Test
    fun forwardDeleteRemovesNextCharacterAndRespectsEndBoundary() {
        val deleted = CalculatorEditorReducer.reduce(
            CalculatorEditorState(text = "123", selectionStart = 1),
            CalculatorEditorAction.DeleteForward,
        )
        assertEquals("13", deleted.text)
        assertEquals(1, deleted.selectionStart)

        val atEnd = CalculatorEditorState(text = "123")
        assertEquals(
            atEnd,
            CalculatorEditorReducer.reduce(atEnd, CalculatorEditorAction.DeleteForward),
        )
    }

    @Test
    fun deletesSurrogatePairsAsOneUnicodeCodePoint() {
        val text = "1😀2"
        val state = CalculatorEditorState(text = text, selectionStart = 3)
        val result = CalculatorEditorReducer.reduce(state, CalculatorEditorAction.Backspace)

        assertEquals("12", result.text)
        assertEquals(1, result.selectionStart)
    }

    @Test
    fun clampsSelectionToValidUtf16Offsets() {
        val result = CalculatorEditorReducer.reduce(
            CalculatorEditorState(text = "123"),
            CalculatorEditorAction.SetSelection(start = -4, end = 99),
        )
        assertEquals(0, result.selectionStart)
        assertEquals(3, result.selectionEnd)
        assertTrue(result.hasSelection)
    }

    @Test
    fun insertsPairedParenthesesAndPlacesCaretBetweenThem() {
        val result = CalculatorEditorReducer.reduce(
            CalculatorEditorState(text = "12"),
            CalculatorEditorAction.InsertParentheses,
        )

        assertEquals("12()", result.text)
        assertEquals(3, result.selectionStart)
        assertEquals(3, result.selectionEnd)
    }

    @Test
    fun wrapsSelectedTextAndKeepsItsSelectionInsideParentheses() {
        val result = CalculatorEditorReducer.reduce(
            CalculatorEditorState(text = "2+3", selectionStart = 0, selectionEnd = 3),
            CalculatorEditorAction.InsertParentheses,
        )

        assertEquals("(2+3)", result.text)
        assertEquals(1, result.selectionStart)
        assertEquals(4, result.selectionEnd)
        assertTrue(result.hasSelection)
    }

    @Test
    fun clearResetsTextAndSelectionAndEmptyInsertionIsANoOp() {
        val state = CalculatorEditorState(text = "123", selectionStart = 1, selectionEnd = 2)
        val cleared = CalculatorEditorReducer.reduce(state, CalculatorEditorAction.Clear)
        assertEquals(CalculatorEditorState(), cleared)
        assertFalse(cleared.hasSelection)

        assertEquals(state, CalculatorEditorReducer.reduce(state, CalculatorEditorAction.InsertText("")))
    }

    @Test
    fun backspaceAtStartIsANoOp() {
        val state = CalculatorEditorState(text = "123", selectionStart = 0)
        assertEquals(state, CalculatorEditorReducer.reduce(state, CalculatorEditorAction.Backspace))
    }
}
