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
    private val _uiState = MutableStateFlow<UiState<List<Quiz>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Quiz>>> = _uiState.asStateFlow()

    init {
        loadQuizzes()
    }

    fun loadQuizzes() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = quizRepository.getQuizzes()
            result.onSuccess {
                if (it.isEmpty()) _uiState.value = UiState.Empty
                else _uiState.value = UiState.Success(it)
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to load quizzes")
            }
        }
    }
    
    fun generateQuiz(materialId: String) {
        viewModelScope.launch {
            quizRepository.generateQuiz(materialId)
            loadQuizzes() // Reload list
        }
    }
}
