package com.nearmedi.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class XmlParserTest {

    @Test
    fun parseHospitals_validXml_returnsList() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
                <header>
                    <resultCode>00</resultCode>
                    <resultMsg>NORMAL SERVICE.</resultMsg>
                </header>
                <body>
                    <items>
                        <item>
                            <dutyName>서울내과의원</dutyName>
                            <dutyAddr>서울특별시 강남구 테헤란로 123</dutyAddr>
                            <dutyTel1>02-1234-5678</dutyTel1>
                            <wgs84Lat>37.5012</wgs84Lat>
                            <wgs84Lon>127.0396</wgs84Lon>
                            <dutyDivNam>의원</dutyDivNam>
                        </item>
                        <item>
                            <dutyName>강남약국</dutyName>
                            <dutyAddr>서울특별시 강남구 역삼로 456</dutyAddr>
                            <dutyTel1>02-9876-5432</dutyTel1>
                            <wgs84Lat>37.4985</wgs84Lat>
                            <wgs84Lon>127.0372</wgs84Lon>
                            <dutyDivNam>약국</dutyDivNam>
                        </item>
                    </items>
                </body>
            </response>
        """.trimIndent()

        val result = XmlParser.parseHospitals(xml)

        assertEquals(2, result.size)
        assertEquals("서울내과의원", result[0].name)
        assertEquals("서울특별시 강남구 테헤란로 123", result[0].address)
        assertEquals("02-1234-5678", result[0].tel)
        assertEquals(37.5012, result[0].lat, 0.0001)
        assertEquals(127.0396, result[0].lon, 0.0001)
        assertEquals("의원", result[0].type)
        assertEquals("강남약국", result[1].name)
    }

    @Test
    fun parseHospitals_emptyItems_returnsEmptyList() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
                <header><resultCode>00</resultCode></header>
                <body><items></items></body>
            </response>
        """.trimIndent()

        val result = XmlParser.parseHospitals(xml)
        assertEquals(0, result.size)
    }

    @Test
    fun parseHospitals_missingCoordinates_skipsItem() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
                <body>
                    <items>
                        <item>
                            <dutyName>좌표없는병원</dutyName>
                            <dutyAddr>주소</dutyAddr>
                        </item>
                    </items>
                </body>
            </response>
        """.trimIndent()

        val result = XmlParser.parseHospitals(xml)
        assertEquals(0, result.size)
    }

    @Test
    fun parseHospitals_apiError_throwsApiException() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
                <header>
                    <resultCode>12</resultCode>
                    <resultMsg>LIMITED NUMBER OF SERVICE REQUESTS EXCEEDS ERROR.</resultMsg>
                </header>
                <body/>
            </response>
        """.trimIndent()

        try {
            XmlParser.parseHospitals(xml)
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertTrue(e.message!!.contains("12"))
        }
    }

    @Test
    fun parseHospitals_invalidCoordinates_skipsItem() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
                <header><resultCode>00</resultCode></header>
                <body>
                    <items>
                        <item>
                            <dutyName>해외병원</dutyName>
                            <dutyAddr>해외주소</dutyAddr>
                            <wgs84Lat>0.0</wgs84Lat>
                            <wgs84Lon>0.0</wgs84Lon>
                        </item>
                        <item>
                            <dutyName>정상병원</dutyName>
                            <dutyAddr>서울</dutyAddr>
                            <wgs84Lat>37.5</wgs84Lat>
                            <wgs84Lon>127.0</wgs84Lon>
                        </item>
                    </items>
                </body>
            </response>
        """.trimIndent()

        val result = XmlParser.parseHospitals(xml)
        assertEquals(1, result.size)
        assertEquals("정상병원", result[0].name)
    }
}
