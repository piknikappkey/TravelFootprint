/*
 * ============================================================================
 * JourneyEdit.kt - 旅程新增/编辑表单组件
 * ============================================================================
 *
 * 【用途】
 *   - 提供旅程的新增和编辑界面
 *   - 作为旅程面板（JourneyPanel7）的子页面，通过 JourneyNavController 导航到此界面
 *
 * 【功能】
 *   1. 新建旅程：用户填写标题/封面/描述/地址/图片等内容后保存到数据库
 *   2. 编辑已有旅程：加载已有旅程数据，修改后更新到数据库
 *   3. 删除旅程：编辑模式下提供删除按钮，弹出确认对话框后删除
 *   4. 表单验证：保存前校验标题/封面/描述的有效性
 *   5. 支持拖拽手势调整面板高度（通过顶部标题栏的拖拽检测）
 *
 * 【关联组件】
 *   - JourneyHead: 顶部操作栏，包含返回按钮、拖动区域、保存按钮、面板高度切换按钮
 *   - JourneyContent: 可滚动的表单内容区，组合了标题/封面/描述/地址/图片各编辑子组件
 *   - JourneyEditTitle: 旅程标题输入组件（InputText3）
 *   - JourneyEditCover: 封面图片选择组件（ImageSquare2）
 *   - JourneyEditDescription: 旅程描述输入组件（InputText3，最长1024字）
 *   - JourneyEditLocation: 旅程地址选择组件（LocationSearch 地图选点）
 *   - JourneyEditImages: 旅程回忆图片管理组件（Reminiscence）
 *   - ConfirmDeleteDialog: 删除确认弹窗对话框（带"取消"和"删除!"按钮）
 *   - ButtonSave: 保存按钮（带阴影的圆角按钮）
 *   - ButtonDelete: 删除按钮（红色圆角按钮）
 *   - IcJourneyHeightButton: 面板高度切换按钮（带旋转动画的箭头图标）
 *   - BGBox / BGImgBox: 背景容器组件（提供阴影和背景图）
 *   - LineBetween: 虚线分隔线组件
 *   - Headline: 标题文字组件
 *   - Journey 实体: Room 数据表实体，字段包括 title/description/coverImagePath/journeyImagePaths/address/longitude/latitude 等
 *   - JourneyNavController: 旅程面板内部导航控制器
 *   - JourneyPanel2State: 面板状态枚举（JOURNEY_LIST / JOURNEY_EDIT / FOOTPRINT_LIST / FOOTPRINT_EDIT）
 *
 * 【实现逻辑简述】
 *   - journeySelected == null 时进入新建模式，使用默认值构建空白 Journey 对象
 *   - journeySelected != null 时进入编辑模式，通过 copy() 深拷贝已有数据进行编辑
 *   - 保存时进行三重校验：标题不为空且非"新的开始"、封面图片路径不为空、描述不为空且非"这是一段新的旅程"
 *   - 校验通过后调用 addJourney() 或 updateJourney() 回调，并导航回 JOURNEY_LIST
 *   - 各子编辑组件通过回调 setJourney(journey.copy(...)) 实现不可变数据更新，触发 Compose 重组
 *   - 删除操作通过 ConfirmDeleteDialog 确认后执行，删除后导航回列表并清空选中数据
 * ============================================================================
 */

package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit

// Android 系统组件
import android.widget.Toast                                // 轻提示组件，用于显示校验失败信息

// Compose 基础组件
import androidx.compose.foundation.Image                   // 图片显示组件
import androidx.compose.foundation.clickable               // 点击修饰符
import androidx.compose.foundation.gestures.detectVerticalDragGestures  // 垂直拖拽手势检测
import androidx.compose.foundation.layout.Column           // 垂直线性布局
import androidx.compose.foundation.layout.PaddingValues    // 内边距值
import androidx.compose.foundation.layout.Row              // 水平线性布局
import androidx.compose.foundation.layout.Spacer           // 空白占位符
import androidx.compose.foundation.layout.padding          // 内边距修饰符
import androidx.compose.foundation.layout.size             // 固定尺寸修饰符
import androidx.compose.foundation.layout.width            // 宽度修饰符
import androidx.compose.foundation.rememberScrollState     // 记住滚动状态
import androidx.compose.foundation.verticalScroll          // 垂直滚动修饰符

// Compose 运行时
import androidx.compose.runtime.Composable                 // 声明 Composable 函数
import androidx.compose.runtime.getValue                   // 读取 State 值
import androidx.compose.runtime.mutableStateOf             // 创建可变状态
import androidx.compose.runtime.remember                   // 记住状态值避免重组时丢失
import androidx.compose.runtime.setValue                   // 修改 State 值

