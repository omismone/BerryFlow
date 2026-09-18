package com.omismone.berryflow

import android.app.Application
import com.omismone.berryflow.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BerryFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            val repository = AppContainer.getRepository(this@BerryFlowApplication)
            repository.ensureCategoriesSeeded()
            repository.repairOrphanedCategoryReferences()
            repository.generatePendingRecurrentTransactions()
        }
    }
}