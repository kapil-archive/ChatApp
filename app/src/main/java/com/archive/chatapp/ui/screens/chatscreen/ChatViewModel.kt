package com.archive.chatapp.ui.screens.chatscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.archive.chatapp.MainActivity
import com.archive.chatapp.data.model.LastMessage
import com.archive.chatapp.data.model.Message
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.archive.chatapp.presentation.sign_in.ChatUser
import com.archive.chatapp.presentation.sign_in.UserData
import com.archive.chatapp.ui.screens.dmscreen.ChatId
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatViewModel: ViewModel() {

    private val _users = MutableStateFlow<List<UserData>?>(null)
    val users = _users.asStateFlow()
    private val _chatUsers = MutableStateFlow<List<ChatUser>?>(null)
    val chatUsers = _chatUsers.asStateFlow()

    private val firestore = Firebase.firestore

    private fun updateChatUsers(chtUsr:ChatUser){
        var item = chtUsr
        _chatUsers.update { list->
            if (list!=null){
                if (list.any {
                        if (it.chatId == chtUsr.chatId) {
                            item = it
                        }
                        it.chatId == chtUsr.chatId
                }){
                    val newList = list.toMutableList()
                    newList.add(chtUsr)
                    newList.remove(item)
                    newList
                }else{
                    val newList = list.toMutableList()
                    newList.add(chtUsr)
                    newList
                }
            }else{
                listOf(chtUsr)
            }
        }
        Log.i("CHAT UPDATE","Called- ${_chatUsers.value}")
    }

    fun getAllUsers(){
        val collection = firestore.collection("users")
        collection.addSnapshotListener { value, error ->
            val userList = mutableListOf<UserData>()
            if (value != null) {
                for (document in value) {
                    // Convert each document to a User object
                    val user = UserData(
                        userId = document.getString("userId") ?: "",
                        username = document.getString("username") ?: "", // You can set other fields to default values or omit them if not needed
                        email = document.getString("email")?: "",
                        profilePictureUrl = document.getString("profilePictureUrl") ?: "",
                        fcmToken = document.getString("fcmToken")?: ""
                    )
                    getChat(
                        username = user.username, profilePictureUrl = user.profilePictureUrl,
                        user = user.userId, fcmToken = user.fcmToken )
                    userList.add(user)
                }
                _users.update { userList.toList() }
                Log.i("CHAT SCREEN", "$userList")
            }
            if (error != null) {
                Log.e("USERS ERROR", "${ error.message }")
            }
        }
    }

    private fun getChat(user:String,username:String?,profilePictureUrl:String?,fcmToken:String){
        val chatId = if (MainActivity.CURRENT_USER_ID!! <user) MainActivity.CURRENT_USER_ID+user else user + MainActivity.CURRENT_USER_ID
        Log.i("CHAT SCREEN","chatId $chatId")
        firestore.collection("chats").document(chatId).addSnapshotListener { document, error ->
            if (document!=null){
                val map = document.data
                val messageMap = map?.get("message") as Map<*, *>?
                val senderId = messageMap?.get("senderId")?: ""
                val receiverId = messageMap?.get("receiverId")?: ""
                val text = messageMap?.get("text")?:""
                val timestampMap = messageMap?.get("timestamp")
                val unreadCount = messageMap?.get("unreadCount") as Long? ?: 0L
                val messageState = messageMap?.get("messageState") as Long? ?: 0L

                val timestamp = timestampMap as Timestamp?

                Log.i("Unique",senderId.toString())
                val chatUser = ChatUser(
                    profilePictureUrl = profilePictureUrl,
                    username = username,
                    chatId = document.getString("chatId") ?: "",
                    lastMessage = LastMessage(
                        senderId.toString(),
                        receiverId.toString(),
                        text.toString(),
                        timestamp,
                        unreadCount = unreadCount,
                        messageState = messageState
                    ),
                    receiverId = user,
                    fcmToken = fcmToken
                )
                if (chatUser.chatId != "") updateChatUsers(chatUser)
                Log.i("CHAT SCREEN","chat user: $chatUser")
            }else if (error!=null){
                Log.e("USERS ERROR", "${ error.message }")
            }
        }
    }
    fun startDM(chatId: String){
        firestore.collection("chats")
            .document(chatId).set(ChatId(chatId = chatId)).addOnSuccessListener {
                Log.i("DM CHAT","SUCCESS chatID")
            }.addOnFailureListener {
                Log.i("DM CHAT",it.message.toString())
            }
    }
    init {
        getAllUsers()
    }

}