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
        PlaylistTrackCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun auraDao(): AuraDao
}
