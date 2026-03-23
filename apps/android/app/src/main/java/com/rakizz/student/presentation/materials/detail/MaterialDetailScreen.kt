package com.rakizz.student.presentation.materials.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Material
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.ErrorView
import com.rakizz.student.presentation.common.components.LoadingView

@Composable
fun MaterialDetailScreen(
    materialId: String,
    viewModel: MaterialDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(materialId) {
        viewModel.loadMaterial(materialId)
    }

    val state by viewModel.uiState.collectAsState()

    when (state) {
        is UiState.Loading -> LoadingView()
        is UiState.Error -> ErrorView(message = (state as UiState.Error).message)
        is UiState.Success -> {
            val material = (state as UiState.Success<Material>).data
            Column {
                Text(text = material.title)
                Text(text = material.description)
            }
        }
        else -> {}
    }
}
