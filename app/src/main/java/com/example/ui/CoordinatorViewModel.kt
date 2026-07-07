package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CoordinatorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CoordinatorRepository

    init {
        val dao = AppDatabase.getDatabase(application).coordinatorDao()
        repository = CoordinatorRepository(dao)

        viewModelScope.launch {
            repository.allInventoryItems.first().let { items ->
                if (items.isEmpty()) {
                    val defaultItems = listOf(
                        InventoryItem(name = "Premium Canopy Tent (20x20)", category = "Tents", totalQty = 8, availableQty = 8, ratePerDay = 150.0),
                        InventoryItem(name = "High-Fidelity Audio sound system", category = "Sound", totalQty = 5, availableQty = 5, ratePerDay = 120.0),
                        InventoryItem(name = "Warm Glow Fairy Lights (50m)", category = "Lighting", totalQty = 15, availableQty = 15, ratePerDay = 25.0),
                        InventoryItem(name = "Elegant Gold Chiavari Chair", category = "Chairs", totalQty = 200, availableQty = 200, ratePerDay = 4.5),
                        InventoryItem(name = "Rustic Vineyard Oak Table", category = "Tables", totalQty = 20, availableQty = 20, ratePerDay = 45.0),
                        InventoryItem(name = "Luxury Velvet Lounge Sofa", category = "Lounge", totalQty = 6, availableQty = 6, ratePerDay = 95.0),
                        InventoryItem(name = "Professional Stage Platform", category = "Stage", totalQty = 3, availableQty = 3, ratePerDay = 250.0)
                    )
                    defaultItems.forEach { repository.insertInventoryItem(it) }
                }
            }
        }
    }

    val eventRentals: StateFlow<List<EventRental>> = repository.allEventRentals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedEventRentalId = MutableStateFlow<String?>(null)
    val selectedEventRentalId: StateFlow<String?> = _selectedEventRentalId.asStateFlow()

    val selectedEventRental: StateFlow<EventRental?> = _selectedEventRentalId
        .flatMapLatest { id ->
            if (id == null) flowOf<EventRental?>(null)
            else flow {
                emit(repository.getEventRentalById(id))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val guests: StateFlow<List<Guest>> = _selectedEventRentalId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getGuestsForEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<LogisticsTask>> = _selectedEventRentalId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getTasksForEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectEventRental(id: String?) {
        _selectedEventRentalId.value = id
    }

    // Event Operations
    fun addEventRental(eventRental: EventRental) {
        viewModelScope.launch {
            repository.insertEventRental(eventRental)
        }
    }

    fun updateEventRental(eventRental: EventRental) {
        viewModelScope.launch {
            repository.updateEventRental(eventRental)
        }
    }

    fun deleteEventRental(eventRental: EventRental) {
        viewModelScope.launch {
            repository.deleteEventRental(eventRental)
            if (_selectedEventRentalId.value == eventRental.id) {
                _selectedEventRentalId.value = null
            }
        }
    }

    // Guest Operations
    fun addGuest(guest: Guest) {
        viewModelScope.launch {
            repository.insertGuest(guest)
        }
    }

    fun updateGuest(guest: Guest) {
        viewModelScope.launch {
            repository.updateGuest(guest)
        }
    }

    fun deleteGuest(guest: Guest) {
        viewModelScope.launch {
            repository.deleteGuest(guest)
        }
    }

    // Task Operations
    fun addTask(task: LogisticsTask) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: LogisticsTask) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: LogisticsTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // AI Guest Message Assist State
    private val _aiResponseDraft = MutableStateFlow<String?>(null)
    val aiResponseDraft: StateFlow<String?> = _aiResponseDraft.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun generateReply(rentalInfo: String, question: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponseDraft.value = null
            val response = GeminiHelper.draftReply(rentalInfo, question)
            _aiResponseDraft.value = response
            _isAiLoading.value = false
        }
    }

    fun clearAiDraft() {
        _aiResponseDraft.value = null
    }

    // AI Checklist Generator State
    private val _aiChecklistDraft = MutableStateFlow<List<LogisticsTask>?>(null)
    val aiChecklistDraft: StateFlow<List<LogisticsTask>?> = _aiChecklistDraft.asStateFlow()

    private val _isAiChecklistLoading = MutableStateFlow(false)
    val isAiChecklistLoading: StateFlow<Boolean> = _isAiChecklistLoading.asStateFlow()

    fun generateLogisticsChecklist(event: EventRental) {
        viewModelScope.launch {
            _isAiChecklistLoading.value = true
            _aiChecklistDraft.value = null
            
            val info = GeminiHelper.draftLogisticsChecklist(
                eventName = event.name,
                eventType = event.type,
                description = event.description
            )
            
            // Parse response lines into LogisticsTasks
            val parsedTasks = info.lines()
                .filter { it.trim().startsWith("*") || it.trim().startsWith("-") || it.trim().startsWith("•") || it.trim().isNotEmpty() }
                .map { line ->
                    line.replace(Regex("^[*\\-•\\s\\d+.]"), "").trim()
                }
                .filter { it.length > 4 && !it.contains("Note:", ignoreCase = true) }
                .map { rawText ->
                    LogisticsTask(
                        eventRentalId = event.id,
                        title = rawText,
                        category = when {
                            rawText.contains("Setup", true) || rawText.contains("inspect", true) || rawText.contains("prepare", true) -> "Setup"
                            rawText.contains("Cater", true) || rawText.contains("food", true) || rawText.contains("drink", true) || rawText.contains("meal", true) -> "Catering"
                            rawText.contains("welcome", true) || rawText.contains("guest", true) || rawText.contains("send", true) || rawText.contains("code", true) -> "Guest Service"
                            rawText.contains("clean", true) || rawText.contains("trash", true) || rawText.contains("sweep", true) -> "Cleanup"
                            else -> "Logistics"
                        },
                        dueDate = event.date
                    )
                }
            
            _aiChecklistDraft.value = parsedTasks.take(6)
            _isAiChecklistLoading.value = false
        }
    }

    fun approveAiChecklist(tasksToInsert: List<LogisticsTask>) {
        viewModelScope.launch {
            tasksToInsert.forEach {
                repository.insertTask(it)
            }
            _aiChecklistDraft.value = null
        }
    }

    fun rejectAiChecklist() {
        _aiChecklistDraft.value = null
    }

    // Additional flows for the new dual role features
    val inventoryItems: StateFlow<List<InventoryItem>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookedEvents: StateFlow<List<BookedEvent>> = repository.allBookedEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<ArrivalNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Inventory operations
    fun addInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.insertInventoryItem(item)
        }
    }

    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.updateInventoryItem(item)
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    // Customer Event Booking Operations
    fun addBookedEvent(bookedEvent: BookedEvent, selectedQuantities: Map<String, Int>) {
        viewModelScope.launch {
            // Insert event booking
            repository.insertBookedEvent(bookedEvent)
            
            // Deduct from available quantity of each inventory item
            selectedQuantities.forEach { (itemId, qty) ->
                repository.getInventoryItemById(itemId)?.let { item ->
                    val newAvailable = (item.availableQty - qty).coerceAtLeast(0)
                    repository.updateInventoryItem(item.copy(availableQty = newAvailable))
                }
            }
        }
    }

    // Admin Dispatch / Status Transition & Push notifications
    fun updateBookedEventStatus(event: BookedEvent, newStatus: String) {
        viewModelScope.launch {
            val updated = event.copy(status = newStatus)
            repository.updateBookedEvent(updated)
            
            // If transitioned to ARRIVED, automatically insert an ArrivalNotification
            if (newStatus == "ARRIVED") {
                val notification = ArrivalNotification(
                    eventName = event.eventName,
                    message = "Hooray! The rental items for your event \"${event.eventName}\" have arrived. Delivery option was ${if(event.isDelivery) "delivery to " + event.location else "self-pickup"}."
                )
                repository.insertNotification(notification)
            }
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    // AI Customer Ideas Generator State
    private val _aiCustomerIdeas = MutableStateFlow<String?>(null)
    val aiCustomerIdeas: StateFlow<String?> = _aiCustomerIdeas.asStateFlow()

    private val _isAiIdeasLoading = MutableStateFlow(false)
    val isAiIdeasLoading: StateFlow<Boolean> = _isAiIdeasLoading.asStateFlow()

    fun generateEventIdeas(promptText: String, requestedType: String) {
        viewModelScope.launch {
            _isAiIdeasLoading.value = true
            _aiCustomerIdeas.value = null
            val response = GeminiHelper.generateEventIdeas(promptText, requestedType)
            _aiCustomerIdeas.value = response
            _isAiIdeasLoading.value = false
        }
    }

    fun clearCustomerIdeas() {
        _aiCustomerIdeas.value = null
    }
}
