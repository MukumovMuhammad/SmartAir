package com.example.smartairmonitoring.modul.core.network

sealed class NetworkResponse<out T> {
    data class Success<out T>(val data: T): NetworkResponse<T>()
    data class Error(val message: String): NetworkResponse<Nothing>()
    object Loading : NetworkResponse<Nothing>()
    object Idle : NetworkResponse<Nothing>()

}