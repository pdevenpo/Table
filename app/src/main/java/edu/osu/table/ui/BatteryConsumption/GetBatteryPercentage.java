package edu.osu.table.ui.BatteryConsumption;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;

import static android.content.Context.BATTERY_SERVICE;



public class GetBatteryPercentage extends AppCompatActivity {
    int batterypct = 0;


    public double getBatteryPercentage(double batterpct){

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        double batteryPct = level / (double) scale;

        return (double) batteryPct * 100;
    }





}
