package com.example.edujourneygermany.heatmap

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.HttpURLConnection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatMapScreen(
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Part-Time Jobs", "Indian Restaurants", "Universities", "Indian Communities")

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var mapDataJson by remember { mutableStateOf("") }
    
    // When user selects a filter tab in Compose, filter the map immediately
    LaunchedEffect(selectedFilter) {
        webViewRef?.evaluateJavascript("filterByCategory('$selectedFilter');", null)
    }

    // Attempt to fetch fresh data from backend (checks 10.0.2.2 and localtunnel)
    LaunchedEffect(selectedFilter) {
        withContext(Dispatchers.IO) {
            val endpoints = listOf(
                "http://10.0.2.2:8000/map/data?category=${java.net.URLEncoder.encode(selectedFilter, "UTF-8")}",
                "https://edugerman-map.loca.lt/map/data?category=${java.net.URLEncoder.encode(selectedFilter, "UTF-8")}",
                "https://young-dogs-think.loca.lt/map/data?category=${java.net.URLEncoder.encode(selectedFilter, "UTF-8")}"
            )
            for (endpoint in endpoints) {
                try {
                    val url = URL(endpoint)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 3000
                    connection.readTimeout = 3000
                    connection.setRequestProperty("Bypass-Tunnel-Reminder", "true")
                    
                    if (connection.responseCode == 200) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonObject = org.json.JSONObject(response)
                        if (jsonObject.has("data")) {
                            val dataArray = jsonObject.getJSONArray("data")
                            if (dataArray.length() > 0) {
                                val encoded = java.net.URLEncoder.encode(dataArray.toString(), "UTF-8").replace("+", "%20")
                                withContext(Dispatchers.Main) {
                                    mapDataJson = encoded
                                    webViewRef?.evaluateJavascript("updateMap(decodeURIComponent('$encoded'));", null)
                                }
                                connection.disconnect()
                                break
                            }
                        }
                    }
                    connection.disconnect()
                } catch (e: Exception) {
                    // Gracefully fallback to bundled authentic dataset
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interactive Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Scrollable Row
            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(selectedFilter).coerceAtLeast(0),
                edgePadding = 8.dp
            ) {
                filters.forEach { title ->
                    Tab(
                        selected = selectedFilter == title,
                        onClick = { selectedFilter = title },
                        text = { Text(title) }
                    )
                }
            }

            // WebView for Leaflet Map
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        
                        // Add JS Interface
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onViewMore(id: String) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    Toast.makeText(context, "Opening details for $id...", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }, "Android")
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (mapDataJson.isNotEmpty()) {
                                    view?.evaluateJavascript("updateMap(decodeURIComponent('$mapDataJson'));", null)
                                }
                                view?.evaluateJavascript("filterByCategory('$selectedFilter');", null)
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                android.util.Log.d("LeafletMap", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()}")
                                return super.onConsoleMessage(consoleMessage)
                            }
                        }
                        loadUrl("file:///android_asset/leaflet_cluster_map.html")
                        webViewRef = this
                    }
                },
                update = { webView ->
                    // Handled by LaunchedEffects
                }
            )
        }
    }
}
