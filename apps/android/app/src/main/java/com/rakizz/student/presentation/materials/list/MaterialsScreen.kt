package com.rakizz.student.presentation.materials.list

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Material
import com.rakizz.student.presentation.common.UiState
import com.rakizz.student.presentation.theme.RakizzColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(
    onNavigateToDetail: (String) -> Unit,
    onOpenHome: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onAddMaterial: () -> Unit = {},
    onTakePhoto: () -> Unit = {},
    onWriteTextNotes: () -> Unit = {},
    onAddLink: () -> Unit = {},
    viewModel: MaterialsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val state by viewModel.uiState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showLinkDialog by rememberSaveable { mutableStateOf(false) }

    var linkTitle by rememberSaveable { mutableStateOf("") }
    var linkUrl by rememberSaveable { mutableStateOf("") }
    var linkError by rememberSaveable { mutableStateOf<String?>(null) }
    //var searchQuery by rememberSaveable { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        val pickedFile = readPickedMaterialFile(
            context = context,
            uri = uri
        )

        if (pickedFile == null) {
            return@rememberLauncherForActivityResult
        }

        // send selected file to backend
        viewModel.uploadMaterialFile(
            title = pickedFile.derivedTitle,
            fileName = pickedFile.fileName,
            mimeType = pickedFile.mimeType,
            bytes = pickedFile.bytes
        )
    }

    LaunchedEffect(actionMessage) {
        if (!actionMessage.isNullOrBlank()) {
            delay(2500)
            viewModel.clearActionMessage()
        }
    }

    val allMaterials = when (val uiState = state) {
        is UiState.Success -> uiState.data
        else -> emptyList()
    }

    /*val filteredMaterials = allMaterials.filter { material ->
    material.title.contains(
        other = searchQuery,
        ignoreCase = true
    )
}*/

    val filteredMaterials = allMaterials.filter { material ->
        material.title.contains(
            other = searchQuery,
            ignoreCase = true
        )
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    Scaffold(
        containerColor = RakizzColors.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddSheet = true
                },
                containerColor = RakizzColors.Primary,
                contentColor = RakizzColors.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "+",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        bottomBar = {
            LibraryBottomBar(
                onOpenHome = onOpenHome,
                onOpenFocus = onOpenFocus,
                onOpenProfile = onOpenProfile
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            RakizzColors.Background,
                            RakizzColors.BackgroundSoft
                        )
                    )
                )
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 14.dp,
                    bottom = 110.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    LibraryHeader(
                        onAddClick = {
                            showAddSheet = true
                        }
                    )
                }

                item {
                    SearchBox(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                        }
                    )
                }

                if (!actionMessage.isNullOrBlank()) {
                    item {
                        MessageCard(
                            title = "Saved",
                            message = actionMessage.orEmpty(),
                            color = RakizzColors.Success
                        )
                    }
                }

                if (state is UiState.Error) {
                    item {
                        MessageCard(
                            title = "Error",
                            message = (state as UiState.Error).message,
                            color = RakizzColors.Error
                        )
                    }
                }

                item {
                    LibraryInfoCard()
                }

                item {
                    SectionTitle(
                        title = "Study materials",
                        subtitle = "${filteredMaterials.size} item(s) in your library"
                    )
                }

                when {
                    state is UiState.Loading -> {
                        items(4) {
                            LoadingMaterialCard()
                        }
                    }

                    else -> {
                        items(
                            items = filteredMaterials,
                            key = { material -> material.id }
                        ) { material ->
                            MaterialCard(
                                material = material,
                                onClick = {
                                    onNavigateToDetail(material.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
            },
            sheetState = sheetState,
            containerColor = RakizzColors.Card,
            scrimColor = com.rakizz.student.presentation.theme.RakizzColors.Background.copy(alpha = 0.55f),
            shape = RoundedCornerShape(
                topStart = 30.dp,
                topEnd = 30.dp
            ),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .width(70.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(RakizzColors.CardBorder)
                )
            }
        ) {
            AddMaterialSheet(
                onClose = {
                    showAddSheet = false
                },
                onUploadFile = {
                    showAddSheet = false
                    onAddMaterial()

                    filePickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "application/vnd.ms-powerpoint",
                            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "text/plain",
                            "image/jpeg",
                            "image/png"
                        )
                    )
                },
                onAddLink = {
                    showAddSheet = false
                    onAddLink()
                    linkError = null
                    showLinkDialog = true
                }
            )
        }
    }

    if (showLinkDialog) {
        AddLinkDialog(
            title = linkTitle,
            url = linkUrl,
            error = linkError,
            onTitleChange = {
                linkTitle = it
                linkError = null
            },
            onUrlChange = {
                linkUrl = it
                linkError = null
            },
            onDismiss = {
                showLinkDialog = false
                linkError = null
            },
            onSave = {
                val cleanTitle = linkTitle.trim()
                val cleanUrl = linkUrl.trim()

                when {
                    cleanTitle.isBlank() -> {
                        linkError = "Title is required"
                    }

                    cleanUrl.isBlank() -> {
                        linkError = "Link is required"
                    }

                    !cleanUrl.startsWith("http://", ignoreCase = true) &&
                        !cleanUrl.startsWith("https://", ignoreCase = true) -> {
                        linkError = "Link must start with http:// or https://"
                    }

                    else -> {
                        showLinkDialog = false
                        linkError = null

                        viewModel.addLinkMaterial(
                            title = cleanTitle,
                            sourceUrl = cleanUrl
                        )

                        linkTitle = ""
                        linkUrl = ""
                    }
                }
            }
        )
    }
}

