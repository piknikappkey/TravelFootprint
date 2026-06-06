/*
 * 文件名：Color.kt
 * 包路径：ui.theme
 *
 * 【用途】
 * 应用全局颜色主题定义文件。集中管理整个应用的所有颜色常量，
 * 包括主色调、副色调、字体颜色、背景颜色、功能按钮颜色、按钮背景色
 * 以及地图用户位置样式颜色。
 *
 * 所有组件统一引用此文件中的颜色常量，而非直接硬编码色值，
 * 从而确保整个应用视觉风格的一致性，并降低后续主题调整的维护成本。
 *
 * 【颜色体系总览】
 * ┌─────────────────────────────────────────────────────────┐
 * │ 类别              │ 命名规则              │ 数量          │
 * ├─────────────────────────────────────────────────────────┤
 * │ MD3 默认色        │ Purple80/Pink40 等    │ 6 种          │
 * │ 主色调（紫色系）  │ MainColor1~4          │ 4 种（由浅到深）│
 * │ 副色调（金色系）  │ SecondColor1~4        │ 4 种（由浅到深）│
 * │ 字体色（灰度）    │ FontLight0~6 / 黑     │ 11 种          │
 * │                   │ FontDark0~8           │               │
 * │ 背景色（浅紫粉系）│ BGLight0~6            │ 6 种（由浅到深）│
 * │ 功能按钮色        │ SaveColor / 绿         │ 3 种          │
 * │                   │ CancelColor / 金       │               │
 * │                   │ DeleteColor / 红       │               │
 * │ 按钮背景色（主）  │ BtnBgColorMain0~3      │ 4 种（紫色系）  │
 * │ 按钮背景色（副）  │ BtnBgColorSecond0~3    │ 4 种（金色系）  │
 * │ 定位相关色        │ LocationStroke/Fill    │ 2 种          │
 * └─────────────────────────────────────────────────────────┘
 *
 * 【功能】
 * 1. 主色调定义：MainColor1~4 紫色渐变系列，用于主按钮、强调元素、活动状态
 * 2. 副色调定义：SecondColor1~4 金色渐变系列，用于辅助元素、装饰、次要强调
 * 3. 字体颜色定义：FontLight 和 FontDark 两套灰度体系，
 *    Light 系列用于深色背景上的文字，Dark 系列用于浅色背景上的文字，
 *    数字越大颜色越深/越亮（如 FontDark0 纯黑、FontLight0 纯白）
 * 4. 背景颜色定义：BGLight0~6 浅紫粉色系渐变，用于卡片、页面、容器背景
 * 5. 功能按钮色：SaveColor（绿色）/ CancelColor（金色）/ DeleteColor（红色），
 *    语义化颜色，用户通过颜色直观理解操作性质
 * 6. 按钮背景色：BtnBgColorMain（紫色主系）和 BtnBgColorSecond（金色副系），
 *    供按钮按下/悬浮等状态使用，各含 4 级深浅变化
 * 7. 定位样式色：LocationStrokeColor（半透明蓝色边框）、
 *    LocationRadiusFillColor（半透明蓝色填充），用于地图用户位置范围指示
 *
 * 【关联组件】
 * - 所有 Button 组件：引用 MainColor、SecondColor、SaveColor/DeleteColor 等
 * - 所有 Text 组件：引用 FontDark2/4/6、FontLight0/1/2 等
 * - BGBox / BGColumn / BGRow 等容器：引用 BGLight 系列
 * - InputText 系列：引用 SecondColor2、MainColor2 作为聚焦/激活边框色
 * - ConfirmDeleteDialog：引用 SecondColor2 作为"取消"按钮背景色
 * - LocationMapView / LocationMarker：引用 LocationStrokeColor / LocationRadiusFillColor
 *
 * 【简单实现逻辑】
 * 1. 导入 Compose Color 类型和 Android graphics Color（用于 ARGB 构造）
 * 2. 按语义分组逐一定义 val 常量，每个常量通过 Color(0xFFRRGGBB) 构造
 * 3. 颜色值使用 8 位十六进制 ARGB 格式：
 *    0xFF = 完全不透明，后 6 位 = RR（红）GG（绿）BB（蓝）
 * 4. 各组件通过 import com.example.travel_footprint_android.ui.theme.* 引用所需颜色
 */
