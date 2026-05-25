package com.rakizz.student.presentation.unlock

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
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.data.remote.dto.QuizAttemptResultDto
import com.rakizz.student.data.remote.dto.QuizDto
import com.rakizz.student.data.remote.dto.QuizQuestionDto
import com.rakizz.student.data.remote.dto.QuizReviewItemDto
import com.rakizz.student.presentation.common.components.RakizzAnimatedTopBar
import com.rakizz.student.presentation.common.components.RakizzGlassCard
import com.rakizz.student.presentation.common.components.RakizzPrimaryButton
import com.rakizz.student.presentation.common.components.RakizzSecondaryButton
import com.rakizz.student.presentation.common.components.RakizzStatusChip
import com.rakizz.student.presentation.theme.RakizzColors
import kotlinx.coroutines.delay

@Composable
fun UnlockQuizScreen(
    packageName: String,
    forceUnlock: Boolean,
    onBackClick: () -> Unit,
    viewModel: UnlockQuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val quiz = uiState.quiz
    val result = uiState.result

    LaunchedEffect(packageName, forceUnlock) {
        viewModel.startUnlockFlow(
            packageName = packageName,
            forceBlocked = forceUnlock
        )
    }

    var showContent by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        showContent = true
    }

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
                            title = "Focus Shield",
                            subtitle = "Learning is required before temporary access.",
                            showBackButton = true,
                            onBackClick = onBackClick,
                            trailingContent = {
                                RakizzStatusChip(
                                    text = if (result?.passed == true || uiState.unlockedUntil != null) {
                                        "UNLOCKED"
                                    } else {
                                        "LOCKED"
                                    },
                                    color = if (result?.passed == true || uiState.unlockedUntil != null) {
                                        RakizzColors.Success
                                    } else {
                                        RakizzColors.Primary
                                    },
                                    softColor = if (result?.passed == true || uiState.unlockedUntil != null) {
                                        RakizzColors.Success.copy(alpha = 0.13f)
                                    } else {
                                        RakizzColors.PrimarySoft
                                    },
                                    icon = Icons.Rounded.Shield
                                )
                            }
                        )
                    }

                    item {
                        FocusShieldHeroCard(
                            appName = uiState.appName.ifBlank {
                                packageName
                            },
                            packageName = packageName,
                            isUnlocked = result?.passed == true || uiState.unlockedUntil != null
                        )
                    }

                    uiState.error?.let { error ->
                        item {
                            StatusMessageCard(
                                title = "Error",
                                message = error,
                                color = RakizzColors.Error,
                                onClick = viewModel::clearError
                            )
                        }
                    }

                    uiState.message?.let { message ->
                        item {
                            StatusMessageCard(
                                title = "Status",
                                message = message,
                                color = if (message.contains("unlocked", ignoreCase = true)) {
                                    RakizzColors.Success
                                } else {
                                    RakizzColors.Primary
                                },
                                onClick = {}
                            )
                        }
                    }

                    when {
                        uiState.isLoading -> {
                            item {
                                UnlockLoadingCard()
                            }
                        }

                        quiz != null && result == null -> {
                            item {
                                UnlockQuizIntroCard(
                                    quiz = quiz,
                                    selectedCount = uiState.selectedAnswers.size
                                )
                            }

                            itemsIndexed(
                                items = quiz.questions,
                                key = { _, question ->
                                    question.id
                                }
                            ) { index, question ->
                                UnlockQuestionCard(
                                    questionNumber = index + 1,
                                    question = question,
                                    selectedAnswer = uiState.selectedAnswers[question.id],
                                    onSelect = { answer ->
                                        viewModel.selectAnswer(
                                            questionId = question.id,
                                            answer = answer
                                        )
                                    }
                                )
                            }

                            item {
                                SubmitUnlockCard(
                                    isSubmitting = uiState.isSubmitting,
                                    answeredCount = uiState.selectedAnswers.size,
                                    totalCount = quiz.questions.size,
                                    onClick = viewModel::submitQuiz
                                )
                            }
                        }

                        result != null -> {
                            item {
                                UnlockResultCard(
                                    result = result,
                                    unlockedUntil = uiState.unlockedUntil,
                                    onRetryClick = viewModel::retryWithNewQuiz
                                )
                            }

                            if (result.reviewData.isNotEmpty()) {
                                item {
                                    SectionTitle(
                                        title = "Answer review",
                                        subtitle = "Understand what unlocked or blocked access."
                                    )
                                }

                                itemsIndexed(
                                    items = result.reviewData,
                                    key = { _, review ->
                                        review.id
                                    }
                                ) { index, reviewItem ->
                                    UnlockReviewCard(
                                        questionNumber = index + 1,
                                        review = reviewItem,
                                        selectedAnswer = uiState.selectedAnswers[reviewItem.id]
                                    )
                                }
                            }
                        }

                        else -> {
                            item {
                                AppAvailableCard()
                            }
                        }
                    }
                }
            }

            if (uiState.isSubmitting) {
                SubmittingUnlockOverlay()
            }
        }
    }
}

