package edu.osu.table

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.work.Worker
import androidx.work.WorkerParameters
import edu.osu.table.ui.ScanActivity.ScanDatabase
import edu.osu.table.ui.WirelessDataFolder.WirelessData
import edu.osu.table.ui.WirelessDataFolder.WirelessDatabase
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import edu.osu.table.ui.ScanActivity.ScanData
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import edu.osu.table.ui.WirelessDataFolder.WiFiScan
import edu.osu.table.ui.WirelessScan.Wireless2Dao
//import edu.osu.table.ui.WirelessDataOptions.WirelessDataOptions
import edu.osu.table.ui.WirelessScan.Wireless2Data
import edu.osu.table.ui.WirelessScan.Wireless2Database
import java.io.FileOutputStream
import java.lang.Thread.sleep

/**
 *
 * Development of this class was aided by the following sources:
 * https://stackoverflow.com/questions/49182661/get-wifi-scan-results-list-with-kotlin
 * https://code.luasoftware.com/tutorials/android/android-download-file-using-okhttp/
 * https://stackoverflow.com/questions/27819100/android-fileoutputstream-location-save-file
 *
 */

class MyWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private var mDb_wireless: WirelessDatabase? = null
    private var mDb_wireless2: Wireless2Database? = null
    //private var mDb_scan: ScanDatabase? = null

    override fun doWork(): Result {

        return try {
            Log.d(ContentValues.TAG, "Entering MyWorker Class")

            //Instantiate the Database
            mDb_wireless = WirelessDatabase.getInstance(this.applicationContext)
            mDb_wireless2 = Wireless2Database.getInstance(this.applicationContext)

            val wirelessDao = mDb_wireless?.wirelessDataDao()
            val wireless2Dao = mDb_wireless2?.wireless2DataDao()

            // Collect Current WiFi Data & Write to Database
            val wifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wifiManager.connectionInfo

            var wirelessData = WirelessData()
            var current_time = System.currentTimeMillis()
            wirelessData.CurDate = current_time
            wirelessData.SSID = "4G-LTE"  // Default if no WiFi

            // Only record WiFi Data in Database if Connected to WiFi
            if( info.linkSpeed != -1 && info.bssid != null && info.ssid != null ) {
                wirelessData.SSID = info.ssid
                wirelessData.MAC_Address = info.bssid
                wirelessData.RSSdBm = info.rssi
                wirelessData.LinkSpeed = info.linkSpeed
                wirelessData.ThroughputMpbs = getThroughput_v2()
            }
            // Get Battery Percentage
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                this.applicationContext.registerReceiver(null, ifilter)
            }
            val batteryPct: Float? = batteryStatus?.let { intent ->
                val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                level / scale.toFloat()
            }
            wirelessData.BatteryPerc = batteryPct!!.toFloat()
            wirelessDao?.insert(wirelessData)

            // WiFi Scan
            var scannedWiFis = ArrayList<ScanResult>()
            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    scannedWiFis = wifiManager.scanResults as ArrayList<ScanResult>
                    Log.d("Scan Complete", "true")
                }
            }

            this.applicationContext.registerReceiver(broadcastReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            var a = wifiManager.startScan()
            Log.d("ScanSuccess", a.toString())
            Log.d("Permissions", (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED).toString())

            sleep(10000)  // Wait for WiFi Scan to Complete

            this.applicationContext.unregisterReceiver(broadcastReceiver)
            var wireless2Data = Wireless2Data()
            for (WiFi_instance in scannedWiFis) {
                Log.d("WiFi Name: ", WiFi_instance.SSID)
                wireless2Data.CurDate = current_time
                wireless2Data.SSID = WiFi_instance.SSID
                wireless2Data.RSSdBm = WiFi_instance.level
                wireless2Data.MAC_Address = WiFi_instance.BSSID
                // Estimate of Conservative LinkSpeed (802.11g)
                if (WiFi_instance.level < -81) {
                    wireless2Data.LinkSpeed = 0
                }
                else if (WiFi_instance.level < -80) {
                    wireless2Data.LinkSpeed = 6
                }
                else if (WiFi_instance.level < -78) {
                    wireless2Data.LinkSpeed = 9
                }
                else if (WiFi_instance.level < -76) {
                    wireless2Data.LinkSpeed = 12
                }
                else if (WiFi_instance.level < -73) {
                    wireless2Data.LinkSpeed = 18
                }
                else if (WiFi_instance.level < -69) {
                    wireless2Data.LinkSpeed = 24
                }
                else if (WiFi_instance.level < -65) {
                    wireless2Data.LinkSpeed = 36
                }
                else {
                    wireless2Data.LinkSpeed = 48
                }
                wireless2Dao?.insert(wireless2Data)
            }

            Result.SUCCESS
        } catch (throwable: Throwable) {
            Log.e(ContentValues.TAG, "Error: Fault in MyWorker Class", throwable)
            Result.FAILURE
        }
    }

    private fun getThroughput_v2(): Double {
        val dir_loc = applicationContext.cacheDir
        val client = OkHttpClient.Builder()
            .cache(Cache(dir_loc, 1000))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("https://upload.wikimedia.org/wikipedia/commons/f/ff/Pizigani_1367_Chart_10MB.jpg")
            .build()

        var beginTime: Long = 0
        var finishTime: Long = 0
        var file = File.createTempFile("temp_image", ".jpg", dir_loc)
        var output_file_stream = FileOutputStream(file)
        var size: Long = 0
        var delta_time: Long = 0
        var throughput_out: Double = 0.0

        beginTime = System.currentTimeMillis()
        val response = client.newCall(request).execute()
        output_file_stream.write(response.body()?.bytes())
        output_file_stream.close()
        finishTime = System.currentTimeMillis()

        size = file!!.length()
        delta_time = finishTime - beginTime
        throughput_out = (((size * 8.0) / 1024) / 1024) / delta_time * 1000

        Log.d("File Size", size.toString())
        Log.d("Delta Time", delta_time.toString())
        Log.d("Throughput", DecimalFormat("##.####").format(throughput_out))

        return throughput_out
    }
}

