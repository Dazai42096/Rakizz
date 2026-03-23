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

    init {
        loadMaterials()
    }

    fun loadMaterials() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = materialRepository.getMaterials()
            result.onSuccess {
                if (it.isEmpty()) _uiState.value = UiState.Empty
                else _uiState.value = UiState.Success(it)
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to load materials")
            }
        }
    }
}
