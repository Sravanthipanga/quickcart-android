package com.sravanthi.quickcart.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.sravanthi.quickcart.core.database.dao.CartDao
import com.sravanthi.quickcart.core.database.dao.MenuItemDao
import com.sravanthi.quickcart.core.database.dao.RemoteKeyDao
import com.sravanthi.quickcart.core.database.dao.StoreDao
import com.sravanthi.quickcart.core.database.model.CartItemEntity
import com.sravanthi.quickcart.core.database.model.MenuItemEntity
import com.sravanthi.quickcart.core.database.model.RemoteKeyEntity
import com.sravanthi.quickcart.core.database.model.StoreEntity

@Database(
    entities = [StoreEntity::class, MenuItemEntity::class, RemoteKeyEntity::class, CartItemEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        // v2: the cart_items table.
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class QuickCartDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun cartDao(): CartDao
}
