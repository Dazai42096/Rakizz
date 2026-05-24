package com.rakizz.student.presentation.quizzes.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Material
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.ErrorView
import com.rakizz.student.presentation.common.components.LoadingView
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
        is UiState.Loading -> LoadingView()

        is UiState.Error -> ErrorView(
            message = state.message
        )

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RakizzColors.Background,
                        RakizzColors.BackgroundSoft
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
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
                    top = 18.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                item {
                    TopBar(
                        onBackClick = onBackClick
                    )
                }

                item {
                    TargetMaterialCard(
                        material = material
                    )
                }

                item {
                    InfoCard(
                        title = "Mixed AI Quiz",
                        body = "Rakizz will generate one mixed quiz from this material.\nThe quiz includes easy, medium, and hard questions together."
                    )
                }

                item {
                    RequirementsCard()
                }

                if (!generateMessage.isNullOrBlank() && !isGenerating) {
                    item {
                        MessageCard(
                            message = generateMessage,
                            isError = !generateMessage.contains(
                                "successfully",
                                ignoreCase = true
                            )
                        )
                    }
                }

                item {
                    Button(
                        onClick = onGenerateClick,
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RakizzColors.Primary,
                            contentColor = RakizzColors.White,
                            disabledContainerColor = RakizzColors.Primary.copy(alpha = 0.55f),
                            disabledContentColor = RakizzColors.White.copy(alpha = 0.8f)
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = RakizzColors.White,
                                strokeWidth = 2.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Text(
                            text = if (isGenerating) {
                                "Generating..."
                            } else {
                                "Generate Mixed AI Quiz"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Text(
                        text = "The student does not choose the level.\nRakizz decides the mix automatically.",
                        modifier = Modifier.fillMaxWidth(),
                        color = RakizzColors.TextMuted,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (isGenerating) {
            GeneratingOverlay(
                materialTitle = material.title
            )
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

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Generate AI Quiz",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = RakizzColors.TextMain
        )
    }
}

@Composable
private fun TargetMaterialCard(
    material: Material
) {
    val descriptionText = material.description.ifBlank {
        "Stored study material selected from your library."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = RakizzColors.Card
        ),
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(84.dp),
                shape = RoundedCornerShape(20.dp),
                color = RakizzColors.PrimarySoft
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = RakizzColors.Primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SELECTED MATERIAL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RakizzColors.Primary
                )

                Text(
                    text = material.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = RakizzColors.TextMain
                )

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RakizzColors.TextSecond,
                    maxLines = 3
                )

                MetaPill(
                    text = inferTypeLabel(material)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = RakizzColors.PrimarySoft
        ),
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = body,
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun RequirementsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = RakizzColors.Card
        ),
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "What Rakizz will do",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            RequirementRow(
                icon = Icons.Filled.CheckCircle,
                text = "Read the selected material."
            )

            RequirementRow(
                icon = Icons.Filled.Description,
                text = "Split it into parts so questions cover more of the file."
            )

            RequirementRow(
                icon = Icons.Filled.AutoAwesome,
                text = "Generate around 20 mixed questions."
            )

            RequirementRow(
                icon = Icons.Filled.Timer,
                text = "Try to make different questions each time."
            )
        }
    }
}

@Composable
private fun RequirementRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = RakizzColors.PrimarySoft,
            border = BorderStroke(1.dp, RakizzColors.CardBorder)
        ) {
            Box(
                modifier = Modifier.padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RakizzColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            color = RakizzColors.TextSecond,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                RakizzColors.Error.copy(alpha = 0.14f)
            } else {
                RakizzColors.PrimarySoft
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isError) {
                RakizzColors.Error.copy(alpha = 0.55f)
            } else {
                RakizzColors.Primary.copy(alpha = 0.35f)
            }
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = if (isError) {
                RakizzColors.Error
            } else {
                RakizzColors.TextMain
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun MetaPill(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = RakizzColors.CardSoft,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = RakizzColors.TextMain
        )
    }
}

@Composable
private fun GeneratingOverlay(
    materialTitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RakizzColors.Background.copy(alpha = 0.96f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = RakizzColors.Primary,
                strokeWidth = 5.dp,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Generating mixed AI quiz...",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Analyzing ${materialTitle.take(35)} and creating easy, medium, and hard questions.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
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