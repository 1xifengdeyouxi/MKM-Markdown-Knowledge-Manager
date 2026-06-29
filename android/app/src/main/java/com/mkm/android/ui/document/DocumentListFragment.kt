package com.mkm.android.ui.document

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mkm.android.R
import com.mkm.android.data.local.AppDatabase
import com.mkm.android.data.local.DocumentEntity
import com.mkm.android.data.remote.RetrofitClient
import com.mkm.android.data.repository.DocumentRepository
import com.mkm.android.databinding.FragmentDocumentListBinding

class DocumentListFragment : Fragment() {

    private var _binding: FragmentDocumentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<DocumentViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.get(requireContext())
                val repo = DocumentRepository(RetrofitClient.api, db)
                @Suppress("UNCHECKED_CAST")
                return DocumentViewModel(repo) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDocumentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = DocumentAdapter { doc ->
            findNavController().navigate(R.id.action_documentList_to_detail, bundleOf("docId" to doc.id))
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.documents.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.loading.observe(viewLifecycleOwner) { binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE }

        binding.fabCreate.setOnClickListener {
            findNavController().navigate(R.id.action_documentList_to_detail, bundleOf("docId" to -1L))
        }

        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class DocumentAdapter(
    private val onClick: (DocumentEntity) -> Unit
) : ListAdapter<DocumentEntity, DocumentAdapter.VH>(DIFF) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val updated: TextView = view.findViewById(R.id.tvUpdated)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val doc = getItem(position)
        holder.title.text = doc.title
        holder.updated.text = doc.updatedAt
        holder.itemView.setOnClickListener { onClick(doc) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<DocumentEntity>() {
            override fun areItemsTheSame(a: DocumentEntity, b: DocumentEntity) = a.id == b.id
            override fun areContentsTheSame(a: DocumentEntity, b: DocumentEntity) = a == b
        }
    }
}
