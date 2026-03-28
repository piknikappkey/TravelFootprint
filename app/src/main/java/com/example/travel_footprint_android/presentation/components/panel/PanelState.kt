package com.example.travel_footprint_android.presentation.components.panel

/**
 * 底部面板状态枚举
 *
 * 定义了底部面板的三种状态：
 * - COLLAPSED: 默认收起状态，只读模式，显示标题和少量城市列表
 * - EXPANDED: 展开状态，只读模式，显示完整城市列表
 * - EDIT_MODE: 编辑模式，可修改点亮的城市和时间
 */
enum class PanelState {
    /**
     * 收起状态
     * - 高度：250dp
     * - 显示：标题 + 2-3行城市列表
     * - 模式：只读
     */
    COLLAPSED,

    /**
     * 展开状态
     * - 高度：450dp
     * - 显示：完整城市列表
     * - 模式：只读
     */
    EXPANDED,

    /**
     * 编辑模式
     * - 高度：450dp
     * - 显示：省份-城市选择器 + 日期选择器
     * - 模式：可编辑
     */
    EDIT_MODE
}