// Compose UI 修饰符
import androidx.compose.ui.Alignment                       // 对齐方式枚举
import androidx.compose.ui.Modifier                        // UI 修饰符链
import androidx.compose.ui.graphics.ColorFilter            // 图片颜色滤镜
import androidx.compose.ui.input.pointer.pointerInput      // 指针输入处理
import androidx.compose.ui.platform.LocalContext           // 获取当前 Android Context
import androidx.compose.ui.res.painterResource             // 从资源加载图片
import androidx.compose.ui.unit.dp                         // 密度无关像素单位

// 项目内部组件和数据
import com.example.travel_footprint_android.R              // 应用资源引用
import com.example.travel_footprint_android.data.entity.Journey  // 旅程实体类
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox       // 带阴影的背景容器
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox     // 带背景图的容器
import com.example.travel_footprint_android.presentation2.components.button.button_delete.ButtonDelete  // 删除按钮组件
import com.example.travel_footprint_android.presentation2.components.button.button_save.ButtonSave      // 保存按钮组件
import com.example.travel_footprint_android.presentation2.components.journey_panel2.confirm_delete_dialog.ConfirmDeleteDialog  // 删除确认弹窗
import com.example.travel_footprint_android.presentation2.components.journey_panel2.ic_journey_height_button.IcJourneyHeightButton  // 面板高度切换按钮
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.cover.JourneyEditCover             // 封面编辑子组件
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.description.JourneyEditDescription  // 描述编辑子组件
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.images.JourneyEditImages            // 图片编辑子组件
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.location.JourneyEditLocation        // 地址编辑子组件
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.title.JourneyEditTitle              // 标题编辑子组件
import com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between.LineBetween                        // 虚线分隔线
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController                   // 旅程面板导航控制器
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State                     // 面板状态枚举
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline                                          // 标题文字组件
import com.example.travel_footprint_android.ui.theme.SecondColor3      // 主题色（用于返回按钮箭头颜色）

// Java 标准库
import java.util.Date                                                 // 日期类（新建旅程的默认日期）

/**
 * 旅程新增/编辑主 Composable 函数
 *
 * 根据 journeySelected 是否为 null 区分新建/编辑模式：
 * - journeySelected == null：新建旅程，使用默认值初始化
 * - journeySelected != null：编辑已有旅程，深拷贝后编辑
 *
 * @param modifier 外部 Modifier，用于整体布局修饰
 * @param journeySelected 当前选中的旅程（null 表示新建模式）
 * @param navigate 导航回调，参数为 (目标状态, 传递的旅程数据)
 * @param addJourney 新增旅程回调
 * @param updateJourney 更新旅程回调
 * @param deleteJourney 删除旅程回调
 * @param journeyPanelHeightState 面板高度状态（true=高，false=低）
 * @param setJourneyPanelHeightState 设置面板高度状态的回调
 * @param setIsDragging 设置拖拽状态的回调（用于面板整体拖拽）
 * @param onDragDelta 拖拽位移量回调
 */
@Composable
fun JourneyEdit(
    modifier: Modifier = Modifier,
    journeySelected: Journey? = null,
    navigate: (JourneyPanel2State, Journey?) -> Unit,
    addJourney: (Journey) -> Unit,
    updateJourney: (Journey) -> Unit,
    deleteJourney: (Journey) -> Unit,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
    ) {
    // 本地编辑的旅程状态，支持通过 remember 保存
    // 编辑模式：深拷贝 journeySelected 以避免直接修改原始数据
    // 新建模式：创建带默认值的空白 Journey 对象
    var journey by remember { mutableStateOf(
        journeySelected?.copy()
            ?: Journey(
                title = "",
                description = "",
                startDate = Date(),
                endDate = Date(),
                coverStyle = "",
                coverImagePath = "",
                journeyImagePaths = List(0, { i -> "" })  // 空的图片路径列表
            )
        )
    }

    // 删除确认弹窗是否显示的状态
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 垂直布局：顶部操作栏 + 可滚动表单内容
    Column {
        // 顶部操作栏：返回按钮、标题（可拖拽）、保存按钮、高度切换按钮
        JourneyHead(
            journey,
            journeySelected,
            navigate,
            addJourney,
            updateJourney,
            journeyPanelHeightState,
            setJourneyPanelHeightState,
            setIsDragging = setIsDragging,
            onDragDelta = onDragDelta,
        )
        // 可滚动的表单内容区：标题/封面/描述/地址/图片编辑
        JourneyContent(
            modifier,
            journey,
            journeySelected,
            // 更新 journey 状态用：通过 copy() 创建新对象触发重组
            { j -> journey = j.copy() },
            // 触发删除确认弹窗
            { showDeleteDialog = true },
        )

        // 删除确认弹窗：仅在编辑模式（journeySelected != null）且弹窗开启时显示
        if (showDeleteDialog && journeySelected != null) {
            ConfirmDeleteDialog(
                title = "删除旅程",
                message = "确定要删除「${journeySelected.title}」吗？此操作不可撤销。",
                onConfirm = {
                    // 确认删除：执行删除回调 → 导航回列表 → 关闭弹窗
                    deleteJourney(journeySelected)
                    JourneyNavController.navigate(JourneyPanel2State.JOURNEY_LIST, null)
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }  // 取消：仅关闭弹窗
            )
        }
    }
}


