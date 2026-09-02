package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.data.model.MemberEntity
import com.example.data.model.MemberStatus
import com.example.i18n.AppStrings
import com.example.ui.theme.PresentGreen
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.GeminiAiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BulkMemberImportDialog(
    groupId: Long,
    viewModel: AttendanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val language by viewModel.currentLanguage.collectAsState()
    val s = AppStrings.get(language)
    val groups by viewModel.groups.collectAsState()

    val prefs = remember { context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE) }
    val defaultKey = com.example.BuildConfig.GEMINI_API_KEY
    var apiKey by remember { mutableStateOf(prefs.getString("user_gemini_api_key", defaultKey) ?: defaultKey) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    // Target Group Selection (defaults to passed groupId, can be switched if desired)
    var selectedTargetGroupId by remember { mutableStateOf(if (groupId > 0L) groupId else 1L) }
    var selectedDefaultSubGroup by remember { mutableStateOf(1) } // 1 or 2

    // Content State (Text, Image, PDF)
    var rawText by remember { mutableStateOf("") }
    var attachedFileUri by remember { mutableStateOf<Uri?>(null) }
    var attachedFileName by remember { mutableStateOf<String?>(null) }
    var attachedFileMimeType by remember { mutableStateOf<String?>(null) }

    // User AI Instruction Prompt
    var userAiPrompt by remember {
        mutableStateOf(
            if (language == com.example.i18n.Language.UYGHUR)
                "تۆۋەندىكى مەزمۇنلاردىن ئەزالارنىڭ ئىسىم، تېلېفون نومۇرى ۋە تېلېگرامىنى ئايرىپ 1 ۋە 2-گۇرۇپچىغا رەتلىك بۆلۈپ كىرگۈزۈپ بەر."
            else
                "استخرج أسماء الأعضاء وأرقام الهواتف والتليجرام وقسمهم لمجموعات فرعية بشكل منظم."
        )
    }

    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val parsedMembers = remember { mutableStateListOf<MemberEntity>() }

    // Edit Member Dialog State
    var memberToEditIndex by remember { mutableStateOf<Int?>(null) }

    // File Helpers
    fun getFileNameFromUri(uri: Uri): String {
        var name = "ھۆججەت"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                }
            }
        } catch (_: Exception) {}
        return name
    }

    // Media & File Launchers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedFileUri = uri
            attachedFileName = getFileNameFromUri(uri)
            attachedFileMimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedFileUri = uri
            attachedFileName = getFileNameFromUri(uri)
            attachedFileMimeType = context.contentResolver.getType(uri) ?: "application/pdf"
        }
    }

    // Core AI Multi-modal Analysis Action
    fun executeAiAnalysis() {
        if (rawText.isBlank() && attachedFileUri == null) {
            statusMessage = "ئالدى بىلەن تېكىست چاپلاڭ ياكى رەسىم/PDF قوشۇڭ"
            return
        }

        isProcessing = true
        statusMessage = null

        coroutineScope.launch {
            try {
                var fileBase64: String? = null
                var mime = attachedFileMimeType ?: "image/jpeg"

                attachedFileUri?.let { uri ->
                    withContext(Dispatchers.IO) {
                        try {
                            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            if (bytes != null) {
                                fileBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                            }
                        } catch (e: Exception) {
                            // File read issue
                        }
                    }
                }

                val result = GeminiAiHelper.parseMembersWithAi(
                    apiKey = apiKey,
                    userPrompt = userAiPrompt,
                    rawText = rawText,
                    fileBase64 = fileBase64,
                    mimeType = mime,
                    defaultSubGroup = selectedDefaultSubGroup
                )

                result.onSuccess { aiMembers ->
                    if (aiMembers.isNotEmpty()) {
                        parsedMembers.clear()
                        parsedMembers.addAll(
                            aiMembers.map { m ->
                                MemberEntity(
                                    groupId = selectedTargetGroupId,
                                    name = m.name,
                                    subGroup = m.subGroup,
                                    contactAddress = m.contactAddress,
                                    telegramContact = m.telegramContact,
                                    whatsappContact = m.whatsappContact,
                                    otherContact = m.notes,
                                    status = MemberStatus.ACTIVE
                                )
                            }
                        )
                        statusMessage = "مۇۋەپپەقىيەتلىك ھالدا ${aiMembers.size} ئەزا تەھلىل قىلىندى"
                    } else {
                        // Fallback manual heuristic parse
                        val fallback = viewModel.parseBulkMemberText(rawText, selectedTargetGroupId, selectedDefaultSubGroup)
                        if (fallback.isNotEmpty()) {
                            parsedMembers.clear()
                            parsedMembers.addAll(fallback)
                            statusMessage = "تېكىستتىن ${fallback.size} ئەزا تېپىلدى"
                        } else {
                            statusMessage = "كىرگۈزۈلگەن مەلۇماتتىن ئەزا تېپىلمىدى"
                        }
                    }
                }.onFailure { err ->
                    val fallback = viewModel.parseBulkMemberText(rawText, selectedTargetGroupId, selectedDefaultSubGroup)
                    if (fallback.isNotEmpty()) {
                        parsedMembers.clear()
                        parsedMembers.addAll(fallback)
                        statusMessage = "تېكىستتىن ${fallback.size} ئەزا تېپىلدى (AI تور سىز تەھلىل قىلدى)"
                    } else {
                        statusMessage = "تەھلىل قىلىش مەغلۇپ بولدى: ${err.localizedMessage}"
                    }
                }
            } catch (e: Exception) {
                statusMessage = "خاتالىق كۆرۈلدى: ${e.localizedMessage}"
            } finally {
                isProcessing = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .heightIn(max = 720.dp)
                .testTag("bulk_import_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Header: Title + API Key indicator + Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "سۈنئىي ئەقىل ئارقىلىق كۆپلەپ كىرگۈزۈش",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "رەسىم، PDF ياكى تېكىستنى AI ئارقىلىق ئەقلىي ئانالىز قىلىش",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showApiKeyDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "API Key",
                                tint = if (apiKey.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = s.cancel,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Group & Subgroup Selector
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "گۇرۇپپا تاللاڭ:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )

                            // Quick Subgroup default
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = selectedDefaultSubGroup == 1,
                                    onClick = { selectedDefaultSubGroup = 1 },
                                    label = { Text(s.subGroup1, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.height(30.dp)
                                )
                                FilterChip(
                                    selected = selectedDefaultSubGroup == 2,
                                    onClick = { selectedDefaultSubGroup = 2 },
                                    label = { Text(s.subGroup2, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.height(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Group Chips (1..6)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            groups.take(6).forEach { grp ->
                                val isSelected = selectedTargetGroupId == grp.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTargetGroupId = grp.id },
                                    label = {
                                        Text(
                                            text = grp.code.ifBlank { "${grp.id}" },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Unified Multi-modal Input Container
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Text Input
                        OutlinedTextField(
                            value = rawText,
                            onValueChange = { rawText = it },
                            placeholder = {
                                Text(
                                    "ئەزالار تىزىملىكىنى چاپلاڭ ياكى رەسىم/PDF قوشۇڭ...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .testTag("bulk_import_text_input"),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Attached File Chip (if any)
                        if (attachedFileUri != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (attachedFileMimeType?.contains("pdf") == true) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = attachedFileName ?: "قوشۇلغان ھۆججەت",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            maxLines = 1
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            attachedFileUri = null
                                            attachedFileName = null
                                            attachedFileMimeType = null
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Attachment Toolbar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Image Picker Button
                                OutlinedButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("سۈرەت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // PDF / Document Picker Button
                                OutlinedButton(
                                    onClick = { documentPickerLauncher.launch("application/pdf") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Clipboard Paste Button
                                OutlinedButton(
                                    onClick = {
                                        val clipText = clipboardManager.getText()?.text
                                        if (!clipText.isNullOrBlank()) {
                                            rawText = if (rawText.isBlank()) clipText else "$rawText\n$clipText"
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("چاپلاش", fontSize = 11.sp)
                                }
                            }

                            if (rawText.isNotBlank() || attachedFileUri != null) {
                                TextButton(
                                    onClick = {
                                        rawText = ""
                                        attachedFileUri = null
                                        attachedFileName = null
                                    },
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("تازىلاش", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // AI Prompt / Instructions Input (Underneath the content)
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "سۈنئىي ئەقىلگە تەلەپ يېزىڭ:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = userAiPrompt,
                            onValueChange = { userAiPrompt = it },
                            placeholder = { Text("تەلەپ يېزىڭ (مەسىلەن: 1 ۋە 2-گۇرۇپچىغا تەڭ بۆلۈپ بەر...)", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Run AI Extraction Button
                        Button(
                            onClick = { executeAiAnalysis() },
                            enabled = !isProcessing && (rawText.isNotBlank() || attachedFileUri != null),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("run_ai_analysis_button")
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI ئانالىز قىلىۋاتىدۇ...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("سۈنئىي ئەقىل ئارقىلىق ئانالىز قىلىش ۋە كىرگۈزۈش", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (statusMessage != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = statusMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (statusMessage?.contains("خاتالىق") == true || statusMessage?.contains("تېپىلمىدى") == true)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Parsed Members Preview Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تەھلىل نەتىجىسى (${parsedMembers.size} ئەزا)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (parsedMembers.isNotEmpty()) {
                        TextButton(
                            onClick = { parsedMembers.clear() },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("ھەممىنى ئۆچۈرۈش", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }
                    }
                }

                // Parsed Members List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 180.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (parsedMembers.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "تېخى ئەزا تەھلىل قىلىنمىدى. يۇقىرىدىن تېكىست چاپلاپ ياكى رەسىم/PDF قوشۇپ ئانالىز قىلىڭ.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(parsedMembers) { index, member ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = member.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val details = listOfNotNull(
                                                member.contactAddress.ifBlank { null },
                                                member.telegramContact.ifBlank { null }
                                            ).joinToString(" | ")
                                            if (details.isNotBlank()) {
                                                Text(
                                                    text = details,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }

                                    // Subgroup switch chip (1 vs 2)
                                    FilterChip(
                                        selected = member.subGroup == 2,
                                        onClick = {
                                            val newSubGroup = if (member.subGroup == 1) 2 else 1
                                            parsedMembers[index] = member.copy(subGroup = newSubGroup)
                                        },
                                        label = {
                                            Text(if (member.subGroup == 2) s.subGroup2 else s.subGroup1, fontSize = 10.sp)
                                        },
                                        modifier = Modifier.height(28.dp)
                                    )

                                    // Edit member button
                                    IconButton(
                                        onClick = { memberToEditIndex = index },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    // Delete member button
                                    IconButton(
                                        onClick = { parsedMembers.removeAt(index) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = s.deleteMember,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons: Cancel vs Confirm Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(s.cancel, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (parsedMembers.isNotEmpty()) {
                                val updatedMembers = parsedMembers.map { it.copy(groupId = selectedTargetGroupId) }
                                viewModel.importBatchMembers(updatedMembers) {
                                    onDismiss()
                                }
                            }
                        },
                        enabled = parsedMembers.isNotEmpty(),
                        modifier = Modifier
                            .weight(1.6f)
                            .testTag("confirm_bulk_import_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PresentGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "گۇرۇپپىغا قوشۇش (${parsedMembers.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Edit Member Inline Dialog
    if (memberToEditIndex != null) {
        val idx = memberToEditIndex!!
        if (idx in parsedMembers.indices) {
            val memb = parsedMembers[idx]
            var editName by remember { mutableStateOf(memb.name) }
            var editContact by remember { mutableStateOf(memb.contactAddress) }
            var editTg by remember { mutableStateOf(memb.telegramContact) }
            var editSub by remember { mutableStateOf(memb.subGroup) }

            AlertDialog(
                onDismissRequest = { memberToEditIndex = null },
                title = { Text("ئەزا ئۇچۇرىنى تەھرىرلەش", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("ئىسىم") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editContact,
                            onValueChange = { editContact = it },
                            label = { Text("تېلېفون / ئالاقە") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editTg,
                            onValueChange = { editTg = it },
                            label = { Text("تېلېگرام") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = editSub == 1,
                                onClick = { editSub = 1 },
                                label = { Text(s.subGroup1) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = editSub == 2,
                                onClick = { editSub = 2 },
                                label = { Text(s.subGroup2) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editName.isNotBlank()) {
                                parsedMembers[idx] = memb.copy(
                                    name = editName.trim(),
                                    contactAddress = editContact.trim(),
                                    telegramContact = editTg.trim(),
                                    subGroup = editSub
                                )
                            }
                            memberToEditIndex = null
                        }
                    ) {
                        Text("ساقلاش")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { memberToEditIndex = null }) {
                        Text(s.cancel)
                    }
                }
            )
        }
    }

    // API Key Settings Dialog
    if (showApiKeyDialog) {
        var tempKey by remember { mutableStateOf(apiKey) }
        var showKeySecret by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(s.geminiApiKeyLabel, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Gemini API ئاچقۇچى سۈنئىي ئەقىل ئارقىلىق رەسىم، PDF ۋە مۇرەككەپ تىزىملىكلەرنى تېز ئانالىز قىلىش ئۈچۈن ئىشلىتىلىدۇ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("API Key") },
                        trailingIcon = {
                            IconButton(onClick = { showKeySecret = !showKeySecret }) {
                                Icon(
                                    imageVector = if (showKeySecret) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showKeySecret) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        apiKey = tempKey.trim()
                        prefs.edit().putString("user_gemini_api_key", apiKey).apply()
                        showApiKeyDialog = false
                    }
                ) {
                    Text("جەزملەش")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text(s.cancel)
                }
            }
        )
    }
}
