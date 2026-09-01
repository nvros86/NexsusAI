package com.nexusai.core.common

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object ErrorHandler {

    fun getLocalizedMessage(error: Throwable): String {
        return when (error) {
            is java.net.UnknownHostException -> "Нет подключения к интернету"
            is java.net.SocketTimeoutException -> "Превышено время ожидания"
            is java.net.ConnectException -> "Не удалось подключиться к серверу"
            is java.io.IOException -> "Ошибка ввода/вывода"
            is SecurityException -> "Ошибка доступа"
            is IllegalArgumentException -> "Некорректные данные"
            is IllegalStateException -> "Некорректное состояние"
            is CancellationException -> throw error
            else -> error.message ?: "Произошла неизвестная ошибка"
        }
    }

    fun safeLaunch(
        scope: CoroutineScope,
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return scope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }
}
