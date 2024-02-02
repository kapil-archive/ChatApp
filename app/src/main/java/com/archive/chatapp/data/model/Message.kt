package com.archive.chatapp.data.model

import com.archive.chatapp.ui.screens.dmscreen.MessageState
import com.google.firebase.Timestamp

data class Message(
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Timestamp? = Timestamp.now(),
    val messageState: Long = 0
)
data class LastMessage(
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Timestamp? = Timestamp.now(),
    val unreadCount: Long = 0,
    val messageState: Long = 0
)

