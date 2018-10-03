package edu.osu.table

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import edu.osu.table.ui.scan.ScanFragment

class ScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scan_activity)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ScanFragment.newInstance())
                    .commitNow()
        }

    }

}
