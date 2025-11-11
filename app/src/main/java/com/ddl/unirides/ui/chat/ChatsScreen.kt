package com.ddl.unirides.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ddl.unirides.ui.chat.components.ChatPreviewCard

@Composable
fun ChatsScreen(
    onChatClick: (String) -> Unit,
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Filter chats based on search query
    val filteredChats = if (uiState.searchQuery.isEmpty()) {
        uiState.chats
    } else {
        uiState.chats.filter { chat ->
            chat.otherUserName.contains(uiState.searchQuery, ignoreCase = true) ||
                    chat.origin.contains(uiState.searchQuery, ignoreCase = true) ||
                    chat.destination.contains(uiState.searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            com.ddl.unirides.ui.chat.components.TopBarChats(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChanged
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error al cargar los chats",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.retryLoading() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                filteredChats.isEmpty() -> {
                    EmptyChatsState(
                        isSearching = uiState.searchQuery.isNotEmpty()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = filteredChats,
                            key = { it.chatId }
                        ) { chat ->
                            ChatPreviewCard(
                                chat = chat,
                                onClick = {
                                    viewModel.onChatClick(chat.chatId)
                                    onChatClick(chat.chatId)
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
private fun EmptyChatsState(
    isSearching: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Text(
            text = if (isSearching) "No se encontraron chats" else "No tienes chats aún",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = if (isSearching) {
                "Intenta con otra búsqueda"
            } else {
                "Busca un viaje y contacta al conductor para iniciar una conversación"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
