package ci.us.zhixing.app.ui.calculator

/**
 * Editing state for the standard calculator expression field.
 *
 * Selection offsets use Kotlin String indices (UTF-16 code units), matching
 * Compose TextFieldValue / TextRange. Both ends are inclusive of the caret
 * position and the selected range is interpreted as [start, end).
 */
internal data class CalculatorEditorState(
    val text: String = "",
    val selectionStart: Int = text.length,
    val selectionEnd: Int = selectionStart,
) {
    init {
        require(selectionStart in 0..text.length) {
            "selectionStart must be a valid UTF-16 offset"
        }
        require(selectionEnd in 0..text.length) {
            "selectionEnd must be a valid UTF-16 offset"
        }
    }

    val hasSelection: Boolean
        get() = selectionStart != selectionEnd

    val selectionMin: Int
        get() = minOf(selectionStart, selectionEnd)

    val selectionMax: Int
        get() = maxOf(selectionStart, selectionEnd)
}

internal sealed interface CalculatorEditorAction {
    data class SetSelection(
        val start: Int,
        val end: Int = start,
    ) : CalculatorEditorAction

    data class InsertText(val value: String) : CalculatorEditorAction

    data object Backspace : CalculatorEditorAction

    data object DeleteForward : CalculatorEditorAction

    data object Clear : CalculatorEditorAction

    /** Inserts a pair at the caret, or wraps the current selection. */
    data object InsertParentheses : CalculatorEditorAction
}

/** Pure reducer: it owns no Compose state, Android input method or UI effects. */
internal object CalculatorEditorReducer {
    fun reduce(
        state: CalculatorEditorState,
        action: CalculatorEditorAction,
    ): CalculatorEditorState = when (action) {
        is CalculatorEditorAction.SetSelection -> state.copy(
            selectionStart = action.start.coerceIn(0, state.text.length),
            selectionEnd = action.end.coerceIn(0, state.text.length),
        )

        is CalculatorEditorAction.InsertText -> {
            if (action.value.isEmpty()) {
                state
            } else {
                replaceSelection(state, action.value)
            }
        }

        CalculatorEditorAction.Backspace -> deleteBackward(state)

        CalculatorEditorAction.DeleteForward -> deleteForward(state)

        CalculatorEditorAction.Clear -> CalculatorEditorState()

        CalculatorEditorAction.InsertParentheses -> insertParentheses(state)
    }

    private fun replaceSelection(
        state: CalculatorEditorState,
        replacement: String,
    ): CalculatorEditorState {
        val start = state.selectionMin
        val end = state.selectionMax
        val nextText = buildString(state.text.length - (end - start) + replacement.length) {
            append(state.text, 0, start)
            append(replacement)
            append(state.text, end, state.text.length)
        }
        val nextCaret = start + replacement.length
        return CalculatorEditorState(
            text = nextText,
            selectionStart = nextCaret,
            selectionEnd = nextCaret,
        )
    }

    private fun deleteBackward(state: CalculatorEditorState): CalculatorEditorState {
        if (state.hasSelection) return replaceSelection(state, "")
        val caret = state.selectionStart
        if (caret == 0) return state

        val start: Int
        val end: Int
        if (caret < state.text.length &&
            state.text[caret - 1].isHighSurrogate() &&
            state.text[caret].isLowSurrogate()
        ) {
            // A platform selection can theoretically land between a surrogate pair.
            start = caret - 1
            end = caret + 1
        } else {
            start = previousCodePointBoundary(state.text, caret)
            end = caret
        }
        return deleteRange(state, start, end)
    }

    private fun deleteForward(state: CalculatorEditorState): CalculatorEditorState {
        if (state.hasSelection) return replaceSelection(state, "")
        val caret = state.selectionStart
        if (caret == state.text.length) return state

        val start: Int
        val end: Int
        if (caret > 0 &&
            state.text[caret - 1].isHighSurrogate() &&
            state.text[caret].isLowSurrogate()
        ) {
            start = caret - 1
            end = caret + 1
        } else {
            start = caret
            end = nextCodePointBoundary(state.text, caret)
        }
        return deleteRange(state, start, end)
    }

    private fun deleteRange(
        state: CalculatorEditorState,
        start: Int,
        end: Int,
    ): CalculatorEditorState {
        val nextText = state.text.removeRange(start, end)
        return CalculatorEditorState(
            text = nextText,
            selectionStart = start,
            selectionEnd = start,
        )
    }

    private fun previousCodePointBoundary(text: String, offset: Int): Int {
        val previous = offset - 1
        return if (previous > 0 &&
            text[previous].isLowSurrogate() &&
            text[previous - 1].isHighSurrogate()
        ) {
            previous - 1
        } else {
            previous
        }
    }

    private fun nextCodePointBoundary(text: String, offset: Int): Int =
        if (offset + 1 < text.length &&
            text[offset].isHighSurrogate() &&
            text[offset + 1].isLowSurrogate()
        ) {
            offset + 2
        } else {
            offset + 1
        }

    private fun insertParentheses(state: CalculatorEditorState): CalculatorEditorState {
        val start = state.selectionMin
        val end = state.selectionMax
        val selected = state.text.substring(start, end)
        val wrapped = "($selected)"
        val nextText = buildString(state.text.length + 2) {
            append(state.text, 0, start)
            append(wrapped)
            append(state.text, end, state.text.length)
        }

        return if (state.hasSelection) {
            CalculatorEditorState(
                text = nextText,
                selectionStart = start + 1,
                selectionEnd = start + 1 + selected.length,
            )
        } else {
            val caret = start + 1
            CalculatorEditorState(
                text = nextText,
                selectionStart = caret,
                selectionEnd = caret,
            )
        }
    }
}
