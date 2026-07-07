package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CoordinatorDao {
    // Event & Rental Queries
    @Query("SELECT * FROM event_rentals ORDER BY date ASC")
    fun getAllEventRentals(): Flow<List<EventRental>>

    @Query("SELECT * FROM event_rentals WHERE id = :id")
    suspend fun getEventRentalById(id: String): EventRental?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventRental(eventRental: EventRental)

    @Update
    suspend fun updateEventRental(eventRental: EventRental)

    @Delete
    suspend fun deleteEventRental(eventRental: EventRental)

    @Query("DELETE FROM event_rentals WHERE id = :id")
    suspend fun deleteEventRentalById(id: String)

    // Guest Queries
    @Query("SELECT * FROM guests WHERE eventRentalId = :eventRentalId ORDER BY name ASC")
    fun getGuestsForEvent(eventRentalId: String): Flow<List<Guest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuest(guest: Guest)

    @Update
    suspend fun updateGuest(guest: Guest)

    @Delete
    suspend fun deleteGuest(guest: Guest)

    @Query("DELETE FROM guests WHERE id = :id")
    suspend fun deleteGuestById(id: String)

    // Logistics Task Queries
    @Query("SELECT * FROM logistics_tasks WHERE eventRentalId = :eventRentalId ORDER BY isCompleted ASC, category ASC, title ASC")
    fun getTasksForEvent(eventRentalId: String): Flow<List<LogisticsTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: LogisticsTask)

    @Update
    suspend fun updateTask(task: LogisticsTask)

    @Delete
    suspend fun deleteTask(task: LogisticsTask)

    @Query("DELETE FROM logistics_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    // Inventory Queries
    @Query("SELECT * FROM inventory_items ORDER BY category ASC, name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getInventoryItemById(id: String): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)

    // BookedEvent Queries
    @Query("SELECT * FROM booked_events ORDER BY date ASC")
    fun getAllBookedEvents(): Flow<List<BookedEvent>>

    @Query("SELECT * FROM booked_events WHERE id = :id")
    suspend fun getBookedEventById(id: String): BookedEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookedEvent(bookedEvent: BookedEvent): Long

    @Update
    suspend fun updateBookedEvent(bookedEvent: BookedEvent)

    @Delete
    suspend fun deleteBookedEvent(bookedEvent: BookedEvent)

    // ArrivalNotification Queries
    @Query("SELECT * FROM arrival_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<ArrivalNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: ArrivalNotification)

    @Query("DELETE FROM arrival_notifications")
    suspend fun clearAllNotifications()
}
