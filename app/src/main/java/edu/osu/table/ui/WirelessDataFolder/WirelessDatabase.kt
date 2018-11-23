package edu.osu.table.ui.WirelessDataFolder

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(WirelessData::class), version = 1)
abstract class WirelessDatabase : RoomDatabase() {

    abstract fun wirelessDataDao(): WirelessDao

    companion object {
        private var INSTANCE: WirelessDatabase? = null

        fun getInstance(context: Context): WirelessDatabase? {
            if (INSTANCE == null) {
                synchronized(WirelessDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WirelessDatabase::class.java, "wirelessdb.db").allowMainThreadQueries()
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