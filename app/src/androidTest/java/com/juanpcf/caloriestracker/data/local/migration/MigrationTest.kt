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

    /**
     * v2 -> v3: azúcar. La entrada y los objetivos preexistentes sobreviven con sugar_snapshot = 0
     * y daily_sugar = 0 por defecto.
     */
    @Test
    fun migrate2To3_agregaAzucarConDefaultCero() {
        helper.createDatabase(dbName, 2).apply {
            execSQL(
                """
                INSERT INTO diary_entry
                (id, user_id, food_id, food_name, calories_snapshot, protein_snapshot,
                 carbs_snapshot, fat_snapshot, servings, meal_type, date, created_at, synced_at, is_deleted)
                VALUES
                ('e1','u1','f1','Pollo',100.0,20.0,0.0,5.0,1.0,'LUNCH',20000,1000000,NULL,0)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO user_goals
                (id, user_id, daily_calories, daily_protein, daily_carbs, daily_fat, updated_at)
                VALUES
                ('g1','u1',2000,150,200,60,1000000)
                """.trimIndent()
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 3, true, MIGRATION_2_3)

        db.query("SELECT sugar_snapshot FROM diary_entry WHERE id = 'e1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0.0, cursor.getDouble(cursor.getColumnIndexOrThrow("sugar_snapshot")), 0.0001)
        }
        db.query("SELECT daily_sugar FROM user_goals WHERE id = 'g1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("daily_sugar")))
        }
        db.close()
    }

    /**
     * v3 -> v4: crea la tabla user_physical_profile. Validamos que el esquema migrado coincide con el
     * exportado y que la tabla nueva acepta inserts.
     */
    @Test
    fun migrate3To4_creaTablaPerfilFisico() {
        helper.createDatabase(dbName, 3).close()

        val db = helper.runMigrationsAndValidate(dbName, 4, true, MIGRATION_3_4)

        db.execSQL(
            """
            INSERT INTO user_physical_profile
            (id, user_id, height_cm, weight_kg, birth_date, gender, activity_level, updated_at)
            VALUES
            ('p1','u1',180.0,80.0,9659,'MALE','MODERATE',1000000)
            """.trimIndent()
        )
        db.query("SELECT gender, activity_level FROM user_physical_profile WHERE id = 'p1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("MALE", cursor.getString(cursor.getColumnIndexOrThrow("gender")))
            assertEquals("MODERATE", cursor.getString(cursor.getColumnIndexOrThrow("activity_level")))
        }
        db.close()
    }
}
