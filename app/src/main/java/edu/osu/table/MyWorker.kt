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
//import edu.osu.table.ui.WirelessDataOptions.WirelessDataOptions
import edu.osu.table.ui.WirelessScan.Wireless2Data
import edu.osu.table.ui.WirelessScan.Wireless2Database
import java.lang.Thread.sleep


//import com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table




class MyWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private var mDb_wireless: WirelessDatabase? = null
    private var mDb_scan: ScanDatabase? = null

    override fun doWork(): Result {

        return try {
            Log.d(ContentValues.TAG, "Entering MyWorker Class")
            //Instantiate the Database
            mDb_wireless = WirelessDatabase.getInstance(this.applicationContext)
            mDb_scan = ScanDatabase.getInstance(this.applicationContext)

            val wirelessDao = mDb_wireless?.wirelessDataDao()
            val scanDao = mDb_scan?.scanDataDao()

            // Collect Current WiFi Data & Write to Database
            val wifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wifiManager.connectionInfo

            var wirelessData = WirelessData()
            var scanData = ScanData()
            wirelessData.CurDate = System.currentTimeMillis()
            wirelessData.SSID = "4G-LTE"  // Default if no WiFi

            // Only record WiFi Data in Database if Connected
            if( info.linkSpeed != -1) {
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

            // Write Battery Percentage to Database
            wirelessData.BatteryPerc = batteryPct!!.toFloat()
            // Final Insert to Database
            wirelessDao?.insert(wirelessData)

            // Mike's Third Attempt at WiFi Scan - https://stackoverflow.com/questions/49182661/get-wifi-scan-results-list-with-kotlin

            var resultList = ArrayList<ScanResult>()
            //lateinit var wifiManager: WifiManager

            var mWireless2Database: Wireless2Database? = null

            mWireless2Database = Wireless2Database.getInstance(this.applicationContext)
            val wireless2Dao = mWireless2Database?.wireless2DataDao()
            //val scanDao = mDb_scan?.wirelessDaoOptions()

            //wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(contxt: Context?, intent: Intent?) {
                    resultList = wifiManager.scanResults as ArrayList<ScanResult>
                    Log.d("TESTING", "onReceive Called")
                    Log.d("WiFi", resultList.size.toString())
                    Log.d("ScanResults", wifiManager.scanResults.toString())
                }
            }
            fun stopScanning() {
                this.applicationContext.unregisterReceiver(broadcastReceiver)
                var wireless2Data = Wireless2Data()
                var current_time = System.currentTimeMillis()
                //val axisList = ArrayList<Axis>()
                for (result in resultList) {
                    wireless2Data.CurDate = current_time
                    wireless2Data.SSID = result.SSID
                    wireless2Data.RSSdBm = result.level
                    wireless2Data.MAC_Address = result.BSSID
                    //wireless2Data.ThroughputMpbs = result.frequency
                    wireless2Dao?.insert(wireless2Data)
                }
            }
            fun startScanning() {
                this.applicationContext.registerReceiver(broadcastReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
                var a = wifiManager.startScan()
                Log.d("ScanSuccess", a.toString())
                Log.d("Permissions", (ContextCompat.checkSelfPermission(this.applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED).toString())
                //val loop = Looper.prepare()
                /*Handler().postDelayed({
                    stopScanning()
                }, 10000)*/
            }

            //wifiManager.createWifiLock("lock_tag")
            startScanning()
            sleep(2000)
            stopScanning()

            // Mike's Second Attempt at WiFi Scan
            //Looper.prepare()
            //WiFiScan()


            // Mike's Attempt at WiFi Scan
            /*
            var resultList = ArrayList<ScanResult>()
            //lateinit var wifiManager: WifiManager

            /*val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(contxt: Context?, intent: Intent?) {
                    resultList = wifiManager.scanResults as ArrayList<ScanResult>
                    Log.d("TESTING", "onReceive Called")
                }
            }*/

            //this.applicationContext.registerReceiver(broadcastReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            val scanres = wifiManager.startScan()

            val scanprogress = WifiManager.SCAN_RESULTS_AVAILABLE_ACTION

            Handler().postDelayed({
                resultList = wifiManager.scanResults as ArrayList<ScanResult>
            }, 10000)
            //sleep(2000)

            //this.applicationContext.unregisterReceiver(broadcastReceiver)
            var wireless2Data = Wireless2Data()
            var current_time = System.currentTimeMillis()

            for (result in resultList) {
                wireless2Data.CurDate = current_time
                wireless2Data.SSID = result.SSID
            }


            //-----------------------------ScanResults-------------------------------------
            //TODO Store in Database
            val lWifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val a = lWifiManager.startScan()//request a scan for access points
            val lResults = lWifiManager.getScanResults()

            for (lScanResult in lResults) {
                val curTime = Calendar.getInstance().time
                val curTimeLong = curTime.time.toLong()
                scanData.CurDate = curTimeLong
                scanData.MAC_Address = lScanResult.BSSID;
                //val ssid = lScanResult.SSID;
                scanData.Security =  lScanResult.capabilities;
                //scanData. = lScanResult.level;
                scanData.ChanFreq =  lScanResult.frequency.toDouble()
                //scanData.ChanFreq = frequency.toDouble()
                scanDao?.insert(scanData)

            }

            //----------------------------endScanResults-------------------------------------
            */
            Result.SUCCESS
        } catch (throwable: Throwable) {
            Log.e(ContentValues.TAG, "Error: Fault in Worker Task", throwable)
            Result.FAILURE
        }
    }

    // Mike's Updated Throughput Function - https://code.luasoftware.com/tutorials/android/android-download-file-using-okhttp/
    private fun getThroughput_v2(): Double {
        val dir = applicationContext.cacheDir
        val fileExt = null
        val name = null
        var beginTime: Long = 0
        var finishTime: Long = 0

        val client = OkHttpClient.Builder()
            .cache(Cache(dir, 1000))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("https://upload.wikimedia.org/wikipedia/commons/f/ff/Pizigani_1367_Chart_10MB.jpg")
            .build()

        beginTime = System.currentTimeMillis()
        val response = client.newCall(request).execute()
        val contentType = response.header("content-type", null)
        var ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType)
        ext = if (ext == null) {
            fileExt
        } else {
            ".$ext"
        }

        // use provided name or generate a temp file
        var file: File? = null
        file = if (name != null) {
            val filename = String.format("%s%s", name, ext)
            File(dir.absolutePath, filename)
        } else {
            File.createTempFile("something", ext, dir)
        }

        val body = response.body()
        val sink = Okio.buffer(Okio.sink(file))

        body?.source().use { input ->
            sink.use { output ->
                output.writeAll(input)
            }
        }
        finishTime = System.currentTimeMillis()
        val size = file!!.length()

        var delta_time = finishTime - beginTime
        var throughput_out = (((size * 8.0) / 1024) / 1024) / delta_time * 1000
        Log.d("File Size", size.toString())
        Log.d("Delta Time", delta_time.toString())
        Log.d("Throughput", DecimalFormat("##.####").format(throughput_out))

        return throughput_out
    }

    // Mike's Original Throughput Function
    private fun getThroughput():  Double {
        var beginTime: Long = 0
        var finishTime: Long = 0

        beginTime = System.currentTimeMillis()

        var image: Bitmap? = null
        val url_val = URL("http://www.daycomsolutions.com/Support/BatchImage/HPIM0050w800.JPG")
        val connection = url_val.openConnection() as HttpURLConnection
        val `is` = connection.inputStream
        image = BitmapFactory.decodeStream(`is`)
        val bitmap = image
        val stream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 99, stream)
        val imageInByte = stream.toByteArray()
        val lengthbmp = imageInByte.size.toLong()

        if (null != image) {
            finishTime = System.currentTimeMillis()
        }

        val dataSize = (lengthbmp / 1024).toLong()
        val deltaTime = finishTime - beginTime
        val s = deltaTime.toDouble() / 1000
        val speed = dataSize / s
        Log.d(ContentValues.TAG, "onPostExecute: " + "" + DecimalFormat("##.##").format(speed) + "kb/second")
        return speed

    }
}