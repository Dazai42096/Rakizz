package com.rakizz.student.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.presentation.common.UiState

private val ScreenTopColor = Color(0xFF020B1F)
private val ScreenBottomColor = Color(0xFF000000)
private val PrimaryBlue = Color(0xFF1F5BDE)
private val WhiteText = Color(0xFFF5F7FB)
private val SecondaryText = Color(0xFF8C93A3)
private val FieldContainer = Color(0xFF171717)
private val FieldBorder = Color(0xFF2B2E35)
private val ErrorText = Color(0xFFFF6B6B)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val isLoading = state is UiState.Loading
    val errorMessage = (state as? UiState.Error)?.message

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        email = email,
        password = password,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onEmailChange = {
            email = it
            viewModel.clearError()
        },
        onPasswordChange = {
            password = it
            viewModel.clearError()
        },
        onLoginClick = {
            viewModel.login(
                email = email.trim(),
                pass = password
            )
        },
        onForgotPasswordClick = onForgotPasswordClick,
        onCreateAccountClick = onCreateAccountClick
    )
}

@Composable
private fun LoginScreenContent(
    email: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ScreenTopColor, ScreenBottomColor)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(46.dp))

            RakizzHeader()

            Spacer(modifier = Modifier.height(188.dp))

            Text(
                text = "Welcome back",
                color = WhiteText,
                fontSize = 40.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Sign in to continue your focus journey.",
                color = SecondaryText,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(56.dp))

            RakizzAuthTextField(
                value = email,
                placeholder = "Email",
                onValueChange = onEmailChange,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            RakizzAuthTextField(
                value = password,
                placeholder = "Password",
                onValueChange = onPasswordChange,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onLoginClick()
                    }
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            SignInButton(
                isLoading = isLoading,
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onLoginClick()
                }
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = errorMessage,
                    color = ErrorText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(54.dp))

            Text(
                text = "Forgot password?",
                color = SecondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onForgotPasswordClick() }
            )

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "or",
                color = SecondaryText.copy(alpha = 0.75f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(38.dp))

            Text(
                text = "Create account",
                color = WhiteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onCreateAccountClick() }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RakizzHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryBlue.copy(alpha = 0.32f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(45f)
                        .background(
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(6.dp)
                        )
                )

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Rakizz")
                        pushStyle(SpanStyle(color = PrimaryBlue))
                        append("!!")
                        pop()
                    },
                    color = WhiteText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun RakizzAuthTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        placeholder = {
            Text(
                text = placeholder,
                color = SecondaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = WhiteText,
            unfocusedTextColor = WhiteText,
            disabledTextColor = WhiteText.copy(alpha = 0.6f),
            focusedContainerColor = FieldContainer,
            unfocusedContainerColor = FieldContainer,
            disabledContainerColor = FieldContainer,
            focusedPlaceholderColor = SecondaryText,
            unfocusedPlaceholderColor = SecondaryText,
            cursorColor = WhiteText,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .border(
                width = 1.dp,
                color = FieldBorder,
                shape = RoundedCornerShape(20.dp)
            )
    )
}

@Composable
private fun SignInButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .offset(y = 2.dp)
                .background(
                    color = PrimaryBlue.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(22.dp)
                )
        )

        Button(
            onClick = onClick,
            enabled = !isLoading,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                disabledContainerColor = PrimaryBlue.copy(alpha = 0.65f)
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = WhiteText,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = "Sign in",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}