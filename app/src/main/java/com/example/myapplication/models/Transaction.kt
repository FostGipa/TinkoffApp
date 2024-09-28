package com.example.myapplication.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    @SerialName("id_транкзации")
    val id : Int? = null,
    @SerialName("id_вклад")
    val id_vklad : Int? = null,
    @SerialName("id_тип_транкзации")
    val id_transaction_type : Int,
    @SerialName("Сумма")
    val sum : Double,
    @SerialName("Дата")
    val date : String,
    @SerialName("id_Вид_Вклада")
    val name : Int
)
