package com.sravanthi.quickcart.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sravanthi.quickcart.core.database.model.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {

    @Query("SELECT * FROM menu_items WHERE storeId = :storeId ORDER BY position")
    fun observeForStore(storeId: Long): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE name LIKE '%' || :query || '%' OR section LIKE '%' || :query || '%' LIMIT :limit")
    suspend fun search(query: String, limit: Int): List<MenuItemEntity>

    @Query("DELETE FROM menu_items WHERE storeId = :storeId")
    suspend fun deleteForStore(storeId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MenuItemEntity>)

    /** Replaces a store's whole menu at once, so removed dishes disappear and the menu is never half-updated. */
    @Transaction
    suspend fun replaceForStore(storeId: Long, items: List<MenuItemEntity>) {
        deleteForStore(storeId)
        insertAll(items)
    }
}
