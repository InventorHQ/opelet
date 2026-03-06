package io.inventor.opelet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrackedApp::class], version = 2, exportSchema = false)
abstract class OpeletDatabase : RoomDatabase() {

    abstract fun trackedAppDao(): TrackedAppDao

    companion object {
        @Volatile
        private var instance: OpeletDatabase? = null

        fun get(context: Context): OpeletDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OpeletDatabase::class.java,
                    "opelet.db"
                ).fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
