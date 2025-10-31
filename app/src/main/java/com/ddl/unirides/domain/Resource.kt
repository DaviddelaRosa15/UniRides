package com.ddl.unirides.domain

/**
 * Clase sellada que representa el estado de una operación
 * Utilizada para manejar estados de carga, éxito y error
 */
sealed class Resource<out T> {
    /**
     * Estado de éxito con datos
     */
    data class Success<T>(val data: T) : Resource<T>()

    /**
     * Estado de error con mensaje
     */
    data class Error(val message: String) : Resource<Nothing>()

    /**
     * Estado de carga
     */
    object Loading : Resource<Nothing>()
}