@Composable
private fun FocusShieldHeroCard(
    appName: String,
    packageName: String,
    isUnlocked: Boolean
) {
    val heroColor = if (isUnlocked) {
        RakizzColors.Success
    } else {
        RakizzColors.Primary
    }

    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 34.dp,
        glow = true,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            heroColor,
                            RakizzColors.PrimaryDark
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShieldPulseIcon(
                isUnlocked = isUnlocked,
                color = heroColor
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isUnlocked) {
                    "Temporary Access Granted"
                } else {
                    "App Locked by Focus Shield"
                },
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = appName,
                color = RakizzColors.White.copy(alpha = 0.94f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = packageName,
                color = RakizzColors.White.copy(alpha = 0.70f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WhitePill("AI quiz")
                WhitePill("70% pass")
                WhitePill("15 min access")
            }
        }
    }
}

@Composable
private fun ShieldPulseIcon(
    isUnlocked: Boolean,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "focus_shield_pulse"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shield_glow_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.46f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shield_glow_alpha"
    )

    Box(
        modifier = Modifier.size(98.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(98.dp)
                .graphicsLayer(
                    scaleX = glowScale,
                    scaleY = glowScale
                )
                .clip(CircleShape)
                .background(RakizzColors.White.copy(alpha = glowAlpha))
        )

        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = RakizzColors.White.copy(alpha = 0.18f),
            border = BorderStroke(
                width = 1.dp,
                color = RakizzColors.White.copy(alpha = 0.28f)
            )
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) {
                        Icons.Filled.LockOpen
                    } else {
                        Icons.Rounded.Security
                    },
                    contentDescription = null,
                    tint = RakizzColors.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}

