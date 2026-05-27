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

@Composable
fun InteractiveChinaProviceMap(
    onCityClick: (cityName: String, adcode: String, parentAdcode: String) -> Unit,
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
                useWideViewPort = true      // 允许缩放
                loadWithOverviewMode = true // 自适应
            }

            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

            // 添加缩放监听接口
            /**
             * 用户双指缩放地图时，实时回调缩放比例
             *
             * 外层可以用这个值来切换省级/市级地图
             */
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
            /**
             * 用户点击地图上的省份时，HTML 调用 Android.onCityClicked()
             * 触发 onCityClick 回调，告诉 Compose 哪个省份被点击了
             */
            addJavascriptInterface(
                CityClickInterface(onCityClick, cityClickState),
                "Android"
            )
            //设置 WebView 的初始缩放比例为 220%。
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

                    // 注入限制滚动的脚本
                    view?.evaluateJavascript(
                        """
            (function() {
                // 限制滚动范围（maxY = 0 禁止垂直滚动）
                var minX = 0;
                var maxX = 500;  // 设置最大水平滚动距离
                var minY = 0;
                var maxY = 0;    // 禁止垂直滚动
                
                function limitScroll() {
                    var currentX = window.scrollX;
                    var currentY = window.scrollY;
                    
                    var newX = Math.max(minX, Math.min(maxX, currentX));
                    var newY = Math.max(minY, Math.min(maxY, currentY));
                    
                    if (newX !== currentX || newY !== currentY) {
                        window.scrollTo(newX, newY);
                    }
                }
                
                // 监听滚动事件
                window.addEventListener('scroll', limitScroll);
                window.addEventListener('touchmove', limitScroll, { passive: false });
                
                // 立即执行一次
                limitScroll();
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

            loadUrl("file:///android_asset/maps_html/china_map_province_pencil.html")
        }
    }

    LaunchedEffect(lightedProvinces) {
        Log.d("SVGMap", "lightedProvinces changed, size = ${lightedProvinces.size}")
        if (lightedProvinces.isEmpty()) return@LaunchedEffect

        if (isPageLoaded) {
            sendLightedDataToWebView(lightedProvinces, webView)
        } else {
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

//高亮已点亮的省份
private fun sendLightedDataToWebView(data: List<LightedProvince>, webView: WebView?) {
    val jsonArray = Gson().toJson(data)
    Log.d("传递的省份数据", "Sending to JS: $jsonArray")
    webView?.evaluateJavascript(
        "if(typeof updateProvinceLightsId === 'function') updateProvinceLightsId($jsonArray);",
        null
    )


}