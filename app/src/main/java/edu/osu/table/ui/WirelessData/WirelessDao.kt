package edu.osu.table.ui.WirelessData

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface WirelessDao {

    @Query("SELECT * from wirelessData")
    fun getAll(): List<WirelessData>

    /*@Query("SELECT * from wirelessData WHERE rss_dbm > :rss_dbm")
    fun getRssGreaterThan(rss_dbm: Double): List<WirelessData>

    @Query("SELECT * FROM wirelessData ORDER BY rss_dbm DESC")
    fun getRssSort(): List<WirelessData>*/

    @Insert(onConflict = REPLACE)
    fun insert(wirelessData: WirelessData)


    @Query("DELETE from wirelessData")
    fun deleteAll()

    @Query("SELECT * FROM wirelessData ORDER BY date ASC LIMIT 500")
    abstract fun getAllBattery(): List<WirelessData>
}