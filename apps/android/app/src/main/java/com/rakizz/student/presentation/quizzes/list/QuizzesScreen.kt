package com.rakizz.student.presentation.quizzes.list

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.theme.RakizzColors

@Composable
fun QuizzesScreen(
    onNavigateToDetail: (String) -> Unit,
    onOpenHome: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    viewModel: QuizzesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = RakizzColors.Background,
        bottomBar = {
            QuizzesBottomBar(
                onOpenHome = onOpenHome,
                onOpenLibrary = onOpenLibrary,
                onOpenFocus = onOpenFocus,
                onOpenProfile = onOpenProfile
            )
        }
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
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 14.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                QuizzesHeader()
            }

            item {
                AiQuizInfoCard()
            }

            when (val state = uiState) {
                is UiState.Loading -> {
                    item {
                        SectionTitle(
                            title = "Generated quizzes",
                            subtitle = "Loading your saved AI quizzes..."
                        )
                    }

                    items(4) {
                        LoadingQuizCard()
                    }
                }

                is UiState.Empty -> {
                    item {
                        SectionTitle(
                            title = "Generated quizzes",
                            subtitle = "No quizzes generated yet"
                        )
                    }

                    item {
                        EmptyQuizzesCard(
                            onOpenLibrary = onOpenLibrary
                        )
                    }
                }

                is UiState.Error -> {
                    item {
                        SectionTitle(
                            title = "Generated quizzes",
                            subtitle = "Something went wrong"
                        )
                    }

                    item {
                        ErrorCard(
                            message = state.message,
                            onRetryClick = {
                                viewModel.loadQuizzes()
                            }
                        )
                    }
                }

                is UiState.Success -> {
                    item {
                        QuizSummaryCard(
                            quizzes = state.data
                        )
                    }

                    item {
                        SectionTitle(
                            title = "Generated quizzes",
                            subtitle = "${state.data.size} saved quiz item(s)"
                        )
                    }

                    if (state.data.isEmpty()) {
                        item {
                            EmptyQuizzesCard(
                                onOpenLibrary = onOpenLibrary
                            )
                        }
                    } else {
                        items(
                            items = state.data,
                            key = { quiz -> quiz.id }
                        ) { quiz ->
                            QuizListCard(
                                quiz = quiz,
                                onClick = {
                                    onNavigateToDetail(quiz.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizzesHeader() {
    Column {
        Text(
            text = "AI Quizzes",
            color = RakizzColors.TextMain,
            fontSize = 29.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Review quizzes generated from uploaded study materials.",
            color = RakizzColors.TextSecond,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AiQuizInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Primary,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            RakizzColors.Primary,
                            RakizzColors.PrimaryDark
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "Mixed AI quiz generation",
                color = RakizzColors.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Rakizz reads uploaded materials and generates a mixed quiz with easy, medium, and hard questions together.",
                color = RakizzColors.White.copy(alpha = 0.88f),
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LightPill("Source based")
                LightPill("20 questions")
                LightPill("70% pass")
            }
        }
    }
}

@Composable
private fun LightPill(
    text: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(com.rakizz.student.presentation.theme.RakizzColors.White.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = RakizzColors.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun QuizSummaryCard(
    quizzes: List<Quiz>
) {
    val totalQuestions = quizzes.sumOf { it.totalQuestions }
    val attempted = quizzes.count { it.score != null }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = RakizzColors.CardBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryItem(
                modifier = Modifier.weight(1f),
                value = quizzes.size.toString(),
                label = "Quizzes"
            )

            SummaryItem(
                modifier = Modifier.weight(1f),
                value = totalQuestions.toString(),
                label = "Questions"
            )

            SummaryItem(
                modifier = Modifier.weight(1f),
                value = attempted.toString(),
                label = "Attempted"
            )
        }
    }
}

@Composable
private fun SummaryItem(
    modifier: Modifier,
    value: String,
    label: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = RakizzColors.Primary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            color = RakizzColors.TextSecond,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
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
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            color = RakizzColors.TextSecond,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun QuizListCard(
    quiz: Quiz,
    onClick: () -> Unit
) {
    val label = quizLabel(quiz.totalQuestions)
    val accent = quizAccent(quiz.totalQuestions)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = RakizzColors.CardBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI",
                        color = accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        color = RakizzColors.TextMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Generated from uploaded material",
                        color = RakizzColors.TextSecond,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "Open",
                    color = RakizzColors.Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallPill(
                    text = "${quiz.totalQuestions} questions",
                    color = accent
                )

                SmallPill(
                    text = scoreText(quiz),
                    color = if (quiz.score != null) {
                        RakizzColors.Success
                    } else {
                        RakizzColors.TextSecond
                    }
                )
            }
        }
    }
}

@Composable
private fun SmallPill(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun EmptyQuizzesCard(
    onOpenLibrary: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = RakizzColors.CardBorder,
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No quizzes yet",
                color = RakizzColors.TextMain,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Open a material from the Library and generate your first mixed AI quiz.",
                color = RakizzColors.TextSecond,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenLibrary,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White
                )
            ) {
                Text(
                    text = "Go to Library",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetryClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = RakizzColors.Error.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(18.dp)
        ) {
            Text(
                text = "Could not load quizzes",
                color = RakizzColors.Error,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                color = RakizzColors.TextSecond,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onRetryClick,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White
                )
            ) {
                Text(
                    text = "Try Again",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun LoadingQuizCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = RakizzColors.CardBorder,
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = RakizzColors.Primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Loading quizzes...",
                color = RakizzColors.TextSecond,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuizzesBottomBar(
    onOpenHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        color = RakizzColors.Card,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .border(1.dp, RakizzColors.CardBorder)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                selected = false,
                onClick = onOpenHome
            )

            BottomNavItem(
                label = "Library",
                selected = false,
                onClick = onOpenLibrary
            )

            BottomNavItem(
                label = "Quizzes",
                selected = true,
                onClick = {}
            )

            BottomNavItem(
                label = "Focus",
                selected = false,
                onClick = onOpenFocus
            )

            BottomNavItem(
                label = "Profile",
                selected = false,
                onClick = onOpenProfile
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (selected) {
        RakizzColors.Primary
    } else {
        RakizzColors.TextMuted
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(textColor)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (selected) {
                FontWeight.ExtraBold
            } else {
                FontWeight.Bold
            }
        )
    }
}

private fun quizLabel(
    count: Int
): String {
    return when {
        count >= 20 -> "Mixed AI Quiz"
        count >= 10 -> "AI Quiz"
        else -> "Saved Quiz"
    }
}

private fun quizAccent(
    count: Int
): Color {
    return when {
        count >= 20 -> RakizzColors.Primary
        count >= 10 -> RakizzColors.Warning
        else -> RakizzColors.TextSecond
    }
}

private fun scoreText(
    quiz: Quiz
): String {
    val score = quiz.score

    return if (score == null) {
        "Not attempted"
    } else {
        "Score $score%"
    }
}
