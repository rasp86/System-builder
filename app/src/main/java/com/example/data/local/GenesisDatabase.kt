package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [OsSnapshotEntity::class], version = 1, exportSchema = false)
abstract class GenesisDatabase : RoomDatabase() {

    abstract fun osSnapshotDao(): OsSnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: GenesisDatabase? = null

        fun getInstance(context: Context): GenesisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GenesisDatabase::class.java,
                    "genesis_os.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
