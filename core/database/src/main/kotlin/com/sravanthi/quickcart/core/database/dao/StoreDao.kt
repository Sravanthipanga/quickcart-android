package com.sravanthi.quickcart.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sravanthi.quickcart.core.database.model.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    /** All stores, or only one [type] (e.g. "GROCERY"), in marketplace order. */
    @Query("SELECT * FROM stores WHERE :type IS NULL OR type = :type ORDER BY position")
    fun pagingSource(type: String?): PagingSource<Int, StoreEntity>

    @Query("SELECT * FROM stores WHERE id = :id")
    fun observeById(id: Long): Flow<StoreEntity?>

    @Query("SELECT imageUrl FROM stores WHERE id = :id")
    suspend fun imageUrlOf(id: Long): String?

    @Query("SELECT position FROM stores WHERE id = :id")
    suspend fun positionOf(id: Long): Int?

    @Query("SELECT * FROM stores WHERE name LIKE '%' || :query || '%' OR cuisine LIKE '%' || :query || '%' ORDER BY position")
    suspend fun search(query: String): List<StoreEntity>

    @Query("SELECT id, name FROM stores WHERE id IN (:ids)")
    suspend fun namesFor(ids: List<Long>): List<StoreName>

    @Upsert
    suspend fun upsertAll(stores: List<StoreEntity>)

    @Query("DELETE FROM stores")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM stores")
    suspend fun count(): Int
}

data class StoreName(val id: Long, val name: String)
