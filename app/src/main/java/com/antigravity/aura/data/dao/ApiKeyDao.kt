package com.antigravity.aura.data.dao

import androidx.room.*
import com.antigravity.aura.data.entity.ApiKeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys")
    fun getAllApiKeys(): Flow<List<ApiKeyEntity>>

    @Query("SELECT * FROM api_keys WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveApiKey(): ApiKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(apiKey: ApiKeyEntity)

    @Update
    suspend fun updateApiKey(apiKey: ApiKeyEntity)

    @Delete
    suspend fun deleteApiKey(apiKey: ApiKeyEntity)

    @Query("UPDATE api_keys SET isActive = 0")
    suspend fun deactivateAllKeys()

    @Transaction
    suspend fun setActiveKey(keyId: Int) {
        deactivateAllKeys()
        activateKey(keyId)
    }

    @Query("UPDATE api_keys SET isActive = 1 WHERE id = :keyId")
    suspend fun activateKey(keyId: Int)
}
