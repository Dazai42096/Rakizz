package com.rakizz.student.presentation.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.FocusPolicy
import com.rakizz.student.domain.repository.FocusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FocusUiState(
    val isLoading: Boolean = false,
    val policies: List<FocusPolicy> = emptyList(),
    val errorMessage: String? = null,
    val actionMessage: String? = null
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val focusRepository: FocusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState(isLoading = true))
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    init {
        loadFocusData()
    }

    fun loadFocusData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                actionMessage = null
            )

            val result = focusRepository.getPolicies()

            result.onSuccess { rules ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    policies = rules
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not load focus rules"
                )
            }
        }
    }

    fun syncInstalledApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                actionMessage = null
            )

            val result = focusRepository.syncInstalledApps()

            result.onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actionMessage = "Phone apps synced: $count apps"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not sync phone apps"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            actionMessage = null,
            errorMessage = null
        )
    }
}