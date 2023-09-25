import androidx.compose.ui.window.ComposeUIViewController
import dev.abhaycloud.linkpreview.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController { App() }
}
