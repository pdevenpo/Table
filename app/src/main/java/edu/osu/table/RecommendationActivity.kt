package edu.osu.table

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import edu.osu.table.ui.recommendation.RecommendationFragment
import kotlinx.android.synthetic.main.recommendation_fragment.*

class RecommendationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommendation_activity)
        // Note that the Toolbar defined in the layout has the id "my_toolbar"
        setSupportActionBar(findViewById(R.id.my_toolbar))
        //display fragment instead of container
        /**if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, RecommendationFragment.newInstance())
                    .commitNow()
        }**/

        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        this.registerReceiver(myBroadcastReceiver,intentFilter)
    }

    // Mike's Modifications
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
        }
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
