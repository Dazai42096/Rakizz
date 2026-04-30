package com.rakizz.student.presentation.parentfocus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.InstalledAppResponseDto
import com.rakizz.student.data.remote.dto.PolicyCreateRequestDto
import com.rakizz.student.data.remote.dto.PolicyDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

data class ParentFocusUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isParentAccount: Boolean? = null,
    val parentEmail: String = "",
    val policies: List<PolicyDto> = emptyList(),
    val studentId: String = "",
    val startTime: String = "16:00",
    val endTime: String = "18:00",
    val studentApps: List<InstalledAppResponseDto> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
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
        checkAccount()
    }

    fun onStudentIdChange(value: String) {
        _uiState.value = _uiState.value.copy(studentId = value)
    }

    fun onStartTimeChange(value: String) {
        _uiState.value = _uiState.value.copy(startTime = value)
    }

    fun onEndTimeChange(value: String) {
        _uiState.value = _uiState.value.copy(endTime = value)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            message = null,
            error = null
        )
    }

    fun toggleApp(packageName: String) {
        val oldSet = _uiState.value.selectedPackages

        val newSet = if (oldSet.contains(packageName)) {
            oldSet - packageName
        } else {
            oldSet + packageName
        }

        _uiState.value = _uiState.value.copy(
            selectedPackages = newSet
        )
    }

    fun selectAllApps() {
        val allPackages = _uiState.value.studentApps.map { it.packageName }.toSet()

        _uiState.value = _uiState.value.copy(
            selectedPackages = allPackages
        )
    }

    fun clearSelectedApps() {
        _uiState.value = _uiState.value.copy(
            selectedPackages = emptySet()
        )
    }

    fun checkAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                message = null
            )

            try {
                val me = api.getMe()
                val isParent = me.role == "parent"

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isParentAccount = isParent,
                    parentEmail = me.email
                )

                if (isParent) {
                    loadPolicies()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Only parent accounts can create focus rules"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isParentAccount = false,
                    error = e.message ?: "Could not check account"
                )
            }
        }
    }

    fun loadPolicies() {
        viewModelScope.launch {
            try {
                // parent can see the rules they created
                val rules = api.getPolicies()

                _uiState.value = _uiState.value.copy(
                    policies = rules
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Could not load rules"
                )
            }
        }
    }

    fun loadStudentApps() {
        val state = _uiState.value

        if (state.isParentAccount != true) {
            _uiState.value = state.copy(error = "Only parent accounts can load student apps")
            return
        }

        if (state.studentId.isBlank()) {
            _uiState.value = state.copy(error = "Student id is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                message = null,
                studentApps = emptyList(),
                selectedPackages = emptySet()
            )

            try {
                // this reads the app list that the student phone synced
                val response = api.getStudentInstalledApps(state.studentId.trim())

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    studentApps = response.apps,
                    message = "Loaded ${response.apps.size} apps from student phone"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not load student apps"
                )
            }
        }
    }

    fun createFocusRule() {
        val state = _uiState.value

        if (state.isParentAccount != true) {
            _uiState.value = state.copy(error = "Only parent accounts can save focus rules")
            return
        }

        if (state.studentId.isBlank()) {
            _uiState.value = state.copy(error = "Student id is required")
            return
        }

        if (state.startTime.isBlank() || state.endTime.isBlank()) {
            _uiState.value = state.copy(error = "Focus time is required")
            return
        }

        if (state.selectedPackages.isEmpty()) {
            _uiState.value = state.copy(error = "Choose at least one app")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                error = null,
                message = null
            )

            try {
                val chosenApps = state.studentApps.filter { app ->
                    state.selectedPackages.contains(app.packageName)
                }

                // this json is saved in backend
                // student app will read it later
                val config = buildJsonObject {
                    put("start_time", state.startTime.trim())
                    put("end_time", state.endTime.trim())
                    put("note", "Apps blocked by parent during focus time")

                    putJsonArray("blocked_apps") {
                        chosenApps.forEach { app ->
                            add(
                                buildJsonObject {
                                    put("package_name", app.packageName)
                                    put("app_name", app.appName)
                                }
                            )
                        }
                    }
                }

                api.createPolicy(
                    PolicyCreateRequestDto(
                        studentId = state.studentId.trim(),
                        ruleType = "time_window",
                        configJson = config
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    message = "Focus rule saved"
                )

                loadPolicies()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Could not save focus rule"
                )
            }
        }
    }
}