package com.rakizz.student.presentation.materials.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Material
import com.rakizz.student.domain.model.MaterialDownload
import com.rakizz.student.presentation.common.UiState
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun MaterialDetailScreen(
    materialId: String = "",
    viewModel: MaterialDetailViewModel? = null,
    onBackClick: () -> Unit = {},
    onGenerateQuizClick: (String) -> Unit = {},
    onMaterialDeleted: () -> Unit = {}
) {
    val colors = detailColors()
    val context = LocalContext.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val materialDetailViewModel = viewModel ?: hiltViewModel()

    val uiState by materialDetailViewModel.uiState.collectAsState()
    val isWorking by materialDetailViewModel.isWorking.collectAsState()
    val actionMessage by materialDetailViewModel.actionMessage.collectAsState()
    val openFileEvent by materialDetailViewModel.openFileEvent.collectAsState()
    val deleteCompleted by materialDetailViewModel.deleteCompleted.collectAsState()

    var message by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var pendingOpenMode by remember { mutableStateOf(MaterialOpenMode.PREVIEW) }

    LaunchedEffect(materialId) {
        if (materialId.isNotBlank()) {
            materialDetailViewModel.loadMaterial(materialId)
        } else {
            message = "Material ID is missing."
        }
    }

    LaunchedEffect(actionMessage) {
        val newMessage = actionMessage

        if (!newMessage.isNullOrBlank()) {
            message = cleanDetailMessage(newMessage)
            materialDetailViewModel.clearActionMessage()
        }
    }

    LaunchedEffect(openFileEvent) {
        val download = openFileEvent ?: return@LaunchedEffect

        val opened = openDownloadedFile(
            context = context,
            download = download,
            editable = pendingOpenMode == MaterialOpenMode.EDIT
        )

        message = when {
            opened && pendingOpenMode == MaterialOpenMode.PREVIEW -> {
                "Material opened."
            }

            opened && pendingOpenMode == MaterialOpenMode.EDIT -> {
                "Material opened for editing."
            }

            pendingOpenMode == MaterialOpenMode.EDIT -> {
                "No editor app found for this file. Try installing a document or PDF editor."
            }

            else -> {
                "No app found to open this file type."
            }
        }

        materialDetailViewModel.clearOpenFileEvent()
    }

    LaunchedEffect(deleteCompleted) {
        if (deleteCompleted) {
            materialDetailViewModel.clearDeleteCompleted()
            delay(500)
            onMaterialDeleted()
        }
    }

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            delay(3000)
            message = ""
        }
    }

    fun goBack() {
        onBackClick()

        if (onBackClick == {}) {
            backDispatcher?.onBackPressed()
        }
    }

    fun generateQuiz() {
        if (materialId.isBlank()) {
            message = "Material ID is missing."
            return
        }

        onGenerateQuizClick(materialId)
    }

    fun previewMaterial() {
        pendingOpenMode = MaterialOpenMode.PREVIEW
        materialDetailViewModel.previewMaterial()
    }

    fun editMaterial() {
        pendingOpenMode = MaterialOpenMode.EDIT
        materialDetailViewModel.previewMaterial()
    }

    fun downloadMaterial() {
        materialDetailViewModel.downloadMaterial()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 20.dp)
                .padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            TopBar(
                colors = colors,
                onBackClick = { goBack() },
                onRefreshClick = {
                    if (materialId.isNotBlank()) {
                        message = "Refreshing material..."
                        materialDetailViewModel.loadMaterial(materialId)
                    }
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            when (val state = uiState) {
                UiState.Loading -> {
                    LoadingCard(colors = colors)
                }

                UiState.Empty -> {
                    ErrorCard(
                        title = "Material not found",
                        message = "This material could not be loaded.",
                        colors = colors,
                        onRetryClick = {
                            if (materialId.isNotBlank()) {
                                materialDetailViewModel.loadMaterial(materialId)
                            }
                        }
                    )
                }

                is UiState.Error -> {
                    ErrorCard(
                        title = "Could not load material",
                        message = cleanDetailMessage(state.message),
                        colors = colors,
                        onRetryClick = {
                            if (materialId.isNotBlank()) {
                                materialDetailViewModel.loadMaterial(materialId)
                            }
                        }
                    )
                }

                is UiState.Success -> {
                    MaterialHeroCard(
                        material = state.data,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(visible = message.isNotEmpty()) {
                        MessageCard(
                            message = message,
                            colors = colors,
                            isError = isErrorMessage(message)
                        )
                    }

                    if (message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    ActionCard(
                        colors = colors,
                        isWorking = isWorking,
                        onGenerateQuiz = { generateQuiz() },
                        onPreview = { previewMaterial() },
                        onEdit = { editMaterial() },
                        onDownload = { downloadMaterial() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    InfoCard(
                        material = state.data,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    EditWarningCard(colors = colors)

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(visible = showDeleteConfirm) {
                        DeleteConfirmationCard(
                            colors = colors,
                            isWorking = isWorking,
                            onCancel = {
                                showDeleteConfirm = false
                            },
                            onConfirm = {
                                showDeleteConfirm = false
                                materialDetailViewModel.deleteMaterial()
                            }
                        )
                    }

                    if (showDeleteConfirm) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    DangerButton(
                        colors = colors,
                        enabled = !isWorking
                    ) {
                        showDeleteConfirm = true
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun TopBar(
    colors: DetailColors,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircleButton("<", colors, onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = "Material Detail",
                color = colors.textPrimary,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Preview, edit, download, and generate quizzes",
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        CircleButton("R", colors, onRefreshClick)
    }
}

@Composable
private fun LoadingCard(colors: DetailColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = "Loading material...",
            color = colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(22.dp)
        )
    }
}

@Composable
private fun ErrorCard(
    title: String,
    message: String,
    colors: DetailColors,
    onRetryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = colors.danger,
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

            MainButton(
                text = "Try Again",
                colors = colors,
                enabled = true,
                onClick = onRetryClick
            )
        }
    }
}

@Composable
private fun MaterialHeroCard(
    material: Material,
    colors: DetailColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.30f),
                            colors.card,
                            colors.cardAlt
                        )
                    )
                )
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                colors.primary,
                                colors.accent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = material.title.ifBlank { "Study Material" },
                color = colors.textPrimary,
                fontSize = 27.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ID: ${material.id}",
                color = colors.textSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = material.description.ifBlank {
                    "This material can be opened, downloaded, deleted, and converted into an AI-generated quiz."
                },
                color = colors.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    colors: DetailColors,
    isError: Boolean
) {
    val messageColor = if (isError) {
        colors.danger
    } else {
        colors.success
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = messageColor.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, messageColor.copy(alpha = 0.32f))
    ) {
        Text(
            text = message,
            color = messageColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(14.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionCard(
    colors: DetailColors,
    isWorking: Boolean,
    onGenerateQuiz: () -> Unit,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Material Actions",
                subtitle = "Open the uploaded file or use it for quiz generation",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            MainButton(
                text = "Generate AI Quiz",
                colors = colors,
                enabled = !isWorking,
                onClick = onGenerateQuiz
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = if (isWorking) "Preparing File..." else "Preview Material",
                subtitle = "Open the uploaded file using an installed viewer",
                icon = "P",
                colors = colors,
                enabled = !isWorking,
                onClick = onPreview
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = if (isWorking) "Preparing Editor..." else "Open / Edit Material",
                subtitle = "Open local copy with an installed editor app",
                icon = "E",
                colors = colors,
                enabled = !isWorking,
                onClick = onEdit
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = if (isWorking) "Downloading..." else "Download Material",
                subtitle = "Save a local copy inside Rakizz",
                icon = "D",
                colors = colors,
                enabled = !isWorking,
                onClick = onDownload
            )
        }
    }
}

@Composable
private fun InfoCard(
    material: Material,
    colors: DetailColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Material Information",
                subtitle = "Metadata loaded from backend",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            InfoLine("ID", "Material ID", material.id, colors)
            InfoLine("Title", "Name", material.title.ifBlank { "Untitled Material" }, colors)
            InfoLine("Source", "File source", material.url.ifBlank { "Stored in Rakizz backend" }, colors)
            InfoLine("AI", "AI Usage", "Can generate MCQ quizzes", colors)
            InfoLine("Flow", "Quiz Flow", "Material -> Quiz Setup -> Quiz Detail", colors)
        }
    }
}

@Composable
private fun InfoLine(
    icon: String,
    title: String,
    value: String,
    colors: DetailColors
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colors.primary.copy(alpha = 0.14f))
                .border(1.dp, colors.primary.copy(alpha = 0.28f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = title,
                color = colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun EditWarningCard(colors: DetailColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.warning.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.32f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Editing note",
                color = colors.warning,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Editing opens a downloaded local copy. Changes are not uploaded back to Rakizz yet.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun DeleteConfirmationCard(
    colors: DetailColors,
    isWorking: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Delete this material?",
                color = colors.danger,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "This removes the material from Rakizz and deletes its stored file if it exists on the server.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryButton(
                text = "Cancel",
                subtitle = "Keep this material",
                icon = "X",
                colors = colors,
                enabled = !isWorking,
                onClick = onCancel
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(enabled = !isWorking, onClick = onConfirm),
                shape = RoundedCornerShape(20.dp),
                color = colors.danger.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.35f))
            ) {
                Text(
                    text = if (isWorking) "Deleting..." else "Yes, Delete Material",
                    color = colors.danger,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(15.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MainButton(
    text: String,
    colors: DetailColors,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.55f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.primary.copy(alpha = alpha),
                        colors.accent.copy(alpha = alpha)
                    )
                )
            )
            .clickable(enabled = enabled, onClick = onClick),
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
private fun SecondaryButton(
    text: String,
    subtitle: String,
    icon: String,
    colors: DetailColors,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.55f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardAlt.copy(alpha = alpha),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(colors.primary.copy(alpha = 0.14f))
                    .border(1.dp, colors.primary.copy(alpha = 0.28f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    color = colors.primary,
                    fontSize = 14.sp,
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
private fun DangerButton(
    colors: DetailColors,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.55f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = colors.danger.copy(alpha = 0.12f * alpha),
        border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.32f * alpha))
    ) {
        Text(
            text = "Delete Material",
            color = colors.danger.copy(alpha = alpha),
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    colors: DetailColors
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
private fun CircleButton(
    text: String,
    colors: DetailColors,
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

private fun openDownloadedFile(
    context: Context,
    download: MaterialDownload,
    editable: Boolean
): Boolean {
    return try {
        val file = File(download.filePath)

        if (!file.exists()) {
            return false
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val mimeType = download.mimeType.ifBlank {
            guessMimeTypeFromName(download.displayName)
        }

        val intentAction = if (editable) {
            Intent.ACTION_EDIT
        } else {
            Intent.ACTION_VIEW
        }

        val intent = Intent(intentAction).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            if (editable) {
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }

        val chooserTitle = if (editable) {
            "Edit material"
        } else {
            "Open material"
        }

        context.startActivity(
            Intent.createChooser(intent, chooserTitle)
        )

        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: Exception) {
        false
    }
}

private fun guessMimeTypeFromName(fileName: String): String {
    val lower = fileName.lowercase()

    return when {
        lower.endsWith(".pdf") -> "application/pdf"
        lower.endsWith(".txt") -> "text/plain"
        lower.endsWith(".doc") -> "application/msword"
        lower.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        lower.endsWith(".ppt") -> "application/vnd.ms-powerpoint"
        lower.endsWith(".pptx") -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
        lower.endsWith(".png") -> "image/png"
        else -> "application/octet-stream"
    }
}

private fun cleanDetailMessage(message: String): String {
    val clean = message.lowercase()

    return when {
        "401" in clean ||
            "403" in clean ||
            "unauthorized" in clean -> {
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

        "stored file not found" in clean -> {
            "The uploaded file is missing on the server. Re-upload this material and try again."
        }

        else -> {
            message.ifBlank { "Something went wrong. Please try again." }
        }
    }
}

private fun isErrorMessage(message: String): Boolean {
    val clean = message.lowercase()

    return "failed" in clean ||
        "missing" in clean ||
        "expired" in clean ||
        "not found" in clean ||
        "unable" in clean ||
        "could not" in clean ||
        "no app" in clean ||
        "error" in clean
}

@Composable
private fun detailColors(): DetailColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        DetailColors(
            backgroundTop = Color(0xFF020617),
            backgroundMiddle = Color(0xFF07111F),
            backgroundBottom = Color(0xFF000000),
            card = Color(0xE60B1220),
            cardAlt = Color(0xCC111A2E),
            border = Color(0x334D9DFF),
            primary = Color(0xFF2F80FF),
            accent = Color(0xFF00D4FF),
            success = Color(0xFF22C55E),
            warning = Color(0xFFF59E0B),
            danger = Color(0xFFEF4444),
            textPrimary = Color.White,
            textSecondary = Color(0xFF94A3B8)
        )
    } else {
        DetailColors(
            backgroundTop = Color(0xFFF8FBFF),
            backgroundMiddle = Color(0xFFEAF4FF),
            backgroundBottom = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            cardAlt = Color(0xFFF1F7FF),
            border = Color(0x263B82F6),
            primary = Color(0xFF2563EB),
            accent = Color(0xFF06B6D4),
            success = Color(0xFF16A34A),
            warning = Color(0xFFD97706),
            danger = Color(0xFFDC2626),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B)
        )
    }
}

private enum class MaterialOpenMode {
    PREVIEW,
    EDIT
}

private data class DetailColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val cardAlt: Color,
    val border: Color,
    val primary: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color
)