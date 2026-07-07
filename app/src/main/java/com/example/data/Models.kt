package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "event_rentals")
data class EventRental(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "Event" or "Rental"
    val date: String, // e.g., "2026-06-25"
    val time: String = "", // e.g., "14:00"
    val location: String,
    val description: String = "",
    val accessCode: String = "",
    val wifiSsid: String = "",
    val wifiPassword: String = "",
    val contactPhone: String = "",
    val imageUrl: String = "" // Hero decoration
)

@Entity(tableName = "guests")
data class Guest(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val eventRentalId: String,
    val name: String,
    val email: String,
    val rsvpStatus: String, // "YES", "NO", "PENDING"
    val checkedIn: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "logistics_tasks")
data class LogisticsTask(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val eventRentalId: String,
    val title: String,
    val category: String, // "Setup", "Catering", "Logistics", "Guest Service", "Cleanup"
    val isCompleted: Boolean = false,
    val assignedTo: String = "",
    val dueDate: String = ""
)

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // "Chairs", "Tables", "Tents", "Lighting", "Stage"
    val totalQty: Int,
    val availableQty: Int,
    val ratePerDay: Double,
    val imageUrl: String = ""
)

@Entity(tableName = "booked_events")
data class BookedEvent(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val eventName: String,
    val customerName: String,
    val date: String,
    val durationDays: Int = 1,
    val isDelivery: Boolean = true,
    val location: String = "",
    val deliveryCost: Double = 0.0,
    val itemsDetail: String = "", // JSON or comma-separated string of items, e.g. "2x Luxury Tent, 10x Chair"
    val customDescription: String = "", // custom explanation
    val customerPhone: String = "",
    val callbackRequested: Boolean = false,
    val status: String = "BOOKED" // "BOOKED", "PREPARING", "SHIPPED", "ARRIVED"
)

@Entity(tableName = "arrival_notifications")
data class ArrivalNotification(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val eventName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
