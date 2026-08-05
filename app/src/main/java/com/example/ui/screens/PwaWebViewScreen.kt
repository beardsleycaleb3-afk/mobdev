package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PwaWebViewScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef: WebView? = remember { null }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🌐 Web PWA Engine",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )
                Text(
                    text = "GitHub Repository index.html PWA Runtime",
                    fontSize = 12.sp,
                    color = PolishTextSecondary
                )
            }

            IconButton(
                onClick = { webViewRef?.reload() },
                modifier = Modifier.testTag("reload_pwa_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload PWA",
                    tint = PolishPrimary
                )
            }
        }

        // WebView displaying the GitHub PWA index.html
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewRef = this
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }
                        webViewClient = object : WebViewClient() {}
                        loadUrl("file:///android_asset/index.html")
                    }
                },
                update = { webView ->
                    webViewRef = webView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("pwa_webview")
            )
        }
    }
}
