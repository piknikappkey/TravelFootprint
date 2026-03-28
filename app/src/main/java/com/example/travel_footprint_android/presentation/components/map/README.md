# ChinaMapView 组件说明文档

## 一、组件概述

ChinaMapView 是旅行足迹地图应用的核心地图组件，负责加载 GeoJSON 数据、使用 Jetpack Compose Canvas 绘制手绘风格的中国地图，并实现省份点击检测。

**支持的手势：**
- **单指拖动**：平移地图
- **双指捏合**：缩放地图（缩放范围 0.5x - 5x）
- **单指点击**：选中省份

## 二、文件结构

```
presentation/components/map/
├── ChinaMapView.kt        # 主组件：地图视图（GeoJSON方案）
├── ChinaMapViewSVG.kt     # 主组件：SVG地图视图（WebView方案）
├── CityInfo.kt            # 城市信息数据类
├── GeoJSONParser.kt       # GeoJSON 解析器
├── MapClickDetector.kt    # 点击检测工具
└── MapPath.kt             # 数据结构和模型
```

## 三、组件清单

| 文件名 | 类型 | 用途 |
|-------|------|------|
| `ChinaMapView.kt` | Composable | 地图主组件（GeoJSON方案），负责渲染和交互 |
| `ChinaMapViewSVG.kt` | Composable | 地图主组件（SVG方案），使用WebView渲染 |
| `CityInfo.kt` | Data Class | 城市信息数据类 |
| `GeoJSONParser.kt` | Object | 解析 GeoJSON 为 Compose Path |
| `MapClickDetector.kt` | Object | 检测点击的省份/城市 |
| `MapPath.kt` | Data Class | 定义地图数据结构 |

## 四、使用方法

### 4.1 基础使用

```kotlin
import com.example.travel_footprint_android.presentation.components.map.ChinaMapView
import com.example.travel_footprint_android.domain.service.HandDrawStyle

@Composable
fun MyScreen() {
    ChinaMapView(
        modifier = Modifier.fillMaxSize(),
        mapStyle = HandDrawStyle.WATERCOLOR,
        onProvinceClick = { adcode ->
            // 处理省份点击事件
            println("点击了省份: $adcode")
        },
        onMapLoaded = {
            // 地图加载完成
            println("地图加载完成")
        }
    )
}
```

### 4.2 在 ViewModel 中预加载数据

```kotlin
import com.example.travel_footprint_android.presentation.components.map.loadMapData

class MyViewModel : ViewModel() {
    
    fun preloadMapData(context: Context) {
        viewModelScope.launch {
            val result = loadMapData(
                context = context,
                canvasWidth = 1000f,
                canvasHeight = 800f
            )
            result.onSuccess { provinces ->
                // 使用省份数据
            }.onFailure { error ->
                // 处理错误
            }
        }
    }
}
```

## 五、API 参考

### 5.1 ChinaMapView

```kotlin
@Composable
fun ChinaMapView(
    modifier: Modifier = Modifier,
    mapStyle: HandDrawStyle = HandDrawStyle.WATERCOLOR,
    onProvinceClick: (String) -> Unit = {},
    onMapLoaded: () -> Unit = {}
)
```

| 参数 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| `modifier` | Modifier | Modifier | 组件修饰符 |
| `mapStyle` | HandDrawStyle | WATERCOLOR | 地图手绘风格 |
| `onProvinceClick` | (String) -> Unit | {} | 省份点击回调，参数为 adcode |
| `onMapLoaded` | () -> Unit | {} | 地图加载完成回调 |

### 5.2 GeoJSONParser

```kotlin
object GeoJSONParser {
    fun parseProvinces(
        context: Context,
        fileName: String = "中华人民共和国(省).geojson",
        canvasWidth: Float,
        canvasHeight: Float
    ): List<ProvincePath>
    
    fun parseCities(
        context: Context,
        fileName: String = "中华人民共和国(市).geojson",
        canvasWidth: Float,
        canvasHeight: Float
    ): List<CityPath>
}
```

### 5.3 MapClickDetector

```kotlin
object MapClickDetector {
    fun findProvinceAt(
        offset: Offset,
        provinces: List<ProvincePath>
    ): ProvincePath?
    
    fun findCityAt(
        offset: Offset,
        cities: List<CityPath>
    ): CityPath?
}
```

## 六、数据结构

### 6.1 ProvincePath（省份路径）

```kotlin
data class ProvincePath(
    val adcode: String,        // 行政区划代码，如 "110000"
    val name: String,          // 省份名称，如 "北京市"
    val path: Path,            // Compose 绘制路径
    val center: Offset,        // 中心点坐标
    val childrenNum: Int = 0   // 下属城市数量
)
```

