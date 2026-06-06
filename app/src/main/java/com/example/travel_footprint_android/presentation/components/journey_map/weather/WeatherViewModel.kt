/*
 * 文件名：WeatherViewModel.kt
 * 包路径：presentation2.components.journey_map3.weather
 *
 * 【用途】
 * 天气数据 ViewModel。集成高德地图定位 SDK 和天气搜索 SDK，
 * 自动获取用户当前位置的实况天气和未来天气预报，以 StateFlow
 * 形式对外提供响应式天气 UI 状态。
 *
 * 供旅程地图页面的天气面板使用，在地图上展示当前城市天气信息。
 *
 * 【功能】
 * 1. 自动定位：通过 AMapLocationClient 获取用户当前所在城市和行政区编码（adcode）
 * 2. 实况天气查询：根据定位 adcode 查询实时温度、天气状况、湿度、风向风力、报告时间
 * 3. 天气预报查询：根据定位 adcode 查询未来多日的逐日预报（白天/夜间天气、温度、风力）
 * 4. 双请求协同加载：只有当实况和预报两个请求都成功返回后，才将 isLoading 置为 false
 * 5. 错误处理：定位失败、天气搜索失败时均设置 error 信息，UI 层可据此展示错误提示
 * 6. Hilt 注入：通过 @HiltViewModel 支持依赖注入，Application 实例由 Hilt 提供
 *
 * 【关联组件】
 * - WeatherCard / WeatherPanel：UI 层组件，通过 collectAsState() 观察 weatherState，
 *   根据 isLoading、liveWeather、forecast、cityName、error 渲染天气界面
 * - JourneyMap3：旅程地图主页面，内部包含天气面板
 * - 高德地图 SDK：AMapLocationClient（定位）、WeatherSearch（天气搜索）
 *
 * 【简单实现逻辑】
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 1. 定义三个数据类：                                         │
 * │    - LiveWeatherData : 实况天气（温度/天气/湿度/风向/风力）│
 * │    - ForecastData    : 逐日预报（日期/白天夜间天气/温度/风）│
 * │    - WeatherUiState  : UI 状态容器（loading/实况/预报/城市）│
 * │                                                             │
 * │ 2. WeatherViewModel 初始化时（init）：                       │
 * │    - 创建两个 WeatherSearch 实例（live + forecast）          │
 * │    - 共用一个 WeatherSearchListener 处理两种回调             │
 * │                                                             │
 * │ 3. loadWeatherForCurrentLocation() 流程：                    │
 * │    a. 设置 isLoading = true，清空旧数据                      │
 * │    b. 创建 AMapLocationClient，配置高精度单次定位            │
 * │    c. 定位回调 onLocationChanged：                           │
 * │       ├─ 成功 → 提取 adcode + cityName → 并发发起           │
 * │       │   queryLiveWeather() + queryForecastWeather()        │
 * │       └─ 失败 → 设置 error 信息，isLoading = false          │
 * │    d. 定位完成后调用 locationClient.onDestroy() 释放资源    │
 * │                                                             │
 * │ 4. 天气搜索回调（weatherSearchListener）：                    │
 * │    ├─ onWeatherLiveSearched → 解析 LiveWeatherResult         │
 * │    │    → 更新 weatherState.liveWeather → checkLoadingComplete│
 * │    └─ onWeatherForecastSearched → 解析 ForecastResult        │
 * │         → 映射为 List<ForecastData> → 更新 forecast         │
 * │         → checkLoadingComplete                               │
 * │                                                             │
 * │ 5. checkLoadingComplete：                                    │
 * │    当 liveWeather 和 forecast 都不为 null 时，              │
 * │    将 isLoading 置为 false，通知 UI 层展示数据               │
 * └─────────────────────────────────────────────────────────────┘
 */
package com.example.travel_footprint_android.presentation.components.journey_map.weather

