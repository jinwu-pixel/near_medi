package com.nearmedi.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

    fun hasInternetConnection(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            try {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
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
        if (!isInKorea(lat, lon)) return null

        return try {
            @Suppress("DEPRECATION")
            val addresses = Geocoder(context, Locale.KOREA).getFromLocation(lat, lon, 1)
            val addr = addresses?.firstOrNull() ?: return null
            val sido = addr.adminArea?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            val sigungu = (addr.locality ?: addr.subAdminArea)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            AddressInfo(sido = sido, sigungu = sigungu)
        } catch (e: Exception) {
            null
        }
    }

    private fun isInKorea(lat: Double, lon: Double): Boolean {
        return lat in 33.0..43.0 && lon in 124.0..132.0
    }
}
