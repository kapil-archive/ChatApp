package com.archive.chatapp.ui.screens.chatscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.archive.chatapp.MainActivity
import com.archive.chatapp.presentation.sign_in.ChatUser
import com.archive.chatapp.presentation.sign_in.UserData
import com.archive.chatapp.ui.screens.dmscreen.getMessageIdFromState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    user: UserData,
    onSignOut: () -> Unit,
    onChatClick: (ChatUser) -> Unit
) {
    val viewModel: ChatViewModel = viewModel()
    val chatUsers by viewModel.chatUsers.collectAsState()
    val users by viewModel.users.collectAsState()
    Box(
        Modifier.fillMaxSize(),
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            var expanded by remember {
                mutableStateOf(false)
            }
            var position by remember {
                mutableStateOf(Offset.Zero)
            }
            TopAppBar(title = {
                Text(text = "ChatApp")
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            ),
                actions = {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.onGloballyPositioned {
                            position = it.windowToLocal(Offset(0f, 0f))
                        }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false },
                        offset = DpOffset(x = position.x.dp, y = (position.y).dp),
                        modifier = Modifier
                            .wrapContentSize()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(text = {
                            Text(
                                text = "Sign Out",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }, onClick = onSignOut)
                    }
                }
            )
            if (chatUsers != null) {
                LazyColumn(userScrollEnabled = true) {
                    items(items = chatUsers!!.sortedByDescending { it.lastMessage?.timestamp }) {
                        ChatCard(user = it) {
                            onChatClick(it.copy())
                        }
                    }
                }
            }

        }
        var userAddEnabled by remember {
            mutableStateOf(false)
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { userAddEnabled = true }
                .padding(12.dp)
        ){
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contacts")
        }
        if (userAddEnabled) {
            UserAddDialog(
                users = users, onDismiss = {
                    userAddEnabled = false
                    viewModel.getAllUsers()
                },
                onCLick = { id ->
                    viewModel.startDM(if (MainActivity.CURRENT_USER_ID!! < id.userId) MainActivity.CURRENT_USER_ID + id.userId else id.userId + MainActivity.CURRENT_USER_ID)
                }, chats = chatUsers
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAddDialog(
    users: List<UserData>?,
    chats: List<ChatUser>?,
    onDismiss: () -> Unit,
    onCLick: (UserData) -> Unit
) {
    var query by remember {
        mutableStateOf("")
    }
    var active by remember {
        mutableStateOf(
            false
        )
    }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            SearchBar(leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
                placeholder = { Text(text = "Search in users") },
                enabled = true,
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                active = active,
                onActiveChange = { active = !active }
            ) {
                users?.filter { it.username?.lowercase()?.contains(query.lowercase()) == true }
                    ?.forEach { user ->
                        UserCard(user = user, onCLick = {
                            onCLick(user)
                        }, added = chats?.any { it.receiverId == user.userId } == true)
                    }
            }
            if (!active) Column {
                users?.filter { it.username?.lowercase()?.contains(query.lowercase()) == true || it.email?.lowercase()?.contains(query.lowercase()) == true }
                    ?.forEach { user ->
                        UserCard(user = user, onCLick = {
                            onCLick(user)
                        }, added = chats?.any { it.receiverId == user.userId } == true)
                    }
            }
        }
    }
}

@Composable
fun ChatCard(user: ChatUser, onCLick: () -> Unit) {
    var picClicked by remember {
        mutableStateOf(false)
    }
    if (picClicked){
        Dialog(onDismissRequest = { picClicked = false }) {
            AsyncImage(
                model = user.profilePictureUrl,
                contentDescription = null, // Required for accessibility
                modifier = Modifier
                    .size(256.dp) // Adjust image size as needed
            )
        }
    }
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onCLick() }
        .padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.Top,
            ) {
                AsyncImage(
                    model = user.profilePictureUrl,
                    contentDescription = null, // Required for accessibility
                    modifier = Modifier
                        .size(56.dp) // Adjust image size as needed
                        .clip(CircleShape) // Round the image
                        .clickable { picClicked = true }
                )
                Spacer(Modifier.width(16.dp)) // Add spacing between image and name
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                    horizontalAlignment = Alignment.Start
                ) {

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.username ?: "",
                            style = MaterialTheme.typography.titleMedium,// Use title size for name
                            modifier = Modifier.padding(top = 0.dp)
                        )
                        Text(
                            text = user.lastMessage?.timestamp?.let {
                                com.archive.chatapp.ui.screens.dmscreen.timeStampToHour(
                                    it
                                )
                            } ?: "",
                            style = MaterialTheme.typography.bodySmall // Use title size for name
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val lastMessageReceived = user.lastMessage?.receiverId == MainActivity.CURRENT_USER_ID
                        Row(
                            Modifier.fillMaxWidth(0.9f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                        ){
                            if (!lastMessageReceived){
                                Icon(painter = painterResource(id = getMessageIdFromState(user.lastMessage?.messageState ?: 3L)), contentDescription = "Message State")
                            }
                            Text(
                                text = user.lastMessage?.text ?: "",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if(user.lastMessage?.unreadCount != 0L && lastMessageReceived){
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                                    .size(24.dp),
                                contentAlignment = Alignment.Center
                            ){
                                Text(
                                    text = "${user.lastMessage?.unreadCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.surface,
                                )
                            }
                        }

                    }
                }
            }

        }
    }
}

@Composable
fun UserCard(user: UserData, onCLick: () -> Unit, added: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.Top,
            ) {
                AsyncImage(
                    model = user.profilePictureUrl,
                    contentDescription = null, // Required for accessibility
                    modifier = Modifier
                        .size(56.dp) // Adjust image size as needed
                        .clip(CircleShape) // Round the image
                )
                Spacer(Modifier.width(16.dp)) // Add spacing between image and name
                Box(
                    Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = user.username ?: "",
                            style = MaterialTheme.typography.titleMedium,// Use title size for name
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(text = user.email ?: "", style = MaterialTheme.typography.bodyMedium)

                    }
                    IconButton(
                        onClick = { if (!added) onCLick() },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = if (added) Icons.Default.CheckCircle else Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    }
                }


            }

        }
    }
}

fun getChatIdFromUserId(uid1: String, uid2: String): String {
    return if (uid1 < uid2) {
        uid1 + uid2
    } else uid2 + uid1
}