package com.sravanthi.quickcart.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sravanthi.quickcart.core.database.dao.MenuItemDao
import com.sravanthi.quickcart.core.database.dao.RemoteKeyDao
import com.sravanthi.quickcart.core.database.dao.StoreDao
import com.sravanthi.quickcart.core.database.model.MenuItemEntity
import com.sravanthi.quickcart.core.database.model.RemoteKeyEntity
import com.sravanthi.quickcart.core.database.model.StoreEntity

@Database(
    entities = [StoreEntity::class, MenuItemEntity::class, RemoteKeyEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class QuickCartDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
