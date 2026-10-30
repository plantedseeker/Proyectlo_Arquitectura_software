package com.tab.utrabajo.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tab.utrabajo.R
import com.tab.utrabajo.data.UTrabajoRepository
import com.tab.utrabajo.presentation.navigation.Screen
import com.tab.utrabajo.presentation.components.MainTab
import com.tab.utrabajo.presentation.components.UTrabajoBottomBar
import com.tab.utrabajo.presentation.components.UTrabajoPageHeader
import com.tab.utrabajo.presentation.components.UTrabajoErrorBanner


@Composable
fun ChatListScreen(navController: NavHostController) {
    val repository = remember { UTrabajoRepository.getInstance() }
    val currentUser = repository.getCurrentUser()
    var chats by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        if (currentUser == null) {
            chats = emptyList()
            isLoading = false
        }
        val listener = repository.listenToChats(
            onUpdate = {
                chats = it.sortedByDescending { chat -> chat["lastMessageTime"]?.toString().orEmpty() }
                isLoading = false
                loadError = null
            },
            onError = { error ->
                chats = emptyList()
                isLoading = false
                loadError = error
            },
        )
        onDispose {
            listener.remove()
        }
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            UTrabajoBottomBar(
                navController = navController,
                selected = MainTab.Chats,
                companyMode = currentUser?.role == "company"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            UTrabajoPageHeader(
                title = stringResource(R.string.chat_list_title),
                subtitle = stringResource(R.string.chat_list_subtitle),
                eyebrow = "Mensajería"
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                loadError != null -> {
                    UTrabajoErrorBanner(
                        message = loadError.orEmpty(),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                chats.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = stringResource(R.string.chat_list_empty),
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.chat_list_empty),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                            Text(
                                text = stringResource(R.string.chat_list_empty_subtitle),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(items = chats, key = { it["id"].toString() }) { chat ->
                            ChatListItem(
                                chat = chat,
                                onClick = {
                                    val chatId = chat["id"].toString()
                                    navController.navigate("${Screen.ChatDetail.route}/$chatId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: Map<String, Any>,
    onClick: () -> Unit
) {
    val jobTitle = chat["jobTitle"]?.toString() ?: "Empleo"
    val lastMessage = chat["lastMessage"]?.toString() ?: "No hay mensajes"

    val timestamp = chat["lastMessageTime"]?.toString()
        ?: chat["createdAt"]?.toString().orEmpty()
    val timeText = timestamp.substringAfter('T', "").take(5)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = "Empresa",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = jobTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Text(
                text = timeText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
