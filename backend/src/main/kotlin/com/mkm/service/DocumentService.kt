package com.mkm.service

import com.mkm.dto.DocumentDto
import com.mkm.dto.DocumentRequest
import com.mkm.model.Document
import com.mkm.repository.DocumentRepository
import com.mkm.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val userRepository: UserRepository
) {
    fun getMyDocuments(username: String): List<DocumentDto> {
        val user = findUser(username)
        return documentRepository.findAllByOwnerOrderByUpdatedAtDesc(user).map { DocumentDto.from(it) }
    }

    fun getPublicDocuments(): List<DocumentDto> =
        documentRepository.findAllByIsPublicTrueOrderByUpdatedAtDesc().map { DocumentDto.from(it) }

    fun getDocument(id: Long, username: String): DocumentDto {
        val doc = findDoc(id)
        check(doc.isPublic || doc.owner.username == username) { "Access denied" }
        return DocumentDto.from(doc)
    }

    fun createDocument(request: DocumentRequest, username: String): DocumentDto {
        val user = findUser(username)
        val doc = documentRepository.save(
            Document(title = request.title, content = request.content, isPublic = request.isPublic, owner = user)
        )
        return DocumentDto.from(doc)
    }

    fun updateDocument(id: Long, request: DocumentRequest, username: String): DocumentDto {
        val doc = findDoc(id)
        check(doc.owner.username == username) { "Access denied" }
        doc.title = request.title
        doc.content = request.content
        doc.isPublic = request.isPublic
        return DocumentDto.from(documentRepository.save(doc))
    }

    fun deleteDocument(id: Long, username: String) {
        val doc = findDoc(id)
        check(doc.owner.username == username) { "Access denied" }
        documentRepository.delete(doc)
    }

    private fun findUser(username: String) =
        userRepository.findByUsername(username).orElseThrow { UsernameNotFoundException(username) }

    private fun findDoc(id: Long) =
        documentRepository.findByIdOrNull(id) ?: throw NoSuchElementException("Document $id not found")
}
