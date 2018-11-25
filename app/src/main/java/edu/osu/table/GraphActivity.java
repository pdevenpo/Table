package edu.osu.table;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import edu.osu.table.ui.WirelessDataFolder.WirelessData;
import edu.osu.table.ui.WirelessDataFolder.WirelessDatabase;
import edu.osu.table.ui.WirelessScan.Wireless2Data;
import edu.osu.table.ui.WirelessScan.Wireless2Database;
import edu.osu.table.ui.graph.CustomXAxisRenderer;
import edu.osu.table.ui.graph.HourAxisValueBarAxisFormatter;
import edu.osu.table.ui.graph.HourAxisValueFormatter;
import edu.osu.table.ui.graph.SsidAxisFormatter;
import org.w3c.dom.Text;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.reverse;
import static java.lang.StrictMath.abs;

public class GraphActivity extends AppCompatActivity {

    private WirelessDatabase wireless_database = null;
    private Wireless2Database wireless2Database = null;
    private String TAG = "GraphActivity";
    private android.support.v7.widget.Toolbar graph_toolbar;

    private List<LineChart> chart = new ArrayList<LineChart>();
    private List<BarChart>  barCharts = new ArrayList<BarChart>();
    private List<LineChart> lineCharts = new ArrayList<LineChart>();
    private List<TextView>  textViews = new ArrayList<TextView>();
    private BarChart barChart1;

