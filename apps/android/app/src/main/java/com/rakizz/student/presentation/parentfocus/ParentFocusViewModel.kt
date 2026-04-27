package com.rakizz.student.presentation.parentfocus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.PolicyCreateRequestDto
import com.rakizz.student.data.remote.dto.PolicyDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class ParentFocusUiState(
    val isLoading: Boolean = false,
    val policies: List<PolicyDto> = emptyList(),
    val studentId: String = "",
    val packageName: String = "com.instagram.android",
    val dailyLimitMinutes: String = "30",
    val note: String = "parent rule from app",
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class ParentFocusViewModel @Inject constructor(
    private val api: RakizzApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentFocusUiState())
    val uiState: StateFlow<ParentFocusUiState> = _uiState.asStateFlow()

    init {
        loadPolicies()
    }

    fun onStudentIdChange(value: String) {
        _uiState.value = _uiState.value.copy(studentId = value)
    }

    fun onPackageNameChange(value: String) {
        _uiState.value = _uiState.value.copy(packageName = value)
    }

    fun onDailyLimitChange(value: String) {
        _uiState.value = _uiState.value.copy(dailyLimitMinutes = value)
    }

    fun onNoteChange(value: String) {
        _uiState.value = _uiState.value.copy(note = value)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            message = null,
            error = null
        )
    }

    fun loadPolicies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null,
                error = null
            )

            try {
                // get the rules that this parent already made
                val rules = api.getPolicies()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    policies = rules
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not load rules"
                )
            }
        }
    }

    fun createRule() {
        val state = _uiState.value
        val limit = state.dailyLimitMinutes.toIntOrNull()

        if (state.studentId.isBlank()) {
            _uiState.value = state.copy(error = "Student id is required")
            return
        }

        if (state.packageName.isBlank()) {
            _uiState.value = state.copy(error = "Package name is required")
            return
        }

        if (limit == null || limit <= 0) {
            _uiState.value = state.copy(error = "Limit must be bigger than 0")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null,
                error = null
            )

            try {
                // this json is saved with the policy in backend
                val config = buildJsonObject {
                    put("package_name", state.packageName.trim())
                    put("daily_limit_minutes", limit)
                    put("note", state.note.trim())
                }

                api.createPolicy(
                    PolicyCreateRequestDto(
                        studentId = state.studentId.trim(),
                        ruleType = "daily_limit",
                        configJson = config
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Rule created"
                )

                loadPolicies()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not create rule"
                )
            }
        }
    }
}