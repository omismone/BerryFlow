package com.omismone.berryflow.data

import android.content.Context

object AppContainer {
    @Volatile
    private var repository: BerryFlowRepository? = null

    fun getRepository(context: Context): BerryFlowRepository {
        return repository ?: synchronized(this) {
            repository ?: run {
                val database = AppDatabase.getInstance(context)
                BerryFlowRepository(
                    categoryDao = database.categoryDao(),
                    balanceDao = database.balanceDao(),
                    transactionDao = database.transactionDao(),
                    recurrentEventDao = database.recurrentEventDao()
                )
            }.also { repository = it }
        }
    }
}