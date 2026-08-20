package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.data.UTrabajoRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(navController: NavHostController, chatId: String?) {
    val repository = remember { UTrabajoRepository.getInstance() }
    val currentUser = repository.getCurrentUser()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("Chat") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(chatId) {
        if (chatId != null) {
            repository.getChatsForUser("", "", onSuccess = { chats ->
                title = chats.firstOrNull { it["id"]?.toString() == chatId }
                    ?.get("jobTitle")?.toString() ?: "Chat"
            }, onError = {})
        }
    }

    DisposableEffect(chatId) {
        val listener = if (chatId == null) null else repository.listenToMessages(
            chatId = chatId,
            onUpdate = {
                messages = it
                scope.launch {
                    if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
                }
            },
            onError = { errorMessage = it },
        )
        onDispose { listener?.remove() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFE9F3F8)),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages, key = { it["id"]?.toString().orEmpty() }) { message ->
                    MessageBubble(message, message["senderId"]?.toString() == currentUser?.uid)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje") },
                    shape = RoundedCornerShape(24.dp),
                    enabled = chatId != null && currentUser != null,
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val text = newMessage.trim()
                        val user = currentUser
                        if (text.isNotEmpty() && chatId != null && user != null) {
                            repository.sendMessage(
                                chatId = chatId,
                                senderId = user.uid,
                                messageText = text,
                                onSuccess = { newMessage = ""; errorMessage = null },
                                onError = { errorMessage = it },
                            )
                        }
                    },
                    modifier = Modifier.size(56.dp).background(Color(0xFF2B7BBF), CircleShape),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Map<String, Any>, isOwnMessage: Boolean) {
    val text = message["message"]?.toString().orEmpty()
    val timestamp = message["timestamp"]?.toString().orEmpty()
    val timeText = timestamp.substringAfter('T', "").take(5)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
    ) {
        Column(horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .background(if (isOwnMessage) Color(0xFF2B7BBF) else Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(text, color = if (isOwnMessage) Color.White else Color.Black, fontSize = 14.sp)
            }
            Text(timeText, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
        }
    }
}
