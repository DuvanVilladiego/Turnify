package com.turnify.app.Utils

object Constants {

    val BASE_URL_AUTH: String = "https://aa34-181-51-32-120.ngrok-free.app/api"
    val BASE_URL_SHIFTS: String = "https://ba99-181-51-32-120.ngrok-free.app/api"

    object ENDPOINTS {
        val LOGIN: String = "/authentication/login"
        val REGISTER: String = "/authentication/register"
        val REFRESH: String = "/authentication/refresh"
        val GET_ALL: String = "/shift"
        val GET_SHIFT: String = "/shift/"
        val SUBSCRIBE: String = "/shift/subscribe/"
        val COMPLETE: String = "/shift/confirm/"
    }

    object METHODS {
        val POST: String = "POST"
        val GET: String = "GET"
        val PUT: String = "PUT"
        val DELETE: String = "DELETE"
    }

}