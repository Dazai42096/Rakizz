package com.rakizz.student.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.AuthToken
import com.rakizz.student.domain.repository.AuthRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AuthToken>?>(null)
    val uiState: StateFlow<UiState<AuthToken>?> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = authRepository.login(email, pass)

            result.onSuccess { token ->
                _uiState.value = UiState.Success(token)
            }.onFailure { throwable ->
                _uiState.value = UiState.Error(throwable.message ?: "Unknown error")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = null
        }
    }
}