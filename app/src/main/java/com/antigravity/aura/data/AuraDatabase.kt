package com.antigravity.aura.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.antigravity.aura.data.dao.AuraDao
import com.antigravity.aura.data.entity.PlaylistEntity
import com.antigravity.aura.data.entity.PlaylistTrackCrossRef
import com.antigravity.aura.data.entity.TrackEntity

@Database(
    entities = [
        PlaylistEntity::class,
        TrackEntity::class,
        PlaylistTrackCrossRef::class,
        com.antigravity.aura.data.entity.ApiKeyEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun auraDao(): AuraDao
    abstract fun apiKeyDao(): com.antigravity.aura.data.dao.ApiKeyDao
}
