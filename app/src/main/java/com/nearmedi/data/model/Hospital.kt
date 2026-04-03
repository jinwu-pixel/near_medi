package com.nearmedi.data.model

data class Hospital(
    val name: String,
    val address: String,
    val tel: String,
    val lat: Double,
    val lon: Double,
    val type: String,
    val distance: Float = 0f,
)
