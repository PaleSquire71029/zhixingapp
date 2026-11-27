package ci.us.zhixing.app.ui.screens.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ci.us.zhixing.app.domain.math.model.MathCalculationStage
import ci.us.zhixing.app.ui.calculator.CalculatorEditorAction
import ci.us.zhixing.app.ui.calculator.CalculatorEditorReducer
import ci.us.zhixing.app.ui.calculator.CalculatorEditorState
import ci.us.zhixing.app.ui.calculator.CalculatorPresentationState
import ci.us.zhixing.app.ui.calculator.CalculatorPresenter
import ci.us.zhixing.app.ui.components.ZhixingButton
import ci.us.zhixing.app.ui.components.ZhixingButtonVariant
import ci.us.zhixing.app.ui.components.ZhixingResultPanel
import ci.us.zhixing.app.ui.foundation.scroll.ZhixingPageScrollContainer
import ci.us.zhixing.app.ui.glass.GlassMaterialState
import ci.us.zhixing.app.ui.glass.ZhixingGlassSurface
import ci.us.zhixing.app.ui.theme.ZhixingGlassShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop

private val calculatorEditorStateSaver: Saver<CalculatorEditorState, Any> = listSaver(
    save = { state ->
        listOf(state.text, state.selectionStart, state.selectionEnd)
    },
    restore = { saved ->
        CalculatorEditorState(
            text = saved[0] as String,
            selectionStart = saved[1] as Int,
            selectionEnd = saved[2] as Int,
        )
    },
)

private data class CalculatorKeySpec(
    val id: String,
    val label: String,
    val action: CalculatorEditorAction? = null,
    val variant: ZhixingButtonVariant = ZhixingButtonVariant.SECONDARY,
)

private val calculatorKeyRows = listOf(
    listOf(
        CalculatorKeySpec("ac", "AC", CalculatorEditorAction.Clear, ZhixingButtonVariant.TERTIARY),
        CalculatorKeySpec("parentheses", "()", CalculatorEditorAction.InsertParentheses, ZhixingButtonVariant.TERTIARY),
        CalculatorKeySpec("percent", "%", CalculatorEditorAction.InsertText("%"), ZhixingButtonVariant.TERTIARY),
        CalculatorKeySpec("divide", "÷", CalculatorEditorAction.InsertText("/"), ZhixingButtonVariant.TERTIARY),
    ),
    listOf(
        CalculatorKeySpec("digit-7", "7", CalculatorEditorAction.InsertText("7")),
        CalculatorKeySpec("digit-8", "8", CalculatorEditorAction.InsertText("8")),
        CalculatorKeySpec("digit-9", "9", CalculatorEditorAction.InsertText("9")),
        CalculatorKeySpec("multiply", "×", CalculatorEditorAction.InsertText("*"), ZhixingButtonVariant.TERTIARY),
    ),
    listOf(
        CalculatorKeySpec("digit-4", "4", CalculatorEditorAction.InsertText("4")),
        CalculatorKeySpec("digit-5", "5", CalculatorEditorAction.InsertText("5")),
        CalculatorKeySpec("digit-6", "6", CalculatorEditorAction.InsertText("6")),
        CalculatorKeySpec("subtract", "−", CalculatorEditorAction.InsertText("-"), ZhixingButtonVariant.TERTIARY),
    ),
    listOf(
        CalculatorKeySpec("digit-1", "1", CalculatorEditorAction.InsertText("1")),
        CalculatorKeySpec("digit-2", "2", CalculatorEditorAction.InsertText("2")),
        CalculatorKeySpec("digit-3", "3", CalculatorEditorAction.InsertText("3")),
        CalculatorKeySpec("add", "+", CalculatorEditorAction.InsertText("+"), ZhixingButtonVariant.TERTIARY),
    ),
    listOf(
        CalculatorKeySpec("digit-0", "0", CalculatorEditorAction.InsertText("0")),
        CalculatorKeySpec("decimal", ".", CalculatorEditorAction.InsertText(".")),
        CalculatorKeySpec("power", "^", CalculatorEditorAction.InsertText("^"), ZhixingButtonVariant.TERTIARY),
        CalculatorKeySpec("backspace", "⌫", CalculatorEditorAction.Backspace, ZhixingButtonVariant.TERTIARY),
    ),
)

