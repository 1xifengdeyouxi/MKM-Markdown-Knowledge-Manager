package com.mkm.android.ui.document

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mkm.android.R
import com.mkm.android.data.local.AppDatabase
import com.mkm.android.data.remote.RetrofitClient
import com.mkm.android.data.repository.DocumentRepository
import com.mkm.android.databinding.ActivityDocumentDetailBinding
import com.mkm.android.model.DocumentRequest
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import kotlinx.coroutines.launch

class DocumentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocumentDetailBinding
    private lateinit var markwon: Markwon
    private var docId: Long = -1L
    private var isEditMode = false
    private var currentContent = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        markwon = Markwon.builder(this)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(this))
            .build()

        docId = intent.getLongExtra("docId", -1L)

        if (docId == -1L) {
            enterEditMode()
        } else {
            loadDocument()
        }

        binding.btnSave.setOnClickListener { saveDocument() }
    }

    private fun loadDocument() {
        lifecycleScope.launch {
            setLoading(true)
            val repo = DocumentRepository(RetrofitClient.api, AppDatabase.get(this@DocumentDetailActivity))
            repo.getDocument(docId)
                .onSuccess { doc ->
                    binding.toolbar.title = doc.title
                    currentContent = doc.content
                    markwon.setMarkdown(binding.tvMarkdown, doc.content)
                }
                .onFailure { showError(it.message ?: "Failed to load") }
            setLoading(false)
        }
    }

    private fun saveDocument() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString()
        if (title.isEmpty()) { showError("Title is required"); return }
        lifecycleScope.launch {
            setLoading(true)
            val repo = DocumentRepository(RetrofitClient.api, AppDatabase.get(this@DocumentDetailActivity))
            val request = DocumentRequest(title = title, content = content)
            val result = if (docId == -1L) repo.createDocument(request)
                         else RetrofitClient.api.updateDocument(docId, request)
                             .let { if (it.isSuccessful) Result.success(it.body()!!) else Result.failure(Exception("${it.code()}")) }
            result.onSuccess { finish() }.onFailure { showError(it.message ?: "Save failed") }
            setLoading(false)
        }
    }

    private fun enterEditMode() {
        isEditMode = true
        binding.tvMarkdown.visibility = View.GONE
        binding.editGroup.visibility = View.VISIBLE
        binding.etContent.setText(currentContent)
        invalidateOptionsMenu()
    }

    private fun exitEditMode() {
        isEditMode = false
        binding.tvMarkdown.visibility = View.VISIBLE
        binding.editGroup.visibility = View.GONE
        markwon.setMarkdown(binding.tvMarkdown, currentContent)
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_document_detail, menu)
        menu.findItem(R.id.action_edit)?.isVisible = !isEditMode && docId != -1L
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { onBackPressedDispatcher.onBackPressed(); true }
        R.id.action_edit -> { enterEditMode(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(msg: String) = Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
}
