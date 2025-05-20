package com.turnify.app.dataObjects

data class RegisterRequest(
    val email: String,
    val name: String,
    val lastName: String,
    val password: String,
    val phone: String,
    val type: Int,
    val phonetoken: String
)