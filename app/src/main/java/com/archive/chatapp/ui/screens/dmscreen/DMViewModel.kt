package com.archive.chatapp.ui.screens.dmscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archive.chatapp.data.model.LastMessage
import com.archive.chatapp.data.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DMViewModel : ViewModel(){
    companion object{
        const val TAG= "DM_VM"
    }

    private val firestore = Firebase.firestore
    private val _messages= MutableStateFlow<List<Message>?>(null)
    val messages = _messages.asStateFlow()
    val uid = Firebase.auth.uid
    private var docPath = ""
    private val _msgInd = MutableStateFlow<MutableList<Int>>(mutableListOf())
    val msgInd = _msgInd.asStateFlow()
    private var unreadCount = 1L
    private var limit = 15L
    private var count = 0
    private var query1: Query? = null


    fun updateMsgInd(indList: MutableList<Int>){
        _msgInd.update { indList }
    }
    fun updateChatId(chatId:String){
        docPath = chatId
    }

    fun getRecentMessages(){
        viewModelScope.launch {
            val collection1 = firestore.collection("chats")
                .document(docPath)
                .collection("messages")

            fun paginate(query: CollectionReference) {
                Log.e("count", query.toString())
                query.addSnapshotListener { value, error ->
                    if (value!=null) {
                        val messageList = mutableListOf<Message>()
                        unreadCount = 1L
                        value.documents.forEach{document ->
                            val message = Message(
                                senderId = document.getString("senderId") ?: "",
                                receiverId = document.getString("receiverId") ?: "", // You can set other fields to default values or omit them if not needed
                                text = document.getString("text") ?: "",
                                timestamp = document.getTimestamp("timestamp") ?: Timestamp.now(),
                                messageState = document.get("messageState") as Long? ?: 0,
                            )
                            if (message.messageState == 1L) unreadCount++
                            messageList.add(message)
                        }
                        _messages.update { messageList.toList() }

                    }else if (error!=null){
                        Log.e("Chats", "${ error.message }")
                    }
                }
            }
            Log.e("count", "$query1 and $collection1")
            paginate(collection1)
            Log.i(TAG+" count and mssg","$count ${messages.value?.size}")
        }
    }








    fun sendMessage(message: Message,fcmToken: String){
        val messageId = message.timestamp?.seconds.toString()+message.timestamp?.nanoseconds.toString()
        val collection = firestore.collection("chats")
            .document(docPath)
            .collection("messages")
        collection.document(messageId)
            .set(message.copy(messageState = 0))
            .addOnSuccessListener {


            }.addOnCompleteListener {

                Log.i("UNREAD","$unreadCount SENT")

                collection.document(messageId).update(mapOf("messageState" to 1L))
//                val remoteMessage = RemoteMessage.Builder(fcmToken)
//                    .addData("title", message.senderId)  // Consider using senderId as the title
//                    .addData("text", message.text)
//                    .setMessageId(messageId)
//                    .build()
//
//                try {
//                    FirebaseMessaging.getInstance().send(remoteMessage)
//                    Log.e("FCM MESSAGE SEND","success")
//                } catch (e: Exception) {
//                    // Handle exceptions, e.g., logging or displaying an error message
//                    e.printStackTrace()
//                    Log.e("FCM MESSAGE SEND",e.message.toString())
//                }

                updateLastMessage(LastMessage(
                    senderId = message.senderId,
                    receiverId = message.receiverId,
                    text = message.text,
                    timestamp = message.timestamp,
                    unreadCount = unreadCount ,
                    messageState = 1))
        }
            .addOnFailureListener{
            Log.i("CHAT",it.message.toString())
        }


    }
    fun updateMessageState(message: Message){
        val collection = firestore.collection("chats")
            .document(docPath)
            .collection("messages")
        collection.document(message.timestamp?.seconds.toString()+message.timestamp?.nanoseconds.toString())
            .set(message).addOnSuccessListener {
                Log.d("SEEN","Success updated")
            }.addOnFailureListener {
                Log.i("SEEN",it.message.toString())
            }
    }
    fun updateLastMessage(message: LastMessage){
        firestore.collection("chats")
            .document(docPath).update(mapOf("message" to message)).addOnSuccessListener {
                unreadCount = 0L
            }

        /*.set(message).addOnSuccessListener {
                Log.i("DM CHAT","SUCCESS chatID")
            }.addOnFailureListener {
                Log.i("DM CHAT",it.message.toString())
            }*/
        Log.d("UPDATE MSSG",message.toString())
    }
    fun updateUnreadCount(){

    }
}
data class ChatId(
    val chatId: String
)
enum class MessageState {
    SENDING_STATE,
    SENT_STATE,
    RECEIVED_STATE,
    SEEN_STATE
}