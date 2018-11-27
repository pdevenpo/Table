package edu.osu.table.ui.RecommendationsActivity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import edu.osu.table.MainActivity
import edu.osu.table.R
//import edu.osu.table.MainActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import edu.osu.table.ui.ScanActivity.ScanDatabase
import edu.osu.table.ui.WirelessDataFolder.WirelessDatabase
import edu.osu.table.ui.WirelessScan.Wireless2Database
import kotlinx.android.synthetic.main.recommendation_activity.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class RecommendationActivity : AppCompatActivity() {

    private var mDb_wireless: WirelessDatabase? = null
    private var mDb_avail_wireless: Wireless2Database? = null

    private lateinit var mRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommendation_activity)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        recycle_rec.layoutManager = LinearLayoutManager(this)

        battery_usage()


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_onlyback, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_back -> {
            // User chose the "Back" item, return to main activity...
            val myIntent = Intent(baseContext, MainActivity::class.java)
            startActivity(myIntent)
            true

        }

        //TODO make a navigation drawer open as result of click
        /*R.id.action_menu -> {
            true
        }*/

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    fun battery_usage ()
    {
        val delta_battery = 0.10 // Battery Drop 10% Per Hour
        mDb_wireless = WirelessDatabase.getInstance(this.applicationContext)
        mDb_avail_wireless = Wireless2Database.getInstance(this.applicationContext)

        var temp_float:Float
        var temp_long:Long
        var date_temp:Date
        var temp_int:Int = -1000
        var temp_double:Double = 1000.0
        var size_db = 0

        var wirelessData = mDb_wireless?.wirelessDataDao()?.getAllBattery(96)
        var wireless2Data = mDb_avail_wireless?.wireless2DataDao()?.getSsidAndRss(0.0)
        //var datelist = mDb_wireless?.wirelessDataDao()?.getDates(96)

        val format_date = SimpleDateFormat("E 'at' h:mm a z")
        format_date.timeZone = TimeZone.getDefault()

        var batt_delta: MutableList<Float> = mutableListOf()
        var time_delta: MutableList<Long> = mutableListOf()
        var date_string: MutableList<String> = mutableListOf()
        var final_string: MutableList<String> = mutableListOf()
        var batt_delta_filter: MutableList<Float> = mutableListOf()

        if (wirelessData != null) {
            size_db = wirelessData?.size


            if (size_db >= 2) {
                for (i in 0..(size_db - 2)) {
                    temp_int = -1000
                    temp_float = wirelessData.get(i + 1).BatteryPerc - wirelessData.get(i).BatteryPerc
                    batt_delta.add(temp_float)
                    temp_long = wirelessData.get(i + 1).CurDate - wirelessData.get(i).CurDate
                    time_delta.add(-temp_long)
                    date_temp = Date(wirelessData.get(i).CurDate)

                    // High Draw on Battery
                    if (-(temp_float.toDouble())/(temp_long.toDouble()) * (1000.0*60.0*60.0) >= delta_battery) {
                        batt_delta_filter.add(temp_float)
                        date_string.add(format_date.format(date_temp))
                        final_string.add("On " + format_date.format(date_temp) + " your battery drain was <b>"
                                + DecimalFormat("##.##").format(-(temp_float.toDouble())*100.0/(temp_long.toDouble()) * (1000.0*60.0*60.0)) +
                                "%</b> per hour." +
                                " Consider reducing your use activity and connecting to a better wireless connection.")
                    }

                    // Poor WiFi Connection
                    temp_long = wirelessData?.get(i)?.CurDate
                    wireless2Data = mDb_avail_wireless?.wireless2DataDao()?.getHighRssWireless(temp_long)
                    if (wireless2Data != null) {
                        if (wireless2Data?.size > 0) {
                            temp_int = wireless2Data.get(0).RSSdBm
                        }
                    }
                    if (temp_int > wirelessData.get(i).RSSdBm + 5) {
                        if (wirelessData.get(i).SSID.removeSurrounding("\"") == wireless2Data?.get(0)?.SSID) {
                            final_string.add(
                                "On " + format_date.format(date_temp) + " you were connected to " +
                                        "<b>" + wirelessData.get(i).SSID.removeSurrounding("\"") + "</b>" +
                                        ", however there was another AP with the same name that had higher Signal Strength"
                            )
                        }
                        else {
                            final_string.add(
                                "On " + format_date.format(date_temp) + " you were connected to " +
                                        "<b>" + wirelessData.get(i).SSID.removeSurrounding("\"") + "</b>" +
                                        ", however " + "<b>" + wireless2Data?.get(0)?.SSID + "</b> may provide higher throughtput."
                            )
                        }
                    }

                    // Poor Throughput
                    temp_double = wirelessData.get(i).ThroughputMpbs
                    if((temp_double < 5) && (wirelessData.get(i).SSID != "4G-LTE")) {
                        final_string.add("On " + format_date.format(date_temp) + " you were connected to " +
                        "<b>" + wirelessData.get(i).SSID + "</b>, however your throughput was well below average at " +
                        "<b>" + DecimalFormat("##.##").format(wirelessData.get(i).ThroughputMpbs) +
                        "Mbps </b>. Consider switching networks or moving to LTE.")
                    }
                }
            }
        }
        // Pass to RecyclerView
        recycle_rec.adapter = BatteryAdapter(final_string, this)

    }
}
