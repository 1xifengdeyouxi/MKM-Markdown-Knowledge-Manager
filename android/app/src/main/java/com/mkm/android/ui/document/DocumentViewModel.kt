package com.mkm.android.ui.document

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkm.android.data.local.DocumentEntity
import com.mkm.android.data.remote.RetrofitClient
import com.mkm.android.data.repository.DocumentRepository
import com.mkm.android.data.local.AppDatabase
import kotlinx.coroutines.launch

class DocumentViewModel(private val repository: DocumentRepository) : ViewModel() {
    val documents: LiveData<List<DocumentEntity>> = repository.observeDocuments()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            repository.refresh().onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
}
