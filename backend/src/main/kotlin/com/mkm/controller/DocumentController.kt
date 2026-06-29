package com.mkm.controller

import com.mkm.dto.DocumentDto
import com.mkm.dto.DocumentRequest
import com.mkm.service.DocumentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val documentService: DocumentService
) {
    @GetMapping
    fun myDocuments(principal: Principal): List<DocumentDto> = documentService.getMyDocuments(principal.name)

    @GetMapping("/public")
    fun publicDocuments(): List<DocumentDto> = documentService.getPublicDocuments()

    @GetMapping("/{id}")
    fun getDocument(@PathVariable id: Long, principal: Principal): DocumentDto =
        documentService.getDocument(id, principal.name)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDocument(
        @Valid @RequestBody request: DocumentRequest,
        principal: Principal
    ): DocumentDto = documentService.createDocument(request, principal.name)

    @PutMapping("/{id}")
    fun updateDocument(
        @PathVariable id: Long,
        @Valid @RequestBody request: DocumentRequest,
        principal: Principal
    ): DocumentDto = documentService.updateDocument(id, request, principal.name)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDocument(@PathVariable id: Long, principal: Principal) {
        documentService.deleteDocument(id, principal.name)
    }
}