    private BarChart currentWiFiRssiBarChart;
    private LineChart currentWiFiLinkSpeedLineChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_activity);
        //getWindow().getDecorView().setBackgroundColor(Color.BLUE);
        graph_toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(graph_toolbar);
        wireless_database = WirelessDatabase.Companion.getInstance(this);
        wireless2Database = Wireless2Database.Companion.getInstance(this);

        //testDatabase();
        //testDatabase2();
        setupTextView();
        setupPieChart();
        setupBatteryGraph();
        setupPerSsidBarCharts();
        setupBarChart();
        setupNewBatteryChart();
        setupCurrentWiFiChart();
        Log.d(TAG, "On-create commands are done.");
    }

    private void setupCurrentWiFiChart() {
        currentWiFiLinkSpeedLineChart = findViewById(R.id.current_wifi_ap_link_speed_bar_graph);
        currentWiFiRssiBarChart = findViewById(R.id.current_wifi_ap_rssi_bar_graph);

        // Collect Current WiFi Data & Write to Database
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getConnectionInfo().getLinkSpeed() == -1)
        {
            currentWiFiRssiBarChart.setEnabled(false);
            currentWiFiRssiBarChart.setVisibility(View.GONE);
            currentWiFiLinkSpeedLineChart.setEnabled(false);
            currentWiFiLinkSpeedLineChart.setVisibility(View.GONE);
            return;
        }
        else
        {
            String ssidName = wifiManager.getConnectionInfo().getSSID();
            List<WirelessData> oneSsidData = new ArrayList<WirelessData>();
            oneSsidData = reverse(wireless_database.wirelessDataDao().getPerSSIDEntries(ssidName, 96));
            if (oneSsidData.size() == 0)
            {
                return;
            }
            populateCurrentAPBarCharts(oneSsidData, currentWiFiRssiBarChart, ssidName);
            populateCurrentAPLinkSpeedLineCharts(oneSsidData, currentWiFiLinkSpeedLineChart, ssidName);
        }

    }

    private void setupTextView() {
        textViews.add((TextView)findViewById(R.id.text_pie_graphs));
        textViews.add((TextView)findViewById(R.id.text_battery_line_graphs));
        textViews.add((TextView)findViewById(R.id.text_overall_battery_line_graphs));
        textViews.add((TextView)findViewById(R.id.text_RSSI_bar_line_graphs));
        textViews.add((TextView)findViewById(R.id.text_current_scan_rssi_graphs));
    }

    private void testDatabase2() {
        int i, j;
        Float[] allAccessPts_speed = {100.0f, 94.0f, 92.0f, 90.0f, 88.0f, //1
                80.0f, 75.0f, 79.0f, 75.0f, 73.0f,  //2
                71.0f, 68.0f, 65.0f, 63.0f, 58.0f,  //3
                70.0f, 75.0f, 80.0f, 85.0f, 90.0f,  //4
                100.0f, 95.0f, 93.0f, 91.0f, 88.0f}; //,  //5

        int[] allAccessPts_rssi = {10, 20, 30, 40, 30, 35, 23, 23, 45, //1
                                    -10, -20, -30, -40, -20, -10, -10, -20,
                                    -9, -1, -2, -3, -4, 5, 10, 10,
                                    -4, -10, -4, -3, -5, -6, -4, -5};

        String[] allAccessPts_mac = {"12:34:45:56:32:21", "ab:cd:ef:gh:67:32", "45:23:34:23:32:23", "34:er:sd:56:23:12",
                                    "12:34:23:12:34:34", "32:56:12:12:12:12", "67:34:23:56:23:45", "gb:sd:ef:as:58:34"};

        String[] allAccessPts_ssid = {"goku", "megumin", "sakuragi", "rukawa", "akagi", "sendoh", "mitsui", "ryouta"};    //8

        for (j = 0; j<4; j++)
        {
            Wireless2Data newScan = new Wireless2Data();
            newScan.setCurDate(System.currentTimeMillis());
            for (i=0; i<allAccessPts_ssid.length; i++)
            {

                newScan.setBatteryPerc(10);
                newScan.setRSSdBm(allAccessPts_rssi[8*j + i]);
                newScan.setMAC_Address(allAccessPts_mac[i]);
                newScan.setSSID(allAccessPts_ssid[i]);
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                wireless2Database.wireless2DataDao().insert(newScan);
            }
        }
        Log.d(TAG, "Test information has been added.");

    }

    private void testDatabase() {
        int i;
        Float[] allAccessPts_battery = {100.0f, 94.0f, 92.0f, 90.0f, 88.0f, //1
                                        80.0f, 75.0f, 79.0f, 75.0f, 73.0f,  //2
                                        71.0f, 68.0f, 65.0f, 63.0f, 58.0f,  //3
                                        70.0f, 75.0f, 80.0f, 85.0f, 90.0f,  //4
                                        100.0f, 95.0f, 93.0f, 91.0f, 88.0f}; //,  //5
                /*
                                        87.0f, 86.0f, 85.0f, 84.0f, 83.0f,  //6
                                        82.0f, 81.0f, 80.0f, 79.0f, 78.0f,  //7
                                        76.0f, 74.0f, 73.0f, 72.0f, 71.0f,  //8
                                        68.0f, 65.0f, 64.0f, 62.0f, 61.0f
                                        };*/
        String[] allAccessPts_ssid = {"goku", "goku", "megumin", "megumin", "sakuragi",  //1
                                    "sakuragi", "sakuragi", "goku", "goku", "sakuragi",
                                    "megumin", "sakuragi", "rukawa", "rukawa", "rukawa",
                                    "rukawa", "rukawa", "rukawa", "sakuragi", "sakuragi",
                                    "sakuragi", "megumin", "megumin", "goku", "goku"};//,    //5
        /*
                                    "goku", "goku", "goku", "goku", "goku",
                                    "goku", "goku", "goku", "goku", "goku",
                                    "sakuragi", "sakuragi", "sakuragi", "sakuragi", "sakuragi",   //8
                                    "sakuragi", "sakuragi", "sakuragi", "sakuragi", "sakuragi"};*/
        for (i=0; i<allAccessPts_battery.length; i++)
        {
            WirelessData newBattery = new WirelessData();
            newBattery.setBatteryPerc(allAccessPts_battery[i]/100);
            newBattery.setSSID(allAccessPts_ssid[i]);
            newBattery.setCurDate(System.currentTimeMillis());
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wireless_database.wirelessDataDao().insert(newBattery);
        }
        Log.d(TAG, "Test information has been added.");
    }

    private void setupPieChart() {
        int i, j, val;
        Float batteryConsperAP = 0f;
        Float tempbatteryConsperAP = 0f;
        boolean first_match = false;
        Typeface tf;
        int maxVal, maxidx;

        //tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        List<WirelessData> onedaydbdata = new ArrayList<WirelessData>();
        onedaydbdata = reverse(wireless_database.wirelessDataDao().getAllBattery(96));
        List<String> allAccessPts_ssid = new ArrayList<String>();
        //String[] allAccessPts_ssid = new String[96];
        Float[] allAccessPts_battery = new Float[96];
        for (i=0; i<onedaydbdata.size(); i++)
        {
            allAccessPts_ssid.add(onedaydbdata.get(i).getSSID());
            //allAccessPts_ssid[i] = onedaydbdata.get(i).getSSID();
        }
        String[] unique = new HashSet<String>(allAccessPts_ssid).toArray(new String[0]);
        List<Integer> freqList = new ArrayList<Integer>();
        for (String key: unique)
        {
            freqList.add(Collections.frequency(allAccessPts_ssid, key));
        }
        for (i=0; i<unique.length; i++)
        {
            batteryConsperAP = 0f;
            tempbatteryConsperAP = 0f;
            first_match = false;
            for (j=0; j<onedaydbdata.size(); j++)
            {
                if (onedaydbdata.get(j).getSSID().equals(unique[i])) {
                    if (first_match)
                    {
                        batteryConsperAP = batteryConsperAP + tempbatteryConsperAP - onedaydbdata.get(j).getBatteryPerc();
                    }
                    first_match = true;
                    tempbatteryConsperAP = onedaydbdata.get(j).getBatteryPerc();
                }
                else {
                    if (first_match)
                    {
                        batteryConsperAP = batteryConsperAP + tempbatteryConsperAP - onedaydbdata.get(j).getBatteryPerc();
                        first_match = false;
                        tempbatteryConsperAP = 0f;
                    }
                }
            }
            allAccessPts_battery[i] = batteryConsperAP;
        }
        List<PieEntry> pieEntriesDischarge = new ArrayList<>();
        List<PieEntry> pieEntriesCharge = new ArrayList<>();
        for (i=0; i<unique.length; i++)
        {
            if (allAccessPts_battery[i]>0)
            {
                pieEntriesDischarge.add(new PieEntry(abs(allAccessPts_battery[i]), unique[i]));
            }
            else
            {
                pieEntriesCharge.add(new PieEntry(abs(allAccessPts_battery[i]), unique[i]));
            }
        }
        PieDataSet dataSetDischarge = new PieDataSet(pieEntriesDischarge, "Battery consumption per Wi-Fi");
        PieDataSet dataSetCharge = new PieDataSet(pieEntriesCharge, "Battery charged per Wi-Fi");

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSetCharge.setColors(colors);

        dataSetDischarge.setColors(ColorTemplate.COLORFUL_COLORS);
        //dataSetCharge.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData dataDischarge = new PieData(dataSetDischarge);
        PieData dataCharge = new PieData(dataSetCharge);

        //dataCharge.setValueTextSize(11f);
        dataCharge.setValueTextColor(Color.BLACK);
        dataDischarge.setValueTextColor(Color.BLACK);

        //dataCharge.setValueTypeface(tf);

        PieChart battery_pie_chart_discharge = (PieChart) findViewById(R.id.battery_pie_chart_discharge);
        PieChart battery_pie_chart_charge = (PieChart) findViewById(R.id.battery_pie_chart_charge);

        battery_pie_chart_discharge.setData(dataDischarge);
        battery_pie_chart_charge.setData(dataCharge);

        battery_pie_chart_discharge.animateY(1000);
        battery_pie_chart_charge.animateY(1000);

        battery_pie_chart_discharge.setCenterText("Discharged");
        battery_pie_chart_charge.setCenterText("Charged");

        battery_pie_chart_charge.getDescription().setEnabled(false);
        battery_pie_chart_discharge.getDescription().setEnabled(false);

        battery_pie_chart_charge.setUsePercentValues(true);
        battery_pie_chart_discharge.setUsePercentValues(true);

        //battery_pie_chart_charge.getLegend().setTextColor(Color.BLACK);
        battery_pie_chart_discharge.invalidate();
        battery_pie_chart_charge.invalidate();
        Log.d(TAG, "Pie Chart has been created");


        LineChart iterChart;
        chart.add((LineChart) findViewById(R.id.battery_first_wifi));
        chart.add((LineChart) findViewById(R.id.battery_second_wifi));
        chart.add((LineChart) findViewById(R.id.battery_third_wifi));

        for (i = 0; i<3; i++) {
            if (freqList.size()>i) {
                maxVal = Collections.max(freqList);
                maxidx = freqList.indexOf(maxVal);
                iterChart = chart.get(i);
                populateLineCharts(iterChart, unique[maxidx]);

                freqList.set(maxidx, 0);
            }
            else {
                chart.get(i).setEnabled(false);
                chart.get(i).setVisibility(View.GONE);
            }
        }
    }

    private void setupPerSsidBarCharts(){
        int i;
        int maxVal, maxidx, maxLength;
        List<Wireless2Data> onedaydbdata = new ArrayList<Wireless2Data>();
        onedaydbdata = reverse(wireless2Database.wireless2DataDao().getAllBattery(500));

        List<String> allAccessPts_ssid = new ArrayList<String>();
        //String[] allAccessPts_ssid = new String[96];
        List<Integer> allAccessPts_rssi = new ArrayList<Integer>();

        for (i=0; i<onedaydbdata.size(); i++)
        {
            allAccessPts_ssid.add(onedaydbdata.get(i).getSSID());
            //allAccessPts_ssid[i] = onedaydbdata.get(i).getSSID();
        }
        String[] unique = new HashSet<String>(allAccessPts_ssid).toArray(new String[0]);

        // Find the best performing APs
        maxLength = (unique.length>96) ? 96 : unique.length;
        for (i=0; i<maxLength; i++)
        {
            allAccessPts_rssi.add(0);
            for (Wireless2Data oneAPdata: onedaydbdata)
            {
                if (oneAPdata.getSSID().equals(unique[i]))
                {
                    allAccessPts_rssi.set(i, allAccessPts_rssi.get(i) + oneAPdata.getRSSdBm());
                }
            }
        }

        BarChart iterBarChart;
        barCharts.add((BarChart)findViewById(R.id.wifi_per_ap_bar_graph1));
        barCharts.add((BarChart)findViewById(R.id.wifi_per_ap_bar_graph2));
        barCharts.add((BarChart)findViewById(R.id.wifi_per_ap_bar_graph3));

        LineChart iterLineChart;
        lineCharts.add((LineChart)findViewById(R.id.wifi_speed_per_ap_line_graph1));
        lineCharts.add((LineChart)findViewById(R.id.wifi_speed_per_ap_line_graph2));
        lineCharts.add((LineChart)findViewById(R.id.wifi_speed_per_ap_line_graph3));

        for (i = 0; i<3; i++) {
            if (allAccessPts_rssi.size()>i) {
                maxVal = Collections.max(allAccessPts_rssi);
                maxidx = allAccessPts_rssi.indexOf(maxVal);

                List<Wireless2Data> singlessiddata = new ArrayList<Wireless2Data>();

                singlessiddata = reverse(wireless2Database.wireless2DataDao().getPerSSIDEntries(unique[maxidx], 96));
                if (singlessiddata.size() == 0)
                {
                    return;
                }
                iterBarChart = barCharts.get(i);
                populateBarCharts(singlessiddata, iterBarChart, unique[maxidx]);
                iterLineChart = lineCharts.get(i);
                populateLinkSpeedLineCharts(singlessiddata, iterLineChart, unique[maxidx]);
                allAccessPts_rssi.set(maxidx, -1000000);
            }
            else {
                barCharts.get(i).setEnabled(false);
                barCharts.get(i).setVisibility(View.GONE);

                lineCharts.get(i).setEnabled(false);
                lineCharts.get(i).setVisibility(View.GONE);
            }
        }
    }

    private void populateCurrentAPLinkSpeedLineCharts(List<WirelessData> singlessiddata, final LineChart iterLineChart, String ssidName) {
        int k;

        ArrayList<Entry> values = new ArrayList<>();
        Long referenceTime = singlessiddata.get(0).getCurDate();
        for (k=0; k<singlessiddata.size(); k++) {
            values.add(new Entry(singlessiddata.get(k).getCurDate() - referenceTime, singlessiddata.get(k).getLinkSpeed()));
        }

        LineDataSet set1;
        set1 = new LineDataSet(values, ssidName);

        setLineDataSetStyle(set1, iterLineChart, referenceTime);

        // Y-Axis Style
        yAxisStyleSetupLinkSpeed(iterLineChart);

        set1.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return iterLineChart.getAxisLeft().getAxisMinimum();
            }
        });

        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
            set1.setFillDrawable(drawable);
        } else {
            set1.setFillColor(Color.BLACK);
        }

        ArrayList<ILineDataSet> dataSet1 = new ArrayList<>();
        dataSet1.add(set1);

        LineData data = new LineData(dataSet1);

        iterLineChart.getDescription().setText("Link Speed per Wi-Fi AP");

        iterLineChart.setData(data);
    }

    private void populateCurrentAPBarCharts(List<WirelessData> singlessiddata, BarChart iterBarChart, String ssidName) {
        int k;

        ArrayList<BarEntry> values = new ArrayList<>();
        List<Long> referenceTime = new ArrayList<Long>();
        WirelessData aScanData = new WirelessData();
        for (k=0; k<singlessiddata.size(); k++)
        {
            aScanData = singlessiddata.get(k);
            values.add(new BarEntry(k, aScanData.getRSSdBm()));
            referenceTime.add(aScanData.getCurDate());
        }

        BarDataSet barDataSet;
        barDataSet = new BarDataSet(values, ssidName);

        barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        barDataSet.setDrawValues(false);

        setBarDataSetChartStyle(barDataSet, iterBarChart, referenceTime);

        ArrayList<IBarDataSet> iBarDataSets = new ArrayList<>();
        iBarDataSets.add(barDataSet);

        BarData data = new BarData(iBarDataSets);
        iterBarChart.getDescription().setText(ssidName);
        iterBarChart.setData(data);
        iterBarChart.setFitBars(true);
        iterBarChart.invalidate();
    }

    private void populateLinkSpeedLineCharts(List<Wireless2Data> singlessiddata, final LineChart iterLineChart, String ssidName) {
        int k;
        LineData lineData = new LineData();
        ArrayList<Entry> values = new ArrayList<>();
        Long referenceTime = singlessiddata.get(0).getCurDate();
        for (k=0; k<singlessiddata.size(); k++) {
            values.add(new Entry(singlessiddata.get(k).getCurDate() - referenceTime, singlessiddata.get(k).getLinkSpeed()));
        }

        LineDataSet set1;
        set1 = new LineDataSet(values, ssidName);

        setLineDataSetStyle(set1, iterLineChart, referenceTime);

        // Y-Axis Style
        yAxisStyleSetupLinkSpeed(iterLineChart);

        set1.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return iterLineChart.getAxisLeft().getAxisMinimum();
            }
        });

        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
            set1.setFillDrawable(drawable);
        } else {
            set1.setFillColor(Color.BLACK);
        }

        ArrayList<ILineDataSet> dataSet1 = new ArrayList<>();
        dataSet1.add(set1);

        LineData data = new LineData(dataSet1);

        iterLineChart.getDescription().setText("Link Speed per Wi-Fi AP");

        iterLineChart.setData(data);
    }

    private void populateBarCharts(List<Wireless2Data> singlessiddata, BarChart iterBarChart, String ssidName) {
        int k;

        ArrayList<BarEntry> values = new ArrayList<>();
        List<Long> referenceTime = new ArrayList<Long>();
        Wireless2Data aScanData = new Wireless2Data();
        for (k=0; k<singlessiddata.size(); k++)
        {
            aScanData = singlessiddata.get(k);
            values.add(new BarEntry(k, aScanData.getRSSdBm()));
            referenceTime.add(aScanData.getCurDate());
        }

        BarDataSet barDataSet;
        barDataSet = new BarDataSet(values, ssidName);

        barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        barDataSet.setDrawValues(false);

        setBarDataSetChartStyle(barDataSet, iterBarChart, referenceTime);

        ArrayList<IBarDataSet> iBarDataSets = new ArrayList<>();
        iBarDataSets.add(barDataSet);

        BarData data = new BarData(iBarDataSets);
        iterBarChart.getDescription().setText(ssidName);
        iterBarChart.setData(data);
        iterBarChart.setFitBars(true);
        iterBarChart.invalidate();
    }

    private void setBarDataSetChartStyle(BarDataSet barDataSet, BarChart iterBarChart, List<Long> referenceTime) {
        setBarChartStyle(iterBarChart, referenceTime);
    }

    private void setBarChartStyle(BarChart iterBarChart, List<Long> referenceTime) {
        iterBarChart.setMaxVisibleValueCount(60);

        iterBarChart.setPinchZoom(false);

        iterBarChart.setDrawBarShadow(false);
        iterBarChart.setDrawGridBackground(false);

        HourAxisValueBarAxisFormatter xAxisFormatter = new HourAxisValueBarAxisFormatter(referenceTime);
        XAxis xAxis = iterBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(xAxisFormatter);

        // Setting values
        iterBarChart.animateY(1500);
    }

    private void populateLineCharts(final LineChart chart, String ssidName) {
        int k;
        List<WirelessData> singlessiddata = new ArrayList<WirelessData>();
        singlessiddata = reverse(wireless_database.wirelessDataDao().getPerSSIDEntries(ssidName, 96));

        if (singlessiddata.size() == 0)
        {
            return;
        }
        //chart = findViewById(R.id.battery_first_wifi);
        LineData lineData = new LineData();
        ArrayList<Entry> values = new ArrayList<>();
        Long referenceTime = singlessiddata.get(0).getCurDate();
        for (k=0; k<singlessiddata.size(); k++) {
            values.add(new Entry(singlessiddata.get(k).getCurDate() - referenceTime, 100*singlessiddata.get(k).getBatteryPerc()));
        }

        LineDataSet set1;
        set1 = new LineDataSet(values, ssidName);

        setLineDataSetStyle(set1, chart, referenceTime);

        // Y-Axis style
        yAxisStyleSetupBattery(chart);

        set1.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return chart.getAxisLeft().getAxisMinimum();
            }
        });

        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
            set1.setFillDrawable(drawable);
        } else {
            set1.setFillColor(Color.BLACK);
        }

        ArrayList<ILineDataSet> dataSet1 = new ArrayList<>();
        dataSet1.add(set1);

        LineData data = new LineData(dataSet1);

        chart.getDescription().setText("Battery consumption per Wi-Fi AP");
        //chart.getDescription().setPosition(50f, 50f);

        //chart.setDrawGridBackground(false);

        chart.setData(data);

        //MyMarkerView myMarkerView = new MyMarkerView(this, R.layout.custom_marker_view_layout, referenceTime);
        //myMarkerView.setChartView(chart);
        //chart.setMarker(myMarkerView);
    }

    private void setLineDataSetStyle(LineDataSet set1, LineChart chart, Long referenceTime) {
        set1.setDrawIcons(false);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);

        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);

        set1.setDrawCircleHole(false);

        set1.setValueTextSize(9f);

        // set the filled area
        set1.setDrawFilled(true);

        chart.animateX(1500);
        chart.setBackgroundColor(Color.WHITE);
        chart.setTouchEnabled(true);

        HourAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(referenceTime);
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLimitLinesBehindData(true);
    }

    private void yAxisStyleSetupBattery(LineChart chart){

        YAxis yAxis = chart.getAxisLeft();
        yAxis.enableGridDashedLine(10f, 10f, 0f);

        // Range
        yAxis.setAxisMaximum(100f);
        yAxis.setAxisMinimum(0f);

        LimitLine ll1 = new LimitLine(80f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        ll1.setLineColor(Color.BLUE);
        //ll1.setTypeface(tfRegular);

        LimitLine ll2 = new LimitLine(40f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        ll2.setLineColor(Color.BLUE);
        //ll2.setTypeface(tfRegular);

        // draw limit lines behind data instead of on top
        yAxis.setDrawLimitLinesBehindData(true);

        // add limit lines
        yAxis.addLimitLine(ll1);
        yAxis.addLimitLine(ll2);
    }

    private void yAxisStyleSetupLinkSpeed(LineChart chart){

        YAxis yAxis = chart.getAxisLeft();
        yAxis.enableGridDashedLine(10f, 10f, 0f);

        // Range

        // draw limit lines behind data instead of on top
        yAxis.setDrawLimitLinesBehindData(true);

        // add limit lines
    }

    private void setupNewBatteryChart() {
        final LineChart graph = findViewById(R.id.battery_new_graph);
        List<WirelessData> alldbdata = new ArrayList<WirelessData>();

        alldbdata = wireless_database.wirelessDataDao().getAllBattery(500);
        if (alldbdata.size() == 0)
        {
            Toast.makeText(this, "No data to show.", Toast.LENGTH_LONG).show();
            return;
        }

        LineData lineData = new LineData();
        ArrayList<Entry> values = new ArrayList<>();
        Long referenceTime = alldbdata.get(0).getCurDate();
        LineDataSet set1;
        LineGraphSeries<DataPoint> batterypnts = new LineGraphSeries<DataPoint>();
        for (WirelessData dbdata:reverse(alldbdata))
        {
            Log.d(TAG, "time : " + dbdata.getCurDate() +" battery Percentage: " + dbdata.getBatteryPerc());
            values.add(new Entry(dbdata.getCurDate()- referenceTime, 100*dbdata.getBatteryPerc()));
        }

        set1 = new LineDataSet(values, "Overall");
        setLineDataSetStyle(set1, graph, referenceTime);

        set1.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return graph.getAxisLeft().getAxisMinimum();
            }
        });

        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_diff);
            set1.setFillDrawable(drawable);
        } else {
            set1.setFillColor(Color.BLACK);
        }

        ArrayList<ILineDataSet> dataSet1 = new ArrayList<>();
        dataSet1.add(set1);

        LineData data = new LineData(dataSet1);

        graph.getDescription().setText("Battery consumption");

        //chart.setDrawGridBackground(false);

        graph.setData(data);

    }

    private void setupBatteryGraph() {
        GraphView graph = findViewById(R.id.battery_graph);
        if (true)
        {
            graph.setVisibility(View.GONE);
            return;
        }
        List<WirelessData> alldbdata = new ArrayList<WirelessData>();

        alldbdata = wireless_database.wirelessDataDao().getAllBattery(500);
        LineGraphSeries<DataPoint> batterypnts = new LineGraphSeries<DataPoint>();
        for (WirelessData dbdata:reverse(alldbdata))
        {
            Log.d(TAG, "time : " + dbdata.getCurDate() +" battery Percentage: " + dbdata.getBatteryPerc());
            Date date = new Date(dbdata.getCurDate());
            Float batterypct = 100*dbdata.getBatteryPerc();
            batterypnts.appendData(new DataPoint(date, batterypct), true, 500);
        }
        Log.d(TAG, "Retrieving from DB has happened");
        graph.removeAllSeries();
        graph.addSeries(batterypnts);
        graph.setTitle("Battery Consumption");
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setLabelsSpace(1);
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Battery Percentage");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time");
        Log.d(TAG, "Graph has been created.");
    }

    private void setupBarChart() {
        int i;
        barChart1 = findViewById(R.id.wifi_ap_bar_graph);
        List<Wireless2Data> rssData = new ArrayList<Wireless2Data>();
        List<Wireless2Data> linkSpeedData = new ArrayList<Wireless2Data>();
        List<Long> recentScan = wireless2Database.wireless2DataDao().getDates(1);
        if (recentScan.size() == 0)
        {
            barChart1.setVisibility(View.GONE);
            return;
        }
        rssData = wireless2Database.wireless2DataDao().getSsidAndRss(recentScan.get(0));
        if (rssData.size() == 0)
        {
            return;
        }
        List<String> allAccessPts_ssid = new ArrayList<String>();
        List<String> allAccessPts_mac = new ArrayList<String>();

        Float[] allAccessPts_battery = new Float[96];
        int maxVal = rssData.size()>6?6:rssData.size();
        for (i=0; i<maxVal; i++)
        {
            allAccessPts_ssid.add(rssData.get(i).getSSID());
            allAccessPts_mac.add(rssData.get(i).getMAC_Address());
            Log.d(TAG, "SSID is " + allAccessPts_ssid.get(i) + " MAC is "+ allAccessPts_mac.get(i));
            //allAccessPts_ssid[i] = onedaydbdata.get(i).getSSID();
        }
        String[] unique = new HashSet<String>(allAccessPts_ssid).toArray(new String[0]);

        ArrayList<BarEntry> values = new ArrayList<>();

        for (i=0; i<maxVal; i++)
        {
            values.add(new BarEntry(i, rssData.get(i).getRSSdBm()));
        }

        BarDataSet barDataSet;
        barDataSet = new BarDataSet(values, "Recent Scan results.");

        barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        //barDataSet.setDrawValues(false);

        ArrayList<IBarDataSet> iBarDataSets = new ArrayList<>();
        iBarDataSets.add(barDataSet);

        BarData data = new BarData(iBarDataSets);

        barChart1.getDescription().setText("RSSI comparison");
        barChart1.getDescription().setPosition(440f, 12f);
        barChart1.setMaxVisibleValueCount(60);

        barChart1.setPinchZoom(false);

        barChart1.setDrawBarShadow(false);
        barChart1.setDrawGridBackground(false);

        Legend l = barChart1.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        //barChart1.getLegend().setEnabled(false);

        XAxis xAxis = barChart1.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        barChart1.setXAxisRenderer(new CustomXAxisRenderer(barChart1.getViewPortHandler(), barChart1.getXAxis(), barChart1.getTransformer(YAxis.AxisDependency.LEFT)));
        SsidAxisFormatter xAxisFormatter = new SsidAxisFormatter(allAccessPts_ssid, allAccessPts_mac);
        barChart1.setViewPortOffsets(0,0,0, 72f);
        xAxis.setValueFormatter(xAxisFormatter);

        // Setting values
        barChart1.setData(data);
        barChart1.setFitBars(true);
        barChart1.invalidate();

        barChart1.animateY(1500);
        Log.d(TAG, "Bar graph has been setup.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(myIntent);
                return true;
            case R.id.action_menu:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
