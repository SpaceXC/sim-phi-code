import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.datetime.internal.JSJoda.Clock
import kotlinx.datetime.internal.JSJoda.ZoneId
import org.jetbrains.skia.Image

fun currentTimeMillis(): Long = Clock.system(ZoneId.SYSTEM).instant().toEpochMilli().toLong()
fun ByteArray.toImageBitmap(): ImageBitmap  {
    return Image.makeFromEncoded(this).toComposeImageBitmap()
}