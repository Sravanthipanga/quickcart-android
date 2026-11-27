package com.sravanthi.quickcart.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Where paging should continue for one product list ([label], e.g. "catalog").
 * [nextSkip] is null once the last page has been loaded.
 */
@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val label: String,
    val nextSkip: Int?,
    val updatedAtMillis: Long,
)
