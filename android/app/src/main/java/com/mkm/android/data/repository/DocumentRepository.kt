package com.mkm.android.data.repository

import androidx.lifecycle.LiveData
import com.mkm.android.data.local.AppDatabase
import com.mkm.android.data.local.DocumentEntity
import com.mkm.android.data.remote.ApiService
import com.mkm.android.model.Document
import com.mkm.android.model.DocumentRequest

class DocumentRepository(
    private val api: ApiService,
    private val db: AppDatabase
) {
    private val dao = db.documentDao()

    fun observeDocuments(): LiveData<List<DocumentEntity>> = dao.observeAll()

    suspend fun refresh(): Result<Unit> = runCatching {
        val docs = api.getMyDocuments().let {
            check(it.isSuccessful) { "Network error: ${it.code()}" }
            it.body()!!
        }
        dao.upsertAll(docs.map { it.toEntity() })
    }

    suspend fun getDocument(id: Long): Result<Document> = runCatching {
        val resp = api.getDocument(id)
        check(resp.isSuccessful) { "Network error: ${resp.code()}" }
        resp.body()!!
    }

    suspend fun createDocument(request: DocumentRequest): Result<Document> = runCatching {
        val resp = api.createDocument(request)
        check(resp.isSuccessful) { "Network error: ${resp.code()}" }
        resp.body()!!
    }

    suspend fun deleteDocument(id: Long): Result<Unit> = runCatching {
        val resp = api.deleteDocument(id)
        check(resp.isSuccessful) { "Network error: ${resp.code()}" }
        dao.deleteById(id)
    }

    private fun Document.toEntity() = DocumentEntity(
        id = id, title = title, content = content,
        isPublic = isPublic, ownerUsername = ownerUsername, updatedAt = updatedAt
    )
}
