package com.ddl.unirides.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    LaunchedEffect(Unit) {
        viewModel.checkAuthentication()
    }

    when (userState) {
        is Resource.Success -> {
            val user = (userState as Resource.Success).data
            if (user == null) {
                // Usuario no autenticado
                LaunchedEffect(Unit) {
                    onNotAuthenticated()
                }
            } else if (!user.verified) {
                // Usuario no verificado
                LaunchedEffect(Unit) {
                    onNotVerified()
                }
            } else {
                // Usuario autenticado y verificado, mostrar contenido
                content()
            }
        }

        is Resource.Loading -> {
            // Mostrar loading mientras verifica
            // Puedes poner un composable de loading aquí si quieres
        }

        is Resource.Error -> {
            // Error al verificar, redirigir a login
            LaunchedEffect(Unit) {
                onNotAuthenticated()
            }
        }
    }
}

