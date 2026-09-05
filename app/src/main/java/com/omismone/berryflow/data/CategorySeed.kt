package com.omismone.berryflow.data

// Categories created automatically on first app launch, if the categories table is still empty.
object CategorySeed {

    val defaultCategory = Category(
        name = "Default",
        color = 0xFFBDBDBD.toInt(),
        emoji = "🗂️",
        isDefault = true
    )

    val baseCategories = listOf(
        Category(name = "books", color = 0xFF9575CD.toInt(), emoji = "📚"),
        Category(name = "fuel", color = 0xFFE57373.toInt(), emoji = "⛽"),
        Category(name = "gift", color = 0xFFFFB74D.toInt(), emoji = "🎁"),
        Category(name = "paycheck", color = 0xFF64B5F6.toInt(), emoji = "💰"),
        Category(name = "bar", color = 0xFFFFD180.toInt(), emoji = "🍺"),
        Category(name = "groceries", color = 0xFFF48FB1.toInt(), emoji = "🛒"),
        Category(name = "car", color = 0xFF90A4AE.toInt(), emoji = "🚗"),
        Category(name = "eat", color = 0xFFFF8A65.toInt(), emoji = "🍎"),
        Category(name = "eat out", color = 0xFFFFCCBC.toInt(), emoji = "🍔"),
        Category(name = "going out", color = 0xFFB39DDB.toInt(), emoji = "🎉"),
        Category(name = "workout", color = 0xFFAED581.toInt(), emoji = "🏋️"),
        Category(name = "entertainment", color = 0xFFBA68C8.toInt(), emoji = "🎬"),
        Category(name = "self care", color = 0xFF4DD0E1.toInt(), emoji = "🧴"),
        Category(name = "smartphone", color = 0xFFB0BEC5.toInt(), emoji = "📱"),
        Category(name = "education", color = 0xFFFFD54F.toInt(), emoji = "🎓"),
        Category(name = "home", color = 0xFFA1887F.toInt(), emoji = "🏠"),
        Category(name = "clothes", color = 0xFF4DB6AC.toInt(), emoji = "👕"),
        Category(name = "hardware", color = 0xFF78909C.toInt(), emoji = "🔧"),
        Category(name = "health", color = 0xFF80CBC4.toInt(), emoji = "🏥"),
        Category(name = "travel", color = 0xFF81D4FA.toInt(), emoji = "✈️"),
        Category(name = "motorbike", color = 0xFFFF8A65.toInt(), emoji = "🏍️"),
        Category(name = "games", color = 0xFF9575CD.toInt(), emoji = "🎮"),
        Category(name = "train", color = 0xFF64B5F6.toInt(), emoji = "🚆"),
        Category(name = "bills", color = 0xFFE0E0E0.toInt(), emoji = "🧾"),
        Category(name = "ai", color = 0xFFB0BEC5.toInt(), emoji = "🤖")
    )
}