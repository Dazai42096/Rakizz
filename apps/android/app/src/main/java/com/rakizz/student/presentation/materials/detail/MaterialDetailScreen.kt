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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Material
import com.rakizz.student.domain.model.MaterialDownload
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.common.components.ErrorView
import com.rakizz.student.presentation.common.components.LoadingView
import kotlinx.coroutines.delay
import java.io.File

private val ScreenTop = Color(0xFF02060E)
private val ScreenBottom = Color(0xFF000000)
private val CardSurface = Color(0xFF171717)
private val CardSurface2 = Color(0xFF101319)
private val CardBorder = Color(0xFF2A2F3A)
private val WhiteText = Color(0xFFFFFFFF)
private val SecondaryText = Color(0xFF8E97AA)
private val PrimaryBlue = Color(0xFF2157D8)
private val PrimaryBlueSoft = Color(0xFF6AA5FF)
private val InfoSurface = Color(0xFF13213E)
private val InfoBorder = Color(0xFF264A8A)

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
        openLocalMaterial(context, fileEvent)
        viewModel.clearOpenFileEvent()
    }

    when (val uiState = state) {
        is UiState.Loading -> LoadingView()

        is UiState.Error -> ErrorView(
            message = uiState.message
        )

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
        containerColor = ScreenTop
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(ScreenTop, ScreenBottom)
                    )
                )
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                MaterialHeroSection(
                    material = material,
                    isWorking = isWorking,
                    onBackClick = onBackClick,
                    onPreviewClick = onPreviewClick
                )
            }

            if (!actionMessage.isNullOrBlank()) {
                item {
                    StatusMessageCard(message = actionMessage.orEmpty())
                }
            }

            item {
                MaterialMetaSection(material = material)
            }

            item {
                GenerateQuizSection(
                    isWorking = isWorking,
                    onGenerateQuizClick = onGenerateQuizClick
                )
            }

            item {
                MaterialActionsSection(
                    isWorking = isWorking,
                    onPreviewClick = onPreviewClick,
                    onDownloadClick = onDownloadClick
                )
            }

            item {
                ImportantNoteSection()
            }
        }
    }
}

@Composable
private fun MaterialHeroSection(
    material: Material,
    isWorking: Boolean,
    onBackClick: () -> Unit,
    onPreviewClick: () -> Unit
) {
    val subject = inferSubject(material.title)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFC8BDAE),
                        Color(0xFFAEA292),
                        Color(0xFF2A221C),
                        ScreenTop
                    )
                )
            )
    ) {
        FilledIconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(start = 20.dp, top = 18.dp)
                .align(Alignment.TopStart),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White.copy(alpha = 0.18f),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 42.dp)
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFECE3D8),
                            Color(0xFFD6CEC2),
                            Color(0xFFAAA294),
                            Color(0xFF736A61)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 18.dp, top = 18.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = subject.uppercase(),
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = subjectGlyph(subject),
                color = Color.White.copy(alpha = 0.56f),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            Text(
                text = "STUDY MATERIAL",
                color = Color.White.copy(alpha = 0.62f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
            )
        }

        FilledIconButton(
            onClick = onPreviewClick,
            enabled = !isWorking,
            modifier = Modifier
                .align(Alignment.Center)
                .size(82.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White.copy(alpha = 0.22f),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Preview material",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun StatusMessageCard(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(InfoSurface)
            .border(1.dp, InfoBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = message,
            color = WhiteText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MaterialMetaSection(
    material: Material
) {
    val subject = inferSubject(material.title)
    val typeLabel = inferTypeLabel(material)
    val descriptionText = material.description.ifBlank {
        "This material is stored in your library and can be previewed, downloaded, or used for quiz generation."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetaBadge(
                text = subject.uppercase(),
                containerColor = Color(0xFF0E2A62),
                contentColor = PrimaryBlueSoft
            )

            MetaBadge(
                text = typeLabel,
                containerColor = Color.White.copy(alpha = 0.08f),
                contentColor = Color.White.copy(alpha = 0.9f)
            )

            Text(
                text = "• Stored in library",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
        }

        Text(
            text = material.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WhiteText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = descriptionText,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFA7AFBF),
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GenerateQuizSection(
    isWorking: Boolean,
    onGenerateQuizClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface2
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryBlueSoft
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Generate AI Quiz",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = WhiteText
                )
            }

            Text(
                text = "Create a quiz from this selected material. If the file cannot be read, Rakizz will show an error and you can try another material.",
                style = MaterialTheme.typography.bodyLarge,
                color = SecondaryText
            )

            Button(
                onClick = onGenerateQuizClick,
                enabled = !isWorking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = WhiteText,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.45f),
                    disabledContentColor = WhiteText.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = "Generate Quiz",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MaterialActionsSection(
    isWorking: Boolean,
    onPreviewClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onPreviewClick,
            enabled = !isWorking,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = WhiteText,
                containerColor = Color.White.copy(alpha = 0.05f)
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Preview Material",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        OutlinedButton(
            onClick = onDownloadClick,
            enabled = !isWorking,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = WhiteText,
                containerColor = Color.White.copy(alpha = 0.05f)
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Download Material",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ImportantNoteSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Important note",
                color = WhiteText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Quiz generation depends on the selected file content. Clear study materials such as readable notes, slides, and PDFs work best.",
                color = SecondaryText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun MetaBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = contentColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun inferSubject(title: String): String {
    val value = title.lowercase()

    return when {
        value.contains("math") || value.contains("calc") || value.contains("algebra") || value.contains("equation") -> "Math"
        value.contains("physics") || value.contains("force") || value.contains("motion") -> "Physics"
        value.contains("chem") || value.contains("atom") || value.contains("molecule") -> "Chemistry"
        value.contains("history") || value.contains("wwii") || value.contains("essay") -> "History"
        else -> "General"
    }
}

private fun inferTypeLabel(material: Material): String {
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

private fun subjectGlyph(subject: String): String {
    return when (subject) {
        "Math" -> "Σ"
        "Physics" -> "⚗"
        "Chemistry" -> "⚗"
        "History" -> "⌲"
        else -> "▣"
    }
}

private fun openLocalMaterial(
    context: Context,
    download: MaterialDownload
) {
    val file = File(download.filePath)
    if (!file.exists()) return

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, download.mimeType.ifBlank { "application/octet-stream" })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val chooser = Intent.createChooser(intent, "Open material").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        // No compatible app found on device.
    }
}