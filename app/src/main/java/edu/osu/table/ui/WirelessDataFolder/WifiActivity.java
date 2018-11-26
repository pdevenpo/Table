package edu.osu.table.ui.WirelessDataFolder;

import android.net.TrafficStats;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import edu.osu.table.R;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;


public class WifiActivity extends AppCompatActivity {

    private Button btn_ping;
    private Button btn_download;

    private TextView pingtime;
    private TextView speed;

    private long l, total;
    private Handler handler = new Handler();
    private TextView download_speed;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        pingtime = (TextView)findViewById(R.id.pinglatency);
        btn_download = (Button)findViewById(R.id.buttonDownload);

        speed = (TextView)findViewById(R.id.speedDownload);
        btn_ping = (Button)findViewById(R.id.buttonPing);

        download_speed =(TextView)findViewById(R.id.speed);

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
                        //final String rate1 = Float.toString(rate);
                        //speed.setText(rate1);

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
                long endTime = System.currentTimeMillis();

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

}