package edu.osu.table.ui.graph;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SsidAxisFormatter  implements IAxisValueFormatter
{

    private List<String> ssids;
    private List<String> macs;
    private int iterval;

    public SsidAxisFormatter(List<String> ssids, List<String> macs) {
        this.ssids = ssids;
        this.macs = macs;
        this.iterval = 0;
    }


    /**
     * Called when a value from an axis is to be formatted
     * before being drawn. For performance reasons, avoid excessive calculations
     * and memory allocations inside this method.
     *
     * @param value the value to be formatted
     * @param axis  the axis the value belongs to
     * @return
     */
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // convertedTimestamp = originalTimestamp - referenceTimestamp
        // Retrieve original timestamp
        iterval = (int)value;
        return getSSID(iterval);
    }

    private String getSSID(int iterval) {
        try{
            return ssids.get(iterval) + "\n" + macs.get(iterval);
            //return ssids.get(iterval);
        }
        catch(Exception ex){
            return "xx";
        }
    }

}

