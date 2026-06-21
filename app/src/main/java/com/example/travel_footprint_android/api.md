# AppService API 文档

## 目录

1. [旅程管理](#一旅程管理)
2. [足迹管理](#二足迹管理)
3. [标签管理](#三标签管理)
4. [位置服务](#四位置服务)
5. [照片管理](#五照片管理)
6. [点亮城市](#六点亮城市)
7. [数据类型说明](#七数据类型说明)
8. [完整使用示例](#八完整使用示例)


## 一、旅程管理

### 1. createJourney - 创建旅程

| 项目 | 内容 |
|:---|:---|
| 参数 | `title: String`（旅程名称）, `style: String`（风格，默认watercolor）, `description: String`（描述，可选） |
| 作用 | 创建一个新旅程 |
| 输出 | `Long` - 新旅程的ID |
| 示例 | `val journeyId = appService.createJourney("北京之旅")` |

### 2. getAllJourneys - 获取所有旅程

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取所有旅程列表，数据变化时自动更新 |
| 输出 | `Flow<List<Journey>>` |
| 示例 | `appService.getAllJourneys().collect { journeys -> }` |

### 3. getJourneyById - 获取单个旅程

| 项目 | 内容 |
|:---|:---|
| 参数 | `id: Long`（旅程ID） |
| 作用 | 根据ID获取旅程详情 |
| 输出 | `Flow<Journey>` |
| 示例 | `appService.getJourneyById(1L).collect { journey -> }` |

### 4. updateJourney - 更新旅程

| 项目 | 内容 |
|:---|:---|
| 参数 | `journey: Journey`（包含新信息的旅程对象） |
| 作用 | 更新旅程信息 |
| 输出 | 无 |
| 示例 | `appService.updateJourney(updatedJourney)` |

### 5. deleteJourney - 删除旅程

| 项目 | 内容 |
|:---|:---|
| 参数 | `journeyId: Long`（要删除的旅程ID） |
| 作用 | 删除旅程及其所有关联数据 |
| 输出 | 无 |
| 示例 | `appService.deleteJourney(1L)` |

### 6. searchJourneys - 搜索旅程

| 项目 | 内容 |
|:---|:---|
| 参数 | `keyword: String`（搜索关键词） |
| 作用 | 按标题或描述搜索旅程 |
| 输出 | `List<Journey>` - 匹配的旅程列表 |
| 示例 | `val results = appService.searchJourneys("北京")` |

### 7. getFootprintCount - 获取单个旅程的足迹数量

| 项目 | 内容 |
|:---|:---|
| 参数 | `journeyId: Long`（旅程ID） |
| 作用 | 获取该旅程有多少个足迹 |
| 输出 | `Int` - 足迹数量 |
| 示例 | `val count = appService.getFootprintCount(1L)` |

### 8. getAllFootprintCounts - 获取所有旅程的足迹数量

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 批量获取所有旅程的足迹数量 |
| 输出 | `Map<Long, Int>` - key是旅程ID，value是足迹数量 |
| 示例 | `val counts = appService.getAllFootprintCounts()` |


## 二、足迹管理

### 9. getFootprintsForMap - 获取旅程的所有足迹

| 项目 | 内容 |
|:---|:---|
| 参数 | `journeyId: Long`（旅程ID） |
| 作用 | 获取该旅程的所有足迹，用于地图显示 |
| 输出 | `Flow<List<Footprint>>` |
| 示例 | `appService.getFootprintsForMap(1L).collect { footprints -> }` |

### 10. addFootprint - 添加足迹

| 项目 | 内容 |
|:---|:---|
| 参数 | `journeyId: Long`, `lat: Double`, `lng: Double`, `notes: String`, `photos: List<String>?`（可选） |
| 作用 | 添加新足迹，自动获取地址和位置 |
| 输出 | `Long` - 新足迹的ID |
| 示例 | `val id = appService.addFootprint(1L, 39.9042, 116.4074, "天安门广场")` |

### 11. updateFootprint - 更新足迹

| 项目 | 内容 |
|:---|:---|
| 参数 | `footprint: Footprint`（包含新信息的足迹对象） |
| 作用 | 更新足迹信息 |
| 输出 | 无 |
| 示例 | `appService.updateFootprint(updatedFootprint)` |

### 12. deleteFootprint - 删除足迹

| 项目 | 内容 |
|:---|:---|
| 参数 | `footprintId: Long`（足迹ID） |
| 作用 | 删除单个足迹及其关联数据 |
| 输出 | 无 |
| 示例 | `appService.deleteFootprint(1L)` |

### 13. clearAllFootprints - 清空旅程所有足迹

| 项目 | 内容 |
|:---|:---|
| 参数 | `journeyId: Long`（旅程ID） |
| 作用 | 删除该旅程下的所有足迹 |
| 输出 | 无 |
| 示例 | `appService.clearAllFootprints(1L)` |

### 14. getFootprintDetail - 获取足迹详情

| 项目 | 内容 |
|:---|:---|
| 参数 | `footprintId: Long`（足迹ID） |
| 作用 | 获取足迹详情，包含关联的照片 |
| 输出 | `Flow<FootprintWithMedia>` |
| 示例 | `appService.getFootprintDetail(1L).collect { detail -> }` |


## 三、标签管理

### 15. addTagToFootprint - 添加标签

| 项目 | 内容 |
|:---|:---|
| 参数 | `footprintId: Long`（足迹ID）, `tagName: String`（标签名称） |
| 作用 | 为足迹添加一个标签 |
| 输出 | 无 |
| 示例 | `appService.addTagToFootprint(1L, "美食")` |

### 16. getTagsByFootprint - 获取足迹的标签

| 项目 | 内容 |
|:---|:---|
| 参数 | `footprintId: Long`（足迹ID） |
| 作用 | 获取该足迹的所有标签 |
| 输出 | `List<String>` - 标签名称列表 |
| 示例 | `val tags = appService.getTagsByFootprint(1L)` |

### 17. getFootprintsByTag - 根据标签获取足迹

| 项目 | 内容 |
|:---|:---|
| 参数 | `tagName: String`（标签名称） |
| 作用 | 获取带有该标签的所有足迹 |
| 输出 | `List<Footprint>` |
| 示例 | `val footprints = appService.getFootprintsByTag("美食")` |


## 四、位置服务

### 18. getCurrentLocation - 获取当前位置

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取当前设备的经纬度 |
| 输出 | `Location?` - 包含latitude和longitude |
| 示例 | `val location = appService.getCurrentLocation()` |

### 19. getCurrentLocationDetail - 获取当前位置详情

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取当前位置的完整信息（省份、城市、区县、地址、经纬度） |
| 输出 | `LocationDetail?` |
| 示例 | `val detail = appService.getCurrentLocationDetail()` |

**LocationDetail 包含的字段：**
- `province: String` - 省份
- `city: String` - 城市
- `district: String` - 区县
- `address: String` - 详细地址
- `latitude: Double` - 纬度
- `longitude: Double` - 经度

### 20. getCurrentProvince - 获取当前省份

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取当前位置的省份名称 |
| 输出 | `String` |
| 示例 | `val province = appService.getCurrentProvince()` |

### 21. getCurrentCity - 获取当前城市

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取当前位置的城市名称 |
| 输出 | `String` |
| 示例 | `val city = appService.getCurrentCity()` |

### 22. getCurrentDistrict - 获取当前区县

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取当前位置的区县名称 |
| 输出 | `String` |
| 示例 | `val district = appService.getCurrentDistrict()` |

### 23. reverseGeocode - 经纬度转地址

| 项目 | 内容 |
|:---|:---|
| 参数 | `lat: Double`（纬度）, `lng: Double`（经度） |
| 作用 | 将经纬度坐标转换为地址文字 |
| 输出 | `String` - 地址 |
| 示例 | `val address = appService.reverseGeocode(39.9042, 116.4074)` |

### 24. calculateDistance - 计算两点距离

| 项目 | 内容 |
|:---|:---|
| 参数 | `lat1: Double`, `lng1: Double`, `lat2: Double`, `lng2: Double` |
| 作用 | 计算两个坐标之间的直线距离 |
| 输出 | `Float` - 距离（米） |
| 示例 | `val distance = appService.calculateDistance(39.9042, 116.4074, 39.9928, 116.2736)` |


## 五、照片管理

### 25. savePhotoToLocal - 保存照片

| 项目 | 内容 |
|:---|:---|
| 参数 | `uri: String`（照片的Uri地址） |
| 作用 | 将照片保存到应用本地目录 |
| 输出 | `String` - 保存后的本地路径 |
| 示例 | `val path = appService.savePhotoToLocal("content://...")` |

### 26. getAllPhotos - 获取所有照片

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取所有已保存的照片，实时更新 |
| 输出 | `Flow<List<MediaAttachment>>` |
| 示例 | `appService.getAllPhotos().collect { photos -> }` |

### 27. getPhotosByFootprint - 获取足迹的照片

| 项目 | 内容 |
|:---|:---|
| 参数 | `footprintId: Long`（足迹ID） |
| 作用 | 获取该足迹下的所有照片 |
| 输出 | `List<MediaAttachment>` |
| 示例 | `val photos = appService.getPhotosByFootprint(1L)` |

### 28. deletePhoto - 删除照片

| 项目 | 内容 |
|:---|:---|
| 参数 | `mediaId: Long`（照片ID） |
| 作用 | 删除照片文件和数据 |
| 输出 | 无 |
| 示例 | `appService.deletePhoto(1L)` |


## 六、点亮城市

### 29. lightCity - 点亮城市

| 项目 | 内容 |
|:---|:---|
| 参数 | `cityAdcode: String`, `cityName: String`, `provinceAdcode: String`, `provinceName: String`, `latitude: Double`, `longitude: Double`, `remark: String`（可选） |
| 作用 | 记录用户点亮了一个城市 |
| 输出 | `Long` - 记录ID，-1表示已点亮 |
| 示例 | `val id = appService.lightCity("110100", "北京市", "110000", "北京市", 39.9042, 116.4074)` |

### 30. unlightCity - 取消点亮城市

| 项目 | 内容 |
|:---|:---|
| 参数 | `cityAdcode: String`（城市代码） |
| 作用 | 取消点亮某个城市 |
| 输出 | 无 |
| 示例 | `appService.unlightCity("110100")` |

### 31. getAllLightedCities - 获取所有点亮城市

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取所有已点亮城市的列表 |
| 输出 | `Flow<List<LightedCity>>` |
| 示例 | `appService.getAllLightedCities().collect { cities -> }` |

### 32. getLightedCityCount - 获取点亮城市数量

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取一共点亮了多少个城市 |
| 输出 | `Int` |
| 示例 | `val count = appService.getLightedCityCount()` |

### 33. isCityLighted - 检查城市是否已点亮

| 项目 | 内容 |
|:---|:---|
| 参数 | `cityAdcode: String`（城市代码） |
| 作用 | 判断某个城市是否已经被点亮 |
| 输出 | `Boolean` - true已点亮，false未点亮 |
| 示例 | `val isLighted = appService.isCityLighted("110100")` |


## 七、数据类型说明

### Journey（旅程）

| 字段 | 类型 | 说明 |
|:---|:---|:---|
| id | Long | 旅程ID |
| title | String | 标题 |
| description | String | 描述 |
| startDate | Date | 开始日期 |
| endDate | Date | 结束日期 |

### Footprint（足迹）

| 字段 | 类型 | 说明 |
|:---|:---|:---|
| id | Long | 足迹ID |
| journeyId | Long | 所属旅程ID |
| title | String | 标题 |
| description | String | 描述 |
| createTime | Date | 创建时间 |
| address | String | 地址 |
| rating | Int | 评分1-5 |

### LightedCity（点亮城市）

| 字段 | 类型 | 说明 |
|:---|:---|:---|
| cityAdcode | String | 城市代码 |
| cityName | String | 城市名称 |
| provinceName | String | 省份名称 |
| lightedTime | Date | 点亮时间 |

## 八、点亮省份管理

### 34. getLightedProvinces - 获取所有点亮省份

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取所有已点亮省份列表（基于点亮城市去重） |
| 输出 | `List<LightedProvince>` - 包含省份名称和代码 |
| 示例 | `val provinces = appService.getLightedProvinces()` |

### 35. getLightedProvinceCount - 获取点亮省份数量

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 获取一共点亮了多少个省份 |
| 输出 | `Int` |
| 示例 | `val count = appService.getLightedProvinceCount()` |

### 36. isProvinceLighted - 检查省份是否已点亮

| 项目 | 内容 |
|:---|:---|
| 参数 | `provinceAdcode: String`（省份代码） |
| 作用 | 判断某个省份是否已被点亮（该省份下是否有城市被点亮） |
| 输出 | `Boolean` - true已点亮，false未点亮 |
| 示例 | `val isLighted = appService.isProvinceLighted("110000")` |

### 37. getLightedCitiesByProvince - 获取省份下的点亮城市

| 项目 | 内容 |
|:---|:---|
| 参数 | `provinceAdcode: String`（省份代码） |
| 作用 | 获取某个省份下所有被点亮的城市 |
| 输出 | `Flow<List<LightedCity>>` |
| 示例 | `appService.getLightedCitiesByProvince("110000").collect { cities -> }` |

### 38. getLightedCitiesCountByProvince - 按省份统计点亮城市

| 项目 | 内容 |
|:---|:---|
| 参数 | 无 |
| 作用 | 按省份分组，统计每个省份点亮的城市数量 |
| 输出 | `List<ProvinceCityCount>` - 包含省份名称、代码、城市数量 |
| 示例 | `val stats = appService.getLightedCitiesCountByProvince()` |


## 九、数据类型说明

### LightedProvince（点亮省份）

| 字段 | 类型 | 说明 |
|:---|:---|:---|
| provinceName | String | 省份名称 |
| provinceAdcode | String | 省份行政区划代码 |

### ProvinceCityCount（省份城市统计）

| 字段 | 类型 | 说明 |
|:---|:---|:---|
| provinceName | String | 省份名称 |
| provinceAdcode | String | 省份行政区划代码 |
| cityCount | Int | 该省份下点亮的城市数量 |

## 十、完整使用示例

```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val appService: AppService
) : ViewModel() {

    fun createJourney() {
        viewModelScope.launch {
            val id = appService.createJourney("北京之旅")
            Log.d("成功", "旅程ID: $id")
        }
    }

    fun loadJourneys() {
        viewModelScope.launch {
            appService.getAllJourneys().collect { journeys ->
                _journeys.value = journeys
            }
        }
    }

    fun lightCity() {
        viewModelScope.launch {
            val id = appService.lightCity(
                cityAdcode = "110100",
                cityName = "北京市",
                provinceAdcode = "110000",
                provinceName = "北京市",
                latitude = 39.9042,
                longitude = 116.4074
            )
            if (id == -1L) {
                // 已点亮
            }
        }
    }

    fun getMyLocation() {
        viewModelScope.launch {
            val detail = appService.getCurrentLocationDetail()
            detail?.let {
                println("${it.province} ${it.city}")
            }
        }
    }
}

文档版本： v1.0
更新日期： 2026-04-19