package edu.osu.table.ui.ScanActivity

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(ScanData::class), version = 1)
abstract class ScanDatabase : RoomDatabase() {

    abstract fun scanDataDao(): ScanDao

    companion object {
        private var INSTANCE: ScanDatabase? = null

        fun getInstance(context: Context): ScanDatabase? {
            if (INSTANCE == null) {
                synchronized(ScanDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ScanDatabase::class.java, "scandb.db").allowMainThreadQueries()
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}