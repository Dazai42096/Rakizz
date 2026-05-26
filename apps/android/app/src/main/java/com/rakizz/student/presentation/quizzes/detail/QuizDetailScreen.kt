package com.rakizz.student.presentation.quizzes.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.domain.model.QuizQuestion
import com.rakizz.student.domain.model.QuizReviewItem
import com.rakizz.student.presentation.common.UiState
import kotlinx.coroutines.delay

@Composable
fun QuizDetailScreen(
    quizId: String,
    onGoHome: () -> Unit = {},
    onBackToMaterials: () -> Unit = {},
    viewModel: QuizDetailViewModel = hiltViewModel()
) {
    val colors = quizColors()

    LaunchedEffect(quizId) {
        viewModel.loadQuiz(quizId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val attemptResult by viewModel.attemptResult.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isGeneratingAnother by viewModel.isGeneratingAnother.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(actionMessage, isGeneratingAnother, isSubmitting) {
        if (!actionMessage.isNullOrBlank() && !isGeneratingAnother && !isSubmitting) {
            delay(2500)
            viewModel.clearActionMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.backgroundTop,
                        colors.backgroundMiddle,
                        colors.backgroundBottom
                    )
                )
            )
    ) {
        when (val state = uiState) {
            UiState.Loading -> {
                FullScreenLoading(
                    text = "Loading quiz...",
                    colors = colors
                )
            }

            UiState.Empty -> {
                FullScreenError(
                    title = "Quiz is empty",
                    message = "This quiz does not contain questions.",
                    colors = colors,
                    onBackToMaterials = onBackToMaterials
                )
            }

            is UiState.Error -> {
                FullScreenError(
                    title = "Could not load quiz",
                    message = state.message,
                    colors = colors,
                    onBackToMaterials = onBackToMaterials
                )
            }

            is UiState.Success -> {
                QuizDetailContent(
                    quiz = state.data,
                    selectedAnswers = selectedAnswers,
                    result = attemptResult,
                    isSubmitting = isSubmitting,
                    isGeneratingAnother = isGeneratingAnother,
                    actionMessage = actionMessage,
                    colors = colors,
                    onBackToMaterials = onBackToMaterials,
                    onGoHome = onGoHome,
                    onSelectAnswer = viewModel::selectAnswer,
                    onSubmitQuiz = viewModel::submitQuiz,
                    onRetryQuiz = viewModel::retryQuiz,
                    onTryAnotherQuiz = viewModel::generateAnotherQuiz
                )
            }
        }

        if (isSubmitting) {
            BusyOverlay(
                title = "Submitting answers...",
                message = "Rakizz is grading your quiz and preparing the answer review.",
                colors = colors
            )
        }

        if (isGeneratingAnother) {
            BusyOverlay(
                title = "Generating another quiz...",
                message = "Rakizz is reading the same material and creating a new quiz.",
                colors = colors
            )
        }
    }
}

