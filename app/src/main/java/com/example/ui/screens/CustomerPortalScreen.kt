package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ArrivalNotification
import com.example.data.BookedEvent
import com.example.data.InventoryItem
import java.util.UUID

@Composable
fun CustomerPortalScreen(
    inventoryItems: List<InventoryItem>,
    bookedEvents: List<BookedEvent>,
    notifications: List<ArrivalNotification>,
    aiCustomerIdeas: String?,
    isAiIdeasLoading: Boolean,
    onGenerateIdeas: (String, String) -> Unit,
    onClearIdeas: () -> Unit,
    onAddBooking: (BookedEvent, Map<String, Int>) -> Unit,
    onClearNotifications: () -> Unit
) {
    val context = LocalContext.current
    var selectedPortalTab by remember { mutableStateOf("rent") } // "rent" or "orders"

    // Inventory search and filtering states
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categoriesList = remember(inventoryItems) {
        listOf("All") + inventoryItems.map { it.category }.distinct().map { it.uppercase() }.distinct().sorted()
    }

    val filteredInventoryItems = remember(inventoryItems, searchQuery, selectedCategoryFilter) {
        inventoryItems.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "All" ||
                    item.category.equals(selectedCategoryFilter, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    // Booking form states
    var eventName by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("2026-06-30") }
    var durationDays by remember { mutableStateOf("1") }
    var isDelivery by remember { mutableStateOf(true) }
    var selectedZoneIndex by remember { mutableStateOf(0) }
    var customAddress by remember { mutableStateOf("") }
    var customDescription by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var callbackRequested by remember { mutableStateOf(false) }

    // Delivery presets list
    val deliveryZones = listOf(
        Pair("Downtown Logistics Core (5 miles)", 15.0),
        Pair("Westside Premium Suburbs (12 miles)", 30.0),
        Pair("Northeast Heights (18 miles)", 45.0),
        Pair("Rural Scenic Ranch Borders (30 miles)", 75.0)
    )

    // Items map: Item ID -> Quantity ordered
    val cartQuantities = remember { mutableStateMapOf<String, Int>() }

    // Total calculations
    val deliveryFee = if (isDelivery) deliveryZones[selectedZoneIndex].second else 0.0
    val durationInt = durationDays.toIntOrNull() ?: 1
    val itemsCost = cartQuantities.entries.sumOf { (itemId, qty) ->
        val item = inventoryItems.find { it.id == itemId }
        (item?.ratePerDay ?: 0.0) * qty * durationInt
    }
    val grandTotal = itemsCost + deliveryFee

    // Notifications status popup
    var showNotificationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111318))
    ) {
        // Portal Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD0BCFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = Color(0xFF381E72),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "CUSTOMER PORTAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFFD0BCFF)
                    )
                    Text(
                        text = "Rent & Fulfill",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE2E2E6)
                    )
                }
            }

            // Notification Ring Icon with count
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { showNotificationDialog = true }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = if (notifications.isNotEmpty()) Color(0xFFD0BCFF) else Color(0xFFC4C6CF)
                )
                if (notifications.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Red, shape = CircleShape)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = notifications.size.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Sub Navigation Tabs
        TabRow(
            selectedTabIndex = if (selectedPortalTab == "rent") 0 else 1,
            containerColor = Color(0xFF1F2023),
            contentColor = Color(0xFFD0BCFF),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedPortalTab == "rent",
                onClick = { selectedPortalTab = "rent" },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Order Rentals", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("portal_tab_rent")
            )
            Tab(
                selected = selectedPortalTab == "orders",
                onClick = { selectedPortalTab = "orders" },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("My Bookings (${bookedEvents.size})", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("portal_tab_orders")
            )
        }

        // Main content render
        if (selectedPortalTab == "rent") {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Intro Header card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2025)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFF2D2F33))
                    ) {
                        Column {
                            // Beautiful generated catalog backdrop banner with soft atmospheric fade
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_customer_hero_1782215999564),
                                    contentDescription = "Cosmic Event Style Catalog Banner",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color(0xFF1E2025).copy(alpha = 0.95f)
                                                )
                                            )
                                        )
                                )
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ESTIMATE & REGISTER EVENT ORDER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFD0BCFF),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "EmmaDavid Nexus",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFE2E2E6)
                                    )
                                    Text(
                                        text = "International Limited",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFD0BCFF),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Reserve elite items directly from real stock. Specify pickup or professional dispatch with automatic tracking notifications upon site arrival.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFC4C6CF)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Image(
                                    painter = painterResource(id = R.drawable.img_emmadavid_logo_1782247274536),
                                    contentDescription = "EmmaDavid Nexus Logo",
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.5.dp, Color(0xFFD0BCFF), RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // 1. Core Profile Details
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "1. GUEST HOST DETAILS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Your Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_customer_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF4A4458)
                            )
                        )

                        OutlinedTextField(
                            value = eventName,
                            onValueChange = { eventName = it },
                            label = { Text("Event Name / Theme") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_event_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF4A4458)
                            )
                        )

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Contact Phone (for delivery driver)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_customer_phone"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF4A4458)
                            )
                        )
                    }
                }

                // 🌟 OPTIONAL: SPARK EVENT IDEAS WITH AI GEMINI CO-GEN
                item {
                    var showAiPanel by remember { mutableStateOf(false) }
                    var ideasPromptLocal by remember { mutableStateOf("") }
                    var selectedType by remember { mutableStateOf("text") } // "text" or "image_prompt"
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F30)),
                        border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAiPanel = !showAiPanel },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFD0BCFF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "SPARK EVENT IDEAS WITH AI",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD0BCFF),
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "Generate Themes & Visual Backdrop Concepts (Optional)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFC4C6CF)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (showAiPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle Section",
                                    tint = Color(0xFFD0BCFF)
                                )
                            }
                            
                            if (showAiPanel) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Confused about styling? Tell Gemini what kind of celebration or party vibe you want, and let it generate high-fidelity thematic blueprints or photorealistic backdrop descriptions:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC4C6CF)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedTextField(
                                    value = ideasPromptLocal,
                                    onValueChange = { ideasPromptLocal = it },
                                    label = { Text("What is your party vision?") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3,
                                    placeholder = { Text("Describe the celebration vibe (e.g. Backyard neon birthday, vintage wedding...)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFD0BCFF),
                                        unfocusedBorderColor = Color(0xFF4A4458)
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Option 1: Styling Blueprint
                                    FilterChip(
                                        selected = selectedType == "text",
                                        onClick = { selectedType = "text" },
                                        label = { Text("Thematic Styling Blueprint", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        leadingIcon = if (selectedType == "text") {
                                            { Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFD0BCFF),
                                            selectedLabelColor = Color(0xFF381E72)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedType == "text")
                                    )
                                    
                                    // Option 2: Visual Backdrop Brief
                                    FilterChip(
                                        selected = selectedType == "image_prompt",
                                        onClick = { selectedType = "image_prompt" },
                                        label = { Text("Camera Backdrop Concept", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        leadingIcon = if (selectedType == "image_prompt") {
                                            { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFD0BCFF),
                                            selectedLabelColor = Color(0xFF381E72)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedType == "image_prompt")
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (ideasPromptLocal.isNotBlank()) {
                                                onGenerateIdeas(ideasPromptLocal, selectedType)
                                            } else {
                                                Toast.makeText(context, "Please describe your vision first!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFD0BCFF),
                                            contentColor = Color(0xFF381E72)
                                        ),
                                        modifier = Modifier.weight(1f),
                                        enabled = !isAiIdeasLoading
                                    ) {
                                        if (isAiIdeasLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF381E72), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Gemini is brainstorming...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ask Gemini Co-Gen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    if (aiCustomerIdeas != null) {
                                        OutlinedButton(
                                            onClick = {
                                                onClearIdeas()
                                                ideasPromptLocal = ""
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFF4A4458))
                                        ) {
                                            Text("Reset", fontSize = 12.sp)
                                        }
                                    }
                                }
                                
                                // Show AI Ideas block
                                if (aiCustomerIdeas != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF111318))
                                            .border(1.dp, Color(0xFF2D2F33), RoundedCornerShape(12.dp))
                                            .padding(14.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (selectedType == "text") "✨ RECOMMENDED BLUEPRINT" else "📸 VISUAL PHOTO CONCEPT",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFFFF9800)
                                                )
                                                
                                                IconButton(
                                                    onClick = {
                                                        // Auto fill Theme / description if applicable!
                                                        eventName = ideasPromptLocal.take(40)
                                                        customDescription = aiCustomerIdeas
                                                        Toast.makeText(context, "Applied blueprint directly to Booking text below!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.AssignmentReturned, contentDescription = "Use in booking", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = aiCustomerIdeas,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White,
                                                lineHeight = 18.sp
                                            )
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "💡 Tip: Click the paste icon next to category label to write this idea directly to your Custom Requirements box below!",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Timeline & Logistics Preference
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "2. SERVICE SCHEDULE & LOGISTICS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = eventDate,
                                onValueChange = { eventDate = it },
                                label = { Text("Date (YYYY-MM-DD)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_event_date"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD0BCFF),
                                    unfocusedBorderColor = Color(0xFF4A4458)
                                )
                            )

                            OutlinedTextField(
                                value = durationDays,
                                onValueChange = {
                                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                        durationDays = it
                                    }
                                },
                                label = { Text("Duration (Days)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_event_duration"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD0BCFF),
                                    unfocusedBorderColor = Color(0xFF4A4458)
                                )
                            )
                        }

                        // Delivery vs Pickup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1F2023))
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDelivery) Color(0xFF4A4458) else Color.Transparent)
                                    .clickable { isDelivery = true }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = if (isDelivery) Color(0xFFD0BCFF) else Color(0xFFC4C6CF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Deliver Items",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDelivery) Color.White else Color(0xFFC4C6CF)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isDelivery) Color(0xFF4A4458) else Color.Transparent)
                                    .clickable { isDelivery = false }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Backpack,
                                        contentDescription = null,
                                        tint = if (!isDelivery) Color(0xFFD0BCFF) else Color(0xFFC4C6CF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Self Pickup",
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isDelivery) Color.White else Color(0xFFC4C6CF)
                                    )
                                }
                            }
                        }

                        // Address or Zone Picker
                        if (isDelivery) {
                            Text(
                                text = "Select Delivery Target Zone (Determines logistics cost):",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC4C6CF)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                deliveryZones.forEachIndexed { idx, zone ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (selectedZoneIndex == idx) Color(0xFF2D2F33) else Color.Transparent)
                                            .border(
                                                width = 1.dp,
                                                color = if (selectedZoneIndex == idx) Color(0xFFD0BCFF) else Color(0xFF4A4458),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedZoneIndex = idx }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            RadioButton(
                                                selected = selectedZoneIndex == idx,
                                                onClick = { selectedZoneIndex = idx },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD0BCFF))
                                            )
                                            Text(
                                                text = zone.first,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = "$${zone.second.toInt()}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD0BCFF)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = customAddress,
                                onValueChange = { customAddress = it },
                                label = { Text("Exact Delivery Street Address") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_delivery_address"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD0BCFF),
                                    unfocusedBorderColor = Color(0xFF4A4458)
                                )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2D2F33))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFD0BCFF)
                                    )
                                    Text(
                                        text = "Pick up address: Rent & Event HQ, Logistics Ward 4B. Pickup is open 24/7. Standard handling fees are $0.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE2E2E6)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Select Live Inventory Catalog Items
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. RENTALS DIRECT INVENTORY",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD0BCFF)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2D2F33))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Catalog stock live",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Green
                                )
                            }
                        }

                        // Search box
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search catalog items by name or category...", fontSize = 13.sp, color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF2D2F33),
                                focusedContainerColor = Color(0xFF1E1F22),
                                unfocusedContainerColor = Color(0xFF1E1F22)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Filters row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Box(modifier = Modifier.weight(1f)) {
                                val scrollState = androidx.compose.foundation.rememberScrollState()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(scrollState),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    categoriesList.forEach { category ->
                                        val isSelected = selectedCategoryFilter.uppercase() == category.uppercase()
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) Color(0xFFD0BCFF) else Color(0xFF2D2F33))
                                                .clickable { selectedCategoryFilter = category }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = category,
                                                color = if (isSelected) Color(0xFF381E72) else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Render items
                        if (filteredInventoryItems.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        "No items found matching criteria",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "Try selecting another category or resetting search query.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            filteredInventoryItems.forEach { item ->
                                val selectedQty = cartQuantities[item.id] ?: 0
                                
                                val itemIconAndGradient = when (item.category.lowercase()) {
                                    "tents", "tent" -> Pair(Icons.Default.Home, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFE57373), Color(0xFFC62828))))
                                    "lighting", "lights" -> Pair(Icons.Default.Lightbulb, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFFF176), Color(0xFFFBC02D))))
                                    "sound", "audio" -> Pair(Icons.Default.VolumeUp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF64B5F6), Color(0xFF1565C0))))
                                    "chairs", "chair" -> Pair(Icons.Default.EventSeat, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFE65100))))
                                    "tables", "table" -> Pair(Icons.Default.Layers, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFAED581), Color(0xFF558B2F))))
                                    "lounge", "sofa" -> Pair(Icons.Default.Weekend, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFBA68C8), Color(0xFF6A1B9A))))
                                    "stage", "platform" -> Pair(Icons.Default.Star, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF4DB6AC), Color(0xFF00695C))))
                                    else -> Pair(Icons.Default.Inventory, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF90A4AE), Color(0xFF37474F))))
                                }

                                val inStock = item.availableQty > 0

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
                                    border = BorderStroke(1.dp, Color(0xFF2D2F33))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Left Side: Beautiful category visual picture / illustration card
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(itemIconAndGradient.second),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = itemIconAndGradient.first,
                                                contentDescription = item.category,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Middle Side: item text metadata
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.category.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD0BCFF)
                                            )
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (inStock) Color.White else Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "$${item.ratePerDay}/Day",
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFFD0BCFF),
                                                    fontSize = 13.sp
                                                )
                                                
                                                // Live Stock Badge (In stock vs unavailable)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (inStock) Color(0xFF1C3A27) else Color(0xFF3D1D1D))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .clip(CircleShape)
                                                                .background(if (inStock) Color.Green else Color.Red)
                                                        )
                                                        Text(
                                                            text = if (inStock) "In Stock: ${item.availableQty}" else "Unavailable",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (inStock) Color.Green else Color.Red
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Right Side: Quantity Incrementor (only enable if item is in stock)
                                        if (inStock) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        if (selectedQty > 0) {
                                                            val next = selectedQty - 1
                                                            if (next == 0) {
                                                                cartQuantities.remove(item.id)
                                                            } else {
                                                                cartQuantities[item.id] = next
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(Color(0xFF2D2F33), shape = CircleShape)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Remove,
                                                        contentDescription = "Decrease",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }

                                                Text(
                                                    text = selectedQty.toString(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 14.sp
                                                )

                                                IconButton(
                                                    onClick = {
                                                        if (selectedQty < item.availableQty) {
                                                            cartQuantities[item.id] = selectedQty + 1
                                                        } else {
                                                            Toast.makeText(context, "Cannot exceed available warehouse stock!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(Color(0xFF2D2F33), shape = CircleShape)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Increase",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF2E2E2E))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "SOLD OUT",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.LightGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Assistive Voice Callback Mode or Text Details
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "4. CAN'T TYPE? EXPLAIN VIA VERBAL / SUPPORT INTAKE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )

                        Text(
                            text = "If you have specific visual requirements, are on the move, or cannot easily detail your layout, dictate notes below or request direct call assistance:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC4C6CF)
                        )

                        OutlinedTextField(
                            value = customDescription,
                            onValueChange = { customDescription = it },
                            label = { Text("Special requirements description (Optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("form_custom_description"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF4A4458)
                            )
                        )

                        // Call helpline assistant button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (callbackRequested) Color(0xFF1B3D2B) else Color(0xFF2D2F33))
                                .clickable {
                                    callbackRequested = !callbackRequested
                                    if (callbackRequested) {
                                        Toast.makeText(context, "Direct Support Intake Scheduled! An agent will call you.", Toast.LENGTH_LONG).show()
                                    }
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (callbackRequested) Color(0xFF2E7D32) else Color(0xFFD0BCFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (callbackRequested) Icons.Default.Check else Icons.Default.SupportAgent,
                                    tint = if (callbackRequested) Color.White else Color(0xFF381E72),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (callbackRequested) "✓ Support Callback Scheduled" else "Call Agent to Dictate Instead",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (callbackRequested) "A layout specialist will contact you on your phone shortly" else "Tap here to schedule a technician call-back within 5 mins",
                                    fontSize = 11.sp,
                                    color = Color(0xFFC4C6CF)
                                )
                            }
                        }
                    }
                }

                // 5. Build Cost Estimation and Submission
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1F2023))
                            .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ORDER COST SUMMARY",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD0BCFF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Selected Rentals Cost:", color = Color(0xFFC4C6CF), fontSize = 14.sp)
                            Text("$${itemsCost.toInt()} (for ${durationInt}d)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fulfillment Dispatch Fee:", color = Color(0xFFC4C6CF), fontSize = 14.sp)
                            Text(if (isDelivery) "$${deliveryFee.toInt()}" else "FREE (Self Collect)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFF4A4458))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Contract Total:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("$${grandTotal.toInt()}", color = Color(0xFFD0BCFF), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (customerName.isBlank() || eventName.isBlank()) {
                                    Toast.makeText(context, "Please enter your name and event name first!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (cartQuantities.isEmpty()) {
                                    Toast.makeText(context, "Please add at least one rental item to order!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // Construct item string
                                val itemDetailsLog = cartQuantities.entries.map { (itemId, qty) ->
                                    val item = inventoryItems.find { it.id == itemId }
                                    "${qty}x ${item?.name ?: "Hardware Item"}"
                                }.joinToString(", ")

                                val finalAddress = if (isDelivery) {
                                    if (customAddress.isNotBlank()) customAddress else deliveryZones[selectedZoneIndex].first
                                } else {
                                    "Self-Pickup from HQ"
                                }

                                val eventBooking = BookedEvent(
                                    eventName = eventName,
                                    customerName = customerName,
                                    date = eventDate,
                                    durationDays = durationInt,
                                    isDelivery = isDelivery,
                                    location = finalAddress,
                                    deliveryCost = deliveryFee,
                                    itemsDetail = itemDetailsLog,
                                    customDescription = customDescription,
                                    customerPhone = customerPhone,
                                    callbackRequested = callbackRequested,
                                    status = "BOOKED"
                                )

                                onAddBooking(eventBooking, cartQuantities.toMap())
                                Toast.makeText(context, "Success! Event booking & rental order placed securely.", Toast.LENGTH_LONG).show()

                                // Reset form
                                customerName = ""
                                eventName = ""
                                customAddress = ""
                                customDescription = ""
                                customerPhone = ""
                                callbackRequested = false
                                cartQuantities.clear()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_submit_order"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.SendAndArchive, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Book Rental & Schedule Setup", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                    }
                }
            }
        } else {
            // "orders" List Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "YOUR ACTIVE EVENT BOOKINGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD0BCFF),
                        letterSpacing = 1.5.sp
                    )
                }

                if (bookedEvents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LibraryAdd,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "No Active Booked Events",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Go to \"Order Rentals\" to select items and book your event date.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    items(bookedEvents) { event ->
                        val phaseColor = when (event.status) {
                            "BOOKED" -> Color(0xFF2196F3)
                            "PREPARING" -> Color(0xFFFF9800)
                            "SHIPPED" -> Color(0xFFE91E63)
                            "ARRIVED" -> Color(0xFF4CAF50)
                            else -> Color.White
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
                            border = BorderStroke(1.dp, Color(0xFF2D2F33))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = event.eventName.uppercase(),
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Host: ${event.customerName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFC4C6CF)
                                        )
                                    }

                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(phaseColor.copy(alpha = 0.15f))
                                            .border(1.dp, phaseColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = event.status,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            color = phaseColor
                                        )
                                    }
                                }

                                Divider(color = Color(0xFF2D2F33))

                                // Logistics list info
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Date: ${event.date} (${event.durationDays} days duration)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Destination: ${event.location}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Rented equipment: ${event.itemsDetail}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    }
                                }

                                if (event.callbackRequested) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1B3D2B))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.Green, modifier = Modifier.size(14.dp))
                                            Text(
                                                "Contact agent call scheduled at ${event.customerPhone}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Active Notifications Center overlay dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CircleNotifications,
                        contentDescription = null,
                        tint = Color(0xFFD0BCFF)
                    )
                    Text("Live Alerts & Arrivals Log", color = Color.White)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (notifications.isEmpty()) {
                        Text(
                            text = "No current arrival alerts. Once the delivery agents dispatch and update your booked events status to 'ARRIVED', instant popups will appear here!",
                            color = Color(0xFFC4C6CF),
                            fontSize = 13.sp
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 280.dp)
                        ) {
                            items(notifications) { notif ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF2D2F33))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = notif.eventName.uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD0BCFF),
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "Just Now",
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notif.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Close", color = Color(0xFFD0BCFF))
                }
            },
            dismissButton = {
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = {
                        onClearNotifications()
                        showNotificationDialog = false
                    }) {
                        Text("Clear All", color = Color.Red)
                    }
                }
            },
            containerColor = Color(0xFF1F2023),
            shape = RoundedCornerShape(24.dp)
        )
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

