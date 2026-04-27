package com.rakizz.student.presentation.focus

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.FocusPolicy
import com.rakizz.student.domain.model.UsageSummary
import com.rakizz.student.domain.repository.FocusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FocusUiState(
    val isLoading: Boolean = false,
    val policies: List<FocusPolicy> = emptyList(),
    val usageSummary: UsageSummary? = null,
    val usageAccessGranted: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val focusRepository: FocusRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FocusUiState(
            isLoading = true,
            usageAccessGranted = hasUsageAccess()
        )
    )
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    init {
        loadFocusData()
    }

    fun loadFocusData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                actionMessage = null,
                usageAccessGranted = hasUsageAccess()
            )

            val policiesResult = focusRepository.getPolicies()
            val summaryResult = focusRepository.getUsageSummary(days = 7)

            val policies = policiesResult.getOrElse {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Could not load focus rules"
                )
                return@launch
            }

            val summary = summaryResult.getOrElse {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    policies = policies,
                    errorMessage = it.message ?: "Could not load usage summary"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                policies = policies,
                usageSummary = summary,
                errorMessage = null
            )
        }
    }

    fun syncDemoUsage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                actionMessage = null,
                errorMessage = null
            )

            val result = focusRepository.syncDemoUsage()

            result.onSuccess { savedCount ->
                _uiState.value = _uiState.value.copy(
                    actionMessage = "Usage synced: $savedCount records saved"
                )
                loadFocusData()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not sync usage"
                )
            }
        }
    }

    fun refreshPermissionState() {
        _uiState.value = _uiState.value.copy(
            usageAccessGranted = hasUsageAccess()
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            actionMessage = null,
            errorMessage = null
        )
    }

    private fun hasUsageAccess(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            } else {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }

            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            false
        }
    }
}