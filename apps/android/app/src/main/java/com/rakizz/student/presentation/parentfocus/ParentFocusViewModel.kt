package com.rakizz.student.presentation.parentfocus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.InstalledAppResponseDto
import com.rakizz.student.data.remote.dto.PairCodeLinkRequestDto
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

    val pairCode: String = "",
    val linkedStudentId: String = "",
    val linkedStudentEmail: String = "",

    val startTime: String = "16:00",
    val endTime: String = "18:00",

    val studentApps: List<InstalledAppResponseDto> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val policies: List<PolicyDto> = emptyList(),

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

    fun onPairCodeChange(value: String) {
        _uiState.value = _uiState.value.copy(
            pairCode = value.uppercase(),
            error = null,
            message = null
        )
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

    fun linkStudent() {
        val code = _uiState.value.pairCode.trim().uppercase()

        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Enter student pair code")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null,
                error = null
            )

            try {
                // parent uses RKZ code here, not the student database id
                val result = api.linkStudentByPairCode(
                    PairCodeLinkRequestDto(
                        pairCode = code
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    linkedStudentId = result.student.id,
                    linkedStudentEmail = result.student.email,
                    studentApps = emptyList(),
                    selectedPackages = emptySet(),
                    message = "Student linked"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not link student"
                )
            }
        }
    }

    fun loadStudentApps() {
        val studentId = _uiState.value.linkedStudentId

        if (studentId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Link student first")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null,
                error = null,
                studentApps = emptyList(),
                selectedPackages = emptySet()
            )

            try {
                // now we use the real student id after linking
                val response = api.getStudentInstalledApps(studentId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    studentApps = response.apps,
                    message = "Loaded ${response.apps.size} apps"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not load student apps"
                )
            }
        }
    }

    fun toggleApp(packageName: String) {
        val old = _uiState.value.selectedPackages

        val newSet = if (old.contains(packageName)) {
            old - packageName
        } else {
            old + packageName
        }

        _uiState.value = _uiState.value.copy(
            selectedPackages = newSet
        )
    }

    fun selectAllApps() {
        val allPackages = _uiState.value.studentApps
            .map { it.packageName }
            .toSet()

        _uiState.value = _uiState.value.copy(
            selectedPackages = allPackages
        )
    }

    fun clearSelectedApps() {
        _uiState.value = _uiState.value.copy(
            selectedPackages = emptySet()
        )
    }

    fun loadPolicies() {
        viewModelScope.launch {
            try {
                val rules = api.getPolicies()

                _uiState.value = _uiState.value.copy(
                    policies = rules
                )
            } catch (_: Exception) {
                // keep parent page open even if old rules fail
            }
        }
    }

    fun createFocusRule() {
        val state = _uiState.value

        if (state.linkedStudentId.isBlank()) {
            _uiState.value = state.copy(error = "Link student first")
            return
        }

        if (state.selectedPackages.isEmpty()) {
            _uiState.value = state.copy(error = "Choose at least one app")
            return
        }

        if (state.startTime.isBlank() || state.endTime.isBlank()) {
            _uiState.value = state.copy(error = "Focus time is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                message = null,
                error = null
            )

            try {
                val chosenApps = state.studentApps.filter { app ->
                    state.selectedPackages.contains(app.packageName)
                }

                // saved in backend as one focus-time rule
                val config = buildJsonObject {
                    put("start_time", state.startTime.trim())
                    put("end_time", state.endTime.trim())
                    put("note", "Blocked during focus time")

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
                        studentId = state.linkedStudentId,
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
                    error = e.message ?: "Could not save rule"
                )
            }
        }
    }
}