package com.example.travel_footprint_android.ui.theme

import androidx.compose.ui.graphics.Color // Compose 颜色类型，支持 ARGB 构造 Color(0xFFRRGGBB)
import android.graphics.Color as color // Android 原生颜色工具（用于 argb() 方法构造带透明度的颜色）

// ═══════════════════════════════════════════════════════════════
// MD3（Material Design 3）默认调色板
// 由 Compose Material3 模板自动生成，未在项目自定义组件中直接使用，
// 保留以防 Theme.kt 中的 Typography 或 Scheme 默认引用
// ═══════════════════════════════════════════════════════════════
val Purple80 = Color(0xFFD0BCFF) // MD3 浅色主题紫色（80% 亮度）
val PurpleGrey80 = Color(0xFFCCC2DC) // MD3 浅色主题紫灰色
val Pink80 = Color(0xFFEFB8C8) // MD3 浅色主题粉色

val Purple40 = Color(0xFF6650a4) // MD3 深色主题紫色（40% 亮度）
val PurpleGrey40 = Color(0xFF625b71) // MD3 深色主题紫灰色
val Pink40 = Color(0xFF7D5260) // MD3 深色主题粉色

// ═══════════════════════════════════════════════════════════════
// 主色调（紫色系列）
// 用于主要交互元素：主按钮背景、选中态高亮、图标强调等
// 数字越大，颜色越深（1=最浅, 4=最深）
// ═══════════════════════════════════════════════════════════════
val MainColor1 = Color(0xFFD0BCFF) // 淡紫色，用于次要背景或悬停态
val MainColor2 = Color(0xFF9F79FA) // 中紫色，主按钮默认背景色
val MainColor3 = Color(0xFF7F4EF1) // 深紫色，按钮按下态或强调文字
val MainColor4 = Color(0xFF4B00FF) // 最深紫色，用于极端强调或品牌色

// ═══════════════════════════════════════════════════════════════
// 副色调（金色/暖黄色系列）
// 用于次要元素：取消按钮、装饰边框、辅助标签、聚焦指示等
// 数字越大，颜色越深（1=最浅, 4=最深）
// ═══════════════════════════════════════════════════════════════
val SecondColor1 = Color(0xFFFFE9BC) // 浅米金色，辅助背景色
val SecondColor2 = Color(0xFFFDD583) // 中金色，取消按钮背景 / 输入框聚焦边框
val SecondColor3 = Color(0xFFFFBF47) // 深金色，强调装饰
val SecondColor4 = Color(0xFFFFAB00) // 最深金色，品牌点缀

// ═══════════════════════════════════════════════════════════════
// 字体颜色（灰度体系）
// FontLight 系列：用于深色背景上的浅色文字（数字越大越亮）
// FontDark 系列：用于浅色背景上的深色文字（数字越大越深）
// 命名规则：Light=亮色文字，Dark=深色文字，数字=亮度级别
// 特殊：FontDark0 = 纯黑 #000000，FontLight0 = 纯白 #FFFFFF
// ═══════════════════════════════════════════════════════════════
// 字体颜色-亮色系列（用于深/彩色背景）
val FontLight0 = Color(0xFFFFFFFF) // 纯白，用于深色背景上的主文字
val FontLight1 = Color(0xFFEEEEEE) // 极浅灰，用于深色背景上的次要文字
val FontLight2 = Color(0xFFDDDDDD) // 浅灰，禁用态或占位文字
val FontLight4 = Color(0xFFBBBBBB) // 中浅灰，辅助说明文字
val FontLight6 = Color(0xFF999999) // 中灰，弱化文字

