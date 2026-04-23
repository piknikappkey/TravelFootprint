package com.example.travel_footprint_android.presentation2.components.svg_map

import android.graphics.Color
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode
import com.google.gson.Gson
import org.json.JSONObject

@Composable
fun SVGMap(
    modifier: Modifier = Modifier,
    setLightenCityMode: (LightenCityMode) -> Unit,
    lightedProvinces: List<LightedProvince>
) {
    // 存储选中的城市
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var cityInfo by remember { mutableStateOf<String?>(null) }

    // 是否显示选中城市
    var cityState by remember { mutableStateOf(false) }

    Log.d("SVGMap", "进入 SVGMap")

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // 交互式地图
        InteractiveChinaMap(
            { cityName, info ->
                selectedCity = cityName
                cityInfo = info
            },
            {
                cityState = it
            },
            lightedProvinces = lightedProvinces,
        )

        if (selectedCity != null) {
            Text(
                text = "您选择了：$selectedCity",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            cityInfo?.let {
                Text(
                    text = "城市信息：$it",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun InteractiveChinaMap(
    onCityClick: (String, String) -> Unit,
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
                setBackgroundColor(Color.TRANSPARENT)
            }

            addJavascriptInterface(
                CityClickInterface(onCityClick, cityClickState),
                "Android"
            )

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 居中滚动逻辑（保持原样）
                    view?.post {
                        val method = WebView::class.java.getDeclaredMethod("computeHorizontalScrollRange")
                        method.isAccessible = true
                        val contentWidth = method.invoke(view) as Int
                        val scrollX = (contentWidth - view.width) / 2
                        if (scrollX != 0) {
                            view.scrollTo(scrollX.coerceAtLeast(0), 0)
                        } else {
                            view.scrollTo(340, 0)
                        }
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

// JavaScript 接口类
class CityClickInterface(
    private val onCityClick: (String, String) -> Unit,
    private val cityClickState: (Boolean) -> Unit,
) {
    @JavascriptInterface
    fun onCityClicked(cityInfoJson: String) {
        Log.d("CityClickInterface", "Received city click: $cityInfoJson")
        try {
            // 解析 JSON 数据
            val jsonObject = JSONObject(cityInfoJson)
            val cityName = jsonObject.getString("name")
            onCityClick(cityName, cityInfoJson)
            cityClickState(true)
        } catch (e: Exception) {
            Log.e("CityClickInterface", "Error handling city click", e)
        }
    }
    @JavascriptInterface
    fun onCityUnClicked() {
        Log.d("CityClickInterface", "Received city un click")
        cityClickState(false)
    }
}