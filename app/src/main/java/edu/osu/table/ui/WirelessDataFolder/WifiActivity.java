package edu.osu.table.ui.WirelessDataFolder;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import edu.osu.table.R;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import com.github.mikephil.charting.charts.PieChart;
import edu.osu.table.ui.graph.HourAxisValueFormatter;

import static com.google.common.collect.Lists.reverse;

public class WifiActivity extends AppCompatActivity {
    private Button btn_ping;
    private Button btn_download;
    private TextView pingtime;
    private TextView speed;
    private long l, total;
    private Handler handler = new Handler();
    private TextView download_speed;
    private WirelessDatabase wireless_database = null;
    private float[] yDATA = new float[96];
    private String[] xDATA = new String[96];

    PieChart pieChart;
    LineChart lineChart;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        pingtime = (TextView)findViewById(R.id.pinglatency);
        btn_download = (Button)findViewById(R.id.buttonDownload);

        speed = (TextView)findViewById(R.id.speedDownload);
        btn_ping = (Button)findViewById(R.id.buttonPing);

        download_speed =(TextView)findViewById(R.id.speed);


        pieChart = (PieChart)findViewById(R.id.wificonnection);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.getDescription().setEnabled(false);

        lineChart = (LineChart)findViewById(R.id.wifithroughput);


        wireless_database = WirelessDatabase.Companion.getInstance(this);


        total = TrafficStats.getTotalRxBytes();
        handler.postDelayed(runnable, 1000);


