package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_edit

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation.components.button.button_delete.ButtonDelete
import com.example.travel_footprint_android.presentation.components.button.button_save.ButtonSave
import com.example.travel_footprint_android.presentation.components.custom_scrollbar.VerticalCustomScrollbar
import com.example.travel_footprint_android.presentation.components.dialog.ConfirmDeleteDialog
import com.example.travel_footprint_android.presentation.components.journey_map.viewmodel.JourneyMapViewModel
import com.example.travel_footprint_android.presentation.components.journey_panel.ic_journey_height_button.IcJourneyHeightButton
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation.components.line_between.LineBetween
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.FontDark8
import com.example.travel_footprint_android.ui.theme.SecondColor3
import java.util.Date

@Composable
fun FootprintEdit(
    footprintSelected: Footprint?,
    journeySelected: Journey,
    addFootprint: (Journey, Footprint) -> Unit,
    updateFootprint: (Footprint) -> Unit,
    deleteFootprint: (Footprint) -> Unit,
    journeyPanelExpandedState: Boolean,
    setJourneyPanelOffset: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    val journeyMapViewModel: JourneyMapViewModel = hiltViewModel(key = "JourneyMap3")
    val currentLatLng by journeyMapViewModel.currentLocation.collectAsState()

    var footprint by remember {
        mutableStateOf(
            footprintSelected?.copy()
                ?: Footprint(
                    journeyId = journeySelected.id,
                    title = "",
                    description = "",
                    createTime = Date(),
                    address = journeySelected.address,
                    longitude = currentLatLng?.longitude ?: 0.0,
                    latitude = currentLatLng?.latitude ?: 0.0,
                    rating = 1,
                    startTime = Date(0L),
                )
        )
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentLatLng) {
        if (footprintSelected == null && currentLatLng != null) {
            footprint = footprint.copy(
                longitude = currentLatLng!!.longitude,
                latitude = currentLatLng!!.latitude
            )
        }
    }

    LaunchedEffect(Unit) {
        journeyMapViewModel.startLocation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(Modifier.height(10.dp))
        // 顶部导航栏
        HeadRow(
            footprintSelected,
            footprint,
            journeySelected,
            addFootprint,
            updateFootprint,
            journeyPanelExpandedState,
            setJourneyPanelOffset,
            setIsDragging = setIsDragging,
            onDragDelta = onDragDelta,
            onPanelNavigate = onPanelNavigate,
        )
        Spacer(Modifier.height(10.dp))

        Content(
            modifier = Modifier.weight(1f),
            footprint,
            footprintSelected,
            journeySelected,
            { f -> footprint = f.copy() },
            { showDeleteDialog = true },
            currentLatLng?.latitude,
            currentLatLng?.longitude
        )

        if (showDeleteDialog && footprintSelected != null) {
            ConfirmDeleteDialog(
                title = "删除足迹",
                message = "确定要删除「${footprintSelected.title}」吗？此操作不可撤销。",
                onConfirm = {
                    deleteFootprint(footprintSelected)
                    onPanelNavigate(JourneyPanel2State.FOOTPRINT_LIST, journeySelected, null)
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
private fun HeadRow(
    footprintSelected: Footprint?,
    footprint: Footprint,
    journeySelected: Journey,
    addFootprint: (Journey, Footprint) -> Unit,
    updateFootprint: (Footprint) -> Unit,
    journeyPanelExpandedState: Boolean,
    setJourneyPanelOffset: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        Image(
            modifier = Modifier
                .size(26.dp)
                .padding(start = 5.dp)
                .clickable(onClick = {
                    onPanelNavigate(JourneyPanel2State.FOOTPRINT_LIST, journeySelected, null)
                }),
            painter = painterResource(id = R.drawable.ic_left2),
            contentDescription = "返回图标",
            colorFilter = ColorFilter.tint(SecondColor3),
        )
        Spacer(Modifier.width(5.dp))
        Headline(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { setIsDragging(true) },
                        onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },
                        onDragEnd = { setIsDragging(false) }
                    )
                },
            text = if(footprintSelected == null) "新增足迹" else "编辑足迹",
            fontSize = 18.sp
        )
        // 保存按钮
        ButtonSave(
            onClick = {
                val titleValid = footprint.title.isNotBlank()
                val descValid = footprint.description.isNotBlank() && footprint.description != "这是一个新的足迹"

                when {
                    !titleValid -> Toast.makeText(context, "请输入有效的足迹标题", Toast.LENGTH_SHORT).show()
                    !descValid -> Toast.makeText(context, "请输入有效的足迹描述", Toast.LENGTH_SHORT).show()
                    else -> {
                        if (footprintSelected == null) {
                            addFootprint(journeySelected, footprint.copy())
                        } else {
                            updateFootprint(footprint.copy())
                        }
                        onPanelNavigate(JourneyPanel2State.FOOTPRINT_LIST, journeySelected, null)
                    }
                }
            }
        )
        Spacer(Modifier.width(10.dp))

        IcJourneyHeightButton(journeyPanelExpandedState, { setJourneyPanelOffset(!journeyPanelExpandedState) })
        Spacer(Modifier.width(10.dp))
    }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    footprint: Footprint,
    footprintSelected: Footprint?,
    journeySelected: Journey,
    setFootprint: (Footprint) -> Unit,
    deleteFootprint: (Footprint) -> Unit,
    latitude: Double?,
    longitude: Double?,
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            BGImgBox(
                R.drawable.bg_rectangular_1__2__1,  // 背景图2
                R.drawable.bg_rectangular_1__2__2,  // 背景图3
                modifier = Modifier
                    .padding(10.dp, 10.dp)
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Column {
                    // 编辑内容区域
                    Spacer(Modifier.padding(3.dp))

                    // 足迹标题编辑
                    FootprintEditCover(footprint, { text -> setFootprint(footprint.copy(title = text)) })

                    LineBetween()

                    // 足迹描述编辑
                    FootprintEditDescription(footprint, { text -> setFootprint(footprint.copy(description = text)) })

                    LineBetween()

                    // 足迹地址编辑
                    FootprintEditLocation(
                        footprint,
                        setFootprint = { f -> setFootprint(f.copy())},
                    )

                    // 当前经纬度
                    if(latitude != null && longitude != null) {
                        Row {
                            Spacer(Modifier.weight(1f))
                            TextSmall(
                                text = "${String.format("%.4f", latitude)} - ${String.format("%.4f", longitude)}",
                                color = FontDark8,
                                fontSize = 12.sp

                            )
                            Spacer(Modifier.width(10.dp))
                        }
                    }

                    LineBetween()

                    // 个人评分编辑
                    FootprintEditRating(footprint, { rating -> setFootprint(footprint.copy(rating = rating)) })

                    if(footprintSelected != null) {
                        LineBetween()
                        Spacer(Modifier.padding(10.dp))
                        Row {
                            Spacer(Modifier.weight(1f))
                            ButtonDelete(
                                title = "删除该足迹",
                                paddingValues = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                deleteFootprint(footprintSelected)
                            }
                            Spacer(Modifier.width(10.dp))
                        }
                    }
                    Spacer(Modifier.padding(10.dp))
                }
            }
        }

        VerticalCustomScrollbar(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd),
        )
    }
}