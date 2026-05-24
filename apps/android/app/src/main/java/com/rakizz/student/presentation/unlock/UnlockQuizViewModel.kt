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
        if (packageName.isBlank()) {
            _uiState.value = UnlockQuizUiState(
                isLoading = false,
                error = "Blocked app package name is missing"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = UnlockQuizUiState(
                isLoading = true,
                packageName = packageName,
                appName = packageName
            )

            try {
                val check = api.checkBlockedApp(
                    UnlockCheckRequestDto(
                        packageName = packageName,
                        forceBlocked = forceBlocked
                    )
                )

                if (!check.blocked) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appName = check.appName ?: check.packageName,
                        message = check.message,
                        unlockedUntil = check.unlockedUntil
                    )
                    return@launch
                }

                val materialId = check.materialId

                if (materialId.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appName = check.appName ?: check.packageName,
                        error = check.message.ifBlank {
                            "This app is blocked, but no study material was found. Upload material first."
                        }
                    )
                    return@launch
                }

                val quiz = api.generateQuiz(
                    QuizGenerateRequestDto(
                        materialId = materialId,
                        difficulty = "MEDIUM"
                    )
                )

                if (quiz.questions.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appName = check.appName ?: check.packageName,
                        error = "Quiz was generated, but no questions were returned"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    appName = check.appName ?: check.packageName,
                    quiz = quiz,
                    result = null,
                    selectedAnswers = emptyMap(),
                    message = check.message.ifBlank {
                        "This app is blocked. Pass the quiz to unlock it."
                    },
                    error = null,
                    unlockedUntil = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = readableError(
                        fallback = "Could not start unlock quiz",
                        throwable = e
                    )
                )
            }
        }
    }

    fun selectAnswer(
        questionId: String,
        answer: String
    ) {
        val currentAnswers = _uiState.value.selectedAnswers.toMutableMap()
        currentAnswers[questionId] = answer

        _uiState.value = _uiState.value.copy(
            selectedAnswers = currentAnswers,
            error = null
        )
    }

    fun submitQuiz() {
        val state = _uiState.value
        val quiz = state.quiz

        if (quiz == null) {
            _uiState.value = state.copy(
                error = "Quiz is not ready yet"
            )
            return
        }

        if (quiz.questions.isEmpty()) {
            _uiState.value = state.copy(
                error = "No questions found"
            )
            return
        }

        val missingQuestion = quiz.questions.firstOrNull { question ->
            state.selectedAnswers[question.id].isNullOrBlank()
        }

        if (missingQuestion != null) {
            _uiState.value = state.copy(
                error = "Answer all questions first"
            )
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

                    FocusRuleCache.saveLocalUnlock(
                        context = context,
                        packageName = state.packageName,
                        minutes = unlock.grantedMinutes
                    )

                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        result = result,
                        message = "App unlocked for ${unlock.grantedMinutes} minutes",
                        unlockedUntil = unlock.expiresAt,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        result = result,
                        message = "Quiz failed.\nApp is still blocked.",
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = readableError(
                        fallback = "Could not submit unlock quiz",
                        throwable = e
                    )
                )
            }
        }
    }

    fun retryWithNewQuiz() {
        val packageName = _uiState.value.packageName

        if (packageName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Blocked app package name is missing"
            )
            return
        }

        startUnlockFlow(
            packageName = packageName,
            forceBlocked = true
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            error = null
        )
    }

    private fun readableError(
        fallback: String,
        throwable: Exception
    ): String {
        val message = throwable.message.orEmpty()

        return when {
            message.contains("422", ignoreCase = true) -> {
                "$fallback. The quiz request was rejected by the backend."
            }

            message.contains("401", ignoreCase = true) -> {
                "$fallback. Please login again."
            }

            message.contains("403", ignoreCase = true) -> {
                "$fallback. This action is only allowed for a student account."
            }

            message.contains("404", ignoreCase = true) -> {
                "$fallback. Required data was not found."
            }

            message.isNotBlank() -> {
                "$fallback: $message"
            }

            else -> fallback
        }
    }
}