### 6.2 CityPath（城市路径）

```kotlin
data class CityPath(
    val adcode: String,        // 行政区划代码
    val name: String,          // 城市名称
    val path: Path,            // Compose 绘制路径
    val center: Offset,        // 中心点坐标
    val provinceAdcode: String // 所属省份代码
)
```

### 6.3 MapLevel（地图层级）

```kotlin
enum class MapLevel {
    PROVINCE,  // 省份层级
    CITY       // 城市层级
}
```

## 七、手绘风格

组件支持 5 种手绘风格（来自 `HandDrawStyle` 枚举）：

| 风格 | 说明 | 填充色 | 边界色 |
|-----|------|-------|-------|
| `WATERCOLOR` | 水彩风格 | 米黄色 | 棕色 |
| `PENCIL_SKETCH` | 铅笔速写 | 浅灰 | 深灰 |
| `VINTAGE_PAPER` | 复古牛皮纸 | 牛皮纸色 | 深棕 |
| `INK_WASH` | 水墨风格 | 浅灰 | 深灰 |
| `CRAYON` | 蜡笔风格 | 浅黄 | 橙色 |

## 八、实现逻辑

### 8.1 数据加载流程

1. **启动加载**：`LaunchedEffect` 触发异步加载
2. **IO 线程解析**：使用 `GeoJSONParser` 解析 GeoJSON
3. **坐标转换**：经纬度 → 屏幕坐标（考虑 Y 轴翻转）
4. **主线程渲染**：解析完成后在主线程更新 UI

### 8.2 坐标映射公式

```kotlin
// 经纬度 → 屏幕坐标
x = (lng - 73) / (135 - 73) * canvasWidth
y = (54 - lat) / (54 - 3) * canvasHeight
```

### 8.3 点击检测流程

1. 用户点击 Canvas
2. `detectTapGestures` 捕获点击坐标
3. `MapClickDetector.findProvinceAt()` 遍历所有省份
4. 使用 `Path.contains()` 判断点是否在路径内
5. 返回点击的省份

## 九、依赖关系

### 9.1 内部依赖

```
ChinaMapView
├── GeoJSONParser      # 解析 GeoJSON
├── MapClickDetector   # 点击检测
└── MapPath            # 数据模型
```

### 9.2 外部依赖

- `HandDrawStyle`（`domain/service/HandDrawStyle.kt`）- 手绘风格枚举
- GeoJSON 数据文件（`assets/中华人民共和国(省).geojson`）

## 十、性能优化

1. **异步加载**：在 IO 线程解析 GeoJSON，避免阻塞主线程
2. **数据缓存**：使用 `remember` 缓存解析后的 Path 对象
3. **按需绘制**：只绘制当前层级的地图数据
4. **点击优化**：从后向前遍历，优先检测上层区域

## 十一、待配合组件

当前版本为最小可用版本，以下组件需要后续开发配合：

| 组件 | 用途 | 状态 |
|-----|------|------|
| `LightenViewModel` | 管理点亮状态 | 待开发 |
| `BottomPanel` | 底部可折叠面板 | 待开发 |
| `HandDrawEngine` | 高级手绘效果 | 已存在，待集成 |

## 十二、注意事项

1. **文件位置**：确保 GeoJSON 文件位于 `app/src/main/assets/` 目录
2. **权限**：无需特殊权限，数据从本地 assets 加载
3. **内存**：省份 GeoJSON 约 580KB，解析后会占用一定内存
4. **兼容性**：最低支持 Android API 24（Android 7.0）

## 十三、示例代码

### 13.1 基础显示

```kotlin
@Composable
fun SimpleMapScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        ChinaMapView(
            mapStyle = HandDrawStyle.WATERCOLOR
        )
    }
}
```

### 13.2 带点击反馈

```kotlin
@Composable
fun InteractiveMapScreen() {
    var selectedProvince by remember { mutableStateOf<String?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = selectedProvince?.let { "选中: $it" } ?: "点击地图选择省份",
            modifier = Modifier.padding(16.dp)
        )
        
        ChinaMapView(
            modifier = Modifier.weight(1f),
            onProvinceClick = { adcode ->
                selectedProvince = adcode
            }
        )
    }
}
```

## 十四、版本历史

| 版本 | 日期 | 说明 |
|-----|------|------|
| v1.0 | 2026-03-24 | 初始版本，支持基础地图显示和点击 |
| v1.1 | 2026-03-24 | 添加手势支持：拖动平移、双指缩放 |
| v1.2 | 2026-03-26 | 添加 ChinaMapViewSVG 组件（WebView + SVG 方案） |

---

**作者**：AI Assistant  
**创建日期**：2026-03-24  
**最后更新**：2026-03-26
