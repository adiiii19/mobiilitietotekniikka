package com.example.mobiili.database

import android.net.Uri
import com.example.mobiili.Message


object SampleData {
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

    )

    private fun getDrawableUri(resourceName: String): String {
        return Uri.parse("android.resource://com.example.mobiili/drawable/$resourceName").toString()
    }
}

