package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventRental

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    existingItem: EventRental?,
    onSave: (EventRental) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var type by remember { mutableStateOf(existingItem?.type ?: "Rental") }
    var date by remember { mutableStateOf(existingItem?.date ?: "") }
    var time by remember { mutableStateOf(existingItem?.time ?: "") }
    var location by remember { mutableStateOf(existingItem?.location ?: "") }
    var description by remember { mutableStateOf(existingItem?.description ?: "") }
    var accessCode by remember { mutableStateOf(existingItem?.accessCode ?: "") }
    var wifiSsid by remember { mutableStateOf(existingItem?.wifiSsid ?: "") }
    var wifiPassword by remember { mutableStateOf(existingItem?.wifiPassword ?: "") }
    var contactPhone by remember { mutableStateOf(existingItem?.contactPhone ?: "") }

    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingItem == null) "Register New Site" else "Update Listing",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E2E6)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFD0BCFF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF111318)
                ),
                modifier = Modifier.testTag("add_edit_top_bar")
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Classification type
                Text(
                    text = "1. REQUISITE CLASSIFICATION",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFFD0BCFF)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TypeChip(
                        label = "Rental Listing",
                        icon = Icons.Default.Home,
                        isSelected = type == "Rental",
                        onClick = { type = "Rental" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("type_chip_rental")
                    )
                    TypeChip(
                        label = "Custom Event",
                        icon = Icons.Default.Festival,
                        isSelected = type == "Event",
                        onClick = { type = "Event" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("type_chip_event")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Section 2: Details & Core Info
                Text(
                    text = "2. CORE LISTING DETAILS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFFD0BCFF)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (e.g. Mansion Sunset Villa, Peak Wedding)") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    isError = showError && name.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Complete Address / Specific Room") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    isError = showError && location.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_location"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Scheduled Date (YYYY-MM-DD)") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        isError = showError && date.isBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_date"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Start Time (e.g. 14:00)") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_time"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Overview & Operational Description") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("input_description"),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Section 3: WiFi & Key Access Codes (This is vital context used by Gemini AI!)
                Text(
                    text = "3. ACCESSIBILITY & LOGISTICS (AI Context)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFFD0BCFF)
                )

                OutlinedTextField(
                    value = accessCode,
                    onValueChange = { accessCode = it },
                    label = { Text("Check-in Access Pin / Lockbox Key") },
                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_access_code"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = wifiSsid,
                        onValueChange = { wifiSsid = it },
                        label = { Text("Wifi SSID (Network)") },
                        leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_wifi_ssid"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = wifiPassword,
                        onValueChange = { wifiPassword = it },
                        label = { Text("Wifi Password") },
                        leadingIcon = { Icon(Icons.Default.Password, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_wifi_password"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Site / Host Emergency Contact Phone") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_phone"),
                    singleLine = true
                )

                if (showError) {
                    Text(
                        text = "Please complete all mandatory fields (* Name, Location, and Date).",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        if (name.isBlank() || location.isBlank() || date.isBlank()) {
                            showError = true
                        } else {
                            onSave(
                                EventRental(
                                    id = existingItem?.id ?: java.util.UUID.randomUUID().toString(),
                                    name = name,
                                    type = type,
                                    date = date,
                                    time = time,
                                    location = location,
                                    description = description,
                                    accessCode = accessCode,
                                    wifiSsid = wifiSsid,
                                    wifiPassword = wifiPassword,
                                    contactPhone = contactPhone
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingItem == null) "Register Listing" else "Save Changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun TypeChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(isSelected),
        modifier = modifier.height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Helper to map default template colors to our gorgeous EmmaDavid gold theme colors
private fun Color(value: Long): androidx.compose.ui.graphics.Color {
    return when (value) {
        0xFFD0BCFF -> androidx.compose.ui.graphics.Color(0xFFE5C158) // Primary Gold
        0xFF381E72 -> androidx.compose.ui.graphics.Color(0xFF0E0F12) // OnPrimary Charcoal
        0xFF4A4458 -> androidx.compose.ui.graphics.Color(0xFF5A523E) // Outline Brass
        else -> androidx.compose.ui.graphics.Color(value)
    }
}

