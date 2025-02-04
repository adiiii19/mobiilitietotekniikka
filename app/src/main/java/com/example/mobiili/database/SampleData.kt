package com.example.mobiili.database

import android.net.Uri
import com.example.mobiili.Message

/**
 * SampleData for Jetpack Compose Tutorial
 */
object SampleData {
    // Sample conversation data with image URLs
    val conversationSample = listOf(
        Message(
            author = "John Smith",
            body = "Test...Test...Test...",
            imageUrl = getDrawableUri("john_smith2")
        ),
        Message(
            author = "John Smith",
            body = """List of Android versions:
            |Android KitKat (API 19)
            |Android Lollipop (API 21)
            |Android Marshmallow (API 23)
            |Android Nougat (API 24)
            |Android Oreo (API 26)
            |Android Pie (API 28)
            |Android 10 (API 29)
            |Android 11 (API 30)
            |Android 12 (API 31)""".trim(),
            imageUrl = getDrawableUri("john_smith2")
        ),
        Message(
            author = "John Smith",
            body = """I think Kotlin is my favorite programming language.
            |It's so much fun!""".trim(),
            imageUrl = getDrawableUri("john_smith2")
        ),
        Message(
            author = "John Smith",
            body = "I love working with Jetpack Compose!",
            imageUrl = getDrawableUri("john_smith2")
        ),
        Message(
            author = "John Smith",
            body = """The latest Android features:
            |1. Jetpack Compose
            |2. Android 12 customizations
            |3. Kotlin-based libraries
            |4. Enhanced UI toolkit""".trim(),
            imageUrl = getDrawableUri("john_smith2")
        ),
        Message(
            author = "John Smith",
            body = """I was reading about Android 13 today. It's bringing new updates like:
            |- Privacy changes
            |- Performance improvements
            |- New UI features""".trim(),
            imageUrl = getDrawableUri("john_smith2")
        ),
        Message(
            author = "John Smith",
            body = """Working on the new mobile app project, it's coming along really well!""",
            imageUrl = getDrawableUri("john_smith2")
        ),
        Message(
            author = "John Smith",
            body = """Excited for the new Kotlin release, with improvements in performance and language features.""",
            imageUrl = getDrawableUri("john_smith2")
        ),
        Message(
            author = "John Smith",
            body = """Android development is evolving so fast, it's amazing how much has changed over the years.""",
            imageUrl = getDrawableUri("john_smith2")
        ),
        Message(
            author = "John Smith",
            body = """Happy to be learning more about Jetpack Compose for UI design. It's so powerful!""",
            imageUrl = getDrawableUri("john_smith2")
        ),
        // Add more messages here if needed
    )

    private fun getDrawableUri(resourceName: String): String {
        return Uri.parse("android.resource://com.example.mobiili/drawable/$resourceName").toString()
    }
}

