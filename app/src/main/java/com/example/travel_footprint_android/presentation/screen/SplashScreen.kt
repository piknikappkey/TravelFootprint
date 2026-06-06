/*
 * ============================================================================
 * SplashScreen.kt - 应用启动闪屏动画组件
 * ============================================================================
 *
 * 【用途】
 *   - 应用启动时展示的过渡动画界面
 *   - 在 MainActivity 初始化期间提供视觉反馈，提升用户体验
 *
 * 【功能】
 *   1. 背景图片淡入效果（透明度 0 → 1，400ms）
 *   2. 中心 Logo 图标缩放动画（1.0x → 1.5x，400ms）
 *   3. 背景图片淡出效果（透明度 1 → 0，300ms）
 *   4. 通过回调通知父组件：何时开始渲染主界面、何时移除闪屏层
 *
 * 【关联组件】
 *   - BGImgBox: 背景图片容器组件，负责加载并绘制全屏背景图，支持随机图片选择
 *   - MainScreen2: 调用方组件，接收 onShowScreen 和 onFinished 回调控制界面切换
 *   - R.drawable.bg_rectangular_1__3__0: 闪屏背景图片资源
 *   - R.drawable.ic_map: 闪屏中心 Logo 图标（地图图标）
 *
 * 【实现逻辑简述】
 *   - 使用 Animatable 控制整体透明度动画（淡入 + 淡出）
 *   - 使用 mutableStateOf + animateFloatAsState 控制 Logo 缩放动画
 *   - LaunchedEffect 编排整个动画时序：淡入 → 通知显示主界面 → 延迟 → Logo放大 → 淡出 → 通知移除闪屏
 *   - 动画时间线：
 *       0ms    : 开始淡入（alpha 0→1，400ms）
 *       400ms  : 触发 onShowScreen，主界面开始预渲染
 *       1000ms : Logo 开始放大（1.0→1.5x，400ms）
 *       1100ms : 开始淡出（alpha 1→0，300ms）
 *       1400ms : 触发 onFinished，闪屏层被移除
 * ============================================================================
 */

package com.example.travel_footprint_android.presentation.screen

// Compose 动画核心库
import androidx.compose.animation.core.Animatable        // 可变动画值，支持手动控制动画进程
import androidx.compose.animation.core.animateFloatAsState // 自动响应状态变化的浮点动画
import androidx.compose.animation.core.tween             // 线性缓动动画规格（指定持续时间和曲线）

// Compose 基础组件
import androidx.compose.foundation.Image                 // 图片显示组件
import androidx.compose.foundation.layout.fillMaxSize    // 填充父容器全部尺寸的修饰符
import androidx.compose.foundation.layout.size           // 设置固定尺寸的修饰符

// Compose 运行时
import androidx.compose.runtime.Composable               // 声明 Composable 函数
import androidx.compose.runtime.LaunchedEffect           // 在组合时启动协程，键值变化时重新执行
import androidx.compose.runtime.getValue                 // 读取 State 值
import androidx.compose.runtime.mutableStateOf           // 创建可变状态
import androidx.compose.runtime.remember                 // 记住状态值避免重组时丢失
import androidx.compose.runtime.setValue                 // 修改 State 值

// Compose UI 修饰符
import androidx.compose.ui.Alignment                     // 对齐方式枚举（居中、顶部等）
import androidx.compose.ui.Modifier                      // UI 修饰符链
import androidx.compose.ui.draw.alpha                    // 透明度修饰符
import androidx.compose.ui.draw.scale                    // 缩放修饰符
import androidx.compose.ui.graphics.Color                // 颜色类
import androidx.compose.ui.graphics.ColorFilter

// 资源加载
import androidx.compose.ui.res.painterResource           // 从资源 ID 加载图片绘制器
import androidx.compose.ui.unit.dp                       // 密度无关像素单位

// 项目资源
import com.example.travel_footprint_android.R            // 应用资源引用（图片、颜色等）
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox  // 背景图片容器组件
import com.example.travel_footprint_android.ui.theme.SecondColor3

// 协程工具
import kotlinx.coroutines.delay                          // 协程延迟函数

/**
 * 应用启动闪屏动画 Composable 函数
 *
 * 动画时序：
 * 1. 背景淡入（400ms）→ 2. 通知父组件显示主界面 → 3. 等待 600ms
 * 4. Logo 放大（400ms）→ 5. 等待 100ms → 6. 背景淡出（300ms）→ 7. 通知父组件移除闪屏
 *
 * @param onFinished 闪屏动画完全结束后回调，父组件接收到此回调后应设置 showSplash = false 移除闪屏层
 * @param onShowScreen 闪屏淡入完成后回调，父组件接收到此回调后应设置 showScreen = true 开始渲染主界面
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    onShowScreen: () -> Unit,
) {
    // 整体透明度动画控制器，初始值为 0（完全透明/不可见）
    // 使用 Animatable 而非 animateFloatAsState 因为需要精确控制动画时序
    val alpha = remember { Animatable(0f) }

    // 控制 Logo 是否放大的状态，初始为 false（原始大小）
    var iconLargen by remember { mutableStateOf(false) }

    // Logo 缩放动画：响应 iconLargen 状态变化自动播放
    // false → 1.0 倍原始大小，true → 1.5 倍放大效果
    val aniIconScale by animateFloatAsState(
        targetValue = if (iconLargen) 1.5f else 1f,
        animationSpec = tween(durationMillis = 400), // 缩放动画持续 400ms
        label = "aniImgRainAlpha"
    )

    // 编排整个闪屏动画的时序（仅在首次组合时执行一次，key 为 Unit）
    LaunchedEffect(Unit) {
        // 阶段1：背景淡入，透明度从 0 动画到 1，持续 400ms
        alpha.animateTo(1f, animationSpec = tween(500))
        // 阶段2：通知父组件开始渲染主界面（此时闪屏仍在显示，主界面可在后台预渲染）
        onShowScreen()
        // 阶段3：等待 600ms，让用户欣赏完整的闪屏画面
        delay(600)

        // 阶段4：触发 Logo 放大动画（iconLargen 变为 true，aniIconScale 自动过渡到 1.5f）
        iconLargen = true

        // 阶段5：等待 100ms，让 Logo 放大动画先播放一小段时间
        delay(100)

        // 阶段6：背景淡出，透明度从 1 动画到 0，持续 300ms
        alpha.animateTo(0f, animationSpec = tween(300))

        // 阶段7：通知父组件闪屏动画已结束，可以安全移除闪屏层
        onFinished()


    }

    // 背景图片容器：加载闪屏背景图并居中放置内容
    BGImgBox(
        imgList = listOf(R.drawable.bg_rectangular_1__3__0), // 背景图片资源列表（仅一张）
        modifier = Modifier
            .fillMaxSize(),     // 全屏显示
        alpha = alpha.value, // 整体透明度由 alpha 动画控制器驱动
        drawRectColor = Color.Transparent, // 背景上覆盖透明色层（不额外着色）
        contentAlignment = Alignment.Center // 内容居中对齐
    ) {
        // 中心 Logo 图标：地图图标，应用品牌标识
        Image(
            painter = painterResource(id = R.drawable.ic_map), // 加载地图图标资源
            contentDescription = null, // 无无障碍描述（装饰性图片）
            modifier = Modifier
                .size(140.dp)            // 固定尺寸 140dp × 140dp
                .scale(aniIconScale)     // 应用缩放动画（1.0x → 1.5x）
                .alpha(alpha.value),     // 透明度与背景同步
            colorFilter = ColorFilter.tint(SecondColor3), // 主题色染色
        )
    }
}
