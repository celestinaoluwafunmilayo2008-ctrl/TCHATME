package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.contacts.ContactSyncHelper
import com.example.data.model.CallType
import com.example.data.model.ContactEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.WhatsAppGreenDark
import com.example.ui.theme.WhatsAppGreenPrimary

@Composable
fun ContactsScreen(
    contacts: List<ContactEntity>,
    onBack: () -> Unit,
    onOpenChat: (contactId: Long) -> Unit,
    onStartCall: (contactId: Long, callType: CallType) -> Unit,
    onAddNewContact: (name: String, phone: String, about: String) -> Unit,
    onSyncPhoneContacts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddContactDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Permission launcher for READ_CONTACTS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onSyncPhoneContacts()
            Toast.makeText(context, "Phone contacts synced successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission needed to access phone contacts", Toast.LENGTH_LONG).show()
        }
    }

    val registeredContacts = remember(contacts, searchQuery) {
        contacts.filter {
            it.isRegisteredOnTChatMe && (
                searchQuery.isEmpty() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery)
            )
        }
    }

    val otherContacts = remember(contacts, searchQuery) {
        contacts.filter {
            !it.isRegisteredOnTChatMe && (
                searchQuery.isEmpty() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery)
            )
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = WhatsAppGreenDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSearching) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(60.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        IconButton(onClick = {
                            isSearching = false
                            searchQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close search",
                                tint = Color.White
                            )
                        }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search name or number...", color = Color.White.copy(alpha = 0.7f)) },
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("contacts_search_input")
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(60.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("contacts_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select contact",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "${contacts.size} contacts available",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = { isSearching = true },
                            modifier = Modifier.testTag("contacts_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Contacts",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                if (ContactSyncHelper.hasContactPermission(context)) {
                                    onSyncPhoneContacts()
                                    Toast.makeText(context, "Synced device phone contacts!", Toast.LENGTH_SHORT).show()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                }
                            },
                            modifier = Modifier.testTag("sync_contacts_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Contacts",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Privacy Assurance Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE7FCE8)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "🔒",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = "Private & Local Conversations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = WhatsAppGreenDark
                            )
                            Text(
                                text = "Only you can see your chats with your contacts. Conversations are stored securely and end-to-end encrypted on this device.",
                                fontSize = 12.sp,
                                color = Color(0xFF2E4D3E),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Action Rows (New Group, New Contact, Sync from Phone)
            item {
                ContactActionRow(
                    icon = Icons.Default.PersonAdd,
                    title = "New contact / Add friend",
                    subtitle = "Add by name & phone number",
                    onClick = { showAddContactDialog = true },
                    trailingIcon = Icons.Default.QrCode
                )

                ContactActionRow(
                    icon = Icons.Default.ContactPhone,
                    title = "Import from Phone Address Book",
                    subtitle = "Sync and chat with your device contacts",
                    onClick = {
                        if (ContactSyncHelper.hasContactPermission(context)) {
                            onSyncPhoneContacts()
                            Toast.makeText(context, "Imported contacts from phone!", Toast.LENGTH_SHORT).show()
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    }
                )

                ContactActionRow(
                    icon = Icons.Default.GroupAdd,
                    title = "New group",
                    onClick = {
                        Toast.makeText(context, "Select contacts to create a private group", Toast.LENGTH_SHORT).show()
                    }
                )

                Text(
                    text = "Your Contacts (${contacts.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // All Contacts (Registered + Synced)
            items(contacts.filter {
                searchQuery.isEmpty() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery)
            }, key = { it.id }) { contact ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChat(contact.id) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("contact_item_${contact.id}")
                ) {
                    AvatarView(
                        name = contact.name,
                        avatarRes = contact.avatarRes,
                        colorHex = contact.avatarColorHex,
                        size = 48.dp,
                        isOnline = contact.isOnline
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (contact.about.isNotBlank()) contact.about else contact.phoneNumber,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    // Quick Voice & Video Call Action Buttons
                    IconButton(
                        onClick = { onStartCall(contact.id, CallType.VOICE) },
                        modifier = Modifier.size(38.dp).testTag("quick_call_${contact.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = WhatsAppGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onStartCall(contact.id, CallType.VIDEO) },
                        modifier = Modifier.size(38.dp).testTag("quick_video_${contact.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = WhatsAppGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Add New Contact Dialog
    if (showAddContactDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newAbout by remember { mutableStateOf("Available on TChatMe") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = {
                Text(text = "Add New Contact", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_contact_name_input")
                    )

                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_contact_phone_input")
                    )

                    OutlinedTextField(
                        value = newAbout,
                        onValueChange = { newAbout = it },
                        label = { Text("Status / About") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            onAddNewContact(newName.trim(), newPhone.trim(), newAbout.trim())
                            showAddContactDialog = false
                        } else {
                            Toast.makeText(context, "Please enter name and phone number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenPrimary),
                    modifier = Modifier.testTag("save_new_contact_button")
                ) {
                    Text("Save Contact", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ContactActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(WhatsAppGreenPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
