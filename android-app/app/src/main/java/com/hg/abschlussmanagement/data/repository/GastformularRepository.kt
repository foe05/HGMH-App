package com.hg.abschlussmanagement.data.repository

import com.hg.abschlussmanagement.data.api.GastmeldungRequest
import com.hg.abschlussmanagement.data.api.HGAMApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GastformularRepository @Inject constructor(
    private val apiService: HGAMApiService
) {
    
    suspend fun submitGastmeldung(
        melderName: String,
        melderEmail: String,
        melderTelefon: String,
        wusNummer: String,
        wildart: String,
        fundort: String,
        datum: String,
        bemerkungen: String
    ) {
        val request = GastmeldungRequest(
            melderName = melderName,
            melderEmail = melderEmail,
            melderTelefon = melderTelefon,
            wusNummer = wusNummer,
            wildart = wildart,
            fundort = fundort,
            datum = datum,
            bemerkungen = bemerkungen
        )
        
        val response = apiService.submitGastmeldung(request)
        
        if (!response.isSuccessful) {
            throw Exception("Fehler beim Absenden der Gastmeldung: ${response.message()}")
        }
    }
}
