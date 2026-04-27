package com.rakizz.student.presentation.assignments.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.Assignment
import com.rakizz.student.domain.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssignmentsUiState(
    val isLoading: Boolean = false,
    val assignments: List<Assignment> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AssignmentsViewModel @Inject constructor(
    private val repository: AssignmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignmentsUiState(isLoading = true))
    val uiState: StateFlow<AssignmentsUiState> = _uiState.asStateFlow()

    init {
        loadAssignments()
    }

    fun loadAssignments(showBlockingLoader: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = showBlockingLoader,
                errorMessage = null
            )

            try {
                val assignments = repository.getAssignments()
                _uiState.value = AssignmentsUiState(
                    isLoading = false,
                    assignments = assignments,
                    errorMessage = null
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = t.userMessage("Failed to load assignments.")
                )
            }
        }
    }
}

private fun Throwable.userMessage(defaultMessage: String): String {
    val message = this.message?.trim().orEmpty()
    return if (message.isBlank()) defaultMessage else message
}