package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CoordinatorViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

// Simple, robust type-safe screens for coordination workspace
sealed class Screen {
    object Main : Screen()
    data class Detail(val id: String) : Screen()
    data class AddEdit(val id: String?) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: CoordinatorViewModel = viewModel()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
                
                // Master roles toggle
                var userRole by remember { mutableStateOf("CUSTOMER") } // "CUSTOMER" or "LOGISTICS"
                
                // Active sub tab for Logistics role
                var currentBottomTab by remember { mutableStateOf("dispatch") } // "dispatch", "listings", "communications"

                // State flows
                val rentals by viewModel.eventRentals.collectAsStateWithLifecycle()
                val selectedRental by viewModel.selectedEventRental.collectAsStateWithLifecycle()
                val guests by viewModel.guests.collectAsStateWithLifecycle()
                val tasks by viewModel.tasks.collectAsStateWithLifecycle()

                // New flows for customer & delivery updates
                val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
                val bookedEvents by viewModel.bookedEvents.collectAsStateWithLifecycle()
                val notifications by viewModel.notifications.collectAsStateWithLifecycle()

                // AI States
                val aiDraft by viewModel.aiResponseDraft.collectAsStateWithLifecycle()
                val isAiDraftLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
                val aiTasksProposed by viewModel.aiChecklistDraft.collectAsStateWithLifecycle()
                val isAiTasksLoading by viewModel.isAiChecklistLoading.collectAsStateWithLifecycle()

                val aiCustomerIdeas by viewModel.aiCustomerIdeas.collectAsStateWithLifecycle()
                val isAiIdeasLoading by viewModel.isAiIdeasLoading.collectAsStateWithLifecycle()

                val selectedEventIdForComm by viewModel.selectedEventRentalId.collectAsStateWithLifecycle()

