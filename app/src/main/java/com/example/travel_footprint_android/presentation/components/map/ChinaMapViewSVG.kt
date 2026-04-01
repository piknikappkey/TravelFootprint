package com.example.travel_footprint_android.presentation.components.map

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 中国地图 WebView 组件
 *
 * 功能：
 * 1. 显示 SVG 格式的中国地图
 * 2. 支持手势缩放和拖拽
 * 3. 支持点击城市获取城市信息
 *
 * @param modifier 修饰符
 * @param onCityClick 城市点击回调
 * @param showInfoCard 是否显示城市信息卡片
 */
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun ChinaMapViewSVG(
    modifier: Modifier = Modifier,
    onCityClick: ((CityInfo) -> Unit)? = null,
    showInfoCard: Boolean = true
) {
    val context = LocalContext.current
    var selectedCity by remember { mutableStateOf<CityInfo?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                    }

                    // 禁用滚动条
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    // 设置初始缩放比例为 150%（比原来大 0.5 倍）
                    setInitialScale(150)

                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            return super.shouldInterceptRequest(view, request)
                        }
                    }

                    // 添加 JavaScript 接口
                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onCityClicked(
                                name: String,
                                adcode: String,
                                centerLng: String,
                                centerLat: String,
                                parentAdcode: String
                            ) {
                                val cityInfo = CityInfo(
                                    name = name,
                                    adcode = adcode,
                                    centerLng = centerLng,
                                    centerLat = centerLat,
                                    parentAdcode = parentAdcode
                                )
                                selectedCity = cityInfo
                                onCityClick?.invoke(cityInfo)
                                Log.d("ChinaMap", "点击城市: $name, 代码: $adcode")
                            }
                        },
                        "AndroidBridge"
                    )

                    // 加载 SVG 并注入交互脚本
                    loadMapWithInteractions()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 显示城市信息卡片
        if (showInfoCard && selectedCity != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = """
                            城市：${selectedCity?.name}
                            区划代码：${selectedCity?.adcode}
                            经度：${selectedCity?.centerLng}
                            纬度：${selectedCity?.centerLat}
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * 加载地图并注入 JavaScript 交互
 */
private fun WebView.loadMapWithInteractions() {
    try {
        val inputStream = context.assets.open("maps/china_map_city_pencil.svg")
        val svgContent = inputStream.bufferedReader().use { it.readText() }

        // 注入 JavaScript 代码到 SVG
        val enhancedSvg = svgContent.replace(
            "</svg>",
            """
                <script type="text/javascript">
                // 等待 DOM 加载完成
                document.addEventListener('DOMContentLoaded', function() {
                    initCityInteractions();
                });
                
                // 立即执行（如果 DOM 已加载）
                if (document.readyState === 'complete' || document.readyState === 'interactive') {
                    initCityInteractions();
                }
                
                function initCityInteractions() {
                    var cities = document.querySelectorAll('.city');
                    cities.forEach(function(city) {
                        // 添加点击事件
                        city.addEventListener('click', function(e) {
                            e.preventDefault();
                            e.stopPropagation();
                            
                            // 移除其他选中状态
                            cities.forEach(function(c) {
                                c.classList.remove('selected');
                            });
                            
                            // 添加选中状态
                            this.classList.add('selected');
                            
                            // 获取城市信息
                            var name = this.getAttribute('data-name') || this.getAttribute('name') || '';
                            var adcode = this.getAttribute('data-adcode') || '';
                            var centerLng = this.getAttribute('data-center-lng') || '';
                            var centerLat = this.getAttribute('data-center-lat') || '';
                            var parentAdcode = this.getAttribute('data-parent-adcode') || '';
                            
                            // 调用 Android 接口
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onCityClicked(
                                    name,
                                    adcode,
                                    centerLng,
                                    centerLat,
                                    parentAdcode
                                );
                            }
                        });
                        
                        // 添加触摸事件（移动端优化）
                        city.addEventListener('touchstart', function(e) {
                            this.style.fill = '#E8E0D0';
                        });
                        
                        city.addEventListener('touchend', function(e) {
                            if (!this.classList.contains('selected')) {
                                this.style.fill = '';
                            }
                        });
                    });
                    
                    // 禁用双击缩放，只允许双指缩放
                    var lastTouchEnd = 0;
                    document.addEventListener('touchend', function(e) {
                        var now = Date.now();
                        if (now - lastTouchEnd <= 300) {
                            e.preventDefault();
                        }
                        lastTouchEnd = now;
                    }, false);
                    
                    console.log('ChinaMap: 已初始化 ' + cities.length + ' 个城市的交互');
                }
                </script>
                </svg>
            """.trimIndent()
        )

        // 添加额外的 CSS 来优化移动端体验
        val finalSvg = enhancedSvg.replace(
            "</style>",
            """
                .city { 
                    touch-action: manipulation;
                    -webkit-tap-highlight-color: transparent;
                }
                .city:active {
                    fill: #E8E0D0 !important;
                }
                </style>
            """.trimIndent()
        )

        loadDataWithBaseURL(
            null,
            finalSvg,
            "text/html",
            "UTF-8",
            null
        )
    } catch (e: Exception) {
        Log.e("ChinaMap", "加载地图失败", e)
        loadData(
            "text/html",
            "UTF-8",
            "<html><body><h3>地图加载失败</h3><p>${e.message}</p></body></html>"
        )
    }
}
