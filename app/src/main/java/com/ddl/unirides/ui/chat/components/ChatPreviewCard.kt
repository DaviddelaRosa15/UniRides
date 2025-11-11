package com.ddl.unirides.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ddl.unirides.ui.chat.ChatPreview
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ChatPreviewCard(
    chat: ChatPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (chat.otherUserProfilePicture != null) {
                AsyncImage(
                    model = chat.otherUserProfilePicture,
                    contentDescription = "Foto de perfil de ${chat.otherUserName}",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.width(12.dp))

            // Chat info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Route: Origin → Destination
                Text(
                    text = "${chat.origin} → ${chat.destination}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Sender name (if not current user) + Message preview
                val messageText = if (chat.lastMessageSenderName != null) {
                    "${chat.lastMessageSenderName}: ${chat.lastMessage}"
                } else {
                    chat.lastMessage
                }

                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Time and unread badge
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatTimestamp(chat.lastMessageTimestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                UnreadBadge(count = chat.unreadCount)
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val calendar = Calendar.getInstance()
    val messageCalendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }

    return when {
        // Hoy - mostrar hora
        calendar.get(Calendar.DAY_OF_YEAR) == messageCalendar.get(Calendar.DAY_OF_YEAR) &&
                calendar.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR) -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
        // Ayer
        diff < TimeUnit.DAYS.toMillis(2) -> {
            "Yesterday"
        }
        // Esta semana - mostrar día
        diff < TimeUnit.DAYS.toMillis(7) -> {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
        }
        // Más de una semana - mostrar fecha
        else -> {
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
        }
    }
}

