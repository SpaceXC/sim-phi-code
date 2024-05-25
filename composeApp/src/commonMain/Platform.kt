import androidx.compose.ui.graphics.ImageBitmap

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun currentTimeMillis(): Long
expect fun ByteArray.toImageBitmap(): ImageBitmap
expect fun runTest(block: suspend () -> Unit)

