package com.rakizz.student.presentation.profile

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Suppress("UNUSED_PARAMETER")
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    viewModel: Any? = null,

    // Back callbacks.
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},

    // Profile callbacks.
    onEditProfileClick: () -> Unit = {},
    onSaveProfileClick: () -> Unit = {},
    onUpdateProfileClick: () -> Unit = {},
    onProfileSaved: () -> Unit = {},
    onRefreshClick: () -> Unit = {},

    // Pair code callbacks.
    onPairCodeClick: () -> Unit = {},
    onOpenPairCode: () -> Unit = {},
    onOpenPairCodeClick: () -> Unit = {},
    onNavigateToPairCode: () -> Unit = {},
    onGeneratePairCodeClick: () -> Unit = {},

    // Navigation callbacks.
    onOpenHome: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenMaterials: () -> Unit = {},
    onOpenQuizzes: () -> Unit = {},
    onOpenAssignments: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProgress: () -> Unit = {},

    // Auth/session callbacks.
    onLogoutClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val colors = profileColors()

    var fullName by remember { mutableStateOf("Azmi Student") }
    var email by remember { mutableStateOf("student@rakizz.com") }
    var role by remember { mutableStateOf("Student") }
    var school by remember { mutableStateOf("University of Petra") }
    var level by remember { mutableStateOf("Computer Science") }
    var pairCode by remember { mutableStateOf("RKZ-428-961") }

    var editMode by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "profile_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "profile_glow"
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
            onNavigateBack()
        }
    }

    fun saveProfile() {
        if (fullName.isBlank()) {
            isError = true
            message = "Name cannot be empty."
            return
        }

        if (email.isBlank()) {
            isError = true
            message = "Email cannot be empty."
            return
        }

        isError = false
        message = "Profile saved for demo."
        editMode = false

        onSaveProfileClick()
        onUpdateProfileClick()
        onProfileSaved()
    }

    fun openPairCode() {
        onPairCodeClick()
        onOpenPairCode()
        onOpenPairCodeClick()
        onNavigateToPairCode()
    }

    fun generateNewPairCode() {
        pairCode = "RKZ-726-314"
        isError = false
        message = "New pair code generated."
        onGeneratePairCodeClick()
    }

    fun logout() {
        onLogoutClick()
        onLogout()
        onSignOutClick()
        onNavigateToLogin()
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
        // Soft animated profile glow.
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-135).dp)
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

            ProfileTopBar(
                colors = colors,
                onBackClick = { goBack() },
                onRefreshClick = {
                    isError = false
                    message = "Profile refreshed for demo."
                    onRefreshClick()
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            ProfileHeroCard(
                fullName = fullName,
                email = email,
                role = role,
                pairCode = pairCode,
                glowScale = glowScale,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = message.isNotEmpty()) {
                ProfileMessageCard(
                    message = message,
                    isError = isError,
                    colors = colors
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            ProfileFormCard(
                editMode = editMode,
                fullName = fullName,
                email = email,
                role = role,
                school = school,
                level = level,
                onFullNameChange = { fullName = it },
                onEmailChange = { email = it },
                onRoleChange = { role = it },
                onSchoolChange = { school = it },
                onLevelChange = { level = it },
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (editMode) {
                MainGradientButton(
                    text = "Save Profile",
                    colors = colors,
                    onClick = { saveProfile() }
                )
            } else {
                MainGradientButton(
                    text = "Edit Profile",
                    colors = colors,
                    onClick = {
                        editMode = true
                        onEditProfileClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PairCodeCard(
                pairCode = pairCode,
                colors = colors,
                onOpenPairCodeClick = { openPairCode() },
                onGeneratePairCodeClick = { generateNewPairCode() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileStatsCard(colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            ProfileNavigationCard(
                colors = colors,
                onOpenHome = onOpenHome,
                onOpenLibrary = {
                    onOpenLibrary()
                    onOpenMaterials()
                },
                onOpenQuizzes = onOpenQuizzes,
                onOpenAssignments = onOpenAssignments,
                onOpenFocus = onOpenFocus,
                onOpenProgress = onOpenProgress
            )

            Spacer(modifier = Modifier.height(16.dp))

            LogoutCard(
                colors = colors,
                onClick = { logout() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileExplanationCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun StudentProfileScreen(
    navController: NavController? = null,
    viewModel: Any? = null,
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSaveProfileClick: () -> Unit = {},
    onUpdateProfileClick: () -> Unit = {},
    onProfileSaved: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onPairCodeClick: () -> Unit = {},
    onOpenPairCode: () -> Unit = {},
    onOpenPairCodeClick: () -> Unit = {},
    onNavigateToPairCode: () -> Unit = {},
    onGeneratePairCodeClick: () -> Unit = {},
    onOpenHome: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenMaterials: () -> Unit = {},
    onOpenQuizzes: () -> Unit = {},
    onOpenAssignments: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProgress: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    ProfileScreen(
        navController = navController,
        viewModel = viewModel,
        onBackClick = onBackClick,
        onNavigateBack = onNavigateBack,
        onEditProfileClick = onEditProfileClick,
        onSaveProfileClick = onSaveProfileClick,
        onUpdateProfileClick = onUpdateProfileClick,
        onProfileSaved = onProfileSaved,
        onRefreshClick = onRefreshClick,
        onPairCodeClick = onPairCodeClick,
        onOpenPairCode = onOpenPairCode,
        onOpenPairCodeClick = onOpenPairCodeClick,
        onNavigateToPairCode = onNavigateToPairCode,
        onGeneratePairCodeClick = onGeneratePairCodeClick,
        onOpenHome = onOpenHome,
        onOpenLibrary = onOpenLibrary,
        onOpenMaterials = onOpenMaterials,
        onOpenQuizzes = onOpenQuizzes,
        onOpenAssignments = onOpenAssignments,
        onOpenFocus = onOpenFocus,
        onOpenProgress = onOpenProgress,
        onLogoutClick = onLogoutClick,
        onLogout = onLogout,
        onSignOutClick = onSignOutClick,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
private fun ProfileTopBar(
    colors: ProfileColors,
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
                text = "Profile",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Account, pair code, and progress identity",
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
private fun ProfileHeroCard(
    fullName: String,
    email: String,
    role: String,
    pairCode: String,
    glowScale: Float,
    colors: ProfileColors
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .graphicsLayer {
                            scaleX = glowScale
                            scaleY = glowScale
                        }
                        .clip(RoundedCornerShape(34.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    colors.primary,
                                    colors.accent
                                )
                            )
                        )
                        .border(3.dp, colors.glassBorder, RoundedCornerShape(34.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fullName.firstOrNull()?.uppercase() ?: "R",
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = fullName,
                    color = colors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email,
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniPill(
                        text = role,
                        color = colors.primary
                    )

                    MiniPill(
                        text = pairCode,
                        color = colors.success
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMessageCard(
    message: String,
    isError: Boolean,
    colors: ProfileColors
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
            modifier = Modifier.padding(14.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileFormCard(
    editMode: Boolean,
    fullName: String,
    email: String,
    role: String,
    school: String,
    level: String,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onSchoolChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    colors: ProfileColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Account Information",
                subtitle = if (editMode) "Edit your profile details" else "Saved profile details",
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (editMode) {
                ProfileTextField(
                    label = "Full name",
                    value = fullName,
                    placeholder = "Full name",
                    keyboardType = KeyboardType.Text,
                    colors = colors,
                    onValueChange = onFullNameChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileTextField(
                    label = "Email",
                    value = email,
                    placeholder = "Email",
                    keyboardType = KeyboardType.Email,
                    colors = colors,
                    onValueChange = onEmailChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileTextField(
                        label = "Role",
                        value = role,
                        placeholder = "Student",
                        keyboardType = KeyboardType.Text,
                        colors = colors,
                        modifier = Modifier.weight(1f),
                        onValueChange = onRoleChange
                    )

                    ProfileTextField(
                        label = "Level",
                        value = level,
                        placeholder = "CS",
                        keyboardType = KeyboardType.Text,
                        colors = colors,
                        modifier = Modifier.weight(1f),
                        onValueChange = onLevelChange
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ProfileTextField(
                    label = "School / University",
                    value = school,
                    placeholder = "School",
                    keyboardType = KeyboardType.Text,
                    colors = colors,
                    onValueChange = onSchoolChange
                )
            } else {
                ProfileInfoRow(
                    iconText = "👤",
                    title = "Full name",
                    value = fullName,
                    colors = colors
                )

                InfoDivider(colors)

                ProfileInfoRow(
                    iconText = "✉",
                    title = "Email",
                    value = email,
                    colors = colors
                )

                InfoDivider(colors)

                ProfileInfoRow(
                    iconText = "🎓",
                    title = "Role",
                    value = role,
                    colors = colors
                )

                InfoDivider(colors)

                ProfileInfoRow(
                    iconText = "🏫",
                    title = "School / University",
                    value = school,
                    colors = colors
                )

                InfoDivider(colors)

                ProfileInfoRow(
                    iconText = "💻",
                    title = "Level / Major",
                    value = level,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType,
    colors: ProfileColors,
    modifier: Modifier = Modifier,
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
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun ProfileInfoRow(
    iconText: String,
    title: String,
    value: String,
    colors: ProfileColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colors.primary.copy(alpha = 0.14f))
                .border(1.dp, colors.primary.copy(alpha = 0.28f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconText,
                color = colors.primary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
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
private fun InfoDivider(colors: ProfileColors) {
    HorizontalDivider(
        color = colors.border,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 56.dp, top = 10.dp, bottom = 10.dp)
    )
}

@Composable
private fun PairCodeCard(
    pairCode: String,
    colors: ProfileColors,
    onOpenPairCodeClick: () -> Unit,
    onGeneratePairCodeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Parent Pair Code",
                subtitle = "Use this code to connect a parent account to this student",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = colors.primary.copy(alpha = 0.13f),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.32f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = pairCode,
                        color = colors.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Share this with the parent / guardian",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryProfileButton(
                text = "Open Pair Code Screen",
                subtitle = "Show the full linking screen",
                iconText = "🔗",
                colors = colors,
                onClick = onOpenPairCodeClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryProfileButton(
                text = "Generate New Code",
                subtitle = "Create a new demo pair code",
                iconText = "↻",
                colors = colors,
                onClick = onGeneratePairCodeClick
            )
        }
    }
}

@Composable
private fun ProfileStatsCard(colors: ProfileColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileStatCard(
            title = "Quizzes",
            value = "12",
            subtitle = "completed",
            iconText = "🧠",
            mainColor = colors.primary,
            colors = colors,
            modifier = Modifier.weight(1f)
        )

        ProfileStatCard(
            title = "Focus",
            value = "8h",
            subtitle = "this week",
            iconText = "🛡",
            mainColor = colors.success,
            colors = colors,
            modifier = Modifier.weight(1f)
        )

        ProfileStatCard(
            title = "Tasks",
            value = "5",
            subtitle = "done",
            iconText = "✓",
            mainColor = colors.accent,
            colors = colors,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    subtitle: String,
    iconText: String,
    mainColor: Color,
    colors: ProfileColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardAlt),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = iconText,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = mainColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ProfileNavigationCard(
    colors: ProfileColors,
    onOpenHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProgress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Profile Navigation",
                subtitle = "Move to important Rakizz screens",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryProfileButton("Home Dashboard", "Return to main dashboard", "⌂", colors, onOpenHome)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProfileButton("Materials Library", "Open uploaded study materials", "📚", colors, onOpenLibrary)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProfileButton("AI Quizzes", "Open generated quiz repository", "🧠", colors, onOpenQuizzes)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProfileButton("Assignments", "Open homework and deadlines", "📝", colors, onOpenAssignments)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProfileButton("Focus Shield", "Open app blocking and quiz unlock", "🛡", colors, onOpenFocus)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryProfileButton("Progress", "Open student progress screen", "📊", colors, onOpenProgress)
        }
    }
}

@Composable
private fun LogoutCard(
    colors: ProfileColors,
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
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🚪",
                fontSize = 25.sp
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "Logout",
                    color = colors.danger,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "End current session and return to login",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "›",
                color = colors.danger,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ProfileExplanationCard(colors: ProfileColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.success.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, colors.success.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "✓",
                color = colors.success,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "Checkpoint explanation",
                    color = colors.success,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "This screen supports user management. It also shows the pair code used to link a parent account with a student account.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun MainGradientButton(
    text: String,
    colors: ProfileColors,
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
private fun SecondaryProfileButton(
    text: String,
    subtitle: String,
    iconText: String,
    colors: ProfileColors,
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(colors.primary.copy(alpha = 0.14f))
                    .border(1.dp, colors.primary.copy(alpha = 0.28f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    fontSize = 20.sp
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
                text = "›",
                color = colors.textSecondary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun MiniPill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    colors: ProfileColors
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
    colors: ProfileColors,
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
private fun profileColors(): ProfileColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        ProfileColors(
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
            textSecondary = Color(0xFF94A3B8),
            glassBorder = Color(0x66FFFFFF)
        )
    } else {
        ProfileColors(
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
            textSecondary = Color(0xFF64748B),
            glassBorder = Color(0xFFFFFFFF)
        )
    }
}

private data class ProfileColors(
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
    val textSecondary: Color,
    val glassBorder: Color
)