package com.rakizz.student.presentation.materials.detail

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
class MaterialDetailViewModel @Inject constructor(
    private val materialRepository: MaterialRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Material>>(UiState.Loading)
    val uiState: StateFlow<UiState<Material>> = _uiState.asStateFlow()

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
}
