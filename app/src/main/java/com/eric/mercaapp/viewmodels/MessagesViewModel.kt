package com.eric.mercaapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eric.mercaapp.models.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessagesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _chatsList = MutableLiveData<List<Chat>>()
    val chatsList: LiveData<List<Chat>> get() = _chatsList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        listenForChatsChanges()
    }

    private fun listenForChatsChanges() {
        if (currentUserId.isEmpty()) {
            Log.e("MessagesViewModel", "Error: Usuario no autenticado")
            return
        }

        _loading.value = true

        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                _loading.value = false

                if (error != null) {
                    Log.e("MessagesViewModel", "Error al escuchar cambios en los chats: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val updatedChats = mutableListOf<Chat>()

                    for (doc in snapshots.documents) {
                        val chat = doc.toObject(Chat::class.java)?.apply { chatId = doc.id }

                        if (chat != null) {
                            val participantsArray = chat.participants.takeIf { it.isNotEmpty() } ?: listOf()

                            if (participantsArray.contains(currentUserId)) {
                                val otherUserId = participantsArray.firstOrNull { it != currentUserId } ?: ""

                                if (otherUserId.isNotEmpty()) {
                                    db.collection("usuarios").document(otherUserId)
                                        .get()
                                        .addOnSuccessListener { userDoc ->
                                            val otherUserName = userDoc.getString("name") ?: "Usuario desconocido"

                                            chat.participantId = otherUserId
                                            chat.participantName = otherUserName

                                            db.collection("chats").document(chat.chatId)
                                                .update("participantName", otherUserName)

                                            updatedChats.add(chat)

                                            _chatsList.value = updatedChats
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("MessagesViewModel", "Error obteniendo usuario: ${e.message}")
                                        }
                                }
                            }
                        }
                    }
                }
            }
    }
}
