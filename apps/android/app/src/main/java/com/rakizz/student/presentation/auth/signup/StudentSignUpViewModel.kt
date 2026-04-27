package com.rakizz.student.presentation.auth.signup

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.AuthToken
import com.rakizz.student.domain.repository.AuthRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StudentSignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AuthToken>?>(null)
    val uiState: StateFlow<UiState<AuthToken>?> = _uiState.asStateFlow()

    fun register(
        fullName: String,
        email: String,
        pass: String
    ) {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        val cleanPass = pass

        when {
            cleanName.length < 2 -> {
                _uiState.value = UiState.Error("Enter your full name")
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> {
                _uiState.value = UiState.Error("Enter a valid email")
                return
            }
            cleanPass.length < 8 -> {
                _uiState.value = UiState.Error("Password must be at least 8 characters")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = authRepository.register(
                fullName = cleanName,
                email = cleanEmail,
                pass = cleanPass
            )

            result.onSuccess { token ->
                _uiState.value = UiState.Success(token)
            }.onFailure { throwable ->
                _uiState.value = UiState.Error(throwable.message ?: "Signup failed")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = null
        }
    }
}