package com.sravanthi.quickcart.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sravanthi.quickcart.core.database.model.OrderEntity
import com.sravanthi.quickcart.core.database.model.OrderLineEntity
import com.sravanthi.quickcart.core.database.model.OrderWithLines
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    /** Newest first. */
    @Transaction
    @Query("SELECT * FROM orders ORDER BY placedAtMillis DESC")
    fun observeAll(): Flow<List<OrderWithLines>>

    @Transaction
    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    fun observe(orderId: String): Flow<OrderWithLines?>

    @Transaction
    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    suspend fun get(orderId: String): OrderWithLines?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<OrderLineEntity>)

    @Transaction
    suspend fun insert(order: OrderEntity, lines: List<OrderLineEntity>) {
        insertOrder(order)
        insertLines(lines)
    }

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateStatus(orderId: String, status: String)
}
