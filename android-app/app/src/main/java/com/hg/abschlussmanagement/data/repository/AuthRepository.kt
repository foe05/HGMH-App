package com.hg.abschlussmanagement.data.repository

import com.hg.abschlussmanagement.data.api.HGAMApiService
import com.hg.abschlussmanagement.data.models.UserSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: HGAMApiService,
    private val sessionManager: SessionManager
) {
    
    suspend fun login(username: String, password: String): Result<UserSession> {
        return try {
            val response = apiService.login(
                com.hg.abschlussmanagement.data.api.LoginRequest(username, password)
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val loginResponse = response.body()!!
                val session = UserSession(
                    username = loginResponse.user.username,
                    sessionToken = loginResponse.sessionToken,
                    roles = loginResponse.user.roles,
                    jagdgebiete = loginResponse.user.jagdgebiete,
                    rememberLogin = true, // TODO: Implement remember logic
                    lastLogin = java.time.LocalDateTime.now()
                )
                sessionManager.saveSession(session)
                Result.success(session)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            val response = apiService.logout()
            sessionManager.clearSession()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Logout failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            sessionManager.clearSession() // Clear local session even if API fails
            Result.failure(e)
        }
    }
    
    fun getCurrentSession(): Flow<UserSession?> = sessionManager.getCurrentSession()
    
    suspend fun isAuthenticated(): Boolean {
        return sessionManager.getCurrentSessionValue() != null
    }
}
