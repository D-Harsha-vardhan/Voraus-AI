package com.example.edujourneygermany.heatmap

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

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

                        // Add JS Interface for Native Actions
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onViewMore(id: String, website: String?) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    val targetUrl = if (!website.isNullOrBlank()) {
                                        website
                                    } else {
                                        "https://www.google.com/search?q=" + Uri.encode("$id Berlin official website")
                                    }
                                    try {
                                        val safeUrl = if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) targetUrl else "https://$targetUrl"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl))
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open website", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun openWebsite(url: String) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    try {
                                        val safeUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl))
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open website", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun openDirections(title: String, lat: Double, lng: Double, address: String) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    val destination = Uri.encode("$title, $address")
                                    // 1. Try launching Google Maps application with navigation/search intent
                                    try {
                                        val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$destination")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(mapIntent)
                                            return@post
                                        }
                                    } catch (e: Exception) {
                                        // Ignore and fallback to web
                                    }

                                    // 2. Fallback to Google Maps web directions in external browser
                                    try {
                                        val webMapsUrl = "https://www.google.com/maps/dir/?api=1&destination=$destination"
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webMapsUrl))
                                        browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(browserIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open map directions", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }, "Android")

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val uri = request?.url ?: return false
                                val urlStr = uri.toString()

                                // 1. Handle intent:// URLs (e.g., Google Maps redirects)
                                if (urlStr.startsWith("intent://")) {
                                    try {
                                        val intent = Intent.parseUri(urlStr, Intent.URI_INTENT_SCHEME)
                                        if (intent != null) {
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            val packageManager = context.packageManager
                                            val info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                                            if (info != null) {
                                                context.startActivity(intent)
                                                return true
                                            }
                                            // Fallback if target app is not installed
                                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                            if (!fallbackUrl.isNullOrEmpty()) {
                                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
                                                browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                context.startActivity(browserIntent)
                                                return true
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    return true
                                }

                                // 2. Handle map, geo, tel, and mailto schemes
                                if (urlStr.startsWith("geo:") || urlStr.startsWith("tel:") || urlStr.startsWith("mailto:") ||
                                    urlStr.contains("maps.google.") || urlStr.contains("google.com/maps")
                                ) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(intent)
                                        return true
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                // 3. All external http(s) links opened outside of our local asset HTML
                                if (!urlStr.startsWith("file:///android_asset/")) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(intent)
                                        return true
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    return true
                                }

                                return false
                            }

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
