package entity.official


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhigrosChart(
    @SerialName("formatVersion")
    val formatVersion: Int,
    @SerialName("judgeLineList")
    val judgeLineList: List<JudgeLine>,
    @SerialName("offset")
    val offset: Double
)