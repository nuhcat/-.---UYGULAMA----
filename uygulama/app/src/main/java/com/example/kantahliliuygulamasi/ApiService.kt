package com.example.kantahliliuygulamasi.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class AnalyzeRequest(val results: Map<String, Double>)

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/analyze")
    suspend fun analyzeResults(@Body request: AnalyzeRequest): AnalyzeResponse
}
