package com.sravanthi.quickcart.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Upgrading the app must keep the user's cached stores and add the new cart table. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), QuickCartDatabase::class.java)

    @Test
    fun `v1 to v2 keeps cached stores and adds an empty cart`() {
        helper.createDatabase(DB, 1).use { db ->
            db.execSQL(
                """INSERT INTO stores (id, name, type, cuisine, rating, ratingCount, deliveryMinMinutes, deliveryMaxMinutes,
                   deliveryFeeCents, minimumOrderCents, opensAt, closesAt, distanceMiles, imageUrl, position)
                   VALUES (1, 'Bella Napoli Pizzeria', 'RESTAURANT', 'Italian', 4.7, 1284, 25, 35, 199, 1200,
                   '11:00', '23:00', 1.2, '', 0)""",
            )
        }

        // Runs the auto-migration and checks the result against the exported v2 schema.
        helper.runMigrationsAndValidate(DB, 2, true).use { db ->
            db.query("SELECT name FROM stores").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Bella Napoli Pizzeria", cursor.getString(0))
            }
            db.query("SELECT COUNT(*) FROM cart_items").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    private companion object {
        const val DB = "migration-test"
    }
}
