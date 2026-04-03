package com.nearmedi.data.repository

import android.location.Location
import com.nearmedi.BuildConfig
import com.nearmedi.data.api.HospitalApi
import com.nearmedi.data.model.Hospital
import com.nearmedi.data.parser.XmlParser

class HospitalRepository(
    private val api: HospitalApi = HospitalApi.create(),
) {
    suspend fun searchNearby(sido: String, sigungu: String, myLat: Double, myLon: Double): List<Hospital> {
        val xml = api.getHospitals(
            serviceKey = BuildConfig.DATA_GO_KR_API_KEY,
            sido = sido,
            sigungu = sigungu,
        )
        val hospitals = XmlParser.parseHospitals(xml)

        return hospitals.map { hospital ->
            val distance = FloatArray(1)
            Location.distanceBetween(myLat, myLon, hospital.lat, hospital.lon, distance)
            hospital.copy(distance = distance[0])
        }.sortedBy { it.distance }
    }
}
