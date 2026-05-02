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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.domain.model.QuizQuestion
import com.rakizz.student.domain.model.QuizReviewItem
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.theme.RakizzColors
import kotlinx.coroutines.delay

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
        is UiState.Loading -> {
            QuizLoadingScreen()
        }

        is UiState.Error -> {
            QuizErrorScreen(
                message = uiState.message,
                onBackToMaterials = onBackToMaterials
            )
        }

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
    val result = attemptResult

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        RakizzColors.Background,
                        RakizzColors.BackgroundSoft
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = RakizzColors.Background
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 14.dp,
                    bottom = 30.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TopBar(
                        title = if (result == null) "Solve Quiz" else "Quiz Review",
                        subtitle = if (result == null) {
                            "Answer all questions, then submit."
                        } else {
                            "Review your score and answers."
                        },
                        onBackClick = onBackToMaterials
                    )
                }

                item {
                    QuizHeaderCard(
                        quiz = quiz,
                        selectedCount = selectedAnswers.size,
                        result = result
                    )
                }

                if (!actionMessage.isNullOrBlank()) {
                    item {
                        MessageCard(
                            title = "Notice",
                            message = actionMessage.orEmpty(),
                            color = RakizzColors.Primary
                        )
                    }
                }

                if (result == null) {
                    item {
                        QuizProgressCard(
                            selectedCount = selectedAnswers.size,
                            totalCount = quiz.questions.size
                        )
                    }

                    if (quiz.questions.isEmpty()) {
                        item {
                            MessageCard(
                                title = "No questions",
                                message = "This quiz does not have questions yet.",
                                color = RakizzColors.Warning
                            )
                        }
                    } else {
                        itemsIndexed(quiz.questions) { index, question ->
                            QuestionCard(
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
                            Button(
                                onClick = onSubmitQuiz,
                                enabled = !isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(66.dp),
                                shape = RoundedCornerShape(22.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RakizzColors.Primary,
                                    contentColor = RakizzColors.White,
                                    disabledContainerColor = RakizzColors.Primary.copy(alpha = 0.55f),
                                    disabledContentColor = RakizzColors.White
                                )
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        color = RakizzColors.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(22.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                Text(
                                    text = if (isSubmitting) "Submitting..." else "Submit Quiz",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = onBackToMaterials,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, RakizzColors.Primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RakizzColors.Primary
                            )
                        ) {
                            Text(
                                text = "Back to Materials",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
                    item {
                        ResultCard(result = result)
                    }

                    item {
                        SectionTitle(
                            title = "Answer review",
                            subtitle = "Correct answers, explanations, and source snippets"
                        )
                    }

                    itemsIndexed(result.reviewData) { index, reviewItem ->
                        ReviewCard(
                            questionNumber = index + 1,
                            reviewItem = reviewItem,
                            selectedAnswer = selectedAnswers[reviewItem.id]
                        )
                    }

                    item {
                        if (result.passed == true) {
                            Button(
                                onClick = onGoHome,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(62.dp),
                                shape = RoundedCornerShape(22.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RakizzColors.Success,
                                    contentColor = RakizzColors.White
                                )
                            ) {
                                Text(
                                    text = "Done",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        } else {
                            Button(
                                onClick = onRetryQuiz,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(62.dp),
                                shape = RoundedCornerShape(22.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RakizzColors.Primary,
                                    contentColor = RakizzColors.White
                                )
                            ) {
                                Text(
                                    text = "Retry Quiz",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = onBackToMaterials,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, RakizzColors.Primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RakizzColors.Primary
                            )
                        ) {
                            Text(
                                text = "Back to Materials",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        if (isSubmitting) {
            SubmittingOverlay()
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            onClick = onBackClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = RakizzColors.Card,
                contentColor = RakizzColors.Primary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = subtitle,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun QuizHeaderCard(
    quiz: Quiz,
    selectedCount: Int,
    result: Quiz?
) {
    val total = quiz.questions.size
    val isResult = result != null

    val title = when {
        isResult && result?.passed == true -> "Quiz Passed"
        isResult && result?.passed == false -> "Quiz Failed"
        total >= 20 -> "Mixed AI Quiz"
        else -> "AI Quiz"
    }

    val color = when {
        isResult && result?.passed == true -> RakizzColors.Success
        isResult && result?.passed == false -> RakizzColors.Error
        else -> RakizzColors.Primary
    }

    val subtitle = if (isResult) {
        "Your answers were graded by the backend."
    } else {
        "Generated from uploaded study material."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = color,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            color,
                            if (color == RakizzColors.Primary) {
                                RakizzColors.PrimaryDark
                            } else {
                                color.copy(alpha = 0.82f)
                            }
                        )
                    )
                )
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = RakizzColors.White.copy(alpha = 0.17f)
            ) {
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isResult && result?.passed == true -> Icons.Filled.Check
                            isResult && result?.passed == false -> Icons.Filled.Close
                            else -> Icons.Filled.Description
                        },
                        contentDescription = null,
                        tint = RakizzColors.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$total questions",
                color = RakizzColors.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = RakizzColors.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            if (!isResult) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WhitePill("Answered $selectedCount")
                    WhitePill("70% pass")
                }
            }
        }
    }
}

@Composable
private fun QuizProgressCard(
    selectedCount: Int,
    totalCount: Int
) {
    val progress = if (totalCount == 0) {
        0f
    } else {
        selectedCount.toFloat() / totalCount.toFloat()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Answer progress",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$selectedCount / $totalCount answered",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(RakizzColors.PrimarySoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(RakizzColors.Primary)
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(
    questionNumber: Int,
    question: QuizQuestion,
    selectedAnswer: String?,
    enabled: Boolean,
    onSelectAnswer: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuestionNumberBubble(number = questionNumber)

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Question $questionNumber",
                    color = RakizzColors.Primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = question.questionText,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = MaterialTheme.typography.titleMedium.lineHeight
            )

            question.options.forEach { option ->
                AnswerOptionCard(
                    option = option,
                    selected = selectedAnswer == option,
                    enabled = enabled,
                    onClick = {
                        onSelectAnswer(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun QuestionNumberBubble(
    number: Int
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(RakizzColors.PrimarySoft),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = RakizzColors.Primary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun AnswerOptionCard(
    option: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) {
        RakizzColors.Primary
    } else {
        RakizzColors.CardBorder
    }

    val bgColor = if (selected) {
        RakizzColors.PrimarySoft
    } else {
        RakizzColors.CardSoft
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            RakizzColors.Primary
                        } else {
                            RakizzColors.Card
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = RakizzColors.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = option,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun ResultCard(
    result: Quiz
) {
    val score = result.score ?: 0
    val passed = result.passed == true
    val color = if (passed) RakizzColors.Success else RakizzColors.Error
    val progress = (score.coerceIn(0, 100)).toFloat() / 100f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconCircle(
                icon = if (passed) Icons.Filled.DoneAll else Icons.Filled.Close,
                color = color
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (passed) "Passed" else "Not passed",
                color = color,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$score%",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(RakizzColors.BackgroundSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(color)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (passed) {
                    "Great. The student reached the 70% passing score."
                } else {
                    "The score is below 70%. The student should review and try again."
                },
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
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
    val correct = selectedAnswer == reviewItem.correctAnswer
    val color = if (correct) RakizzColors.Success else RakizzColors.Error

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuestionNumberBubble(number = questionNumber)

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (correct) "Correct answer" else "Needs review",
                    color = color,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = reviewItem.questionText,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            AnswerReviewLine(
                label = "Your answer",
                value = selectedAnswer ?: "No answer",
                color = color
            )

            AnswerReviewLine(
                label = "Correct answer",
                value = reviewItem.correctAnswer,
                color = RakizzColors.Success
            )

            SourceBox(
                title = "Explanation",
                text = reviewItem.explanation.ifBlank {
                    "No explanation was provided."
                },
                icon = Icons.Filled.Check
            )

            if (!reviewItem.sourceChunkSnippet.isNullOrBlank()) {
                SourceBox(
                    title = "Source snippet",
                    text = reviewItem.sourceChunkSnippet,
                    icon = Icons.Filled.Description
                )
            }
        }
    }
}

@Composable
private fun AnswerReviewLine(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label.uppercase(),
            color = RakizzColors.TextMuted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

@Composable
private fun SourceBox(
    title: String,
    text: String,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = RakizzColors.CardSoft,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RakizzColors.Primary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    color = RakizzColors.Primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = RakizzColors.TextMain,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            color = RakizzColors.TextSecond,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun IconCircle(
    icon: ImageVector,
    color: Color
) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.13f)
    ) {
        Box(
            modifier = Modifier.padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun WhitePill(
    text: String
) {
    Box(
        modifier = Modifier
            .background(
                color = RakizzColors.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(11.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = RakizzColors.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SubmittingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RakizzColors.Background.copy(alpha = 0.96f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(30.dp),
            color = RakizzColors.Card,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, RakizzColors.CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = RakizzColors.Primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Submitting answers...",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rakizz is grading the quiz and preparing the review.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun QuizLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RakizzColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = RakizzColors.Card,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, RakizzColors.CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = RakizzColors.Primary,
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Loading quiz...",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun QuizErrorScreen(
    message: String,
    onBackToMaterials: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RakizzColors.Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = RakizzColors.Card,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, RakizzColors.Error.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconCircle(
                    icon = Icons.Filled.Close,
                    color = RakizzColors.Error
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Could not load quiz",
                    color = RakizzColors.Error,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onBackToMaterials,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RakizzColors.Primary,
                        contentColor = RakizzColors.White
                    )
                ) {
                    Text(
                        text = "Back",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}