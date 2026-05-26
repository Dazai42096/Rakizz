package com.rakizz.student.presentation.materials.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.Material
import com.rakizz.student.domain.model.MaterialDownload
import com.rakizz.student.domain.repository.MaterialRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MaterialDetailViewModel @Inject constructor(
    private val materialRepository: MaterialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Material>>(UiState.Loading)
    val uiState: StateFlow<UiState<Material>> = _uiState.asStateFlow()

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _openFileEvent = MutableStateFlow<MaterialDownload?>(null)
    val openFileEvent: StateFlow<MaterialDownload?> = _openFileEvent.asStateFlow()

    private val _deleteCompleted = MutableStateFlow(false)
    val deleteCompleted: StateFlow<Boolean> = _deleteCompleted.asStateFlow()

    fun loadMaterial(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = materialRepository.getMaterial(id)

            result.onSuccess { material ->
                _uiState.value = UiState.Success(material)
            }.onFailure { error ->
                _uiState.value = UiState.Error(
                    error.message ?: "Failed to load material"
                )
            }
        }
    }

    fun previewMaterial() {
        downloadMaterialFile(openAfterDownload = true)
    }

    fun downloadMaterial() {
        downloadMaterialFile(openAfterDownload = false)
    }

    fun deleteMaterial() {
        val material = (_uiState.value as? UiState.Success)?.data

        if (material == null) {
            _actionMessage.value = "Material is not ready yet."
            return
        }

        viewModelScope.launch {
            _isWorking.value = true

            val result = materialRepository.deleteMaterial(material.id)

            result.onSuccess {
                _actionMessage.value = "Material deleted successfully."
                _deleteCompleted.value = true
            }.onFailure { error ->
                _actionMessage.value = error.message ?: "Failed to delete material"
            }

            _isWorking.value = false
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun clearOpenFileEvent() {
        _openFileEvent.value = null
    }

    fun clearDeleteCompleted() {
        _deleteCompleted.value = false
    }

    private fun downloadMaterialFile(openAfterDownload: Boolean) {
        val material = (_uiState.value as? UiState.Success)?.data

        if (material == null) {
            _actionMessage.value = "Material is not ready yet."
            return
        }

        viewModelScope.launch {
            _isWorking.value = true

            val result = materialRepository.downloadMaterial(material.id)

            result.onSuccess { downloaded ->
                if (openAfterDownload) {
                    _openFileEvent.value = downloaded
                    _actionMessage.value = "Material ready to open."
                } else {
                    _actionMessage.value = "Material downloaded successfully."
                }
            }.onFailure { error ->
                _actionMessage.value = error.message ?: "Failed to download material"
            }

            _isWorking.value = false
        }
    }
}