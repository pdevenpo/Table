package edu.osu.table.ui.ScanActivity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "scanData")
data class ScanData(@PrimaryKey(autoGenerate = true) var id: Long?,
                    @ColumnInfo(name = "date") var CurDate: Long,
                    @ColumnInfo(name = "mac_address") var MAC_Address: String,
                    @ColumnInfo(name = "security") var Security: String,
                    @ColumnInfo(name = "rss_dbm") var RSSdBm: Int,
                    @ColumnInfo(name = "chan_freq") var ChanFreq: Double




){
    constructor():this(0,0,  "", "",0,0.0)

}