// 字体颜色-深色系列（用于浅/白色背景）
val FontDark8 = Color(0xFF888888) // 中灰，次要说明文字
val FontDark6 = Color(0xFF666666) // 中深灰，正文辅助色
val FontDark5 = Color(0xFF555555) // 偏深灰
val FontDark4 = Color(0xFF444444) // 深灰，标题/正文默认色
val FontDark3 = Color(0xFF333333) // 更深灰，强调文字
val FontDark2 = Color(0xFF222222) // 接近纯黑的主文字
val FontDark0 = Color(0xFF000000) // 纯黑，最高强调文字

// ═══════════════════════════════════════════════════════════════
// 背景颜色（浅紫粉色系列）
// 用于页面背景、卡片背景、容器底色
// 数字越大颜色越深（0=最浅, 6=最深）
// ═══════════════════════════════════════════════════════════════
val BGLight0 = Color(0xFFFFFFFF) // 纯白，页面最外层背景
val BGLight1 = Color(0xFFFFFCFF) // 极浅粉色底，主页面背景色
val BGLight2 = Color(0xFFFFFAFF) // 更明显的浅粉色，次要页面背景
val BGLight3 = Color(0xFFFAF1FC) // 浅紫色底，卡片/容器背景
val BGLight4 = Color(0xFFF6E9F8) // 中浅紫底，按钮/输入框背景
val BGLight6 = Color(0xFFF1DDF5) // 较深紫底，按下态背景

val BGPaperTexture = Color(0xFFFCF1EB)

// ═══════════════════════════════════════════════════════════════
// 功能按钮颜色（语义化颜色）
// 通过颜色传递操作语义，帮助用户直观理解操作性质
// ═══════════════════════════════════════════════════════════════
val SaveColor = Color(0xFF97FD89) // 绿色，保存/确认/添加（积极操作）
val CancelColor = Color(0xFFFFBF47) // 金色，取消/返回（中性操作）
val DeleteColor = Color(0xFFFF7B6E) // 红色，删除/移除（危险/破坏性操作）

// ═══════════════════════════════════════════════════════════════
// 按钮背景色（主色调紫色系 & 副色调金色系）
// 用于按钮的背景填充，各含 4 级深浅过渡
// 数字越大颜色越深（0=最浅, 3=最深）
// ═══════════════════════════════════════════════════════════════
// 主色调按钮背景（紫色系列）
val BtnBgColorMain0 = Color(0xFFF2E6FD) // 最浅紫色，禁用态背景
val BtnBgColorMain1 = Color(0xFFEBD5FF) // 浅紫色，默认态背景
val BtnBgColorMain2 = Color(0xFFDDB9FD) // 中紫色，悬浮/按下态背景
val BtnBgColorMain3 = Color(0xFFCD99FC) // 较深紫色，激活态背景
// 副色调按钮背景（金色系列）
val BtnBgColorSecond0 = Color(0xFFFDF2DB) // 最浅金色，禁用态背景
val BtnBgColorSecond1 = Color(0xFFFFE9BC) // 浅金色，默认态背景
val BtnBgColorSecond2 = Color(0xFFFDDD9A) // 中金色，悬浮/按下态背景
val BtnBgColorSecond3 = Color(0xFFFDD37A) // 较深金色，激活态背景

// ═══════════════════════════════════════════════════════════════
// 用户位置样式颜色（地图相关）
// 用于高德地图上用户当前位置的视觉指示
// 使用 Android graphics Color.argb() 方法构造带透明度颜色
// ═══════════════════════════════════════════════════════════════
// 用户位置可能范围的蓝色半透明边框
val LocationStrokeColor = color.argb(100, 100, 200, 255) // R=100, G=200, B=255, α=100（淡蓝色）
// 用户位置可能范围的蓝色半透明填充区域
val LocationRadiusFillColor = color.argb(100, 180, 200, 255) // R=180, G=200, B=255, α=100（浅蓝色）
