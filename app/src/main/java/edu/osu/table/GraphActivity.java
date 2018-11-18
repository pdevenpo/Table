package edu.osu.table;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import edu.osu.table.ui.WirelessData.WirelessDao;
import edu.osu.table.ui.WirelessData.WirelessData;
import edu.osu.table.ui.WirelessData.WirelessDatabase;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.reverse;
import static java.lang.StrictMath.abs;

public class GraphActivity extends AppCompatActivity {

    private WirelessDatabase wireless_database = null;
    private String TAG = "GraphActivity";
    private android.support.v7.widget.Toolbar graph_toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_activity);
        //getWindow().getDecorView().setBackgroundColor(Color.BLUE);
        graph_toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(graph_toolbar);
        wireless_database = WirelessDatabase.Companion.getInstance(this);
        //testDatabase();
        setupBatteryGraph();
        setupPieChart();

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
        int i, j;
        Float batteryConsperAP = 0f;
        Float tempbatteryConsperAP = 0f;
        boolean first_match = false;
        Typeface tf;

        //tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        List<WirelessData> onedaydbdata = new ArrayList<WirelessData>();
        onedaydbdata = reverse(wireless_database.wirelessDataDao().getAllBattery(96));
        String[] allAccessPts_ssid = new String[96];
        Float[] allAccessPts_battery = new Float[96];
        for (i=0; i<onedaydbdata.size(); i++)
        {
            allAccessPts_ssid[i] = onedaydbdata.get(i).getSSID();
        }
        String[] unique = new HashSet<String>(Arrays.asList(allAccessPts_ssid)).toArray(new String[0]);
        for (i=0; i<unique.length-1; i++)
        {
            batteryConsperAP = 0f;
            tempbatteryConsperAP = 0f;
            first_match = false;
            for (j=0; j<onedaydbdata.size(); j++)
            {
                if (onedaydbdata.get(j).getSSID().equals(unique[i+1])) {
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
        for (i=0; i<unique.length-1; i++)
        {
            if (allAccessPts_battery[i]>0)
            {
                pieEntriesDischarge.add(new PieEntry(abs(allAccessPts_battery[i]), unique[i+1]));
            }
            else
            {
                pieEntriesCharge.add(new PieEntry(abs(allAccessPts_battery[i]), unique[i+1]));
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
