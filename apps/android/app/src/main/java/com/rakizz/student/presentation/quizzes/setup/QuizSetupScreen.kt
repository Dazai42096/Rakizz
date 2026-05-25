package com.rakizz.student.presentation.quizzes.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Material
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.RakizzAnimatedTopBar
import com.rakizz.student.presentation.common.components.RakizzGlassCard
import com.rakizz.student.presentation.common.components.RakizzPrimaryButton
import com.rakizz.student.presentation.common.components.RakizzStatusChip
import com.rakizz.student.presentation.theme.RakizzColors
import kotlinx.coroutines.delay

@Composable
fun QuizSetupScreen(
    materialId: String,
    onBackClick: () -> Unit,
    onQuizGenerated: (String) -> Unit,
    viewModel: QuizSetupViewModel = hiltViewModel()
) {
    val materialState by viewModel.materialState.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generatedQuizId by viewModel.generatedQuizId.collectAsState()
    val generateMessage by viewModel.generateMessage.collectAsState()

    LaunchedEffect(materialId) {
        viewModel.loadMaterial(materialId)
    }

    LaunchedEffect(generatedQuizId) {
        val quizId = generatedQuizId

        if (!quizId.isNullOrBlank()) {
            onQuizGenerated(quizId)
            viewModel.clearGeneratedQuiz()
        }
    }

    LaunchedEffect(generateMessage, isGenerating) {
        if (!isGenerating && !generateMessage.isNullOrBlank()) {
            delay(3000)
            viewModel.clearGenerateMessage()
        }
    }

    when (val state = materialState) {
        is UiState.Loading -> {
            QuizSetupLoadingScreen()
        }

        is UiState.Error -> {
            QuizSetupErrorScreen(
                message = state.message,
                onBackClick = onBackClick
            )
        }

        is UiState.Success -> {
            QuizSetupContent(
                material = state.data,
                isGenerating = isGenerating,
                generateMessage = generateMessage,
                onBackClick = onBackClick,
                onGenerateClick = {
                    viewModel.generateQuiz(materialId)
                }
            )
        }

        else -> Unit
    }
}

@Composable
private fun QuizSetupContent(
    material: Material,
    isGenerating: Boolean,
    generateMessage: String?,
    onBackClick: () -> Unit,
    onGenerateClick: () -> Unit
) {
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RakizzAnimatedTopBar(
                            title = "AI Quiz Studio",
                            subtitle = "Turn this material into a mixed AI quiz.",
                            showBackButton = true,
                            onBackClick = onBackClick,
                            trailingContent = {
                                RakizzStatusChip(
                                    text = "AI",
                                    icon = Icons.Rounded.AutoAwesome
                                )
                            }
                        )

                        SelectedMaterialCard(
                            material = material
                        )

                        AiTutorPlanCard()

                        GenerationStepsCard()

                        if (!generateMessage.isNullOrBlank() && !isGenerating) {
                            MessageCard(
                                message = generateMessage,
                                isError = !generateMessage.contains(
                                    "successfully",
                                    ignoreCase = true
                                )
                            )
                        }

                        RakizzPrimaryButton(
                            text = if (isGenerating) {
                                "Generating..."
                            } else {
                                "Generate Mixed AI Quiz"
                            },
                            enabled = !isGenerating,
                            icon = Icons.Filled.AutoAwesome,
                            onClick = onGenerateClick
                        )

                        Text(
                            text = "Rakizz automatically mixes easy, medium, and hard questions. The student does not choose the level.",
                            modifier = Modifier.fillMaxWidth(),
                            color = RakizzColors.TextMuted,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        if (isGenerating) {
            AiGenerationOverlay(
                materialTitle = material.title
            )
        }
    }
}

