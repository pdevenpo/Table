package edu.osu.table

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.BatteryManager
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.work.Worker
import androidx.work.WorkerParameters
import edu.osu.table.ui.ScanActivity.ScanDatabase
import edu.osu.table.ui.WirelessData.WirelessData
import edu.osu.table.ui.WirelessData.WirelessDatabase
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalTime
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.widget.Toast;
import edu.osu.table.ui.ScanActivity.ScanData
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

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

            }
            scanDao?.insert(scanData)
            //----------------------------endScanResults-------------------------------------
            Result.SUCCESS
        } catch (throwable: Throwable) {
            Log.e(ContentValues.TAG, "Error: Fault in Worker Task", throwable)
            Result.FAILURE
        }
    }

    // Mike's Updated Throughput Function
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