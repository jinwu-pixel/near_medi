package com.nearmedi.data.parser

import com.nearmedi.data.model.Hospital
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

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
                    if (inItem) {
                        val text = parser.text?.trim() ?: ""
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
                        if (name.isNotEmpty() && lat != 0.0 && lon != 0.0) {
                            hospitals.add(Hospital(name, address, tel, lat, lon, type))
                        }
                        inItem = false
                    }
                    currentTag = ""
                }
            }
            parser.next()
        }
        return hospitals
    }
}
