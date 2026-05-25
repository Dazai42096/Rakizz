package com.rakizz.student.presentation.auth.signup

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Suppress("UNUSED_PARAMETER")
@Composable
fun StudentSignUpScreen(
    navController: NavController? = null,
    viewModel: StudentSignUpViewModel? = null,

    // Back/login callbacks.
    onBackClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onBackToLogin: () -> Unit = {},
    onLoginClick: () -> Unit = {},

    // Names used by RakizzNavHost.kt.
    onCreateAccountClick: () -> Unit = {},
    onAlreadyHaveAccountClick: () -> Unit = {},

    // Success callbacks.
    onSignUpSuccess: () -> Unit = {},
    onSignupSuccess: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
    onAccountCreated: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onOpenHome: () -> Unit = {},

    // Other auth callbacks.
    onParentSignUpClick: () -> Unit = {},
    onChooseRoleClick: () -> Unit = {},
    onContinueAsStudent: () -> Unit = {}
) {
    val colors = studentSignUpColors()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "student_signup_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "student_signup_glow"
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
            onBackToLogin()
        }
    }

    fun createStudentAccount() {
        if (fullName.isBlank()) {
            isError = true
            message = "Please enter your full name."
            return
        }

        if (email.isBlank()) {
            isError = true
            message = "Please enter your email."
            return
        }

        if (password.length < 6) {
            isError = true
            message = "Password must be at least 6 characters."
            return
        }

        if (password != confirmPassword) {
            isError = true
            message = "Passwords do not match."
            return
        }

        if (!acceptTerms) {
            isError = true
            message = "Please accept the account terms."
            return
        }

        isError = false
        message = "Student account created."

        // Keep all common callbacks supported for your NavHost.
        onCreateAccountClick()
        onContinueAsStudent()
        onSignUpSuccess()
        onSignupSuccess()
        onRegisterSuccess()
        onAccountCreated()
        onNavigateToHome()
        onOpenHome()
    }

    fun alreadyHaveAccount() {
        onAlreadyHaveAccountClick()
        onLoginClick()
        goBack()
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
        // Soft animated background glow.
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
                .padding(horizontal = 22.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            StudentSignUpTopBar(
                colors = colors,
                onBackClick = { goBack() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            StudentSignUpLogoSection(
                colors = colors,
                glowScale = glowScale
            )

            Spacer(modifier = Modifier.height(24.dp))

            StudentSignUpHeroCard(colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            StudentAccountFormCard(
                fullName = fullName,
                email = email,
                school = school,
                gradeLevel = gradeLevel,
                password = password,
                confirmPassword = confirmPassword,
                passwordVisible = passwordVisible,
                confirmPasswordVisible = confirmPasswordVisible,
                onFullNameChange = { fullName = it },
                onEmailChange = { email = it },
                onSchoolChange = { school = it },
                onGradeLevelChange = { gradeLevel = it },
                onPasswordChange = { password = it },
                onConfirmPasswordChange = { confirmPassword = it },
                onPasswordVisibleChange = { passwordVisible = !passwordVisible },
                onConfirmPasswordVisibleChange = { confirmPasswordVisible = !confirmPasswordVisible },
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            StudentFeaturesCard(colors = colors)

            Spacer(modifier = Modifier.height(12.dp))

            TermsCard(
                accepted = acceptTerms,
                colors = colors,
                onClick = { acceptTerms = !acceptTerms }
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = message.isNotEmpty()) {
                SignUpMessageCard(
                    message = message,
                    isError = isError,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CreateAccountButton(
                colors = colors,
                onClick = { createStudentAccount() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryAuthButton(
                text = "Already have an account? Login",
                subtitle = "Return to the login screen",
                iconText = "↩",
                colors = colors,
                onClick = { alreadyHaveAccount() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryAuthButton(
                text = "Register as Parent Instead",
                subtitle = "Switch to parent / guardian registration",
                iconText = "👨‍👩‍👧",
                colors = colors,
                onClick = {
                    onParentSignUpClick()
                    onChooseRoleClick()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            StudentSignUpExplanationCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun StudentSignUpTopBar(
    colors: StudentSignUpColors,
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
                text = "Student Sign Up",
                color = colors.textPrimary,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Create your Rakizz learning account",
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
                text = "🎓",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun StudentSignUpLogoSection(
    colors: StudentSignUpColors,
    glowScale: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                }
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary,
                            colors.accent
                        )
                    )
                )
                .border(3.dp, colors.glassBorder, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "R",
                color = Color.White,
                fontSize = 50.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Join Rakizz",
            color = colors.textPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "Build focus. Learn faster.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StudentSignUpHeroCard(colors: StudentSignUpColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.25f),
                            colors.card,
                            colors.cardAlt
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = colors.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.32f))
                ) {
                    Text(
                        text = "● Student learning account",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Your personal space for materials, quizzes, and focus mode.",
                    color = colors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Create a student profile to upload materials, generate AI quizzes, track assignments, and unlock distracting apps through learning.",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun StudentAccountFormCard(
    fullName: String,
    email: String,
    school: String,
    gradeLevel: String,
    password: String,
    confirmPassword: String,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSchoolChange: (String) -> Unit,
    onGradeLevelChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibleChange: () -> Unit,
    onConfirmPasswordVisibleChange: () -> Unit,
    colors: StudentSignUpColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Account Information",
                subtitle = "Fill the main student profile details",
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            SignUpTextField(
                label = "Full name",
                value = fullName,
                placeholder = "Full name",
                keyboardType = KeyboardType.Text,
                colors = colors,
                onValueChange = onFullNameChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            SignUpTextField(
                label = "Email",
                value = email,
                placeholder = "student@email.com",
                keyboardType = KeyboardType.Email,
                colors = colors,
                onValueChange = onEmailChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SignUpTextField(
                    label = "School",
                    value = school,
                    placeholder = "University",
                    keyboardType = KeyboardType.Text,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    onValueChange = onSchoolChange
                )

                SignUpTextField(
                    label = "Level",
                    value = gradeLevel,
                    placeholder = "CS",
                    keyboardType = KeyboardType.Text,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    onValueChange = onGradeLevelChange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SignUpPasswordField(
                label = "Password",
                value = password,
                placeholder = "Create password",
                passwordVisible = passwordVisible,
                colors = colors,
                onValueChange = onPasswordChange,
                onPasswordVisibleChange = onPasswordVisibleChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            SignUpPasswordField(
                label = "Confirm password",
                value = confirmPassword,
                placeholder = "Repeat password",
                passwordVisible = confirmPasswordVisible,
                colors = colors,
                onValueChange = onConfirmPasswordChange,
                onPasswordVisibleChange = onConfirmPasswordVisibleChange
            )
        }
    }
}

@Composable
private fun SignUpTextField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType,
    colors: StudentSignUpColors,
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
private fun SignUpPasswordField(
    label: String,
    value: String,
    placeholder: String,
    passwordVisible: Boolean,
    colors: StudentSignUpColors,
    onValueChange: (String) -> Unit,
    onPasswordVisibleChange: () -> Unit
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
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            Text(
                text = if (passwordVisible) "Hide" else "Show",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable(onClick = onPasswordVisibleChange)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StudentFeaturesCard(colors: StudentSignUpColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Student Features",
                subtitle = "What this account can use inside Rakizz",
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            FeatureRow(
                iconText = "📚",
                title = "Materials Library",
                subtitle = "Upload and organize study materials.",
                colors = colors
            )

            FeatureDivider(colors)

            FeatureRow(
                iconText = "🧠",
                title = "AI Quizzes",
                subtitle = "Generate practice questions from uploaded materials.",
                colors = colors
            )

            FeatureDivider(colors)

            FeatureRow(
                iconText = "🛡",
                title = "Focus Unlock",
                subtitle = "Use quizzes to unlock distracting apps during focus rules.",
                colors = colors
            )
        }
    }
}

@Composable
private fun FeatureRow(
    iconText: String,
    title: String,
    subtitle: String,
    colors: StudentSignUpColors
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
            Text(text = iconText, fontSize = 20.sp)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun FeatureDivider(colors: StudentSignUpColors) {
    HorizontalDivider(
        color = colors.border,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 56.dp, top = 10.dp, bottom = 10.dp)
    )
}

@Composable
private fun TermsCard(
    accepted: Boolean,
    colors: StudentSignUpColors,
    onClick: () -> Unit
) {
    val mainColor = if (accepted) colors.primary else colors.textSecondary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(mainColor.copy(alpha = 0.13f))
                    .border(1.dp, mainColor.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (accepted) "✓" else "○",
                    color = mainColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = "I agree to create a Rakizz student account for learning and focus features.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun SignUpMessageCard(
    message: String,
    isError: Boolean,
    colors: StudentSignUpColors
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
private fun CreateAccountButton(
    colors: StudentSignUpColors,
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
            text = "Create Student Account",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun SecondaryAuthButton(
    text: String,
    subtitle: String,
    iconText: String,
    colors: StudentSignUpColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.card,
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
                    color = colors.primary,
                    fontSize = 20.sp,
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
                text = "›",
                color = colors.textSecondary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun StudentSignUpExplanationCard(colors: StudentSignUpColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "How Rakizz works",
                color = colors.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "This screen supports standard user management by creating a student account. The student account is used for materials, quizzes, assignments, profile, and focus mode.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    colors: StudentSignUpColors
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
    colors: StudentSignUpColors,
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
private fun studentSignUpColors(): StudentSignUpColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        StudentSignUpColors(
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
        StudentSignUpColors(
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

private data class StudentSignUpColors(
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