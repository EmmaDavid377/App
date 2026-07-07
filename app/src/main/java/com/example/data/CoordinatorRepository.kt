package com.example.data

import kotlinx.coroutines.flow.Flow

class CoordinatorRepository(private val dao: CoordinatorDao) {
    val allEventRentals: Flow<List<EventRental>> = dao.getAllEventRentals()

    suspend fun getEventRentalById(id: String): EventRental? = dao.getEventRentalById(id)

    suspend fun insertEventRental(eventRental: EventRental) = dao.insertEventRental(eventRental)

    suspend fun updateEventRental(eventRental: EventRental) = dao.updateEventRental(eventRental)

    suspend fun deleteEventRental(eventRental: EventRental) = dao.deleteEventRental(eventRental)

    suspend fun deleteEventRentalById(id: String) = dao.deleteEventRentalById(id)

    fun getGuestsForEvent(eventRentalId: String): Flow<List<Guest>> = dao.getGuestsForEvent(eventRentalId)

    suspend fun insertGuest(guest: Guest) = dao.insertGuest(guest)

    suspend fun updateGuest(guest: Guest) = dao.updateGuest(guest)

    suspend fun deleteGuest(guest: Guest) = dao.deleteGuest(guest)

    suspend fun deleteGuestById(id: String) = dao.deleteGuestById(id)

    fun getTasksForEvent(eventRentalId: String): Flow<List<LogisticsTask>> = dao.getTasksForEvent(eventRentalId)

    suspend fun insertTask(task: LogisticsTask) = dao.insertTask(task)

    suspend fun updateTask(task: LogisticsTask) = dao.updateTask(task)

    suspend fun deleteTask(task: LogisticsTask) = dao.deleteTask(task)

    suspend fun deleteTaskById(id: String) = dao.deleteTaskById(id)

    // Inventory operations
    val allInventoryItems: Flow<List<InventoryItem>> = dao.getAllInventoryItems()
    suspend fun getInventoryItemById(id: String): InventoryItem? = dao.getInventoryItemById(id)
    suspend fun insertInventoryItem(item: InventoryItem) = dao.insertInventoryItem(item)
    suspend fun updateInventoryItem(item: InventoryItem) = dao.updateInventoryItem(item)
    suspend fun deleteInventoryItem(item: InventoryItem) = dao.deleteInventoryItem(item)

    // Booking operations
    val allBookedEvents: Flow<List<BookedEvent>> = dao.getAllBookedEvents()
    suspend fun getBookedEventById(id: String): BookedEvent? = dao.getBookedEventById(id)
    suspend fun insertBookedEvent(bookedEvent: BookedEvent): Long = dao.insertBookedEvent(bookedEvent)
    suspend fun updateBookedEvent(bookedEvent: BookedEvent) = dao.updateBookedEvent(bookedEvent)
    suspend fun deleteBookedEvent(bookedEvent: BookedEvent) = dao.deleteBookedEvent(bookedEvent)

    // Notification operations
    val allNotifications: Flow<List<ArrivalNotification>> = dao.getAllNotifications()
    suspend fun insertNotification(notification: ArrivalNotification) = dao.insertNotification(notification)
    suspend fun clearAllNotifications() = dao.clearAllNotifications()
}