                // Back button handler for detailed overlays
                BackHandler(enabled = currentScreen != Screen.Main) {
                    currentScreen = Screen.Main
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Main -> {
                            Scaffold(
                                bottomBar = {
                                    // Present bottom bar only inside LOGISTICS Mode
                                    if (userRole == "LOGISTICS") {
                                            NavigationBar(
                                                modifier = Modifier
                                                    .testTag("bottom_nav_bar")
                                                    .windowInsetsPadding(WindowInsets.navigationBars),
                                                containerColor = androidx.compose.ui.graphics.Color(0xFF131418)
                                            ) {
                                                NavigationBarItem(
                                                    selected = currentBottomTab == "dispatch",
                                                    onClick = { currentBottomTab = "dispatch" },
                                                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Dispatch") },
                                                    label = { Text("Logistics Hub", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                                                    colors = NavigationBarItemDefaults.colors(
                                                        selectedIconColor = androidx.compose.ui.graphics.Color(0xFFE5C158),
                                                        selectedTextColor = androidx.compose.ui.graphics.Color(0xFFE5C158),
                                                        indicatorColor = androidx.compose.ui.graphics.Color(0xFF2C2E35),
                                                        unselectedIconColor = androidx.compose.ui.graphics.Color(0xFFC4C6CF),
                                                        unselectedTextColor = androidx.compose.ui.graphics.Color(0xFFC4C6CF)
                                                    ),
                                                    modifier = Modifier.testTag("nav_tab_dispatch")
                                                )
                                                NavigationBarItem(
                                                    selected = currentBottomTab == "listings",
                                                    onClick = { currentBottomTab = "listings" },
                                                    icon = { Icon(Icons.Default.Business, contentDescription = "Listings") },
                                                    label = { Text("Listings & Sites", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                                                    colors = NavigationBarItemDefaults.colors(
                                                        selectedIconColor = androidx.compose.ui.graphics.Color(0xFFE5C158),
                                                        selectedTextColor = androidx.compose.ui.graphics.Color(0xFFE5C158),
                                                        indicatorColor = androidx.compose.ui.graphics.Color(0xFF2C2E35),
                                                        unselectedIconColor = androidx.compose.ui.graphics.Color(0xFFC4C6CF),
                                                        unselectedTextColor = androidx.compose.ui.graphics.Color(0xFFC4C6CF)
                                                    ),
                                                    modifier = Modifier.testTag("nav_tab_listings")
                                                )
                                                NavigationBarItem(
                                                    selected = currentBottomTab == "communications",
                                                    onClick = { currentBottomTab = "communications" },
                                                    icon = { Icon(Icons.Default.Chat, contentDescription = "Communications") },
                                                    label = { Text("Guest Chat AI", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                                                    colors = NavigationBarItemDefaults.colors(
                                                        selectedIconColor = androidx.compose.ui.graphics.Color(0xFFE5C158),
                                                        selectedTextColor = androidx.compose.ui.graphics.Color(0xFFE5C158),
                                                        indicatorColor = androidx.compose.ui.graphics.Color(0xFF2C2E35),
                                                        unselectedIconColor = androidx.compose.ui.graphics.Color(0xFFC4C6CF),
                                                        unselectedTextColor = androidx.compose.ui.graphics.Color(0xFFC4C6CF)
                                                    ),
                                                    modifier = Modifier.testTag("nav_tab_communications")
                                                )
                                            }
                                    }
                                }
                            ) { innerPadding ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    // 🌟 Master Role Selector Bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(androidx.compose.ui.graphics.Color(0xFF1F2023))
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(
                                                        if (userRole == "CUSTOMER") androidx.compose.ui.graphics.Color(0xFFE5C158) else androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                                        shape = RoundedCornerShape(50)
                                                    )
                                            )
                                            Text(
                                                text = "PORTAL MODE: " + if (userRole == "CUSTOMER") "CUSTOMER" else "LOGISTICS / ADMIN",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = androidx.compose.ui.graphics.Color(0xFFC4C6CF),
                                                letterSpacing = 0.5.sp
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                userRole = if (userRole == "CUSTOMER") "LOGISTICS" else "CUSTOMER"
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = androidx.compose.ui.graphics.Color(0xFF4A4458),
                                                contentColor = androidx.compose.ui.graphics.Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (userRole == "CUSTOMER") Icons.Default.SwapCalls else Icons.Default.Flip,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Role: " + if (userRole == "CUSTOMER") "Admin" else "Customer",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Render core screen based on Role State
                                    AnimatedContent(
                                        targetState = if (userRole == "CUSTOMER") "CUSTOMER" else currentBottomTab,
                                        label = "DualRoleNavigationState",
                                        modifier = Modifier.weight(1f)
                                    ) { tabId ->
                                        when (tabId) {
                                            "CUSTOMER" -> {
                                                CustomerPortalScreen(
                                                    inventoryItems = inventoryItems,
                                                    bookedEvents = bookedEvents,
                                                    notifications = notifications,
                                                    aiCustomerIdeas = aiCustomerIdeas,
                                                    isAiIdeasLoading = isAiIdeasLoading,
                                                    onGenerateIdeas = { promptText, requestedType ->
                                                        viewModel.generateEventIdeas(promptText, requestedType)
                                                    },
                                                    onClearIdeas = {
                                                        viewModel.clearCustomerIdeas()
                                                    },
                                                    onAddBooking = { booking, selectedQtys ->
                                                        viewModel.addBookedEvent(booking, selectedQtys)
                                                    },
                                                    onClearNotifications = {
                                                        viewModel.clearNotifications()
                                                    }
                                                )
                                            }
                                            "dispatch" -> {
                                                LogisticsHubScreen(
                                                    inventoryItems = inventoryItems,
                                                    bookedEvents = bookedEvents,
                                                    onUpdateBookingStatus = { event, stepStatus ->
                                                        viewModel.updateBookedEventStatus(event, stepStatus)
                                                    },
                                                    onAddInventoryItem = { item ->
                                                        viewModel.addInventoryItem(item)
                                                    },
                                                    onUpdateInventoryItem = { item ->
                                                        viewModel.updateInventoryItem(item)
                                                    },
                                                    onDeleteInventoryItem = { item ->
                                                        viewModel.deleteInventoryItem(item)
                                                    }
                                                )
                                            }
                                            "listings" -> {
                                                DashboardScreen(
                                                    eventRentals = rentals,
                                                    onEventClick = { id ->
                                                        viewModel.selectEventRental(id)
                                                        currentScreen = Screen.Detail(id)
                                                    },
                                                    onAddEventClick = {
                                                        currentScreen = Screen.AddEdit(null)
                                                    },
                                                    onEditEventClick = { id ->
                                                        viewModel.selectEventRental(id)
                                                        currentScreen = Screen.AddEdit(id)
                                                    },
                                                    onDeleteEventClick = { item ->
                                                        viewModel.deleteEventRental(item)
                                                    }
                                                )
                                            }
                                            "communications" -> {
                                                GuestCommunicationScreen(
                                                    eventRentals = rentals,
                                                    guests = guests,
                                                    selectedEventId = selectedEventIdForComm,
                                                    onEventSelected = { id ->
                                                        viewModel.selectEventRental(id)
                                                    },
                                                    aiDraft = aiDraft,
                                                    isAiLoading = isAiDraftLoading,
                                                    onGenerateReply = { contextInfo, inquiry ->
                                                        viewModel.generateReply(contextInfo, inquiry)
                                                    },
                                                    onClearDraft = {
                                                        viewModel.clearAiDraft()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        is Screen.Detail -> {
                            selectedRental?.let { rental ->
                                DetailScreen(
                                    eventRental = rental,
                                    guests = guests,
                                    tasks = tasks,
                                    aiLinecheck = aiTasksProposed,
                                    isAiLoading = isAiTasksLoading,
                                    onBack = { currentScreen = Screen.Main },
                                    onAddTask = { task -> viewModel.addTask(task) },
                                    onToggleTask = { task -> viewModel.updateTask(task.copy(isCompleted = !task.isCompleted)) },
                                    onDeleteTask = { task -> viewModel.deleteTask(task) },
                                    onAddGuest = { guest -> viewModel.addGuest(guest) },
                                    onToggleCheckIn = { guest -> viewModel.updateGuest(guest.copy(checkedIn = !guest.checkedIn)) },
                                    onDeleteGuest = { guest -> viewModel.deleteGuest(guest) },
                                    onRunAiChecklist = { viewModel.generateLogisticsChecklist(rental) },
                                    onApproveAiChecklist = { checklist -> viewModel.approveAiChecklist(checklist) },
                                    onRejectAiChecklist = { viewModel.rejectAiChecklist() }
                                )
                            } ?: run {
                                currentScreen = Screen.Main
                            }
                        }

                        is Screen.AddEdit -> {
                            val existingIndex = rentals.find { it.id == screen.id }
                            AddEditScreen(
                                existingItem = existingIndex,
                                onSave = { updated ->
                                    if (screen.id == null) {
                                        viewModel.addEventRental(updated)
                                    } else {
                                        viewModel.updateEventRental(updated)
                                    }
                                    currentScreen = Screen.Main
                                },
                                onBack = { currentScreen = Screen.Main }
                            )
                        }
                    }
                }
            }
        }
    }
}
