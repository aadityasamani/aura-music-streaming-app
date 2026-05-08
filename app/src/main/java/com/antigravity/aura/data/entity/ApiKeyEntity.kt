package com.antigravity.aura.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val key: String,
    val label: String,
    val isActive: Boolean = false,
    val isQuotaExceeded: Boolean = false
)
