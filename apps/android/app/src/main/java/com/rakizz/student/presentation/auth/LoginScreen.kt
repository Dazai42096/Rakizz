package com.rakizz.student.presentation.auth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.presentation.common.UiState
import kotlinx.coroutines.delay

@Suppress("UNUSED_PARAMETER")
@Composable
fun LoginScreen(
    navController: NavController? = null,
    viewModel: LoginViewModel? = null,

    // NavHost expects a String role here.
    // Example values: "student" or "parent".
    onLoginSuccess: (String) -> Unit = {},

    onLoginClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onOpenHome: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    onChooseRoleClick: () -> Unit = {},
    onNavigateToChooseRole: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onContinueAsGuestClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val colors = loginScreenColors()
    val loginViewModel = viewModel ?: hiltViewModel<LoginViewModel>()
    val uiState by loginViewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "login_animation")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "login_glow"
    )

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            delay(2300)
            message = ""
        }
    }

    fun goHomeAfterLogin(role: String = "student") {
        onLoginClick()

        // Send role back to RakizzNavHost.
        // This fixes the ignoreCase error from the build.
        onLoginSuccess(role)

        onNavigateToHome()
        onOpenHome()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                isError = false
                message = "Login successful."
                goHomeAfterLogin(state.data.role)
            }
            is UiState.Error -> {
                isError = true
                message = state.message
                loginViewModel.clearError()
            }
            else -> Unit
        }
    }

    fun openSignUpFlow() {
        onSignUpClick()
        onNavigateToSignUp()
        onCreateAccountClick()
        onChooseRoleClick()
        onNavigateToChooseRole()
    }

    fun submitLogin() {
        if (email.isBlank()) {
            isError = true
            message = "Please enter your email."
            return
        }

        if (password.isBlank()) {
            isError = true
            message = "Please enter your password."
            return
        }

        isError = false
        message = ""
        loginViewModel.login(email, password)
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
        // Animated glass glow.
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(26.dp))

            LoginLogoSection(
                colors = colors,
                glowScale = glowScale
            )

            Spacer(modifier = Modifier.height(26.dp))

            LoginHeroCard(colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            LoginFormCard(
                email = email,
                password = password,
                passwordVisible = passwordVisible,
                rememberMe = rememberMe,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onPasswordVisibleChange = { passwordVisible = !passwordVisible },
                onRememberMeChange = { rememberMe = !rememberMe },
                onForgotPasswordClick = {
                    isError = false
                    message = "Password recovery is not connected yet."
                    onForgotPasswordClick()
                },
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = message.isNotEmpty()) {
                LoginMessageCard(
                    message = message,
                    isError = isError,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            MainLoginButton(
                text = if (uiState == UiState.Loading) "Logging in..." else "Login to Rakizz",
                colors = colors,
                onClick = { if (uiState != UiState.Loading) submitLogin() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryLoginButton(
                text = "Create New Account",
                subtitle = "Student or parent registration",
                iconText = "ï¼‹",
                colors = colors,
                onClick = { openSignUpFlow() }
            )


            Spacer(modifier = Modifier.height(18.dp))

            AuthInfoCard(colors = colors)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun LoginLogoSection(
    colors: LoginScreenColors,
    glowScale: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(112.dp)
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
                text = "R",
                color = Color.White,
                fontSize = 54.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Rakizz",
            color = colors.textPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "Study smarter. Stay focused.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoginHeroCard(colors: LoginScreenColors) {
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
                    color = colors.success.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, colors.success.copy(alpha = 0.32f))
                ) {
                    Text(
                        text = "â— Secure learning access",
                        color = colors.success,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Welcome back to your focus learning system.",
                    color = colors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Login to access materials, quizzes, assignments, focus mode, and parent-student linking.",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun LoginFormCard(
    email: String,
    password: String,
    passwordVisible: Boolean,
    rememberMe: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibleChange: () -> Unit,
    onRememberMeChange: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    colors: LoginScreenColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                title = "Account Login",
                subtitle = "Use your student or parent account",
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                label = "Email",
                value = email,
                placeholder = "student@email.com",
                keyboardType = KeyboardType.Email,
                colors = colors,
                onValueChange = onEmailChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthPasswordField(
                label = "Password",
                value = password,
                placeholder = "Enter your password",
                passwordVisible = passwordVisible,
                colors = colors,
                onValueChange = onPasswordChange,
                onPasswordVisibleChange = onPasswordVisibleChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RememberMeChip(
                    selected = rememberMe,
                    colors = colors,
                    onClick = onRememberMeChange
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Forgot password?",
                    color = colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onForgotPasswordClick)
                )
            }
        }
    }
}

@Composable
private fun AuthTextField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType,
    colors: LoginScreenColors,
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
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthPasswordField(
    label: String,
    value: String,
    placeholder: String,
    passwordVisible: Boolean,
    colors: LoginScreenColors,
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
private fun RememberMeChip(
    selected: Boolean,
    colors: LoginScreenColors,
    onClick: () -> Unit
) {
    val mainColor = if (selected) colors.primary else colors.textSecondary

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = mainColor.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, mainColor.copy(alpha = 0.28f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selected) "âœ“" else "â—‹",
                color = mainColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.size(6.dp))

            Text(
                text = "Remember me",
                color = mainColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoginMessageCard(
    message: String,
    isError: Boolean,
    colors: LoginScreenColors
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
private fun MainLoginButton(
    text: String,
    colors: LoginScreenColors,
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
private fun SecondaryLoginButton(
    text: String,
    subtitle: String,
    iconText: String,
    colors: LoginScreenColors,
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
                text = "â€º",
                color = colors.textSecondary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun AuthInfoCard(colors: LoginScreenColors) {
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
                text = "This login screen is the entry point for Rakizz. After login, the student can access materials, AI quizzes, assignments, profile, and focus mode.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = colors.border)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sign in to sync your materials, quizzes, assignments, and focus settings securely.",
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
    colors: LoginScreenColors
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
private fun loginScreenColors(): LoginScreenColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        LoginScreenColors(
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
        LoginScreenColors(
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

private data class LoginScreenColors(
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
