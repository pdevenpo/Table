package edu.osu.table

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface ScanDao {

    @Query("SELECT * from scanData")
    fun getAll(): List<ScanData>

    /*@Query("SELECT * from wirelessData WHERE rss_dbm > :rss_dbm")
    fun getRssGreaterThan(rss_dbm: Double): List<WirelessData>

    @Query("SELECT * FROM wirelessData ORDER BY rss_dbm DESC")
    fun getRssSort(): List<WirelessData>*/

    @Insert(onConflict = REPLACE)
    fun insert(scanData: ScanData)


//    @Query("DELETE from scanData")
//    fun deleteAll()

//    @Query("SELECT * FROM scanData ORDER BY date ASC LIMIT 500")
//    abstract fun getAllBattery(): List<ScanData>
}