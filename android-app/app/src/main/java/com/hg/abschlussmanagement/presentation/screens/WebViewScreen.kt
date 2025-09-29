package com.hg.abschlussmanagement.presentation.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    onLoginClick: () -> Unit,
    onGastformularClick: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Website") },
            navigationIcon = {
                IconButton(onClick = { webView?.goBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { webView?.reload() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                }
                IconButton(onClick = onGastformularClick) {
                    Icon(Icons.Default.Person, contentDescription = "Wildfund melden")
                }
                IconButton(onClick = onLoginClick) {
                    Icon(Icons.Default.Login, contentDescription = "Anmelden")
                }
            }
        )
        
        // WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            canGoBack = view?.canGoBack() == true
                        }
                    }
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                    }
                    loadUrl("https://test.hg-landgraben-sued.de/abschussplan")
                    webView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Loading Indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
