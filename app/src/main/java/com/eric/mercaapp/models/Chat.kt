package com.eric.mercaapp.models

import androidx.annotation.Keep

@Keep
data class Chat(
    var chatId: String = "",
    var participantId: String = "",
    var participantName: String = "",
    var lastMessage: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var participants: List<String> = listOf()
) {
    constructor() : this("", "", "", "", 0L, listOf())
}
