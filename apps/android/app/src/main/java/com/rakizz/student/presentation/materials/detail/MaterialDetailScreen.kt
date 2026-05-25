package com.rakizz.student.presentation.materials.detail

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Suppress("UNUSED_PARAMETER")
@Composable
fun MaterialDetailScreen(
    navController: NavController? = null,
    viewModel: Any? = null,

    // Material id from navigation
    materialId: String? = null,
    id: String? = null,

    // Back callbacks
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},

    // This fixes RakizzNavHost.kt onDownloadClick
    onDownloadClick: () -> Unit = {},

    // Other material actions
    onPreviewClick: () -> Unit = {},
    onOpenPreviewClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},

    // Quiz callbacks
    onGenerateQuizClick: (String) -> Unit = {},
    onCreateQuizClick: () -> Unit = {},
    onStartQuizClick: () -> Unit = {},
    onOpenQuizSetupClick: () -> Unit = {},
    onNavigateToQuizSetup: () -> Unit = {},
    onNavigateToQuizSetupClick: () -> Unit = {},

    // Navigation callbacks
    onOpenMaterials: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenHome: () -> Unit = {},
    onOpenQuizzes: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val colors = detailColors()
    val finalId = materialId ?: id ?: "material-preview-001"

    var message by remember { mutableStateOf("") }

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

    fun openQuizSetup() {
        message = "Opening AI Quiz Studio."

        onGenerateQuizClick(finalId)
        onCreateQuizClick()
        onStartQuizClick()
        onOpenQuizSetupClick()
        onNavigateToQuizSetup()
        onNavigateToQuizSetupClick()
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
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleButton("←", colors) {
                    goBack()
                }

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
                        text = "Preview, download, and generate quizzes",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }

                CircleButton("↻", colors) {
                    message = "Material refreshed."
                    onRefreshClick()
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            MaterialHeroCard(
                materialId = finalId,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (message.isNotEmpty()) {
                MessageCard(
                    message = message,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            ActionCard(
                colors = colors,
                onGenerateQuiz = {
                    openQuizSetup()
                },
                onPreview = {
                    message = "Preview opened."
                    onPreviewClick()
                    onOpenPreviewClick()
                },
                onDownload = {
                    message = "Download clicked."
                    onDownloadClick()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(
                finalId = finalId,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            NavigationCard(
                colors = colors,
                onOpenHome = onOpenHome,
                onOpenMaterials = {
                    onOpenMaterials()
                    onOpenLibrary()
                },
                onOpenQuizzes = onOpenQuizzes,
                onOpenFocus = onOpenFocus,
                onOpenProfile = onOpenProfile
            )

            Spacer(modifier = Modifier.height(16.dp))

            DangerButton(colors = colors) {
                message = "Delete clicked."
                onDeleteClick()
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MaterialHeroCard(
    materialId: String,
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
                    text = "📘",
                    fontSize = 44.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Study Material",
                color = colors.textPrimary,
                fontSize = 27.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ID: $materialId",
                color = colors.textSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "This material can be previewed, downloaded, and converted into an AI-generated quiz.",
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
    colors: DetailColors
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
private fun ActionCard(
    colors: DetailColors,
    onGenerateQuiz: () -> Unit,
    onPreview: () -> Unit,
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
                subtitle = "Use this material inside Rakizz",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            MainButton(
                text = "Generate AI Quiz",
                colors = colors,
                onClick = onGenerateQuiz
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Preview Material",
                subtitle = "Open the file inside the app",
                icon = "👁",
                colors = colors,
                onClick = onPreview
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "Download Material",
                subtitle = "Save or open the material source",
                icon = "⬇",
                colors = colors,
                onClick = onDownload
            )
        }
    }
}

@Composable
private fun InfoCard(
    finalId: String,
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
                subtitle = "Simple metadata for app overview",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            InfoLine("🆔", "Material ID", finalId, colors)
            InfoLine("📄", "Type", "Uploaded study file", colors)
            InfoLine("🧠", "AI Usage", "Can generate MCQ quizzes", colors)
            InfoLine("🔗", "Flow", "Material → Quiz Setup → Quiz Detail", colors)
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 22.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        Column {
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
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun NavigationCard(
    colors: DetailColors,
    onOpenHome: () -> Unit,
    onOpenMaterials: () -> Unit,
    onOpenQuizzes: () -> Unit,
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
                subtitle = "Move to related screens",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryButton("Home Dashboard", "Return to dashboard", "⌂", colors, onOpenHome)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Materials Library", "Back to all materials", "📚", colors, onOpenMaterials)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("AI Quizzes", "Open quiz list", "🧠", colors, onOpenQuizzes)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Focus Shield", "Open focus mode", "🛡", colors, onOpenFocus)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton("Profile", "Open account screen", "👤", colors, onOpenProfile)
        }
    }
}

@Composable
private fun DangerButton(
    colors: DetailColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = colors.danger.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.32f))
    ) {
        Text(
            text = "🗑  Delete Material",
            color = colors.danger,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MainButton(
    text: String,
    colors: DetailColors,
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
    icon: String,
    colors: DetailColors,
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
                text = icon,
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
            fontSize = 21.sp,
            fontWeight = FontWeight.Black
        )
    }
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
            danger = Color(0xFFDC2626),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B)
        )
    }
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
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color
)
