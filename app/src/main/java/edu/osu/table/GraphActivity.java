package edu.osu.table;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import edu.osu.table.ui.WirelessData.WirelessDao;
import edu.osu.table.ui.WirelessData.WirelessData;
import edu.osu.table.ui.WirelessData.WirelessDatabase;
import edu.osu.table.ui.graph.HourAxisValueFormatter;
import edu.osu.table.ui.graph.MyMarkerView;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.reverse;
import static java.lang.StrictMath.abs;

public class GraphActivity extends AppCompatActivity {

    private WirelessDatabase wireless_database = null;
    private String TAG = "GraphActivity";
    private android.support.v7.widget.Toolbar graph_toolbar;

    private List<LineChart> chart = new ArrayList<LineChart>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_activity);
        //getWindow().getDecorView().setBackgroundColor(Color.BLUE);
        graph_toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(graph_toolbar);
        wireless_database = WirelessDatabase.Companion.getInstance(this);
        //testDatabase();
        setupPieChart();
        setupBatteryGraph();
        setupNewBatteryChart();
        Log.d(TAG, "On-create commands are done.");
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
        xAxis.setDrawLimitLinesBehindData(true);

        // add limit lines
        yAxis.addLimitLine(ll1);
        yAxis.addLimitLine(ll2);
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
