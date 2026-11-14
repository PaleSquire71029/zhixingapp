package ci.us.zhixing.app.ui.units

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.mergeDescendants
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.domain.units.UnitConversionError
import ci.us.zhixing.app.domain.units.UnitConversionResult
import ci.us.zhixing.app.ui.glass.GlassMaterialState
import ci.us.zhixing.app.ui.glass.ZhixingGlassSurface
import ci.us.zhixing.app.ui.glass.ZhixingLiquidButton
import ci.us.zhixing.app.ui.theme.ZhixingGlassShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop

@Composable
internal fun ToolsDirectoryScreen(
    backdrop: Backdrop,
    material: GlassMaterialState,
    modifier: Modifier = Modifier,
    onOpenUnitConverter: () -> Unit,
) {
    val calmMaterial = material.copy(
        refractionEnabled = false,
        chromaticDispersionEnabled = false,
        environmentReflectionEnabled = false,
    )
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ZhixingTheme.spacing.pageHorizontal)
            .padding(top = 30.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = "工具",
                style = ZhixingTheme.type.screenTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = "常用工具独立呈现，计算逻辑在本地完成。",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "实用工具",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            ZhixingGlassSurface(
                backdrop = backdrop,
                material = calmMaterial,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ZhixingGlassShapes.card),
                shape = ZhixingGlassShapes.card,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ZhixingGlassShapes.card)
                        .clickable(role = Role.Button, onClick = onOpenUnitConverter)
                        .padding(horizontal = 17.dp, vertical = 17.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(15.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "⇄",
                                style = ZhixingTheme.type.screenTitle,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "单位换算",
                            style = ZhixingTheme.type.featureTitle,
                            color = ZhixingTheme.colors.onSurface,
                        )
                        Text(
                            text = "长度、面积、体积、质量、温度等九大类别",
                            style = ZhixingTheme.type.supporting,
                            color = ZhixingTheme.colors.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "›",
                        style = ZhixingTheme.type.sectionTitle,
                        color = ZhixingTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun UnitConverterScreen(
    backdrop: Backdrop,
    material: GlassMaterialState,
    onBack: () -> Unit,
) {
    var selectedCategoryId by rememberSaveable { mutableStateOf("length") }
    var sourceUnitId by rememberSaveable { mutableStateOf("m") }
    var targetUnitId by rememberSaveable { mutableStateOf("ft") }
    var inputText by rememberSaveable { mutableStateOf("") }
    var copiedResultKey by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val categories = UnitConversionPresenter.categories()
    val units = remember(selectedCategoryId) {
        UnitConversionPresenter.units(selectedCategoryId)
    }
    val sourceUnit = units.firstOrNull { it.id == sourceUnitId } ?: units.firstOrNull()
    val targetUnit = units.firstOrNull { it.id == targetUnitId }
        ?: units.getOrNull(1)
        ?: units.firstOrNull()
    val copyStateKey = listOf(
        selectedCategoryId,
        sourceUnit?.id.orEmpty(),
        targetUnit?.id.orEmpty(),
        inputText,
    ).joinToString(separator = "\u0000")
    val copied = copiedResultKey == copyStateKey
    val presentation = remember(inputText, selectedCategoryId, sourceUnit?.id, targetUnit?.id) {
        if (sourceUnit == null || targetUnit == null) {
            UnitConversionPresentation.InvalidInput
        } else {
            UnitConversionPresenter.present(
                inputText = inputText,
                categoryId = selectedCategoryId,
                sourceUnitId = sourceUnit.id,
                targetUnitId = targetUnit.id,
            )
        }
    }
    val calmMaterial = material.copy(
        refractionEnabled = false,
        chromaticDispersionEnabled = false,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ZhixingTheme.spacing.pageHorizontal)
            .padding(top = 18.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ZhixingLiquidButton(
                backdrop = backdrop,
                onClick = onBack,
                shape = ZhixingGlassShapes.pill,
            ) {
                Text(
                    text = "‹ 返回",
                    style = ZhixingTheme.type.featureTitle,
                    color = ZhixingTheme.colors.primary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "单位换算",
                    style = ZhixingTheme.type.screenTitle,
                    color = ZhixingTheme.colors.onBackground,
                )
                Text(
                    text = "选择类别与单位，输入后即时换算",
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "换算类别",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                categories.forEach { item ->
                    FilterChip(
                        selected = item.id == selectedCategoryId,
                        onClick = {
                            selectedCategoryId = item.id
                            val defaults = UnitConversionPresenter.defaults(item.id)
                            sourceUnitId = defaults?.first.orEmpty()
                            targetUnitId = defaults?.second.orEmpty()
                        },
                        label = { Text(item.displayName) },
                    )
                }
            }
        }

        UnitGlassPanel(backdrop = backdrop, material = calmMaterial) {
            Text(
                text = "输入数值",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onSurface,
            )
            OutlinedTextField(
                value = inputText,
                onValueChange = { next ->
                    // Retain one extra character so the presenter can show a clear limit error.
                    inputText = next.take(UnitConversionPresenter.MAX_INPUT_LENGTH + 1)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("数值") },
                placeholder = { Text("例如 12.5 或 1e-3") },
                supportingText = {
                    Text("支持正负数、小数和科学计数法，最多 64 个字符")
                },
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        TextButton(onClick = { inputText = "" }) {
                            Text("清除")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() },
                ),
                textStyle = ZhixingTheme.type.body,
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 340.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        UnitSelector(
                            label = "原单位",
                            options = units,
                            selected = sourceUnit,
                            modifier = Modifier.fillMaxWidth(),
                            onSelect = { sourceUnitId = it.id },
                        )
                        UnitSwapButton(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            compactLabel = true,
                            onClick = {
                                val oldSource = sourceUnitId
                                sourceUnitId = targetUnit?.id.orEmpty()
                                targetUnitId = oldSource
                            },
                        )
                        UnitSelector(
                            label = "目标单位",
                            options = units,
                            selected = targetUnit,
                            modifier = Modifier.fillMaxWidth(),
                            onSelect = { targetUnitId = it.id },
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        UnitSelector(
                            label = "原单位",
                            options = units,
                            selected = sourceUnit,
                            modifier = Modifier.weight(1f),
                            onSelect = { sourceUnitId = it.id },
                        )
                        UnitSwapButton(
                            modifier = Modifier.padding(bottom = 2.dp),
                            onClick = {
                                val oldSource = sourceUnitId
                                sourceUnitId = targetUnit?.id.orEmpty()
                                targetUnitId = oldSource
                            },
                        )
                        UnitSelector(
                            label = "目标单位",
                            options = units,
                            selected = targetUnit,
                            modifier = Modifier.weight(1f),
                            onSelect = { targetUnitId = it.id },
                        )
                    }
                }
            }
        }

        UnitGlassPanel(backdrop = backdrop, material = calmMaterial) {
            Text(
                text = "换算结果",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onSurface,
            )
            when (val state = presentation) {
                UnitConversionPresentation.EmptyInput -> {
                    Text(
                        text = "输入数值后自动显示结果",
                        style = ZhixingTheme.type.body,
                        color = ZhixingTheme.colors.onSurfaceVariant,
                    )
                }
                UnitConversionPresentation.IncompleteInput -> {
                    Text(
                        text = "数字尚未输入完整",
                        style = ZhixingTheme.type.body,
                        color = ZhixingTheme.colors.onSurfaceVariant,
                    )
                }
                UnitConversionPresentation.InvalidInput -> {
                    Text(
                        text = "请输入有效数字，支持正负数、小数和科学计数法",
                        style = ZhixingTheme.type.body,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                UnitConversionPresentation.InputTooLong -> {
                    Text(
                        text = "输入过长，请控制在 64 个字符以内",
                        style = ZhixingTheme.type.body,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                is UnitConversionPresentation.Failure -> {
                    Text(
                        text = unitConversionErrorMessage(state.error),
                        style = ZhixingTheme.type.body,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                is UnitConversionPresentation.Converted -> {
                    Text(
                        text = "${inputText.trim()} ${sourceUnit?.label.orEmpty()}",
                        style = ZhixingTheme.type.body,
                        color = ZhixingTheme.colors.onSurface,
                    )
                    Text(
                        text = UnitValueFormatter.format(state.value),
                        modifier = Modifier.semantics {
                            contentDescription = "换算结果：${UnitValueFormatter.format(state.value)} ${targetUnit?.label.orEmpty()}"
                        },
                        style = ZhixingTheme.type.screenTitle,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = targetUnit?.label.orEmpty(),
                        style = ZhixingTheme.type.supporting,
                        color = ZhixingTheme.colors.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = {
                            val text = UnitConversionPresenter.copyText(
                                inputText = inputText,
                                sourceUnitLabel = sourceUnit?.label.orEmpty(),
                                resultValue = state.value,
                                targetUnitLabel = targetUnit?.label.orEmpty(),
                            )
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                                as? ClipboardManager
                            if (clipboard != null) {
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("单位换算结果", text),
                                )
                                copiedResultKey = copyStateKey
                            }
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "复制完整换算结果"
                            stateDescription = if (copied) "已复制" else "尚未复制"
                        },
                    ) {
                        Text(if (copied) "已复制" else "复制结果")
                    }
                }
            }
        }

        if (presentation is UnitConversionPresentation.Converted) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(
                    text = "同类单位一览",
                    style = ZhixingTheme.type.sectionTitle,
                    color = ZhixingTheme.colors.onBackground,
                )
                ZhixingGlassSurface(
                    backdrop = backdrop,
                    material = calmMaterial,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ZhixingGlassShapes.card),
                    shape = ZhixingGlassShapes.card,
                ) {
                    Column(modifier = Modifier.padding(vertical = 5.dp)) {
                        presentation.rows.forEach { row ->
                            val selected = row.unit.id == targetUnit?.id
                            val valueDescription = when (val result = row.result) {
                                is UnitConversionResult.Success -> UnitValueFormatter.format(result.value)
                                is UnitConversionResult.Failure -> unitConversionErrorMessage(result.error)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (selected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f)
                                        } else {
                                            androidx.compose.ui.graphics.Color.Transparent
                                        },
                                    )
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "${row.unit.label}：$valueDescription"
                                        if (selected) stateDescription = "当前目标单位"
                                    }
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = row.unit.label,
                                        style = ZhixingTheme.type.featureTitle,
                                        color = ZhixingTheme.colors.onSurface,
                                    )
                                    if (selected) {
                                        Text(
                                            text = "当前目标单位",
                                            style = ZhixingTheme.type.supporting,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                when (val result = row.result) {
                                    is UnitConversionResult.Success -> {
                                        Text(
                                            text = UnitValueFormatter.format(result.value),
                                            style = ZhixingTheme.type.body,
                                            color = ZhixingTheme.colors.onSurface,
                                        )
                                    }
                                    is UnitConversionResult.Failure -> {
                                        Text(
                                            text = "超出范围",
                                            style = ZhixingTheme.type.supporting,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitGlassPanel(
    backdrop: Backdrop,
    material: GlassMaterialState,
    content: @Composable () -> Unit,
) {
    ZhixingGlassSurface(
        backdrop = backdrop,
        material = material,
        modifier = Modifier
            .fillMaxWidth()
            .clip(ZhixingGlassShapes.card),
        shape = ZhixingGlassShapes.card,
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun UnitSelector(
    label: String,
    options: List<UnitOption>,
    selected: UnitOption?,
    modifier: Modifier = Modifier,
    onSelect: (UnitOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            style = ZhixingTheme.type.supporting,
            color = ZhixingTheme.colors.onSurfaceVariant,
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "$label，当前${selected?.label ?: "未选择"}"
                    },
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = selected?.label ?: "选择单位",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ZhixingTheme.type.supporting,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .widthIn(max = 320.dp),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitSwapButton(
    modifier: Modifier = Modifier,
    compactLabel: Boolean = false,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = "交换原单位和目标单位"
            role = Role.Button
        },
    ) {
        Text(if (compactLabel) "交换单位" else "⇄")
    }
}

private fun unitConversionErrorMessage(error: UnitConversionError): String =
    when (error) {
        UnitConversionError.NonFiniteInput -> "请输入有限数值"
        is UnitConversionError.UnknownSourceCategory -> "找不到原单位类别"
        is UnitConversionError.UnknownTargetCategory -> "找不到目标单位类别"
        is UnitConversionError.IncompatibleDimensions,
        is UnitConversionError.CategoryMismatch -> "原单位与目标单位不属于同一换算类别"
        is UnitConversionError.UnsupportedCategory -> "暂不支持此单位类别"
        is UnitConversionError.UnknownSourceUnit -> "找不到原单位"
        is UnitConversionError.UnknownTargetUnit -> "找不到目标单位"
        UnitConversionError.BelowAbsoluteZero -> "温度不能低于绝对零度"
        UnitConversionError.NonFiniteIntermediateValue,
        UnitConversionError.NonFiniteResult -> "数值超出可计算范围"
        UnitConversionError.PrecisionUnderflow -> "结果过小，超出可显示精度"
    }
