package com.archive.chatapp.ui.screens.dmscreen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.archive.chatapp.MainActivity
import com.archive.chatapp.R
import com.archive.chatapp.data.model.LastMessage
import com.archive.chatapp.data.model.Message
import com.archive.chatapp.presentation.sign_in.ChatUser
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DMScreen(chatUser: ChatUser, navigateBack: () -> Unit) {
    val viewModel: DMViewModel = viewModel()
    viewModel.updateChatId(chatUser.chatId)
    viewModel.getRecentMessages()
    val messages by viewModel.messages.collectAsState()
    val uid = MainActivity.CURRENT_USER_ID
    val msgInd by viewModel.msgInd.collectAsState()
    Column {
        TopAppBar(title = {
            chatUser.username?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            ), navigationIcon = {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .clickable {
                            navigateBack()
                        }
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    AsyncImage(
                        model = chatUser.profilePictureUrl,
                        contentDescription = "Image",
                        modifier = Modifier.clip(
                            CircleShape
                        )
                    )
                }
            })
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(0.2f))
                .padding(horizontal = 0.dp, vertical = 0.dp),
        ) {
            LazyColumn(
                userScrollEnabled = true,
                reverseLayout = true,
                content = {

                    item {
                        Spacer(modifier = Modifier.size(66.dp))
                    }
                    Log.i("MESSAGE COUNT",messages?.size.toString())
                    if (messages != null) itemsIndexed(messages!!.asReversed()) { index, message ->
                        val alignment = if (uid == message.senderId) Alignment.End else Alignment.Start
                        val messageIconId = getMessageIdFromState(message.messageState)
                        if (message.senderId == chatUser.receiverId && message.messageState!=2L){
                            viewModel.updateMessageState(message.copy(messageState = 2L))
                            if (index == 0) viewModel.updateLastMessage(
                                LastMessage(
                                    senderId = message.senderId,
                                    receiverId = message.receiverId,
                                    timestamp = message.timestamp,
                                    unreadCount = 0L,
                                    messageState = 2L,
                                    text = message.text
                                ))
                            Log.d("SEEN","SEEN UPDATE CALLED")
                        }
                        MessageCard(
                            message = message,
                            alignment = alignment,
                            onLongClick = {
                                val list = msgInd
                                list.add(index)
                                viewModel.updateMsgInd(list)
                            },
                            onClick = {
                                Log.i("MESSAGE", "CLICK")
                            },
                            messageIconId = if (uid == message.senderId) messageIconId else null
                        )

                        Spacer(modifier = Modifier.size(6.dp))


                    }
                    item {
//                        viewModel.getRecentMessages()
                        Spacer(modifier = Modifier.size(6.dp))
                    }
                })
            var text by remember {
                mutableStateOf("")
            }

            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(0.2f))
                    .padding(4.dp)
            ) {
                TypeBox(
                    text = text, onValueChange = { text = it },
                    onSendClick = {
                        if (uid != null) {
                            viewModel.sendMessage(
                                Message(
                                    senderId = uid,
                                    receiverId = chatUser.receiverId,
                                    text = text,
                                ),
                                fcmToken = chatUser.fcmToken
                            )
                            text = ""
                        }

                    }, modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageCard(
    message: Message,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    messageIconId: Int?,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(
                end = if (alignment == Alignment.Start) 70.dp else 4.dp,
                start = if (alignment != Alignment.Start) 70.dp else 4.dp
            )
    ) {
        Column(
            modifier
                .align(alignment)
                .shadow(1.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color.Black)
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { onLongClick() })
                .background(if (alignment== Alignment.Start) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier
                    .align(alignment)
                    .padding(top = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
                ){
                Text(text = message.timestamp?.let { timeStampToHour(it) } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.5f)
                )
                if (messageIconId!=null) Icon(/*modifier = Modifier.size(16.dp)*/painter = painterResource(id = messageIconId), contentDescription = "Message State")
            }
        }
    }
}

@Composable
fun TypeBox(
    text: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSendClick: () -> Unit
) {
    TextField(
        value = text, onValueChange = onValueChange,
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface
        ),
        placeholder = { Text(text = "Message") },
        modifier = modifier
            .shadow(1.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black)
            .fillMaxWidth()
            .padding(),
        shape = RoundedCornerShape(24.dp),
        trailingIcon = {
            if (text != "") {
                IconButton(onClick = onSendClick) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "send message")
                }
            }
        },
    )
}

fun timeStampToHour(timestamp: Timestamp): String {
    val time = timestamp.toDate()
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Log.i("TESTING TIME", dateFormat.format(time))
    return dateFormat.format(time)
}

fun getMessageIdFromState(messageState: Long): Int {
    return when (messageState){
        0L -> {
            R.drawable.sharp_access_time_24
        }

        1L -> {
            R.drawable.baseline_done_24
        }

        2L -> {
            R.drawable.baseline_done_all_24
        }

        3L -> {
            R.drawable.baseline_error_24
        }

        else -> {
            R.drawable.sharp_access_time_24
        }
    }
}