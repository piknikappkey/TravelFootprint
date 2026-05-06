package com.example.travel_footprint_android.presentation2.components.svg_map.interactive_china_map2

import android.graphics.Color
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.presentation2.components.svg_map.CityClickInterface
import com.google.gson.Gson

@Composable
fun InteractiveChinaMap2(
    onCityClick: (String, String) -> Unit,
    cityClickState: (Boolean) -> Unit,
    lightedProvinces: List<LightedProvince>
) {
    val context = LocalContext.current

    var onPageLoaded: (() -> Unit)? = null

    // 1. 使用 remember 缓存 WebView 实例
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
            }

            // 添加 JavaScript 接口
            addJavascriptInterface(
                CityClickInterface(onCityClick, cityClickState),
                "Android"
            )

            setInitialScale(220)



            // 设置 WebViewClient
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.post {
                        val method = WebView::class.java.getDeclaredMethod("computeHorizontalScrollRange")
                        method.isAccessible = true
                        val contentWidth = method.invoke(view) as Int
                        val scrollX = (contentWidth - view.width) / 2
                        if(scrollX != 0) {
                            view.scrollTo(scrollX.coerceAtLeast(0), 0)
                        } else {
                            view.scrollTo(340, 0)
                        }
                    }
                    onPageLoaded?.invoke()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.d("WebViewConsole", consoleMessage?.message() ?: "")
                    return true
                }
            }

            // 加载 SVG 文件
            loadUrl("file:///android_asset/maps_html/china_map_city_pencil.html")
        }
    }

    // 观察省份点亮数据，如果有改变则将新的省份点亮数据传入webView
    LaunchedEffect(lightedProvinces) {
        if(lightedProvinces.isEmpty()) return@LaunchedEffect

        if (webView.progress == 100) {
            val adcodeList = lightedProvinces.map { it.provinceAdcode }
            val jsonArray = Gson().toJson(adcodeList)  // 结果如 ["110000","120000"]
            // 调用网页中定义的 JS 函数，例如 updateCityLights
            Log.d("SVGMap", "jsonArray = $jsonArray")
            webView.evaluateJavascript(
                "if(typeof updateProvinceLightsId === 'function') updateProvinceLightsId($jsonArray);",
                null
            )
        } else {
            // 设置回调，等待页面加载完成后再发一次
            onPageLoaded = {
                val adcodeList = lightedProvinces.map { it.provinceAdcode }
                val jsonArray = Gson().toJson(adcodeList)  // 结果如 ["110000","120000"]
                // 调用网页中定义的 JS 函数，例如 updateCityLights
                Log.d("SVGMap", "jsonArray = $jsonArray")
                webView.evaluateJavascript(
                    "if(typeof updateProvinceLightsId === 'function') updateProvinceLightsId($jsonArray);",
                    null
                )
            }
        }
    }

    // 2. 使用 DisposableEffect 管理生命周期
    DisposableEffect(key1 = webView) {
        // 组件进入组合树时
        webView.onResume()

        onDispose {
            // 组件退出组合树时（页面切换）
            webView.onPause()
            // 注意：不要调用 webView.destroy()，否则会销毁实例
        }
    }

    // 3. 使用缓存的 WebView 实例
    AndroidView(
        factory = { webView }, // 直接使用缓存的实例
        modifier = Modifier.wrapContentSize()
    )
}