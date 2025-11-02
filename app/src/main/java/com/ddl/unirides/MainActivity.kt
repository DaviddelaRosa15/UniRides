package com.ddl.unirides

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ddl.unirides.ui.navigation.AuthState
import com.ddl.unirides.ui.navigation.AuthViewModel
import com.ddl.unirides.ui.navigation.NavGraph
import com.ddl.unirides.ui.navigation.Screen
import com.ddl.unirides.ui.theme.UniRidesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniRidesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authState by authViewModel.authState.collectAsState()
                    val navController = rememberNavController()

                    // Determinar la pantalla inicial basada en el estado de autenticación
                    when (authState) {
                        is AuthState.Loading -> {
                            // Mostrar pantalla de carga mientras verifica la sesión
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is AuthState.NotAuthenticated -> {
                            // Usuario no autenticado, ir a Login
                            NavGraph(
                                navController = navController,
                                startDestination = Screen.Login.route
                            )
                        }

                        is AuthState.NotVerified -> {
                            // Usuario autenticado pero no verificado, ir a verificación
                            NavGraph(
                                navController = navController,
                                startDestination = Screen.EmailVerification.route
                            )
                        }

                        is AuthState.Authenticated -> {
                            // Usuario autenticado y verificado, ir a Home
                            NavGraph(
                                navController = navController,
                                startDestination = Screen.Home.route
                            )
                        }
                    }
                }
            }
        }
    }
}