/**
 * 旅程编辑界面顶部操作栏
 *
 * 水平布局：返回箭头 | 标题（可拖拽） | 保存按钮 | 面板高度切换按钮
 * 标题区域支持垂直拖拽手势，用于整体控制面板的高度/位置变化
 *
 * @param journey 当前编辑中的旅程数据
 * @param journeySelected 原始选中的旅程（用于判断新建/编辑模式）
 * @param navigate 导航回调
 * @param addJourney 新增旅程回调
 * @param updateJourney 更新旅程回调
 * @param journeyPanelHeightState 面板高度状态
 * @param setJourneyPanelHeightState 设置面板高度状态
 * @param setIsDragging 标记是否正在拖拽
 * @param onDragDelta 拖拽位移量回调
 */
@Composable
fun JourneyHead(
    journey: Journey,
    journeySelected: Journey? = null,
    navigate: (JourneyPanel2State, Journey?) -> Unit,
    addJourney: (Journey) -> Unit,
    updateJourney: (Journey) -> Unit,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
) {
    // 获取当前本地 Context，用于 Toast 提示
    val context = LocalContext.current

    // 水平布局
    Row(
        verticalAlignment = Alignment.CenterVertically  // 所有子元素垂直居中
    ){
        // 左侧：返回箭头图标
        Image(
            modifier = Modifier
                .size(26.dp)
                .padding(start = 5.dp)
                .clickable(onClick = {
                    // 新建模式下返回列表时传 null，编辑模式下传当前选中旅程
                    if(journeySelected == null) {
                        navigate(JourneyPanel2State.JOURNEY_LIST, null)
                    } else {
                        navigate(JourneyPanel2State.JOURNEY_LIST, journeySelected)
                    }
                }),
            painter = painterResource(id = R.drawable.ic_left_long),  // 左箭头图标
            contentDescription = "返回图标",
            colorFilter = ColorFilter.tint(SecondColor3),  // 着色为主题色
        )

        // 中间：标题文字（占据剩余空间，支持垂直拖拽手势）
        Headline(
            text = "开启新旅程",
            modifier = Modifier
                .weight(1f)  // 占据剩余全部空间
                .padding(vertical = 5.dp, horizontal = 3.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { setIsDragging(true) },   // 开始拖拽时标记
                        onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },  // 拖拽中传递位移量
                        onDragEnd = { setIsDragging(false) }     // 拖拽结束取消标记
                    )
                },
        )

        // 右侧：保存按钮（带表单验证）
        ButtonSave(
            onClick = {
                // 三重表单验证
                val titleValid = journey.title.isNotBlank() && journey.title != "新的开始"
                val coverValid = journey.coverImagePath.isNotBlank()
                val descValid = journey.description.isNotBlank() && journey.description != "这是一段新的旅程"

                when {
                    !titleValid -> Toast.makeText(context, "请输入有效的旅程标题", Toast.LENGTH_SHORT).show()
                    !coverValid -> Toast.makeText(context, "请选择封面图片", Toast.LENGTH_SHORT).show()
                    !descValid -> Toast.makeText(context, "请输入有效的旅程描述", Toast.LENGTH_SHORT).show()
                    else -> {
                        // 校验通过：新建或更新旅程
                        if (journeySelected == null) {
                            addJourney(journey)    // 新建模式
                        } else {
                            updateJourney(journey) // 编辑模式
                        }
                        navigate(JourneyPanel2State.JOURNEY_LIST, null)  // 保存后返回列表
                    }
                }
            }
        )

        Spacer(Modifier.width(10.dp))

        // 面板高度切换按钮（展开/收起箭头，带旋转动画）
        IcJourneyHeightButton(journeyPanelHeightState, { setJourneyPanelHeightState(!journeyPanelHeightState) })

        Spacer(Modifier.width(10.dp))
    }
}

