package com.rakizz.student.presentation.materials.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.Material
import com.rakizz.student.domain.model.MaterialDownload
import com.rakizz.student.domain.repository.MaterialRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    fun loadMaterial(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = materialRepository.getMaterial(id)
            result.onSuccess {
                _uiState.value = UiState.Success(it)
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to load material")
            }
        }
    }

    fun previewMaterial() {
        performMaterialDownload(openAfterDownload = true)
    }

    fun downloadMaterial() {
        performMaterialDownload(openAfterDownload = false)
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun clearOpenFileEvent() {
        _openFileEvent.value = null
    }

    private fun performMaterialDownload(openAfterDownload: Boolean) {
        val material = (_uiState.value as? UiState.Success)?.data ?: run {
            _actionMessage.value = "Material is not ready yet"
            return
        }

        viewModelScope.launch {
            _isWorking.value = true

            val result = materialRepository.downloadMaterial(material.id)
            result.onSuccess { downloaded ->
                if (openAfterDownload) {
                    _openFileEvent.value = downloaded
                    _actionMessage.value = "Material ready to open"
                } else {
                    _actionMessage.value = "Material downloaded successfully"
                }
            }.onFailure { error ->
                _actionMessage.value = error.message ?: "Failed to download material"
            }

            _isWorking.value = false
        }
    }
}