import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chart.ChartRenderer
import chart.ViewModel
import chart.initBitmaps
import chart.loadBitmapFromFile
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.ui.tooling.preview.Preview

val songs = listOf(
    "风屿",
    "今年も「雪降り、メリクリ」目指して頑張ります！！",
    "狂喜蘭舞",
    "Break Over",
    "Cthugha",
    "dB doll",
    "Destruction 321",
    "Disorted Fate",
    "IgaIIta",
    "INFiNiTE ENERZY -Overdoze-",
    "Lyrith -迷宮リリス",
    "mopemope",
    "Retribution Cycle of Redemption",
    "Rrhar 'il",
    "Shadow",
    "Sigma (Haocore Mix)105秒の伝説",
    "Sigma (HaocoreMix) Regrets of The Yellow Tulip",
    "Spasmodic",
    "Stasis",
    "volcanic",
    "You are the miserable"
)

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {
    LaunchedEffect(Unit) {
        initBitmaps()
    }

    val viewModel = remember { ViewModel() }
    var currentSongName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val aspectRatio by remember { mutableStateOf(4f / 3f) }

    var bitmap: ImageBitmap? by remember { mutableStateOf(null) }

    if(currentSongName.isNotEmpty()) {
        LaunchedEffect(Unit) {
            bitmap = loadBitmapFromFile("Charts/$currentSongName/$currentSongName.png")
        }
        ChartRenderer(
            viewModel,
            currentSongName,
            bitmap,
            aspectRatio
        ) {
            currentSongName = ""
        }
    }
    else {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            songs.forEach {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), onClick = {
                    scope.launch {
                        viewModel.initChart(it)
                        viewModel.prepare()
                        currentSongName = it
                    }
                }) {
                    Text(it, fontFamily = phigrosFontFamily(), fontSize = 15.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}