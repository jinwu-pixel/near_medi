package com.nearmedi.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nearmedi.data.model.Hospital
import com.nearmedi.data.repository.HospitalRepository
import com.nearmedi.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val hospitals: List<Hospital> = emptyList(),
    val myLat: Double = 37.5665,
    val myLon: Double = 126.9780,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasLocationPermission: Boolean = false,
    val permissionDenied: Boolean = false,
    val selectedHospitalIndex: Int = -1,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val locationHelper = LocationHelper(application)
    private val repository = HospitalRepository()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(hasLocationPermission = true, permissionDenied = false)
    }

    fun onPermissionDenied() {
        _uiState.value = _uiState.value.copy(permissionDenied = true, hasLocationPermission = false)
    }

    fun selectHospital(index: Int) {
        _uiState.value = _uiState.value.copy(selectedHospitalIndex = index)
    }

    fun loadNearbyHospitals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, hasLocationPermission = true)

            if (!locationHelper.hasInternetConnection()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "인터넷 연결을 확인해주세요"
                )
                return@launch
            }

            val location = locationHelper.getCurrentLocation()
            if (location == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "위치를 확인할 수 없습니다"
                )
                return@launch
            }

            val lat = location.latitude
            val lon = location.longitude
            _uiState.value = _uiState.value.copy(myLat = lat, myLon = lon)

            val addressInfo = locationHelper.getAddressInfo(lat, lon)
            if (addressInfo == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "현재 위치가 한국 내가 아니거나\n주소를 확인할 수 없습니다"
                )
                return@launch
            }

            try {
                val hospitals = repository.searchNearby(
                    sido = addressInfo.sido,
                    sigungu = addressInfo.sigungu,
                    myLat = lat,
                    myLon = lon,
                )
                _uiState.value = _uiState.value.copy(
                    hospitals = hospitals,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "데이터를 불러올 수 없습니다"
                )
            }
        }
    }

    fun hasLocationPermission(): Boolean = locationHelper.hasLocationPermission()
}
