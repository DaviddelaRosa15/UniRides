package com.ddl.unirides.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ddl.unirides.ui.common.ProfileActionItem
import com.ddl.unirides.ui.common.ProfileAvatar
import com.ddl.unirides.ui.common.RatingBar
import com.ddl.unirides.ui.theme.UniRidesTheme
import com.ddl.unirides.util.UniversityUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToSettings: () -> Unit,
    onNavigateToMyTrips: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    showTopBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()

    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mi Perfil",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        if (onNavigateBack != null) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuración",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            ProfileContent(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToMyTrips = onNavigateToMyTrips,
                onSignOut = {
                    viewModel.signOut()
                    onSignOut()
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    } else {
        // Sin TopBar para cuando se usa con Bottom Navigation
        ProfileContent(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateToMyTrips = onNavigateToMyTrips,
            onSignOut = {
                viewModel.signOut()
                onSignOut()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileState,
    viewModel: ProfileViewModel,
    onNavigateToMyTrips: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            uiState.user != null -> {
                val user = uiState.user
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Profile Picture
                    ProfileAvatar(
                        imageUrl = user.profilePictureUrl,
                        size = 140.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Name
                    Text(
                        text = user.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // University
                    Text(
                        text = UniversityUtils.getUniversityFromEmail(user.email),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Verified Badge
                    if (user.verified) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificado",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = ".edu Verificado",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Ratings Section
                    RatingsSection(
                        ratingStats = uiState.ratingStats
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions - Agrupados con menos espaciado
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileActionItem(
                            icon = Icons.Default.DirectionsCar,
                            text = "Mis viajes",
                            onClick = onNavigateToMyTrips,
                            iconTint = MaterialTheme.colorScheme.primary
                        )

                        ProfileActionItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            text = "Cerrar Sesión",
                            onClick = onSignOut,
                            iconTint = MaterialTheme.colorScheme.error,
                            textColor = MaterialTheme.colorScheme.error,
                            showArrow = false
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RatingsSection(
    ratingStats: RatingStats
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Puntuaciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Rating Bars
        if (ratingStats.totalRatings > 0) {
            for (score in 5 downTo 1) {
                RatingBar(
                    score = score,
                    percentage = ratingStats.percentageDistribution[score] ?: 0f
                )
            }
        } else {
            Text(
                text = "Aún no tienes calificaciones",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    UniRidesTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A2F26))
        ) {
            ProfileContent(
                uiState = ProfileState(
                    user = com.ddl.unirides.data.model.User(
                        id = "1",
                        name = "Alex Johnson",
                        email = "alex@stanford.edu",
                        verified = true,
                        profilePictureUrl = null
                    ),
                    ratingStats = RatingStats(
                        totalRatings = 100,
                        averageRating = 4.5f,
                        percentageDistribution = mapOf(
                            5 to 90f,
                            4 to 8f,
                            3 to 2f,
                            2 to 0f,
                            1 to 0f
                        )
                    ),
                    isLoading = false
                ),
                viewModel = hiltViewModel(),
                onNavigateToMyTrips = {},
                onSignOut = {}
            )
        }
    }
}
