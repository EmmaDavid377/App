package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventRental
import com.example.data.Guest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestCommunicationScreen(
    eventRentals: List<EventRental>,
    guests: List<Guest>,
    selectedEventId: String?,
    onEventSelected: (String) -> Unit,
    aiDraft: String?,
    isAiLoading: Boolean,
    onGenerateReply: (rentalInfo: String, inquiry: String) -> Unit,
    onClearDraft: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedGuest by remember { mutableStateOf<Guest?>(null) }
    var guestInquiry by remember { mutableStateOf("") }

    val activeRental = eventRentals.find { it.id == selectedEventId }

    // Update selected guest if guest list changes or empty
    LaunchedEffect(guests, selectedEventId) {
        selectedGuest = guests.firstOrNull()
    }

    // Pre-filled quick inquiries
    val quickInquiries = listOf(
        "What is the WiFi network and password?",
        "How do I gain entry/access?",
        "Who is the emergency contact on-site?",
        "Is there options for early check-in or late check-out?",
        "What is the physical address of the listing?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD0BCFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RE",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF381E72),
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "Guest Communications AI",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFE2E2E6)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF111318)
                ),
                modifier = Modifier.testTag("comm_top_bar")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Choose Property/Event
            Text(
                text = "1. SELECT LISTING OR VENUE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Color(0xFFD0BCFF)
            )

            if (eventRentals.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "Please register an Event or Rental listing first on the dashboard to access AI drafting.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("comm_properties_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(eventRentals, key = { it.id }) { item ->
                        val selected = item.id == selectedEventId
                        FilterChip(
                            selected = selected,
                            onClick = { onEventSelected(item.id) },
                            label = { Text(item.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (item.type == "Rental") Icons.Default.Home else Icons.Default.Festival,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("comm_chip_prop_${item.id}")
                        )
                    }
                }
            }

            // Section 2: Choose Guest (if any)
            if (activeRental != null) {
                Text(
                    text = "2. SELECT RECIPIENT GUEST",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFFD0BCFF)
                )

                if (guests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No guests registered for this venue/event. Go to details to register guests.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(guests, key = { it.id }) { guest ->
                            val isSelected = guest.id == selectedGuest?.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedGuest = guest },
                                label = { Text(guest.name) },
                                modifier = Modifier.testTag("comm_chip_guest_${guest.id}")
                            )
                        }
                    }
                }

                // Section 3: Enter Guest Inquiry
                Text(
                    text = "3. ENTER GUEST QUESTION",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFFD0BCFF)
                )

                OutlinedTextField(
                    value = guestInquiry,
                    onValueChange = { guestInquiry = it },
                    placeholder = { Text("e.g. Can I connect to the wifi? or I can't open the front door lockbox.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("comm_inquiry_input"),
                    label = { Text("What did the guest ask?") },
                    maxLines = 4
                )

                // Quick presets
                Text(
                    text = "Suggested FAQ Presets:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickInquiries.forEachIndexed { idx, item ->
                        SuggestionChip(
                            onClick = { guestInquiry = item },
                            label = { Text(item, maxLines = 1) },
                            modifier = Modifier.testTag("comm_preset_$idx")
                        )
                    }
                }

                // Call to action
                Button(
                    onClick = {
                        val wifiDetails = "SSID: ${activeRental.wifiSsid}, Pass: ${activeRental.wifiPassword}"
                        val contactDetails = "Emergency Contact: ${activeRental.contactPhone}"
                        val accessDetails = "Lockbox Pin: ${activeRental.accessCode}"
                        val fullContext = """
                            Item Name: ${activeRental.name}
                            Type: ${activeRental.type}
                            Address: ${activeRental.location}
                            Description: ${activeRental.description}
                            Wifi Details: $wifiDetails
                            Lockbox / Entry Code: $accessDetails
                            Contact Number: $contactDetails
                            Guest Name: ${selectedGuest?.name ?: "Guest"}
                        """.trimIndent()

                        onGenerateReply(fullContext, guestInquiry)
                    },
                    enabled = guestInquiry.isNotBlank() && !isAiLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72),
                        disabledContainerColor = Color(0xFF2D2F33),
                        disabledContentColor = Color(0xFF909094)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("comm_btn_generate"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF381E72)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini is drafting...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Draft Reply with Gemini AI")
                    }
                }

                // Section 4: AI Response Output Box
                if (aiDraft != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comm_result_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2D2F33)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MarkChatRead,
                                        contentDescription = null,
                                        tint = Color(0xFFD0BCFF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI Response Draft",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE2E2E6)
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(aiDraft))
                                            Toast.makeText(context, "Draft copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("comm_btn_copy")
                                    ) {
                                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Response", tint = Color(0xFFD0BCFF))
                                    }

                                    IconButton(
                                        onClick = onClearDraft,
                                        modifier = Modifier.testTag("comm_btn_clear")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear Draft", tint = Color(0xFFC4C6CF))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color(0xFF4A4458))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = aiDraft,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE2E2E6),
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(aiDraft))
                                    Toast.makeText(context, "Ready to send! Text copied.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4A4458),
                                    contentColor = Color(0xFFE2E2E6)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy & Send Response")
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Property/Event selected",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Select a site above to access customized AI drafts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
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

