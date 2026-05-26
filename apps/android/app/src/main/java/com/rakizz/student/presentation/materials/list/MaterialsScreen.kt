package com.rakizz.student.presentation.materials.list

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rakizz.student.domain.model.Material
import com.rakizz.student.presentation.common.UiState
import kotlinx.coroutines.delay

@Suppress("UNUSED_PARAMETER")
@Composable
fun MaterialsScreen(
    navController: NavController? = null,
    viewModel: MaterialsViewModel? = null,

    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},

    onAddMaterial: () -> Unit = {},
    onTakePhoto: () -> Unit = {},
    onWriteTextNotes: () -> Unit = {},
    onAddLink: () -> Unit = {},

    onAddMaterialClick: () -> Unit = {},
    onUploadMaterialClick: () -> Unit = {},
    onCreateMaterialClick: () -> Unit = {},

    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToMaterialDetail: (String) -> Unit = {},
    onMaterialClick: (String) -> Unit = {},
    onMaterialSelected: (String) -> Unit = {},
    onOpenMaterial: (String) -> Unit = {},
    onOpenMaterialClick: (String) -> Unit = {},
    onOpenMaterialDetailClick: (String) -> Unit = {},
    onOpenDetailClick: (String) -> Unit = {},
    onGenerateQuizClick: (String) -> Unit = {},

    onRefreshClick: () -> Unit = {},

    onOpenHome: () -> Unit = {},
    onOpenQuizzes: () -> Unit = {},
    onOpenAssignments: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val colors = materialsColors()
    val context = LocalContext.current
    val materialsViewModel = viewModel ?: hiltViewModel()
    val uiState by materialsViewModel.uiState.collectAsState()
    val actionMessage by materialsViewModel.actionMessage.collectAsState()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    var message by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            message = "No file selected."
            return@rememberLauncherForActivityResult
        }

        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Some providers do not allow persistable permission. Reading still works.
        }

        val fileName = getDisplayName(context, uri)
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val bytes = readUriBytes(context, uri)

        if (bytes == null || bytes.isEmpty()) {
            message = "Selected file is empty or could not be read."
            return@rememberLauncherForActivityResult
        }

        val title = fileName
            .substringBeforeLast(".")
            .replace("_", " ")
            .replace("-", " ")
            .trim()
            .ifBlank { "Uploaded Material" }

        materialsViewModel.uploadMaterialFile(
            title = title,
            fileName = fileName,
            mimeType = mimeType,
            bytes = bytes
        )
    }

    val materials = when (val state = uiState) {
        is UiState.Success -> state.data
        else -> emptyList()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "materials_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "materials_glow"
    )

    LaunchedEffect(actionMessage) {
        val newMessage = actionMessage
        if (!newMessage.isNullOrBlank()) {
            message = cleanMaterialMessage(newMessage)
            materialsViewModel.clearActionMessage()
        }
    }

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            delay(2500)
            message = ""
        }
    }

    fun goBack() {
        if (navController != null) {
            val popped = navController.popBackStack()
            if (!popped) {
                onOpenHome()
            }
        } else {
            onBackClick()
            onNavigateBack()

            if (backDispatcher != null) {
                backDispatcher.onBackPressed()
            } else {
                onOpenHome()
            }
        }
    }

    fun openMaterial(materialId: String) {
        onMaterialClick(materialId)
        onMaterialSelected(materialId)
        onOpenMaterial(materialId)
        onOpenMaterialClick(materialId)
        onOpenMaterialDetailClick(materialId)
        onOpenDetailClick(materialId)
        onNavigateToMaterialDetail(materialId)
        onNavigateToDetail(materialId)
    }

    fun uploadMaterial() {
        onAddMaterial()
        onAddMaterialClick()
        onUploadMaterialClick()
        onCreateMaterialClick()

        filePickerLauncher.launch(
            arrayOf(
                "text/plain",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/octet-stream"
            )
        )
    }

    fun openCameraUpload() {
        message = "Photo upload is not connected yet. Use Upload New Material for now."
        onTakePhoto()
    }

    fun openTextNotes() {
        message = "Text notes are not connected yet. Use Upload New Material for now."
        onWriteTextNotes()
    }

    fun openAddLink() {
        message = "Link upload is not connected yet. Use Upload New Material for now."
        onAddLink()
    }

    fun generateQuizFromFirstMaterial() {
        val firstMaterial = materials.firstOrNull()

        if (firstMaterial == null) {
            message = "Upload a material first, then generate a quiz."
            return
        }

        openMaterial(firstMaterial.id)
    }

    fun refreshMaterials() {
        message = "Refreshing materials..."
        materialsViewModel.loadMaterials()
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
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-135).dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                    alpha = 0.9f
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

            MaterialsTopBar(
                colors = colors,
                onBackClick = { goBack() },
                onRefreshClick = { refreshMaterials() }
            )

            Spacer(modifier = Modifier.height(22.dp))

            MaterialsHeroCard(
                total = materials.size,
                ready = materials.size,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = message.isNotEmpty()) {
                MessageCard(
                    message = message,
                    colors = colors
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            MaterialUploadActionsCard(
                colors = colors,
                onAddMaterial = { uploadMaterial() },
                onTakePhoto = { openCameraUpload() },
                onWriteTextNotes = { openTextNotes() },
                onAddLink = { openAddLink() },
                onGenerateQuiz = { generateQuizFromFirstMaterial() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                UiState.Loading -> {
                    LoadingMaterialsCard(colors = colors)
                }

                UiState.Empty -> {
                    EmptyMaterialsCard(
                        colors = colors,
                        onUploadClick = { uploadMaterial() }
                    )
                }

                is UiState.Error -> {
                    ErrorMaterialsCard(
                        message = cleanMaterialMessage(state.message),
                        colors = colors,
                        onRetryClick = { materialsViewModel.loadMaterials() }
                    )
                }

                is UiState.Success -> {
                    MaterialsListCard(
                        materials = state.data,
                        colors = colors,
                        onMaterialClick = { item -> openMaterial(item.id) },
                        onGenerateQuizClick = { item ->
                            openMaterial(item.id)
                            onGenerateQuizClick(item.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MaterialsNavigationCard(
                colors = colors,
                onOpenHome = onOpenHome,
                onOpenQuizzes = onOpenQuizzes,
                onOpenAssignments = onOpenAssignments,
                onOpenFocus = onOpenFocus,
                onOpenProfile = onOpenProfile
            )

            Spacer(modifier = Modifier.height(16.dp))

            MaterialsExplanationCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MaterialsTopBar(
    colors: MaterialsColors,
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
                text = "Materials Library",
                color = colors.textPrimary,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Upload, organize, and generate AI quizzes",
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
private fun MaterialsHeroCard(
    total: Int,
    ready: Int,
    colors: MaterialsColors
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
                        text = "Smart learning library",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your study files become AI quiz fuel.",
                    color = colors.textPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 31.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$total materials uploaded - $ready ready for quiz generation",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    colors: MaterialsColors
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
private fun MaterialUploadActionsCard(
    colors: MaterialsColors,
    onAddMaterial: () -> Unit,
    onTakePhoto: () -> Unit,
    onWriteTextNotes: () -> Unit,
    onAddLink: () -> Unit,
    onGenerateQuiz: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Add Study Material",
                subtitle = "Choose how the student adds learning content",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            MainButton(
                text = "Upload New Material",
                colors = colors,
                onClick = onAddMaterial
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Take Photo",
                subtitle = "Coming later. Use file upload for now.",
                iconText = "P",
                colors = colors,
                onClick = onTakePhoto
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Write Text Notes",
                subtitle = "Coming later. Use file upload for now.",
                iconText = "T",
                colors = colors,
                onClick = onWriteTextNotes
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Add Link",
                subtitle = "Coming later. Use file upload for now.",
                iconText = "L",
                colors = colors,
                onClick = onAddLink
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Generate Quiz from Material",
                subtitle = "Open the first material and start quiz generation",
                iconText = "Q",
                colors = colors,
                onClick = onGenerateQuiz
            )
        }
    }
}

@Composable
private fun LoadingMaterialsCard(colors: MaterialsColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = "Loading materials...",
            color = colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(22.dp)
        )
    }
}

@Composable
private fun EmptyMaterialsCard(
    colors: MaterialsColors,
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No materials yet",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Upload a study file to generate AI quizzes from it.",
                color = colors.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            MainButton(
                text = "Upload Material",
                colors = colors,
                onClick = onUploadClick
            )
        }
    }
}

@Composable
private fun ErrorMaterialsCard(
    message: String,
    colors: MaterialsColors,
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
                text = "Could not load materials",
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

            MainButton(
                text = "Try Again",
                colors = colors,
                onClick = onRetryClick
            )
        }
    }
}

@Composable
private fun MaterialsListCard(
    materials: List<Material>,
    colors: MaterialsColors,
    onMaterialClick: (Material) -> Unit,
    onGenerateQuizClick: (Material) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Uploaded Materials",
                subtitle = "Tap a material to open its detail screen",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            materials.forEachIndexed { index, item ->
                MaterialRow(
                    item = item,
                    colors = colors,
                    onClick = { onMaterialClick(item) },
                    onGenerateQuizClick = { onGenerateQuizClick(item) }
                )

                if (index != materials.lastIndex) {
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

@Composable
private fun MaterialRow(
    item: Material,
    colors: MaterialsColors,
    onClick: () -> Unit,
    onGenerateQuizClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.primary.copy(alpha = 0.15f))
                    .border(1.dp, colors.primary.copy(alpha = 0.32f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    color = colors.primary,
                    fontSize = 20.sp,
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
                        text = "Uploaded material",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = colors.success.copy(alpha = 0.13f),
                        border = BorderStroke(1.dp, colors.success.copy(alpha = 0.28f))
                    ) {
                        Text(
                            text = "Ready",
                            color = colors.success,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = item.title.ifBlank { "Untitled Material" },
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description.ifBlank { "No description available." },
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = item.url.ifBlank { "Stored in Rakizz backend" },
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onGenerateQuizClick),
            shape = RoundedCornerShape(16.dp),
            color = colors.primary.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f))
        ) {
            Text(
                text = "Open Material and Generate Quiz",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MaterialsNavigationCard(
    colors: MaterialsColors,
    onOpenHome: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenAssignments: () -> Unit,
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
                subtitle = "Move to related Rakizz screens",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryButton("Home Dashboard", "Return to main dashboard", "H", colors, onOpenHome)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("AI Quizzes", "Open generated quizzes", "Q", colors, onOpenQuizzes)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Assignments", "Open deadlines and tasks", "A", colors, onOpenAssignments)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Focus Mode", "Open focus mode", "F", colors, onOpenFocus)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Profile", "Open account and pair code", "P", colors, onOpenProfile)
        }
    }
}

@Composable
private fun MaterialsExplanationCard(colors: MaterialsColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.success.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, colors.success.copy(alpha = 0.32f))
    ) {
        Text(
            text = "How Rakizz works\nMaterials are the source for AI quiz generation. The flow is: Material, Detail, Quiz Setup, Quiz Detail.",
            color = colors.textSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun MainButton(
    text: String,
    colors: MaterialsColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.primary,
                        colors.accent
                    )
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
private fun SecondaryButton(
    text: String,
    subtitle: String,
    iconText: String,
    colors: MaterialsColors,
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
                    .size(42.dp)
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
    colors: MaterialsColors
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
    colors: MaterialsColors,
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

private fun getDisplayName(
    context: Context,
    uri: Uri
): String {
    var result: String? = null

    val cursor: Cursor? = context.contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && it.moveToFirst()) {
            result = it.getString(nameIndex)
        }
    }

    return result
        ?.takeIf { it.isNotBlank() }
        ?: "uploaded_material.txt"
}

private fun readUriBytes(
    context: Context,
    uri: Uri
): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        }
    } catch (_: Exception) {
        null
    }
}

private fun cleanMaterialMessage(message: String): String {
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

        "success" in clean || "uploaded" in clean || "added" in clean -> {
            message
        }

        else -> {
            message.ifBlank { "Something went wrong. Please try again." }
        }
    }
}

@Composable
private fun materialsColors(): MaterialsColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        MaterialsColors(
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
            error = Color(0xFFEF4444),
            textPrimary = Color.White,
            textSecondary = Color(0xFF94A3B8)
        )
    } else {
        MaterialsColors(
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
            error = Color(0xFFDC2626),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B)
        )
    }
}

private data class MaterialsColors(
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
    val error: Color,
    val textPrimary: Color,
    val textSecondary: Color
)