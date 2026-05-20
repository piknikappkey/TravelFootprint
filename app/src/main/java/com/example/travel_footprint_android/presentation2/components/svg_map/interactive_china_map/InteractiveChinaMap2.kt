package com.example.travel_footprint_android.presentation2.components.svg_map.interactive_china_map

import android.util.Log
import android.view.ViewTreeObserver
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.presentation2.components.svg_map.CityClickInterface
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.google.gson.Gson
import kotlin.math.log

@Composable
fun InteractiveChinaMap2(
    onCityClick: (String, String, String) -> Unit,
    cityClickState: (Boolean) -> Unit,
    lightedProvinces: List<LightedProvince>
) {
    val context = LocalContext.current
    var isPageLoaded by remember { mutableStateOf(false) }
    var pendingData by remember { mutableStateOf<List<LightedProvince>?>(null) }

    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                javaScriptEnabled = true
                domStorageEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                allowFileAccess = true
                allowContentAccess = true
                javaScriptCanOpenWindowsAutomatically = true
                isVerticalScrollBarEnabled = false
                isHorizontalFadingEdgeEnabled = false
            }

            addJavascriptInterface(
                CityClickInterface(onCityClick, cityClickState),
                "Android"
            )

            webViewClient = object : WebViewClient() {
                // 限制缩放范围：例如 1.0f ~ 3.0f
                private val minScale = 1.0f
                private val maxScale = 3.0f

                override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
                    Log.d( "onScaleChanged","onScaleChanged ")
                    super.onScaleChanged(view, oldScale, newScale)
                    // 当缩放超出范围时，强制调整回边界值
                    when {
                        newScale < minScale -> view.zoomBy(minScale / newScale)
                        newScale > maxScale -> view.zoomBy(maxScale / newScale)
                    }
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 居中滚动逻辑（保持原样）
                    view?.post {
                        waitForCorrectContentWidth(view)
//                        val method = WebView::class.java.getDeclaredMethod("computeHorizontalScrollRange")
//                        method.isAccessible = true
//                        val contentWidth = method.invoke(view) as Int
//                        val scrollX = (contentWidth - view.width) / 2
//                        if (scrollX != 0) {
//                            view.scrollTo(scrollX.coerceAtLeast(0), 0)
//                        } else {
//                            view.scrollTo(340, 0)
//                        }
//                        Log.d("webViewClient-init", "contentWidth = $contentWidth, view.width = ${view.width}, scrollX = $scrollX")
                    }
                    isPageLoaded = true
                    // 页面加载完成，发送暂存的数据
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

            loadUrl("file:///android_asset/maps_html/china_map_province_pencil.html")
        }
    }

//    设置背景是否透明
//    webView.setBackgroundColor(BGLight0.toArgb())

    // 监听数据变化
    LaunchedEffect(lightedProvinces) {
        Log.d("SVGMap", "lightedProvinces changed, size = ${lightedProvinces.size}")
        if (lightedProvinces.isEmpty()) return@LaunchedEffect

        if (isPageLoaded) {
            sendLightedDataToWebView(lightedProvinces, webView)
        } else {
            // 页面未加载完成，暂存数据，等待 onPageFinished 发送
            pendingData = lightedProvinces
        }
    }

    DisposableEffect(webView) {
        webView.onResume()
        onDispose { webView.onPause() }
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier.wrapContentSize()
    )
}

// 发送数据的工具函数
private fun sendLightedDataToWebView(data: List<LightedProvince>, webView: WebView?) {
    val jsonArray = Gson().toJson(data)
    Log.d("SVGMap", "Sending to JS: $jsonArray")
    webView?.evaluateJavascript(
        "if(typeof updateProvinceLightsId === 'function') updateProvinceLightsId($jsonArray);",
        null
    )
}

private fun waitForCorrectContentWidth(webView: WebView) {
    var isMoved = false
    val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val contentWidth = runCatching {
                val method = WebView::class.java.getDeclaredMethod("computeHorizontalScrollRange")
                method.isAccessible = true
                method.invoke(webView) as Int
            }.getOrElse { 0 }

            Log.d("WebViewScroll", "检测 contentWidth = $contentWidth, view.width = ${webView.width}")

            // 当 contentWidth 不是错误值 1080 并且大于视图宽度时，执行滚动
            if (contentWidth != 1080 && contentWidth > webView.width) {
                val scrollX = (contentWidth - webView.width) / 2
                webView.scrollTo(scrollX.coerceAtLeast(0), 0)
                Log.d("WebViewScroll", "有效宽度，执行居中: scrollX = $scrollX")
                webView.viewTreeObserver.removeOnPreDrawListener(this)
                isMoved = true
                return true // 返回 true 表示继续绘制
            }
            return true // 继续等待下一次绘制
        }
    }

    webView.viewTreeObserver.addOnPreDrawListener(preDrawListener)

    // 超时保护
    webView.postDelayed({
        if(isMoved) return@postDelayed
        if (webView.viewTreeObserver.isAlive) {
            webView.viewTreeObserver.removeOnPreDrawListener(preDrawListener)
            val contentWidth = runCatching {
                val method = WebView::class.java.getDeclaredMethod("computeHorizontalScrollRange")
                method.isAccessible = true
                method.invoke(webView) as Int
            }.getOrElse { 0 }
            if (contentWidth > webView.width) {
                val scrollX = (contentWidth - webView.width) / 2
                webView.scrollTo(scrollX.coerceAtLeast(0), 0)
                Log.d("WebViewScroll", "超时后强制滚动: contentWidth=$contentWidth, scrollX=$scrollX")
            }
        }
    }, 2000)
}