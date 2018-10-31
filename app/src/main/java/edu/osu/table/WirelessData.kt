package edu.osu.table

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "wirelessData")
data class WirelessData(@PrimaryKey(autoGenerate = true) var id: Long?,
                       @ColumnInfo(name = "date") var CurDate: Long,
                       //@ColumnInfo(name = "ssid") var SSID: String,
                       @ColumnInfo(name = "mac_address") var MAC_Address: String,
                       //@ColumnInfo(name = "security") var Security: String,
                       //@ColumnInfo(name = "rss_dbm") var RSSdBm: Int,
                       //@ColumnInfo(name = "chan_freq") var ChanFreq: Double,
                       //@ColumnInfo(name = "thoughput_Mbps") var ThroughputMpbs: Double,
                       //@ColumnInfo(name = "channel_bw") var ChBW: Double,
                       @ColumnInfo(name = "battery_percentage") var BatteryPerc: Double



){
    constructor():this(null,0,  "", -1.0)

}