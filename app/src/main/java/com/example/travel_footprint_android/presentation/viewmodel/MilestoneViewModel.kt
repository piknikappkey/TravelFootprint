package com.example.travel_footprint_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.data.repository.JourneyRepository
import com.example.travel_footprint_android.data.repository.FootprintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.travel_footprint_android.presentation.components.milestone.MileageData
import com.example.travel_footprint_android.presentation.components.milestone.Milestone
import com.example.travel_footprint_android.presentation.components.milestone.MonthGroup
import com.example.travel_footprint_android.presentation.components.milestone.UnlockCondition
import com.example.travel_footprint_android.presentation.components.milestone.calculateMileageFromFootprints
import com.example.travel_footprint_android.presentation.components.milestone.groupFootprintsByMonth
import com.example.travel_footprint_android.presentation.components.milestone.milestones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * MilestoneViewModel - 里程碑面板的状态管理中心
 *
 * 职责：
 * - 接收外部数据源（足迹列表、已点亮城市列表、已点亮省份数量）
 * - 计算派生数据（里程统计、月份分组、成就解锁状态）
 * - 管理 UI 状态（展开/收起控制）
 */
@HiltViewModel
class MilestoneViewModel @Inject constructor(
    private val journeyRepository: JourneyRepository,
    private val footprintRepository: FootprintRepository
) : ViewModel() {

    // ==================== 输入数据源 ====================

    private val _allFootprints = MutableStateFlow<List<Footprint>>(emptyList())
    private val _lightedProvinceCount = MutableStateFlow(0)
    val lightedProvinceCount: StateFlow<Int> = _lightedProvinceCount.asStateFlow()
    private val _lightCityList = MutableStateFlow<List<LightedCity>>(emptyList())
    
    // 新增数据源
    private val _journeyCount = MutableStateFlow(0)
    val journeyCount: StateFlow<Int> = _journeyCount.asStateFlow()
    
    private val _footprintCount = MutableStateFlow(0)
    val footprintCount: StateFlow<Int> = _footprintCount.asStateFlow()
    
    private val _coverCount = MutableStateFlow(0)
    val coverCount: StateFlow<Int> = _coverCount.asStateFlow()
    
    private val _imageCount = MutableStateFlow(0)
    val imageCount: StateFlow<Int> = _imageCount.asStateFlow()

    /**
     * 从外部同步数据（由 Screen 层调用）
     */
    fun updateData(
        footprints: List<Footprint>,
        provinceCount: Int,
        cityList: List<LightedCity>
    ) {
        _allFootprints.value = footprints
        _lightedProvinceCount.value = provinceCount
        _lightCityList.value = cityList
        
        // 异步获取新数据
        viewModelScope.launch {
            _journeyCount.value = journeyRepository.getJourneyCount()
            _footprintCount.value = footprintRepository.getTotalFootprintCount()
            _coverCount.value = journeyRepository.getCoveredJourneyCount()
            _imageCount.value = journeyRepository.getTotalImageCount()
        }
    }

    // ==================== 派生数据 ====================

    /** 里程统计（总里程 + 近6个月月度里程） */
    val mileageData: StateFlow<MileageData> = _allFootprints
        .map { calculateMileageFromFootprints(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MileageData(totalKm = 0.0, monthlyData = emptyList()))

    /** 总里程（km） */
    val totalKm: StateFlow<Double> = mileageData
        .map { it.totalKm }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** 足迹按月份分组 */
    val monthGroups: StateFlow<List<MonthGroup>> = _allFootprints
        .map { groupFootprintsByMonth(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 已解锁成就列表 */
    val achievedMilestones: StateFlow<List<Milestone>> = combine(
        _lightedProvinceCount,
        totalKm,
        _journeyCount,
        _footprintCount,
        combine(_coverCount, _imageCount) { cover, image -> Pair(cover, image) }
    ) { provinceCount, km, journeyCount, footprintCount, coverAndImage ->
        val (coverCount, imageCount) = coverAndImage
        milestones.filter { milestone ->
            when (val condition = milestone.condition) {
                is UnlockCondition.Province -> provinceCount >= condition.required
                is UnlockCondition.Mileage -> km >= condition.requiredKm
                is UnlockCondition.JourneyCount -> journeyCount >= condition.required
                is UnlockCondition.FootprintCount -> footprintCount >= condition.required
                is UnlockCondition.CoverCount -> coverCount >= condition.required
                is UnlockCondition.ImageCount -> imageCount >= condition.required
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 全部未解锁成就 */
    val allUnachievedMilestones: StateFlow<List<Milestone>> = combine(
        _lightedProvinceCount,
        totalKm,
        _journeyCount,
        _footprintCount,
        combine(_coverCount, _imageCount) { cover, image -> Pair(cover, image) }
    ) { provinceCount, km, journeyCount, footprintCount, coverAndImage ->
        val (coverCount, imageCount) = coverAndImage
        milestones.filter { milestone ->
            when (val condition = milestone.condition) {
                is UnlockCondition.Province -> provinceCount < condition.required
                is UnlockCondition.Mileage -> km < condition.requiredKm
                is UnlockCondition.JourneyCount -> journeyCount < condition.required
                is UnlockCondition.FootprintCount -> footprintCount < condition.required
                is UnlockCondition.CoverCount -> coverCount < condition.required
                is UnlockCondition.ImageCount -> imageCount < condition.required
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 下一项待解锁成就：每种条件类型只显示门槛最低的一项 */
    val nextUnachievedMilestones: StateFlow<List<Milestone>> = allUnachievedMilestones
        .map { unachieved ->
            unachieved.groupBy { it.condition::class }
                .mapNotNull { (_, group) ->
                    when (val condition = group.first().condition) {
                        is UnlockCondition.Province -> group.minBy {
                            (it.condition as UnlockCondition.Province).required
                        }
                        is UnlockCondition.Mileage -> group.minBy {
                            (it.condition as UnlockCondition.Mileage).requiredKm
                        }
                        is UnlockCondition.JourneyCount -> group.minBy {
                            (it.condition as UnlockCondition.JourneyCount).required
                        }
                        is UnlockCondition.FootprintCount -> group.minBy {
                            (it.condition as UnlockCondition.FootprintCount).required
                        }
                        is UnlockCondition.CoverCount -> group.minBy {
                            (it.condition as UnlockCondition.CoverCount).required
                        }
                        is UnlockCondition.ImageCount -> group.minBy {
                            (it.condition as UnlockCondition.ImageCount).required
                        }
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== UI 状态 ====================

    private val _showAllRecords = MutableStateFlow(false)
    val showAllRecords: StateFlow<Boolean> = _showAllRecords.asStateFlow()

    private val _showAllUnachieved = MutableStateFlow(false)
    val showAllUnachieved: StateFlow<Boolean> = _showAllUnachieved.asStateFlow()

    private val _headExpanded = MutableStateFlow(false)
    val headExpanded: StateFlow<Boolean> = _headExpanded.asStateFlow()

    fun toggleAllRecords() {
        _showAllRecords.value = !_showAllRecords.value
    }

    fun toggleShowAllUnachieved() {
        _showAllUnachieved.value = !_showAllUnachieved.value
    }

    fun toggleHeadExpanded() {
        _headExpanded.value = !_headExpanded.value
    }
}
