package com.hg.abschlussmanagement.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "user_session")
data class UserSession(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val sessionToken: String,
    val roles: List<String>,
    val jagdgebiete: List<Int>,
    val rememberLogin: Boolean,
    val lastLogin: LocalDateTime
)
