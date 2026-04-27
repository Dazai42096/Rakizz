package com.rakizz.student.presentation.quizzes.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.domain.repository.QuizRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizDetailViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Quiz>>(UiState.Loading)
    val uiState: StateFlow<UiState<Quiz>> = _uiState.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<String, String>>(emptyMap())
    val selectedAnswers: StateFlow<Map<String, String>> = _selectedAnswers.asStateFlow()

    private val _attemptResult = MutableStateFlow<Quiz?>(null)
    val attemptResult: StateFlow<Quiz?> = _attemptResult.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun loadQuiz(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _selectedAnswers.value = emptyMap()
            _attemptResult.value = null

            val result = quizRepository.getQuiz(id)
            result.onSuccess {
                _uiState.value = UiState.Success(it)
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to load quiz")
            }
        }
    }

    fun selectAnswer(questionId: String, answer: String) {
        if (_attemptResult.value != null) return

        _selectedAnswers.update { current ->
            current + (questionId to answer)
        }
    }

    fun submitQuiz() {
        val quiz = (_uiState.value as? UiState.Success)?.data ?: run {
            _actionMessage.value = "Quiz is not ready yet"
            return
        }

        if (quiz.questions.isEmpty()) {
            _actionMessage.value = "No quiz questions were returned"
            return
        }

        val unanswered = quiz.questions.firstOrNull { question ->
            _selectedAnswers.value[question.id].isNullOrBlank()
        }

        if (unanswered != null) {
            _actionMessage.value = "Answer all questions before submitting"
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true

            val result = quizRepository.submitQuizAttempt(
                quizId = quiz.id,
                answers = _selectedAnswers.value
            )

            result.onSuccess {
                _attemptResult.value = it
                _actionMessage.value = "Quiz submitted successfully"
            }.onFailure {
                _actionMessage.value = it.message ?: "Failed to submit quiz"
            }

            _isSubmitting.value = false
        }
    }

    fun retryQuiz() {
        _attemptResult.value = null
        _selectedAnswers.value = emptyMap()
        _actionMessage.value = null
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}