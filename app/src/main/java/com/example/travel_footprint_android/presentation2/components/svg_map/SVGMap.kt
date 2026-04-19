package com.example.travel_footprint_android.presentation2.components

import android.util.Log
import android.view.ViewTreeObserver
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

@Composable
fun SVGMap(
    modifier: Modifier = Modifier
) {
    // 存储选中的城市
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var cityInfo by remember { mutableStateOf<String?>(null) }

    Log.d("SVGMap", "进入 SVGMap")

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 交互式地图
        InteractiveChinaMap(
            { cityName, info ->
                selectedCity = cityName
                cityInfo = info
            }
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
    onCityClick: (String, String) -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true          // 开启 JavaScript 执行能力
                    domStorageEnabled = true          // 允许 DOM 存储（某些 SVG 交互可能需要）
                    builtInZoomControls = true        // 支持双指缩放
                    displayZoomControls = false       // 隐藏缩放控件按钮
                    allowFileAccess = true            // 允许访问 file:// 协议（assets 文件依赖）
                    allowContentAccess = true
                    javaScriptCanOpenWindowsAutomatically = true
                    isVerticalScrollBarEnabled = false   // 禁用垂直滚动条
                }

                // 添加 JavaScript 接口 - 注意：这里使用 "Android" 作为接口名称
                addJavascriptInterface(
                    CityClickInterface(onCityClick),
                    "Android" // 与 SVG 中的接口名称匹配
                )

                setInitialScale(220)

                // 设置 WebViewClient，在页面加载完成后滚动到中间
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.post {
                            val method = WebView::class.java.getDeclaredMethod("computeHorizontalScrollRange")
                            method.isAccessible = true
                            val contentWidth = method.invoke(view) as Int
                            val scrollX = (contentWidth - view.width) / 2
                            view.scrollTo(scrollX.coerceAtLeast(0), 0)
                            Log.d("WebViewClient", "contentWidth = $contentWidth, view.width = ${view.width}, scrollX = $scrollX, scrollX.coerceAtLeast(0) = ${scrollX.coerceAtLeast(0)}")
                        }
                    }
                }

//                webViewClient = object : WebViewClient() {
//                    override fun onPageFinished(view: WebView?, url: String?) {
//                        super.onPageFinished(view, url)
//                        view?.evaluateJavascript(
//                            "document.documentElement.scrollWidth"
//                        ) { result ->
//                            val contentWidth = result.toDoubleOrNull()?.toInt() ?: 0
//                            view.post {
//                                val scrollX = (contentWidth - view.width) / 2
//                                view.scrollTo(scrollX.coerceAtLeast(0), 0)
//                            }
//                        }
//                    }
//                }

                // 加载 assets 中的 SVG 文件
                loadUrl("file:///android_asset/maps/china_map_province_pencil.svg")
            }
        },
        modifier = Modifier
            .wrapContentSize()
    )
}

// JavaScript 接口类
class CityClickInterface(private val onCityClick: (String, String) -> Unit) {
    @JavascriptInterface
    fun onCityClicked(cityInfoJson: String) {
        Log.d("CityClickInterface", "Received city click: $cityInfoJson")
        try {
            // 解析 JSON 数据
            val jsonObject = JSONObject(cityInfoJson)
            val cityName = jsonObject.getString("name")
            onCityClick(cityName, cityInfoJson)
        } catch (e: Exception) {
            Log.e("CityClickInterface", "Error handling city click", e)
        }
    }
}