package com.turnify.app.Utils

object Constants {

    val BASE_URL: String = "https://5e77-181-51-32-120.ngrok-free.app/api"

    object ENDPOINTS {
        val LOGIN: String = "/authentication/login"
        val REGISTER: String = "/authentication/register"
        val REFRESH: String = "/authentication/refresh"
    }

    object METHODS {
        val POST: String = "POST"
        val GET: String = "GET"
        val PUT: String = "PUT"
        val DELETE: String = "DELETE"
    }

}