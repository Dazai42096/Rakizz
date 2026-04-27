package com.rakizz.student.presentation.quizzes.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.Material
import com.rakizz.student.domain.repository.MaterialRepository
import com.rakizz.student.domain.repository.QuizRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class QuizSetupViewModel @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _materialState = MutableStateFlow<UiState<Material>>(UiState.Loading)
    val materialState: StateFlow<UiState<Material>> = _materialState.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatedQuizId = MutableStateFlow<String?>(null)
    val generatedQuizId: StateFlow<String?> = _generatedQuizId.asStateFlow()

    private val _generateMessage = MutableStateFlow<String?>(null)
    val generateMessage: StateFlow<String?> = _generateMessage.asStateFlow()

    fun loadMaterial(id: String) {
        viewModelScope.launch {
            _materialState.value = UiState.Loading
            _generateMessage.value = null
            _generatedQuizId.value = null

            materialRepository.getMaterial(id)
                .onSuccess { material ->
                    _materialState.value = UiState.Success(material)
                }
                .onFailure { throwable ->
                    _materialState.value = UiState.Error(
                        throwable.message ?: "Failed to load material"
                    )
                }
        }
    }

    fun generateQuiz(materialId: String) {
        if (materialId.isBlank()) {
            _generateMessage.value = "Missing material. Please go back and select a study material first."
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _generateMessage.value = null
            _generatedQuizId.value = null

            quizRepository.generateQuiz(materialId)
                .onSuccess { quiz ->
                    _generatedQuizId.value = quiz.id
                    _generateMessage.value = "Quiz generated successfully."
                }
                .onFailure { throwable ->
                    _generateMessage.value = mapGenerateFailure(throwable)
                }

            _isGenerating.value = false
        }
    }

    fun clearGeneratedQuiz() {
        _generatedQuizId.value = null
    }

    fun clearGenerateMessage() {
        _generateMessage.value = null
    }

    private fun mapGenerateFailure(throwable: Throwable): String {
        val rawMessage = throwable.message.orEmpty()
        val message = rawMessage.lowercase()

        if (throwable is HttpException) {
            return when (throwable.code()) {
                400, 415, 422 -> "This material could not be read for quiz generation. Try another file or upload a clearer study material."
                404 -> "The selected material was not found. Please return to the library and try again."
                408, 504 -> "Quiz generation timed out. Please try again."
                in 500..599 -> "Quiz generation is temporarily unavailable. Please try again."
                else -> if (rawMessage.isNotBlank()) rawMessage else "Failed to generate quiz."
            }
        }

        return when {
            "timeout" in message -> "Quiz generation timed out. Please try again."
            "unreadable" in message ||
                "invalid format" in message ||
                "unsupported" in message ||
                "parse" in message ||
                "extract" in message ||
                "empty file" in message ||
                "no content" in message -> {
                "This material could not be read for quiz generation. Try another file or upload a clearer study material."
            }
            "not found" in message -> "The selected material was not found. Please return to the library and try again."
            rawMessage.isNotBlank() -> rawMessage
            else -> "Failed to generate quiz."
        }
    }
}