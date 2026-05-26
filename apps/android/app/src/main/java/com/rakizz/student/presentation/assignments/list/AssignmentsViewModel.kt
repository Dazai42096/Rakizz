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
    val isUpdating: Boolean = false,
    val assignments: List<Assignment> = emptyList(),
    val actionMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AssignmentsViewModel @Inject constructor(
    private val repository: AssignmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AssignmentsUiState(isLoading = true)
    )
    val uiState: StateFlow<AssignmentsUiState> = _uiState.asStateFlow()

    init {
        loadAssignments()
    }

    fun loadAssignments(showBlockingLoader: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = showBlockingLoader,
                errorMessage = null,
                actionMessage = null
            )

            try {
                val assignments = repository.getAssignments()

                _uiState.value = AssignmentsUiState(
                    isLoading = false,
                    isUpdating = false,
                    assignments = assignments,
                    actionMessage = null,
                    errorMessage = null
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isUpdating = false,
                    errorMessage = t.userMessage("Failed to load assignments.")
                )
            }
        }
    }

    fun markAssignmentCompleted(assignmentId: String) {
        updateStatus(
            assignmentId = assignmentId,
            status = "COMPLETED",
            successMessage = "Assignment marked as completed."
        )
    }

    fun reopenAssignment(assignmentId: String) {
        updateStatus(
            assignmentId = assignmentId,
            status = "PENDING",
            successMessage = "Assignment moved back to active."
        )
    }

    fun deleteAssignment(assignmentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdating = true,
                errorMessage = null,
                actionMessage = null
            )

            try {
                repository.deleteAssignment(assignmentId)

                val refreshed = repository.getAssignments()

                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    assignments = refreshed,
                    actionMessage = "Assignment deleted.",
                    errorMessage = null
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    errorMessage = t.userMessage("Failed to delete assignment.")
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            actionMessage = null,
            errorMessage = null
        )
    }

    private fun updateStatus(
        assignmentId: String,
        status: String,
        successMessage: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdating = true,
                errorMessage = null,
                actionMessage = null
            )

            try {
                repository.updateAssignmentStatus(
                    assignmentId = assignmentId,
                    status = status
                )

                val refreshed = repository.getAssignments()

                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    assignments = refreshed,
                    actionMessage = successMessage,
                    errorMessage = null
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    errorMessage = t.userMessage("Failed to update assignment.")
                )
            }
        }
    }
}

private fun Throwable.userMessage(defaultMessage: String): String {
    val message = this.message?.trim().orEmpty()
    return if (message.isBlank()) defaultMessage else message
}