package org.example.ursafun.api

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.setBodyText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.example.ursafun.model.LocalPriceEntry // Import your shared data class
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import com.varabyte.kobweb.api.http.readBodyText

// 1. GET Route: Fetch all prices
// This creates an endpoint at: /api/getprices
@Api("getprices")
suspend fun getPrices(ctx: ApiContext) {
    val pricesList = transaction {
        // Select all rows and order them by the lowest price first
        LocalPriceEntries.selectAll()
            .orderBy(LocalPriceEntries.price to SortOrder.ASC)
            .map { row ->
                // Map the database rows back into our shared Kotlin data class
                LocalPriceEntry(
                    id = row[LocalPriceEntries.id],
                    itemName = row[LocalPriceEntries.itemName],
                    price = row[LocalPriceEntries.price],
                    location = row[LocalPriceEntries.location],
                    submittedBy = row[LocalPriceEntries.submittedBy]
                )
            }
    }

    // Convert the Kotlin list to a JSON string and send it to the frontend
    ctx.res.setBodyText(Json.encodeToString(pricesList))
}

// 2. POST Route: Add a new price
// This creates an endpoint at: /api/addprice
@Api("addprice")
suspend fun addPrice(ctx: ApiContext) {
    try {
        // Read the incoming JSON string from the frontend
        val bodyText = ctx.req.readBodyText()
        if (bodyText == null) {
            ctx.res.status = 400 // Bad Request
            return
        }

        // Convert the JSON string back into our shared Kotlin object
        val newEntry = Json.decodeFromString<LocalPriceEntry>(bodyText)

        // Insert the new entry into the SQLite database
        transaction {
            LocalPriceEntries.insert {
                it[itemName] = newEntry.itemName
                it[price] = newEntry.price
                it[location] = newEntry.location
                it[submittedBy] = newEntry.submittedBy
            }
        }

        ctx.res.status = 200 // OK
        ctx.res.setBodyText("Price successfully added!")

    } catch (e: Exception) {
        ctx.res.status = 500 // Internal Server Error
        ctx.res.setBodyText(e.message ?: "An error occurred while adding the price.")
    }
}