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
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
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

    //private var mDb: WirelessDatabase? = null

    private lateinit var mBatteryPercent: TextView
    private lateinit var mTime: TextView

    //private lateinit var mDbWorkerThread: DbWorkerThread

    //private val mUiHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommendation_activity)

        //mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        //mDbWorkerThread.start()

        //mBatteryPercent = findViewById(R.id.batt_perc)
        //mTime = findViewById(R.id.time_id)

        DownloadFile().execute("https://edmullen.net/test/rc.jpg")

        //val file = downloadFile("https://www.travelopy.com/static/img/cover.jpg", applicationContext.cacheDir, null, null)
        //mDb = WirelessDatabase.getInstance(this)

        // Note that the Toolbar defined in the layout has the id "my_toolbar"
        setSupportActionBar(findViewById(R.id.my_toolbar))
        //display fragment instead of container
        /**if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, RecommendationFragment.newInstance())
                    .commitNow()
        }**/

        //val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        //this.registerReceiver(myBroadcastReceiver,intentFilter)


        //Thread.sleep(1000)
        //var wirelessData = WirelessData()
        //wirelessData.BatteryPerc = 97.0 //BatteryManager.EXTRA_LEVEL.toDouble()
        //insertWirelessDataInDb(wirelessData = wirelessData)
        //Thread.sleep(1000)
        //wirelessData.BatteryPerc = BatteryManager.EXTRA_LEVEL.toDouble()
        //insertWirelessDataInDb(wirelessData = wirelessData)
        //Thread.sleep(1000)
        //wirelessData.BatteryPerc = BatteryManager.EXTRA_LEVEL.toDouble()
        //insertWirelessDataInDb(wirelessData = wirelessData)

        //Thread.sleep(1000)

    }

    private inner class DownloadFile : AsyncTask<String, String, String>() {

        //private var progressDialog: ProgressDialog? = null
        private var fileName: String? = null
        private var folder: String? = null
        private val isDownloaded: Boolean = false

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        /*override fun onPreExecute() {
            super.onPreExecute()
            this.progressDialog = ProgressDialog(this@RecommendationActivity)
            this.progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            this.progressDialog!!.setCancelable(false)
            this.progressDialog!!.show()
        }*/

        /**
         * Downloading file in background thread
         */
        override fun doInBackground(vararg f_url: String): String {


            var count: Int
            try {

                val dir = applicationContext.cacheDir
                val fileExt = null
                val name = null

                var beginTime: Long = 0
                var finishTime: Long = 0


                //val client = OkHttpClient()
                val client = OkHttpClient.Builder()
                    .cache(Cache(dir, 1000))
                    .connectTimeout(10,TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()


                //client.setReadTimeout(15, TimeUnit.SECONDS)

                //val response = client.newCall()
                //val client = OkHttpClient()
                //client.setConnectTimeout(15, TimeUnit.SECONDS) // connect timeout
                //client.setReadTimeout(15, TimeUnit.SECONDS)
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
                    //val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-kkmmss"))
                    File.createTempFile("something", ext, dir)
                }

                val body = response.body()
                val sink = Okio.buffer(Okio.sink(file))
                /*
                sink.writeAll(body!!.source())
                sink.close()
                body.close()
                 */


                body?.source().use { input ->
                    sink.use { output ->
                        output.writeAll(input)
                    }
                }
                finishTime = System.currentTimeMillis()
                val size = file!!.length()

                var delta_time = finishTime - beginTime
                var throughtheput = (((size * 8.0) / 1024) / 1024) / delta_time * 1000
                Log.d("ElSizoDelFileo", size.toString())
                Log.d("DeltaTeaTime", delta_time.toString())
                Log.d("Throughtheput", "WorkDamnIt: " + DecimalFormat("##.####").format(throughtheput))
                //Toast.makeText(applicationContext,delta_time.toString(), 10)
                //return file!!



                //val file = downloadFile("https://upload.wikimedia.org/wikipedia/commons/f/ff/Pizigani_1367_Chart_10MB.jpg", applicationContext.cacheDir, null, null)

                /*
                val url = URL(f_url[0])
                val connection = url.openConnection()
                connection.connect()
                // getting file length
                val lengthOfFile = connection.getContentLength()


                // input stream to read file - with 8k buffer
                val input = BufferedInputStream(url.openStream(), 8192)

                val timestamp = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Date())

                //Extract file name from URL
                fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1, f_url[0].length)

                //Append timestamp to file name
                fileName = timestamp + "_" + fileName

                val directory = File.createTempFile(fileName,null)
                folder = directory.absolutePath

                //private fun getTempFile(context: RecommendationActivity, url: String): File? =
                //    Uri.parse(url)?.lastPathSegment?.let { filename ->
                //        File.createTempFile(filename, null, context.cacheDir)
                //    }

                //External directory path to save file
                //folder = Environment.getExternalStorageDirectory() + File.separator + "androiddeft/"

                //Create androiddeft folder if it does not exist
                //val directory = File(folder)

                if (!directory.exists()) {
                    directory.mkdirs()
                }

                // Output stream to write file


                //val file = downloadFile("https://www.travelopy.com/static/img/cover.jpg", applicationContext.cacheDir, null, null)

                val output = FileOutputStream(folder + fileName)
                val data = ByteArray(1024)

                var total: Long = 0

                count = input.read(data)
                while (count != -1) {
                    total += count.toLong()
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (total * 100 / lengthOfFile).toInt())
                    Log.d("Something_Tag", "Progress: " + (total * 100 / lengthOfFile).toInt())

                    // writing data to file
                    output.write(data, 0, count)
                    count = input.read(data)
                }

                // flushing output
                output.flush()

                // closing streams
                output.close()
                input.close()
                return "Downloaded at: $folder$fileName"
                */
                return "Throughput: " + DecimalFormat("##.##").format(throughtheput) + "Mbps"

            } catch (e: Exception) {
                Log.e("Error: ", e.message)
            }

            return "Something went wrong"
        }

        /**
         * Updating progress bar
         */
        /*override fun onProgressUpdate(vararg progress: String) {
            // setting progress percentage
            progressDialog!!.progress = Integer.parseInt(progress[0])
        }*/


        override fun onPostExecute(message: String) {
            // dismiss the dialog after the file was downloaded
            //this.progressDialog!!.dismiss()

            // Display File path after downloading
            Toast.makeText(
                applicationContext,
                message, Toast.LENGTH_LONG
            ).show()
        }
    }

    fun downloadFile(url: String, dir: File, name: String?, fileExt: String?): File {
        var beginTime: Long = 0
        var finishTime: Long = 0
        beginTime = System.currentTimeMillis()

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

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
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-kkmmss"))
            File.createTempFile(timestamp, ext, dir)
        }

        val body = response.body()
        val sink = Okio.buffer(Okio.sink(file))
        /*
        sink.writeAll(body!!.source())
        sink.close()
        body.close()
         */

        body?.source().use { input ->
            sink.use { output ->
                output.writeAll(input)
            }
        }
        val size = file!!.length()
        finishTime = System.currentTimeMillis()
        var delta_time = finishTime - beginTime
        var throughtheput = size * 8.0 / 1024 /1024 / delta_time
        Log.d("ElSizoDelFileo", size.toString())
        Log.d("DeltaTeaTime", delta_time.toString())
        Log.d("Throughtheput", "WorkDamnIt: " + DecimalFormat("##.####").format(throughtheput))
        //Toast.makeText(applicationContext,delta_time.toString(), 10)
        return file
    }

    // Mike's Modifications
    /*
    private val myBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val stringBuilder = StringBuilder()
            val batteryPercentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            stringBuilder.append("Battery Percentage: \n$batteryPercentage %\n")
            stringBuilder.append("\nBattery Condition:\n")

            when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> stringBuilder.append("over heat\n")
                BatteryManager.BATTERY_HEALTH_GOOD -> stringBuilder.append("good\n")
                BatteryManager.BATTERY_HEALTH_COLD -> stringBuilder.append("cold\n")
                BatteryManager.BATTERY_HEALTH_DEAD -> stringBuilder.append("dead\n")
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> stringBuilder.append("over voltage\n")
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> stringBuilder.append("failure\n")
                else -> stringBuilder.append("unknown\n")
            }
            stringBuilder.append("\nTemperature:\n")

            val temperatureInCelsius = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10
            stringBuilder.append("$temperatureInCelsius \u00B0C\n")

            val temperatureInFahrenheit = ((temperatureInCelsius * 1.8) + 32).toInt()
            stringBuilder.append("\nPower source:\n")

            when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                BatteryManager.BATTERY_PLUGGED_AC -> stringBuilder.append("AC adapter\n")
                BatteryManager.BATTERY_PLUGGED_USB -> stringBuilder.append("USB connection\n")
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> stringBuilder.append("Wireless connections\n")
                else -> stringBuilder.append("No power source")
            }

            stringBuilder.append("\nCharging status:\n")
            when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                BatteryManager.BATTERY_STATUS_CHARGING -> stringBuilder.append("charging\n")
                BatteryManager.BATTERY_STATUS_DISCHARGING -> stringBuilder.append("not charging\n")
                BatteryManager.BATTERY_STATUS_FULL -> stringBuilder.append("full\n")
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> stringBuilder.append("not changing\n")
                BatteryManager.BATTERY_STATUS_UNKNOWN -> stringBuilder.append("unknown\n")
                else -> stringBuilder.append("unknown\n")
            }

            val technology = intent.extras.getString(BatteryManager.EXTRA_TECHNOLOGY)

            stringBuilder.append("\nTechnology\n$technology\n")

            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toDouble() / 1000
            stringBuilder.append("\nVoltage:\n$voltage V\n")
            message_recommend.text = stringBuilder

            var wirelessData = WirelessData()
            wirelessData.BatteryPerc = batteryPercentage.toDouble()
            //Date currentTime = Calendar.getInstance().getTime()
            wirelessData.CurDate = System.currentTimeMillis()


            insertWirelessDataInDb(wirelessData = wirelessData)
            //Thread.sleep(1000)

            fetchWirelessDataFromDb()


        }
    }*/


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

