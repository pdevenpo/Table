package edu.osu.table.ui.RecommendationsActivity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import edu.osu.table.MainActivity
import edu.osu.table.R
import android.widget.Toast
import com.google.common.io.Flushables.flush
import java.nio.file.Files.exists
import java.io.File.separator
import android.os.Environment.getExternalStorageDirectory
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
//import edu.osu.table.MainActivity
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import edu.osu.table.ui.ScanActivity.ScanDatabase
import edu.osu.table.ui.WirelessData.WirelessData
import edu.osu.table.ui.WirelessData.WirelessDatabase
import kotlinx.android.synthetic.main.recommendation_activity.*
import okhttp3.Cache
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import java.io.*
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


class RecommendationActivity : AppCompatActivity() {

    private var mDb_wireless: WirelessDatabase? = null
    private var mDb_scan: ScanDatabase? = null

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
        menuInflater.inflate(R.menu.menu_main, menu)
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
        R.id.action_menu -> {
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    fun battery_usage ()
    {
        mDb_wireless = WirelessDatabase.getInstance(this.applicationContext)
        //val wirelessDao = mDb_wireless?.wirelessDataDao()
        //val scanDao = mDb_scan?.scanDataDao()

        var batt_delta: MutableList<Float> = mutableListOf()
        var batt_delta_filter: MutableList<Float> = mutableListOf()
        var temp_var:Float = (0.0).toFloat()

        //val wirelessData = listOf(WirelessData())

        val wirelessData = mDb_wireless?.wirelessDataDao()?.getAllBattery(96)

        val datelist = mDb_wireless?.wirelessDataDao()?.getDates(96)

        var date_temp: Date
        val format_date = SimpleDateFormat("E 'at' h:mm a z")
        format_date.timeZone = TimeZone.getDefault()

        var date_string: MutableList<String> = mutableListOf()
        var final_string: MutableList<String> = mutableListOf()

        var size_db = 0
        if (wirelessData != null) {
            size_db = wirelessData?.size


            if (size_db >= 2) {
                for (i in 0..(size_db - 2)) {
                    temp_var = wirelessData.get(i).BatteryPerc - wirelessData.get(i + 1).BatteryPerc
                    batt_delta.add(temp_var)
                }
            }

            if(size_db >=1){
                for (i in 0..(wirelessData.size-1)){
                    date_temp = Date(datelist!!.get(i))
                    if (i >= 1){
                        if (batt_delta.get(i-1) >= 0.05) {
                            batt_delta_filter.add(batt_delta.get(i-1))
                            date_string.add(format_date.format(date_temp))
                            final_string.add("On " + format_date.format(date_temp) + " your battery drain was "
                                    + DecimalFormat("##.##").format(batt_delta.get(i-1)*400) + "% per hour." +
                                    " Consider reducing your use activity and connecting to a better wireless connection.")
                        }
                    }


                }
            }
        }

        // Batt_Delta_Filter is not used here... I need to remove it
        recycle_rec.adapter = BatteryAdapter(final_string, batt_delta_filter, this)

    }
}
