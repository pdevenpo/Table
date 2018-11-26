package edu.osu.table.ui.WirelessScan

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface Wireless2Dao {

    @Query("SELECT * from wireless2Data")
    fun getAll(): List<Wireless2Data>

    /*@Query("SELECT * from wirelessData WHERE rss_dbm > :rss_dbm")
    fun getRssGreaterThan(rss_dbm: Double): List<Wireless2Data>

    @Query("SELECT * FROM wirelessData ORDER BY rss_dbm DESC")
    fun getRssSort(): List<Wireless2Data>*/

    @Insert(onConflict = REPLACE)
    fun insert(wireless2Data: Wireless2Data)

    @Query("DELETE from wireless2Data")
    fun deleteAll()

    @Query("SELECT * FROM wireless2Data ORDER BY date DESC LIMIT :value")
    abstract fun getAllBattery(value: Int): List<Wireless2Data>

    @Query("SELECT * FROM  wireless2Data WHERE ssid IS :name ORDER BY date DESC LIMIT :value")
    abstract fun getPerSSIDEntries(name: String, value: Int): List<Wireless2Data>

    @Query("SELECT date FROM wireless2Data ORDER BY date DESC LIMIT :value")
    abstract fun getDates(value: Int): List<Long>

    @Query("SELECT * from wireless2Data WHERE date IS :date ORDER BY rss_dbm DESC")
    abstract  fun getSsidAndRss(date: Double): List<Wireless2Data>

    @Query("SELECT * from wireless2Data WHERE date IS :date ORDER BY rss_dbm DESC")
    abstract  fun getHighRssWireless(date: Long): List<Wireless2Data>
}