package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MemberEntity
import com.example.i18n.AppStrings
import com.example.i18n.Language

@Composable
fun MemberDialog(
    initialMember: MemberEntity? = null,
    initialSubGroup: Int = 1,
    groupId: Long,
    language: Language,
    onDismiss: () -> Unit,
    onSave: (name: String, subGroup: Int, contactAddress: String, telegram: String, whatsapp: String, otherContact: String) -> Unit
) {
    val s = AppStrings.get(language)
    var name by remember { mutableStateOf(initialMember?.name ?: "") }
    var subGroup by remember { mutableStateOf(initialMember?.subGroup ?: initialSubGroup) }
    var contactAddress by remember { mutableStateOf(initialMember?.contactAddress ?: "") }
    var telegramContact by remember { mutableStateOf(initialMember?.telegramContact ?: "") }
    var whatsappContact by remember { mutableStateOf(initialMember?.whatsappContact ?: "") }
    var otherContact by remember { mutableStateOf(initialMember?.otherContact ?: "") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (initialMember == null) s.addMember else s.editMember,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) error = false
                    },
                    label = { Text(s.memberName) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    isError = error,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input")
                )

                OutlinedTextField(
                    value = contactAddress,
                    onValueChange = { contactAddress = it },
                    label = { Text(s.contactAddress) },
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_contact_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = telegramContact,
                        onValueChange = { telegramContact = it },
                        label = { Text(s.telegramHint) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = whatsappContact,
                        onValueChange = { whatsappContact = it },
                        label = { Text(s.whatsappHint) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Other contact field (Requirement 3)
                OutlinedTextField(
                    value = otherContact,
                    onValueChange = { otherContact = it },
                    label = { Text(s.otherContact) },
                    placeholder = { Text(s.otherContactHint) },
                    leadingIcon = { Icon(Icons.Default.Radio, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = s.subGroupSelection,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = subGroup == 1,
                        onClick = { subGroup = 1 },
                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                        label = { Text(s.subGroup1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = subGroup == 2,
                        onClick = { subGroup = 2 },
                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                        label = { Text(s.subGroup2) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        error = true
                    } else {
                        onSave(
                            name.trim(),
                            subGroup,
                            contactAddress.trim(),
                            telegramContact.trim(),
                            whatsappContact.trim(),
                            otherContact.trim()
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_member_button")
            ) {
                Text(s.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel)
            }
        }
    )
}
