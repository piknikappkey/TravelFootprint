// InteractiveChinaMap2.kt (完整替换版)
package com.example.travel_footprint_android.presentation.components.svg_map.interactive_china_map2

import android.graphics.Color
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.presentation.components.svg_map.CityClickInterface
import com.google.gson.Gson

@Composable
fun InteractiveChinaMap2(
    onCityClick: (String, String, String) -> Unit,
    cityClickState: (Boolean) -> Unit,
    lightedProvinces: List<LightedProvince>,
    onZoomChange: ((Float) -> Unit)? = null  // 新增：缩放回调
) {
    val context = LocalContext.current
    var isPageLoaded by remember { mutableStateOf(false) }
    var pendingData by remember { mutableStateOf<List<LightedProvince>?>(null) }

    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                allowFileAccess = true
                allowContentAccess = true
                javaScriptCanOpenWindowsAutomatically = true
                isVerticalScrollBarEnabled = false
                isHorizontalFadingEdgeEnabled = false
                setBackgroundColor(Color.TRANSPARENT)
                useWideViewPort = true      // 允许缩放
                loadWithOverviewMode = true // 自适应
            }

            // 添加缩放监听接口
            addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onScaleChanged(scale: Float) {
                        Log.d("ZoomListener", "City map scale: $scale")
                        onZoomChange?.invoke(scale)
                    }
                },
                "AndroidScale"
            )

            addJavascriptInterface(
                CityClickInterface(onCityClick, cityClickState),
                "Android"
            )

            setInitialScale(220)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    // 注入缩放监听脚本
                    view?.evaluateJavascript(
                        """
                        (function() {
                            var lastScale = 1;
                            function checkScale() {
                                var scale = window.visualViewport ? window.visualViewport.scale : 1;
                                if (Math.abs(scale - lastScale) > 0.05) {
                                    lastScale = scale;
                                    AndroidScale.onScaleChanged(scale);
                                }
                            }
                            document.addEventListener('touchend', checkScale);
                            document.addEventListener('gestureend', checkScale);
                            setInterval(checkScale, 200);
                        })();
                        """.trimIndent(),
                        null
                    )

                    view?.post {
                        try {
                            val method = WebView::class.java.getDeclaredMethod("computeHorizontalScrollRange")
                            method.isAccessible = true
                            val contentWidth = method.invoke(view) as Int
                            val scrollX = (contentWidth - view.width) / 2
                            if(scrollX != 0) {
                                view.scrollTo(scrollX.coerceAtLeast(0), 0)
                            } else {
                                view.scrollTo(340, 0)
                            }
                        } catch (e: Exception) {
                            Log.e("WebView", "Error centering", e)
                        }
                    }

                    isPageLoaded = true
                    pendingData?.let { data ->
                        sendLightedDataToWebView(data, view)
                        pendingData = null
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.d("WebViewConsole", consoleMessage?.message() ?: "")
                    return true
                }
            }

            loadUrl("file:///android_asset/maps_html/china_map_city_pencil.html")
        }
    }

    LaunchedEffect(lightedProvinces) {
        if(lightedProvinces.isEmpty()) return@LaunchedEffect

        if (isPageLoaded) {
            sendLightedDataToWebView(lightedProvinces, webView)
        } else {
            pendingData = lightedProvinces
        }
    }

    DisposableEffect(key1 = webView) {
        webView.onResume()
        onDispose {
            webView.onPause()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier.wrapContentSize()
    )
}

private fun sendLightedDataToWebView(data: List<LightedProvince>, webView: WebView?) {
    val adcodeList = data.map { it.provinceAdcode }
    val jsonArray = Gson().toJson(adcodeList)
    Log.d("SVGMap", "jsonArray = $jsonArray")
    webView?.evaluateJavascript(
        "if(typeof updateCityLightsId === 'function') updateCityLightsId($jsonArray);",
        null
    )
}