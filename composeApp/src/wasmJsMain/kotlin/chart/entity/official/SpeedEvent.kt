package entity.official


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpeedEvent(
    @SerialName("endTime")
    val endTime: Double,
    @SerialName("startTime")
    val startTime: Double,
    @SerialName("value")
    val value: Double
)