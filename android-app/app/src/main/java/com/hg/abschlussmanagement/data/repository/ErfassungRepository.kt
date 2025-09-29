package com.hg.abschlussmanagement.data.repository

import com.hg.abschlussmanagement.data.api.ErfassungRequest
import com.hg.abschlussmanagement.data.api.HGAMApiService
import com.hg.abschlussmanagement.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErfassungRepository @Inject constructor(
    private val apiService: HGAMApiService
) {
    
    suspend fun getWildarten(): List<Wildart> {
        val response = apiService.getWildarten()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            // Fallback data for development
            listOf(
                Wildart(1, "Rotwild", "RW", listOf("Gruppe_A", "Gruppe_B")),
                Wildart(2, "Damwild", "DW", listOf("Gruppe_A", "Gruppe_B"))
            )
        }
    }
    
    suspend fun getKategorien(): List<Kategorie> {
        val response = apiService.getKategorien()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            // Fallback data for development
            listOf(
                Kategorie(1, "Wildkalb", "W0"),
                Kategorie(2, "Schmaltier", "W1"),
                Kategorie(3, "Alttier", "W2"),
                Kategorie(4, "Hirschkalb", "M0"),
                Kategorie(5, "Schmalspießer", "M1"),
                Kategorie(6, "Junger Hirsch", "M2"),
                Kategorie(7, "Mittelalter Hirsch", "M3"),
                Kategorie(8, "Alter Hirsch", "M4")
            )
        }
    }
    
    suspend fun getJagdgebiete(): List<Jagdgebiet> {
        val response = apiService.getJagdgebiete()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            // Fallback data for development
            listOf(
                Jagdgebiet(1, "Jagdgebiet Nord", "JG_N"),
                Jagdgebiet(2, "Jagdgebiet Süd", "JG_S"),
                Jagdgebiet(3, "Jagdgebiet Ost", "JG_O"),
                Jagdgebiet(4, "Jagdgebiet West", "JG_W")
            )
        }
    }
    
    suspend fun getErfassungen(): List<Erfassung> {
        val response = apiService.getErfassungen()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            // Fallback data for development
            listOf(
                Erfassung(
                    id = 1,
                    wusNummer = "1234567",
                    wildart = Wildart(1, "Rotwild", "RW"),
                    kategorie = Kategorie(2, "Schmaltier", "W1"),
                    jagdgebiet = Jagdgebiet(1, "Jagdgebiet Nord", "JG_N"),
                    erfasser = "Max Mustermann",
                    erfassungsdatum = "2025-01-15",
                    bemerkungen = "Testerfassung",
                    interneNotiz = "Interne Notiz"
                ),
                Erfassung(
                    id = 2,
                    wusNummer = "2345678",
                    wildart = Wildart(2, "Damwild", "DW"),
                    kategorie = Kategorie(5, "Schmalspießer", "M1"),
                    jagdgebiet = Jagdgebiet(2, "Jagdgebiet Süd", "JG_S"),
                    erfasser = "Anna Schmidt",
                    erfassungsdatum = "2025-01-14",
                    bemerkungen = "Weitere Testerfassung"
                )
            )
        }
    }
    
    suspend fun getErfassung(id: Long): Erfassung? {
        val response = apiService.getErfassung(id)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
    
    suspend fun createErfassung(request: ErfassungRequest): Erfassung {
        val response = apiService.createErfassung(request)
        return if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to create erfassung: ${response.message()}")
        }
    }
    
    suspend fun updateErfassung(id: Long, request: ErfassungRequest): Erfassung {
        val response = apiService.updateErfassung(id, request)
        return if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to update erfassung: ${response.message()}")
        }
    }
    
    suspend fun deleteErfassung(id: Long): Boolean {
        val response = apiService.deleteErfassung(id)
        return response.isSuccessful
    }
}
