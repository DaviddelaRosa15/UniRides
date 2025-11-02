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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

                    // Guardar si ya se realizó la navegación inicial
                    var isInitialized by rememberSaveable { mutableStateOf(false) }

                    // Navegar a la pantalla inicial solo una vez, basándose en el estado de autenticación
                    LaunchedEffect(authState, isInitialized) {
                        if (!isInitialized && authState !is AuthState.Loading) {
                            val destination = when (authState) {
                                is AuthState.NotAuthenticated -> Screen.Login.route
                                is AuthState.NotVerified -> Screen.EmailVerification.route
                                is AuthState.Authenticated -> Screen.Home.route
                                else -> Screen.Login.route
                            }

                            // Solo navegar si la ruta actual es diferente
                            val currentRoute =
                                navController.currentBackStackEntry?.destination?.route
                            if (currentRoute == null || currentRoute != destination) {
                                navController.navigate(destination) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            isInitialized = true
                        }
                    }

                    // Mostrar loading solo durante la carga inicial
                    if (authState is AuthState.Loading && !isInitialized) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // El NavGraph siempre existe con un destino inicial genérico
                        // El LaunchedEffect de arriba se encarga de navegar a la pantalla correcta
                        NavGraph(
                            navController = navController,
                            startDestination = Screen.Login.route
                        )
                    }
                }
            }
        }
    }
}
