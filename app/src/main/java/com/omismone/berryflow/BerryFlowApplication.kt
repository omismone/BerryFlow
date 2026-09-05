package com.omismone.berryflow

import android.app.Application
import com.omismone.berryflow.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Runs one-time startup work (seeding default/base categories) as soon as
// the app process starts, regardless of which screen is shown first. This
// used to only happen when opening the Categories screen, which meant other
// screens could see an empty category list if opened first.
class BerryFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            AppContainer.getRepository(this@BerryFlowApplication).ensureCategoriesSeeded()
        }
    }
}