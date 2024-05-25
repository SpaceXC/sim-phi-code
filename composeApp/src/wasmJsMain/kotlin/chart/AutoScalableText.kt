package cn.spacexc.wearbili.remake.common.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

/**
 * Created by XC-Qan on 2023/4/14.
 * I'm very cute so please be nice to my code!
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 */

@Composable
fun AutoResizedText(
    modifier: Modifier = Modifier,
    text: String,
    fontWeight: FontWeight = FontWeight.Normal,
    style: TextStyle = MaterialTheme.typography.body1,
    maxLines: Int = 1,
    isHeightOverflow: Boolean = false,
    color: Color = style.color
) {
    var resizedTextStyle by remember {
        mutableStateOf(style)
    }
    var shouldDraw by remember {
        mutableStateOf(false)
    }

    val defaultFontSize = MaterialTheme.typography.body1.fontSize

    Text(
        text = text,
        color = color,
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        softWrap = false,
        fontWeight = fontWeight,
        //overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        style = resizedTextStyle,
        onTextLayout = { result ->
            if (isHeightOverflow) {
                if (result.didOverflowHeight) {
                    if (style.fontSize.isUnspecified) {
                        resizedTextStyle = resizedTextStyle.copy(
                            fontSize = defaultFontSize
                        )
                    }
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = resizedTextStyle.fontSize * 0.95
                    )
                } else {
                    shouldDraw = true
                }
            } else {
                if (result.didOverflowWidth && resizedTextStyle.fontSize >= 13.sp) {
                    if (style.fontSize.isUnspecified) {
                        resizedTextStyle = resizedTextStyle.copy(
                            fontSize = defaultFontSize
                        )
                    }
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = resizedTextStyle.fontSize * 0.95
                    )
                } else {
                    shouldDraw = true
                }
            }
        }
    )
}