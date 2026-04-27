package com.rakizz.student.presentation.materials.list

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rakizz.student.domain.model.Material
import com.rakizz.student.presentation.common.UiState
import kotlinx.coroutines.delay

private val ScreenTop = Color(0xFF020B1F)
private val ScreenBottom = Color(0xFF000000)
private val WhiteText = Color(0xFFF5F7FB)
private val SecondaryText = Color(0xFF8A92A3)
private val PrimaryBlue = Color(0xFF1F5BDE)
private val CardBorder = Color(0xFF2A2F3A)
private val SurfaceDark = Color(0xFF171717)
private val SurfaceDark2 = Color(0xFF101319)
private val SheetSurface = Color(0xFF141414)
private val SheetItemSurface = Color(0xFF202020)
private val SheetHandle = Color(0xFF4A4D55)
private val SuccessBg = Color(0xFF14261A)
private val SuccessBorder = Color(0xFF245B35)

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
    var selectedSubject by rememberSaveable { mutableStateOf("All") }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }

    var showLinkDialog by rememberSaveable { mutableStateOf(false) }
    var linkTitle by rememberSaveable { mutableStateOf("") }
    var linkUrl by rememberSaveable { mutableStateOf("") }
    var linkError by rememberSaveable { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        val pickedFile = readPickedMaterialFile(context, uri)
        if (pickedFile == null) {
            return@rememberLauncherForActivityResult
        }

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

    val subjectFilters = buildSubjectFilters(allMaterials)
    val filteredMaterials = allMaterials.filter { material ->
        val titleMatches = material.title.contains(searchQuery, ignoreCase = true)
        val subjectMatches = selectedSubject == "All" || inferSubject(material) == selectedSubject
        titleMatches && subjectMatches
    }

    val recentMaterials = filteredMaterials.take(8)
    val subjectCards = buildSubjectCards(allMaterials)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            AddMaterialButton(
                onClick = { showAddSheet = true }
            )
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
                    brush = Brush.verticalGradient(
                        colors = listOf(ScreenTop, ScreenBottom)
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 18.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    LibraryTopBar()
                }

                item {
                    LibrarySearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                }

                item {
                    SubjectChipsRow(
                        filters = subjectFilters,
                        selected = selectedSubject,
                        onSelect = { selectedSubject = it }
                    )
                }

                if (!actionMessage.isNullOrBlank()) {
                    item {
                        InlineMessageCard(
                            message = actionMessage.orEmpty(),
                            bg = SuccessBg,
                            border = SuccessBorder
                        )
                    }
                }

                if (state is UiState.Error) {
                    item {
                        InlineMessageCard(
                            message = (state as UiState.Error).message,
                            bg = Color(0xFF351515),
                            border = Color(0xFF6B2222)
                        )
                    }
                }

                item {
                    SectionHeader(title = "Stored Materials")
                }

                item {
                    if (filteredMaterials.isEmpty()) {
                        EmptySectionCard(message = "No materials found")
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            items(filteredMaterials.take(6)) { material ->
                                LargeMaterialCard(
                                    material = material,
                                    onClick = { onNavigateToDetail(material.id) }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    SectionHeader(title = "Recently Added")
                }

                item {
                    if (recentMaterials.isEmpty()) {
                        EmptySectionCard(message = "No recent materials available")
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(recentMaterials) { material ->
                                RecentMaterialCard(
                                    material = material,
                                    onClick = { onNavigateToDetail(material.id) }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    SectionHeader(title = "By Subject")
                }

                item {
                    if (subjectCards.isEmpty()) {
                        EmptySectionCard(message = "No subject summaries available")
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(subjectCards) { subject ->
                                SubjectSummaryCard(subject = subject)
                            }
                        }
                    }
                }
            }

            if (state is UiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryBlue,
                        strokeWidth = 3.dp
                    )
                }
            }

            if (state is UiState.Empty && allMaterials.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InlineMessageCard(
                        message = "Your library is empty",
                        bg = SurfaceDark,
                        border = CardBorder
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = SheetSurface,
            scrimColor = Color.Black.copy(alpha = 0.72f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 4.dp)
                        .width(76.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(SheetHandle)
                )
            },
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            tonalElevation = 0.dp
        ) {
            AddMaterialBottomSheetContent(
                onClose = { showAddSheet = false },
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
        AlertDialog(
            onDismissRequest = {
                showLinkDialog = false
                linkError = null
            },
            containerColor = SheetSurface,
            title = {
                Text(
                    text = "Add material link",
                    color = WhiteText,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = linkTitle,
                        onValueChange = {
                            linkTitle = it
                            linkError = null
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = WhiteText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        placeholder = {
                            Text(
                                text = "Material title",
                                color = SecondaryText
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedPlaceholderColor = SecondaryText,
                            unfocusedPlaceholderColor = SecondaryText,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = WhiteText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    )

                    TextField(
                        value = linkUrl,
                        onValueChange = {
                            linkUrl = it
                            linkError = null
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = WhiteText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        placeholder = {
                            Text(
                                text = "https://example.com/file.pdf",
                                color = SecondaryText
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedPlaceholderColor = SecondaryText,
                            unfocusedPlaceholderColor = SecondaryText,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = WhiteText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    )

                    if (!linkError.isNullOrBlank()) {
                        Text(
                            text = linkError.orEmpty(),
                            color = Color(0xFFFF8A8A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
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
                ) {
                    Text(
                        text = "Save",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLinkDialog = false
                        linkError = null
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = SecondaryText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun LibraryTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Library",
            color = WhiteText,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.weight(1f))

        ProfileAvatar()
    }
}

@Composable
private fun LibrarySearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(
            color = WhiteText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        ),
        leadingIcon = {
            SearchGlyph(color = SecondaryText)
        },
        placeholder = {
            Text(
                text = "Search title or subject...",
                color = SecondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text
        ),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = WhiteText,
            unfocusedTextColor = WhiteText,
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceDark,
            focusedPlaceholderColor = SecondaryText,
            unfocusedPlaceholderColor = SecondaryText,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = WhiteText
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
    )
}

@Composable
private fun SubjectChipsRow(
    filters: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filters.forEach { item ->
            val isSelected = item == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) Color.White else SurfaceDark)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.White else CardBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onSelect(item) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = item,
                    color = if (isSelected) Color.Black else WhiteText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String
) {
    Text(
        text = title,
        color = WhiteText,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun LargeMaterialCard(
    material: Material,
    onClick: () -> Unit
) {
    val subject = inferSubject(material)
    val typeLabel = inferTypeLabel(material)
    val colors = subjectGradient(subject)
    val accent = subjectColor(subject)

    Column(
        modifier = Modifier
            .width(178.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(266.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.linearGradient(colors)
                )
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Text(
                text = subjectGlyph(subject),
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            Text(
                text = subject.uppercase(),
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = material.title,
            color = WhiteText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = subject.uppercase(),
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = typeLabel,
                color = SecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecentMaterialCard(
    material: Material,
    onClick: () -> Unit
) {
    val subject = inferSubject(material)
    val colors = recentCardGradient(subject)
    val accent = subjectColor(subject)

    Column(
        modifier = Modifier
            .width(152.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(colors)
                )
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            Text(
                text = recentGlyph(subject),
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 10.dp)
                    .width(100.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = material.title,
            color = WhiteText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 19.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subject.uppercase(),
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SubjectSummaryCard(
    subject: SubjectCardUi
) {
    Box(
        modifier = Modifier
            .width(172.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.linearGradient(subject.colors)
            )
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(
            text = subject.icon,
            color = subject.accent,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart)
        )

        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = subject.title,
                color = WhiteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${subject.count} Items",
                color = SecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AddMaterialButton(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(26.dp))
            .background(PrimaryBlue)
            .border(1.dp, PrimaryBlue.copy(alpha = 0.9f), RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .padding(horizontal = 26.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "+",
            color = WhiteText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Add material",
            color = WhiteText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LibraryBottomBar(
    onOpenHome: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .border(1.dp, Color.White.copy(alpha = 0.06f))
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            label = "Home",
            selected = false,
            icon = "⌂",
            onClick = onOpenHome
        )
        BottomNavItem(
            label = "Library",
            selected = true,
            icon = "▤",
            onClick = {}
        )
        BottomNavItem(
            label = "Focus",
            selected = false,
            icon = "◔",
            onClick = onOpenFocus
        )
        BottomNavItem(
            label = "Profile",
            selected = false,
            icon = "●",
            onClick = onOpenProfile
        )
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    icon: String,
    onClick: () -> Unit
) {
    val color = if (selected) PrimaryBlue else SecondaryText

    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileAvatar() {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF1C1F26))
            .border(1.dp, CardBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBFC3CC))
                    .align(Alignment.TopCenter)
            )
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color(0xFFBFC3CC))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun SearchGlyph(color: Color) {
    Box(
        modifier = Modifier.size(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .border(2.dp, color, CircleShape)
        )
        Box(
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp)
                .width(7.dp)
                .height(2.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun InlineMessageCard(
    message: String,
    bg: Color,
    border: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = message,
            color = WhiteText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptySectionCard(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDark2)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = SecondaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AddMaterialBottomSheetContent(
    onClose: () -> Unit,
    onUploadFile: () -> Unit,
    onAddLink: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SheetSurface)
            .padding(horizontal = 28.dp)
            .padding(top = 6.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add New Material",
                color = WhiteText,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF8A92A3),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        AddMaterialSheetItem(
            icon = Icons.Rounded.InsertDriveFile,
            iconBackground = Color(0xFF182B52),
            iconTint = Color(0xFF6EA7FF),
            title = "Upload file",
            subtitle = "PDF, Image, Doc, Slides, Text",
            onClick = onUploadFile
        )

        Spacer(modifier = Modifier.height(16.dp))

        AddMaterialSheetItem(
            icon = Icons.Rounded.Link,
            iconBackground = Color(0xFF4C2D12),
            iconTint = Color(0xFFFFA54A),
            title = "Add link",
            subtitle = "Website or file URL",
            onClick = onAddLink
        )

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "SUPPORTED FORMATS: JPG, PNG, PDF, DOC, DOCX, PPT, PPTX, TXT",
            color = Color(0xFF6F7B93),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun AddMaterialSheetItem(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SheetItemSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(iconBackground)
                .border(
                    width = 1.dp,
                    color = iconTint.copy(alpha = 0.28f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = WhiteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = SecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Icon(
            imageVector = Icons.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(16.dp)
        )
    }
}

private data class SubjectCardUi(
    val title: String,
    val count: Int,
    val icon: String,
    val accent: Color,
    val colors: List<Color>
)

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
    val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
    val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
        input.readBytes()
    } ?: return null

    val derivedTitle = fileName
        .substringBeforeLast(".")
        .replace("_", " ")
        .replace("-", " ")
        .trim()
        .ifBlank { "Material" }

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
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return null
}

private fun buildSubjectFilters(materials: List<Material>): List<String> {
    val subjects = materials
        .map { inferSubject(it) }
        .distinct()

    return listOf("All") + subjects
}

private fun buildSubjectCards(materials: List<Material>): List<SubjectCardUi> {
    return materials
        .groupBy { inferSubject(it) }
        .map { (subject, items) ->
            SubjectCardUi(
                title = subjectDisplayName(subject),
                count = items.size,
                icon = subjectSummaryGlyph(subject),
                accent = subjectColor(subject),
                colors = subjectSummaryGradient(subject)
            )
        }
}

private fun inferSubject(material: Material): String {
    val title = material.title.lowercase()

    return when {
        title.contains("math") || title.contains("calc") || title.contains("algebra") || title.contains("derivative") || title.contains("equation") -> "Math"
        title.contains("physics") || title.contains("mechanic") || title.contains("motion") || title.contains("force") -> "Physics"
        title.contains("chem") || title.contains("organic") || title.contains("atom") || title.contains("molecule") -> "Chemistry"
        title.contains("history") || title.contains("essay") || title.contains("wwii") || title.contains("draft") -> "History"
        else -> "General"
    }
}

private fun inferTypeLabel(material: Material): String {
    val value = material.url.lowercase()
    val title = material.title.lowercase()

    return when {
        value.endsWith(".pdf") || title.contains("pdf") -> "PDF"
        value.endsWith(".ppt") || value.endsWith(".pptx") -> "Slides"
        value.endsWith(".doc") || value.endsWith(".docx") -> "Doc"
        value.endsWith(".txt") -> "Text"
        value.endsWith(".jpg") || value.endsWith(".jpeg") || value.endsWith(".png") -> "Image"
        title.contains("note") || title.contains("notes") -> "Notes"
        title.contains("draft") -> "Draft"
        else -> "File"
    }
}

private fun subjectDisplayName(subject: String): String {
    return when (subject) {
        "Math" -> "Mathematics"
        else -> subject
    }
}

private fun subjectGlyph(subject: String): String {
    return when (subject) {
        "Math" -> "Σ"
        "Physics" -> "⚗"
        "Chemistry" -> "⚗"
        "History" -> "⌲"
        else -> "▣"
    }
}

private fun recentGlyph(subject: String): String {
    return when (subject) {
        "Math" -> "✚"
        "Physics" -> "⚗"
        "Chemistry" -> "⚗"
        "History" -> "⌲"
        else -> "📄"
    }
}

private fun subjectSummaryGlyph(subject: String): String {
    return when (subject) {
        "Math" -> "✚"
        "Physics" -> "🚀"
        "Chemistry" -> "⚗"
        "History" -> "⌲"
        else -> "📄"
    }
}

private fun subjectColor(subject: String): Color {
    return when (subject) {
        "Math" -> Color(0xFF2F7BFF)
        "Physics" -> Color(0xFF00C2FF)
        "Chemistry" -> Color(0xFF2DD4BF)
        "History" -> Color(0xFF4ADE80)
        else -> Color(0xFF94A3B8)
    }
}

private fun subjectGradient(subject: String): List<Color> {
    return when (subject) {
        "Math" -> listOf(Color(0xFF4C35A0), Color(0xFF5A2092), Color(0xFF12061D))
        "Physics" -> listOf(Color(0xFF21489D), Color(0xFF0C4767), Color(0xFF02131A))
        "Chemistry" -> listOf(Color(0xFF0B6A4F), Color(0xFF0F4B37), Color(0xFF041812))
        "History" -> listOf(Color(0xFF6B3D15), Color(0xFF4A2511), Color(0xFF180A06))
        else -> listOf(Color(0xFF243149), Color(0xFF172131), Color(0xFF0C1018))
    }
}

private fun recentCardGradient(subject: String): List<Color> {
    return when (subject) {
        "Chemistry" -> listOf(Color(0xFF91270F), Color(0xFF7A220F), Color(0xFF1A0B09))
        "History" -> listOf(Color(0xFF0A7B49), Color(0xFF0C5E39), Color(0xFF07160F))
        "Math" -> listOf(Color(0xFF1A325E), Color(0xFF162847), Color(0xFF0A0F18))
        "Physics" -> listOf(Color(0xFF1A305C), Color(0xFF0F3652), Color(0xFF08121A))
        else -> listOf(Color(0xFF1B2A45), Color(0xFF162235), Color(0xFF0A0F18))
    }
}

private fun subjectSummaryGradient(subject: String): List<Color> {
    return when (subject) {
        "Math" -> listOf(Color(0xFF1A2238), Color(0xFF1B243A))
        "Physics" -> listOf(Color(0xFF2B1D38), Color(0xFF33223E))
        "Chemistry" -> listOf(Color(0xFF193323), Color(0xFF173525))
        "History" -> listOf(Color(0xFF2D2E18), Color(0xFF31311C))
        else -> listOf(Color(0xFF1C2330), Color(0xFF202632))
    }
}