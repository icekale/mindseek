package com.mindseek.podcast.presentation.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindseek.podcast.core.error.ErrorEvent
import com.mindseek.podcast.core.error.ErrorHandler
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * Global error handler composable that listens to error events and displays appropriate UI
 */
@Composable
fun GlobalErrorHandler(
    errorHandler: ErrorHandler,
    snackbarHostState: SnackbarHostState,
    onRetry: ((ErrorEvent) -> Unit)? = null
) {
    var currentError by remember { mutableStateOf<ErrorEvent?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    
    // Listen to error events
    LaunchedEffect(errorHandler) {
        errorHandler.errorEvents.collectLatest { errorEvent ->
            currentError = errorEvent
            
            // Decide whether to show dialog or snackbar based on error severity
            when (errorEvent) {
                is ErrorEvent.NetworkError,
                is ErrorEvent.ServerError -> {
                    // Show snackbar for network/server errors
                    snackbarHostState.showSnackbar(
                        message = errorEvent.message,
                        actionLabel = if (errorEvent.isRetryable) "重试" else null
                    )
                }
                is ErrorEvent.ClientError,
                is ErrorEvent.AudioError,
                is ErrorEvent.StorageError,
                is ErrorEvent.DataError,
                is ErrorEvent.UnknownError,
                is ErrorEvent.CustomError -> {
                    // Show dialog for other errors
                    showDialog = true
                }
            }
        }
    }
    
    // Show error dialog when needed
    if (showDialog && currentError != null) {
        ErrorDialog(
            errorEvent = currentError!!,
            onDismiss = { 
                showDialog = false
                currentError = null
            },
            onRetry = if (currentError!!.isRetryable && onRetry != null) {
                {
                    onRetry(currentError!!)
                    showDialog = false
                    currentError = null
                }
            } else null
        )
    }
}

/**
 * ViewModel for managing global error state
 */
class GlobalErrorViewModel @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    val errorEvents = errorHandler.errorEvents
    
    suspend fun handleError(throwable: Throwable, context: String = "") {
        errorHandler.handleError(throwable, context)
    }
    
    suspend fun handleError(message: String, context: String = "", isRetryable: Boolean = false) {
        errorHandler.handleError(message, context, isRetryable)
    }
}