package com.nearmedi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.nearmedi.data.model.Hospital

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(state: MainUiState, onRetry: () -> Unit) {
    val myPosition = LatLng(state.myLat, state.myLon)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(myPosition, 14f)
    }

    LaunchedEffect(state.myLat, state.myLon) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(myPosition, 14f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("니어메디") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Map area (top 50%)
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(myLocationButtonEnabled = true),
            ) {
                state.hospitals.forEach { hospital ->
                    Marker(
                        state = MarkerState(position = LatLng(hospital.lat, hospital.lon)),
                        title = hospital.name,
                        snippet = hospital.address,
                    )
                }
            }

            // List area (bottom 50%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(state.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onRetry) {
                                Text("재시도")
                            }
                        }
                    }
                    state.hospitals.isEmpty() -> {
                        Text(
                            text = "주변에 병원/약국이 없습니다",
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    else -> {
                        LazyColumn {
                            items(state.hospitals) { hospital ->
                                HospitalListItem(hospital = hospital)
                            }
                        }
                    }
                }
            }
        }
    }
}
