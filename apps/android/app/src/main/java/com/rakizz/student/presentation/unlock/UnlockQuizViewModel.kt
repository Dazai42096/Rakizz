package com.rakizz.student.presentation.unlock

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.blocking.FocusRuleCache
import com.rakizz.student.data.network.RakizzApi
import com.rakizz.student.data.remote.dto.QuizAttemptRequestDto
import com.rakizz.student.data.remote.dto.QuizAttemptResultDto
import com.rakizz.student.data.remote.dto.QuizDto
import com.rakizz.student.data.remote.dto.QuizGenerateRequestDto
import com.rakizz.student.data.remote.dto.UnlockCheckRequestDto
import com.rakizz.student.data.remote.dto.UnlockGrantRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UnlockQuizUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,

    val packageName: String = "",
    val appName: String = "",
    val quiz: QuizDto? = null,
    val result: QuizAttemptResultDto? = null,

    val selectedAnswers: Map<String, String> = emptyMap(),

    val message: String? = null,
    val error: String? = null,
    val unlockedUntil: String? = null
)

@HiltViewModel
class UnlockQuizViewModel @Inject constructor(
    private val api: RakizzApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnlockQuizUiState())
    val uiState: StateFlow<UnlockQuizUiState> = _uiState.asStateFlow()

    fun startUnlockFlow(
        packageName: String,
        forceBlocked: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = UnlockQuizUiState(
                isLoading = true,
                packageName = packageName
            )

            try {
                // if forceBlocked is true, backend will generate quiz directly
                val check = api.checkBlockedApp(
                    UnlockCheckRequestDto(
                        packageName = packageName,
                        forceBlocked = forceBlocked
                    )
                )

                if (!check.blocked) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = check.message,
                        unlockedUntil = check.unlockedUntil
                    )
                    return@launch
                }

                if (check.materialId.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = check.message
                    )
                    return@launch
                }

                // generate mixed AI quiz from newest material
                val quiz = api.generateQuiz(
                    QuizGenerateRequestDto(
                        materialId = check.materialId,
                        difficulty = "MIXED"
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    appName = check.appName ?: check.packageName,
                    quiz = quiz,
                    message = check.message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Could not start unlock quiz"
                )
            }
        }
    }

    fun selectAnswer(questionId: String, answer: String) {
        val current = _uiState.value.selectedAnswers.toMutableMap()
        current[questionId] = answer

        _uiState.value = _uiState.value.copy(
            selectedAnswers = current
        )
    }

    fun submitQuiz() {
        val state = _uiState.value
        val quiz = state.quiz

        if (quiz == null) {
            _uiState.value = state.copy(error = "Quiz is not ready")
            return
        }

        if (quiz.questions.isEmpty()) {
            _uiState.value = state.copy(error = "No questions found")
            return
        }

        val missing = quiz.questions.firstOrNull { question ->
            state.selectedAnswers[question.id].isNullOrBlank()
        }

        if (missing != null) {
            _uiState.value = state.copy(error = "Answer all questions first")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                error = null,
                message = null
            )

            try {
                val result = api.submitQuizAttempt(
                    id = quiz.id,
                    request = QuizAttemptRequestDto(
                        answers = state.selectedAnswers
                    )
                )

                if (result.passed) {
                    val unlock = api.grantUnlock(
                        UnlockGrantRequestDto(
                            packageName = state.packageName,
                            quizAttemptId = result.id,
                            grantedMinutes = 15
                        )
                    )

                    // local unlock makes accessibility stop blocking it
                    FocusRuleCache.saveLocalUnlock(
                        context = context,
                        packageName = state.packageName,
                        minutes = unlock.grantedMinutes
                    )

                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        result = result,
                        message = "App unlocked for ${unlock.grantedMinutes} minutes",
                        unlockedUntil = unlock.expiresAt
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        result = result,
                        message = "Quiz failed. App is still blocked."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Could not submit unlock quiz"
                )
            }
        }
    }

    fun retryWithNewQuiz() {
        val packageName = _uiState.value.packageName

        if (packageName.isNotBlank()) {
            startUnlockFlow(
                packageName = packageName,
                forceBlocked = true
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}