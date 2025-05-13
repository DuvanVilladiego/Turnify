package com.turnify.app.services

import com.google.gson.Gson
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
    ) : String? {

        val fullUrl = "$_url$_path"

        // Crear el cuerpo de la solicitud si se provee
        val requestBody = _body?.let { toRequestBodyFromAny(it) }

        val builder = Request.Builder()
            .url(fullUrl)
            .method(_method, if (_method == "GET" || _body == null) null else requestBody)

        // Agregar cabeceras si se proveen
        _headers?.forEach { (key, value) ->
            builder.addHeader(key, value)
        }

        val request = builder.build()

        // Ejecutamos la petición en un hilo aparte (recomendado en apps Android)
        return try {
            client.newCall(request).execute().use { response ->
                response.body?.string()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun toRequestBodyFromAny(obj: Any): RequestBody {
        val json = gson.toJson(obj)
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        return json.toRequestBody(mediaType)
    }
}
