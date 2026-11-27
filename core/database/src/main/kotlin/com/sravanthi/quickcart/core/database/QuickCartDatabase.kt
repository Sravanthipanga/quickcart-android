package com.sravanthi.quickcart.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sravanthi.quickcart.core.database.dao.ProductDao
import com.sravanthi.quickcart.core.database.dao.RemoteKeyDao
import com.sravanthi.quickcart.core.database.model.ProductEntity
import com.sravanthi.quickcart.core.database.model.RemoteKeyEntity

@Database(
    entities = [ProductEntity::class, RemoteKeyEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(QuickCartConverters::class)
abstract class QuickCartDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
