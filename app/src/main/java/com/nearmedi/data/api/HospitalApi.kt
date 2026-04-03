package com.nearmedi.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface HospitalApi {

    @GET("getHsptlMdcncListInfoInqire")
    suspend fun getHospitals(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("Q0") sido: String,
        @Query("Q1") sigungu: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 50,
    ): String

    companion object {
        private const val BASE_URL = "https://apis.data.go.kr/B552657/HsptlAsembySearchService/"

        fun create(): HospitalApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(HospitalApi::class.java)
        }
    }
}
