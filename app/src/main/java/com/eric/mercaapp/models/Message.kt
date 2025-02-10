package com.eric.mercaapp.models

import androidx.annotation.Keep

@Keep
data class Message(
    var text: String = "",
    var senderId: String = "",
    var receiverId: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", 0L)
}
