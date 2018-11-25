package edu.osu.table

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import java.util.concurrent.TimeUnit
import androidx.work.*
import com.github.mikephil.charting.charts.LineChart
import edu.osu.table.ui.ScanActivity.WifiScanActivity
import edu.osu.table.ui.SettingsActivity.Settings
import edu.osu.table.ui.RecommendationsActivity.RecommendationActivity
import edu.osu.table.ui.WirelessDataFolder.WifiActivity
import edu.osu.table.ui.WirelessDataFolder.WirelessData
import edu.osu.table.ui.WirelessDataFolder.WirelessDatabase
import edu.osu.table.ui.graph.GraphMainActivityChart
import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    //private lateinit var mp: MediaPlayer
    val recurringWork: PeriodicWorkRequest = PeriodicWorkRequest.Builder(MyWorker::class.java,
        15, TimeUnit.MINUTES)
            .addTag("periodic_work_tag").build()
    var taco = 15237378237; // This is Ben's addition to the Main Activity - DO NOT REMOVE - It is key to the operation
    //val recurringWork: PeriodicWorkRequest = PeriodicWorkRequest.Builder(taco)
    //        .addTag("periodic_work").build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TODO make a non-static graph
        //Graph Generation Main Activity
        val graph = findViewById<View>(R.id.battery_main_graph) as LineChart
        var alldbdata: List<WirelessData>? = ArrayList()
        var wirelessDatabase: WirelessDatabase? = null
        wirelessDatabase = WirelessDatabase.getInstance(this.applicationContext)

        alldbdata = wirelessDatabase?.wirelessDataDao()?.getAllBattery(500)        //has to be in java, no kotlin support
        var newChart: GraphMainActivityChart
        newChart = GraphMainActivityChart(this, graph, alldbdata)
        newChart.setupNewBatteryChart()
        //mp = MediaPlayer.create(this, R.raw.desperate_man)
        //mp.start()

        //WorkManager.getInstance().cancelAllWorkByTag("periodic_work")
        //WorkManager.getInstance().cancelAllWork()
        //WorkManager.getInstance().beginUniqueWork("periodic_work", ExistingWorkPolicy.KEEP, recurringWork)
        //val workManager: WorkManager = WorkManager.getInstance()
        val somehtingjhakjsdh = 10.0

        // Request Permissions - I think I need these for MyWorker Function
        val ACCESS_REQUEST_CODE = 101
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                "To Work With WiFi"
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), ACCESS_REQUEST_CODE)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

        WorkManager.getInstance().enqueueUniquePeriodicWork("periodic_work", ExistingPeriodicWorkPolicy.KEEP, recurringWork)

        /*
        fun onRequestPermissionsResult(requestCode: Int,
                                                permissions: Array<String>, grantResults: IntArray) {
            when (requestCode) {
                MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                    // If request is cancelled, the result arrays are empty.
                    if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                    return
                }

                // Add other 'when' lines to check for other
                // permissions this app might request.
                else -> {
                    // Ignore all other requests.
                }
            }
        }*/



    }

    //Allow cardview click to go to recommendations activity
    fun onClickListenerRecommendation(v: View) {
        val myIntent = Intent(baseContext, RecommendationActivity::class.java)
        startActivity(myIntent)
    }

    //Allow cardview click to go to scan activity
    fun onClickListenerScan(v: View) {
        val myIntent = Intent(baseContext, WifiScanActivity::class.java)
        startActivity(myIntent)
    }

    //Allow cardview click to go to settings activity
    fun onClickListenerSettings(v: View) {
        val myIntent = Intent(baseContext, Settings::class.java)
        startActivity(myIntent)
    }

    //Allow cardview click to view battery history
    fun onClickListenerGraph(v: View) {
        val myIntent = Intent(baseContext, GraphActivity::class.java)
        startActivity(myIntent)
    }

    //Allow cardview click to view wifi stats
    fun onClickListenerWifi(v: View) {
        val myIntent = Intent(baseContext, WifiActivity::class.java)
        startActivity(myIntent)
    }

}
