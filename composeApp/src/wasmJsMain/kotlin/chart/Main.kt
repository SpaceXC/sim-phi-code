package chart

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import entity.ChartInfo
import entity.SongInfo
import entity.display.NotePosition
import entity.display.NoteType
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import phigrosFontFamily
import sim_phi_web.composeapp.generated.resources.Res
import toImageBitmap
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

const val DEGREES_TO_RADIANS = 0.017453292519943295

fun toRadians(degree: Double) = degree * DEGREES_TO_RADIANS

val PerfectJudgeLineColor = Color(250, 250, 180)
val GoodJudgeLineColor = Color(190, 233, 240)
val JudgeLineColor = Color.White

lateinit var headerBitmap: ImageBitmap
lateinit var tailBitmap: ImageBitmap
lateinit var bodyBitmap: ImageBitmap
lateinit var headerDoubleBitmap: ImageBitmap
lateinit var tailDoubleBitmap: ImageBitmap
lateinit var bodyDoubleBitmap: ImageBitmap

lateinit var tapBitmap: ImageBitmap
lateinit var tapDoubleBitmap: ImageBitmap
lateinit var dragBitmap: ImageBitmap
lateinit var dragDoubleBitmap: ImageBitmap
lateinit var flickBitmap: ImageBitmap
lateinit var flickDoubleBitmap: ImageBitmap

lateinit var vfxBitmap: ImageBitmap

suspend fun initBitmaps() {
    headerBitmap = loadBitmapFromFile("StaticAssets/HoldTextures/HoldHeader.png")
    tailBitmap = loadBitmapFromFile("StaticAssets/HoldTextures/HoldTail.png")
    bodyBitmap = loadBitmapFromFile("StaticAssets/HoldTextures/HoldBody.png")
    headerDoubleBitmap = loadBitmapFromFile("StaticAssets/HoldTextures/HoldHeaderDouble.png")
    tailDoubleBitmap = loadBitmapFromFile("StaticAssets/HoldTextures/HoldTailDouble.png")
    bodyDoubleBitmap = loadBitmapFromFile("StaticAssets/HoldTextures/HoldBodyDouble.png")
    tapBitmap = loadBitmapFromFile("StaticAssets/Tap.png")
    tapDoubleBitmap = loadBitmapFromFile("StaticAssets/TapDouble.png")
    dragBitmap = loadBitmapFromFile("StaticAssets/Drag.png")
    dragDoubleBitmap = loadBitmapFromFile("StaticAssets/DragDouble.png")
    flickBitmap = loadBitmapFromFile("StaticAssets/Flick.png")
    flickDoubleBitmap = loadBitmapFromFile("StaticAssets/FlickDouble.png")
    vfxBitmap = loadBitmapFromFile("StaticAssets/hit_fx.png")
}

