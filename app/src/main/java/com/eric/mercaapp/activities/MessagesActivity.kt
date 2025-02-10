package com.eric.mercaapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eric.mercaapp.R
import com.eric.mercaapp.adapters.ChatsAdapter
import com.eric.mercaapp.models.Chat
import com.eric.mercaapp.viewmodels.MessagesViewModel

class MessagesActivity : AppCompatActivity() {

    private val viewModel: MessagesViewModel by viewModels()

    private lateinit var recyclerViewChats: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var chatsAdapter: ChatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerViewChats = findViewById(R.id.recyclerViewChats)
        progressBar = findViewById(R.id.progressBar)

        recyclerViewChats.layoutManager = LinearLayoutManager(this)
        chatsAdapter = ChatsAdapter(mutableListOf()) { chat -> openChat(chat) }
        recyclerViewChats.adapter = chatsAdapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.chatsList.observe(this) { chats ->
            chatsAdapter.updateChats(chats.toMutableList())
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun openChat(chat: Chat) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("clientId", chat.participantId)
            putExtra("clientName", chat.participantName)
        }
        startActivity(intent)
    }
}
