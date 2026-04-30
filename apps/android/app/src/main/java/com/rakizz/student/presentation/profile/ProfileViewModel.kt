package com.rakizz.student.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.ProfileUpdateRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,

    val email: String = "",
    val role: String = "",
    val userId: String = "",

    val fullName: String = "",
    val school: String = "",
    val gradeLevel: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",

    val showPairCode: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val api: RakizzApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                message = null
            )

            try {
                // get real profile info from backend
                val me = api.getMe()

                _uiState.value = ProfileUiState(
                    isLoading = false,
                    email = me.email,
                    role = me.role,
                    userId = me.id,
                    fullName = me.fullName.orEmpty(),
                    school = me.school.orEmpty(),
                    gradeLevel = me.gradeLevel.orEmpty(),
                    phoneNumber = me.phoneNumber.orEmpty(),
                    profileImageUrl = me.profileImageUrl.orEmpty()
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    error = e.message ?: "Could not load profile"
                )
            }
        }
    }

    fun startEditing() {
        _uiState.value = _uiState.value.copy(
            isEditing = true,
            message = null,
            error = null
        )
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            message = null,
            error = null
        )

        loadProfile()
    }

    fun togglePairCode() {
        _uiState.value = _uiState.value.copy(
            showPairCode = !_uiState.value.showPairCode,
            message = null,
            error = null
        )
    }

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value)
    }

    fun onSchoolChange(value: String) {
        _uiState.value = _uiState.value.copy(school = value)
    }

    fun onGradeChange(value: String) {
        _uiState.value = _uiState.value.copy(gradeLevel = value)
    }

    fun onPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = value)
    }

    fun onImageChange(value: String) {
        _uiState.value = _uiState.value.copy(profileImageUrl = value)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            error = null
        )
    }

    fun saveProfile() {
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.value = state.copy(
                isSaving = true,
                error = null,
                message = null
            )

            try {
                // save profile fields into database
                val updated = api.updateMe(
                    ProfileUpdateRequestDto(
                        fullName = state.fullName.trim(),
                        school = state.school.trim(),
                        gradeLevel = state.gradeLevel.trim(),
                        phoneNumber = state.phoneNumber.trim(),
                        profileImageUrl = state.profileImageUrl.trim()
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isEditing = false,
                    email = updated.email,
                    role = updated.role,
                    userId = updated.id,
                    fullName = updated.fullName.orEmpty(),
                    school = updated.school.orEmpty(),
                    gradeLevel = updated.gradeLevel.orEmpty(),
                    phoneNumber = updated.phoneNumber.orEmpty(),
                    profileImageUrl = updated.profileImageUrl.orEmpty(),
                    message = "Profile saved"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Could not save profile"
                )
            }
        }
    }
}