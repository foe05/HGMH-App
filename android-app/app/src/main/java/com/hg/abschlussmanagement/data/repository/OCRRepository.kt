package com.hg.abschlussmanagement.data.repository

import com.hg.abschlussmanagement.data.api.HGAMApiService
import com.hg.abschlussmanagement.data.models.OCRResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OCRRepository @Inject constructor(
    private val apiService: HGAMApiService
) {
    
    suspend fun analyzeImage(imageFile: File): OCRResult {
        val requestFile = imageFile.asRequestBody("image/*".toMediaType())
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
        
        val response = apiService.analyzeOCR(imagePart)
        
        return if (response.isSuccessful) {
            response.body() ?: OCRResult(rawText = "", confidence = 0f)
        } else {
            // Fallback to local processing
            OCRResult(rawText = "OCR API nicht verfügbar", confidence = 0f)
        }
    }
}
