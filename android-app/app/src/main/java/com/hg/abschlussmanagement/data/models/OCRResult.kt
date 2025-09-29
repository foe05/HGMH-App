package com.hg.abschlussmanagement.data.models

data class OCRResult(
    val wusNummer: String? = null,
    val wildart: String? = null,
    val datum: String? = null,
    val jagdgebiet: String? = null,
    val erleger: String? = null,
    val geschlecht: String? = null,
    val altersklasse: String? = null,
    val rawText: String,
    val confidence: Float
)
