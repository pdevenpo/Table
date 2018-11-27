package edu.osu.table.ui.graph;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import edu.osu.table.R;
import edu.osu.table.ui.WirelessDataFolder.WirelessData;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.reverse;

public class GraphMainActivityChart {

    LineChart graph;
    Context context;
    List<WirelessData> alldbdata;

    public GraphMainActivityChart(Context context, LineChart graph, List<WirelessData> alldbdata) {
        this.graph = graph;
        this.context = context;
        this.alldbdata = alldbdata;
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

        YAxis yAxis = chart.getAxisLeft();
        yAxis.enableGridDashedLine(10f, 10f, 0f);

        // Range
        yAxis.setAxisMaximum(100f);
        yAxis.setAxisMinimum(0f);

        LimitLine ll1 = new LimitLine(80f, "80%");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        ll1.setLineColor(Color.BLUE);
        //ll1.setTypeface(tfRegular);

        LimitLine ll2 = new LimitLine(20f, "20%");
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

    public void setupNewBatteryChart() {

        if (alldbdata.size() == 0)
        {
            Toast.makeText(context, "No data to show.", Toast.LENGTH_LONG).show();
            return;
        }

        LineData lineData = new LineData();
        ArrayList<Entry> values = new ArrayList<>();
        Long referenceTime = alldbdata.get(0).getCurDate();
        LineDataSet set1;
        LineGraphSeries<DataPoint> batterypnts = new LineGraphSeries<DataPoint>();
        for (WirelessData dbdata:reverse(alldbdata))
        {
            //Log.d(TAG, "time : " + dbdata.getCurDate() +" battery Percentage: " + dbdata.getBatteryPerc());
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
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.fade_diff);
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
}
