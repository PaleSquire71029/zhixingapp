package ci.us.zhixing.app.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.navigation.AppNavigationIcon

/**
 * One geometric icon family for primary navigation and the My subpages.
 * Drawing on a 24dp grid keeps visual weight and alignment consistent.
 */
@Composable
internal fun ZhixingNavigationIcon(
    icon: AppNavigationIcon,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val line = if (selected) Color(0xFF4D7187) else Color(0xFF71818D)
    val activeFill = if (selected) Color(0xFFDCEBF4) else Color.Transparent

    Canvas(modifier = modifier.size(24.dp)) {
        val scaleX = size.width / 24f
        val scaleY = size.height / 24f
        withTransform({
            scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero)
        }) {
            when (icon) {
                AppNavigationIcon.CALCULATOR -> drawCalculatorIcon(line, activeFill)
                AppNavigationIcon.AI -> drawAiIcon(line, activeFill)
                AppNavigationIcon.TOOLS -> drawToolsIcon(line, activeFill)
                AppNavigationIcon.ME -> drawMeIcon(line, activeFill)
                AppNavigationIcon.HISTORY -> drawHistoryIcon(line, activeFill)
                AppNavigationIcon.MATERIAL -> drawMaterialIcon(line, activeFill)
            }
        }
    }
}

private val iconStroke: Stroke
    get() = Stroke(
        width = 1.85f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )

private fun DrawScope.drawCalculatorIcon(line: Color, fill: Color) {
    drawRoundRect(
        color = fill,
        topLeft = Offset(5.2f, 4.2f),
        size = Size(13.6f, 7.2f),
        cornerRadius = CornerRadius(2.1f),
    )
    drawRoundRect(
        color = line,
        topLeft = Offset(3.8f, 3.2f),
        size = Size(16.4f, 10.5f),
        cornerRadius = CornerRadius(2.6f),
        style = iconStroke,
    )
    drawLine(line, Offset(4.8f, 14.2f), Offset(19.2f, 14.2f), 1.65f, cap = StrokeCap.Round)
    listOf(7f, 12f, 17f).forEach { x ->
        drawCircle(color = line, radius = 0.95f, center = Offset(x, 18.2f))
    }
    drawLine(line, Offset(15.9f, 17.3f), Offset(18.1f, 19.1f), 1.3f, cap = StrokeCap.Round)
}

private fun DrawScope.drawAiIcon(line: Color, fill: Color) {
    val upper = Path().apply {
        moveTo(3.5f, 14.5f)
        cubicTo(5.0f, 8.8f, 8.0f, 5.6f, 12.0f, 5.6f)
        cubicTo(16.0f, 5.6f, 19.0f, 8.5f, 20.5f, 13.2f)
    }
    val lower = Path().apply {
        moveTo(4.1f, 16.0f)
        cubicTo(6.1f, 19.4f, 9.0f, 20.2f, 12.0f, 18.8f)
        cubicTo(14.0f, 17.9f, 16.1f, 16.1f, 19.8f, 15.0f)
    }
    drawPath(upper, line, style = iconStroke)
    drawPath(lower, line, style = iconStroke)
    drawCircle(fill, radius = 2.05f, center = Offset(12f, 12.2f))
    drawCircle(line, radius = 2.05f, center = Offset(12f, 12.2f), style = iconStroke)
    drawCircle(line, radius = 0.8f, center = Offset(5.0f, 15.7f))
    drawCircle(line, radius = 0.8f, center = Offset(19.0f, 13.8f))
}

private fun DrawScope.drawToolsIcon(line: Color, fill: Color) {
    drawRoundRect(fill, Offset(3.8f, 4.1f), Size(7.2f, 7.2f), CornerRadius(2.1f))
    drawRoundRect(line, Offset(3.8f, 4.1f), Size(7.2f, 7.2f), CornerRadius(2.1f), style = iconStroke)
    drawRoundRect(line, Offset(13.2f, 3.7f), Size(7.0f, 5.8f), CornerRadius(2.0f), style = iconStroke)
    drawRoundRect(line, Offset(4.2f, 14.0f), Size(5.8f, 6.0f), CornerRadius(1.9f), style = iconStroke)
    drawRoundRect(fill, Offset(12.1f, 11.7f), Size(8.2f, 8.2f), CornerRadius(2.3f))
    drawRoundRect(line, Offset(12.1f, 11.7f), Size(8.2f, 8.2f), CornerRadius(2.3f), style = iconStroke)
}

private fun DrawScope.drawMeIcon(line: Color, fill: Color) {
    drawCircle(fill, radius = 2.9f, center = Offset(12f, 7.8f))
    drawCircle(line, radius = 2.9f, center = Offset(12f, 7.8f), style = iconStroke)
    val shoulders = Path().apply {
        moveTo(4.0f, 20.0f)
        cubicTo(4.3f, 15.9f, 7.4f, 13.7f, 12.0f, 13.7f)
        cubicTo(16.6f, 13.7f, 19.7f, 15.9f, 20.0f, 20.0f)
    }
    drawPath(shoulders, line, style = iconStroke)
}

private fun DrawScope.drawHistoryIcon(line: Color, fill: Color) {
    drawCircle(fill, radius = 8.1f, center = Offset(12f, 12f))
    drawCircle(line, radius = 8.1f, center = Offset(12f, 12f), style = iconStroke)
    drawLine(line, Offset(12f, 7.2f), Offset(12f, 12.1f), 1.8f, cap = StrokeCap.Round)
    drawLine(line, Offset(12f, 12.1f), Offset(15.4f, 14.0f), 1.8f, cap = StrokeCap.Round)
    val returnArrow = Path().apply {
        moveTo(4.2f, 7.1f)
        lineTo(4.4f, 3.9f)
        lineTo(7.5f, 4.4f)
    }
    drawPath(returnArrow, line, style = iconStroke)
}

private fun DrawScope.drawMaterialIcon(line: Color, fill: Color) {
    val rows = listOf(6.0f, 12.0f, 18.0f)
    val knobs = listOf(9.0f, 15.0f, 7.0f)
    rows.forEachIndexed { index, y ->
        drawLine(line.copy(alpha = 0.78f), Offset(3.5f, y), Offset(20.5f, y), 1.65f, cap = StrokeCap.Round)
        drawCircle(fill, radius = 2.05f, center = Offset(knobs[index], y))
        drawCircle(line, radius = 2.05f, center = Offset(knobs[index], y), style = iconStroke)
    }
}
