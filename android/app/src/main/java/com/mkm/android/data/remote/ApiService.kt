package com.mkm.android.data.remote

import com.mkm.android.model.Document
import com.mkm.android.model.DocumentRequest
import com.mkm.android.model.LoginRequest
import com.mkm.android.model.RegisterRequest
import com.mkm.android.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<TokenResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @GET("documents")
    suspend fun getMyDocuments(): Response<List<Document>>

    @GET("documents/public")
    suspend fun getPublicDocuments(): Response<List<Document>>

    @GET("documents/{id}")
    suspend fun getDocument(@Path("id") id: Long): Response<Document>

    @POST("documents")
    suspend fun createDocument(@Body request: DocumentRequest): Response<Document>

    @PUT("documents/{id}")
    suspend fun updateDocument(@Path("id") id: Long, @Body request: DocumentRequest): Response<Document>

    @DELETE("documents/{id}")
    suspend fun deleteDocument(@Path("id") id: Long): Response<Unit>
}
