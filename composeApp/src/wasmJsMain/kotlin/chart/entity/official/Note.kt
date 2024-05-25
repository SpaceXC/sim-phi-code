package entity.official


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    @SerialName("floorPosition")
    val floorPosition: Double,
    @SerialName("holdTime")
    val holdTime: Double,
    @SerialName("positionX")
    val positionX: Double,
    @SerialName("speed")
    val speed: Double,
    @SerialName("time")
    val time: Double,
    @SerialName("type")
    val type: Int
)