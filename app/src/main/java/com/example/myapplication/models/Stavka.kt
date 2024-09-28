package com.example.myapplication.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stavka(
    @SerialName("stavka")
    val stavka : Double
)
