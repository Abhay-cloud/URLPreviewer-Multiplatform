import dev.abhaycloud.linkpreview.App
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        BrowserViewportWindow("Link Preview Compose") {
            App()
        }
    }
}
