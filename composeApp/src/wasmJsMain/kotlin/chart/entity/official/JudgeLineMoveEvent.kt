package entity.official


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JudgeLineMoveEvent(
    @SerialName("end")
    val end: Double = 0.0,
    @SerialName("end2")
    val end2: Double = 0.0,
    @SerialName("endTime")
    val endTime: Double = 0.0,
    @SerialName("start")
    val start: Double = 0.0,
    @SerialName("start2")
    val start2: Double = 0.0,
    @SerialName("startTime")
    val startTime: Double = 0.0
)