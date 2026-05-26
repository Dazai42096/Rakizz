package com.rakizz.student.presentation.quizzes.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.domain.repository.QuizRepository
import com.rakizz.student.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

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

    private val _isGeneratingAnother = MutableStateFlow(false)
    val isGeneratingAnother: StateFlow<Boolean> = _isGeneratingAnother.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private var currentMaterialId: String = ""

    fun loadQuiz(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _selectedAnswers.value = emptyMap()
            _attemptResult.value = null
            _actionMessage.value = null
            _isSubmitting.value = false
            _isGeneratingAnother.value = false

            val result = quizRepository.getQuiz(id)

            result.onSuccess { quiz ->
                currentMaterialId = quiz.materialId
                _uiState.value = UiState.Success(quiz)
            }.onFailure { error ->
                _uiState.value = UiState.Error(
                    error.message ?: "Failed to load quiz"
                )
            }
        }
    }

    fun selectAnswer(questionId: String, answer: String) {
        if (_attemptResult.value != null) {
            return
        }

        if (_isSubmitting.value || _isGeneratingAnother.value) {
            return
        }

        _selectedAnswers.update { current ->
            current + (questionId to answer)
        }
    }

    fun submitQuiz() {
        val quiz = (_uiState.value as? UiState.Success)?.data

        if (quiz == null) {
            _actionMessage.value = "Quiz is not ready yet."
            return
        }

        if (quiz.questions.isEmpty()) {
            _actionMessage.value = "No quiz questions were returned."
            return
        }

        val unansweredQuestion = quiz.questions.firstOrNull { question ->
            _selectedAnswers.value[question.id].isNullOrBlank()
        }

        if (unansweredQuestion != null) {
            _actionMessage.value = "Answer all questions before submitting."
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _actionMessage.value = null

            val result = quizRepository.submitQuizAttempt(
                quizId = quiz.id,
                answers = _selectedAnswers.value
            )

            result.onSuccess { gradedQuiz ->
                _attemptResult.value = gradedQuiz
                _actionMessage.value = "Quiz submitted successfully."
            }.onFailure { error ->
                _actionMessage.value = mapQuizError(error)
            }

            _isSubmitting.value = false
        }
    }

    fun retryQuiz() {
        _attemptResult.value = null
        _selectedAnswers.value = emptyMap()
        _actionMessage.value = null
    }

    fun generateAnotherQuiz() {
        val visibleQuiz = (_uiState.value as? UiState.Success)?.data

        if (visibleQuiz == null) {
            _actionMessage.value = "Quiz is not ready yet."
            return
        }

        val materialId = visibleQuiz.materialId.ifBlank {
            currentMaterialId
        }

        if (materialId.isBlank()) {
            _actionMessage.value = "This quiz is missing its material link. Go back to Materials and generate a new quiz from the material."
            return
        }

        if (_isGeneratingAnother.value || _isSubmitting.value) {
            return
        }

        viewModelScope.launch {
            _isGeneratingAnother.value = true
            _actionMessage.value = "Generating another quiz from the same material..."

            val result = quizRepository.generateQuiz(materialId)

            result.onSuccess { newQuiz ->
                currentMaterialId = newQuiz.materialId.ifBlank {
                    materialId
                }

                _uiState.value = UiState.Success(
                    newQuiz.copy(
                        materialId = currentMaterialId
                    )
                )

                _selectedAnswers.value = emptyMap()
                _attemptResult.value = null
                _actionMessage.value = "New quiz generated."
            }.onFailure { error ->
                _actionMessage.value = mapQuizError(error)
            }

            _isGeneratingAnother.value = false
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    private fun mapQuizError(error: Throwable): String {
        val rawMessage = error.message.orEmpty()
        val cleanMessage = rawMessage.lowercase()

        if (error is HttpException) {
            return when (error.code()) {
                400, 415, 422 -> {
                    "This material could not generate another quality quiz. Try uploading a clearer or longer material."
                }

                401, 403 -> {
                    "Your login session may have expired. Please login again."
                }

                404 -> {
                    "The selected quiz or material was not found."
                }

                408, 504 -> {
                    "Quiz generation timed out. Please try again."
                }

                in 500..599 -> {
                    "Rakizz quiz service is temporarily unavailable. Please try again."
                }

                else -> {
                    rawMessage.ifBlank { "Quiz action failed." }
                }
            }
        }

        return when {
            "timeout" in cleanMessage -> {
                "Quiz generation timed out. Please try again."
            }

            "network" in cleanMessage ||
                "failed to connect" in cleanMessage ||
                "unable to resolve host" in cleanMessage -> {
                "Unable to reach Rakizz server. Check your connection and try again."
            }

            "not found" in cleanMessage -> {
                "The selected quiz or material was not found."
            }

            rawMessage.isNotBlank() -> {
                rawMessage
            }

            else -> {
                "Quiz action failed."
            }
        }
    }
}