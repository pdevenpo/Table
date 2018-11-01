package edu.osu.table;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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
import android.view.*;
import android.support.v7.widget.Toolbar;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
/**
 * Document Provided by Professor Adam Champion of The Ohio State University for use in
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
    public String bestSSID;
    public int bestDBM;



    /*
     * ************************************************************************
     * Declare a Broadcast Receiver that "responds" to Android system Intents.
     * In our case, we only want to display the results of a WiFi scan, which
     * are made available when the SCAN_RESULTS_AVAILABLE_ACTION fires (after
     * the scan is completed).
     * ************************************************************************
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        // Override onReceive() method to implement our custom logic.
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // Get the Intent action.
            String action = intent.getAction();

            // If the WiFi scan results are ready, iterate through them and
            // record the WiFi APs' SSIDs, BSSIDs, WiFi capabilities, radio
            // frequency, and signal strength (in dBm).
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
            {
                // Ensure WifiManager is not null first.
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

    /**
     * Inflate the Fragment view for Wi-Fi scans.
     *
     * @param inflater LayoutInflater that inflates XML view
     * @param container The parent view container
     * @param savedInstanceState Any previous saved state
     * @return Created Fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the appropriate view for ui
        View v = inflater.inflate(R.layout.fragment_wifi_scan, container, false);
        //Initialize buttons
        Button wifi_button = (Button) v.findViewById(R.id.connect_wifi);
        //onclick listener for wifi_button to connect to a new wifi
        Button wifi_button_5G = (Button) v.findViewById(R.id.connect_wifi_5G);

        wifi_button_5G.setOnClickListener(new View.OnClickListener(){
                                        @Override
                                        public void onClick(View view){

                                        }
                                          });

        wifi_button.setOnClickListener(new View.OnClickListener() {
                                            //-54 is a better signal than -80 dbm
                                            //2.4 or 5.5 better freq?
                                            //TODO force a wifi connection based on BSSID
                                           @Override
                                           public void onClick(View view) {
                                               WifiConfiguration config = new WifiConfiguration();
                                               config.allowedAuthAlgorithms.clear();
                                               config.allowedGroupCiphers.clear();
                                               config.allowedKeyManagement.clear();
                                               config.allowedPairwiseCiphers.clear();
                                               config.allowedProtocols.clear();
                                               config.SSID = "\"" + "WiFi@OSU" + "\"";
                                               config.BSSID = "6c:f3:7f:52:4e:a1"; // <--BSSID should be set without ->"<-
                                               List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
                                               for (WifiConfiguration existingConfig : existingConfigs)
                                               {
                                                   if (null != existingConfig && existingConfig.SSID.toString().equals("\"" + "osuwireless" + "\""))
                                                   {
                                                       mWifiManager.removeNetwork(existingConfig.networkId);
                                                   }
                                               }
                                               config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                                               int wcgID = mWifiManager.addNetwork(config);
                                               boolean b =  mWifiManager.enableNetwork(wcgID, true);

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
        if (!hideDialog) {
            Log.d(TAG, "Showing permission info dialog to user");
            FragmentManager fm = getActivity().getSupportFragmentManager();
            DialogFragment fragment = new NoticeDialogFragment();
            fragment.show(fm, "info_dialog");
        }

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
                if (!hasLocationPermission()) {
                    requestLocationPermission();
                }
                else {
                    doWifiScan();
                }
                return true;
            case R.id.action_back:
                // User chose the "Back" item, return to main activity...

                Intent intent = new Intent(WifiScanFragment.this.getActivity(), MainActivity.class);
                startActivity(intent);
                return true;

        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doWifiScan();
            }
            else {
                Log.e(TAG, "Error: Permission denied to read location");
                Toast.makeText(getActivity(), getResources().getString(R.string.read_location_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
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

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasLocationPermission()) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_LOCATION);
            }
        }
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
            ScanDatabase database = Room.databaseBuilder(getActivity().getApplicationContext(), ScanDatabase.class, "db-wifi.db")
                    .allowMainThreadQueries()   //Allows room to do operation on main thread
                    .build();
            ScanDao scanDao = database.scanDataDao();
            ScanData scanData = new ScanData();
            //set wirelessdata to all the wifi information
            wifiChanFreq = mScanResult.frequency;
            wifiMacAddress = mScanResult.BSSID;
            wifiRssDbm = mScanResult.level;
            wifiSecurity = mScanResult.capabilities;
            wifiSSID = mScanResult.SSID;

            //wirelessData.setSSID(wifiSSID);
            scanData.setId(wifiID);
            //wirelessData.setChanFreq(wifiChanFreq);
            scanData.setMAC_Address(wifiMacAddress);
            //wirelessData.setRSSdBm(wifiRssDbm);
            //.setSecurity(wifiSecurity);

            scanDao.insert(scanData);
            //Toast tester to ensure correct information
            //Toast.makeText(getContext(), hello , Toast.LENGTH_SHORT).show();

            String resultTextStr = "WIFI NAME: " + mScanResult.SSID + "; " + '\n' +
                    "BSSID: " + mScanResult.BSSID + "; " + '\n' +
                    "SECURITY: " + mScanResult.capabilities + "; " + '\n' +
                    "FREQUENCY: " + mScanResult.frequency + " MHz;" + '\n' +
                    "NOISE: " + mScanResult.level + " dBm";
            wifiID++;
            if(mScanResult.capabilities.contains("WPA")) {
                    mScanResultTextView.setTextColor(getResources().getColor(R.color.darkPrimaryColor));
                    mScanResultTextView.setText(resultTextStr);
            }else{
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
}
