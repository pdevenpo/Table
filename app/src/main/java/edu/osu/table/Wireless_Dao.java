package edu.osu.table;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

@Dao
public interface Wireless_Dao
{
    @Insert
    public void addWireless_Data(Wireless_Data wireless_data);


}
