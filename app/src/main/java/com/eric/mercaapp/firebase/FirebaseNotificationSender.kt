package com.eric.mercaapp.firebase

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FirebaseNotificationSender {

    fun sendNotificationToReceiver(context: Context, receiverId: String, body: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("usuarios").document(receiverId)

        userRef.get()
            .addOnSuccessListener { document ->
                val token = document.getString("fcmToken")
                if (!token.isNullOrEmpty()) {
                    sendNotificationToFCM(context, token, body)
                } else {
                    Log.e("FirebaseService", "El usuario no tiene un token FCM registrado.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al obtener el token del usuario: ${e.message}")
            }
    }

    @SuppressLint("DiscouragedApi")
    private fun getAccessToken(context: Context): String? {
        return try {
            val inputStream = context.resources.openRawResource(
                context.resources.getIdentifier("service_account", "raw", context.packageName)
            )
            val googleCredentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

            googleCredentials.refreshIfExpired()
            googleCredentials.accessToken.tokenValue
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error al obtener token de acceso: ${e.message}")
            null
        }
    }

    private fun sendNotificationToFCM(context: Context, token: String, body: String) {
        Thread {
            try {
                val accessToken = getAccessToken(context)
                if (accessToken.isNullOrEmpty()) {
                    Log.e("FirebaseService", "No se pudo obtener el token de acceso")
                    return@Thread
                }

                val url = URL("https://fcm.googleapis.com/v1/projects/mercaapp-716f3/messages:send")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.setRequestProperty("Content-Type", "application/json")

                val jsonMessage = JSONObject()
                val jsonData = JSONObject()

                jsonData.put("title", "MercaAPP")
                jsonData.put("body", body)

                jsonMessage.put("message", JSONObject().apply {
                    put("token", token)
                    put("data", jsonData)
                })

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonMessage.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("FirebaseService", "Notificación enviada correctamente")
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream)).use { errorStream ->
                        val response = errorStream.readText()
                        Log.e("FirebaseService", "Error en FCM: $response")
                    }
                }
            } catch (e: Exception) {
                Log.e("FirebaseService", "Error al enviar notificación: ${e.message}")
            }
        }.start()
    }
}
