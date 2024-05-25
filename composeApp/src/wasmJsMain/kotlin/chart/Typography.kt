import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import sim_phi_web.composeapp.generated.resources.Res
import sim_phi_web.composeapp.generated.resources.phi_medium
import sim_phi_web.composeapp.generated.resources.phi_normal

@OptIn(ExperimentalResourceApi::class)
@Composable
fun phigrosFontFamily() = FontFamily(
    Font(Res.font.phi_normal, FontWeight.Normal),
    Font(Res.font.phi_medium, FontWeight.Normal)
)