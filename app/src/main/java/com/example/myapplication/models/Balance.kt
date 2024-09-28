package com.example.myapplication.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Balance(
    val id : Int? = null,
    val user_uuid : String,
    @SerialName("Баланс")
    val balance : Double
)
