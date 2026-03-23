package com.rakizz.student.presentation.assignments.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.Assignment
import com.rakizz.student.domain.repository.AssignmentRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignmentsViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Assignment>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Assignment>>> = _uiState.asStateFlow()

    init {
        loadAssignments()
    }

    private fun loadAssignments() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = assignmentRepository.getAssignments()
            result.onSuccess {
                if (it.isEmpty()) _uiState.value = UiState.Empty
                else _uiState.value = UiState.Success(it)
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to load assignments")
            }
        }
    }
}
