package com.rakizz.student.presentation.assignments.add

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun AddAssignmentScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onAssignmentCreated: () -> Unit = {},
    onAssignmentSaved: () -> Unit = {},
    viewModel: AddAssignmentViewModel = hiltViewModel()
) {
    val colors = addAssignmentScreenColors()
    val uiState by viewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "add_assignment_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "add_assignment_glow"
    )

    fun goBack() {
        if (navController != null) {
            navController.popBackStack()
        } else {
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddAssignmentEvent.Saved -> {
                    onSaveClick()
                    onAssignmentCreated()
                    onAssignmentSaved()

                    if (navController != null) {
                        navController.popBackStack()
                    } else {
                        onBackClick()
                    }
                }
            }
        }
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
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = (-110).dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                    alpha = 0.85f
                }
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.38f),
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
                .padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            AddAssignmentTopBar(
                colors = colors,
                onBackClick = { goBack() }
            )

            Spacer(modifier = Modifier.height(22.dp))

            AddAssignmentHeroCard(colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            AssignmentFormCard(
                state = uiState,
                colors = colors,
                onTitleChange = viewModel::onTitleChanged,
                onDescriptionChange = viewModel::onDescriptionChanged,
                onDueDateChange = viewModel::onDueDateChanged,
                onDueTimeChange = viewModel::onDueTimeChanged
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = uiState.inputError != null || uiState.submitError != null
            ) {
                MessageCard(
                    message = uiState.inputError ?: uiState.submitError.orEmpty(),
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SaveAssignmentButton(
                isSaving = uiState.isSaving,
                colors = colors,
                onClick = {
                    viewModel.saveAssignment()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            CancelAssignmentButton(
                colors = colors,
                enabled = !uiState.isSaving,
                onClick = {
                    onCancelClick()
                    goBack()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            BackendFormatInfoCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }

        if (uiState.isSaving) {
            SavingOverlay(colors = colors)
        }
    }
}

@Composable
private fun AddAssignmentTopBar(
    colors: AddAssignmentScreenColors,
    onBackClick: () -> Unit
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
                text = "Add Assignment",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Save a real assignment to the backend",
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.14f))
                .border(1.dp, colors.primary.copy(alpha = 0.32f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = colors.primary,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun AddAssignmentHeroCard(colors: AddAssignmentScreenColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.28f),
                            colors.card,
                            colors.cardAlt
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = colors.success.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, colors.success.copy(alpha = 0.32f))
            ) {
                Text(
                    text = "Backend assignment create",
                    color = colors.success,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create homework that actually persists.",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 31.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This form calls the Rakizz backend. After saving, the assignment appears in the Assignments screen.",
                color = colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun AssignmentFormCard(
    state: AddAssignmentUiState,
    colors: AddAssignmentScreenColors,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onDueTimeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Assignment Details",
                subtitle = "These fields are sent to the backend",
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            FormTextField(
                label = "Assignment title",
                value = state.title,
                placeholder = "Math worksheet",
                colors = colors,
                enabled = !state.isSaving,
                onValueChange = onTitleChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                label = "Description",
                value = state.description,
                placeholder = "Solve questions 1 to 10",
                colors = colors,
                enabled = !state.isSaving,
                minLines = 3,
                onValueChange = onDescriptionChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormTextField(
                    label = "Due date",
                    value = state.dueDate,
                    placeholder = "2026-05-28",
                    colors = colors,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                    onValueChange = onDueDateChange
                )

                FormTextField(
                    label = "Due time",
                    value = state.dueTime,
                    placeholder = "20:00",
                    colors = colors,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                    onValueChange = onDueTimeChange
                )
            }
        }
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    placeholder: String,
    colors: AddAssignmentScreenColors,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        enabled = enabled,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = colors.textSecondary
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = colors.textSecondary.copy(alpha = 0.65f)
            )
        },
        minLines = minLines,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun MessageCard(
    message: String,
    colors: AddAssignmentScreenColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.danger.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.32f))
    ) {
        Text(
            text = message,
            color = colors.danger,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun SaveAssignmentButton(
    isSaving: Boolean,
    colors: AddAssignmentScreenColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSaving) {
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.45f),
                            colors.accent.copy(alpha = 0.45f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            colors.primary,
                            colors.accent
                        )
                    )
                }
            )
            .clickable(enabled = !isSaving, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isSaving) "Saving..." else "Save Assignment",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun CancelAssignmentButton(
    colors: AddAssignmentScreenColors,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = colors.cardAlt,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Cancel",
                color = colors.textSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BackendFormatInfoCard(colors: AddAssignmentScreenColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.primary.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.28f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Date format",
                color = colors.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Use due date as YYYY-MM-DD and due time as HH:mm, for example 2026-05-28 and 20:00.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SavingOverlay(colors: AddAssignmentScreenColors) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBottom.copy(alpha = 0.90f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = colors.primary)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Saving assignment...",
                    color = colors.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    colors: AddAssignmentScreenColors
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
    colors: AddAssignmentScreenColors,
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

@Composable
private fun addAssignmentScreenColors(): AddAssignmentScreenColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        AddAssignmentScreenColors(
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
        AddAssignmentScreenColors(
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

private data class AddAssignmentScreenColors(
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