package com.eric.mercaapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eric.mercaapp.models.Chat
import com.eric.mercaapp.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid.orEmpty()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _chatId = MutableLiveData<String>()
    val chatId: LiveData<String> get() = _chatId

    private var messagesListener: ListenerRegistration? = null

    fun initializeChat(receiverId: String) {
        if (currentUserId.isEmpty() || receiverId.isEmpty()) {
            Log.e("ChatViewModel", "Error: Datos insuficientes para cargar el chat")
            return
        }

        val newChatId = generateChatId(currentUserId, receiverId)

        if (_chatId.value != newChatId) {
            _chatId.value = newChatId
            listenForMessages(newChatId)
        }
    }

    private fun generateChatId(user1: String, user2: String): String =
        if (user1 < user2) "$user1-$user2" else "$user2-$user1"

    private fun listenForMessages(chatId: String) {
        messagesListener?.remove()

        messagesListener = db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("ChatViewModel", "Error al cargar mensajes: ${e.message}")
                    return@addSnapshotListener
                }

                _messages.value = snapshots?.documents?.mapNotNull { it.toObject(Message::class.java) } ?: emptyList()
            }
    }

    fun sendMessage(receiverId: String, text: String) {
        if (text.isEmpty()) return

        val chatId = _chatId.value ?: return
        val message = Message(text, currentUserId, receiverId, System.currentTimeMillis())

        val chatRef = db.collection("chats").document(chatId)

        chatRef.get().addOnSuccessListener { document ->
            val updates = mapOf(
                "lastMessage" to text,
                "timestamp" to System.currentTimeMillis()
            )

            if (!document.exists()) {
                val chat = Chat(
                    chatId = chatId,
                    participantId = receiverId,
                    participantName = "",
                    lastMessage = text,
                    timestamp = System.currentTimeMillis(),
                    participants = listOf(currentUserId, receiverId)
                )
                chatRef.set(chat)
            } else {
                chatRef.update(updates)
            }

            chatRef.collection("messages").add(message)
                .addOnFailureListener {
                    Log.e("ChatViewModel", "Error al enviar mensaje")
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
    }
}
