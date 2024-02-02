package com.archive.chatapp.presentation.sign_in

import com.archive.chatapp.data.model.LastMessage
import com.archive.chatapp.data.model.Message

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val email: String?,
    val profilePictureUrl: String?,
    val fcmToken: String
)
data class Contacts(
    val contactList: List<String>
)
data class ChatUser(
    val profilePictureUrl: String?,
    val username: String?,
    val receiverId: String,
    val chatId: String,
    val lastMessage: LastMessage?,
    val fcmToken: String
)