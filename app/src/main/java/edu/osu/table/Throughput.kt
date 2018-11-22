package edu.osu.table

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import java.io.File
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


class Throughput : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommendation_activity)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        DownloadFile().execute("https://upload.wikimedia.org/wikipedia/commons/f/ff/Pizigani_1367_Chart_10MB.jpg")
    }

    private inner class DownloadFile : AsyncTask<String, String, String>() {

        //private var progressDialog: ProgressDialog? = null
        private var fileName: String? = null
        private var folder: String? = null
        private val isDownloaded: Boolean = false

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

                val client = OkHttpClient.Builder()
                    .cache(Cache(dir, 1000))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url(f_url[0])
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
                var throughtheput = (((size * 8.0) / 1024) / 1024) / delta_time * 1000
                Log.d("ElSizoDelFileo", size.toString())
                Log.d("DeltaTeaTime", delta_time.toString())
                Log.d("Throughtheput", "WorkDamnIt: " + DecimalFormat("##.####").format(throughtheput))

                return "Throughput: " + DecimalFormat("##.##").format(throughtheput) + "Mbps"

            } catch (e: Exception) {
                Log.e("Error: ", e.message)
            }

            return "Something went wrong"
        }


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

        R.id.action_menu -> {
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

}
