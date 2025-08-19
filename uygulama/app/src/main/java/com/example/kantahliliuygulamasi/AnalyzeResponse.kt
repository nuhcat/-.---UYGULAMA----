package com.example.kantahliliuygulamasi.api

data class AnalyzeResponse(
    val analiz: Map<String, TestResult>?,  // Test adı -> sonuç objesi
    val genel_özet: String?                 // Nullable genel özet metni
)

data class TestResult(
    val durum: String?,                     // Test durumu (örn: "aşırı düşük", "normal" vb.)
    val öneri: List<String>?                // Öneriler listesi, nullable
)