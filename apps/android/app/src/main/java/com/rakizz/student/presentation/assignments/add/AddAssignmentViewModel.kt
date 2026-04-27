package com.rakizz.student.presentation.assignments.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddAssignmentUiState(
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val dueTime: String = "",
    val isSaving: Boolean = false,
    val inputError: String? = null,
    val submitError: String? = null
)

sealed interface AddAssignmentEvent {
    data class Saved(
        val assignmentId: String,
        val title: String,
        val description: String,
        val dueAtRaw: String,
        val reminderWarning: String?
    ) : AddAssignmentEvent
}

@HiltViewModel
class AddAssignmentViewModel @Inject constructor(
    private val repository: AssignmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAssignmentUiState())
    val uiState: StateFlow<AddAssignmentUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddAssignmentEvent>()
    val events: SharedFlow<AddAssignmentEvent> = _events.asSharedFlow()

    fun onTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            title = value,
            inputError = null,
            submitError = null
        )
    }

    fun onDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            description = value,
            inputError = null,
            submitError = null
        )
    }

    fun onDueDateChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            dueDate = value,
            inputError = null,
            submitError = null
        )
    }

    fun onDueTimeChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            dueTime = value,
            inputError = null,
            submitError = null
        )
    }

    fun saveAssignment() {
        val state = _uiState.value

        if (state.isSaving) return

        val title = state.title.trim()
        if (title.isBlank()) {
            _uiState.value = state.copy(inputError = "Title is required.")
            return
        }

        val description = state.description.trim()
        if (description.isBlank()) {
            _uiState.value = state.copy(inputError = "Description is required.")
            return
        }

        val dueAt = buildDueAtIso(
            dueDate = state.dueDate.trim(),
            dueTime = state.dueTime.trim()
        )

        if (dueAt == null) {
            _uiState.value = state.copy(
                inputError = "Enter a valid due date and time."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                inputError = null,
                submitError = null
            )

            try {
                val created = repository.createAssignment(
                    title = title,
                    description = description,
                    dueAt = dueAt
                )

                _uiState.value = AddAssignmentUiState()
                _events.emit(
                    AddAssignmentEvent.Saved(
                        assignmentId = created.id,
                        title = created.title,
                        description = created.description,
                        dueAtRaw = created.dueAtRaw,
                        reminderWarning = created.reminderWarning
                    )
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    submitError = t.userMessage("Failed to save assignment.")
                )
            }
        }
    }

    private fun buildDueAtIso(
        dueDate: String,
        dueTime: String
    ): String? {
        val date = try {
            LocalDate.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: DateTimeParseException) {
            return null
        }

        val time = try {
            LocalTime.parse(dueTime, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (_: DateTimeParseException) {
            return null
        }

        return LocalDateTime.of(date, time)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

private fun Throwable.userMessage(defaultMessage: String): String {
    val message = this.message?.trim().orEmpty()
    return if (message.isBlank()) defaultMessage else message
}