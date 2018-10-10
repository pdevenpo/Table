package edu.osu.table;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Wireless_Data.class},version = 1)
public abstract class Wireless_Database extends RoomDatabase
{
    public abstract Wireless_Dao wirelessDao();

}
