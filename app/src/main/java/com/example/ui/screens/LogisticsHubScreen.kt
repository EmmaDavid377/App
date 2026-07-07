package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookedEvent
import com.example.data.InventoryItem

@Composable
fun LogisticsHubScreen(
    inventoryItems: List<InventoryItem>,
    bookedEvents: List<BookedEvent>,
    onUpdateBookingStatus: (BookedEvent, String) -> Unit,
    onAddInventoryItem: (InventoryItem) -> Unit,
    onUpdateInventoryItem: (InventoryItem) -> Unit,
    onDeleteInventoryItem: (InventoryItem) -> Unit
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf("bookings") } // "bookings" or "inventory"

    // Addition dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("Tents") }
    var newItemTotalQty by remember { mutableStateOf("10") }
    var newItemRate by remember { mutableStateOf("50.0") }

    // Callback simulated dialer state
    var simulatingCallUser by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111318))
    ) {
        // Hub Header
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
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = Color(0xFF381E72),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "DISPATCH & DELIVERY PANEL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFFD0BCFF)
                    )
                    Text(
                        text = "Warehouse Hub",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE2E2E6)
                    )
                }
            }

            // Quick Stats Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2D2F33))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${bookedEvents.size} Booked | ${inventoryItems.size} items",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Sub Navigation Tabs
        TabRow(
            selectedTabIndex = if (activeSubTab == "bookings") 0 else 1,
            containerColor = Color(0xFF1F2023),
            contentColor = Color(0xFFD0BCFF),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeSubTab == "bookings",
                onClick = { activeSubTab = "bookings" },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Bookings & Run", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("admin_tab_bookings")
            )
            Tab(
                selected = activeSubTab == "inventory",
                onClick = { activeSubTab = "inventory" },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Manage Inventory", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("admin_tab_inventory")
            )
        }

        // Tab Content
        if (activeSubTab == "bookings") {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "ACTIVE DISPATCH SCHEDULE",
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
                                    Icons.Default.AddTask,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "No Bookings Currently Scheduled",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "When customers book events and rent gear, they will appear here.",
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
                            border = BorderStroke(1.dp, Color(0xFF292A2D))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // User info & Status flag
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = event.eventName.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "Customer: ${event.customerName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFC4C6CF)
                                        )
                                    }

                                    // Display color coded Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(phaseColor.copy(alpha = 0.15f))
                                            .border(1.dp, phaseColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = event.status,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = phaseColor
                                        )
                                    }
                                }

                                Divider(color = Color(0xFF2d2f33))

                                // Event Details
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Date Scheduled: ${event.date} (${event.durationDays} days reservation)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PinDrop, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Address/Location: ${event.location}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    }

                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Items to deliver: ${event.itemsDetail}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    }

                                    if (event.customDescription.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Host Requests: \"${event.customDescription}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFFFCC80)
                                            )
                                        }
                                    }
                                }

                                // Callback Help Section
                                if (event.callbackRequested) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF382315))
                                            .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(8.dp))
                                            .clickable { simulatingCallUser = event.customerName + " (" + event.customerPhone + ")" }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SupportAgent,
                                                contentDescription = null,
                                                tint = Color(0xFFFF9800),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "VOICE EXPLAIN REQUESTED",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFFF9800)
                                                )
                                                Text(
                                                    text = "Customer cannot easily type, wants callback.",
                                                    color = Color.White,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { simulatingCallUser = event.customerName + " (" + event.customerPhone + ")" },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFF9800),
                                                contentColor = Color.Black
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("CALL OUT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Divider(color = Color(0xFF2D2F33))

                                // Status controls
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "TRANSITION DELIVERY PROCESS:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC4C6CF)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("BOOKED", "PREPARING", "SHIPPED", "ARRIVED").forEach { step ->
                                            val isSelected = event.status == step
                                            val stepColor = when (step) {
                                                "BOOKED" -> Color(0xFF2196F3)
                                                "PREPARING" -> Color(0xFFFF9800)
                                                "SHIPPED" -> Color(0xFFE91E63)
                                                "ARRIVED" -> Color(0xFF4CAF50)
                                                else -> Color.White
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) stepColor else Color(0xFF292A2D))
                                                    .clickable { onUpdateBookingStatus(event, step) }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = step,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF8C8E93),
                                                    fontSize = 9.sp
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
        } else {
            // "inventory" Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card with add action
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT CATALOG STOCKS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF),
                            letterSpacing = 1.5.sp
                        )

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Stock Item", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                if (inventoryItems.isEmpty()) {
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
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "No stock items registered",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    items(inventoryItems) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2023)),
                            border = BorderStroke(1.dp, Color(0xFF2D2F33))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFD0BCFF)
                                    )
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$${item.ratePerDay.toInt()}/day",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.LightGray,
                                            fontSize = 13.sp
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF2D2F33))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Total Qty: ${item.totalQty}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Adjust counters
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "AVAILABLESTOCK:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (item.availableQty > 0) {
                                                    onUpdateInventoryItem(item.copy(availableQty = item.availableQty - 1))
                                                }
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Color(0xFF2D2F33), shape = CircleShape)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }

                                        Text(
                                            text = item.availableQty.toString(),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            modifier = Modifier.widthIn(min = 16.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (item.availableQty < item.totalQty) {
                                                    onUpdateInventoryItem(item.copy(availableQty = item.availableQty + 1))
                                                } else {
                                                    // Auto increment total too if increasing available beyond total
                                                    onUpdateInventoryItem(item.copy(
                                                        totalQty = item.totalQty + 1,
                                                        availableQty = item.availableQty + 1
                                                    ))
                                                }
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Color(0xFF2D2F33), shape = CircleShape)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }

                                    TextButton(
                                        onClick = { onDeleteInventoryItem(item) },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text("Delete Item", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dial Simulating Dialog
    if (simulatingCallUser != null) {
        AlertDialog(
            onDismissRequest = { simulatingCallUser = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = Color.Green)
                    Text("Support Hotline Outbound Dial")
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Simulating call integration with customer:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Text(
                        text = simulatingCallUser ?: "",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Green
                    )

                    Text(
                        text = "Connects dispatch technician directly to customer phone line to discuss item requirements, spatial layouts, and delivery coordinates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { simulatingCallUser = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                ) {
                    Text("Disconnect Call", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1F2023),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    // Add Stock Item dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Catalog Product", color = Color.White) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Item Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF4A4458)
                        )
                    )

                    OutlinedTextField(
                        value = newItemCategory,
                        onValueChange = { newItemCategory = it },
                        label = { Text("Category (e.g. Chairs, Lighting, Tents)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF4A4458)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newItemTotalQty,
                            onValueChange = { newItemTotalQty = it },
                            label = { Text("Initial Stock Qty") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF4A4458)
                            )
                        )

                        OutlinedTextField(
                            value = newItemRate,
                            onValueChange = { newItemRate = it },
                            label = { Text("Rental Price / Day ($)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF4A4458)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = newItemTotalQty.toIntOrNull() ?: 1
                        val rate = newItemRate.toDoubleOrNull() ?: 20.0
                        if (newItemName.isNotBlank() && newItemCategory.isNotBlank()) {
                            val item = InventoryItem(
                                name = newItemName,
                                category = newItemCategory,
                                totalQty = qty,
                                availableQty = qty,
                                ratePerDay = rate
                            )
                            onAddInventoryItem(item)
                            Toast.makeText(context, "Product registered to direct catalog!", Toast.LENGTH_SHORT).show()

                            // Reset fields & close
                            newItemName = ""
                            showAddDialog = false
                        } else {
                            Toast.makeText(context, "Fill header fields first!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    )
                ) {
                    Text("Add Product", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color(0xFFC4C6CF))
                }
            },
            containerColor = Color(0xFF1F2023)
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

