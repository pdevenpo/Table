package edu.osu.table.ui.WirelessScan

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context


@Database(entities = arrayOf(Wireless2Data::class), version = 1)
abstract class Wireless2Database : RoomDatabase() {

    abstract fun wireless2DataDao(): Wireless2Dao

    companion object {
        private var INSTANCE: Wireless2Database? = null

        fun getInstance(context: Context): Wireless2Database? {
            if (INSTANCE == null) {
                synchronized(Wireless2Database::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        Wireless2Database::class.java, "wireless2db.db").allowMainThreadQueries()
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