@Composable
private fun QuizDetailContent(
    quiz: Quiz,
    selectedAnswers: Map<String, String>,
    result: Quiz?,
    isSubmitting: Boolean,
    isGeneratingAnother: Boolean,
    actionMessage: String?,
    colors: QuizColors,
    onBackToMaterials: () -> Unit,
    onGoHome: () -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onSubmitQuiz: () -> Unit,
    onRetryQuiz: () -> Unit,
    onTryAnotherQuiz: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 20.dp)
            .padding(
                bottom = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            ),
        contentPadding = PaddingValues(top = 18.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            QuizTopBar(
                title = if (result == null) "AI Quiz" else "Quiz Review",
                subtitle = if (result == null) {
                    "Answer all questions and submit your attempt"
                } else {
                    "Review your score, answers, and explanations"
                },
                colors = colors,
                onBackClick = onBackToMaterials
            )
        }

        item {
            QuizHeroCard(
                quiz = quiz,
                selectedCount = selectedAnswers.size,
                result = result,
                colors = colors
            )
        }

        if (!actionMessage.isNullOrBlank()) {
            item {
                MessageCard(
                    message = actionMessage,
                    colors = colors
                )
            }
        }

        if (result == null) {
            item {
                AnswerProgressCard(
                    answered = selectedAnswers.size,
                    total = quiz.questions.size,
                    colors = colors
                )
            }

            if (quiz.questions.isEmpty()) {
                item {
                    MessageCard(
                        message = "No quiz questions were returned.",
                        colors = colors
                    )
                }
            } else {
                itemsIndexed(quiz.questions) { index, question ->
                    QuestionCard(
                        number = index + 1,
                        question = question,
                        selectedAnswer = selectedAnswers[question.id],
                        enabled = !isSubmitting && !isGeneratingAnother,
                        colors = colors,
                        onSelectAnswer = { answer ->
                            onSelectAnswer(question.id, answer)
                        }
                    )
                }

                item {
                    PrimaryActionButton(
                        text = if (isSubmitting) "Submitting..." else "Submit Quiz",
                        enabled = !isSubmitting && !isGeneratingAnother,
                        colors = colors,
                        onClick = onSubmitQuiz
                    )
                }
            }

            item {
                SecondaryActionButton(
                    text = "Back to Materials",
                    enabled = !isSubmitting && !isGeneratingAnother,
                    colors = colors,
                    onClick = onBackToMaterials
                )
            }
        } else {
            item {
                ResultCard(
                    result = result,
                    colors = colors
                )
            }

            item {
                SectionTitle(
                    title = "AI Answer Review",
                    subtitle = "Correct answers, explanations, and source snippets",
                    colors = colors
                )
            }

            itemsIndexed(result.reviewData) { index, review ->
                ReviewCard(
                    number = index + 1,
                    review = review,
                    selectedAnswer = selectedAnswers[review.id],
                    colors = colors
                )
            }

            item {
                if (result.passed == true) {
                    PrimaryActionButton(
                        text = "Done",
                        enabled = !isGeneratingAnother,
                        colors = colors,
                        onClick = onGoHome
                    )
                } else {
                    PrimaryActionButton(
                        text = "Retry Same Quiz",
                        enabled = !isGeneratingAnother,
                        colors = colors,
                        onClick = onRetryQuiz
                    )
                }
            }

            item {
                SecondaryActionButton(
                    text = if (isGeneratingAnother) {
                        "Generating Another Quiz..."
                    } else {
                        "Try Another Quiz"
                    },
                    enabled = !isGeneratingAnother && !isSubmitting,
                    colors = colors,
                    onClick = onTryAnotherQuiz
                )
            }

            item {
                SecondaryActionButton(
                    text = "Back to Materials",
                    enabled = !isGeneratingAnother && !isSubmitting,
                    colors = colors,
                    onClick = onBackToMaterials
                )
            }
        }
    }
}

@Composable
private fun QuizTopBar(
    title: String,
    subtitle: String,
    colors: QuizColors,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleButton(
            text = "<",
            colors = colors,
            onClick = onBackClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun QuizHeroCard(
    quiz: Quiz,
    selectedCount: Int,
    result: Quiz?,
    colors: QuizColors
) {
    val isResult = result != null
    val score = result?.score ?: 0

    val title = when {
        isResult && result?.passed == true -> "Quiz Passed"
        isResult && result?.passed == false -> "Quiz Needs Review"
        quiz.questions.size >= 20 -> "Mixed AI Quiz"
        else -> "AI Practice Quiz"
    }

    val statusText = if (isResult) {
        "$score% score"
    } else {
        "$selectedCount/${quiz.questions.size} answered"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.28f),
                            colors.card,
                            colors.cardAlt
                        )
                    )
                )
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                colors.primary,
                                colors.accent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Q",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 27.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isResult) {
                    "Your answers were graded. Review the explanations below."
                } else {
                    "Generated from your uploaded material."
                },
                color = colors.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            StatusChip(
                text = statusText,
                colors = colors
            )

            if (quiz.materialId.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Material ID: ${quiz.materialId}",
                    color = colors.textMuted,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AnswerProgressCard(
    answered: Int,
    total: Int,
    colors: QuizColors
) {
    val progress = if (total == 0) {
        0f
    } else {
        answered.toFloat() / total.toFloat()
    }.coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Answer Progress",
                    color = colors.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$answered / $total",
                    color = colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = colors.primary,
                trackColor = colors.track
            )
        }
    }
}

