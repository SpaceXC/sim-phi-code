package entity.display

import androidx.compose.ui.graphics.ImageBitmap
import entity.official.Note

enum class NoteType {
    Tap, Drag, Hold, Flick
}

enum class NotePosition {
    Above, Below
}

data class DisplayedNote(
    val orig: Note,
    val index: Int,
    val type: NoteType,
    val judgeLineIndex: Int,
    val position: NotePosition,
    val timePosition: Double,
    val endTimePosition: Double,
    val positionX: Float,
    val hasDoubleHit: Boolean,
    val heightKeyframes: List<Keyframe>,
    val bitmap: ImageBitmap?,
    val speed: Double,
)