@Composable
private fun SelectedMaterialCard(
    material: Material
) {
    val descriptionText = material.description.ifBlank {
        "Stored study material selected from your library."
    }

    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        glow = true,
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(78.dp),
                shape = RoundedCornerShape(24.dp),
                color = RakizzColors.PrimarySoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = RakizzColors.Primary.copy(alpha = 0.32f)
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        tint = RakizzColors.Primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                RakizzStatusChip(
                    text = inferTypeLabel(material),
                    color = RakizzColors.Accent,
                    softColor = RakizzColors.AccentSoft
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = material.title,
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = descriptionText,
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
private fun AiTutorPlanCard() {
    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            AiOrb(
                size = 58.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Rakizz AI Tutor plan",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The AI will read the selected material, extract the important concepts, and create a mixed quiz that checks understanding instead of memorization only.",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RakizzStatusChip(
                        text = "Mixed",
                        color = RakizzColors.Primary,
                        softColor = RakizzColors.PrimarySoft
                    )

                    RakizzStatusChip(
                        text = "Personalized",
                        color = RakizzColors.Success,
                        softColor = RakizzColors.Success.copy(alpha = 0.13f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GenerationStepsCard() {
    RakizzGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        contentPadding = PaddingValues(20.dp)
    ) {
        Text(
            text = "Generation pipeline",
            color = RakizzColors.TextMain,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        GenerationStepRow(
            number = "1",
            title = "Read material",
            subtitle = "Scan the uploaded file or saved material.",
            icon = Icons.Rounded.Description
        )

        GenerationStepRow(
            number = "2",
            title = "Extract concepts",
            subtitle = "Find key terms, explanations, and study points.",
            icon = Icons.Rounded.Psychology
        )

        GenerationStepRow(
            number = "3",
            title = "Mix difficulty",
            subtitle = "Balance easy, medium, and hard questions.",
            icon = Icons.Rounded.Tune
        )

        GenerationStepRow(
            number = "4",
            title = "Build quiz",
            subtitle = "Create MCQs with correct answers and explanations.",
            icon = Icons.Rounded.Summarize
        )
    }
}

@Composable
private fun GenerationStepRow(
    number: String,
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = RakizzColors.PrimarySoft,
            border = BorderStroke(
                width = 1.dp,
                color = RakizzColors.Primary.copy(alpha = 0.25f)
            )
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RakizzColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = number,
                    color = RakizzColors.Primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean
) {
    val cardColor = if (isError) {
        RakizzColors.Error.copy(alpha = 0.13f)
    } else {
        RakizzColors.Success.copy(alpha = 0.13f)
    }

    val borderColor = if (isError) {
        RakizzColors.Error.copy(alpha = 0.45f)
    } else {
        RakizzColors.Success.copy(alpha = 0.45f)
    }

    val textColor = if (isError) {
        RakizzColors.Error
    } else {
        RakizzColors.Success
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = cardColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AiGenerationOverlay(
    materialTitle: String
) {
    var currentStep by remember {
        mutableIntStateOf(0)
    }

    val steps = listOf(
        "Reading material...",
        "Extracting key ideas...",
        "Creating mixed questions...",
        "Writing explanations...",
        "Finalizing quiz..."
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(900)
            currentStep = (currentStep + 1) % steps.size
        }
    }

    val progress = (currentStep + 1).toFloat() / steps.size.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RakizzColors.Background.copy(alpha = 0.97f))
            .padding(horizontal = 26.dp),
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
                AiOrb(
                    size = 94.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Generating AI Quiz",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = materialTitle.take(55),
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = steps[currentStep],
                    color = RakizzColors.Primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = {
                        progress.coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = RakizzColors.Primary,
                    trackColor = RakizzColors.PrimarySoft
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Please wait. Rakizz is building questions, answers, and explanations from your selected material.",
                    color = RakizzColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }
    }
}

@Composable
private fun AiOrb(
    size: androidx.compose.ui.unit.Dp
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "ai_orb_transition"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 950,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_orb_glow_alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_orb_scale"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            RakizzColors.Primary.copy(alpha = glowAlpha * 0.42f),
                            RakizzColors.PrimarySoft.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        Surface(
            modifier = Modifier.size(size * 0.66f),
            shape = CircleShape,
            color = RakizzColors.Primary,
            shadowElevation = 8.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = RakizzColors.White,
                    modifier = Modifier.size(size * 0.32f)
                )
            }
        }
    }
}

@Composable
private fun QuizSetupLoadingScreen() {
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = RakizzColors.Primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Loading material...",
                    color = RakizzColors.TextSecond,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuizSetupErrorScreen(
    message: String,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = RakizzColors.Background
    ) { paddingValues ->
        Column(
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
                .navigationBarsPadding()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            RakizzAnimatedTopBar(
                title = "AI Quiz Studio",
                subtitle = "Could not load this material.",
                showBackButton = true,
                onBackClick = onBackClick
            )

            RakizzGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                contentPadding = PaddingValues(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = RakizzColors.Error,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Something went wrong",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

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

private fun inferTypeLabel(
    material: Material
): String {
    val value = "${material.title} ${material.url}".lowercase()

    return when {
        value.contains(".pdf") || value.contains("pdf") -> "PDF"
        value.contains(".pptx") || value.contains(".ppt") -> "SLIDES"
        value.contains(".docx") || value.contains(".doc") -> "DOC"
        value.contains(".txt") -> "TEXT"
        value.contains(".jpg") || value.contains(".jpeg") || value.contains(".png") -> "IMAGE"
        value.contains("note") -> "NOTES"
        else -> "FILE"
    }
}