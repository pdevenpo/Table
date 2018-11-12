package edu.osu.table

import android.app.PendingIntent.getActivity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import edu.osu.table.ui.graph.GraphFragment
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import edu.osu.table.ui.WirelessData.WirelessData
import edu.osu.table.ui.WirelessData.WirelessDatabase
import java.sql.Date
import java.sql.Timestamp
import java.util.*

class Graph : AppCompatActivity() {

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graph_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, GraphFragment.newInstance())
                    .commitNow()
        }
    }*/

    //private lateinit var myBatteryService: MyBatteryIntentService
    //private lateinit var newWirelessData: WirelessData
    private var wireless_Database: WirelessDatabase? = null
    private val TAG: String = "GraphActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graph_activity)
        // Note that the Toolbar defined in the layout has the id "my_toolbar"
        setSupportActionBar(findViewById(R.id.my_toolbar))
        //display fragment instead of container
        /**if (savedInstanceState == null) {
        supportFragmentManager.beginTransaction()
        .replace(R.id.container, RecommendationFragment.newInstance())
        .commitNow()
        }**/

        wireless_Database = WirelessDatabase.getInstance(this)

        val graph = findViewById<View>(R.id.battery_graph) as GraphView
        /*
        val series = LineGraphSeries<DataPoint>(arrayOf<DataPoint>(DataPoint(1.0, 1.0), DataPoint(1.0, 5.0), DataPoint(2.0, 3.0), DataPoint(3.0, 2.0), DataPoint(4.0, 6.0)))
        //graph.addSeries(series)
        var x : Double = 0.0
        var y : Double = 0.0
        var newseries = LineGraphSeries<DataPoint>()
        for (i in 1..500){
            x += 0.1
            y = Math.sin(x)
            newseries.appendData(DataPoint(x,y), true, 500)
        }
        graph.addSeries(newseries)
        val stringBuilder = StringBuilder()

        myBatteryService = MyBatteryIntentService()
        if (myBatteryService != null){
            val i = Intent(baseContext, MyBatteryIntentService::class.java)
            startService(i)
        }

        var currLevel = 0.1
        var maxLevel = 0.2
        for (i in 1..500){
            val percentage = currLevel * 100.0 / maxLevel
            val newbattery_data: WirelessData
            newbattery_data = WirelessData()
            newbattery_data.BatteryPerc = percentage
            newbattery_data.MAC_Address = ""
            //newbattery_data.ChanFreq = -1.0
            //newbattery_data.SSID = ""
            //newbattery_data.ThroughputMpbs = 0.0
            //newbattery_data.Security = ""
            //newbattery_data.RSSdBm = -1
            val currtime = Calendar.getInstance().timeInMillis
            newbattery_data.CurDate = currtime
            currLevel += 0.5
            maxLevel  +=0.1
            //MainActivity().myappdb.wirelessDao().addWireless_Data(newbattery_data)
            //wireless_Database.wirelessDao().addWireless_Data(newbattery_data)

        }*/

        val alldbdata: List<WirelessData>?
        alldbdata = wireless_Database?.wirelessDataDao()?.getAllBattery()
        var batterypnts : LineGraphSeries<DataPoint> = LineGraphSeries()
        for (dbdata in alldbdata.orEmpty().asReversed())
        {
            Log.d(TAG, "time : " + dbdata.CurDate.toString() +" battery Percentage: " + dbdata.BatteryPerc.toString())
            val date = Date(dbdata.CurDate)
            var batterypct: Double = dbdata.BatteryPerc
            batterypnts.appendData(DataPoint(date, batterypct), true, 500)
        }
        Log.d(TAG, "Retrieving from the DB has happened")
        graph.removeAllSeries()
        graph.addSeries(batterypnts)
        graph.title = "Battery Consumption"
        //Toast.makeText(this, "Graph Activity Creted", Toast.LENGTH_LONG)

        // set date label formatter
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        graph.getGridLabelRenderer().setHumanRounding(false);

        // set manual x bounds to have nice steps
        //graph.getViewport().setMinX(alldbdata.orEmpty()[0].CurDate);
        //graph.getViewport().setMaxX(d3.getTime());
        graph.getViewport().setXAxisBoundsManual(true);
    }

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

}
