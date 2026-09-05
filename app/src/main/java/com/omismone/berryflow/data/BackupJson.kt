package com.omismone.berryflow.data

import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val categories: List<Category>,
    val transactions: List<Transaction>,
    val recurrentEvents: List<RecurrentEvent>,
    val balance: Balance?
)

fun buildBackupJson(
    categories: List<Category>,
    transactions: List<Transaction>,
    recurrentEvents: List<RecurrentEvent>,
    balance: Balance?
): String {
    val root = JSONObject()

    root.put("categories", JSONArray().apply {
        categories.forEach { c ->
            put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("color", c.color)
                put("emoji", c.emoji)
                put("isDefault", c.isDefault)
            })
        }
    })

    root.put("transactions", JSONArray().apply {
        transactions.forEach { t ->
            put(JSONObject().apply {
                put("id", t.id)
                put("amount", t.amount)
                put("isIncome", t.isIncome)
                put("categoryId", t.categoryId)
                put("date", t.date)
                put("name", t.name ?: JSONObject.NULL)
            })
        }
    })

    root.put("recurrentEvents", JSONArray().apply {
        recurrentEvents.forEach { e ->
            put(JSONObject().apply {
                put("id", e.id)
                put("amount", e.amount)
                put("isIncome", e.isIncome)
                put("categoryId", e.categoryId)
                put("startDate", e.startDate)
                put("frequency", e.frequency)
                put("name", e.name ?: JSONObject.NULL)
                put("lastGeneratedDate", e.lastGeneratedDate ?: JSONObject.NULL)
            })
        }
    })

    root.put(
        "balance",
        if (balance == null) JSONObject.NULL
        else JSONObject().apply {
            put("amount", balance.amount)
            put("isSet", balance.isSet)
        }
    )

    return root.toString(2)
}

fun parseBackupJson(json: String): BackupData {
    val root = JSONObject(json)

    val categories = mutableListOf<Category>()
    val categoriesArray = root.getJSONArray("categories")
    for (i in 0 until categoriesArray.length()) {
        val obj = categoriesArray.getJSONObject(i)
        categories.add(
            Category(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                color = obj.getInt("color"),
                emoji = obj.getString("emoji"),
                isDefault = obj.getBoolean("isDefault")
            )
        )
    }

    val transactions = mutableListOf<Transaction>()
    val transactionsArray = root.getJSONArray("transactions")
    for (i in 0 until transactionsArray.length()) {
        val obj = transactionsArray.getJSONObject(i)
        transactions.add(
            Transaction(
                id = obj.getLong("id"),
                amount = obj.getDouble("amount"),
                isIncome = obj.getBoolean("isIncome"),
                categoryId = obj.getLong("categoryId"),
                date = obj.getLong("date"),
                name = if (obj.isNull("name")) null else obj.getString("name")
            )
        )
    }

    val recurrentEvents = mutableListOf<RecurrentEvent>()
    val eventsArray = root.getJSONArray("recurrentEvents")
    for (i in 0 until eventsArray.length()) {
        val obj = eventsArray.getJSONObject(i)
        recurrentEvents.add(
            RecurrentEvent(
                id = obj.getLong("id"),
                amount = obj.getDouble("amount"),
                isIncome = obj.getBoolean("isIncome"),
                categoryId = obj.getLong("categoryId"),
                startDate = obj.getLong("startDate"),
                frequency = obj.getString("frequency"),
                name = if (obj.isNull("name")) null else obj.getString("name"),
                lastGeneratedDate = if (obj.isNull("lastGeneratedDate")) null else obj.getLong("lastGeneratedDate")
            )
        )
    }

    val balance = if (root.isNull("balance")) null else {
        val obj = root.getJSONObject("balance")
        Balance(amount = obj.getDouble("amount"), isSet = obj.getBoolean("isSet"))
    }

    return BackupData(categories, transactions, recurrentEvents, balance)
}