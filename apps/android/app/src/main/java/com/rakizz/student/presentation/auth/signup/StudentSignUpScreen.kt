package com.rakizz.student.presentation.auth.signup

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PersonAddAlt1
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.presentation.common.UiState

private val ScreenTop = Color(0xFF03112A)
private val ScreenBottom = Color(0xFF000000)
private val WhiteText = Color(0xFFF7F8FA)
private val SecondaryText = Color(0xFF8A92A3)
private val HintText = Color(0xFF5E667A)
private val FieldSurface = Color(0xFF171717)
private val CardSurface = Color(0xFF11151E)
private val CardBorder = Color(0xFF2A2F3A)
private val PrimaryBlue = Color(0xFF2457D6)
private val BlueSoft = Color(0xFF6EA7FF)
private val GreenAccent = Color(0xFF30D158)
private val ErrorText = Color(0xFFFF6B6B)

@Composable
fun StudentSignUpScreen(
    onBackClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onAlreadyHaveAccountClick: () -> Unit = {},
    viewModel: StudentSignUpViewModel = hiltViewModel()
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var selectedGrade by rememberSaveable { mutableStateOf("Grade 10") }
    var parentCode by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showGradeMenu by remember { mutableStateOf(false) }

    val grades = listOf(
        "Grade 7",
        "Grade 8",
        "Grade 9",
        "Grade 10",
        "Grade 11",
        "Grade 12"
    )

    val state by viewModel.uiState.collectAsState()
    val isLoading = state is UiState.Loading
    val errorMessage = (state as? UiState.Error)?.message

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            onCreateAccountClick()
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(ScreenTop, ScreenBottom)
                    )
                )
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 18.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                StudentSignUpTopBar(
                    onBackClick = onBackClick
                )
            }

            item {
                StudentSignUpHero()
            }

            item {
                FieldLabel("FULL NAME")
            }

            item {
                SignUpTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        viewModel.clearError()
                    },
                    placeholder = "Enter your full name",
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words
                )
            }

            item {
                FieldLabel("EMAIL")
            }

            item {
                SignUpTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        viewModel.clearError()
                    },
                    placeholder = "Enter your email",
                    keyboardType = KeyboardType.Email
                )
            }

            item {
                FieldLabel("PASSWORD")
            }

            item {
                SignUpTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.clearError()
                    },
                    placeholder = "Create a password",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailing = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = "Toggle password",
                                tint = SecondaryText
                            )
                        }
                    }
                )
            }

            item {
                FieldLabel("GRADE")
            }

            item {
                GradeSelectorField(
                    selectedGrade = selectedGrade,
                    expanded = showGradeMenu,
                    onExpandedChange = { showGradeMenu = it },
                    grades = grades,
                    onGradeSelected = {
                        selectedGrade = it
                        showGradeMenu = false
                    }
                )
            }

            item {
                FieldLabel("PARENT PAIR CODE")
            }

            item {
                SignUpTextField(
                    value = parentCode,
                    onValueChange = { parentCode = it },
                    placeholder = "Optional pair code",
                    keyboardType = KeyboardType.Text
                )
            }

            item {
                HelpCard()
            }

            if (!errorMessage.isNullOrBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = ErrorText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.register(
                            fullName = fullName,
                            email = email,
                            pass = password
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = WhiteText,
                        disabledContainerColor = PrimaryBlue.copy(alpha = 0.65f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = WhiteText,
                            strokeWidth = 2.4.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text = "Create Student Account",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account?",
                        color = SecondaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Sign In",
                        color = BlueSoft,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onAlreadyHaveAccountClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentSignUpTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = WhiteText,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "Student Sign Up",
            color = WhiteText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StudentSignUpHero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(138.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(138.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryBlue.copy(alpha = 0.26f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(CardSurface)
                    .border(1.dp, CardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PersonAddAlt1,
                    contentDescription = null,
                    tint = BlueSoft,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create your student account",
            color = WhiteText,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set up your learning profile to access focus mode, quizzes, and materials.",
            color = SecondaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun FieldLabel(
    text: String
) {
    Text(
        text = text,
        color = SecondaryText,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.1.sp
    )
}

@Composable
private fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp)),
        singleLine = true,
        shape = RoundedCornerShape(22.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = HintText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization
        ),
        visualTransformation = visualTransformation,
        trailingIcon = trailing,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = FieldSurface,
            unfocusedContainerColor = FieldSurface,
            disabledContainerColor = FieldSurface,
            focusedTextColor = WhiteText,
            unfocusedTextColor = WhiteText,
            disabledTextColor = WhiteText,
            focusedPlaceholderColor = HintText,
            unfocusedPlaceholderColor = HintText,
            disabledPlaceholderColor = HintText,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = WhiteText
        )
    )
}

@Composable
private fun GradeSelectorField(
    selectedGrade: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    grades: List<String>,
    onGradeSelected: (String) -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(FieldSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                .clickable { onExpandedChange(true) }
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedGrade,
                color = WhiteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = "Select grade",
                tint = SecondaryText,
                modifier = Modifier.size(28.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(Color(0xFF11151E))
        ) {
            grades.forEach { grade ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = grade,
                            color = WhiteText
                        )
                    },
                    onClick = { onGradeSelected(grade) }
                )
            }
        }
    }
}

@Composable
private fun HelpCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CardSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Pair code is optional",
                    color = WhiteText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "You can connect your account to a parent later from your profile.",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 21.sp
                )
            }
        }
    }
}