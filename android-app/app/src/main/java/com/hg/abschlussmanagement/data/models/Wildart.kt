package com.hg.abschlussmanagement.data.models

data class Wildart(
    val id: Int,
    val name: String,
    val code: String, // RW, DW, etc.
    val meldegruppen: List<String> = emptyList()
)

data class Kategorie(
    val id: Int,
    val name: String,
    val code: String
)

data class Jagdgebiet(
    val id: Int,
    val name: String,
    val code: String
)

data class Erfassung(
    val id: Long? = null,
    val wusNummer: String,
    val wildart: Wildart,
    val kategorie: Kategorie,
    val jagdgebiet: Jagdgebiet,
    val erfasser: String,
    val erfassungsdatum: String,
    val bemerkungen: String,
    val interneNotiz: String = ""
)
