package com.mkm.repository

import com.mkm.model.Document
import com.mkm.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface DocumentRepository : JpaRepository<Document, Long> {
    fun findAllByOwnerOrderByUpdatedAtDesc(owner: User): List<Document>
    fun findAllByIsPublicTrueOrderByUpdatedAtDesc(): List<Document>
}
