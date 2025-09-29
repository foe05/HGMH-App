package com.hg.abschlussmanagement.data.api

import com.hg.abschlussmanagement.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface HGAMApiService {
    
    // Authentication
    @POST("wp-json/hgam/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("wp-json/hgam/v1/auth/logout")
    suspend fun logout(): Response<LogoutResponse>
    
    @GET("wp-json/hgam/v1/auth/status")
    suspend fun getAuthStatus(): Response<AuthStatusResponse>
    
    // Stammdaten
    @GET("wp-json/hgam/v1/wildarten")
    suspend fun getWildarten(): Response<List<Wildart>>
    
    @GET("wp-json/hgam/v1/kategorien")
    suspend fun getKategorien(): Response<List<Kategorie>>
    
    @GET("wp-json/hgam/v1/jagdgebiete")
    suspend fun getJagdgebiete(): Response<List<Jagdgebiet>>
    
    // Erfassungen
    @GET("wp-json/hgam/v1/erfassungen")
    suspend fun getErfassungen(): Response<List<Erfassung>>
    
    @GET("wp-json/hgam/v1/erfassungen/{id}")
    suspend fun getErfassung(@Path("id") id: Long): Response<Erfassung>
    
    @POST("wp-json/hgam/v1/erfassungen")
    suspend fun createErfassung(@Body request: ErfassungRequest): Response<Erfassung>
    
    @PUT("wp-json/hgam/v1/erfassungen/{id}")
    suspend fun updateErfassung(@Path("id") id: Long, @Body request: ErfassungRequest): Response<Erfassung>
    
    @DELETE("wp-json/hgam/v1/erfassungen/{id}")
    suspend fun deleteErfassung(@Path("id") id: Long): Response<Unit>
    
    // Gastmeldung
    @POST("wp-json/hgam/v1/gastmeldung")
    suspend fun submitGastmeldung(@Body request: GastmeldungRequest): Response<GastmeldungResponse>
    
    // OCR
    @POST("wp-json/hgam/v1/ocr/analyze")
    @Multipart
    suspend fun analyzeOCR(@Part image: okhttp3.MultipartBody.Part): Response<OCRResult>
    
    // Notifications
    @POST("wp-json/hgam/v1/notifications/register")
    suspend fun registerNotification(@Body request: NotificationRegisterRequest): Response<Unit>
    
    @GET("wp-json/hgam/v1/notifications/history")
    suspend fun getNotificationHistory(): Response<List<NotificationHistory>>
    
    // Export (nur für Obmänner)
    @GET("wp-json/hgam/v1/export/csv")
    suspend fun exportCSV(@Query("from") from: String?, @Query("to") to: String?): Response<okhttp3.ResponseBody>
    
    @GET("wp-json/hgam/v1/export/pdf")
    suspend fun exportPDF(@Query("from") from: String?, @Query("to") to: String?): Response<okhttp3.ResponseBody>
}

// Request/Response DTOs
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val sessionToken: String,
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val roles: List<String>,
    val jagdgebiete: List<Int>,
    val hegegemeinschaftId: Int
)

data class LogoutResponse(
    val success: Boolean
)

data class AuthStatusResponse(
    val isAuthenticated: Boolean,
    val user: User?
)

data class ErfassungRequest(
    val wusNummer: String,
    val wildartId: Int,
    val kategorieId: Int,
    val jagdgebietId: Int,
    val bemerkungen: String,
    val interneNotiz: String = ""
)

data class GastmeldungRequest(
    val melderName: String,
    val melderEmail: String,
    val melderTelefon: String,
    val wusNummer: String,
    val wildart: String,
    val fundort: String,
    val datum: String,
    val bemerkungen: String,
    val ocrData: OCRResult? = null
)

data class GastmeldungResponse(
    val success: Boolean,
    val message: String
)

data class NotificationRegisterRequest(
    val fcmToken: String,
    val deviceId: String
)

data class NotificationHistory(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val receivedAt: String,
    val isRead: Boolean,
    val deepLink: String?
)