@Composable
private fun LibraryHeader(
    onAddClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Study Library",
                    color = RakizzColors.TextMain,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Keep your study files in one calm place.",
                    color = RakizzColors.TextSecond,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        onAddClick()
                    },
                color = RakizzColors.Primary,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "Add Material",
                    color = RakizzColors.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 11.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun SearchBox(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search materials...",
                color = RakizzColors.TextMuted
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = RakizzColors.TextMain,
            unfocusedTextColor = RakizzColors.TextMain,
            focusedContainerColor = RakizzColors.Card,
            unfocusedContainerColor = RakizzColors.Card,
            focusedBorderColor = RakizzColors.Primary,
            unfocusedBorderColor = RakizzColors.CardBorder,
            cursorColor = RakizzColors.Primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
    )
}

@Composable
private fun LibraryInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.PrimarySoft,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = RakizzColors.CardBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(18.dp)
        ) {
            Text(
                text = "Rakizz Materials",
                color = RakizzColors.PrimaryDark,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Upload a file, open it, then generate a mixed AI quiz from the content. The same materials are also used in quiz-to-unlock mode.",
                color = RakizzColors.TextSecond,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = RakizzColors.TextMain,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            color = RakizzColors.TextSecond,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun MaterialCard(
    material: Material,
    onClick: () -> Unit
) {
    val typeLabel = inferTypeLabel(material)
    val typeColor = typeColor(typeLabel)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        color = RakizzColors.Card,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = RakizzColors.CardBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(typeColor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = typeLabel.take(3).uppercase(),
                    color = typeColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = material.title,
                    color = RakizzColors.TextMain,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "Open material and generate quiz",
                    color = RakizzColors.TextSecond,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(9.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallPill(
                        text = typeLabel,
                        color = typeColor
                    )

                    SmallPill(
                        text = "AI Ready",
                        color = RakizzColors.Primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Open",
                color = RakizzColors.Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SmallPill(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun LoadingMaterialCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = RakizzColors.CardBorder,
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = RakizzColors.Primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Loading materials...",
                color = RakizzColors.TextSecond,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = RakizzColors.Card,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message,
                color = RakizzColors.TextSecond,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AddMaterialSheet(
    onClose: () -> Unit,
    onUploadFile: () -> Unit,
    onAddLink: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RakizzColors.Card)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 26.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Material",
                color = RakizzColors.TextMain,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(RakizzColors.BackgroundSoft)
                    .clickable {
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = RakizzColors.TextSecond,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose how you want to add study content.",
            color = RakizzColors.TextSecond,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        AddSheetItem(
            icon = Icons.Rounded.InsertDriveFile,
            title = "Upload file",
            subtitle = "PDF, DOC, slides, text, or image",
            color = RakizzColors.Primary,
            onClick = onUploadFile
        )

        Spacer(modifier = Modifier.height(12.dp))

        AddSheetItem(
            icon = Icons.Rounded.Link,
            title = "Add link",
            subtitle = "Save a study URL as a material",
            color = RakizzColors.Warning,
            onClick = onAddLink
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Supported: PDF, DOC, DOCX, PPT, PPTX, TXT, JPG, PNG",
            color = RakizzColors.TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun AddSheetItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        color = RakizzColors.CardSoft,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = RakizzColors.CardBorder,
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(25.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    color = RakizzColors.TextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    color = RakizzColors.TextSecond,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun AddLinkDialog(
    title: String,
    url: String,
    error: String?,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RakizzColors.Card,
        title = {
            Text(
                text = "Add material link",
                color = RakizzColors.TextMain,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = {
                        Text("Material title")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = dialogTextFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = {
                        Text("Link")
                    },
                    placeholder = {
                        Text("https://example.com/file.pdf")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = dialogTextFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!error.isNullOrBlank()) {
                    Text(
                        text = error,
                        color = RakizzColors.Error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave
            ) {
                Text(
                    text = "Save",
                    color = RakizzColors.Primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = RakizzColors.TextSecond,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = RakizzColors.TextMain,
    unfocusedTextColor = RakizzColors.TextMain,
    focusedBorderColor = RakizzColors.Primary,
    unfocusedBorderColor = RakizzColors.CardBorder,
    focusedLabelColor = RakizzColors.Primary,
    unfocusedLabelColor = RakizzColors.TextSecond,
    cursorColor = RakizzColors.Primary
)

@Composable
private fun LibraryBottomBar(
    onOpenHome: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        color = RakizzColors.Card,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .border(1.dp, RakizzColors.CardBorder)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                selected = false,
                onClick = onOpenHome
            )

            BottomNavItem(
                label = "Library",
                selected = true,
                onClick = {}
            )

            BottomNavItem(
                label = "Focus",
                selected = false,
                onClick = onOpenFocus
            )

            BottomNavItem(
                label = "Profile",
                selected = false,
                onClick = onOpenProfile
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (selected) {
        RakizzColors.Primary
    } else {
        RakizzColors.TextMuted
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(textColor)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (selected) {
                FontWeight.ExtraBold
            } else {
                FontWeight.Bold
            }
        )
    }
}

private data class PickedMaterialFile(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val derivedTitle: String
)

private fun readPickedMaterialFile(
    context: Context,
    uri: Uri
): PickedMaterialFile? {
    val fileName = queryDisplayName(context, uri) ?: "material"

    val mimeType = context.contentResolver.getType(uri)
        ?: "application/octet-stream"

    val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
        input.readBytes()
    } ?: return null

    val derivedTitle = fileName
        .substringBeforeLast(".")
        .replace("_", " ")
        .replace("-", " ")
        .trim()
        .ifBlank {
            "Material"
        }

    return PickedMaterialFile(
        fileName = fileName,
        mimeType = mimeType,
        bytes = bytes,
        derivedTitle = derivedTitle
    )
}

private fun queryDisplayName(
    context: Context,
    uri: Uri
): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)

    context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        null
    )?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }

    return null
}

private fun inferTypeLabel(
    material: Material
): String {
    val value = "${material.url} ${material.title}".lowercase()

    return when {
        value.contains(".pdf") || value.contains("pdf") -> "PDF"
        value.contains(".pptx") || value.contains(".ppt") -> "Slides"
        value.contains(".docx") || value.contains(".doc") -> "Doc"
        value.contains(".txt") -> "Text"
        value.contains(".jpg") || value.contains(".jpeg") || value.contains(".png") -> "Image"
        value.contains("note") -> "Notes"
        else -> "File"
    }
}

private fun typeColor(
    type: String
): Color {
    return when (type.lowercase()) {
        "pdf" -> RakizzColors.Error
        "slides" -> RakizzColors.Warning
        "doc" -> RakizzColors.Primary
        "text" -> RakizzColors.Success
        "image" -> RakizzColors.Accent
        "notes" -> RakizzColors.PrimaryDark
        else -> RakizzColors.TextSecond
    }
}
