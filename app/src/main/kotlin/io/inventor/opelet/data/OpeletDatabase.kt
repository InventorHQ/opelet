package io.inventor.opelet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TrackedApp::class], version = 3, exportSchema = false)
abstract class OpeletDatabase : RoomDatabase() {

    abstract fun trackedAppDao(): TrackedAppDao

    companion object {
        @Volatile
        private var instance: OpeletDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tracked_apps ADD COLUMN description TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tracked_apps ADD COLUMN packageName TEXT DEFAULT NULL")
            }
        }

        fun get(context: Context): OpeletDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OpeletDatabase::class.java,
                    "opelet.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { instance = it }
            }
    }
}
