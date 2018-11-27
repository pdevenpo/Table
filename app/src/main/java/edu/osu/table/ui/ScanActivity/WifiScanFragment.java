package edu.osu.table.ui.ScanActivity;

import android.Manifest;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edu.osu.table.MainActivity;
import edu.osu.table.R;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
/**
 * Partial pieces of the document Provided by Professor Adam Champion of The Ohio State University for use in
 * Professor Dong Xuan's Mobile Handset System class with permission from Professor Champion.
 * Other Documents provided by him are prefaced with this descriptor at the top of the file -->
 */

/**
 * Fragment for Wi-Fi scans.
 *
 * Created by adamcchampion on 2018/01/05.
 */

public class WifiScanFragment extends Fragment {
    /*
     * ************************************************************************
     * Declare class scoped fields. Notice they are prefixed with "m" (for most
     * variables) or "s" (for services).
     * ************************************************************************
     */
    private Button btn_ping;
    private Button btn_download;

    private TextView pingtime;
    private TextView speed;
    Activity activity;
    private long l, total;
    private Handler handler = new Handler();
    private TextView download_speed;
    private WifiManager mWifiManager;
    private IntentFilter mIntentFilter;
    private RecyclerView mScanResultRecyclerView;
    private ScanResultAdapter mScanResultAdapter;
    private List<ScanResult> mScanResultList = new ArrayList<>();
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private final String TAG = getClass().getSimpleName();
    //Create necessary variables for database insertion
    private String wifiSSID;
    private String wifiMacAddress;
    private String wifiSecurity;
    private int wifiRssDbm;
    private double wifiChanFreq;
    //will be incremented for every added wifi to keep different ids on the wifi
    public long wifiID = 0;
    //create a variable to hold the best connections dbm and name
    public String bestBSSID = "";
    public int bestDBM = -80;
    public String bestSSID = "";


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        // Override onReceive() method to implement our custom logic.
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                if (mWifiManager == null) {
                    setupWifi();
                }
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                Log.d(TAG, "Wi-Fi scan results available");
                int numResults = scanResults.size(), numTotalResults = mScanResultList.size();
                mScanResultList.addAll(scanResults);
                mScanResultAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Inflate the appropriate view for ui
        View v = inflater.inflate(R.layout.fragment_wifi_scan, container, false);
        //Initialize buttons
        Button wifi_button = (Button) v.findViewById(R.id.connect_wifi);
        final View t = container;

