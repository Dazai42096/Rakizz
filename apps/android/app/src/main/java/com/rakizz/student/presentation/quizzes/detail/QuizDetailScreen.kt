package com.rakizz.student.presentation.quizzes.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.domain.model.QuizQuestion
import com.rakizz.student.domain.model.QuizReviewItem
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.ErrorView
import com.rakizz.student.presentation.common.components.LoadingView
import kotlinx.coroutines.delay

private val ScreenTop = Color(0xFF041127)
private val ScreenMid = Color(0xFF02060E)
private val ScreenBottom = Color(0xFF000000)
private val AccentBlue = Color(0xFF2157D8)
private val AccentBlueSoft = Color(0xFF6AA5FF)
private val SuccessGreen = Color(0xFF34E27A)
private val ErrorRed = Color(0xFFFF4D5C)
private val CardSurface = Color.White.copy(alpha = 0.06f)
private val CardBorder = Color.White.copy(alpha = 0.08f)
private val SecondaryText = Color(0xFF8F97A9)

@Composable
fun QuizDetailScreen(
    quizId: String,
    onStartQuiz: () -> Unit = {},
    onGoHome: () -> Unit = {},
    onTryAnotherQuiz: () -> Unit = {},
    onBackToMaterials: () -> Unit = {},
    viewModel: QuizDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(quizId) {
        viewModel.loadQuiz(quizId)
    }

    val state by viewModel.uiState.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val attemptResult by viewModel.attemptResult.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(actionMessage) {
        if (!actionMessage.isNullOrBlank()) {
            delay(2500)
            viewModel.clearActionMessage()
        }
    }

    when (val uiState = state) {
        is UiState.Loading -> LoadingView()

        is UiState.Error -> ErrorView(
            message = uiState.message
        )

        is UiState.Success -> {
            QuizDetailContent(
                quiz = uiState.data,
                selectedAnswers = selectedAnswers,
                attemptResult = attemptResult,
                isSubmitting = isSubmitting,
                actionMessage = actionMessage,
                onSelectAnswer = viewModel::selectAnswer,
                onSubmitQuiz = viewModel::submitQuiz,
                onRetryQuiz = viewModel::retryQuiz,
                onGoHome = onGoHome,
                onBackToMaterials = onBackToMaterials
            )
        }

        else -> Unit
    }
}

@Composable
private fun QuizDetailContent(
    quiz: Quiz,
    selectedAnswers: Map<String, String>,
    attemptResult: Quiz?,
    isSubmitting: Boolean,
    actionMessage: String?,
    onSelectAnswer: (String, String) -> Unit,
    onSubmitQuiz: () -> Unit,
    onRetryQuiz: () -> Unit,
    onGoHome: () -> Unit,
    onBackToMaterials: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ScreenTop, ScreenMid, ScreenBottom)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 22.dp,
                    end = 22.dp,
                    top = 24.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    HeaderCard(
                        quiz = quiz,
                        attemptResult = attemptResult
                    )
                }

                if (!actionMessage.isNullOrBlank()) {
                    item {
                        MessageCard(message = actionMessage.orEmpty())
                    }
                }

                val result = attemptResult

                if (result == null) {
                    item {
                        QuizInfoCard(
                            totalQuestions = quiz.questions.size
                        )
                    }

                    if (quiz.questions.isEmpty()) {
                        item {
                            MessageCard(
                                message = "No questions were returned for this quiz."
                            )
                        }
                    } else {
                        itemsIndexed(quiz.questions) { index, question ->
                            QuizQuestionCard(
                                questionNumber = index + 1,
                                question = question,
                                selectedAnswer = selectedAnswers[question.id],
                                enabled = !isSubmitting,
                                onSelectAnswer = { answer ->
                                    onSelectAnswer(question.id, answer)
                                }
                            )
                        }

                        item {
                            PrimaryActionButton(
                                label = if (isSubmitting) "Submitting..." else "Submit Quiz",
                                enabled = !isSubmitting,
                                onClick = onSubmitQuiz
                            )
                        }
                    }

                    item {
                        SecondaryActionButton(
                            label = "Back to Materials",
                            onClick = onBackToMaterials
                        )
                    }
                } else {
                    item {
                        ResultCard(result = result)
                    }

                    itemsIndexed(result.reviewData) { index, reviewItem ->
                        ReviewCard(
                            questionNumber = index + 1,
                            reviewItem = reviewItem,
                            selectedAnswer = selectedAnswers[reviewItem.id]
                        )
                    }

                    item {
                        PrimaryActionButton(
                            label = if (result.passed == true) "Go to Home" else "Retry Quiz",
                            enabled = true,
                            onClick = {
                                if (result.passed == true) {
                                    onGoHome()
                                } else {
                                    onRetryQuiz()
                                }
                            }
                        )
                    }

                    item {
                        SecondaryActionButton(
                            label = "Back to Materials",
                            onClick = onBackToMaterials
                        )
                    }
                }
            }
        }

        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AccentBlue,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Composable
