package com.juanpcf.caloriestracker.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.juanpcf.caloriestracker.data.local.CaloriesTrackerDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CaloriesTrackerDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    /**
     * v1 -> v2: la entrada preexistente sobrevive y arranca con is_deleted = 0. Si esto falla,
     * los usuarios en upgrade crashearían al abrir la app.
     */
    @Test
    fun migrate1To2_preservaDatosYDefaultIsDeleted() {
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                """
                INSERT INTO diary_entry
                (id, user_id, food_id, food_name, calories_snapshot, protein_snapshot,
                 carbs_snapshot, fat_snapshot, servings, meal_type, date, created_at, synced_at)
                VALUES
                ('e1','u1','f1','Pollo',100.0,20.0,0.0,5.0,1.0,'LUNCH',20000,1000000,NULL)
                """.trimIndent()
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)

        db.query("SELECT food_name, is_deleted FROM diary_entry WHERE id = 'e1'").use { cursor ->
            assertTrue("La entrada preexistente debe sobrevivir la migración", cursor.moveToFirst())
            assertEquals("Pollo", cursor.getString(cursor.getColumnIndexOrThrow("food_name")))
            assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")))
        }
        db.close()
    }
}
