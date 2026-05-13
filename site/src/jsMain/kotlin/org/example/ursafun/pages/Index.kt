package org.example.ursafun.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.browser.api
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.ursafun.model.LocalPriceEntry
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.TextInput

@Page
@Composable
fun HomePage() {
    val priceList = remember { mutableStateListOf<LocalPriceEntry>() }
    val coroutineScope = rememberCoroutineScope()

    // Form State
    var showForm by remember { mutableStateOf(false) } // Hides the text inputs initially
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemLocation by remember { mutableStateOf("") }
    var submitterName by remember { mutableStateOf("") }

    // Inline Editing State
    var editingRowId by remember { mutableStateOf<Int?>(null) }
    var editingPriceValue by remember { mutableStateOf("") }

    // Load data on start
    LaunchedEffect(Unit) {
        val fetchedData = fetchPrices()
        priceList.clear()
        priceList.addAll(fetchedData)
    }

    Surface(Modifier.minHeight(100.vh).fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(topBottom = 2.cssRem, leftRight = 1.cssRem),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpanText("URSA 2026 Local Price Leaderboard", Modifier.fontSize(2.cssRem).fontWeight(FontWeight.Bold).margin(bottom = 2.cssRem))

            // --- TOGGLE BUTTON ---
            Button(
                onClick = { showForm = !showForm },
                Modifier.margin(bottom = 1.cssRem).backgroundColor(if (showForm) Colors.Gray else Colors.Blue).color(Colors.White)
            ) {
                SpanText(if (showForm) "Close Form" else "Post a New Deal")
            }

            // --- SUBMISSION FORM (Hidden until clicked) ---
            if (showForm) {
                Column(
                    Modifier
                        .fillMaxWidth(90.percent)
                        .maxWidth(500.px)
                        .padding(1.cssRem)
                        .border(width = 1.px, style = LineStyle.Solid, color = Colors.LightGray)
                        .borderRadius(8.px)
                        .margin(bottom = 3.cssRem),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SpanText("Add New Price", Modifier.fontWeight(FontWeight.SemiBold).margin(bottom = 1.cssRem))

                    TextInput(value = itemName, attrs = Modifier.fillMaxWidth().margin(bottom = 0.5.cssRem).toAttrs { attr("placeholder", "Item (e.g. Water 1.5L)"); onInput { itemName = it.value } })
                    TextInput(value = itemPrice, attrs = Modifier.fillMaxWidth().margin(bottom = 0.5.cssRem).toAttrs { attr("placeholder", "Price (e.g. 5.50)"); onInput { itemPrice = it.value } })
                    TextInput(value = itemLocation, attrs = Modifier.fillMaxWidth().margin(bottom = 0.5.cssRem).toAttrs { attr("placeholder", "Store/Location"); onInput { itemLocation = it.value } })
                    TextInput(value = submitterName, attrs = Modifier.fillMaxWidth().margin(bottom = 1.cssRem).toAttrs { attr("placeholder", "Your Name"); onInput { submitterName = it.value } })

                    Button(
                        onClick = {
                            val priceDouble = itemPrice.toDoubleOrNull() ?: 0.0
                            if (itemName.isNotBlank() && priceDouble > 0 && submitterName.isNotBlank()) {
                                coroutineScope.launch {
                                    val success = submitNewPrice(LocalPriceEntry(itemName = itemName, price = priceDouble, location = itemLocation, submittedBy = submitterName))
                                    if (success) {
                                        itemName = ""; itemPrice = ""; itemLocation = ""
                                        val updated = fetchPrices()
                                        priceList.clear()
                                        priceList.addAll(updated)
                                        showForm = false // Auto-hide form on success
                                    }
                                }
                            }
                        },
                        Modifier.fillMaxWidth().backgroundColor(Colors.Green).color(Colors.White)
                    ) {
                        SpanText("Submit")
                    }
                }
            }

            // --- LEADERBOARD TABLE ---
            SpanText("Leaderboard", Modifier.fontSize(1.5.cssRem).margin(bottom = 1.cssRem))

            Column(Modifier.fillMaxWidth(95.percent).maxWidth(800.px)) {
                // Header Row (Added Submitter column and set text to white for contrast against black background)
                Row(Modifier.fillMaxWidth().padding(0.5.cssRem).backgroundColor(Colors.Black).color(Colors.White).fontWeight(FontWeight.Bold)) {
                    SpanText("Item", Modifier.weight(1f))
                    SpanText("Price", Modifier.weight(0.5f))
                    SpanText("Location", Modifier.weight(1f))
                    SpanText("User", Modifier.weight(0.8f))
                }

                // Data Rows
                priceList.forEach { entry ->
                    Row(
                        Modifier.fillMaxWidth().padding(0.5.cssRem).borderBottom(width = 1.px, style = LineStyle.Solid, color = Colors.LightGray),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpanText(entry.itemName, Modifier.weight(1f))

                        // --- INLINE EDITING LOGIC ---
                        if (editingRowId == entry.id) {
                            // Show input field when clicked
                            TextInput(
                                value = editingPriceValue,
                                attrs = Modifier.weight(0.5f).toAttrs {
                                    onInput { editingPriceValue = it.value }
                                    onKeyDown { event ->
                                        if (event.key == "Enter") {
                                            val newPrice = editingPriceValue.toDoubleOrNull()
                                            if (newPrice != null) {
                                                coroutineScope.launch {
                                                    // Update the database
                                                    val success = updatePriceData(entry.copy(price = newPrice))
                                                    if (success) {
                                                        val updated = fetchPrices()
                                                        priceList.clear()
                                                        priceList.addAll(updated)
                                                        editingRowId = null // Close the edit field
                                                    }
                                                }
                                            } else {
                                                editingRowId = null // Revert if they typed something invalid
                                            }
                                        }
                                    }
                                }
                            )
                        } else {
                            // Default text view (Clickable)
                            SpanText(
                                "DH ${entry.price}", // Added currency indicator for Morocco context
                                Modifier
                                    .weight(0.5f)
                                    .fontWeight(FontWeight.Bold)
                                    .color(Colors.Green)
                                    .cursor(Cursor.Pointer) // Changes mouse to a hand pointer
                                    .onClick {
                                        editingRowId = entry.id
                                        editingPriceValue = entry.price.toString()
                                    }
                            )
                        }

                        SpanText(entry.location, Modifier.weight(1f).fontSize(0.9.cssRem).color(Colors.DimGray))
                        // Display the submitter
                        SpanText(entry.submittedBy, Modifier.weight(0.8f).fontSize(0.8.cssRem).color(Colors.Gray).fontStyle(FontStyle.Italic))
                    }
                }

                if (priceList.isEmpty()) {
                    SpanText("No prices recorded yet. Be the first!", Modifier.margin(top = 1.cssRem).fontStyle(FontStyle.Italic))
                }
            }
        }
    }
}

// Keep your existing fetchPrices() and submitNewPrice() here...
suspend fun fetchPrices(): List<LocalPriceEntry> {
    return try {
        val responseBytes = window.api.get("getprices")
        val responseString = responseBytes.decodeToString()
        Json.decodeFromString<List<LocalPriceEntry>>(responseString)
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun submitNewPrice(entry: LocalPriceEntry): Boolean {
    return try {
        val jsonString = Json.encodeToString(entry)
        window.api.post("addprice", body = jsonString.encodeToByteArray())
        true
    } catch (e: Exception) {
        false
    }
}

// NEW: Function to send the edited price to the backend
suspend fun updatePriceData(entry: LocalPriceEntry): Boolean {
    return try {
        val jsonString = Json.encodeToString(entry)
        window.api.post("updateprice", body = jsonString.encodeToByteArray())
        true
    } catch (e: Exception) {
        console.error("Failed to update: ${e.message}")
        false
    }
}