@Composable
private fun UnlockQuizIntroCard(
    quiz: QuizDto,
    selectedCount: Int
) {
    val progress = if (quiz.questions.isEmpty()) {
        0f
    } else {
        selectedCount.toFloat() / quiz.questions.size.toFloat()
    }.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 420),
        label = "unlock_quiz_progress"
    )

    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        glow = false,
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = RakizzColors.PrimarySoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = RakizzColors.Primary.copy(alpha = 0.30f)
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = null,
                        tint = RakizzColors.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "AI Unlock Quiz",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "Pass this quiz to unlock the app temporarily.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RakizzStatusChip(
                text = "${quiz.questions.size} questions",
                color = RakizzColors.Primary,
                softColor = RakizzColors.PrimarySoft
            )

            RakizzStatusChip(
                text = "$selectedCount answered",
                color = RakizzColors.Accent,
                softColor = RakizzColors.AccentSoft
            )

            RakizzStatusChip(
                text = "70% pass",
                color = RakizzColors.Success,
                softColor = RakizzColors.Success.copy(alpha = 0.13f)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

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
private fun UnlockQuestionCard(
    questionNumber: Int,
    question: QuizQuestionDto,
    selectedAnswer: String?,
    onSelect: (String) -> Unit
) {
    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberBubble(
                number = questionNumber,
                color = RakizzColors.Primary
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
                    text = "Answer to request access",
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
            UnlockAnswerOption(
                optionLetter = ('A' + index).toString(),
                option = option,
                selected = selectedAnswer == option,
                onClick = {
                    onSelect(option)
                }
            )

            if (index != question.options.lastIndex) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun UnlockAnswerOption(
    optionLetter: String,
    option: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "unlock_answer_scale"
    )

    val borderColor = if (selected) {
        RakizzColors.Primary
    } else {
        RakizzColors.CardBorder
    }

    val backgroundColor = if (selected) {
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
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        shadowElevation = if (selected) {
            3.dp
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
private fun SubmitUnlockCard(
    isSubmitting: Boolean,
    answeredCount: Int,
    totalCount: Int,
    onClick: () -> Unit
) {
    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        glow = true,
        contentPadding = PaddingValues(18.dp)
    ) {
        Text(
            text = "Ready to request unlock?",
            color = RakizzColors.TextMain,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Submit your answers. Passing score is 70%.",
            color = RakizzColors.TextSecond,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        RakizzPrimaryButton(
            text = if (isSubmitting) {
                "Submitting..."
            } else {
                "Submit Unlock Quiz ($answeredCount/$totalCount)"
            },
            enabled = !isSubmitting,
            icon = Icons.Rounded.TaskAlt,
            onClick = onClick
        )
    }
}

@Composable
private fun UnlockResultCard(
    result: QuizAttemptResultDto,
    unlockedUntil: String?,
    onRetryClick: () -> Unit
) {
    val percent = (result.score * 100).toInt()
    val passed = result.passed

    val color = if (passed) {
        RakizzColors.Success
    } else {
        RakizzColors.Error
    }

    val progress = percent.coerceIn(0, 100).toFloat() / 100f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 700),
        label = "unlock_result_progress"
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
            ResultPulseIcon(
                passed = passed,
                color = color
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (passed) {
                    "Access Granted"
                } else {
                    "Access Denied"
                },
                color = color,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$percent%",
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
                    "The app is unlocked for a short time.\nUntil: ${unlockedUntil ?: "soon"}"
                } else {
                    "The app stays blocked. Review the explanations and try another quiz."
                },
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            if (!passed) {
                Spacer(modifier = Modifier.height(18.dp))

                RakizzPrimaryButton(
                    text = "Take Another Quiz",
                    icon = Icons.Rounded.Refresh,
                    onClick = onRetryClick
                )
            }
        }
    }
}

@Composable
private fun ResultPulseIcon(
    passed: Boolean,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "unlock_result_pulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "unlock_result_glow"
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
                    imageVector = if (passed) {
                        Icons.Filled.LockOpen
                    } else {
                        Icons.Filled.Close
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
private fun UnlockReviewCard(
    questionNumber: Int,
    review: QuizReviewItemDto,
    selectedAnswer: String?
) {
    val correct = selectedAnswer == review.correctAnswer
    val color = if (correct) {
        RakizzColors.Success
    } else {
        RakizzColors.Error
    }

    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberBubble(
                number = questionNumber,
                color = color
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (correct) {
                        "Correct"
                    } else {
                        "Needs Review"
                    },
                    color = color,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Unlock quiz explanation",
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
            text = review.questionText,
            color = RakizzColors.TextMain,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = MaterialTheme.typography.titleMedium.lineHeight
        )

        Spacer(modifier = Modifier.height(14.dp))

        AnswerLine(
            label = "Your answer",
            value = selectedAnswer ?: "No answer",
            color = color
        )

        Spacer(modifier = Modifier.height(10.dp))

        AnswerLine(
            label = "Correct answer",
            value = review.correctAnswer,
            color = RakizzColors.Success
        )

        Spacer(modifier = Modifier.height(14.dp))

        SourceBox(
            title = "Explanation",
            value = review.explanation.ifBlank {
                "No explanation was provided."
            },
            icon = Icons.Rounded.CheckCircle
        )

        if (!review.sourceChunkSnippet.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))

            SourceBox(
                title = "Source snippet",
                value = review.sourceChunkSnippet,
                icon = Icons.Rounded.Description
            )
        }
    }
}

@Composable
private fun AppAvailableCard() {
    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        glow = true,
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ResultPulseIcon(
                passed = true,
                color = RakizzColors.Success
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "App is Available",
                color = RakizzColors.Success,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This app is not blocked right now.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun UnlockLoadingCard() {
    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        glow = true,
        contentPadding = PaddingValues(22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = RakizzColors.Primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Preparing unlock quiz...",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Rakizz is checking the focus rule and generating the learning challenge.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}

@Composable
private fun StatusMessageCard(
    title: String,
    message: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (color == RakizzColors.Error) {
                    Icons.Rounded.ErrorOutline
                } else {
                    Icons.Rounded.AutoAwesome
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
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
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}

@Composable
private fun SubmittingUnlockOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RakizzColors.Background.copy(alpha = 0.97f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        RakizzGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 34.dp,
            glow = true,
            contentPadding = PaddingValues(26.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShieldPulseIcon(
                    isUnlocked = false,
                    color = RakizzColors.Primary
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Checking Unlock Request",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rakizz is grading your answers and deciding whether temporary access can be granted.",
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
private fun NumberBubble(
    number: Int,
    color: Color
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.30f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun AnswerLine(
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
    value: String,
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
                    text = title.uppercase(),
                    color = RakizzColors.Primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = value,
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
private fun WhitePill(
    text: String
) {
    Box(
        modifier = Modifier
            .background(
                color = RakizzColors.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
    ) {
        Text(
            text = text,
            color = RakizzColors.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}