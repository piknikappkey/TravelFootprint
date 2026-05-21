// InteractiveChinaProviceMap.kt (完整替换版)
package com.example.travel_footprint_android.presentation2.components.svg_map.interactive_china_map

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
import com.example.travel_footprint_android.presentation2.components.svg_map.CityClickInterface
import com.google.gson.Gson
import kotlin.math.log

@Composable
fun InteractiveChinaCityMap(
    onCityClick: (cityName: String, adcode: String, parentAdcode: String) -> Unit,
    cityClickState: (Boolean) -> Unit,
    lightedProvinces: List<LightedProvince>,
    onZoomChange: ((Float) -> Unit)? = null  // 新增：缩放回调
) {
    //获取当前 Composable 函数所在的 Android Context。
    val context = LocalContext.current
    //标记 HTML 页面是否已经加载完成。
    var isPageLoaded by remember { mutableStateOf(false) }
    if (!isPageLoaded){
        Log.d("页面加载状态","页面未被加载")
    }
    else{
        Log.d("页面加载状态","页面成功加载")
    }
    //暂存等待发送的数据。
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
                useWideViewPort = true      // 允许缩放
                loadWithOverviewMode = true // 自适应
            }

            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

            // 添加缩放监听接口
            addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onScaleChanged(scale: Float) {
                        Log.d("ZoomListener", "Province map scale: $scale")
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
                            if (scrollX != 0) {
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

    //监听 lightedProvinces 数据变化，并自动同步到 WebView 地图中。
    LaunchedEffect(lightedProvinces) {
        Log.d("SVGMap", "点亮省份页面改变, 变化 = ${lightedProvinces.size}")
        if (lightedProvinces.isEmpty()) return@LaunchedEffect

        if (isPageLoaded) {
            sendLightedDataToWebView(lightedProvinces, webView)
        } else {
            pendingData = lightedProvinces
        }
    }

    //管理 WebView 的生命周期，确保它在 Compose 界面可见时处于活动状态，不可见时正确暂停。
    DisposableEffect(webView) {
        webView.onResume()
        onDispose { webView.onPause() }
    }


    //将传统的 Android View（WebView）嵌入到 Compose UI 树中。
    AndroidView(
        factory = { webView },
        modifier = Modifier.wrapContentSize()
    )
}

private fun sendLightedDataToWebView(data: List<LightedProvince>, webView: WebView?) {
    val jsonArray = Gson().toJson(data)
    Log.d("SVGMap", "Sending to JS: $jsonArray")
    webView?.evaluateJavascript(
        "if(typeof updateProvinceLightsId === 'function') updateProvinceLightsId($jsonArray);",
        null
    )


}