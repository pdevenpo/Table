package edu.osu.table.ui.WirelessData;

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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        pingtime = (TextView)findViewById(R.id.speedWifi);
        btn_ping = (Button)findViewById(R.id.buttonWifi);

        speed = (TextView)findViewById(R.id.speedDownload);
        btn_download = (Button)findViewById(R.id.buttonDownload);




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
                        String rate1 = Float.toString(rate);
                        speed.setText(rate1 + "Kbps");
                    }
                }).start();


            }
        });

    }


    public static float getdelay(){

        String result = null;
        float value = 0;


        try {
            Process p = Runtime.getRuntime().exec( "ping -c 1 -w 1 www.osu.edu");

            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";

            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.i("Throughput", "result content : " + stringBuffer.toString());
            String arr[] = stringBuffer.toString().split(" ");
            String time[] = arr[13].split("=");

            Log.i("Throughout","time=" + time[1]);
            float k = Float.parseFloat(time[1]);

            int status = p.waitFor();
            if (status == 0) {
                result = "successful~";
                //value = 64 * 8 / Float.parseFloat(time[1])/1000 ; // Mbits/s
                value = Float.parseFloat(time[1]);
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
        String download_url = "https://www.osu.edu/assets/images/features/2018/hidden_gems_osu.jpg";
        try{
            URL url = new URL(download_url);
            float red = 0;
            float size = 0;
            long time;
            float time1;
            byte[] buf = new byte[1024];
            long startTime = System.currentTimeMillis();
            Log.i("Throughput","start time : "+ startTime);
            URLConnection con = url.openConnection();

            //define inputStream to read from the URLConnection
            InputStream in = con.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(in);

            while ((red = bis.read(buf)) != -1){
                size += red;
            }
            long endTime = System.currentTimeMillis();
            time = endTime-startTime;
            time1 = time;

            Log.i("Throughput","end time : "+ endTime);

            Log.i("Throughput","size:"+size/1024);
            Log.i("Throughput","time:"+time1/1000);

            rate = (((size/1024)*8)/((time1-latency)/1000));

        }
        catch (IOException e){
            Log.d("Throughput","download Error:" + e);
        }
        return rate;
    }

}
