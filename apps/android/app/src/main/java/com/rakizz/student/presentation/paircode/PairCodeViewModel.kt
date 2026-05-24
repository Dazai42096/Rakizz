package com.rakizz.student.presentation.paircode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.data.network.RakizzApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PairCodeUiState(
    val isLoading: Boolean = false,
    val pairCode: String = "",
    val error: String? = null
)

@HiltViewModel
class PairCodeViewModel @Inject constructor(
    private val api: RakizzApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairCodeUiState())
    val uiState: StateFlow<PairCodeUiState> = _uiState.asStateFlow()

    init {
        loadPairCode()
    }

    fun loadPairCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val response = api.generatePairCode()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pairCode = response.pairCode,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not load pair code"
                )
            }
        }
    }
}