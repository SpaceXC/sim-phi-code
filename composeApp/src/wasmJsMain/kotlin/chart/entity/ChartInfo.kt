package entity

import kotlinx.serialization.Serializable

@Serializable
data class SongInfo(
    val songName: String,
    val composer: String,
    val illustrator: String,
    val charts: List<ChartInfo>
)

@Serializable
data class ChartInfo(
    val songName: String,
    val composer: String,
    val fileName: String,
    val levelInfo: LevelInfo,
)

@Serializable
data class LevelInfo(
    val levelType: String,  //IN
    val levelInt: Int,   //15
    val level: Float,    //15.9
    val charter: String,     //Ner_San
    val noteCount: Int
)