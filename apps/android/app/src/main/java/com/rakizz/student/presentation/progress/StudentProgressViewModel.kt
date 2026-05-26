package com.rakizz.student.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakizz.student.domain.model.Assignment
import com.rakizz.student.domain.model.FocusPolicy
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.domain.repository.AssignmentRepository
import com.rakizz.student.domain.repository.FocusRepository
import com.rakizz.student.domain.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StudentProgressViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val assignmentRepository: AssignmentRepository,
    private val focusRepository: FocusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProgressUiState())
    val uiState: StateFlow<StudentProgressUiState> = _uiState.asStateFlow()

    fun loadProgress() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                actionMessage = null
            )

            try {
                val quizzesResult = quizRepository.getQuizzes()
                val policiesResult = focusRepository.getPolicies()
                val assignments = assignmentRepository.getAssignments()

                val quizzes = quizzesResult.getOrElse {
                    emptyList()
                }

                val policies = policiesResult.getOrElse {
                    emptyList()
                }

                val summary = buildProgressSummary(
                    quizzes = quizzes,
                    assignments = assignments,
                    policies = policies
                )

                _uiState.value = StudentProgressUiState(
                    isLoading = false,
                    quizzes = quizzes,
                    assignments = assignments,
                    policies = policies,
                    summary = summary,
                    errorMessage = null,
                    actionMessage = "Progress loaded from backend."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Could not load progress data"
                )
            }
        }
    }

    fun refreshProgress() {
        loadProgress()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            actionMessage = null,
            errorMessage = null
        )
    }

    private fun buildProgressSummary(
        quizzes: List<Quiz>,
        assignments: List<Assignment>,
        policies: List<FocusPolicy>
    ): StudentProgressSummary {
        val attemptedQuizzes = quizzes.filter { quiz ->
            quiz.score != null
        }

        val quizAverage = if (attemptedQuizzes.isNotEmpty()) {
            attemptedQuizzes.mapNotNull { it.score }.average().roundToInt()
        } else {
            null
        }

        val passedQuizzes = attemptedQuizzes.count { quiz ->
            (quiz.score ?: 0) >= 70 || quiz.passed == true
        }

        val completedAssignments = assignments.count { assignment ->
            assignment.isCompleted
        }

        val assignmentCompletion = if (assignments.isNotEmpty()) {
            ((completedAssignments.toDouble() / assignments.size.toDouble()) * 100.0).roundToInt()
        } else {
            null
        }

        val protectedApps = policies
            .flatMap { policy ->
                if (policy.blockedApps.isNotEmpty()) {
                    policy.blockedApps
                } else {
                    listOf(policy.packageName)
                }
            }
            .map { it.trim() }
            .filter { it.isNotBlank() && it.lowercase() != "focus rule" }
            .distinct()

        val availableScores = listOfNotNull(
            quizAverage,
            assignmentCompletion
        )

        val overallScore = if (availableScores.isNotEmpty()) {
            availableScores.average().roundToInt()
        } else {
            null
        }

        return StudentProgressSummary(
            overallScore = overallScore,
            quizAverage = quizAverage,
            attemptedQuizCount = attemptedQuizzes.size,
            totalQuizCount = quizzes.size,
            passedQuizCount = passedQuizzes,
            assignmentCompletion = assignmentCompletion,
            completedAssignmentCount = completedAssignments,
            totalAssignmentCount = assignments.size,
            focusRuleCount = policies.size,
            protectedAppCount = protectedApps.size,
            protectedApps = protectedApps
        )
    }
}

data class StudentProgressUiState(
    val isLoading: Boolean = false,
    val quizzes: List<Quiz> = emptyList(),
    val assignments: List<Assignment> = emptyList(),
    val policies: List<FocusPolicy> = emptyList(),
    val summary: StudentProgressSummary = StudentProgressSummary(),
    val actionMessage: String? = null,
    val errorMessage: String? = null
)

data class StudentProgressSummary(
    val overallScore: Int? = null,
    val quizAverage: Int? = null,
    val attemptedQuizCount: Int = 0,
    val totalQuizCount: Int = 0,
    val passedQuizCount: Int = 0,
    val assignmentCompletion: Int? = null,
    val completedAssignmentCount: Int = 0,
    val totalAssignmentCount: Int = 0,
    val focusRuleCount: Int = 0,
    val protectedAppCount: Int = 0,
    val protectedApps: List<String> = emptyList()
)