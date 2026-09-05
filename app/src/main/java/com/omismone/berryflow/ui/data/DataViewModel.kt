package com.omismone.berryflow.ui.data

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omismone.berryflow.data.BerryFlowRepository
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class DataViewModel(
    private val repository: BerryFlowRepository
) : ViewModel() {

    fun exportData(contentResolver: ContentResolver, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportDataAsJson()
                contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun importData(contentResolver: ContentResolver, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val json = contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).readText()
                } ?: throw IllegalStateException("Could not read file")
                repository.importDataFromJson(json)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun eraseData(onResult: () -> Unit) {
        viewModelScope.launch {
            repository.eraseAllData()
            onResult()
        }
    }
}

class DataViewModelFactory(
    private val repository: BerryFlowRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DataViewModel(repository) as T
    }
}