        //onclick listener for wifi_button to connect to a new wifi
        Button wifi_button_5G = (Button) v.findViewById(R.id.connect_wifi_5G);
        wifi_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mWifiManager.isWifiEnabled()) {
                    WifiConfiguration config = new WifiConfiguration();
                    config.allowedAuthAlgorithms.clear();
                    config.allowedGroupCiphers.clear();
                    config.allowedKeyManagement.clear();
                    config.allowedPairwiseCiphers.clear();
                    config.allowedProtocols.clear();
                    config.SSID = "\"" + bestSSID + "\"";
                    config.BSSID = bestBSSID;
                    List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
                    for (WifiConfiguration existingConfig : existingConfigs) {
                        if (null != existingConfig && existingConfig.SSID.toString().equals("\"" + "osuwireless" + "\"")) {
                            mWifiManager.removeNetwork(existingConfig.networkId);
                        }
                    }
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int wcgID = mWifiManager.addNetwork(config);
                    boolean b = mWifiManager.enableNetwork(wcgID, true);
                    Snackbar mySnackbar2 = Snackbar.make(t, "Please wait as your device re-connects.", Snackbar.LENGTH_LONG);
                    mySnackbar2.show();

                }else{
                    mWifiManager.setWifiEnabled(true);
                    WifiConfiguration config = new WifiConfiguration();
                    config.allowedAuthAlgorithms.clear();
                    config.allowedGroupCiphers.clear();
                    config.allowedKeyManagement.clear();
                    config.allowedPairwiseCiphers.clear();
                    config.allowedProtocols.clear();
                    config.SSID = "\"" + bestSSID + "\"";
                    config.BSSID = bestBSSID;
                    List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
                    for (WifiConfiguration existingConfig : existingConfigs) {
                        if (null != existingConfig && existingConfig.SSID.toString().equals("\"" + "osuwireless" + "\"")) {
                            mWifiManager.removeNetwork(existingConfig.networkId);
                        }
                    }
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int wcgID = mWifiManager.addNetwork(config);
                    boolean b = mWifiManager.enableNetwork(wcgID, true);
                    Snackbar mySnackbar2 = Snackbar.make(t, "Please wait as your device re-connects.", Snackbar.LENGTH_LONG);
                    mySnackbar2.show();
                }
            }
        });
        //Initialize the action bar
        setHasOptionsMenu(true);
        //Initialize the recycler view
        mScanResultRecyclerView = (RecyclerView) v.findViewById(R.id.scan_result_recyclerview);
        mScanResultAdapter = new ScanResultAdapter(mScanResultList);
        mScanResultRecyclerView.setAdapter(mScanResultAdapter);
        mScanResultRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Call the setupwifi method to
        setupWifi();
        mIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        setHasOptionsMenu(true);
        setRetainInstance(true);
        //Call the wifiscan on activity load to avoid button press
        doWifiScan();

        wifi_button_5G.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar mySnackbar3 = Snackbar.make(t, "Please wait as we conduct a download speed test.", Snackbar.LENGTH_LONG);
                mySnackbar3.show();
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        float rate = download();
                        rate = (float) rate / 1000;
                        final String rate1 = Float.toString(rate);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar mySnackbar = Snackbar.make(t, "Your current download speeds are: " + rate1 + "Mbps.", Snackbar.LENGTH_LONG);
                                mySnackbar.show();
                            }
                        });
                    }

                }).start();
            }
        });

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean hideDialog = sharedPreferences.getBoolean(
                getResources().getString(R.string.suppress_dialog_key), false);


        try {
            getActivity().registerReceiver(mReceiver, mIntentFilter);
        } catch (NullPointerException npe) {
            Log.d(TAG, "Error registering BroadcastReceiver");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            getActivity().unregisterReceiver(mReceiver);
        } catch (NullPointerException npe) {
            Log.d(TAG, "Error un-registering BroadcastReceiver");
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //give actions to actionbar items
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                Log.d(TAG, "Request Wi-Fi scan");
                doWifiScan();
                return true;
            case R.id.action_back:
                // User chose the "Back" item, return to main activity...
                Intent intent = new Intent(WifiScanFragment.this.getActivity(), MainActivity.class);
                startActivity(intent);
                return true;

        }
        return false;
    }



    private void setupWifi() {
        try {
            Context context = getActivity().getApplicationContext();
            if (context != null) {
                mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Error setting up Wi-Fi");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasLocationPermission() {
        return getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }



    private void doWifiScan() {
        if (mWifiManager == null) {
            setupWifi();
        }
        boolean scanRetVal = mWifiManager.startScan();
        if (!scanRetVal) {
            Log.e(TAG, "Error scanning for Wi-Fi");
        }
    }

    private class ScanResultHolder extends RecyclerView.ViewHolder {

        private TextView mScanResultTextView;
        private ScanResult mScanResult;

        public ScanResultHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_wifi_scan, parent, false));

            mScanResultTextView = itemView.findViewById(R.id.scan_result_textview);
        }

        public void bind(ScanResult scanResult) {
            mScanResult = scanResult;

            //Instantiate the Database
            ScanDatabase database = Room.databaseBuilder(getActivity().getApplicationContext(), ScanDatabase.class, "scandb.db")
                    .allowMainThreadQueries()   //Allows room to do operation on main thread
                    .build();
            ScanDao scanDao = database.scanDataDao();
            ScanData scanData = new ScanData();
            //set wirelessdata to all the wifi information
            //int j = GetBatteryPercentage.getBatteryPercentage();
            if (mScanResult.level > bestDBM) {
                bestDBM = mScanResult.level;
                bestBSSID = mScanResult.BSSID;
                bestSSID = mScanResult.SSID;
            }
            wifiChanFreq = mScanResult.frequency;
            wifiMacAddress = mScanResult.BSSID;
            wifiRssDbm = mScanResult.level;
            wifiSecurity = mScanResult.capabilities;
            if(wifiSecurity.contains("WPA2")){
                wifiSecurity = "WPA2";
            }else if(wifiSecurity.contains("WEP")){
                wifiSecurity = "WEP";
            }else if(wifiSecurity.contains("WPA")){
                wifiSecurity = "WPA";
            }else{
                wifiSecurity = "None";

            }
            wifiSSID = mScanResult.SSID;


            scanData.setId(wifiID);
            scanData.setMAC_Address(wifiMacAddress);
            scanDao.insert(scanData);


            String resultTextStr = "WIFI NAME: " + mScanResult.SSID + "; " + '\n' +
                    "BSSID: " + mScanResult.BSSID + "; " + '\n' +
                    "SECURITY: " + wifiSecurity + "; " + '\n' +
                    "FREQUENCY: " + mScanResult.frequency + " MHz;" + '\n' +
                    "RSS: " + mScanResult.level + " dBm";
            wifiID++;
            if (mScanResult.capabilities.contains("WPA")) {
                mScanResultTextView.setTextColor(getResources().getColor(R.color.dividerColor));
                mScanResultTextView.setText(resultTextStr);
            } else {
                mScanResultTextView.setTextColor(getResources().getColor(R.color.white));
                mScanResultTextView.setText(resultTextStr);
            }

        }
    }

    private class ScanResultAdapter extends RecyclerView.Adapter<ScanResultHolder> {

        private List<ScanResult> mScanResults;

        public ScanResultAdapter(List<ScanResult> scanResultList) {
            mScanResults = scanResultList;
        }

        @Override
        public ScanResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            //View view = LayoutInflater.from(activity).inflate(R.layout.item_layout, parent, false);
            //return new ScanResultHolder(view);
            //return null;
            return new ScanResultHolder(layoutInflater, parent);
        }


        @Override
        public void onBindViewHolder(ScanResultHolder holder, int position) {
            ScanResult scanResult = mScanResults.get(position);
            holder.bind(scanResult);
        }

        @Override
        public int getItemCount() {
            return mScanResultList.size();
        }
    }

    public static float getdelay() {

        String result = null;
        float value = 0;


        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 1 www.google.com");
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.i("Throughput", "result content : " + stringBuffer.toString());
            String arr[] = stringBuffer.toString().split("=");
            String time[] = arr[3].split(" ");


            Log.i("Throughput", "time=" + time[0]);
            float k = Float.parseFloat(time[0]);

            int status = p.waitFor();
            if (status == 0) {
                result = "successful~";
                //value = 64 * 8 / Float.parseFloat(time[1])/1000 ; // Mbits/s
                value = Float.parseFloat(time[0]);
                return value;
            } else {
                result = "failed~ cannot reach the IP address";
            }
        } catch (IOException e) {
            result = "failed~ IOException";
        } catch (InterruptedException e) {
            result = "failed~ InterruptedException";
        } finally {
            Log.i("Throughput", "result = " + result);
        }
        return value;
    }

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            l = TrafficStats.getTotalRxBytes() - total;
            total += l;
            Log.i("speed", "download speed: " + ((l / 1024) * 8) + "kb/s");
            handler.postDelayed(runnable, 1000);
            download_speed.setText("Download Speed:" + ((l / 1024) * 8) + " kb/s");
        }
    };


    public static float download() {
        float rate = 0;
        float latency = getdelay();
        int i;
        String download_url = "https://lh3.googleusercontent.com/upeaGdkSJ_2rr4vmYb8xND5r15UGwcnJr1MBQW8W7VFxJclJ7w1VxH-Fv_OboqrPVtxY-ASxPgWhyqRUHTQFbVIX54RNpXTGEitkgQ=w1440";

        try {
            URL url = new URL(download_url);
            float red = 0;
            float size = 0;
            long time;
            float time1;
            byte[] buf = new byte[1024];
            long startTime = System.currentTimeMillis();
            Log.i("Throughput", "start time =" + startTime);
            for (i = 0; i < 150; i++) {

                URLConnection con = url.openConnection();

                //define inputStream to read from the URLConnection
                InputStream in = con.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(in);

                while ((red = bis.read(buf)) != -1) {
                    size += red;
                }
                long endTime = System.currentTimeMillis();

            }


            long endTime = System.currentTimeMillis();
            time = endTime - startTime;
            time1 = time;

            Log.i("Throughput", "end time : " + endTime);

            Log.i("Throughput", "size:" + size / 1024);
            Log.i("Throughput", "time:" + time1 / 1000);

            rate = (((size / 1024) * 8) / ((time1 - latency) / 1000));

        } catch (IOException e) {
            Log.d("Throughput", "download Error:" + e);
        }
        return rate;
    }

}


