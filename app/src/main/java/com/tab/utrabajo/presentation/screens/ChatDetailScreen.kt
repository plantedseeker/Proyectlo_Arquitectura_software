package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tab.utrabajo.R
import com.tab.utrabajo.data.UTrabajoRepository
import com.tab.utrabajo.presentation.components.UTrabajoEmptyState
import com.tab.utrabajo.presentation.components.UTrabajoErrorBanner
import com.tab.utrabajo.presentation.components.UTrabajoLoadingState
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
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(chatId) {
        if (chatId != null) {
            repository.getChatsForUser("", "", onSuccess = { chats ->
                title = chats.firstOrNull { it["id"]?.toString() == chatId }
                    ?.get("jobTitle")?.toString() ?: "Chat"
            }, onError = {})
        }
    }

    DisposableEffect(chatId) {
        val listener = if (chatId == null) {
            isLoading = false
            null
        } else {
            repository.listenToMessages(
                chatId = chatId,
                onUpdate = {
                    messages = it
                    isLoading = false
                    errorMessage = null
                    scope.launch {
                        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
                    }
                },
                onError = {
                    isLoading = false
                    errorMessage = it
                },
            )
        }
        onDispose { listener?.remove() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Conversación segura",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            errorMessage?.let {
                UTrabajoErrorBanner(
                    message = it,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when {
                    isLoading -> UTrabajoLoadingState(modifier = Modifier.fillMaxSize())
                    messages.isEmpty() -> UTrabajoEmptyState(
                        title = "Aún no hay mensajes",
                        subtitle = "Rompe el hielo y comienza la conversación.",
                        icon = Icons.Outlined.Forum,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(messages, key = { it["id"]?.toString().orEmpty() }) { message ->
                            MessageBubble(
                                message = message,
                                isOwnMessage = message["senderId"]?.toString() == currentUser?.uid
                            )
                        }
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.chat_detail_input_placeholder)) },
                        shape = MaterialTheme.shapes.extraLarge,
                        maxLines = 4,
                        enabled = chatId != null && currentUser != null,
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            val text = newMessage.trim()
                            val user = currentUser
                            if (text.isNotEmpty() && chatId != null && user != null) {
                                repository.sendMessage(
                                    chatId = chatId,
                                    senderId = user.uid,
                                    messageText = text,
                                    onSuccess = {
                                        newMessage = ""
                                        errorMessage = null
                                    },
                                    onError = { errorMessage = it },
                                )
                            }
                        },
                        enabled = newMessage.isNotBlank() && chatId != null && currentUser != null,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
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
    val bubbleColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val textColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
    ) {
        Column(horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start) {
            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isOwnMessage) 18.dp else 4.dp,
                    bottomEnd = if (isOwnMessage) 4.dp else 18.dp
                ),
                shadowElevation = if (isOwnMessage) 0.dp else 1.dp
            ) {
                Text(
                    text = text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
            if (timeText.isNotBlank()) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}