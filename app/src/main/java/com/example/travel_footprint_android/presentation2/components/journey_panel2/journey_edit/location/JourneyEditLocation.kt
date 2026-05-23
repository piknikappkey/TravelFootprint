package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.location

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation2.components.journey_map3.location_search.LocationSearch
import com.example.travel_footprint_android.presentation2.components.journey_map3.location_search.LocationSearchViewModel
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.components.text.text_small.TextSmall
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel
import com.example.travel_footprint_android.ui.theme.FontDark6
import com.example.travel_footprint_android.ui.theme.FontDark8
import com.example.travel_footprint_android.ui.theme.MainColor3

@Composable
fun JourneyEditLocation(
    journey: Journey,
    setJourney: (Journey) -> Unit,
    locationSearchViewModel: LocationSearchViewModel = hiltViewModel(),
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(),
) {
    var isSelectedLocation by remember { mutableStateOf(journey.address != "") }

    var name by remember { mutableStateOf(if(journey.address != "") journey.address.split("\n")[0] else "") }

    var address by remember { mutableStateOf(if(journey.address != "") journey.address.split("\n").last() else "") }

    var latitude by remember { mutableStateOf(if(journey.address != "") journey.latitude else 0.0) }

    var longitude by remember { mutableStateOf(if(journey.address != "") journey.longitude else 0.0) }

    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        locationSearchViewModel.setOnLocationSelectedCallback { latLng ->
            journeyMap3ViewModel.setSelectedLocation(latLng)
        }
    }

    TextMedium(
        text = "旅程地址：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )

    Spacer(Modifier.padding(2.dp))
    LocationSearch(
        locationSearchViewModel = locationSearchViewModel,
        onLocationSelected = { location ->
            Log.d("JourneyEditLocation", "location = $location")
//            if (location.latitude != latitude || location.longitude != longitude) {
            isSelectedLocation = true
            showButton = true
            name = location.name
            address = location.address
            latitude = location.latitude
            longitude = location.longitude
//            }
        }
    )

    Spacer(Modifier.padding(2.dp))
    if(isSelectedLocation) {
        LocationPanel(
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            showButton = showButton,
            submit = {
                Log.d("JourneyEditLocation", "submit")
                Log.d("JourneyEditLocation", "showButton = $showButton")
                setJourney(
                    journey.copy(
                        address = address + "\n" + name,
                        longitude = longitude,
                        latitude = latitude
                    )
                )
                showButton = false
                Log.d("JourneyEditLocation", "showButton = $showButton")
            },
            cancel = {
                Log.d("JourneyEditLocation", "cancel")
                Log.d("JourneyEditLocation", "journey.address = ${journey.address}")
                if (journey.address != "") {
                    Log.d("JourneyEditLocation", "journey.address != \"\"")
                    name = journey.address.split("\n")[0]
                    address = journey.address.split("\n").last()
                    latitude = journey.latitude
                    longitude = journey.longitude
                    isSelectedLocation = true
                    Log.d("JourneyEditLocation", "name = $name")
                    Log.d("JourneyEditLocation", "address = $address")
                    Log.d("JourneyEditLocation", "latitude = $latitude")
                    Log.d("JourneyEditLocation", "longitude = $longitude")
                } else {
                    Log.d("JourneyEditLocation", "journey.address == \"\"")
                    name = ""
                    address = ""
                    latitude = 0.0
                    longitude = 0.0
                    isSelectedLocation = false
                }
                showButton = false
            },
            delete = {
                showButton = false
                isSelectedLocation = false
                name = ""
                address = ""
                latitude = 0.0
                longitude = 0.0
                setJourney(
                    journey.copy(
                        address = "",
                        longitude = 0.0,
                        latitude = 0.0
                    )
                )
            }
        )
    }
}

@Composable
fun LocationPanel(
    name: String,
    address: String,
    latitude: Double,
    longitude: Double,
    showButton: Boolean,
    submit: () -> Unit,
    cancel: () -> Unit,
    delete: () -> Unit,
) {
    BGBox (
        modifier = Modifier
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        BGImgBox(listOf(R.drawable.bg_rectangular_2__1__0, R.drawable.bg_rectangular_2__1__1, R.drawable.bg_rectangular_2__1__2, R.drawable.bg_rectangular_2__1__3)) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, 8.dp)
            ) {
                Row {
                    TextMedium(
                        text = "已选择位置",
                        color = FontDark6,
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Image(
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(onClick = delete),
                        painter = painterResource(R.drawable.ic_delete_trash),
                        contentDescription = "修改图标",
                        colorFilter = ColorFilter.tint(Color(0xFFFF0000)),
                    )
                    Spacer(Modifier.width(10.dp))
                }

                // 位置名称
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "位置图标",
                        tint = MainColor3,
                        modifier = Modifier.size(20.dp)
                    )
                    Headline(
                        text = name,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 28.dp)
                    )
//                    TextMedium(
//                        text = name,
//                        modifier = Modifier.padding(start = 28.dp)
//                    )
                }

                // 详细地址
                if (address.isNotEmpty()) {
                    Row {
                        Spacer(Modifier.weight(1f))
                        TextMedium(
                            text = address,
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 经纬度信息
                Row {
                    Spacer(Modifier.weight(1f))
                    TextSmall(
                        text = "${String.format("%.4f", latitude)} - ${String.format("%.4f", longitude)}",
                        color = FontDark8,
                        fontSize = 12.sp

                    )
                }

                Spacer(Modifier.height(8.dp))

                if(showButton) {
                    Row {
                        Spacer(
                            modifier = Modifier
                                .weight(1f),
                        )
                        ButtonMain(
                            onClick = cancel
                        ) {
                            TextMedium("取消")
                        }
                        Spacer(
                            modifier = Modifier
                                .width(10.dp),
                        )
                        ButtonMain(
                            onClick = submit
                        ) {
                            TextMedium("确定")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}