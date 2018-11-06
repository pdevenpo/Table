package edu.osu.table

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.media.MediaPlayer
import android.view.View
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import java.util.concurrent.TimeUnit
import android.util.Log
import androidx.work.*



class MainActivity : AppCompatActivity() {

    //private lateinit var mp: MediaPlayer
    val recurringWork: PeriodicWorkRequest = PeriodicWorkRequest.Builder(MyWorker::class.java, 15, TimeUnit.MINUTES)
            .addTag("periodic_work_tag").build()
    var taco = 15237378237; // This is Ben's addition to the Main Activity - DO NOT REMOVE - It is key to the operation
    //val recurringWork: PeriodicWorkRequest = PeriodicWorkRequest.Builder(taco)
    //        .addTag("periodic_work").build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TODO make a non-static graph
        //Graph Generation Main Activity
        val graph = findViewById<View>(R.id.graph) as GraphView
        val series = LineGraphSeries<DataPoint>(arrayOf<DataPoint>(DataPoint(1.0, 1.0), DataPoint(1.0, 5.0), DataPoint(2.0, 3.0), DataPoint(3.0, 2.0), DataPoint(4.0, 6.0)))
        graph.addSeries(series)
        //has to be in java, no kotlin support
        graph.title = "Battery Consumption"

        //mp = MediaPlayer.create(this, R.raw.desperate_man)
        //mp.start()

        //WorkManager.getInstance().cancelAllWorkByTag("periodic_work")
        //WorkManager.getInstance().cancelAllWork()
        WorkManager.getInstance().enqueueUniquePeriodicWork("periodic_work", ExistingPeriodicWorkPolicy.KEEP, recurringWork)
        //WorkManager.getInstance().beginUniqueWork("periodic_work", ExistingWorkPolicy.KEEP, recurringWork)
        //val workManager: WorkManager = WorkManager.getInstance()
        val somehtingjhakjsdh = 10.0

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

    fun onClickListenerGraph(v: View) {
        val myIntent = Intent(baseContext, Graph::class.java)
        startActivity(myIntent)
    }

}