private fun HeaderCard(
    quiz: Quiz,
    attemptResult: Quiz?
) {
    val passed = attemptResult?.passed == true
    val failed = attemptResult != null && attemptResult.passed == false

    val iconTint = when {
        passed -> SuccessGreen
        failed -> ErrorRed
        else -> AccentBlueSoft
    }

    val borderTint = when {
        passed -> SuccessGreen.copy(alpha = 0.30f)
        failed -> ErrorRed.copy(alpha = 0.30f)
        else -> AccentBlue.copy(alpha = 0.30f)
    }

    val title = when {
        passed -> "Quiz Passed"
        failed -> "Quiz Failed"
        else -> "Solve Quiz"
    }

    val subtitle = when {
        passed -> "Your answers were graded successfully."
        failed -> "Review the correct answers and try again."
        else -> "Answer all questions and submit to get your grade."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, borderTint)
            ) {
                Box(
                    modifier = Modifier
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = when {
                            passed -> Icons.Filled.Check
                            failed -> Icons.Filled.Close
                            else -> Icons.Filled.Description
                        },
                        contentDescription = null,
                        tint = iconTint
                    )
                }
            }

            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = quiz.title.ifBlank { "Generated Quiz" },
                color = AccentBlueSoft,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                color = SecondaryText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuizInfoCard(
    totalQuestions: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "QUIZ DETAILS",
                color = SecondaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Questions: $totalQuestions",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Select one answer for each question, then submit to get your real score from the backend.",
                color = SecondaryText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun QuizQuestionCard(
    questionNumber: Int,
    question: QuizQuestion,
    selectedAnswer: String?,
    enabled: Boolean,
    onSelectAnswer: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "QUESTION $questionNumber",
                color = SecondaryText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = question.questionText,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            question.options.forEach { option ->
                val isSelected = selectedAnswer == option

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) {
                            onSelectAnswer(option)
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AccentBlue.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) AccentBlue else CardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) AccentBlue else Color.Transparent,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) AccentBlue else SecondaryText
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = option,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: Quiz
) {
    val score = result.score ?: 0
    val progress = (score.coerceIn(0, 100)) / 100f
    val passed = result.passed == true
    val accent = if (passed) SuccessGreen else ErrorRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (passed) "RESULT: PASSED" else "RESULT: FAILED",
                color = accent,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "$score%",
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = accent,
                trackColor = Color.White.copy(alpha = 0.10f)
            )

            Text(
                text = "Backend score based on your submitted answers.",
                color = SecondaryText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReviewCard(
    questionNumber: Int,
    reviewItem: QuizReviewItem,
    selectedAnswer: String?
) {
    val answeredCorrectly = selectedAnswer == reviewItem.correctAnswer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "REVIEW $questionNumber",
                color = SecondaryText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = reviewItem.questionText,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            AnswerLine(
                label = "Your answer",
                value = selectedAnswer ?: "No answer",
                valueColor = if (answeredCorrectly) SuccessGreen else ErrorRed
            )

            AnswerLine(
                label = "Correct answer",
                value = reviewItem.correctAnswer,
                valueColor = SuccessGreen
            )

            Text(
                text = "Explanation",
                color = SecondaryText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = reviewItem.explanation.ifBlank { "No explanation provided." },
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )

            if (!reviewItem.sourceChunkSnippet.isNullOrBlank()) {
                Text(
                    text = "Source snippet",
                    color = SecondaryText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = reviewItem.sourceChunkSnippet,
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AnswerLine(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = SecondaryText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MessageCard(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF12203D)
        ),
        border = BorderStroke(1.dp, Color(0xFF23498E))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun PrimaryActionButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentBlue,
            contentColor = Color.White,
            disabledContainerColor = AccentBlue.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.8f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SecondaryActionButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFFB0B6C4)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
    }
}