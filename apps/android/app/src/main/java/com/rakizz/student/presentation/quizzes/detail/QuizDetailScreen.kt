package com.rakizz.student.presentation.quizzes.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Reviews
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.domain.model.QuizQuestion
import com.rakizz.student.domain.model.QuizReviewItem
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.RakizzAnimatedTopBar
import com.rakizz.student.presentation.common.components.RakizzGlassCard
import com.rakizz.student.presentation.common.components.RakizzPrimaryButton
import com.rakizz.student.presentation.common.components.RakizzSecondaryButton
import com.rakizz.student.presentation.common.components.RakizzStatusChip
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
                onTryAnotherQuiz = onTryAnotherQuiz,
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
    onTryAnotherQuiz: () -> Unit,
    onBackToMaterials: () -> Unit
) {
    val result = attemptResult

    var showContent by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        showContent = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = RakizzColors.Background
        ) { paddingValues ->
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
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 450)
                    ) + slideInVertically(
                        animationSpec = tween(durationMillis = 450),
                        initialOffsetY = {
                            it / 5
                        }
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 18.dp,
                            bottom = 30.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            RakizzAnimatedTopBar(
                                title = if (result == null) {
                                    "AI Quiz"
                                } else {
                                    "Quiz Review"
                                },
                                subtitle = if (result == null) {
                                    "Answer the questions and submit your attempt."
                                } else {
                                    "Review your score, answers, and AI explanations."
                                },
                                showBackButton = true,
                                onBackClick = onBackToMaterials,
                                trailingContent = {
                                    RakizzStatusChip(
                                        text = if (result == null) {
                                            "SOLVE"
                                        } else {
                                            "RESULT"
                                        },
                                        icon = if (result == null) {
                                            Icons.Rounded.Quiz
                                        } else {
                                            Icons.Rounded.Reviews
                                        }
                                    )
                                }
                            )
                        }

                        item {
                            QuizHeroCard(
                                quiz = quiz,
                                selectedCount = selectedAnswers.size,
                                result = result
                            )
                        }

                        if (!actionMessage.isNullOrBlank()) {
                            item {
                                NoticeCard(
                                    title = "Notice",
                                    message = actionMessage,
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
                                    NoticeCard(
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
                                    RakizzPrimaryButton(
                                        text = if (isSubmitting) {
                                            "Submitting..."
                                        } else {
                                            "Submit Quiz"
                                        },
                                        enabled = !isSubmitting,
                                        icon = Icons.Rounded.TaskAlt,
                                        onClick = onSubmitQuiz
                                    )
                                }
                            }

                            item {
                                RakizzSecondaryButton(
                                    text = "Back to Materials",
                                    icon = Icons.Rounded.Description,
                                    onClick = onBackToMaterials
                                )
                            }
                        } else {
                            item {
                                ResultCard(
                                    result = result
                                )
                            }

                            item {
                                ReviewSectionTitle()
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
                                    RakizzPrimaryButton(
                                        text = "Done",
                                        icon = Icons.Filled.DoneAll,
                                        onClick = onGoHome
                                    )
                                } else {
                                    RakizzPrimaryButton(
                                        text = "Retry Quiz",
                                        icon = Icons.Rounded.Refresh,
                                        onClick = onRetryQuiz
                                    )
                                }
                            }

                            item {
                                RakizzSecondaryButton(
                                    text = "Try Another Quiz",
                                    icon = Icons.Rounded.AutoAwesome,
                                    onClick = onTryAnotherQuiz
                                )
                            }

                            item {
                                RakizzSecondaryButton(
                                    text = "Back to Materials",
                                    icon = Icons.Rounded.Description,
                                    onClick = onBackToMaterials
                                )
                            }
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
private fun QuizHeroCard(
    quiz: Quiz,
    selectedCount: Int,
    result: Quiz?
) {
    val totalQuestions = quiz.questions.size
    val isResult = result != null

    val title = when {
        isResult && result?.passed == true -> "Quiz Passed"
        isResult && result?.passed == false -> "Quiz Needs Review"
        totalQuestions >= 20 -> "Mixed AI Quiz"
        else -> "AI Practice Quiz"
    }

    val accentColor = when {
        isResult && result?.passed == true -> RakizzColors.Success
        isResult && result?.passed == false -> RakizzColors.Error
        else -> RakizzColors.Primary
    }

    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 34.dp,
        glow = true,
        contentPadding = PaddingValues(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PulsingQuizIcon(
                color = accentColor,
                result = result
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isResult) {
                    "Your attempt was graded. Review the explanations below."
                } else {
                    "Generated from your uploaded material by Rakizz AI Tutor."
                },
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RakizzStatusChip(
                    text = "$totalQuestions questions",
                    color = RakizzColors.Primary,
                    softColor = RakizzColors.PrimarySoft
                )

                if (!isResult) {
                    RakizzStatusChip(
                        text = "$selectedCount answered",
                        color = RakizzColors.Accent,
                        softColor = RakizzColors.AccentSoft
                    )
                } else {
                    RakizzStatusChip(
                        text = "${result?.score ?: 0}%",
                        color = accentColor,
                        softColor = accentColor.copy(alpha = 0.13f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PulsingQuizIcon(
    color: Color,
    result: Quiz?
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "quiz_icon_transition"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "quiz_icon_glow_alpha"
    )

    Box(
        modifier = Modifier.size(92.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = glowAlpha))
        )

        Surface(
            modifier = Modifier.size(66.dp),
            shape = CircleShape,
            color = color,
            shadowElevation = 8.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        result?.passed == true -> Icons.Filled.Check
                        result?.passed == false -> Icons.Filled.Close
                        else -> Icons.Rounded.School
                    },
                    contentDescription = null,
                    tint = RakizzColors.White,
                    modifier = Modifier.size(32.dp)
                )
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
    }.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 450),
        label = "quiz_progress"
    )

    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Answer progress",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "$selectedCount / $totalCount answered",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            RakizzStatusChip(
                text = "${(animatedProgress * 100).toInt()}%",
                color = RakizzColors.Primary,
                softColor = RakizzColors.PrimarySoft
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LinearProgressIndicator(
            progress = {
                animatedProgress
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(50)),
            color = RakizzColors.Primary,
            trackColor = RakizzColors.PrimarySoft
        )
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
    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuestionNumberBubble(
                number = questionNumber
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "QUESTION $questionNumber",
                    color = RakizzColors.Primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Choose one answer",
                    color = RakizzColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = question.questionText,
            color = RakizzColors.TextMain,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = MaterialTheme.typography.titleMedium.lineHeight
        )

        Spacer(modifier = Modifier.height(16.dp))

        question.options.forEachIndexed { index, option ->
            AnswerOptionCard(
                optionLetter = ('A' + index).toString(),
                option = option,
                selected = selectedAnswer == option,
                enabled = enabled,
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

@Composable
private fun QuestionNumberBubble(
    number: Int
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = RakizzColors.PrimarySoft,
        border = BorderStroke(
            width = 1.dp,
            color = RakizzColors.Primary.copy(alpha = 0.28f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = RakizzColors.Primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun AnswerOptionCard(
    optionLetter: String,
    option: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "answer_option_scale"
    )

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
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clickable(enabled = enabled) {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        shadowElevation = if (selected) {
            4.dp
        } else {
            0.dp
        }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                color = if (selected) {
                    RakizzColors.Primary
                } else {
                    RakizzColors.Card
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = borderColor
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = RakizzColors.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = optionLetter,
                            color = RakizzColors.TextMuted,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = option,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) {
                    FontWeight.ExtraBold
                } else {
                    FontWeight.Medium
                },
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
    val color = if (passed) {
        RakizzColors.Success
    } else {
        RakizzColors.Error
    }

    val progress = (score.coerceIn(0, 100)).toFloat() / 100f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 700),
        label = "result_progress"
    )

    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 34.dp,
        glow = true,
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PulsingQuizIcon(
                color = color,
                result = result
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (passed) {
                    "Passed"
                } else {
                    "Not Passed"
                },
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

            LinearProgressIndicator(
                progress = {
                    animatedProgress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (passed) {
                    "Great work. You reached the 70% passing score."
                } else {
                    "The score is below 70%. Review the explanations and try again."
                },
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun ReviewSectionTitle() {
    Column {
        Text(
            text = "AI Answer Review",
            color = RakizzColors.TextMain,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Correct answers, explanations, and source snippets.",
            color = RakizzColors.TextSecond,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ReviewCard(
    questionNumber: Int,
    reviewItem: QuizReviewItem,
    selectedAnswer: String?
) {
    val correct = selectedAnswer == reviewItem.correctAnswer
    val color = if (correct) {
        RakizzColors.Success
    } else {
        RakizzColors.Error
    }

    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        contentPadding = PaddingValues(18.dp),
        glow = false
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuestionNumberBubble(
                number = questionNumber
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (correct) {
                        "Correct answer"
                    } else {
                        "Needs review"
                    },
                    color = color,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "AI explanation included",
                    color = RakizzColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            RakizzStatusChip(
                text = if (correct) {
                    "Correct"
                } else {
                    "Review"
                },
                color = color,
                softColor = color.copy(alpha = 0.13f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = reviewItem.questionText,
            color = RakizzColors.TextMain,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = MaterialTheme.typography.titleMedium.lineHeight
        )

        Spacer(modifier = Modifier.height(14.dp))

        AnswerReviewLine(
            label = "Your answer",
            value = selectedAnswer ?: "No answer",
            color = color
        )

        Spacer(modifier = Modifier.height(10.dp))

        AnswerReviewLine(
            label = "Correct answer",
            value = reviewItem.correctAnswer,
            color = RakizzColors.Success
        )

        Spacer(modifier = Modifier.height(14.dp))

        SourceBox(
            title = "AI Explanation",
            text = reviewItem.explanation.ifBlank {
                "No explanation was provided."
            },
            icon = Icons.Rounded.CheckCircle
        )

        if (!reviewItem.sourceChunkSnippet.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))

            SourceBox(
                title = "Source Snippet",
                text = reviewItem.sourceChunkSnippet,
                icon = Icons.Rounded.Description
            )
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
        shape = RoundedCornerShape(20.dp),
        color = RakizzColors.CardSoft,
        border = BorderStroke(
            width = 1.dp,
            color = RakizzColors.CardBorder
        )
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
private fun NoticeCard(
    title: String,
    message: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.35f)
        )
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
private fun SubmittingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RakizzColors.Background.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center
    ) {
        RakizzGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            cornerRadius = 34.dp,
            glow = true,
            contentPadding = PaddingValues(26.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = RakizzColors.Primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(76.dp)
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
                    text = "Rakizz is grading your quiz and preparing the AI review.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
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
            .background(
                Brush.verticalGradient(
                    listOf(
                        RakizzColors.Background,
                        RakizzColors.BackgroundSoft
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        RakizzGlassCard(
            cornerRadius = 30.dp,
            glow = true,
            contentPadding = PaddingValues(28.dp)
        ) {
            Column(
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
            .background(
                Brush.verticalGradient(
                    listOf(
                        RakizzColors.Background,
                        RakizzColors.BackgroundSoft
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        RakizzGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 30.dp,
            glow = false,
            contentPadding = PaddingValues(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    color = RakizzColors.Error.copy(alpha = 0.13f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = RakizzColors.Error.copy(alpha = 0.35f)
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = RakizzColors.Error,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

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
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                Spacer(modifier = Modifier.height(18.dp))

                RakizzPrimaryButton(
                    text = "Back to Materials",
                    onClick = onBackToMaterials
                )
            }
        }
    }
}