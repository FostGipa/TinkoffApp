package com.example.myapplication.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDeposits(
    @SerialName("id")
    val id : Int? = null,
    @SerialName("user_uuid")
    val user_uuid : String,
    @SerialName("id_Вид_Вклада")
    val id_Deposit_Type : Int,
    @SerialName("Баланс")
    val balance : Double,
    @SerialName("Начальная_Сумма")
    val start_sum : Double,
    @SerialName("Срок")
    val srok : Int? = null,
    @SerialName("Дата_Открытия")
    val open_date : String,
    @SerialName("Скрыт")
    val hide : Boolean? = null,
    @SerialName("Ставка")
    val stavka : Double
)
