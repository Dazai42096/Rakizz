package com.rakizz.student.presentation.quizzes.list

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rakizz.student.domain.model.Quiz
import com.rakizz.student.presentation.common.UiState
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Suppress("UNUSED_PARAMETER")
@Composable
fun QuizzesScreen(
    navController: NavController? = null,
    viewModel: QuizzesViewModel? = null,
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onQuizClick: (String) -> Unit = {},
    onQuizSelected: (String) -> Unit = {},
    onOpenQuizClick: (String) -> Unit = {},
    onOpenQuizDetailClick: (String) -> Unit = {},
    onStartQuizClick: (String) -> Unit = {},
    onNavigateToQuizDetail: (String) -> Unit = {},
    onGenerateQuizClick: () -> Unit = {},
    onCreateQuizClick: () -> Unit = {},
    onNewQuizClick: () -> Unit = {},
    onOpenQuizSetupClick: () -> Unit = {},
    onNavigateToQuizSetup: () -> Unit = {},
    onMaterialsClick: () -> Unit = {},
    onGoToMaterialsClick: () -> Unit = {},
    onOpenMaterialsClick: () -> Unit = {},
    onOpenHome: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onRefreshClick: () -> Unit = {}
) {
    val colors = quizzesScreenColors()
    val quizzesViewModel = viewModel ?: hiltViewModel()
    val uiState by quizzesViewModel.uiState.collectAsState()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    var selectedFilter by remember { mutableStateOf(QuizFilter.ALL) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            delay(2200)
            message = ""
        }
    }

    val quizzes = when (val state = uiState) {
        is UiState.Success -> state.data
        else -> emptyList()
    }

    val completedQuizzes = quizzes.filter { it.score != null }
    val readyQuizzes = quizzes.filter { it.score == null }

    val completedCount = completedQuizzes.size
    val readyCount = readyQuizzes.size
    val totalQuizzes = quizzes.size

    val averageAccuracy = if (completedQuizzes.isNotEmpty()) {
        completedQuizzes
            .mapNotNull { it.score }
            .average()
            .roundToInt()
            .coerceIn(0, 100)
    } else {
        0
    }

    val filteredQuizzes = when (selectedFilter) {
        QuizFilter.ALL -> quizzes
        QuizFilter.READY -> readyQuizzes
        QuizFilter.COMPLETED -> completedQuizzes
    }

    val infiniteTransition = rememberInfiniteTransition(label = "quizzes_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "quizzes_glow"
    )

    fun goBack() {
        if (navController != null) {
            val popped = navController.popBackStack()
            if (!popped) {
                onOpenHome()
            }
        } else {
            onBackClick()
            onNavigateBack()
            backDispatcher?.onBackPressed()
        }
    }

    fun openQuiz(quizId: String) {
        onQuizClick(quizId)
        onQuizSelected(quizId)
        onOpenQuizClick(quizId)
        onOpenQuizDetailClick(quizId)
        onStartQuizClick(quizId)
        onNavigateToQuizDetail(quizId)
        onNavigateToDetail(quizId)
    }

    fun openMaterialsForQuizGeneration() {
        message = "Choose a material before generating a quiz."
        onMaterialsClick()
        onGoToMaterialsClick()
        onOpenMaterialsClick()
        onOpenLibrary()
        onGenerateQuizClick()
        onCreateQuizClick()
        onNewQuizClick()
        onOpenQuizSetupClick()
        onNavigateToQuizSetup()
    }

    fun openMaterials() {
        onMaterialsClick()
        onGoToMaterialsClick()
        onOpenMaterialsClick()
        onOpenLibrary()
    }

    fun refreshQuizzes() {
        message = "Refreshing quizzes..."
        quizzesViewModel.loadQuizzes()
        onRefreshClick()
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
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = (-120).dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                    alpha = 0.85f
                }
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.42f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 20.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            QuizzesTopBar(
                colors = colors,
                onBackClick = { goBack() },
                onRefreshClick = { refreshQuizzes() }
            )

            Spacer(modifier = Modifier.height(22.dp))

            QuizHeroCard(
                averageAccuracy = averageAccuracy,
                totalQuizzes = totalQuizzes,
                readyCount = readyCount,
                completedCount = completedCount,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = message.isNotEmpty()) {
                QuizMessageCard(
                    message = message,
                    colors = colors
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuizSummaryCard(
                    title = "Ready",
                    value = readyCount.toString(),
                    subtitle = "not solved",
                    iconText = "Q",
                    mainColor = colors.primary,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                QuizSummaryCard(
                    title = "Accuracy",
                    value = "$averageAccuracy%",
                    subtitle = "completed only",
                    iconText = "%",
                    mainColor = colors.success,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                QuizSummaryCard(
                    title = "Done",
                    value = completedCount.toString(),
                    subtitle = "scored",
                    iconText = "D",
                    mainColor = colors.accent,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            QuizActionCard(
                colors = colors,
                onGenerateQuizClick = { openMaterialsForQuizGeneration() },
                onMaterialsClick = { openMaterials() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilterCard(
                selectedFilter = selectedFilter,
                colors = colors,
                onFilterSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                UiState.Loading -> {
                    LoadingQuizCard(colors = colors)
                }

                UiState.Empty -> {
                    QuizzesSection(
                        title = "Quiz Library",
                        subtitle = "Generated quizzes from uploaded materials",
                        quizzes = emptyList(),
                        colors = colors,
                        emptyText = "No quizzes found yet. Open Materials and generate a quiz first.",
                        onQuizClick = { quiz -> openQuiz(quiz.id) }
                    )
                }

                is UiState.Error -> {
                    ErrorQuizCard(
                        message = cleanQuizError(state.message),
                        colors = colors,
                        onRetryClick = { quizzesViewModel.loadQuizzes() }
                    )
                }

                is UiState.Success -> {
                    QuizzesSection(
                        title = "Quiz Library",
                        subtitle = "Generated quizzes from uploaded materials",
                        quizzes = filteredQuizzes,
                        colors = colors,
                        emptyText = "No quizzes found for this filter.",
                        onQuizClick = { quiz -> openQuiz(quiz.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            QuizNavigationCard(
                colors = colors,
                onOpenHome = onOpenHome,
                onOpenLibrary = { openMaterials() },
                onOpenFocus = onOpenFocus,
                onOpenProfile = onOpenProfile
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuizFlowExplanationCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun QuizzesTopBar(
    colors: QuizzesScreenColors,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleIconButton(
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
                text = "AI Quizzes",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Practice from your uploaded study materials",
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        CircleIconButton(
            text = "R",
            colors = colors,
            onClick = onRefreshClick
        )
    }
}

@Composable
private fun QuizHeroCard(
    averageAccuracy: Int,
    totalQuizzes: Int,
    readyCount: Int,
    completedCount: Int,
    colors: QuizzesScreenColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.30f),
                            colors.card,
                            colors.cardAlt
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = colors.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.32f))
                ) {
                    Text(
                        text = "AI Quiz Engine",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Turn materials into smart practice sessions.",
                    color = colors.textPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 31.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$totalQuizzes quizzes available - $readyCount ready - $completedCount completed - $averageAccuracy% average accuracy",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                AccuracyProgressBar(averageAccuracy, colors)
            }
        }
    }
}

@Composable
private fun AccuracyProgressBar(
    progress: Int,
    colors: QuizzesScreenColors
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Learning accuracy",
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$progress%",
                color = colors.success,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(colors.track)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0, 100) / 100f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(colors.success, colors.accent)
                        )
                    )
            )
        }
    }
}

@Composable
private fun QuizMessageCard(
    message: String,
    colors: QuizzesScreenColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.success.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, colors.success.copy(alpha = 0.32f))
    ) {
        Text(
            text = message,
            color = colors.success,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(14.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuizSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    iconText: String,
    mainColor: Color,
    colors: QuizzesScreenColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardAlt),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(mainColor.copy(alpha = 0.16f))
                    .border(1.dp, mainColor.copy(alpha = 0.30f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    color = mainColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = mainColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuizActionCard(
    colors: QuizzesScreenColors,
    onGenerateQuizClick: () -> Unit,
    onMaterialsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Quick Quiz Actions",
                subtitle = "Choose a material before generating a new quiz",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            MainGradientButton(
                text = "Generate New Quiz",
                colors = colors,
                onClick = onGenerateQuizClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryQuizButton(
                text = "Open Materials Library",
                subtitle = "Select a material and generate a quiz from it",
                iconText = "M",
                colors = colors,
                onClick = onMaterialsClick
            )
        }
    }
}

@Composable
private fun FilterCard(
    selectedFilter: QuizFilter,
    colors: QuizzesScreenColors,
    onFilterSelected: (QuizFilter) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Filter Quizzes",
                subtitle = "Show quizzes by real completion status",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuizFilter.values().forEach { filter ->
                    FilterChip(
                        filter = filter,
                        selected = selectedFilter == filter,
                        colors = colors,
                        modifier = Modifier.weight(1f),
                        onClick = { onFilterSelected(filter) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    filter: QuizFilter,
    selected: Boolean,
    colors: QuizzesScreenColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.primary.copy(alpha = 0.18f) else colors.cardAlt,
        border = BorderStroke(
            1.dp,
            if (selected) colors.primary.copy(alpha = 0.45f) else colors.border
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = filter.label,
                color = if (selected) colors.primary else colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuizzesSection(
    title: String,
    subtitle: String,
    quizzes: List<Quiz>,
    colors: QuizzesScreenColors,
    emptyText: String,
    onQuizClick: (Quiz) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(title, subtitle, colors)

            Spacer(modifier = Modifier.height(14.dp))

            if (quizzes.isEmpty()) {
                EmptyQuizBox(emptyText, colors)
            } else {
                quizzes.forEachIndexed { index, quiz ->
                    QuizCard(
                        quiz = quiz,
                        colors = colors,
                        onClick = { onQuizClick(quiz) }
                    )

                    if (index != quizzes.lastIndex) {
                        HorizontalDivider(
                            color = colors.border,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizCard(
    quiz: Quiz,
    colors: QuizzesScreenColors,
    onClick: () -> Unit
) {
    val isCompleted = quiz.score != null
    val statusColor = if (isCompleted) colors.success else colors.primary
    val statusText = if (isCompleted) "Done" else "Ready"
    val scoreText = if (isCompleted) "${quiz.score?.coerceIn(0, 100)}%" else "Not solved"
    val questionText = if (quiz.totalQuestions == 1) {
        "1 question"
    } else {
        "${quiz.totalQuestions} questions"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .border(1.dp, statusColor.copy(alpha = 0.32f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isCompleted) "D" else "Q",
                color = statusColor,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = materialLabel(quiz.materialId),
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )

                StatusPill(statusText, statusColor)
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = quiz.title,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "$questionText - $scoreText",
                color = colors.textSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            MiniAccuracyBar(
                progress = quiz.score ?: 0,
                color = statusColor,
                colors = colors
            )
        }
    }
}

@Composable
private fun LoadingQuizCard(colors: QuizzesScreenColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = "Loading quizzes...",
            color = colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(22.dp)
        )
    }
}

@Composable
private fun ErrorQuizCard(
    message: String,
    colors: QuizzesScreenColors,
    onRetryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Could not load quizzes",
                color = colors.error,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = colors.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            MainGradientButton(
                text = "Try Again",
                colors = colors,
                onClick = onRetryClick
            )
        }
    }
}

@Composable
private fun QuizNavigationCard(
    colors: QuizzesScreenColors,
    onOpenHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Quick Navigation",
                subtitle = "Move to other important Rakizz screens",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryQuizButton(
                text = "Home Dashboard",
                subtitle = "Return to the main student dashboard",
                iconText = "H",
                colors = colors,
                onClick = onOpenHome
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryQuizButton(
                text = "Materials Library",
                subtitle = "Open uploaded materials used by AI quizzes",
                iconText = "M",
                colors = colors,
                onClick = onOpenLibrary
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryQuizButton(
                text = "Focus Mode",
                subtitle = "Open focus mode and quiz unlock flow",
                iconText = "F",
                colors = colors,
                onClick = onOpenFocus
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryQuizButton(
                text = "Student Profile",
                subtitle = "Open account and pair code settings",
                iconText = "P",
                colors = colors,
                onClick = onOpenProfile
            )
        }
    }
}

@Composable
private fun MiniAccuracyBar(
    progress: Int,
    color: Color,
    colors: QuizzesScreenColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(colors.track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0, 100) / 100f)
                .height(8.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(color)
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun EmptyQuizBox(
    text: String,
    colors: QuizzesScreenColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardAlt,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = text,
            color = colors.textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Composable
private fun QuizFlowExplanationCard(colors: QuizzesScreenColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.success.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, colors.success.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "OK",
                color = colors.success,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "How Rakizz works",
                    color = colors.success,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "This screen shows real stored quizzes from the backend. Ready means not submitted yet. Done means a real score exists.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun MainGradientButton(
    text: String,
    colors: QuizzesScreenColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.primary, colors.accent)
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun SecondaryQuizButton(
    text: String,
    subtitle: String,
    iconText: String,
    colors: QuizzesScreenColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardAlt,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(colors.primary.copy(alpha = 0.14f))
                    .border(1.dp, colors.primary.copy(alpha = 0.28f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    color = colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = text,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Text(
                text = "Open",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    colors: QuizzesScreenColors
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
private fun CircleIconButton(
    text: String,
    colors: QuizzesScreenColors,
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

private fun materialLabel(materialId: String): String {
    return if (materialId.isBlank()) {
        "Uploaded material"
    } else {
        "Material ${materialId.take(8)}"
    }
}

private fun cleanQuizError(message: String): String {
    val clean = message.lowercase()

    return when {
        "401" in clean || "403" in clean || "unauthorized" in clean -> {
            "Your login session may have expired. Please login again."
        }

        "network" in clean ||
            "timeout" in clean ||
            "failed to connect" in clean ||
            "unable to resolve host" in clean -> {
            "Unable to reach Rakizz server. Check your connection and try again."
        }

        "500" in clean ||
            "502" in clean ||
            "503" in clean ||
            "504" in clean -> {
            "Rakizz server is temporarily unavailable. Please try again."
        }

        else -> {
            "Failed to load quizzes. Please try again."
        }
    }
}

@Composable
private fun quizzesScreenColors(): QuizzesScreenColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        QuizzesScreenColors(
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
            warning = Color(0xFFF59E0B),
            error = Color(0xFFEF4444),
            textPrimary = Color.White,
            textSecondary = Color(0xFF94A3B8)
        )
    } else {
        QuizzesScreenColors(
            backgroundTop = Color(0xFFF8FBFF),
            backgroundMiddle = Color(0xFFEAF4FF),
            backgroundBottom = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            cardAlt = Color(0xFFF1F7FF),
            track = Color(0xFFE2E8F0),
            border = Color(0x263B82F6),
            primary = Color(0xFF2563EB),
            accent = Color(0xFF06B6D4),
            success = Color(0xFF16A34A),
            warning = Color(0xFFD97706),
            error = Color(0xFFDC2626),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B)
        )
    }
}

private enum class QuizFilter(val label: String) {
    ALL("All"),
    READY("Ready"),
    COMPLETED("Done")
}

private data class QuizzesScreenColors(
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
    val warning: Color,
    val error: Color,
    val textPrimary: Color,
    val textSecondary: Color
)