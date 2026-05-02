package com.rakizz.student.presentation.unlock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.data.remote.dto.QuizAttemptResultDto
import com.rakizz.student.data.remote.dto.QuizDto
import com.rakizz.student.data.remote.dto.QuizQuestionDto
import com.rakizz.student.data.remote.dto.QuizReviewItemDto

private val ScreenTop = Color(0xFF03112A)
private val ScreenBottom = Color(0xFF000000)
private val WhiteText = Color(0xFFF7F8FA)
private val SecondaryText = Color(0xFF9EA6B8)
private val PrimaryBlue = Color(0xFF2457D6)
private val CardSurface = Color(0xFF171717)
private val CardBorder = Color(0xFF2A2F3A)
private val Green = Color(0xFF30D158)
private val Orange = Color(0xFFFF9F0A)
private val Red = Color(0xFFFF453A)

@Composable
fun UnlockQuizScreen(
    packageName: String,
    forceUnlock: Boolean,
    onBackClick: () -> Unit,
    viewModel: UnlockQuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(packageName, forceUnlock) {
        viewModel.startUnlockFlow(
            packageName = packageName,
            forceBlocked = forceUnlock
        )
    }

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ScreenTop, ScreenBottom)
                    )
                )
                .padding(paddingValues)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TopCard(
                    appName = uiState.appName.ifBlank { packageName },
                    onBackClick = onBackClick
                )
            }

            uiState.error?.let { error ->
                item {
                    MessageCard(
                        title = "Error",
                        message = error,
                        color = Orange,
                        onClick = viewModel::clearError
                    )
                }
            }

            uiState.message?.let { message ->
                item {
                    MessageCard(
                        title = "Status",
                        message = message,
                        color = if (message.contains("unlocked", true)) Green else PrimaryBlue,
                        onClick = {}
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    LoadingCard()
                }
            } else if (uiState.quiz != null && uiState.result == null) {
                item {
                    QuizIntroCard(
                        quiz = uiState.quiz!!,
                        selectedCount = uiState.selectedAnswers.size
                    )
                }

                itemsIndexed(uiState.quiz!!.questions) { index, question ->
                    QuestionCard(
                        index = index,
                        question = question,
                        selectedAnswer = uiState.selectedAnswers[question.id],
                        onSelect = { answer ->
                            viewModel.selectAnswer(question.id, answer)
                        }
                    )
                }

                item {
                    Button(
                        onClick = viewModel::submitQuiz,
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = WhiteText
                        )
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                color = WhiteText,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Submit Unlock Quiz",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (uiState.result != null) {
                item {
                    ResultCard(
                        result = uiState.result!!,
                        unlockedUntil = uiState.unlockedUntil,
                        onRetryClick = viewModel::retryWithNewQuiz
                    )
                }

                itemsIndexed(uiState.result!!.reviewData) { index, item ->
                    ReviewCard(
                        index = index,
                        review = item,
                        selectedAnswer = uiState.selectedAnswers[item.id]
                    )
                }
            } else {
                item {
                    MessageCard(
                        title = "No quiz needed",
                        message = "This app is not blocked right now.",
                        color = Green,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun TopCard(
    appName: String,
    onBackClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Back",
                color = SecondaryText,
                modifier = Modifier.clickable {
                    onBackClick()
                }
            )

            Text(
                text = "App Locked",
                color = WhiteText,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = appName,
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Pass the mixed AI quiz to unlock this app for a short time.",
                color = SecondaryText
            )
        }
    }
}

@Composable
private fun QuizIntroCard(
    quiz: QuizDto,
    selectedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Mixed AI Unlock Quiz",
                color = WhiteText,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Questions: ${quiz.questions.size}",
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Answered: $selectedCount / ${quiz.questions.size}",
                color = SecondaryText
            )
        }
    }
}

@Composable
private fun QuestionCard(
    index: Int,
    question: QuizQuestionDto,
    selectedAnswer: String?,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Question ${index + 1}",
                color = SecondaryText,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = question.questionText,
                color = WhiteText,
                fontWeight = FontWeight.Bold
            )

            question.options.forEach { option ->
                val selected = selectedAnswer == option

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(option)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) {
                            PrimaryBlue.copy(alpha = 0.25f)
                        } else {
                            Color.White.copy(alpha = 0.05f)
                        }
                    )
                ) {
                    Text(
                        text = option,
                        color = WhiteText,
                        modifier = Modifier.padding(14.dp),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: QuizAttemptResultDto,
    unlockedUntil: String?,
    onRetryClick: () -> Unit
) {
    val percent = (result.score * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (result.passed) "Quiz Passed" else "Quiz Failed",
                color = if (result.passed) Green else Red,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Score: $percent%",
                color = WhiteText,
                fontWeight = FontWeight.Bold
            )

            if (result.passed) {
                Text(
                    text = "Unlocked until: ${unlockedUntil ?: "soon"}",
                    color = SecondaryText
                )
            } else {
                Text(
                    text = "The app is still blocked. Take another quiz to unlock it.",
                    color = SecondaryText
                )

                OutlinedButton(
                    onClick = onRetryClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Take Another Quiz",
                        color = WhiteText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    index: Int,
    review: QuizReviewItemDto,
    selectedAnswer: String?
) {
    val correct = selectedAnswer == review.correctAnswer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Review ${index + 1}",
                color = SecondaryText,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = review.questionText,
                color = WhiteText,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Your answer: ${selectedAnswer ?: "No answer"}",
                color = if (correct) Green else Red
            )

            Text(
                text = "Correct answer: ${review.correctAnswer}",
                color = Green,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = review.explanation,
                color = SecondaryText
            )

            if (!review.sourceChunkSnippet.isNullOrBlank()) {
                Text(
                    text = "Source: ${review.sourceChunkSnippet}",
                    color = SecondaryText
                )
            }
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = color,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message,
                color = SecondaryText
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = PrimaryBlue
            )

            Text(
                text = "Preparing unlock quiz...",
                color = WhiteText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}