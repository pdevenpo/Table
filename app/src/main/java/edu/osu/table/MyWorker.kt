package edu.osu.table

import android.app.PendingIntent.getActivity
import android.arch.persistence.room.Room
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.util.*

/*
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v4.content.PermissionChecker.checkCallingOrSelfPermission
import android.util.Log
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
*/
//import com.example.background.R


class MyWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private var mDb_wireless: WirelessDatabase? = null
    private var mDb_scan: ScanDatabase? = null

    var speed_public = -1.0


    override fun doWork(): Result {
        //val appContext = applicationContext

        //Toast.makeText(applicationContext, "Doing Some Stuff in the Back", Toast.LENGTH_SHORT).show()


        //Log.d(TAG, "We Made It")
        //InternetSpeedTest().execute("http://www.daycomsolutions.com/Support/BatchImage/HPIM0050w800.JPG")




        return try {
            // Do Some Stuff Here
            //Instantiate the Database
            /*
            val database =
                Room.databaseBuilder(getActivity()!!.getApplicationContext(), ScanDatabase::class.java, "db-wifi.db")
                    .allowMainThreadQueries()   //Allows room to do operation on main thread
                    .build()
            */
            mDb_wireless = WirelessDatabase.getInstance(this.applicationContext)
            mDb_scan = ScanDatabase.getInstance(this.applicationContext)

            val wirelessDao = mDb_wireless?.wirelessDataDao()
            val scanDao = mDb_scan?.scanDataDao()


            Log.d(ContentValues.TAG, "We Made It")

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
            InternetSpeedTest().execute("http://www.daycomsolutions.com/Support/BatchImage/HPIM0050w800.JPG")
            // Write to Database Throughput
            val wifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wifiManager.connectionInfo
            //val ssid = info.ssid
            val bssid = info.bssid
            //val linkspeed = info.linkSpeed
            //val macaddress = info.macAddress
            val rss = info.rssi

            var wirelessData = WirelessData()

            wirelessData.CurDate = System.currentTimeMillis()
            wirelessData.MAC_Address = bssid
            wirelessData.RSSdBm = rss
            wirelessData.BatteryPerc = 10.0
            wirelessData.ThroughputMpbs = speed_public

            //TODO - Add WiFi Scan Fragment - Are you happy Ben?

            Result.SUCCESS
        } catch (throwable: Throwable) {
            Log.e(ContentValues.TAG, "Error: Who Knows... Shit Happens", throwable)
            Result.FAILURE
        }
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