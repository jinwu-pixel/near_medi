package com.nearmedi.data.parser

import com.nearmedi.data.model.Hospital
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class ApiException(message: String) : Exception(message)

object XmlParser {

    fun parseHospitals(xml: String): List<Hospital> {
        val hospitals = mutableListOf<Hospital>()
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(xml.reader())

        var inItem = false
        var currentTag = ""
        var name = ""
        var address = ""
        var tel = ""
        var lat = 0.0
        var lon = 0.0
        var type = ""
        var resultCode: String? = null
        var resultMsg: String? = null

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    if (currentTag == "item") {
                        inItem = true
                        name = ""; address = ""; tel = ""
                        lat = 0.0; lon = 0.0; type = ""
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim() ?: ""
                    if (!inItem) {
                        when (currentTag) {
                            "resultCode" -> resultCode = text
                            "resultMsg" -> resultMsg = text
                        }
                    } else {
                        when (currentTag) {
                            "dutyName" -> name = text
                            "dutyAddr" -> address = text
                            "dutyTel1" -> tel = text
                            "wgs84Lat" -> lat = text.toDoubleOrNull() ?: 0.0
                            "wgs84Lon" -> lon = text.toDoubleOrNull() ?: 0.0
                            "dutyDivNam" -> type = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "item" && inItem) {
                        if (name.isNotEmpty() && isValidCoordinate(lat, lon)) {
                            hospitals.add(Hospital(name, address, tel, lat, lon, type))
                        }
                        inItem = false
                    }
                    currentTag = ""
                }
            }
            parser.next()
        }

        if (resultCode != null && resultCode != "00") {
            throw ApiException("API 오류: ${resultMsg ?: "알 수 없는 오류"} (코드: $resultCode)")
        }

        return hospitals
    }

    private fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in 33.0..43.0 && lon in 124.0..132.0
    }
}
