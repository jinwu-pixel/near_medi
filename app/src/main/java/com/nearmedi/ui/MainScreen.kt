package com.nearmedi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onHospitalClick: (Int) -> Unit,
    onCallPhone: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    val myPosition = LatLng(state.myLat, state.myLon)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(myPosition, 14f)
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Move camera when location updates
    LaunchedEffect(state.myLat, state.myLon) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(myPosition, 14f)
        )
    }

    // Move camera when a hospital is selected from the list
    LaunchedEffect(state.selectedHospitalIndex) {
        val index = state.selectedHospitalIndex
        if (index in state.hospitals.indices) {
            val hospital = state.hospitals[index]
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(hospital.lat, hospital.lon), 16f
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("니어메디") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
        floatingActionButton = {
            if (!state.permissionDenied && !state.isLoading) {
                FloatingActionButton(
                    onClick = onRefresh,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                }
            }
        },
    ) { paddingValues ->
        if (state.permissionDenied) {
            PermissionDeniedContent(
                modifier = Modifier.padding(paddingValues),
                onOpenSettings = onOpenSettings,
                onRequestPermission = onRequestPermission,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                // Map area (top 50%)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = state.hasLocationPermission),
                        uiSettings = MapUiSettings(myLocationButtonEnabled = state.hasLocationPermission),
                    ) {
                        state.hospitals.forEachIndexed { index, hospital ->
                            val isPharmacy = hospital.type.contains("약국")
                            val isSelected = index == state.selectedHospitalIndex
                            Marker(
                                state = MarkerState(position = LatLng(hospital.lat, hospital.lon)),
                                title = hospital.name,
                                snippet = "${hospital.type} · ${formatDistance(hospital.distance)}",
                                icon = BitmapDescriptorFactory.defaultMarker(
                                    if (isPharmacy) BitmapDescriptorFactory.HUE_GREEN
                                    else BitmapDescriptorFactory.HUE_RED
                                ),
                                alpha = if (isSelected) 1.0f else 0.7f,
                                onClick = {
                                    onHospitalClick(index)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                    false // show info window
                                },
                            )
                        }
                    }

                    // Loading overlay on map
                    if (state.isLoading) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "주변 병원/약국 검색 중...",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }

                // List area (bottom 50%)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    when {
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
                        !state.isLoading && state.hospitals.isEmpty() -> {
                            Text(
                                text = "주변에 병원/약국이 없습니다",
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                        else -> {
                            Column {
                                // Result count header
                                if (state.hospitals.isNotEmpty()) {
                                    Text(
                                        text = "검색결과 ${state.hospitals.size}건",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            start = 16.dp, top = 8.dp, bottom = 4.dp,
                                        ),
                                    )
                                }
                                LazyColumn(state = listState) {
                                    itemsIndexed(state.hospitals) { index, hospital ->
                                        HospitalListItem(
                                            hospital = hospital,
                                            isSelected = index == state.selectedHospitalIndex,
                                            onClick = { onHospitalClick(index) },
                                            onCallPhone = onCallPhone,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "위치 권한이 필요합니다",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "주변 병원과 약국을 찾으려면\n위치 접근 권한을 허용해 주세요.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("권한 허용하기")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenSettings) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("설정에서 허용하기")
        }
    }
}

private fun formatDistance(meters: Float): String {
    return if (meters < 1000) {
        "${meters.toInt()}m"
    } else {
        String.format("%.1fkm", meters / 1000)
    }
}
