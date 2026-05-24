package com.rakizz.student.presentation.parentfocus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.CurrentUserDto
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

    val linkedStudents: List<CurrentUserDto> = emptyList(),
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
        loadParentDashboard()
    }

    fun loadParentDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                message = null
            )

            try {
                val students = api.getLinkedStudents()
                val policies = runCatching {
                    api.getPolicies()
                }.getOrDefault(emptyList())

                val selectedStudent = students.firstOrNull()
                val apps = if (selectedStudent != null) {
                    runCatching {
                        api.getStudentInstalledApps(selectedStudent.id).apps
                    }.getOrDefault(emptyList())
                } else {
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    linkedStudents = students,
                    linkedStudentId = selectedStudent?.id.orEmpty(),
                    linkedStudentEmail = selectedStudent?.email.orEmpty(),
                    studentApps = apps,
                    selectedPackages = emptySet(),
                    policies = policies
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not load parent focus data"
                )
            }
        }
    }

    fun onPairCodeChange(value: String) {
        _uiState.value = _uiState.value.copy(
            pairCode = value.uppercase(),
            error = null,
            message = null
        )
    }

    fun onStartTimeChange(value: String) {
        _uiState.value = _uiState.value.copy(
            startTime = value,
            error = null,
            message = null
        )
    }

    fun onEndTimeChange(value: String) {
        _uiState.value = _uiState.value.copy(
            endTime = value,
            error = null,
            message = null
        )
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
            _uiState.value = _uiState.value.copy(
                error = "Enter student pair code"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null,
                error = null
            )

            try {
                val result = api.linkStudentByPairCode(
                    PairCodeLinkRequestDto(
                        pairCode = code
                    )
                )

                val apps = runCatching {
                    api.getStudentInstalledApps(result.student.id).apps
                }.getOrDefault(emptyList())

                val updatedStudents = buildList {
                    addAll(_uiState.value.linkedStudents)
                    if (_uiState.value.linkedStudents.none { it.id == result.student.id }) {
                        add(result.student)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pairCode = "",
                    linkedStudents = updatedStudents,
                    linkedStudentId = result.student.id,
                    linkedStudentEmail = result.student.email,
                    studentApps = apps,
                    selectedPackages = emptySet(),
                    message = "Student linked successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not link student"
                )
            }
        }
    }

    fun selectStudent(student: CurrentUserDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                linkedStudentId = student.id,
                linkedStudentEmail = student.email,
                selectedPackages = emptySet(),
                studentApps = emptyList(),
                error = null,
                message = null
            )

            try {
                val apps = api.getStudentInstalledApps(student.id).apps

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    studentApps = apps,
                    message = "Loaded ${apps.size} apps"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not load student apps"
                )
            }
        }
    }

    fun loadStudentApps() {
        val studentId = _uiState.value.linkedStudentId

        if (studentId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Link student first"
            )
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
        val oldSelected = _uiState.value.selectedPackages

        val newSelected = if (oldSelected.contains(packageName)) {
            oldSelected - packageName
        } else {
            oldSelected + packageName
        }

        _uiState.value = _uiState.value.copy(
            selectedPackages = newSelected,
            error = null,
            message = null
        )
    }

    fun selectAllApps() {
        val allPackages = _uiState.value.studentApps
            .map { it.packageName }
            .toSet()

        _uiState.value = _uiState.value.copy(
            selectedPackages = allPackages,
            error = null,
            message = null
        )
    }

    fun clearSelectedApps() {
        _uiState.value = _uiState.value.copy(
            selectedPackages = emptySet(),
            error = null,
            message = null
        )
    }

    fun loadPolicies() {
        viewModelScope.launch {
            try {
                val rules = api.getPolicies()

                _uiState.value = _uiState.value.copy(
                    policies = rules
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Could not load focus rules"
                )
            }
        }
    }

    fun createFocusRule() {
        val state = _uiState.value

        if (state.linkedStudentId.isBlank()) {
            _uiState.value = state.copy(
                error = "Link student first"
            )
            return
        }

        if (state.selectedPackages.isEmpty()) {
            _uiState.value = state.copy(
                error = "Choose at least one app"
            )
            return
        }

        if (state.startTime.isBlank() || state.endTime.isBlank()) {
            _uiState.value = state.copy(
                error = "Focus time is required"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                message = null,
                error = null
            )

            try {
                val chosenApps = state.selectedPackages.map { packageName ->
                    val app = state.studentApps.firstOrNull {
                        it.packageName == packageName
                    }

                    packageName to (app?.appName ?: packageName)
                }

                val config = buildJsonObject {
                    put("start_time", state.startTime.trim())
                    put("end_time", state.endTime.trim())
                    put("note", "Blocked during focus time")

                    putJsonArray("blocked_apps") {
                        chosenApps.forEach { app ->
                            add(
                                buildJsonObject {
                                    put("package_name", app.first)
                                    put("app_name", app.second)
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

                val updatedPolicies = runCatching {
                    api.getPolicies()
                }.getOrDefault(state.policies)

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    policies = updatedPolicies,
                    message = "Focus rule saved"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Could not save rule"
                )
            }
        }
    }
}