package com.eric.mercaapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.eric.mercaapp.R
import com.eric.mercaapp.models.Chat

class ChatsAdapter(
    private var chatList: MutableList<Chat> = mutableListOf(),
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view, onChatClick)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int = chatList.size

    fun updateChats(newChatList: List<Chat>) {
        val diffCallback = ChatDiffCallback(chatList, newChatList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        chatList.clear()
        chatList.addAll(newChatList)
        diffResult.dispatchUpdatesTo(this)
    }

    class ChatViewHolder(itemView: View, private val onChatClick: (Chat) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val textViewChatName: TextView = itemView.findViewById(R.id.textViewChatName)
        private val textViewLastMessage: TextView = itemView.findViewById(R.id.textViewLastMessage)

        fun bind(chat: Chat) {
            textViewChatName.text = chat.participantName.takeIf { !it.isNullOrEmpty() } ?: "Usuario desconocido"
            textViewLastMessage.text = chat.lastMessage.takeIf { !it.isNullOrEmpty() } ?: "Sin mensajes aún"

            itemView.setOnClickListener { onChatClick(chat) }
        }
    }

    class ChatDiffCallback(
        private val oldList: List<Chat>,
        private val newList: List<Chat>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].chatId == newList[newItemPosition].chatId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