import android.app.Application // Android Application 类，AndroidViewModel 需要
import android.util.Log // 日志输出，用于调试和错误追踪
import androidx.lifecycle.AndroidViewModel // 持有 Application context 的 ViewModel 基类
import androidx.lifecycle.viewModelScope // ViewModel 绑定的协程作用域，自动跟随生命周期取消
import com.amap.api.location.AMapLocationClient // 高德定位客户端，用于获取用户当前位置
import com.amap.api.location.AMapLocationClientOption // 定位参数配置（模式/单次/地址等）
import com.amap.api.services.core.AMapException // 高德 SDK 异常类，含 errorCode
import com.amap.api.services.weather.LocalDayWeatherForecast // 单日天气预报数据类
import com.amap.api.services.weather.LocalWeatherForecastResult // 天气预报搜索结果
import com.amap.api.services.weather.LocalWeatherLiveResult // 实况天气搜索结果
import com.amap.api.services.weather.WeatherSearch // 高德天气搜索客户端
import com.amap.api.services.weather.WeatherSearchQuery // 天气搜索查询参数（adcode + 类型）
import com.example.travel_footprint_android.data.local.AppSettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel // Hilt ViewModel 注解，支持依赖注入
import kotlinx.coroutines.delay // 协程延时
import kotlinx.coroutines.Dispatchers // 协程调度器（IO/Main）
import kotlinx.coroutines.flow.MutableStateFlow // 可变状态流，支持 emit 更新
import kotlinx.coroutines.flow.StateFlow // 只读状态流，UI 层 collect 观察
import kotlinx.coroutines.flow.asStateFlow // 将 Mutable 转为只读 StateFlow
import kotlinx.coroutines.launch // 启动协程
import kotlinx.coroutines.withContext // 切换协程上下文（调度器）
import javax.inject.Inject // Hilt 依赖注入注解

// 实况天气数据模型：封装高德 SDK 返回的实时天气各字段
data class LiveWeatherData(
    val temperature: String, // 当前温度（摄氏度，如"26"）
    val weather: String, // 天气状况描述（如"晴"、"多云"、"小雨"）
    val humidity: String, // 相对湿度百分比（如"65"）
    val windDirection: String, // 风向描述（如"东南风"）
    val windPower: String, // 风力等级（如"3级"）
    val reportTime: String // 数据发布时间（如"2025-07-25 14:00:00"）
)

// 逐日天气预报数据模型：封装高德 SDK 返回的单日预报各字段
data class ForecastData(
    val date: String, // 预报日期（如"2025-07-26"）
    val dayWeather: String, // 白天天气状况（如"晴"）
    val nightWeather: String, // 夜间天气状况（如"多云"）
    val dayTemp: String, // 白天最高温度（摄氏度）
    val nightTemp: String, // 夜间最低温度（摄氏度）
    val dayWindDirection: String, // 白天风向
    val nightWindDirection: String, // 夜间风向
    val dayWindPower: String, // 白天风力等级
    val nightWindPower: String // 夜间风力等级
)

