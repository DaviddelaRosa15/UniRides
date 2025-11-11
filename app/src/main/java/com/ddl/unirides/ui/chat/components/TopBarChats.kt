package com.ddl.unirides.ui.chat.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarChats(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (isSearchExpanded) {
                ChatSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else {
                Text(
                    text = "Chats",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    isSearchExpanded = !isSearchExpanded
                    if (!isSearchExpanded) {
                        onSearchQueryChange("")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = if (isSearchExpanded) "Cerrar búsqueda" else "Buscar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = modifier
    )
}

