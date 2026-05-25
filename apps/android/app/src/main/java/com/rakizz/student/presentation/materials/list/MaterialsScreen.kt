package com.rakizz.student.presentation.materials.list

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
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Suppress("UNUSED_PARAMETER")
@Composable
fun MaterialsScreen(
    navController: NavController? = null,
    viewModel: Any? = null,

    // Back callbacks
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},

    // Add material callbacks used by RakizzNavHost.kt
    onAddMaterial: () -> Unit = {},
    onTakePhoto: () -> Unit = {},
    onWriteTextNotes: () -> Unit = {},
    onAddLink: () -> Unit = {},

    // Extra add callbacks for compatibility
    onAddMaterialClick: () -> Unit = {},
    onUploadMaterialClick: () -> Unit = {},
    onCreateMaterialClick: () -> Unit = {},

    // Important: these accept a material id from NavHost
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToMaterialDetail: (String) -> Unit = {},
    onMaterialClick: (String) -> Unit = {},
    onMaterialSelected: (String) -> Unit = {},
    onOpenMaterial: (String) -> Unit = {},
    onOpenMaterialClick: (String) -> Unit = {},
    onOpenMaterialDetailClick: (String) -> Unit = {},
    onOpenDetailClick: (String) -> Unit = {},
    onGenerateQuizClick: (String) -> Unit = {},

    // Other callbacks
    onRefreshClick: () -> Unit = {},

    // Navigation callbacks
    onOpenHome: () -> Unit = {},
    onOpenQuizzes: () -> Unit = {},
    onOpenAssignments: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val colors = materialsColors()

    val materials = remember {
        listOf(
            MaterialItem(
                id = "material-001",
                title = "Software Engineering Notes",
                subtitle = "Requirements, architecture, implementation, and testing",
                type = "PDF",
                size = "2.4 MB",
                status = "Ready for Quiz",
                progress = 92
            ),
            MaterialItem(
                id = "material-002",
                title = "Database Mapping",
                subtitle = "PostgreSQL tables, relationships, and backend models",
                type = "DOCX",
                size = "1.7 MB",
                status = "Reviewed",
                progress = 84
            ),
            MaterialItem(
                id = "material-003",
                title = "Kotlin Compose Study File",
                subtitle = "Android UI screens, ViewModels, and navigation",
                type = "PDF",
                size = "3.1 MB",
                status = "Needs Quiz",
                progress = 68
            )
        )
    }

    var message by remember { mutableStateOf("") }

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

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            delay(2200)
            message = ""
        }
    }

    fun goBack() {
        if (navController != null) {
            navController.popBackStack()
        } else {
            onBackClick()
            onNavigateBack()
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
        message = "Add material clicked."

        onAddMaterial()
        onAddMaterialClick()
        onUploadMaterialClick()
        onCreateMaterialClick()
    }

    fun openCameraUpload() {
        message = "Take photo clicked."
        onTakePhoto()
    }

    fun openTextNotes() {
        message = "Write text notes clicked."
        onWriteTextNotes()
    }

    fun openAddLink() {
        message = "Add link clicked."
        onAddLink()
    }

    fun generateQuizFromFirstMaterial() {
        val firstId = materials.firstOrNull()?.id ?: "material-001"
        message = "Opening AI Quiz Studio."
        onGenerateQuizClick(firstId)
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
                onRefreshClick = {
                    message = "Materials refreshed."
                    onRefreshClick()
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            MaterialsHeroCard(
                total = materials.size,
                ready = materials.count { it.progress >= 80 },
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

            MaterialsListCard(
                materials = materials,
                colors = colors,
                onMaterialClick = { item -> openMaterial(item.id) },
                onGenerateQuizClick = { item -> onGenerateQuizClick(item.id) }
            )

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
            text = "←",
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
                fontSize = 13.sp
            )
        }

        CircleIconButton(
            text = "↻",
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
                        text = "● Smart learning library",
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
                    text = "$total materials uploaded • $ready ready for quiz generation",
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
                subtitle = "Capture notes or textbook pages",
                iconText = "📷",
                colors = colors,
                onClick = onTakePhoto
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Write Text Notes",
                subtitle = "Create a manual study note",
                iconText = "✍",
                colors = colors,
                onClick = onWriteTextNotes
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Add Link",
                subtitle = "Save a useful study resource URL",
                iconText = "🔗",
                colors = colors,
                onClick = onAddLink
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Generate Quiz from Material",
                subtitle = "Open AI quiz flow using a selected material",
                iconText = "🧠",
                colors = colors,
                onClick = onGenerateQuiz
            )
        }
    }
}

@Composable
private fun MaterialsListCard(
    materials: List<MaterialItem>,
    colors: MaterialsColors,
    onMaterialClick: (MaterialItem) -> Unit,
    onGenerateQuizClick: (MaterialItem) -> Unit
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
    item: MaterialItem,
    colors: MaterialsColors,
    onClick: () -> Unit,
    onGenerateQuizClick: () -> Unit
) {
    val statusColor = if (item.progress >= 80) colors.success else colors.warning

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
                    text = "📘",
                    fontSize = 23.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.type,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = statusColor.copy(alpha = 0.13f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.28f))
                    ) {
                        Text(
                            text = item.status,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = item.title,
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.subtitle,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "${item.size} • Progress ${item.progress}%",
                    color = colors.textSecondary,
                    fontSize = 11.sp
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
                text = "Generate AI Quiz",
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

            SecondaryButton("Home Dashboard", "Return to main dashboard", "⌂", colors, onOpenHome)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("AI Quizzes", "Open generated quizzes", "🧠", colors, onOpenQuizzes)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Assignments", "Open deadlines and tasks", "📝", colors, onOpenAssignments)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Focus Shield", "Open focus mode", "🛡", colors, onOpenFocus)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Profile", "Open account and pair code", "👤", colors, onOpenProfile)
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
            text = "✓ How Rakizz works\nMaterials are the source for AI quiz generation. The flow is: Material → Detail → Quiz Setup → Quiz Detail.",
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
            Text(
                text = iconText,
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
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
                text = "›",
                color = colors.textSecondary,
                fontSize = 28.sp,
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
            fontSize = 21.sp,
            fontWeight = FontWeight.Black
        )
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
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B)
        )
    }
}

private data class MaterialItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String,
    val size: String,
    val status: String,
    val progress: Int
)

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
    val textPrimary: Color,
    val textSecondary: Color
)