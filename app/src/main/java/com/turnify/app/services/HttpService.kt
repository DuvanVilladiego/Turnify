package com.turnify.app.services

import com.google.gson.Gson
import com.turnify.app.Utils.Constants
import com.turnify.app.Utils.DatabaseManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object HttpService {

    val gson = Gson()
    var client = OkHttpClient()

    fun fetch(
        _url: String,
        _path: String,
        _body: Any? = null,
        _method: String = "GET",
        _headers: Map<String, String>? = null
    ): String? {
        val fullUrl = "$_url$_path"
        val requestBody = _body?.let { toRequestBodyFromAny(it) }

        fun buildRequest(headers: Map<String, String>?): Request {
            val builder = Request.Builder()
                .url(fullUrl)
                .method(_method, if (_method == "GET" || _body == null) null else requestBody)

            headers?.forEach { (key, value) ->
                builder.addHeader(key, value)
            }

            return builder.build()
        }

        val request = buildRequest(_headers)

        try {
            client.newCall(request).execute().use { response ->
                if (response.code == 401) {
                    // Intentamos refrescar el token
                    if (refreshToken()) {
                        // Intentamos de nuevo con nuevo token
                        val newHeaders = _headers?.toMutableMap() ?: mutableMapOf()
                        newHeaders["Authorization"] = "Bearer ${DatabaseManager.get().obtenerToken()}"

                        val newRequest = buildRequest(newHeaders)
                        client.newCall(newRequest).execute().use { retryResponse ->
                            return retryResponse.body?.string()
                        }
                    } else {
                        return null // Refresh falló
                    }
                }

                return response.body?.string()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun toRequestBodyFromAny(obj: Any): RequestBody {
        val json = gson.toJson(obj)
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        return json.toRequestBody(mediaType)
    }

    private fun refreshToken(): Boolean {
        val refreshToken = DatabaseManager.get().obtenerRefreshToken()

        val url = Constants.BASE_URL_AUTH + Constants.ENDPOINTS.REFRESH
        val headers = mapOf("Content-Type" to "application/json")
        val body = mapOf("refresh_token" to refreshToken)

        val requestBody = toRequestBodyFromAny(body)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val jsonResponse = gson.fromJson(response.body?.string(), Map::class.java)
                    val data = jsonResponse["data"] as? Map<*, *> ?: return false
                    val newToken = data["token"] as? String
                    val newRefreshToken = data["refreshToken"] as? String

                    if (!newToken.isNullOrBlank() && !newRefreshToken.isNullOrBlank()) {
                        DatabaseManager.get().insertarTokens(newToken, newRefreshToken)
                        return true
                    }
                }
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


}
