package edu.osu.table.ui.WirelessDataFolder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import edu.osu.table.ui.WirelessScan.Wireless2Data
import edu.osu.table.ui.WirelessScan.Wireless2Database


class WiFiScan : AppCompatActivity() {

    var resultList = ArrayList<ScanResult>()
    lateinit var wifiManager: WifiManager

    private var mDb_scan: Wireless2Database? = null
    //private var mDb_scan: ScanDatabase? = null

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            resultList = wifiManager.scanResults as ArrayList<ScanResult>
            Log.d("TESTING", "onReceive Called")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mDb_scan = Wireless2Database.getInstance(this.applicationContext)
        //val scanDao = mDb_scan?.wirelessDaoOptions()

        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        startScanning()

    }

    //override fun onGridTileClicked(x: Int, y: Int) {
    //    startScanning()
    //}

    fun startScanning() {
        registerReceiver(broadcastReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()

        Handler().postDelayed({
            stopScanning()
        }, 10000)
    }

    fun stopScanning() {
        unregisterReceiver(broadcastReceiver)
        var wireless2Data = Wireless2Data()
        var current_time = System.currentTimeMillis()
        //val axisList = ArrayList<Axis>()
        for (result in resultList) {
            wireless2Data.CurDate = current_time
            wireless2Data.SSID = result.SSID


        }


    }
}

// https://stackoverflow.com/questions/49182661/get-wifi-scan-results-list-with-kotlin