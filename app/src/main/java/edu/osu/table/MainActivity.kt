package edu.osu.table

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.media.MediaPlayer
import android.view.View
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint


class MainActivity : AppCompatActivity() {

    //private lateinit var mp: MediaPlayer

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
