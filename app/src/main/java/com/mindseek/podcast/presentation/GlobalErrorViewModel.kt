package com.mindseek.podcast.presentation

import androidx.lifecycle.ViewModel
import com.mindseek.podcast.core.error.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing global error state
 */
@HiltViewModel
class GlobalErrorViewModel @Inject constructor(
    val errorHandler: ErrorHandler
) : ViewModel()