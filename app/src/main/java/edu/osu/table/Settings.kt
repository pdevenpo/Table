package edu.osu.table

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import edu.osu.table.ui.settings.SettingsFragment

class Settings : AppCompatActivity() {

/*    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, SettingsFragment.newInstance())
                    .commitNow()
        }
    }*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        // Note that the Toolbar defined in the layout has the id "my_toolbar"
        setSupportActionBar(findViewById(R.id.my_toolbar))
        //display fragment instead of container
        /**if (savedInstanceState == null) {
        supportFragmentManager.beginTransaction()
        .replace(R.id.container, RecommendationFragment.newInstance())
        .commitNow()
        }**/
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