//    fun onClickListenerMain(v: View) {
//        val myIntent = Intent(baseContext, MainActivity::class.java)
//        startActivity(myIntent)
//    }

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
/*
    private fun bindDataWithUi(wirelessData: WirelessData?){
        mBatteryPercent.text = wirelessData?.BatteryPerc.toString()
        mTime.text = wirelessData?.CurDate.toString()
    }

    private fun fetchWirelessDataFromDb() {
        val task = Runnable {
            val wirelessData =
                    mDb?.wirelessDataDao()?.getAll()
            mUiHandler.post({
                if (wirelessData == null || wirelessData?.size == 0) {
                    //showToast("No data in cache..!!", Toast.LENGTH_SHORT)
                } else {
                    bindDataWithUi(wirelessData = wirelessData?.get(1))
                }
            })
        }
        //mDbWorkerThread.postTask(task)
    }

    private fun insertWirelessDataInDb(wirelessData: WirelessData) {
        val task = Runnable { mDb?.wirelessDataDao()?.insert(wirelessData) }
        //mDbWorkerThread.postTask(task)
    }

    /*private fun readWirelessDataInDb() {
        val task = Runnable { mDb?.wirelessDataDao()?.getAll() }
        mDbWorkerThread.postTask(task)
    }*/

    /*override fun onDestroy() {
        WirelessDatabase.destroyInstance()
        mDbWorkerThread.quit()
        super.onDestroy()
    }*/
    */
}
