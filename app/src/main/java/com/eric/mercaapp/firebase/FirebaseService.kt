package com.eric.mercaapp.firebase

import android.util.Log
import com.eric.mercaapp.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FirebaseService", "Nuevo token FCM: $token")
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        try {
            remoteMessage.notification?.let {
                val title = it.title ?: "Nuevo mensaje"
                val body = it.body ?: ""
                Log.d("FirebaseService", "Notificación recibida: $title - $body")
                NotificationHelper.showNotification(this, title, body)
            }

            if (remoteMessage.data.isNotEmpty()) {
                val title = remoteMessage.data["title"] ?: "Nuevo mensaje"
                val body = remoteMessage.data["body"] ?: "Has recibido un mensaje"
                Log.d("FirebaseService", "Datos recibidos: ${remoteMessage.data}")
                NotificationHelper.showNotification(this, title, body)
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error procesando el mensaje: ${e.message}", e)
        }
    }

    private fun saveTokenToFirestore(newToken: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("FirebaseService", "Usuario no autenticado, no se puede guardar el token.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("usuarios").document(user.uid)

        userRef.update("fcmToken", newToken)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Token guardado en Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al guardar el token: ${e.message}", e)
            }
    }
}
