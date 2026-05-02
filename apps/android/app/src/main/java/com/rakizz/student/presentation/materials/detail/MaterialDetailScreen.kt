package com.rakizz.student.presentation.materials.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Material
import com.rakizz.student.domain.model.MaterialDownload
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.theme.RakizzColors
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun MaterialDetailScreen(
    materialId: String,
    onBackClick: () -> Unit = {},
    onGenerateQuizClick: (String) -> Unit = {},
    viewModel: MaterialDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(materialId) {
        viewModel.loadMaterial(materialId)
    }

    val state by viewModel.uiState.collectAsState()
    val isWorking by viewModel.isWorking.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val openFileEvent by viewModel.openFileEvent.collectAsState()

    LaunchedEffect(actionMessage) {
        if (!actionMessage.isNullOrBlank()) {
            delay(2500)
            viewModel.clearActionMessage()
        }
    }

    LaunchedEffect(openFileEvent) {
        val fileEvent = openFileEvent ?: return@LaunchedEffect

        // after download, Android tries to open the file
        openLocalMaterial(
            context = context,
            download = fileEvent
        )

        viewModel.clearOpenFileEvent()
    }

    when (val uiState = state) {
        is UiState.Loading -> {
            MaterialLoadingScreen()
        }

        is UiState.Error -> {
            MaterialErrorScreen(
                message = uiState.message,
                onBackClick = onBackClick
            )
        }

        is UiState.Success -> {
            MaterialDetailContent(
                material = uiState.data,
                actionMessage = actionMessage,
                isWorking = isWorking,
                onBackClick = onBackClick,
                onGenerateQuizClick = {
                    onGenerateQuizClick(uiState.data.id)
                },
                onPreviewClick = {
                    viewModel.previewMaterial()
                },
                onDownloadClick = {
                    viewModel.downloadMaterial()
                }
            )
        }

        else -> Unit
    }
}

