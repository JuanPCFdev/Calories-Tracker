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

/** Todas las migraciones, en orden, para registrar en el builder de Room. */
val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