@Composable
fun ChartRenderer(
    viewModel: ViewModel,
    songName: String,
    backgroundBitmap: ImageBitmap?,
    aspectRatio: Float,
    onBack: () -> Unit
) {
    val currentProgress by viewModel.progress.collectAsState(0L)
    val localDensity = LocalDensity.current
    var size by remember { mutableStateOf(Size(1f, 1f)) }   //Base 1600x1200
    val dpSize by derivedStateOf { with(localDensity) { size.toDpSize() } }

    val sizeScale = size.width / 1600f
    val noteSize = 100.dp * sizeScale


    /*val size by remember {
        mutableStateOf(IntSize(1600, 900))
    }*/

    var enterAnimation by remember { mutableStateOf(false) }
    val enterLineLength by animateFloatAsState(
        targetValue = if (enterAnimation) 5000f else 0f,
        tween(1000)
    )
    var isEnterLineVisible by remember { mutableStateOf(true) }
    val enterLineAlpha by animateFloatAsState(if (isEnterLineVisible) 1f else 0f, tween(1000))
    var isPrepared by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        backgroundBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.5f).aspectRatio(aspectRatio).align(Alignment.Center).blur(15.dp).alpha(0.6f),
                contentScale = ContentScale.FillHeight
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(0.5f).aspectRatio(aspectRatio)/*.background(Color.Black)*///.fillMaxHeight().aspectRatio(aspectRatio)
                .onSizeChanged { size = it.toSize() }.align(Alignment.Center)
            //.border(4.dp, Color(255, 255, 255, 128))
        ) {
            Text(
                currentProgress.toFloat().div(1000).toString(),
                fontFamily = phigrosFontFamily(),
                color = Color.White
            )

            Canvas(modifier = Modifier.fillMaxSize()/*.fillMaxSize()*/) {
                viewModel.displayedJudgeLines.forEach { (_, line) ->
                    val offsetX =
                        (line.offsetX).toFloat() * size.width
                    val offsetY =
                        (1 - line.offsetY).toFloat() * size.height
                    val alpha =
                        line.alpha.toFloat()
                    val rotation =
                        -line.rotation.toFloat()

                    drawRotatedLine(
                        offsetX,
                        offsetY,
                        rotation,
                        PerfectJudgeLineColor,
                        6f * sizeScale,
                        if (isEnterLineVisible) 0f else alpha,//.coerceIn(0.3f..0.8f).coerceAtLeast(0.3f)
                    )
                }

                viewModel.displayedJudgeLines[0]?.let { line ->
                    val offsetX =
                        (line.offsetX).toFloat() * size.width
                    val offsetY =
                        (1 - line.offsetY).toFloat() * size.height
                    val alpha =
                        line.alpha.toFloat()
                    val rotation =
                        -line.rotation.toFloat()
                    drawRotatedLine(
                        offsetX,
                        offsetY,
                        rotation,
                        PerfectJudgeLineColor,
                        6f * sizeScale,
                        enterLineAlpha,
                        enterLineLength
                    )
                }
                //chart.drawRotatedLine(0.5f * size.width, 0.5f * size.height, 0f, chart.getPerfectJudgeLineColor, 6f, enterLineAlpha, enterLineLength)
            }

            if (isPrepared) {
                viewModel.displayedNote.entries.forEach { (lineIndex, notes) ->
                    viewModel.displayedJudgeLines[lineIndex]?.let { line ->
                        val offsetX =
                            (line.offsetX - 0.5).toFloat() * size.width
                        val offsetY =
                            (1 - line.offsetY).toFloat() * size.height
                        val rotation =
                            -line.rotation.toFloat()
                        Box(
                            modifier = Modifier.graphicsLayer {
                                transformOrigin = TransformOrigin(0.5f, 0f)
                                rotationZ = rotation
                                translationY = offsetY
                                translationX = offsetX
                            }
                        ) {
                            Box(
                                modifier = Modifier.alpha(line.alpha.toFloat().coerceAtLeast(0.4f))
                                    .height(4.dp)
                                    .fillMaxWidth().background(Color.Transparent)
                            )
                            notes.forEach { note ->
                                if (note.endTimePosition > currentProgress) {
                                    note.heightKeyframes.find { currentProgress.toDouble() in it.startTime..it.endTime }
                                        ?.apply {
                                            val deltaPerTime =
                                                (endValue - startValue) / (endTime - startTime)
                                            val elapsedTime = currentProgress.toDouble() - startTime
                                            val currentDelta = elapsedTime * deltaPerTime
                                            val currentValue = startValue + currentDelta

                                            val currentY = with(localDensity) {
                                                currentValue.toFloat()
                                                    .toDp() * if (note.position == NotePosition.Above) -1 else 1
                                            } * sizeScale



                                            if (note.type == NoteType.Hold) {
                                                val holdTime =
                                                    (note.endTimePosition - note.timePosition)
                                                val totalHeightPx =
                                                    note.speed * holdTime //* sizeScale
                                                val totalHeight = with(localDensity) {
                                                    totalHeightPx.toFloat().toDp()
                                                } * sizeScale
                                                val currentHoldTime =
                                                    (note.endTimePosition - currentProgress.toDouble()).coerceIn(
                                                        0.0..holdTime
                                                    )
                                                val height =
                                                    (currentHoldTime / holdTime) * totalHeight


                                                Box(
                                                    Modifier.offset(
                                                        x = (note.positionX + 0.5f) * dpSize.width - noteSize / 2,
                                                        y = (currentY - if (note.position == NotePosition.Above) totalHeight else (height - totalHeight))
                                                    ).width(noteSize)
                                                        .alpha(if (note.endTimePosition > currentProgress) 1f else 0.3f)//.rotate(if(note.hasDoubleHit) 180f else 0f)
                                                        .drawBehind {
                                                            drawHold(
                                                                localDensity,
                                                                note.hasDoubleHit,
                                                                noteSize,
                                                                totalHeight,
                                                                height,
                                                                note.position
                                                            )
                                                        }
                                                )
                                            } else {
                                                val doubleScaleRatio =
                                                    if (note.hasDoubleHit) 1.0756661f else 1f

                                                note.bitmap?.let {
                                                    Image(
                                                        it,
                                                        null,
                                                        Modifier.offset(
                                                            x = (note.positionX + 0.5f) * dpSize.width - noteSize / 2 * doubleScaleRatio,
                                                            y = (currentY - 10.dp * doubleScaleRatio * sizeScale) //* sizeScale
                                                        ).width(noteSize * doubleScaleRatio)
                                                            .height(20.dp * doubleScaleRatio * sizeScale),
                                                        contentScale = ContentScale.FillWidth
                                                    )
                                                }
                                            }

                                        }
                                }

                                if ((currentProgress - note.endTimePosition) in 0f..400f || currentProgress.toDouble() in note.timePosition..note.endTimePosition) {
                                    val interval = 150
                                    val vfxAmount =
                                        (note.endTimePosition - note.timePosition).toInt() / interval + 1
                                    repeat(vfxAmount) {
                                        HitVfx(
                                            modifier = Modifier.offset(y = (-55).dp * sizeScale)
                                                .offset(x = (note.positionX + 0.5f) * dpSize.width - 55.dp * sizeScale),
                                            delay = it * interval.toLong(),
                                            vfxSize = 110.dp * sizeScale
                                        )
                                    }
                                    LaunchedEffect(key1 = Unit) {
                                        delay(
                                            (note.endTimePosition - note.timePosition - 200).toLong()
                                                .coerceAtLeast(0L)
                                        )
                                        viewModel.comboCount++
                                        viewModel.calculateScore()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.size(DpSize(1200.dp, 1600.dp))//fillMaxSize()
                    .padding(top = 8.dp, bottom = 14.dp, start = 14.dp, end = 14.dp)
            ) {
                var comboCountHeight by remember { mutableStateOf(0.dp) }
                AnimatedVisibility(
                    enterAnimation,
                    enter = slideInVertically(tween(500)) + fadeIn(tween(500)),
                    exit = slideOutVertically(tween(500)) + fadeOut(tween(500))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(comboCountHeight),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            tint = Color.White,
                            modifier = Modifier.alpha(0.9f).size(30.dp * sizeScale).clickable {
                                onBack()
                            },
                            contentDescription = null
                        )
                        Spacer(Modifier.weight(1f))

                        Text(
                            text = viewModel.score.asMillion(),
                            fontFamily = phigrosFontFamily(),
                            fontSize = 30.sp * sizeScale,
                            color = Color.White,
                            modifier = Modifier
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().onSizeChanged {
                        comboCountHeight = with(localDensity) { it.height.toDp() }
                    }.alpha(if (viewModel.comboCount >= 3) 1f else 0f)
                ) {
                    Text(
                        text = viewModel.comboCount.toString(),
                        fontFamily = phigrosFontFamily(),
                        fontSize = 34.sp * sizeScale,
                        color = Color.White
                    )
                    Text(
                        text = "AUTOPLAY",
                        fontFamily = phigrosFontFamily(),
                        fontSize = 14.sp * sizeScale,
                        color = Color.White
                    )
                }
                AnimatedVisibility(
                    enterAnimation,
                    enter = slideInVertically(tween(500)) { it / 2 } + fadeIn(tween(500)),
                    exit = slideOutVertically(tween(500)) { it / 2 } + fadeOut(
                        tween(500)
                    ),
                    modifier = Modifier.align(Alignment.BottomCenter)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = songName,
                            fontFamily = phigrosFontFamily(),
                            fontSize = 18.sp * sizeScale,
                            color = Color.White,
                            modifier = Modifier
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "SP Lv.?",
                            fontFamily = phigrosFontFamily(),
                            fontSize = 18.sp * sizeScale,
                            color = Color.White,
                            modifier = Modifier
                        )

                    }
                }
            }
        }
    }

    LaunchedEffect(songName) {
        enterAnimation = false
        isEnterLineVisible = true
        isPrepared = true
        //isPrepared = false
        //delay(400)
        enterAnimation = true
        delay(1300)
        isEnterLineVisible = false
        //delay(800)
        viewModel.start()

        //isPrepared = true
    }
}

fun DrawScope.drawRotatedLine(
    centerX: Float,
    centerY: Float,
    angle: Float,
    color: Color,
    lineWidth: Float,
    alpha: Float,
    largeLineLength: Float = 999999f// Use a large but finite line length (adjust as needed)
) {

    val radians = toRadians(angle.toDouble())
    val cosTheta = cos(radians).toFloat()
    val sinTheta = sin(radians).toFloat()

    val halfLineLengthX = largeLineLength * cosTheta / 2f
    val halfLineLengthY = largeLineLength * sinTheta / 2f

    val startX = centerX - halfLineLengthX
    val startY = centerY - halfLineLengthY
    val endX = centerX + halfLineLengthX
    val endY = centerY + halfLineLengthY

    with(this) {
        drawLine(
            color = color,
            Offset(startX, startY),
            Offset(endX, endY),
            strokeWidth = lineWidth,
            alpha = alpha.coerceAtMost(1f)
        )
    }
}

fun DrawScope.drawHold(
    localDensity: Density,
    isDouble: Boolean,
    width: Dp,
    height: Dp,
    currentHeight: Dp,
    notePosition: NotePosition
) {
    val doubleScaleRatio = if (isDouble) 1.0756661 else 1.0

    val endSize = IntSize(989, 50)

    val widthPx = with(localDensity) { width.toPx() * doubleScaleRatio }
    val originalHeightPx = with(localDensity) { height.toPx().roundToInt() }
    val heightPx = with(localDensity) { currentHeight.toPx().roundToInt() }
    val currentHeightPx = with(localDensity) { currentHeight.toPx().roundToInt() }

    val delta = originalHeightPx - currentHeightPx

    val endWidth = widthPx.toInt()
    val originalEndHeight = (widthPx / endSize.width * endSize.height).toInt().coerceAtLeast(0)
    val headerHeight =
        (widthPx / headerBitmap.width * endSize.height - delta).toInt().coerceAtLeast(0)
    val bodyHeight = (heightPx - originalEndHeight - headerHeight).coerceAtLeast(0)
    val tailHeight =
        currentHeightPx.coerceAtMost(originalEndHeight)//(widthPx / chart.getHeaderBitmap.width * endSize.height ).toInt().coerceAtLeast(0)


    val header = if (isDouble) headerDoubleBitmap else headerBitmap
    val body = if (isDouble) bodyDoubleBitmap else bodyBitmap
    val tail = if (isDouble) tailDoubleBitmap else tailBitmap


    rotate(if (notePosition == NotePosition.Above) 0f else 180f) {
        drawImage(
            image = /*if (notePosition == NotePosition.Above) header else tail*/header,
            dstSize = IntSize(endWidth, headerHeight),
            dstOffset = IntOffset(
                x = ((1.0 - doubleScaleRatio) * 0.5 * widthPx).toInt(),
                y = heightPx - headerHeight - if (notePosition == NotePosition.Below) currentHeightPx else 0
            ),
        )
    }
    rotate(if (notePosition == NotePosition.Above) 0f else 180f) {
        drawImage(
            image = body,
            dstSize = IntSize(endWidth, bodyHeight),
            dstOffset = IntOffset(
                x = ((1.0 - doubleScaleRatio) * 0.5 * widthPx).toInt(),
                y = heightPx - bodyHeight - headerHeight - if (notePosition == NotePosition.Below) currentHeightPx else 0
            )
        )
    }
    rotate(if (notePosition == NotePosition.Above) 0f else 180f) {
        drawImage(
            image = /*if (notePosition == NotePosition.Above) tail else header*/tail,
            dstSize = IntSize(endWidth, tailHeight),
            dstOffset = IntOffset(
                x = ((1.0 - doubleScaleRatio) * 0.5 * widthPx).toInt(),
                y = -if (notePosition == NotePosition.Below) currentHeightPx else 0
            )
        )
    }
}

@Composable
fun HoldDemo() {
    val localDensity = LocalDensity.current
    val height = 400.dp
    val width = 100.dp

    var currentHeight by remember {
        mutableStateOf(400.dp)
    }

    Column {

        Slider(
            currentHeight.value,
            onValueChange = { currentHeight = it.dp },
            valueRange = 0f..400f
        )

        Box(Modifier.fillMaxWidth().weight(1f).background(Color.DarkGray)) {


            Row(modifier = Modifier.align(Alignment.Center)) {

                Box(
                    modifier = Modifier.width(width).height(height).border(1.dp, Color.Red)
                        .drawBehind {
                            drawHold(
                                localDensity,
                                false,
                                width,
                                height,
                                currentHeight,
                                NotePosition.Above
                            )
                        })
                Box(
                    modifier = Modifier.width(width).height(height).border(1.dp, Color.Red)
                        .drawBehind {
                            drawHold(
                                localDensity,
                                true,
                                width,
                                height,
                                currentHeight,
                                NotePosition.Above
                            )
                            /*val endWidth = (widthPx * doubleScaleRatio).toInt()
                            val endHeight = ((widthPx / chart.getHeaderDoubleBitmap.width * endSize.height)).toInt()
                            val bodyHeight = ((heightPx - endHeight * 2))

                            drawImage(
                                image = chart.getHeaderDoubleBitmap,
                                dstSize = IntSize(endWidth, endHeight),
                                dstOffset = IntOffset(
                                    x = ((1 - doubleScaleRatio) * 0.5 * widthPx).toInt(),
                                    y = bodyHeight + endHeight + delta
                                )
                            )
                            drawImage(
                                image = chart.getBodyDoubleBitmap,
                                dstSize = IntSize(endWidth, bodyHeight),
                                dstOffset = IntOffset(
                                    x = ((1 - doubleScaleRatio) * 0.5 * widthPx).toInt(),
                                    y = endHeight + delta
                                )
                            )
                            drawImage(
                                image = chart.getTailDoubleBitmap,
                                dstSize = IntSize(endWidth, endHeight),
                                dstOffset = IntOffset(
                                    x = ((1 - doubleScaleRatio) * 0.5 * widthPx).toInt(),
                                    y = delta
                                )
                            )*/
                        })
            }
        }
    }
}

@Composable
fun VfxDemo() {
    var isTriggered by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            isTriggered.let {
                HitVfx()
            }
            Button(onClick = {
                isTriggered = !isTriggered
            }) {
                Text("trigger")
            }
        }
    }
}

