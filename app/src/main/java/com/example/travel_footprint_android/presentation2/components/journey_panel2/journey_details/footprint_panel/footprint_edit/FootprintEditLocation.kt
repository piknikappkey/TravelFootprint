package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_edit

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.presentation2.components.journey_map3.location_search.LocationSearch
import com.example.travel_footprint_android.presentation2.components.journey_map3.location_search.LocationSearchViewModel
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.location.LocationPanel
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel
import com.example.travel_footprint_android.ui.theme.FontDark4

@Composable
fun FootprintEditLocation(
    footprint: Footprint,
    setFootprint: (Footprint) -> Unit,
    locationSearchViewModel: LocationSearchViewModel = hiltViewModel(),
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(),
) {
    var isSelectedLocation by remember { mutableStateOf(footprint.address != "") }

    var name by remember { mutableStateOf(if(footprint.address != "") footprint.address.split("\n")[0] else "") }

    var address by remember { mutableStateOf(if(footprint.address != "") footprint.address.split("\n").last() else "") }

//    var latitude by remember { mutableStateOf(if(footprint.address != "") footprint.latitude else 0.0) }
    var latitude by remember { mutableStateOf(if(footprint.address != "") 0.0 else 0.0) }

//    var longitude by remember { mutableStateOf(if(footprint.address != "") footprint.longitude else 0.0) }
    var longitude by remember { mutableStateOf(if(footprint.address != "") 0.0 else 0.0) }


    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        locationSearchViewModel.setOnLocationSelectedCallback { latLng ->
            journeyMap3ViewModel.setSelectedLocation(latLng)
        }
    }

    TextMedium(
        text = "地址：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp),
        color = FontDark4,
    )

    Spacer(Modifier.padding(2.dp))
    LocationSearch(
        locationSearchViewModel = locationSearchViewModel,
        onLocationSelected = { location ->
            Log.d("FootprintEditLocation", "location = $location")
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
                Log.d("FootprintEditLocation", "submit")
                Log.d("FootprintEditLocation", "showButton = $showButton")
                setFootprint(
                    footprint.copy()
                    /////////////////////////////////////////////////////////////
                )
                showButton = false
                Log.d("FootprintEditLocation", "showButton = $showButton")
            },
            cancel = {
                Log.d("FootprintEditLocation", "cancel")
                Log.d("FootprintEditLocation", "journey.address = ${footprint.address}")
                if (footprint.address != "") {
                    Log.d("FootprintEditLocation", "footprint.address != \"\"")
                    name = footprint.address.split("\n")[0]
                    address = footprint.address.split("\n").last()
//                    latitude = footprint.latitude
//                    longitude = footprint.longitude
                    isSelectedLocation = true
                    Log.d("FootprintEditLocation", "name = $name")
                    Log.d("FootprintEditLocation", "address = $address")
                    Log.d("FootprintEditLocation", "latitude = $latitude")
                    Log.d("FootprintEditLocation", "longitude = $longitude")
                } else {
                    Log.d("FootprintEditLocation", "footprint.address == \"\"")
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
                setFootprint(
                    footprint.copy()
                )
            }
        )
    }
}

//@Composable
//fun LocationPanel(
//    name: String,
//    address: String,
//    latitude: Double,
//    longitude: Double,
//    showButton: Boolean,
//    submit: () -> Unit,
//    cancel: () -> Unit,
//    delete: () -> Unit,
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 16.dp)
//            .background(BGLight2, RoundedCornerShape(12.dp))
//            .padding(16.dp)
//    ) {
//        Row {
//            Text(
//                text = "已选择位置",
//                color = FontDark6,
//                style = TextStyle(fontSize = 14.sp),
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//            Spacer(Modifier.weight(1f))
//            Image(
//                modifier = Modifier
//                    .size(24.dp)
//                    .clickable(onClick = delete),
//                painter = painterResource(R.drawable.ic_delete_trash),
//                contentDescription = "修改图标",
//                colorFilter = ColorFilter.tint(Color(0xFFFF0000)),
//            )
//            Spacer(Modifier.width(10.dp))
//        }
//
//        // 位置名称
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.LocationOn,
//                contentDescription = "位置图标",
//                tint = MainColor3,
//                modifier = Modifier.size(20.dp)
//            )
//            Text(
//                text = name,
//                color = FontDark2,
//                style = TextStyle(fontSize = 16.sp),
//                modifier = Modifier.padding(start = 28.dp)
//            )
//        }
//
//        // 详细地址
//        if (address.isNotEmpty()) {
//            Text(
//                text = address,
//                color = FontDark6,
//                style = TextStyle(fontSize = 14.sp),
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//        }
//
//        // 经纬度信息
//        Text(
//            text = "纬度: ${String.format("%.6f", latitude)}",
//            color = FontDark6,
//            style = TextStyle(fontSize = 14.sp),
//            modifier = Modifier.padding(bottom = 4.dp)
//        )
//        Text(
//            text = "经度: ${String.format("%.6f", longitude)}",
//            color = FontDark6,
//            style = TextStyle(fontSize = 14.sp),
//            modifier = Modifier.padding(bottom = 12.dp)
//        )
//        if(showButton) {
//            Row {
//                Spacer(
//                    modifier = Modifier
//                        .weight(1f),
//                )
//                ButtonMain(
//                    title = "取消"
//                ) {
//                    cancel()
//                }
//                Spacer(
//                    modifier = Modifier
//                        .width(10.dp),
//                )
//                ButtonMain(
//                    title = "确定"
//                ) {
//                    submit()
//                }
//            }
//        }
//    }
//}