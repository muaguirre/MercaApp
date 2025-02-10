package com.eric.mercaapp.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eric.mercaapp.R
import com.eric.mercaapp.adapters.ChatAdapter
import com.eric.mercaapp.models.Message
import com.eric.mercaapp.viewmodels.ChatViewModel
import com.eric.mercaapp.firebase.FirebaseNotificationSender

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()

    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var chatAdapter: ChatAdapter

    private val messages: MutableList<Message> = mutableListOf()
    private val receiverId: String by lazy { intent.getStringExtra("clientId").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        if (receiverId.isEmpty()) {
            showErrorAndExit("Error: Datos insuficientes para cargar el chat")
            return
        }

        recyclerViewChat = findViewById(R.id.recyclerViewChat)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)

        recyclerViewChat.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(messages, receiverId)
        recyclerViewChat.adapter = chatAdapter

        observeViewModel()
        viewModel.initializeChat(receiverId)

        buttonSend.setOnClickListener { sendMessage() }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { newMessages ->
            messages.clear()
            messages.addAll(newMessages)
            chatAdapter.notifyDataSetChanged()
            if (messages.isNotEmpty()) {
                recyclerViewChat.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun sendMessage() {
        val text = editTextMessage.text.toString().trim()
        if (text.isNotEmpty()) {
            viewModel.sendMessage(receiverId, text)
            FirebaseNotificationSender.sendNotificationToReceiver(this, receiverId, text)
            editTextMessage.text.clear()
        } else {
            Toast.makeText(this, "El mensaje no puede estar vacío", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showErrorAndExit(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
