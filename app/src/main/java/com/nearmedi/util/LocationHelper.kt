package com.nearmedi.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class AddressInfo(
    val sido: String,
    val sigungu: String,
)

class LocationHelper(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { cont ->
            try {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { location ->
                    cont.resume(location)
                }.addOnFailureListener {
                    cont.resume(null)
                }
            } catch (e: SecurityException) {
                cont.resume(null)
            }
        }
    }

    fun getAddressInfo(lat: Double, lon: Double): AddressInfo? {
        return try {
            @Suppress("DEPRECATION")
            val addresses = Geocoder(context, Locale.KOREA).getFromLocation(lat, lon, 1)
            val addr = addresses?.firstOrNull() ?: return null
            val sido = addr.adminArea ?: return null
            val sigungu = addr.locality ?: addr.subAdminArea ?: return null
            AddressInfo(sido = sido, sigungu = sigungu)
        } catch (e: Exception) {
            null
        }
    }
}
