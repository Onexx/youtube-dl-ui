import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

suspend fun runConsoleApp(command: String, outputCallback: suspend (String) -> Unit) {
    val process = ProcessBuilder(*command.split(" ").toTypedArray())
        .redirectErrorStream(true)
        .start()

    val reader = BufferedReader(InputStreamReader(process.inputStream))
    var line: String?

    while (reader.readLine().also { line = it } != null) {
        outputCallback(line!!)
    }

    process.waitFor()
    reader.close()
}

@Composable
@Preview
fun App() {
    val text = remember { mutableStateOf("") }
    val output = remember { mutableStateOf("") }
    val validUrlPattern = """https://www\.youtube\.com/watch\?v=.*""".toRegex()

    MaterialTheme {
        val scrollState = rememberScrollState()
        Column {
            TextField(
                value = text.value,
                onValueChange = { text.value = it },
                label = { Text(text = "Youtube link: https://www.youtube.com/watch?v=XXXX") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    output.value = ""
                    if (text.value.isBlank()) {
                        output.value = "Input is blank. Please provide a valid youtube link"
                    } else if (!text.value.matches(validUrlPattern)) {
                        output.value =
                            "Invalid URL.\nPlease enter a valid URL similar to 'https://www.youtube.com/watch?v=XXXX'"
                    } else {
                        GlobalScope.launch(Dispatchers.IO) {
                            runConsoleApp("app\\resources\\youtube-dl -o \"~/Downloads/%(title)s.%(ext)s\" ${text.value}") { line ->
                                output.value += "$line\n"
                                scrollState.scrollTo(scrollState.maxValue)
                            }
                            scrollState.scrollTo(scrollState.maxValue)
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Download")
            }

            Spacer(Modifier.height(16.dp))

            Column(Modifier.verticalScroll(scrollState)) {
                SelectionContainer {
                    Text(output.value)
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(width = 500.dp, height = 400.dp),
        title = "Youtube downloader"
    ) {
        App()
    }
}
