package com.juanpcf.caloriestracker.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones de Room. Disciplina (ver AGENTS.md): todo cambio de esquema bumpea la versión de
 * [com.juanpcf.caloriestracker.data.local.CaloriesTrackerDatabase], agrega su Migration acá, la
 * registra en DatabaseModule y suma su test con MigrationTestHelper. NUNCA fallbackToDestructiveMigration.
 */

/**
 * v1 -> v2: soft-delete de entradas del diario.
 * Agrega `is_deleted` a `diary_entry` para no perder borrados ni resucitarlos en el pull desde Firestore.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE diary_entry ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0"
        )
    }
}

/**
 * v2 -> v3: azúcar como macro. Las entradas viejas quedan con sugar_snapshot = 0 (histórico aceptable)
 * y los objetivos con daily_sugar = 0 hasta que el usuario fije su meta.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE diary_entry ADD COLUMN sugar_snapshot REAL NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE user_goals ADD COLUMN daily_sugar INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * v3 -> v4: perfil físico del usuario (altura, peso, fecha de nacimiento, sexo, nivel de actividad)
 * para calcular BMR/TDEE. Tabla nueva, sin backfill.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `user_physical_profile` (
                `id` TEXT NOT NULL,
                `user_id` TEXT NOT NULL,
                `height_cm` REAL NOT NULL,
                `weight_kg` REAL NOT NULL,
                `birth_date` INTEGER NOT NULL,
                `gender` TEXT NOT NULL,
                `activity_level` TEXT NOT NULL,
                `updated_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_user_physical_profile_user_id` " +
                "ON `user_physical_profile` (`user_id`)"
        )
    }
}

/** Todas las migraciones, en orden, para registrar en el builder de Room. */
val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