@Composable
private fun QuestionCard(
    number: Int,
    question: QuizQuestion,
    selectedAnswer: String?,
    enabled: Boolean,
    colors: QuizColors,
    onSelectAnswer: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.16f))
                        .border(1.dp, colors.primary.copy(alpha = 0.32f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        color = colors.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "QUESTION $number",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Choose one answer",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = question.questionText,
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            question.options.forEachIndexed { index, option ->
                AnswerOption(
                    letter = ('A' + index).toString(),
                    text = option,
                    selected = selectedAnswer == option,
                    enabled = enabled,
                    colors = colors,
                    onClick = {
                        onSelectAnswer(option)
                    }
                )

                if (index != question.options.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun AnswerOption(
    letter: String,
    text: String,
    selected: Boolean,
    enabled: Boolean,
    colors: QuizColors,
    onClick: () -> Unit
) {
    val borderColor = if (selected) {
        colors.primary
    } else {
        colors.border
    }

    val background = if (selected) {
        colors.primary.copy(alpha = 0.15f)
    } else {
        colors.cardAlt
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = background,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            colors.primary
                        } else {
                            colors.card
                        }
                    )
                    .border(1.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selected) "✓" else letter,
                    color = if (selected) Color.White else colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = text,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                lineHeight = 20.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun ResultCard(
    result: Quiz,
    colors: QuizColors
) {
    val score = result.score ?: 0
    val passed = result.passed == true
    val statusColor = if (passed) colors.success else colors.danger

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (passed) "Passed" else "Not Passed",
                color = statusColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$score%",
                color = colors.textPrimary,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = {
                    score.coerceIn(0, 100) / 100f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = statusColor,
                trackColor = colors.track
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (passed) {
                    "Great work. You reached the passing score."
                } else {
                    "Review the explanations and try again."
                },
                color = colors.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun ReviewCard(
    number: Int,
    review: QuizReviewItem,
    selectedAnswer: String?,
    colors: QuizColors
) {
    val correct = selectedAnswer == review.correctAnswer
    val statusColor = if (correct) colors.success else colors.danger

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.32f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        color = statusColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = if (correct) "Correct" else "Needs Review",
                        color = statusColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "AI explanation included",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = review.questionText,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnswerReviewLine(
                label = "Your answer",
                value = selectedAnswer ?: "No answer",
                color = statusColor,
                colors = colors
            )

            Spacer(modifier = Modifier.height(10.dp))

            AnswerReviewLine(
                label = "Correct answer",
                value = review.correctAnswer,
                color = colors.success,
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            InfoBox(
                title = "AI Explanation",
                text = review.explanation.ifBlank { "No explanation was provided." },
                colors = colors
            )

            if (!review.sourceChunkSnippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                InfoBox(
                    title = "Source Snippet",
                    text = review.sourceChunkSnippet,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun AnswerReviewLine(
    label: String,
    value: String,
    color: Color,
    colors: QuizColors
) {
    Column {
        Text(
            text = label.uppercase(),
            color = colors.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun InfoBox(
    title: String,
    text: String,
    colors: QuizColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.cardAlt,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = colors.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = text,
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    colors: QuizColors
) {
    Column {
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            color = colors.textSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun MessageCard(
    message: String,
    colors: QuizColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.primary.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.32f))
    ) {
        Text(
            text = message,
            color = colors.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    enabled: Boolean,
    colors: QuizColors,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = Color.White,
            disabledContainerColor = colors.primary.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.70f)
        ),
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    enabled: Boolean,
    colors: QuizColors,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.textPrimary,
            disabledContentColor = colors.textSecondary
        ),
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun StatusChip(
    text: String,
    colors: QuizColors
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = colors.primary.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.30f))
    ) {
        Text(
            text = text,
            color = colors.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun CircleButton(
    text: String,
    colors: QuizColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(colors.card)
            .border(1.dp, colors.border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colors.textPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun FullScreenLoading(
    text: String,
    colors: QuizColors
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = colors.primary)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = text,
                    color = colors.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun FullScreenError(
    title: String,
    message: String,
    colors: QuizColors,
    onBackToMaterials: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = colors.danger,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryActionButton(
                    text = "Back to Materials",
                    enabled = true,
                    colors = colors,
                    onClick = onBackToMaterials
                )
            }
        }
    }
}

@Composable
private fun BusyOverlay(
    title: String,
    message: String,
    colors: QuizColors
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBottom.copy(alpha = 0.96f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = colors.primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(70.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

private fun quizColors(): QuizColors {
    return QuizColors(
        backgroundTop = Color(0xFF020617),
        backgroundMiddle = Color(0xFF07111F),
        backgroundBottom = Color(0xFF000000),
        card = Color(0xE60B1220),
        cardAlt = Color(0xCC111A2E),
        track = Color(0x6623344F),
        border = Color(0x334D9DFF),
        primary = Color(0xFF2F80FF),
        accent = Color(0xFF00D4FF),
        success = Color(0xFF22C55E),
        danger = Color(0xFFEF4444),
        textPrimary = Color.White,
        textSecondary = Color(0xFF94A3B8),
        textMuted = Color(0xFF64748B)
    )
}

private data class QuizColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val cardAlt: Color,
    val track: Color,
    val border: Color,
    val primary: Color,
    val accent: Color,
    val success: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)