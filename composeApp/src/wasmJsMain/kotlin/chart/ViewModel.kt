package chart

import androidx.compose.runtime.*
import currentTimeMillis
import entity.display.DisplayedJudgeLine
import entity.display.DisplayedNote
import entity.display.Keyframe
import entity.display.NotePosition
import entity.display.NoteType
import entity.official.Note
import entity.official.PhigrosChart
import entity.official.SpeedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import sim_phi_web.composeapp.generated.resources.Res

class ViewModel {
    private var chartName: String = ""

    //private var audioClip = AudioSystem.getClip()

    enum class Status {
        LOADING,
        IDLE,
        PLAYING,
        PAUSED,
        FINISHED
    }

    var comboCount by mutableIntStateOf(0)
    var score by mutableIntStateOf(0)

    var currentStatus by mutableStateOf(Status.IDLE)

    var chart: PhigrosChart? by mutableStateOf(null)
    var noteCount = 0

    var judgeLineBeat by mutableStateOf(HashMap<Int, Double>())

    var displayedJudgeLines by mutableStateOf(HashMap<Int, DisplayedJudgeLine>())

    var displayedNote by mutableStateOf(mapOf<Int, List<DisplayedNote>>())

    private var startTime = currentTimeMillis()
    var progress = flow {
        while (true) {
            if (currentStatus == Status.PLAYING) {
                emit(currentTimeMillis() - startTime)
            }
            if(currentStatus == Status.IDLE) {
                emit(100)
            }
            kotlinx.coroutines.delay(5)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        explicitNulls = true
        ignoreUnknownKeys = true
    }

    fun clear() {
        chartName = ""
        currentStatus = Status.LOADING
        comboCount = 0
        score = 0
        chart = null
        noteCount = 0
        judgeLineBeat = HashMap()
        displayedNote = HashMap()
        displayedJudgeLines = HashMap()

    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun initChart(
        name: String
    ) {
        processChart(Res.readBytes("files/Charts/$name/$name.json"), name)
    }

    private fun processChart(chartFile: ByteArray, name: String) {
        //audioClip.stop()
        clear()
        chartName = name
        chart = json.decodeFromString<PhigrosChart>(chartFile.decodeToString())
        val allNotes = chart!!.judgeLineList.flatMap { it.notesBelow + it.notesAbove }
        noteCount = allNotes.size
        displayedNote = buildList {
            chart?.judgeLineList?.forEachIndexed { index, line ->
                line.notesAbove.forEach { note ->
                    val hasDoubleHit = hasDoubleHit(allNotes, note.time)
                    val bitmap = if (hasDoubleHit) {
                        when (note.type) {
                            1 -> tapDoubleBitmap
                            2 -> dragDoubleBitmap
                            4 -> flickDoubleBitmap
                            else -> null
                        }
                    } else {
                        when (note.type) {
                            1 -> tapBitmap
                            2 -> dragBitmap
                            4 -> flickBitmap
                            else -> null
                        }
                    }
                    val displayedNote = DisplayedNote(
                        orig = note,
                        index = index,
                        type = when (note.type) {
                            1 -> NoteType.Tap
                            2 -> NoteType.Drag
                            3 -> NoteType.Hold
                            4 -> NoteType.Flick
                            else -> throw RuntimeException("Unknown Note Type")
                        },
                        judgeLineIndex = index,
                        position = NotePosition.Above,
                        timePosition = note.time.realTime(line.bpm),
                        endTimePosition = (note.time + note.holdTime).realTime(line.bpm),
                        positionX = (note.positionX * (2f * 9f / 160f)).toFloat() / 2,  //Phira Magic Number
                        hasDoubleHit = hasDoubleHit,
                        speed = note.speed * 0.6,
                        heightKeyframes = processHoldHeightEvent(line.speedEvents, note, line.bpm),
                        bitmap = bitmap
                    )
                    add(displayedNote)
                }
                line.notesBelow.forEach { note ->
                    val hasDoubleHit = hasDoubleHit(allNotes, note.time)
                    val bitmap = if (hasDoubleHit) {
                        when (note.type) {
                            1 -> tapDoubleBitmap
                            2 -> dragDoubleBitmap
                            4 -> flickDoubleBitmap
                            else -> null
                        }
                    } else {
                        when (note.type) {
                            1 -> tapBitmap
                            2 -> dragBitmap
                            4 -> flickBitmap
                            else -> null
                        }
                    }
                    val displayedNote = DisplayedNote(
                        orig = note,
                        index = index,
                        type = when (note.type) {
                            1 -> NoteType.Tap
                            2 -> NoteType.Drag
                            3 -> NoteType.Hold
                            4 -> NoteType.Flick
                            else -> throw RuntimeException("Unknown Note Type")
                        },
                        judgeLineIndex = index,
                        position = NotePosition.Below,
                        timePosition = note.time.realTime(line.bpm),
                        endTimePosition = (note.time + note.holdTime).realTime(line.bpm),
                        positionX = (note.positionX * (2f * 9f / 160f)).toFloat() / 2,
                        hasDoubleHit = hasDoubleHit,
                        speed = note.speed * 0.6,
                        heightKeyframes = processHoldHeightEvent(line.speedEvents, note, line.bpm),
                        bitmap = bitmap
                    )
                    add(displayedNote)
                }
            }
        }.sortedBy { it.timePosition }.mapIndexed { index, note -> note.copy(index = index) }
            .groupBy { it.judgeLineIndex }
        displayedNote
        displayedJudgeLines = HashMap<Int, DisplayedJudgeLine>().apply {
            chart?.judgeLineList?.forEachIndexed { index, line ->
                put(index, DisplayedJudgeLine(index, 0.5, 0.5, 0.0, 0.0))
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            progress.collect { currentProgress ->
                updateBeat(currentProgress)
                updateLinePlacement(currentProgress)
            }
        }

        currentStatus = Status.IDLE
    }

    private fun hasDoubleHit(allNotes: List<Note>, time: Double): Boolean {
        return allNotes.filter { it.time == time }.size >= 2
    }

    fun calculateScore() {
        val perfectCount = comboCount.toFloat()
        val judge = perfectCount / noteCount.toFloat() * 900000
        val combo = comboCount / noteCount.toFloat() * 100000
        score = (judge + combo).toInt()
    }

    private fun processHoldHeightEvent(
        speedEvent: List<SpeedEvent>,
        note: Note,
        bpm: Double
    ): List<Keyframe> {
        return buildList {
            if (speedEvent.isEmpty()) {
                add(
                    Keyframe(
                        0.0, note.time.realTime(bpm), note.time.realTime(bpm) * 0.6, 0.0
                    )
                )
            }
            if (note.type == 3) {
                add(Keyframe(
                    note.time.realTime(bpm),
                    (note.time + note.holdTime).realTime(bpm),
                    0.0,
                    -(note.holdTime.realTime(bpm) * note.speed) * 0.6
                ))
            }
            val targetIndex = speedEvent.indexOfFirst { note.time in it.startTime..it.endTime }
            if (targetIndex == -1)
                return@buildList
            var targetHeight = 0.0
            for (event in speedEvent.subList(0, targetIndex + 1).asReversed()) {    //end-exclusive
                val startTime = event.startTime.realTime(bpm)
                val endTime =
                    (if (note.time in event.startTime..event.endTime) note.time else event.endTime).realTime(bpm)
                val delta = (endTime - startTime) * event.value * 0.6// * 0.6f
                add(Keyframe(startTime, endTime, targetHeight + delta, targetHeight))
                targetHeight += delta
            }
        }.asReversed()
    }

    private fun updateBeat(currentProgress: Long) {
        chart!!.judgeLineList.forEachIndexed { index, judgeLine ->
            val currentBeat = currentProgress.toDouble() / (60000.toDouble() / judgeLine.bpm)
            val tempMap = judgeLineBeat
            tempMap[index] = currentBeat
            judgeLineBeat = tempMap
        }
    }

    private fun updateLinePlacement(timeProgress: Long) {
        val tempMap = displayedJudgeLines
        chart!!.judgeLineList.forEachIndexed { index, judgeLine ->
            val currentProgress = timeProgress.toDouble()
            val speedFactor = (60f / judgeLine.bpm / 32f) * 1000
            judgeLine.judgeLineMoveEvents.find { currentProgress in it.startTime * speedFactor..it.endTime * speedFactor }
                ?.let { currentMove ->
                    val line = tempMap[index] ?: DisplayedJudgeLine(
                        index = index,
                        offsetX = 0.toDouble(),
                        offsetY = 0.toDouble(),
                        rotation = 0.toDouble(),
                        alpha = 1.toDouble()
                    )

                    val startTime = currentMove.startTime * speedFactor
                    val endTime = currentMove.endTime * speedFactor
                    val elapsedTime = currentProgress - startTime
                    val deltaYPerUnitTime = (currentMove.end2 - currentMove.start2) / (endTime - startTime)
                    val deltaY = elapsedTime * deltaYPerUnitTime
                    val deltaXPerUnitTime = (currentMove.end - currentMove.start) / (endTime - startTime)
                    val deltaX = elapsedTime * deltaXPerUnitTime
                    val currentOffsetX = currentMove.start + deltaX
                    val currentOffsetY = currentMove.start2 + deltaY
                    tempMap[index] = line.copy(offsetY = currentOffsetY, offsetX = currentOffsetX)
                }
            judgeLine.judgeLineDisappearEvents.find { currentProgress in it.startTime * speedFactor..it.endTime * speedFactor }
                ?.let { currentVisibility ->
                    val line = tempMap[index] ?: DisplayedJudgeLine(
                        index = index,
                        offsetX = 0.toDouble(),
                        offsetY = 0.toDouble(),
                        rotation = 0.toDouble(),
                        alpha = 1.toDouble()
                    )
                    val startTime = currentVisibility.startTime * speedFactor
                    val endTime = currentVisibility.endTime * speedFactor
                    val elapsedTime = currentProgress - startTime
                    val deltaPerUnitTime = (currentVisibility.end - currentVisibility.start) / (endTime - startTime)
                    val delta = elapsedTime * deltaPerUnitTime
                    val currentAlpha = currentVisibility.start + delta
                    tempMap[index] = line.copy(alpha = currentAlpha)
                }
            judgeLine.judgeLineRotateEvents.find { currentProgress in it.startTime * speedFactor..it.endTime * speedFactor }
                ?.let { currentRotation ->
                    val line = tempMap[index] ?: DisplayedJudgeLine(
                        index = index,
                        offsetX = 0.toDouble(),
                        offsetY = 0.toDouble(),
                        rotation = 0.toDouble(),
                        alpha = 1.toDouble()
                    )
                    val startTime = currentRotation.startTime * speedFactor
                    val endTime = currentRotation.endTime * speedFactor
                    val elapsedTime = currentProgress - startTime
                    val deltaPerUnitTime = (currentRotation.end - currentRotation.start) / (endTime - startTime)
                    val delta = elapsedTime * deltaPerUnitTime
                    val currentDegrees = currentRotation.start + delta
                    tempMap[index] = line.copy(rotation = currentDegrees)
                }
        }
        displayedJudgeLines = tempMap
    }


    fun start(withAudio: Boolean = false) {
        try {
            /*audioClip.stop()
            if(withAudio) {
                val audioInputStream =
                    AudioSystem.getAudioInputStream(File("file.Charts/$chartName/$chartName.wav"))

                val clip = AudioSystem.getClip()
                clip.open(audioInputStream)
                audioClip = clip
                clip.start()
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
        comboCount = 0
        score = 0
        currentStatus = Status.PLAYING
        startTime = currentTimeMillis()
    }

    fun prepare() {

        comboCount = 0
        score = 0
        currentStatus = Status.LOADING
        startTime = currentTimeMillis()
        DefaultMonotonicFrameClock
    }

    fun pause() {
        currentStatus = Status.PAUSED

    }

    fun stop() {
        //audioClip.stop()
    }

    fun resume() {
        currentStatus = Status.PLAYING

    }

    fun Double.realTime(bpm: Double) = this * (60f / bpm / 32f) * 1000
}