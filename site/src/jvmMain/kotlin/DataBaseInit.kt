package org.example.ursafun.api

import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

// 1. The Exposed Table Definition
// This maps directly to the shared LocalPriceEntry data class we made earlier.
object LocalPriceEntries : Table("local_prices") {
    val id = integer("id").autoIncrement()
    val itemName = varchar("item_name", 255)
    val price = double("price")
    val location = varchar("location", 255)
    val submittedBy = varchar("submitted_by", 100)

    override val primaryKey = PrimaryKey(id)
}

// 2. The Initialization Function
// Kobweb runs this automatically on startup because of @InitApi
@InitApi
fun initDb(ctx: InitApiContext) {
    // Connect to a local file named "ursaprices.db".
    // SQLite will automatically create this file in your project root if it doesn't exist.
    Database.connect("jdbc:sqlite:ursaprices.db", driver = "org.sqlite.JDBC")

    // Create the table safely (it won't overwrite existing data if the table is already there)
    transaction {
        SchemaUtils.create(LocalPriceEntries)
    }
}