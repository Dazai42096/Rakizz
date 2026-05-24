package com.rakizz.student.presentation.unlock

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.data.remote.dto.QuizAttemptResultDto
import com.rakizz.student.data.remote.dto.QuizDto
import com.rakizz.student.data.remote.dto.QuizQuestionDto
import com.rakizz.student.data.remote.dto.QuizReviewItemDto
import com.rakizz.student.presentation.theme.RakizzColors

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

    Scaffold(
        containerColor = RakizzColors.Background
    ) { paddingValues ->
        LazyColumn(
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
                    onBackClick = onBackClick
                )
            }

            item {
                LockedAppHeroCard(
                    appName = uiState.appName.ifBlank { packageName },
                    packageName = packageName,
                    isUnlocked = result?.passed == true || uiState.unlockedUntil != null
                )
            }

            uiState.error?.let { error ->
                item {
                    MessageCard(
                        title = "Error",
                        message = error,
                        color = RakizzColors.Error,
                        onClick = viewModel::clearError
                    )
                }
            }

            uiState.message?.let { message ->
                item {
                    MessageCard(
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
                        LoadingCard()
                    }
                }

                quiz != null && result == null -> {
                    item {
                        QuizIntroCard(
                            quiz = quiz,
                            selectedCount = uiState.selectedAnswers.size
                        )
                    }

                    itemsIndexed(
                        items = quiz.questions,
                        key = { _, question -> question.id }
                    ) { index, question ->
                        QuestionCard(
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
                        SubmitButton(
                            isSubmitting = uiState.isSubmitting,
                            answeredCount = uiState.selectedAnswers.size,
                            totalCount = quiz.questions.size,
                            onClick = viewModel::submitQuiz
                        )
                    }
                }

                result != null -> {
                    item {
                        ResultCard(
                            result = result,
                            unlockedUntil = uiState.unlockedUntil,
                            onRetryClick = viewModel::retryWithNewQuiz
                        )
                    }

                    if (result.reviewData.isNotEmpty()) {
                        item {
                            SectionTitle(
                                title = "Answer review",
                                subtitle = "Check your answers, correct answer, and explanation."
                            )
                        }

                        itemsIndexed(
                            items = result.reviewData,
                            key = { _, review -> review.id }
                        ) { index, reviewItem ->
                            ReviewCard(
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
}

@Composable
private fun TopBar(
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
                text = "Unlock Required",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Pass the quiz to use this app.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LockedAppHeroCard(
    appName: String,
    packageName: String,
    isUnlocked: Boolean
) {
    val heroColor = if (isUnlocked) {
        RakizzColors.Success
    } else {
        RakizzColors.Primary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = heroColor,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            heroColor,
                            RakizzColors.PrimaryDark
                        )
                    )
                )
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = RakizzColors.White.copy(alpha = 0.16f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Filled.LockOpen else Icons.Filled.Block,
                        contentDescription = null,
                        tint = RakizzColors.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isUnlocked) "Temporary access granted" else "App is locked",
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = appName,
                color = RakizzColors.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = packageName,
                color = RakizzColors.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

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
private fun QuizIntroCard(
    quiz: QuizDto,
    selectedCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBubble(
                    icon = Icons.Filled.Description,
                    color = RakizzColors.Primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "AI Unlock Quiz",
                        color = RakizzColors.TextMain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Generated from the student's uploaded material.",
                        color = RakizzColors.TextSecond,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoBox(
                label = "Questions",
                value = quiz.questions.size.toString()
            )

            Spacer(modifier = Modifier.height(10.dp))

            InfoBox(
                label = "Answered",
                value = "$selectedCount / ${quiz.questions.size}"
            )

            Spacer(modifier = Modifier.height(10.dp))

            InfoBox(
                label = "Passing score",
                value = "70%"
            )
        }
    }
}

@Composable
private fun QuestionCard(
    questionNumber: Int,
    question: QuizQuestionDto,
    selectedAnswer: String?,
    onSelect: (String) -> Unit
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
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RakizzColors.PrimarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = questionNumber.toString(),
                        color = RakizzColors.Primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

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
                AnswerOption(
                    option = option,
                    selected = selectedAnswer == option,
                    onClick = {
                        onSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun AnswerOption(
    option: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) {
        RakizzColors.PrimarySoft
    } else {
        RakizzColors.CardSoft
    }

    val borderColor = if (selected) {
        RakizzColors.Primary
    } else {
        RakizzColors.CardBorder
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
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
private fun SubmitButton(
    isSubmitting: Boolean,
    answeredCount: Int,
    totalCount: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isSubmitting,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
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

            Spacer(modifier = Modifier.width(10.dp))
        }

        Text(
            text = if (isSubmitting) {
                "Submitting..."
            } else {
                "Submit Unlock Quiz ($answeredCount/$totalCount)"
            },
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ResultCard(
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
            IconBubbleLarge(
                icon = if (passed) Icons.Filled.LockOpen else Icons.Filled.Close,
                color = color
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (passed) "App unlocked" else "Quiz not passed",
                color = color,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Score: $percent%",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (passed) {
                    "The app is unlocked for a short time.\nUntil: ${unlockedUntil ?: "soon"}"
                } else {
                    "The app stays blocked.\nTake another quiz to unlock it."
                },
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            if (!passed) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RakizzColors.Primary,
                        contentColor = RakizzColors.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Take Another Quiz",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
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
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = questionNumber.toString(),
                        color = color,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (correct) "Correct" else "Needs review",
                    color = color,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = review.questionText,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = MaterialTheme.typography.titleMedium.lineHeight
            )

            AnswerLine(
                label = "Your answer",
                value = selectedAnswer ?: "No answer",
                color = color
            )

            AnswerLine(
                label = "Correct answer",
                value = review.correctAnswer,
                color = RakizzColors.Success
            )

            SourceBox(
                title = "Explanation",
                value = review.explanation.ifBlank {
                    "No explanation was provided."
                }
            )

            if (!review.sourceChunkSnippet.isNullOrBlank()) {
                SourceBox(
                    title = "Source snippet",
                    value = review.sourceChunkSnippet
                )
            }
        }
    }
}

@Composable
private fun AppAvailableCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.Success.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBubbleLarge(
                icon = Icons.Filled.LockOpen,
                color = RakizzColors.Success
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "App is available",
                color = RakizzColors.Success,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

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
    value: String
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
            Text(
                text = title.uppercase(),
                color = RakizzColors.Primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

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
private fun InfoBox(
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = RakizzColors.CardSoft,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold
            )
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
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
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = RakizzColors.Primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Preparing unlock quiz...",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Rakizz is checking the blocked app rule.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
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
private fun IconBubble(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.13f)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun IconBubbleLarge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier.size(76.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.13f)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(36.dp)
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