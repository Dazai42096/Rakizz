package com.rakizz.student.presentation.materials.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.Material
import com.rakizz.student.domain.repository.MaterialRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialsViewModel @Inject constructor(
    private val materialRepository: MaterialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Material>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Material>>> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        loadMaterials()
    }

    fun loadMaterials() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = materialRepository.getMaterials()
            result.onSuccess { materials ->
                _uiState.value = if (materials.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(materials)
                }
            }.onFailure { error ->
                _uiState.value = UiState.Error(error.message ?: "Failed to load materials")
            }
        }
    }

    fun uploadMaterialFile(
        title: String?,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ) {
        if (fileName.isBlank()) {
            _actionMessage.value = "Invalid file name"
            return
        }

        if (bytes.isEmpty()) {
            _actionMessage.value = "Selected file is empty"
            return
        }

        val previousState = _uiState.value

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = materialRepository.uploadMaterial(
                title = title?.trim(),
                fileName = fileName,
                mimeType = mimeType,
                bytes = bytes
            )

            result.onSuccess {
                _actionMessage.value = "Material uploaded successfully"
                loadMaterials()
            }.onFailure { error ->
                _uiState.value = previousState
                _actionMessage.value = error.message ?: "Failed to upload material"
            }
        }
    }

    fun addLinkMaterial(
        title: String,
        sourceUrl: String
    ) {
        val cleanTitle = title.trim()
        val cleanUrl = sourceUrl.trim()

        when {
            cleanTitle.isBlank() -> {
                _actionMessage.value = "Title is required"
                return
            }

            cleanUrl.isBlank() -> {
                _actionMessage.value = "Link is required"
                return
            }

            !cleanUrl.startsWith("http://", ignoreCase = true) &&
                !cleanUrl.startsWith("https://", ignoreCase = true) -> {
                _actionMessage.value = "Link must start with http:// or https://"
                return
            }
        }

        val previousState = _uiState.value

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = materialRepository.createMaterialFromUrl(
                title = cleanTitle,
                sourceUrl = cleanUrl
            )

            result.onSuccess {
                _actionMessage.value = "Material added successfully"
                loadMaterials()
            }.onFailure { error ->
                _uiState.value = previousState
                _actionMessage.value = error.message ?: "Failed to add material"
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}