// 天气 UI 状态容器：封装所有天气相关的 UI 状态，供界面层观察
data class WeatherUiState(
    val isLoading: Boolean = false, // 是否正在加载（定位/天气查询进行中）
    val showWeatherCard: Boolean = true, // 是否显示天气卡片（外部可控制显隐）
    val liveWeather: LiveWeatherData? = null, // 实况天气数据（null 表示未加载或失败）
    val forecast: List<ForecastData>? = null, // 天气预报数据列表（null 表示未加载或失败）
    val cityName: String? = null, // 当前城市名称（如"北京市"）
    val error: String? = null // 错误信息（定位失败/搜索失败的描述）
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    application: Application, // Hilt 自动注入 Application 实例
    private val appSettingsStore: AppSettingsStore
) : AndroidViewModel(application) {

    // 私有可变状态流：驱动天气 UI 的所有状态
    private val _weatherState = MutableStateFlow(WeatherUiState())
    // 对外只读状态流：UI 层通过 collectAsState() 观察并触发重组
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    // 两个独立的 WeatherSearch 实例，分别处理实况和预报查询
    private var liveWeatherSearch: WeatherSearch? = null // 实况天气搜索客户端
    private var forecastWeatherSearch: WeatherSearch? = null // 天气预报搜索客户端

    // 共用天气搜索结果监听器：同时处理实况和预报两种回调
    private val weatherSearchListener = object : WeatherSearch.OnWeatherSearchListener {

        // 实况天气搜索结果回调（异步返回）
        override fun onWeatherLiveSearched(result: LocalWeatherLiveResult?, rCode: Int) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) { // 切到主线程更新 StateFlow
                    // rCode == 1000 表示高德 SDK 请求成功
                    if (rCode == 1000 && result != null && result.liveResult != null) {
                        val live = result.liveResult // 获取实况天气数据
                        // 更新状态：保存实况天气数据
                        _weatherState.value = _weatherState.value.copy(
                            liveWeather = LiveWeatherData(
                                temperature = live.temperature,
                                weather = live.weather,
                                humidity = live.humidity,
                                windDirection = live.windDirection,
                                windPower = live.windPower,
                                reportTime = live.reportTime
                            )
                        )
                        checkLoadingComplete() // 检查是否两个请求都已完成
                    } else {
                        // 请求失败，仅结束加载并设置错误信息
                        _weatherState.value = _weatherState.value.copy(
                            isLoading = false,
                            error = "获取实况天气失败"
                        )
                    }
                }
            }
        }

        // 天气预报搜索结果回调（异步返回）
        override fun onWeatherForecastSearched(result: LocalWeatherForecastResult?, rCode: Int) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) { // 切到主线程更新 StateFlow
                    // rCode == 1000 表示高德 SDK 请求成功
                    if (rCode == 1000 && result != null && result.forecastResult != null) {
                        val dailyForecasts = result.forecastResult.weatherForecast // 获取逐日预报列表
                        if (dailyForecasts != null) {
                            // 将高德 SDK 的 LocalDayWeatherForecast 映射为应用层 ForecastData
                            _weatherState.value = _weatherState.value.copy(
                                forecast = dailyForecasts.map { daily: LocalDayWeatherForecast ->
                                    ForecastData(
                                        date = daily.date,
                                        dayWeather = daily.dayWeather,
                                        nightWeather = daily.nightWeather,
                                        dayTemp = daily.dayTemp,
                                        nightTemp = daily.nightTemp,
                                        dayWindDirection = daily.dayWindDirection,
                                        nightWindDirection = daily.nightWindDirection,
                                        dayWindPower = daily.dayWindPower,
                                        nightWindPower = daily.nightWindPower
                                    )
                                }
                            )
                        }
                        checkLoadingComplete() // 检查是否两个请求都已完成
                    } else {
                        // 请求失败，仅结束加载并设置错误信息
                        _weatherState.value = _weatherState.value.copy(
                            isLoading = false,
                            error = "获取天气预报失败"
                        )
                    }
                }
            }
        }
    }

    // 初始化块：在 ViewModel 创建时初始化两个 WeatherSearch 客户端
    init {
        // 监听设置变化并同步到 UI 状态
        viewModelScope.launch {
            appSettingsStore.settingsFlow.collect { settings ->
                _weatherState.value = _weatherState.value.copy(
                    showWeatherCard = settings.showWeatherCard
                )
            }
        }

        val context = application.applicationContext
        try {
            // 初始化实况天气搜索客户端，注册共用监听器
            liveWeatherSearch = WeatherSearch(context)
            liveWeatherSearch?.setOnWeatherSearchListener(weatherSearchListener)
        } catch (e: AMapException) {
            Log.e("WeatherViewModel", "WeatherSearch live init failed: ${e.errorCode}")
        }
        try {
            // 初始化天气预报搜索客户端，注册共用监听器
            forecastWeatherSearch = WeatherSearch(context)
            forecastWeatherSearch?.setOnWeatherSearchListener(weatherSearchListener)
        } catch (e: AMapException) {
            Log.e("WeatherViewModel", "WeatherSearch forecast init failed: ${e.errorCode}")
        }
        // 初始化完成后立即加载天气数据（仅在 ViewModel 创建时执行一次）
        loadWeatherForCurrentLocation()
        // 每 10 分钟自动刷新天气数据
        viewModelScope.launch {
            while (true) {
                delay(10 * 60 * 1000L) // 10 分钟
                loadWeatherForCurrentLocation()
            }
        }
    }

    // 切换天气卡片的显示/隐藏状态,并持久化存储
    fun toggleWeatherCard() {
        val newState = !_weatherState.value.showWeatherCard
        _weatherState.value = _weatherState.value.copy(showWeatherCard = newState)
        viewModelScope.launch {
            appSettingsStore.updateShowWeatherCard(newState)
        }
    }

    // 对外入口：加载当前定位城市的天气数据（定位 → 实况查询 → 预报查询）
    fun loadWeatherForCurrentLocation() {
        // 重置数据字段，保留 showWeatherCard 等 UI 控制字段不丢失
        _weatherState.value = _weatherState.value.copy(
            isLoading = true,
            liveWeather = null,
            forecast = null,
            cityName = null,
            error = null
        )
        val context = getApplication<Application>().applicationContext

        // 创建高德定位客户端，配置单次高精度定位
        val locationClient = AMapLocationClient(context)
        val locationOption = AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy // GPS + 网络混合定位
            isOnceLocation = true // 仅定位一次，不持续更新
            isOnceLocationLatest = true // 返回最新缓存结果
            isNeedAddress = true // 需要返回地址信息（城市、adcode 等）
        }
        locationClient.setLocationOption(locationOption)
        // 注册定位结果监听器
        locationClient.setLocationListener { amapLocation ->
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    // 定位成功判断：amapLocation 非 null 且 errorCode == 0
                    if (amapLocation != null && amapLocation.errorCode == 0) {
                        val adcode = amapLocation.adCode // 行政区编码，用于天气查询
                        val cityName = amapLocation.city ?: amapLocation.province // 城市名（兜底用省份）
                        _weatherState.value = _weatherState.value.copy(cityName = cityName)
                        // 并发发起实况和预报查询
                        queryLiveWeather(adcode)
                        queryForecastWeather(adcode)
                    } else {
                        // 定位失败：提取错误信息
                        val errorMsg = if (amapLocation != null) {
                            "定位失败: ${amapLocation.errorCode} ${amapLocation.errorInfo}"
                        } else {
                            "定位失败: 未知错误"
                        }
                        _weatherState.value = _weatherState.value.copy(
                            isLoading = false,
                            error = errorMsg
                        )
                    }
                    // 定位完成后释放定位客户端资源，避免内存泄漏
                    locationClient.onDestroy()
                }
            }
        }
        // 启动定位
        locationClient.startLocation()
    }

    // 发起实况天气异步查询（adcode：行政区编码）
    private fun queryLiveWeather(adcode: String) {
        val query = WeatherSearchQuery(adcode, WeatherSearchQuery.WEATHER_TYPE_LIVE) // 实况类型
        liveWeatherSearch?.let {
            it.setQuery(query) // 设置查询参数
            it.searchWeatherAsyn() // 异步执行搜索
        }
    }

    // 发起天气预报异步查询（adcode：行政区编码）
    private fun queryForecastWeather(adcode: String) {
        val query = WeatherSearchQuery(adcode, WeatherSearchQuery.WEATHER_TYPE_FORECAST) // 预报类型
        forecastWeatherSearch?.let {
            it.setQuery(query) // 设置查询参数
            it.searchWeatherAsyn() // 异步执行搜索
        }
    }

    // 检查实况和预报是否都加载完毕，是则结束 loading 状态
    private fun checkLoadingComplete() {
        val state = _weatherState.value
        // 只有当 liveWeather 和 forecast 都 != null 时才关闭 loading
        // 防止两个请求中只有一个完成时就结束加载
        if (state.liveWeather != null && state.forecast != null) {
            _weatherState.value = state.copy(isLoading = false)
        }
    }
}