        btn_ping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float value = getdelay();
                String value1 = Float.toString(value);
                pingtime.setText(value1 + "ms");
            }
        });

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        float rate = download();

                        int aux = (int)(rate*100);//1243
                        final double result = aux/100d;


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                speed.setText(result+ " Mbps");

                            }
                        });
                    }

                }).start();


            }
        });


        getdataandplot();
        plotlinechart();
    }



    private void addDataSet() {
        Log.d("plotting", "addDataSet Started");
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for (int i = 0; i < yDATA.length; i++) {
            yEntrys.add(new PieEntry(yDATA[i], i));
        }

        for (int i = 0; i < xDATA.length; i++) {
            xEntrys.add(xDATA[i]);
        }

        PieDataSet pieDataSet = new PieDataSet(yEntrys, "issd:");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(20);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        colors.add(Color.DKGRAY);
        pieDataSet.setColors(colors);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }



    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            l = TrafficStats.getTotalRxBytes() - total;
            total += l;
            Log.i("speed", "download speed: " + ((l / 1024)*8) + "kb/s");
            handler.postDelayed(runnable, 1000);
            download_speed.setText("Download Speed:" + ((l / 1024)*8) + " kbps");
        }
    };


    public static float getdelay(){
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
            Log.i("Throughput","time=" + time[0]);
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
            Log.i("Throughput","result = " + result);
        }
        return value;
    }


    public static float download(){
        float rate = 0;
        float latency = getdelay();
        int i;
        String download_url = "https://lh3.googleusercontent.com/upeaGdkSJ_2rr4vmYb8xND5r15UGwcnJr1MBQW8W7VFxJclJ7w1VxH-Fv_OboqrPVtxY-ASxPgWhyqRUHTQFbVIX54RNpXTGEitkgQ=w1440";

        try{
            URL url = new URL(download_url);
            float red = 0;
            float size = 0;
            long time;
            float time1;
            byte[] buf = new byte[1024];
            long startTime = System.currentTimeMillis();
            Log.i("Throughput","start time ="+ startTime);
            for (i = 0; i<150 ; i++){

                URLConnection con = url.openConnection();

                //define inputStream to read from the URLConnection
                InputStream in = con.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(in);

                while ((red = bis.read(buf)) != -1){
                    size += red;
                }


            }


            long endTime = System.currentTimeMillis();
            time = endTime-startTime;
            time1 = time;

            Log.i("Throughput","end time : "+ endTime);
            Log.i("Throughput","size:"+size/1024);
            Log.i("Throughput","time:"+time1/1000);

            rate = (((size/1024)*8)/((time1-75*latency)/1000));


        }
        catch (IOException e){
            Log.d("Throughput","download Error:" + e);
        }
        return rate/1000;
    }


    public void getdataandplot(){
        List<WirelessData> onedaydbdata = new ArrayList<WirelessData>();
        onedaydbdata = reverse(wireless_database.wirelessDataDao().getAllBattery(96));
        //List<String> allAccessPts_ssid = new ArrayList<String>();
        String arr[] = new String[96];
        final String data[] = new String[96];
        float numer[] =new float[96];

        int count=0;
        int i,j;
        int in = 0;
        boolean isEqual = false;

        for (i=0; i< onedaydbdata.size(); i++)
        {
            Log.i("VALUE", Integer.toString(i));
            String ssid =onedaydbdata.get(i).getSSID();
            arr[i]=ssid;
            //allAccessPts_ssid.add(ssid);
            Log.i("value arr:",arr[i]);
        }

        for (i=0; i<96 ; i++){  //arr
            if(i==0){
                data[i]=arr[i]; // first array to data
                in++;
                Log.i("Freq",data[i]);
            }
            for(j=0;j<data.length;j++){
                if(data[j] == null || arr[i] == null){
                    break;
                }
                isEqual = arr[i].equals(data[j]);
                if(isEqual){
                    break;
                }
            }
            if(!isEqual){
                data[in]=arr[i];
                Log.i("Freq",data[in]);
                in++;
            }
        }

        for (i=0;i<data.length;i++){
            count=0;
            for(j=0;j<arr.length;j++){
                if(data[j] == null || arr[i] == null){
                    break;
                }
                if(arr[j].equals(data[i])==true){
                    count=count+1;
                }
            }
            if(count!=0) {
                numer[i] = count;
                Log.i("VALUE I", String.valueOf(i));
                Log.i("VALUE number", String.valueOf(numer[i]));
            }
        }

        for (i=0;i<data.length;i++){
            xDATA[i]=data[i];
            float round = (float)(Math.round(numer[i]/96*100)*10000/10000);
            yDATA[i]=round;
        }

        addDataSet();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d("VALUE","value select from chart");
                Log.d("VALUE",e.toString());
                Log.d("VALUE",h.toString());

                int pos1 = e.toString().indexOf("x: 0.0 y: ");
                String ssid = e.toString().substring(pos1+10);

                for(int m =0;m <yDATA.length;m++){
                    if(yDATA[m]==Float.parseFloat(ssid)){
                        pos1 = m;
                        break;
                    }

                }


                String SSID = xDATA[pos1];
                Toast.makeText(WifiActivity.this, "SSID: "+ SSID +"\n"+"Time: "+ssid+"%", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }


    public void plotlinechart(){

        List<WirelessData> onedaydbdata;
        onedaydbdata = reverse(wireless_database.wirelessDataDao().getAllBattery(96));
        float throughput[] = new float[96];
        long date[] = new long[96];
        Long referenceTime = onedaydbdata.get(0).getCurDate();

        ArrayList<Entry> values = new ArrayList<>();
        LineDataSet lineDataSet;

        int i;

        for (i=0; i< onedaydbdata.size(); i++)
        {
            Log.i("VALUE", Integer.toString(i));
            double rate =onedaydbdata.get(i).getThroughputMpbs();
            throughput[i]=(float)(Math.round(rate*100)/100);
            Log.i("value arr:",String.valueOf(throughput[i]));
        }

        for(i=0; i<onedaydbdata.size();i++){
            long DATE = onedaydbdata.get(i).getCurDate();
            date[i]=DATE;
        }

        for (i=0;i<onedaydbdata.size();i++){
            values.add(new Entry(date[i]-referenceTime,throughput[i]));
        }

        lineDataSet=new LineDataSet(values,"Throughput Within Past 24 Hours / Mbps");
        HourAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(referenceTime);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChart.setData(new LineData(lineDataSet));



    }

}
