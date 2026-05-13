package org.example.ursafun.model

import kotlinx.serialization.Serializable

/**
 * Shared data model for the Local Price List.
 * This class is used by both the Ktor backend and the Compose HTML frontend.
 */
@Serializable
data class LocalPriceEntry(
    val id: Int = 0, // Defaults to 0 so the database can auto-generate the real ID later
    val itemName: String,
    val price: Double,
    val location: String,
    val submittedBy: String
)