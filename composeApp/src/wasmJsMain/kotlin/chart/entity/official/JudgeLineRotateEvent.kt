package entity.official


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JudgeLineRotateEvent(
    @SerialName("end")
    val end: Double,
    /*@SerialName("end2")
    val end2: Double,*/
    @SerialName("endTime")
    val endTime: Double,
    @SerialName("start")
    val start: Double,
    /*@SerialName("start2")
    val start2: Double,*/
    @SerialName("startTime")
    val startTime: Double
)