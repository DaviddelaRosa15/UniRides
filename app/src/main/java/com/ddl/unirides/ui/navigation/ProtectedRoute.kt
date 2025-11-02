package com.ddl.unirides.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.hilt.navigation.compose.hiltViewModel
import com.ddl.unirides.domain.Resource

/**
 * Componente que protege las rutas verificando que el usuario esté autenticado y verificado
 */
@Composable
fun ProtectedRoute(
    onNotAuthenticated: () -> Unit,
    onNotVerified: () -> Unit,
    viewModel: ProtectedRouteViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val userState by viewModel.userState.collectAsState()
    val saveableStateHolder = rememberSaveableStateHolder()

    LaunchedEffect(Unit) {
        viewModel.checkAuthentication()
    }

    when (userState) {
        is Resource.Success -> {
            val user = (userState as Resource.Success).data
            if (user == null) {
                LaunchedEffect(Unit) { onNotAuthenticated() }
            } else if (!user.verified) {
                LaunchedEffect(Unit) { onNotVerified() }
            } else {
                saveableStateHolder.SaveableStateProvider(key = "protected_content") {
                    content()
                }
            }
        }

        is Resource.Loading -> {
            // Mantener el contenido montado para no perder estado durante recargas breves
            saveableStateHolder.SaveableStateProvider(key = "protected_content") {
                content()
            }
        }

        is Resource.Error -> {
            LaunchedEffect(Unit) { onNotAuthenticated() }
        }
    }
}
