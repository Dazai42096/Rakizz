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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.OutlinedTextField
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAssignmentScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onAssignmentCreated: () -> Unit = {},
    onAssignmentSaved: () -> Unit = {}
) {
    val colors = addAssignmentScreenColors()

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(AssignmentPriority.MEDIUM) }
    var selectedType by remember { mutableStateOf(AssignmentType.HOMEWORK) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

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

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            delay(2300)
            message = ""
        }
    }

    fun goBack() {
        if (navController != null) {
            navController.popBackStack()
        } else {
            onBackClick()
        }
    }

    fun saveAssignment() {
        if (title.isBlank()) {
            isError = true
            message = "Please enter the assignment title."
            return
        }

        if (subject.isBlank()) {
            isError = true
            message = "Please enter the subject."
            return
        }

        if (dueDate.isBlank()) {
            isError = true
            message = "Please enter the due date."
            return
        }

        isError = false
        message = "Assignment saved."

        // These callbacks keep the screen compatible with navigation code.
        onSaveClick()
        onAssignmentCreated()
        onAssignmentSaved()
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
        // Soft animated blue glow.
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
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
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
                title = title,
                subject = subject,
                description = description,
                dueDate = dueDate,
                reminderTime = reminderTime,
                onTitleChange = { title = it },
                onSubjectChange = { subject = it },
                onDescriptionChange = { description = it },
                onDueDateChange = { dueDate = it },
                onReminderTimeChange = { reminderTime = it },
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            OptionSectionCard(
                title = "Assignment Type",
                subtitle = "Choose what kind of task this is",
                colors = colors
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssignmentType.entries.forEach { type ->
                        ChoiceChip(
                            text = type.label,
                            iconText = type.iconText,
                            selected = selectedType == type,
                            colors = colors,
                            onClick = { selectedType = type }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OptionSectionCard(
                title = "Priority Level",
                subtitle = "This helps the student know what to finish first",
                colors = colors
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssignmentPriority.entries.forEach { priority ->
                        PriorityChip(
                            priority = priority,
                            selected = selectedPriority == priority,
                            colors = colors,
                            onClick = { selectedPriority = priority }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = message.isNotEmpty()) {
                MessageCard(
                    message = message,
                    isError = isError,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SaveAssignmentButton(
                colors = colors,
                onClick = { saveAssignment() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            CancelAssignmentButton(
                colors = colors,
                onClick = {
                    onCancelClick()
                    goBack()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            AssignmentPreviewCard(
                title = title,
                subject = subject,
                dueDate = dueDate,
                priority = selectedPriority,
                type = selectedType,
                colors = colors
            )

            Spacer(modifier = Modifier.height(28.dp))
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
                text = "Add Assignment",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Create homework, deadlines, and reminders",
                color = colors.textSecondary,
                fontSize = 13.sp
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
            Column {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = colors.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.32f))
                ) {
                    Text(
                        text = "● Assignment builder",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Turn deadlines into clear study tasks.",
                    color = colors.textPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 31.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This form helps students organize homework and gives parents a clearer view of upcoming school tasks.",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun AssignmentFormCard(
    title: String,
    subject: String,
    description: String,
    dueDate: String,
    reminderTime: String,
    onTitleChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onReminderTimeChange: (String) -> Unit,
    colors: AddAssignmentScreenColors
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
                subtitle = "Fill the important information for this task",
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            FormTextField(
                label = "Assignment title",
                value = title,
                placeholder = "Math worksheet",
                colors = colors,
                onValueChange = onTitleChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                label = "Subject",
                value = subject,
                placeholder = "Mathematics",
                colors = colors,
                onValueChange = onSubjectChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                label = "Description",
                value = description,
                placeholder = "Write a short explanation",
                colors = colors,
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
                    value = dueDate,
                    placeholder = "May 28",
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    onValueChange = onDueDateChange
                )

                FormTextField(
                    label = "Reminder",
                    value = reminderTime,
                    placeholder = "8:00 PM",
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    onValueChange = onReminderTimeChange
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
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
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
private fun OptionSectionCard(
    title: String,
    subtitle: String,
    colors: AddAssignmentScreenColors,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = title,
                subtitle = subtitle,
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
private fun ChoiceChip(
    text: String,
    iconText: String,
    selected: Boolean,
    colors: AddAssignmentScreenColors,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        colors.primary.copy(alpha = 0.18f)
    } else {
        colors.cardAlt
    }

    val borderColor = if (selected) {
        colors.primary.copy(alpha = 0.45f)
    } else {
        colors.border
    }

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = iconText,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.size(7.dp))

            Text(
                text = text,
                color = if (selected) colors.primary else colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PriorityChip(
    priority: AssignmentPriority,
    selected: Boolean,
    colors: AddAssignmentScreenColors,
    onClick: () -> Unit
) {
    val priorityColor = when (priority) {
        AssignmentPriority.LOW -> colors.success
        AssignmentPriority.MEDIUM -> colors.warning
        AssignmentPriority.HIGH -> colors.danger
    }

    val backgroundColor = if (selected) {
        priorityColor.copy(alpha = 0.17f)
    } else {
        colors.cardAlt
    }

    val borderColor = if (selected) {
        priorityColor.copy(alpha = 0.42f)
    } else {
        colors.border
    }

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = priority.label,
            color = if (selected) priorityColor else colors.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
    colors: AddAssignmentScreenColors
) {
    val messageColor = if (isError) colors.danger else colors.success

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
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun SaveAssignmentButton(
    colors: AddAssignmentScreenColors,
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
            text = "Save Assignment",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun CancelAssignmentButton(
    colors: AddAssignmentScreenColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
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
private fun AssignmentPreviewCard(
    title: String,
    subject: String,
    dueDate: String,
    priority: AssignmentPriority,
    type: AssignmentType,
    colors: AddAssignmentScreenColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Live Preview",
                subtitle = "This is how the assignment card will feel",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = colors.cardAlt,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = type.iconText,
                            fontSize = 23.sp
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = if (title.isBlank()) "Assignment title preview" else title,
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = if (subject.isBlank()) "Subject preview" else subject,
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = colors.primary.copy(alpha = 0.13f),
                            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.28f))
                        ) {
                            Text(
                                text = if (dueDate.isBlank()) "Due date" else dueDate,
                                color = colors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(color = colors.border)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Priority: ${priority.label} • Type: ${type.label}",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    )
                }
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
            fontSize = 12.sp
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
            fontSize = 21.sp,
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

private enum class AssignmentPriority(
    val label: String
) {
    LOW("Low Priority"),
    MEDIUM("Medium Priority"),
    HIGH("High Priority")
}

private enum class AssignmentType(
    val label: String,
    val iconText: String
) {
    HOMEWORK("Homework", "📝"),
    EXAM("Exam", "🎯"),
    PROJECT("Project", "💻"),
    READING("Reading", "📚")
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