fun Number.asMillion(): String {
    val originalString = toString()
    val zerosLeft = 7 - originalString.length
    return buildString {
        repeat(zerosLeft) {
            append('0')
        }
        append(originalString)
    }
}

@Composable
fun HitVfx(modifier: Modifier = Modifier, vfxSize: Dp = 100.dp, delay: Long = 0) {
    var isTriggered by remember { mutableStateOf(false) }
    val height = vfxBitmap.height / 6
    val width = vfxBitmap.width / 5
    val index by animateIntAsState(
        if (isTriggered) 30 else 0,
        animationSpec = tween(350, easing = LinearEasing)
    )
    val y = (index - 1) / 5
    val x = (index - 1) % 5
    Box(modifier.size(vfxSize).drawBehind {
        drawImage(
            vfxBitmap,
            srcSize = IntSize(width, height),
            srcOffset = IntOffset(x * width, y * height),
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            colorFilter = ColorFilter.tint(PerfectJudgeLineColor)
        )
    })
    LaunchedEffect(Unit) {
        delay(delay)
        isTriggered = true
    }
}

@OptIn(ExperimentalResourceApi::class)
suspend fun loadBitmapFromFile(fileName: String): ImageBitmap {
    return Res.readBytes("files/$fileName").toImageBitmap()
}


@Serializable
data class SongList(
    val chartMaps: Map<String, ChartInfo>,
    val songs: Map<String, SongInfo>
)