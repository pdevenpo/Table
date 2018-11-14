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
import androidx.work.Worker
import androidx.work.WorkerParameters
import edu.osu.table.ui.ScanActivity.ScanDatabase
import edu.osu.table.ui.WirelessData.WirelessData
import edu.osu.table.ui.WirelessData.WirelessDatabase
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat


class MyWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private var mDb_wireless: WirelessDatabase? = null
    private var mDb_scan: ScanDatabase? = null

    var speed_public = -1.0


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
            wirelessData.CurDate = System.currentTimeMillis()

            // Only record WiFi Data in Database if Connected
            if( info.linkSpeed != -1) {
                wirelessData.SSID = info.ssid
                wirelessData.MAC_Address = info.bssid
                wirelessData.RSSdBm = info.rssi
                wirelessData.LinkSpeed = info.linkSpeed

                //TODO - Add Corrected Throughput Function Call Here
                wirelessData.ThroughputMpbs = getThroughput()
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
            wirelessData.BatteryPerc = batteryPct!!.toDouble()

            // Final Insert to Database
            wirelessDao?.insert(wirelessData)

            //TODO - Add WiFi Scan Fragment

            Result.SUCCESS
        } catch (throwable: Throwable) {
            Log.e(ContentValues.TAG, "Error: Fault in Worker Task", throwable)
            Result.FAILURE
        }
    }

    // This function will be removed and replaced by Yaxiang's Code
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
    /* InterenetSpeed Test was modified from : https://stackoverflow.com/questions/19258254/how-to-correctly-measure-download-speed-with-java-android */

    // This downloads a JPG image, and it calculates how long it took to do so for the throughput.
    inner class InternetSpeedTest : AsyncTask<String, Void, String>() {

        internal var startTime: Long = 0
        internal var endTime: Long = 0
        private var takenTime: Long = 0

        override fun doInBackground(vararg paramVarArgs: String): String? {

            startTime = System.currentTimeMillis()
            Log.d(ContentValues.TAG, "doInBackground: StartTime: $startTime")

            //val app = AndroidAppHelper.currentApplication()

            //val ctx = app.getApplicationContext()

            /*val res = checkCallingOrSelfPermission(android.Manifest.permission.INTERNET)

            if (res == PackageManager.PERMISSION_GRANTED)
                Log.d(ContentValues.TAG,"Internet granted");
            else
                Log.d(ContentValues.TAG,"No internet");
            */
            var bmp: Bitmap? = null
            try {
                val ulrn = URL(paramVarArgs[0])
                val con = ulrn.openConnection() as HttpURLConnection
                val `is` = con.inputStream
                bmp = BitmapFactory.decodeStream(`is`)

                val bitmap = bmp
                val stream = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 99, stream)
                val imageInByte = stream.toByteArray()
                val lengthbmp = imageInByte.size.toLong()

                if (null != bmp) {
                    endTime = System.currentTimeMillis()
                    Log.d(ContentValues.TAG, "doInBackground: EndTIme$endTime")
                    return lengthbmp.toString() + ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null

        }

        override fun onPostExecute(result: String?) {

            if (result != null) {
                val dataSize = (Integer.parseInt(result) / 1024).toLong()
                takenTime = endTime - startTime
                val s = takenTime.toDouble() / 1000
                val speed = dataSize / s
                Log.d(ContentValues.TAG, "onPostExecute: " + "" + DecimalFormat("##.##").format(speed) + "kb/second")
                speed_public = speed
                //Toast.makeText(this@MyWorker, DecimalFormat("##.##").format(speed_public) + "kb/sec", Toast.LENGTH_SHORT).show()

            }
        }
    }
}


// Other code - kept for reference

/*
            var startTime: Long = 0
            var endTime: Long = 0
            var takenTime: Long = 0

            startTime = System.currentTimeMillis()
            Log.d(ContentValues.TAG, "doInBackground: StartTime: $startTime")
            var bmp: Bitmap? = null

            val ulrn = URL("http://www.daycomsolutions.com/Support/BatchImage/HPIM0050w800.JPG")
            val con = ulrn.openConnection() as HttpURLConnection
            val `is` = con.inputStream
            bmp = BitmapFactory.decodeStream(`is`)

            val bitmap = bmp
            val stream = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 99, stream)
            val imageInByte = stream.toByteArray()
            val lengthbmp = imageInByte.size.toLong()

            if (null != bmp) {
                endTime = System.currentTimeMillis()
                Log.d(ContentValues.TAG, "doInBackground: EndTIme$endTime") }

            if (lengthbmp != null){
                val dataSize = lengthbmp
                takenTime = endTime - startTime
                val s = takenTime.toDouble() / 1000
                val speed = dataSize / s
                Log.d(ContentValues.TAG, "onPostExecute: " + "" + DecimalFormat("##.##").format(speed) + "kb/second")
                speed_public = speed
            }
            */

//InternetSpeedTest().execute("http://www.daycomsolutions.com/Support/BatchImage/HPIM0050w800.JPG")
