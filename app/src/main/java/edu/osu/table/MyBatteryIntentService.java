package edu.osu.table;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class MyBatteryIntentService extends IntentService {

    //private MainActivity mainActivity;
    private WirelessDatabase wireless_Database;
    public MyBatteryIntentService() {
        super("My_Battery_Intent_Service");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Toast.makeText(this, "Intent Service Started", Toast.LENGTH_LONG).show();
        wireless_Database = WirelessDatabase.Companion.getInstance(this);
        final IntentFilter battChangeFilter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
        );
        this.registerReceiver(this.batteryChangeReceiver, battChangeFilter);
        return super.onStartCommand(intent, flags, startId);
    }

    private final BroadcastReceiver batteryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkBatteryLevel(intent);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Intent Service Destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        synchronized (this){
            int count=0;
            while(count<10)
            {
                try {
                    wait(1500);
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void checkBatteryLevel(Intent batteryChangeIntent){
        int currLevel = batteryChangeIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int maxLevel = batteryChangeIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        Double percentage = currLevel * 100.0/maxLevel;
        WirelessData newbattery_data;
        newbattery_data = new WirelessData();
        newbattery_data.setBatteryPerc(percentage);
        newbattery_data.setMAC_Address("");
        newbattery_data.setChanFreq(-1.0);
        newbattery_data.setChBW(0);
        newbattery_data.setSSID("");
        newbattery_data.setThroughputMpbs(0.0);
        newbattery_data.setRSSdBm(0);
        long currtime = Calendar.getInstance().getTimeInMillis();
        newbattery_data.setCurDate(currtime);
        wireless_Database.wirelessDataDao().insert(newbattery_data);
        Toast.makeText(this, "current battery level: " + percentage, Toast.LENGTH_LONG).show();
        Log.d("MyService", "current battery level: " + percentage);
    }
}
