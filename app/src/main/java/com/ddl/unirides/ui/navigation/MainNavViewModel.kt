package com.ddl.unirides.ui.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainNavViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_SELECTED_ROUTE = "selected_route"
    }

    private val _selectedRoute = MutableStateFlow(
        savedStateHandle.get<String>(KEY_SELECTED_ROUTE) ?: Screen.Home.route
    )
    val selectedRoute: StateFlow<String> = _selectedRoute.asStateFlow()

    fun selectRoute(route: String) {
        _selectedRoute.value = route
        savedStateHandle[KEY_SELECTED_ROUTE] = route
    }
}
