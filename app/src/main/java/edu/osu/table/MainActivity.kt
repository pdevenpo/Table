package edu.osu.table

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClickListenerRecommendation(v: View) {
        val myIntent = Intent(baseContext, RecommendationActivity::class.java)
        startActivity(myIntent)
    }

    fun onClickListenerScan(v: View) {
        val myIntent = Intent(baseContext, ScanActivity::class.java)
        startActivity(myIntent)
    }


}
