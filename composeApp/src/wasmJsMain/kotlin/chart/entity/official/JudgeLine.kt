package entity.official


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JudgeLine(
    @SerialName("bpm")
    val bpm: Double,
    @SerialName("judgeLineDisappearEvents")
    val judgeLineDisappearEvents: List<JudgeLineDisappearEvent>,
    @SerialName("judgeLineMoveEvents")
    val judgeLineMoveEvents: List<JudgeLineMoveEvent>,
    @SerialName("judgeLineRotateEvents")
    val judgeLineRotateEvents: List<JudgeLineRotateEvent>,
    @SerialName("notesAbove")
    val notesAbove: List<Note>,
    @SerialName("notesBelow")
    val notesBelow: List<Note>,
    @SerialName("speedEvents")
    val speedEvents: List<SpeedEvent>
)