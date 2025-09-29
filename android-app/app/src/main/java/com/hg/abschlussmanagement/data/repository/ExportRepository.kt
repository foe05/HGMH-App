package com.hg.abschlussmanagement.data.repository

import com.hg.abschlussmanagement.data.api.HGAMApiService
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepository @Inject constructor(
    private val apiService: HGAMApiService
) {
    
    suspend fun exportData(
        fromDate: String,
        toDate: String,
        format: String,
        onlyOwnData: Boolean,
        includeInternalNotes: Boolean,
        sendEmail: Boolean
    ): ResponseBody {
        val response = when (format.lowercase()) {
            "csv" -> apiService.exportCSV(fromDate, toDate)
            "pdf" -> apiService.exportPDF(fromDate, toDate)
            else -> throw IllegalArgumentException("Unsupported format: $format")
        }
        
        if (!response.isSuccessful) {
            throw Exception("Export failed: ${response.message()}")
        }
        
        return response.body() ?: throw Exception("Empty response body")
    }
}
