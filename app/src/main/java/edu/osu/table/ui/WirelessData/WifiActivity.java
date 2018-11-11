package edu.osu.table.ui.WirelessData;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import edu.osu.table.R;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class WifiActivity extends AppCompatActivity {

    private Button throughput_test;
    private TextView speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        speed = (TextView)findViewById(R.id.speedWifi);
        throughput_test = (Button)findViewById(R.id.buttonWifi);

        throughput_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float value = getSpeed();
                String value1 = Float.toString(value);
                speed.setText(value1 + "Mbps");
            }
        });


    }

    public static float getSpeed(){

        String result = null;
        float value = 0;


        try {
            String ip = "8.8.4.4";  // change it into 8.8.4.4 before you test it on your android phone, 127.0.0.1 for emulator
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 1 -s 65500 " + ip);

            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";

            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.i("Throughput", "result content : " + stringBuffer.toString());
            String arr[] = stringBuffer.toString().split(" ");
            String time[] = arr[12].split("=");

            Log.i("Throughout","time=" + time[1]);
            float k = Float.parseFloat(time[1]);

            int status = p.waitFor();
            if (status == 0) {
                result = "successful~";
                value = 65508 * 8 / Float.parseFloat(time[1])/1000 ; // Mbits/s
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




}
