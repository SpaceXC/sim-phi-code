import kotlinx.coroutines.GlobalScope

class WasmPlatform {
    val name: String = "Web with Kotlin/Wasm"
}

fun getPlatform() = WasmPlatform()

