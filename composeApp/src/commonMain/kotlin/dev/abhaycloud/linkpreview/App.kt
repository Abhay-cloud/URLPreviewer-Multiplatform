package dev.abhaycloud.linkpreview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.abhaycloud.linkpreview.theme.AppTheme
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType.Text.Html
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun App() = AppTheme {
    var urlInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var urlPreview: UrlPreview? by remember { mutableStateOf(null) }

    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Url Preview",
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            placeholder = { Text("Enter an URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )


        Button(
            onClick = {
                isLoading = true
                loadUrlPreview(urlInput, coroutineScope) { preview, loadingState, errorState ->
                    if (preview != null) {
                        urlPreview = preview
                        isLoading = loadingState
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            if(!isLoading) Text("Get Preview") else CircularProgressIndicator(color = Color.White, modifier = Modifier.size(25.dp))
        }

        if (urlPreview != null) {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp).clickable { openUrl(urlInput) }) {
//                    asyncPainterResource(data = "https://github.com/Abhay-cloud/ImageGallery-Compose/blob/master/images/MainBanner.png")
                    if(urlPreview!!.imageUrl.isNotEmpty())
                    KamelImage(
                        resource = asyncPainterResource(data = Url(urlPreview!!.imageUrl)),
                        contentDescription = "og:image",
                        modifier = Modifier.height(200.dp),
                        onLoading = {
                                 CircularProgressIndicator()
                        },
                        onFailure ={
                            println("\nImage load error: ${it.message} ${it.cause}")
                            Text(it.message!!)
                        }
                    )
                    Text(
                        text = urlPreview!!.title.ifEmpty { urlInput },
                        modifier = Modifier.padding(top = 10.dp),
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if(urlPreview!!.description.isNotEmpty())
                    Text(
                        text = urlPreview!!.description,
                        modifier = Modifier.padding(top = 10.dp),
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }

}

private fun loadUrlPreview(
    url: String,
    coroutineScope: CoroutineScope,
    callback: (UrlPreview?, Boolean, String?) -> Unit
) {
    var urlPreview: UrlPreview? = null
    var loading = true
    var error: String? = null

    val client = HttpClient()

    coroutineScope.launch {
        try {
            val response: HttpResponse = client.get(url) {
                method = HttpMethod.Get
                contentType(Html)
            }

            if (response.status.isSuccess()) {
                val htmlContent: String = response.body()
                val title = """<meta property="og:title" content="([^"]*)"""".toRegex()
                    .find(htmlContent)?.groupValues?.get(1) ?: ""
                val description = """<meta property="og:description" content="([^"]*)"""".toRegex()
                    .find(htmlContent)?.groupValues?.get(1) ?: ""
                val image = """<meta property="og:image" content="([^"]*)"""".toRegex()
                    .find(htmlContent)?.groupValues?.get(1) ?: ""
                print("abhay's log: $image")
                urlPreview = UrlPreview(title, description, image)
            } else {
                error = "HTTP Error: ${response.status.value}"
            }
        } catch (e: Exception) {
            error = "Failed to fetch URL preview: ${e.message}"
        } finally {
            loading = false
            callback(urlPreview, loading, error)
        }
    }
}


data class UrlPreview(
    val title: String,
    val description: String,
    val imageUrl: String
)

internal expect fun openUrl(url: String?)