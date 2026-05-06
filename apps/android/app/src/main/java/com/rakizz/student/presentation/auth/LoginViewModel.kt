package com.rakizz.student.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.domain.repository.AuthRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginResult(
    val role: String
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val api: RakizzApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<LoginResult>?>(null)
    val uiState: StateFlow<UiState<LoginResult>?> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
    viewModelScope.launch {

        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = UiState.Error("Please enter your email and password")
            return@launch
        }

        _uiState.value = UiState.Loading

        val result = authRepository.login(email, pass)

            result.onSuccess {
                try {
                    // after login, get the logged-in user role from backend
                    val me = api.getMe()

                    _uiState.value = UiState.Success(
                        LoginResult(
                            role = me.role
                        )
                    )
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(
                        e.message ?: "Login worked, but could not load user role"
                    )
                }
            }.onFailure { throwable ->
                _uiState.value = UiState.Error(
                    throwable.message ?: "Unknown error"
                )
            }
        }
    }

    fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = null
        }
        
    }
}