@Composable
private fun MaterialDetailContent(
    material: Material,
    actionMessage: String?,
    isWorking: Boolean,
    onBackClick: () -> Unit,
    onGenerateQuizClick: () -> Unit,
    onPreviewClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
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
                MaterialHeroCard(
                    material = material,
                    isWorking = isWorking,
                    onPreviewClick = onPreviewClick
                )
            }

            if (!actionMessage.isNullOrBlank()) {
                item {
                    MessageCard(
                        title = "Status",
                        message = actionMessage.orEmpty(),
                        color = RakizzColors.Primary
                    )
                }
            }

            item {
                MaterialInfoCard(material = material)
            }

            item {
                GenerateQuizCard(
                    isWorking = isWorking,
                    onGenerateQuizClick = onGenerateQuizClick
                )
            }

            item {
                MaterialActionsCard(
                    isWorking = isWorking,
                    onPreviewClick = onPreviewClick,
                    onDownloadClick = onDownloadClick
                )
            }

            item {
                BestMaterialCard()
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
                text = "Material Details",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Preview, download, or generate a quiz.",
                color = RakizzColors.TextSecond,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MaterialHeroCard(
    material: Material,
    isWorking: Boolean,
    onPreviewClick: () -> Unit
) {
    val typeLabel = inferTypeLabel(material)
    val subjectLabel = inferSubject(material.title)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
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
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(94.dp),
                shape = CircleShape,
                color = RakizzColors.White.copy(alpha = 0.16f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = typeLabel.take(3).uppercase(),
                        color = RakizzColors.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = material.title,
                color = RakizzColors.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$subjectLabel • $typeLabel",
                color = RakizzColors.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPreviewClick,
                enabled = !isWorking,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.White,
                    contentColor = RakizzColors.PrimaryDark,
                    disabledContainerColor = RakizzColors.White.copy(alpha = 0.55f),
                    disabledContentColor = RakizzColors.PrimaryDark
                )
            ) {
                if (isWorking) {
                    CircularProgressIndicator(
                        color = RakizzColors.Primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(21.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = if (isWorking) "Preparing..." else "Preview Material",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun MaterialInfoCard(
    material: Material
) {
    val descriptionText = material.description.ifBlank {
        "This material is saved in your study library and can be used to generate AI quizzes."
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
            Text(
                text = "Material information",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            InfoBox(
                label = "Title",
                value = material.title
            )

            InfoBox(
                label = "Description",
                value = descriptionText
            )

            InfoBox(
                label = "File type",
                value = inferTypeLabel(material)
            )
        }
    }
}

@Composable
private fun GenerateQuizCard(
    isWorking: Boolean,
    onGenerateQuizClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
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
                    icon = Icons.Filled.AutoAwesome,
                    color = RakizzColors.Primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Generate Mixed AI Quiz",
                        color = RakizzColors.TextMain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Rakizz creates easy, medium, and hard questions from this material.",
                        color = RakizzColors.TextSecond,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGenerateQuizClick,
                enabled = !isWorking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RakizzColors.Primary,
                    contentColor = RakizzColors.White,
                    disabledContainerColor = RakizzColors.Primary.copy(alpha = 0.55f),
                    disabledContentColor = RakizzColors.White
                )
            ) {
                Text(
                    text = "Generate Quiz",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun MaterialActionsCard(
    isWorking: Boolean,
    onPreviewClick: () -> Unit,
    onDownloadClick: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "File actions",
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            OutlinedButton(
                onClick = onPreviewClick,
                enabled = !isWorking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, RakizzColors.Primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RakizzColors.Primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Preview Material",
                    fontWeight = FontWeight.ExtraBold
                )
            }

            OutlinedButton(
                onClick = onDownloadClick,
                enabled = !isWorking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, RakizzColors.Primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RakizzColors.Primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Download Material",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun BestMaterialCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.PrimarySoft,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RakizzColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Best results",
                color = RakizzColors.PrimaryDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Readable PDFs, notes, slides, and text files usually give better AI quiz questions.",
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
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = RakizzColors.TextMuted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = value,
                color = RakizzColors.TextMain,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
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
private fun MaterialLoadingScreen() {
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
                    text = "Loading material...",
                    color = RakizzColors.TextMain,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun MaterialErrorScreen(
    message: String,
    onBackClick: () -> Unit
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
                Text(
                    text = "Could not load material",
                    color = RakizzColors.Error,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
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
                    onClick = onBackClick,
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

private fun inferSubject(
    title: String
): String {
    val value = title.lowercase()

    return when {
        value.contains("math") ||
            value.contains("calc") ||
            value.contains("algebra") ||
            value.contains("equation") -> "Math"

        value.contains("physics") ||
            value.contains("force") ||
            value.contains("motion") -> "Physics"

        value.contains("chem") ||
            value.contains("atom") ||
            value.contains("molecule") -> "Chemistry"

        value.contains("history") ||
            value.contains("essay") -> "History"

        else -> "General"
    }
}

private fun inferTypeLabel(
    material: Material
): String {
    val value = "${material.title} ${material.url}".lowercase()

    return when {
        value.contains(".pdf") || value.contains("pdf") -> "PDF"
        value.contains(".pptx") || value.contains(".ppt") -> "Slides"
        value.contains(".docx") || value.contains(".doc") -> "Doc"
        value.contains(".txt") -> "Text"
        value.contains(".jpg") || value.contains(".jpeg") || value.contains(".png") -> "Image"
        value.contains("note") -> "Notes"
        else -> "File"
    }
}

private fun openLocalMaterial(
    context: Context,
    download: MaterialDownload
) {
    val file = File(download.filePath)

    if (!file.exists()) {
        return
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(
            uri,
            download.mimeType.ifBlank {
                "application/octet-stream"
            }
        )

        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val chooser = Intent.createChooser(
        intent,
        "Open material"
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        // no app on the device can open this file
    }
}