/** Standard local arithmetic workspace; navigation and Dock stay in the Shell. */
@Composable
internal fun CalculatorScreen(
    backdrop: Backdrop,
    material: GlassMaterialState,
    modifier: Modifier = Modifier,
) {
    var editorState by rememberSaveable(stateSaver = calculatorEditorStateSaver) {
        mutableStateOf(CalculatorEditorState())
    }
    var showSubmittedResult by rememberSaveable { mutableStateOf(false) }
    val presentation = remember(editorState.text) {
        CalculatorPresenter.present(editorState.text)
    }
    val fieldValue = TextFieldValue(
        text = editorState.text,
        selection = TextRange(editorState.selectionStart, editorState.selectionEnd),
    )
    val calmMaterial = remember(material) {
        material.copy(
            refractionEnabled = false,
            chromaticDispersionEnabled = false,
        )
    }

    fun dispatch(action: CalculatorEditorAction) {
        val next = CalculatorEditorReducer.reduce(editorState, action)
        if (next.text != editorState.text) showSubmittedResult = false
        editorState = next
    }

    val resultValue: String
    val resultTitle: String
    val resultSupporting: String?
    val isResultError: Boolean
    when (presentation) {
        CalculatorPresentationState.Empty -> {
            resultValue = "输入表达式后，结果会显示在这里"
            resultTitle = "计算结果"
            resultSupporting = null
            isResultError = false
        }

        is CalculatorPresentationState.Success -> {
            resultValue = presentation.displayText
            resultTitle = if (showSubmittedResult) "计算结果" else "实时预览"
            resultSupporting = if (showSubmittedResult) "本地计算完成" else "按 = 确认计算结果"
            isResultError = false
        }

        is CalculatorPresentationState.Failure -> {
            resultValue = if (showSubmittedResult) presentation.error.message else "继续输入，按 = 查看结果"
            resultTitle = if (showSubmittedResult) "暂时无法计算" else "实时预览"
            resultSupporting = if (showSubmittedResult) {
                stageLabel(presentation.error.stage) + " · 位置 " + (presentation.error.offset + 1)
            } else {
                "表达式尚未形成可计算结果"
            }
            isResultError = showSubmittedResult
        }
    }

    ZhixingPageScrollContainer(
        modifier = modifier.testTag("calculator-page-scroll-container"),
        contentPadding = PaddingValues(
            horizontal = ZhixingTheme.spacing.pageHorizontal,
            top = 20.dp,
            bottom = 112.dp,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "计算",
                    style = ZhixingTheme.type.screenTitle,
                    color = ZhixingTheme.colors.onBackground,
                )
                Text(
                    text = "标准模式 · 本地运算",
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }

            ZhixingGlassSurface(
                backdrop = backdrop,
                material = calmMaterial,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calculator-expression-surface"),
                shape = ZhixingGlassShapes.card,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "表达式",
                        style = ZhixingTheme.type.sectionLabel,
                        color = ZhixingTheme.colors.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (editorState.text.isEmpty()) {
                            Text(
                                text = "输入表达式",
                                style = TextStyle(
                                    fontSize = 27.sp,
                                    lineHeight = 36.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = ZhixingTheme.colors.onSurfaceVariant.copy(alpha = 0.55f),
                            )
                        }
                        BasicTextField(
                            value = fieldValue,
                            onValueChange = { updated ->
                                val next = CalculatorEditorState(
                                    text = updated.text,
                                    selectionStart = updated.selection.start,
                                    selectionEnd = updated.selection.end,
                                )
                                if (next.text != editorState.text) showSubmittedResult = false
                                editorState = next
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("calculator-expression-field"),
                            readOnly = true,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = ZhixingTheme.colors.onSurface,
                                fontSize = 27.sp,
                                lineHeight = 36.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                            ),
                            cursorBrush = SolidColor(ZhixingTheme.colors.primary),
                        )
                    }
                }
            }

            ZhixingResultPanel(
                title = resultTitle,
                value = resultValue,
                supportingText = resultSupporting,
                isError = isResultError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calculator-result-panel"),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calculator-keypad"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                calculatorKeyRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { key ->
                            ZhixingButton(
                                label = key.label,
                                onClick = {
                                    val action = key.action
                                    if (action != null) dispatch(action)
                                },
                                variant = key.variant,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 52.dp)
                                    .testTag("calculator-key-" + key.id),
                            )
                        }
                    }
                }
                ZhixingButton(
                    label = "=",
                    onClick = { showSubmittedResult = editorState.text.isNotBlank() },
                    variant = ZhixingButtonVariant.PRIMARY,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("calculator-key-equals"),
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

private fun stageLabel(stage: MathCalculationStage): String = when (stage) {
    MathCalculationStage.TOKENIZER -> "输入错误"
    MathCalculationStage.PARSER -> "表达式错误"
    MathCalculationStage.EVALUATOR -> "计算错误"
}
