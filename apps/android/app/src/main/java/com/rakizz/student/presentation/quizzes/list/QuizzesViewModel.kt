package com.rakizz.student.presentation.quizzes.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.domain.repository.QuizRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizzesViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Quiz>>>(UiState.Empty)
    val uiState: StateFlow<UiState<List<Quiz>>> = _uiState.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generateMessage = MutableStateFlow<String?>(null)
    val generateMessage: StateFlow<String?> = _generateMessage.asStateFlow()

    private val _generatedQuizId = MutableStateFlow<String?>(null)
    val generatedQuizId: StateFlow<String?> = _generatedQuizId.asStateFlow()

    fun generateQuiz(materialId: String) {
        if (materialId.isBlank()) {
            _generateMessage.value = "Enter a material ID first"
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _generateMessage.value = null
            _generatedQuizId.value = null

            val result = quizRepository.generateQuiz(materialId)

            result.onSuccess { quiz ->
                _generateMessage.value = "Quiz generated successfully"
                _generatedQuizId.value = quiz.id
            }.onFailure {
                _generateMessage.value = it.message ?: "Failed to generate quiz"
            }

            _isGenerating.value = false
        }
    }

    fun clearGenerateMessage() {
        _generateMessage.value = null
    }
}