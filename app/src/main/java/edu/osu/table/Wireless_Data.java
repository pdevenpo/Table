package edu.osu.table;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Wireless_Data {
    // TO DO - Want to make this required to be unique and indexed

    @PrimaryKey
    private int uid;

    private String SSID;
    private String MAC_Address;
    private int RSS_dBm;
    private int chan_freq;
    private int throughput_Mbps;
    private int battery_percentage;
    private int time;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getThroughput_Mbps() {
        return throughput_Mbps;
    }

    public void setThroughput_Mbps(int throughput_Mbps) {
        this.throughput_Mbps = throughput_Mbps;
    }

    public int getBattery_percentage() {
        return battery_percentage;
    }

    public void setBattery_percentage(int battery_percentage) {
        this.battery_percentage = battery_percentage;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getMAC_Address() {
        return MAC_Address;
    }

    public void setMAC_Address(String MAC_Address) {
        this.MAC_Address = MAC_Address;
    }

    public int getRSS_dBm() {
        return RSS_dBm;
    }

    public void setRSS_dBm(int RSS_dBm) {
        this.RSS_dBm = RSS_dBm;
    }

    public int getChan_freq() {
        return chan_freq;
    }

    public void setChan_freq(int chan_freq) {
        this.chan_freq = chan_freq;
    }


}
