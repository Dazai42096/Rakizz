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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Material
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.ErrorView
import com.rakizz.student.presentation.common.components.LoadingView
import kotlinx.coroutines.delay

private val ScreenTop = Color(0xFF041127)
private val ScreenMid = Color(0xFF01050B)
private val ScreenBottom = Color(0xFF000000)
private val AccentBlue = Color(0xFF2157D8)
private val AccentBlueSoft = Color(0xFF6AA5FF)
private val CardSurface = Color.White.copy(alpha = 0.07f)
private val CardBorder = Color.White.copy(alpha = 0.08f)
private val SecondaryText = Color(0xFF8F97A9)
private val InfoSurface = Color(0xFF10203E)
private val InfoBorder = Color(0xFF264A8A)
private val ErrorSurface = Color(0xFF2B0E12)
private val ErrorBorder = Color(0xFF5B222C)
private val ErrorText = Color(0xFFFFB4C0)
private val GreenText = Color(0xFF30D158)

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
                        body = "Rakizz will generate one mixed quiz from this material. The quiz includes easy, medium, and hard questions together."
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
                            containerColor = AccentBlue,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF214488),
                            disabledContentColor = Color.White.copy(alpha = 0.8f)
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
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
                        text = "The student does not choose the level. Rakizz decides the mix automatically.",
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF566074),
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
                containerColor = Color.White.copy(alpha = 0.10f),
                contentColor = Color.White
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
            color = Color.White
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
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
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
                color = Color(0xFF103A9C)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = Color.White,
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
                    color = AccentBlueSoft
                )

                Text(
                    text = material.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
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
            containerColor = InfoSurface
        ),
        border = BorderStroke(1.dp, InfoBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = body,
                color = Color(0xFFB7C4E6),
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
                text = "What Rakizz will do",
                color = Color.White,
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
            color = Color.White.copy(alpha = 0.06f),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Box(
                modifier = Modifier.padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentBlueSoft,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            color = SecondaryText,
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
            containerColor = if (isError) ErrorSurface else InfoSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isError) ErrorBorder else InfoBorder
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = if (isError) ErrorText else Color.White,
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
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
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
            .background(Color.Black.copy(alpha = 0.94f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = AccentBlue,
                strokeWidth = 5.dp,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Generating mixed AI quiz...",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Analyzing ${materialTitle.take(35)} and creating easy, medium, and hard questions.",
                color = Color(0xFFB0B8C8),
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