/**
 * 旅程编辑表单内容区（可滚动）
 *
 * 按顺序展示各编辑字段：标题 → 封面 → 描述 → 地址 → 图片 → 删除按钮（仅编辑模式）
 *
 * @param modifier 外部 Modifier
 * @param journey 当前编辑中的旅程数据
 * @param journeySelected 原始选中的旅程（null 时不显示删除按钮）
 * @param setJourney 更新旅程数据的回调
 * @param deleteJourney 触发删除确认弹窗的回调
 */
@Composable
fun JourneyContent(
    modifier: Modifier = Modifier,
    journey: Journey,
    journeySelected: Journey? = null,
    setJourney: (Journey) -> Unit,
    deleteJourney: (Journey) -> Unit,
) {
    // 可垂直滚动的表单容器
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())  // 支持滚动，内容可能超出屏幕
    ) {
        Spacer(Modifier.padding(2.dp))

        // 外层背景容器（带阴影 + 圆角）
        BGBox(
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            // 内层背景图片容器（随机选择背景图之一）
            BGImgBox(
                imgList = listOf<Int>(
                    R.drawable.bg_rectangular_1__3__0,  // 背景图1
                    R.drawable.bg_rectangular_1__3__1,  // 背景图2
                    R.drawable.bg_rectangular_1__3__2   // 背景图3
                ),
            ) {
                Column {
                    Spacer(Modifier.padding(3.dp))

                    // ======== 旅程标题编辑 ========
                    // 使用 InputText3 文本输入框，最长 20 字
                    JourneyEditTitle(
                        journey = journey,
                        onValueChange = { text -> setJourney(journey.copy(title = text)) }
                    )
                    // 虚线分隔线（默认样式：SecondColor2 颜色，95%宽度）
                    LineBetween()

                    // ======== 封面图片编辑 ========
                    // 使用 ImageSquare2 组件选择/替换/删除封面图片
                    JourneyEditCover(
                        journey = journey,
                        updateImgPath = { file ->
                            setJourney(journey.copy(coverImagePath = file.absolutePath))  // 保存图片绝对路径
                            file  // 返回 File 供 ImageSquare2 内部使用
                        },
                        deleteImgPath = { imgPath ->
                            setJourney(journey.copy(coverImagePath = ""))  // 清空封面路径
                        }
                    )
                    LineBetween()

                    // ======== 旅程描述编辑 ========
                    // 使用 InputText3 多行文本输入框，最长 1024 字
                    JourneyEditDescription(
                        journey = journey,
                        onValueChange = { text -> setJourney(journey.copy(description = text)) }
                    )
                    LineBetween()

                    // ======== 旅程地址 ========
                    // 使用 LocationSearch 组件地图选点，显示位置信息面板
                    JourneyEditLocation(
                        journey = journey,
                        setJourney = { j ->
                            setJourney(j.copy())  // 通过 copy 触发重组
                        }
                    )
                    LineBetween()

                    // ======== 回忆编辑 ========
                    // Management

                    // 使用 Reminiscence 组件管理多张旅程回忆图片
                    JourneyEditImages(
                        journey = journey,
                        updateJourney = { j ->
                            // 触发 UI 更新：通过 copy 生成新列表对象，强制 Compose 重组
                            // 先清空再赋值的技巧确保 State 检测到变化
                            val newList =
                                List(j.journeyImagePaths.size, { i -> j.journeyImagePaths[i] })
                            setJourney(j.copy(journeyImagePaths = List(0, { i -> "" })))
                            setJourney(j.copy(journeyImagePaths = newList))
                        }
                    )
                    Spacer(Modifier.padding(10.dp))

                    // ======== 删除按钮（仅编辑模式） ========
                    // 只有编辑已有旅程时显示，新建模式下隐藏
                    if (journeySelected != null) {
                        Row {
                            Spacer(Modifier.weight(1f))  // 将按钮推到右侧
                            ButtonDelete(
                                title = "删除该旅程",
                                paddingValues = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                deleteJourney(journeySelected)  // 触发删除确认弹窗
                            }
                            Spacer(Modifier.width(10.dp))
                        }

                        Spacer(Modifier.padding(10.dp))
                    }
                }
            }
        }
        Spacer(Modifier.padding(10.dp))  // 底部留白，避免内容贴边
    }
}
