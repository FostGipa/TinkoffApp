package com.example.myapplication.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vklad(
    @SerialName("2")
    val srok2 : Double,
    @SerialName("12")
    val srok12 : Double,
    @SerialName("3-6")
    val srok3to6 : Double,
    @SerialName("7-11")
    val srok7to11 : Double,
    @SerialName("13-17")
    val srok13to17 : Double,
    @SerialName("18-24")
    val srok18to24 : Double
)
