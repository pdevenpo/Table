package edu.osu.table.ui.ScanActivity;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import edu.osu.table.R;

/**
 * Document Provided by Professor Adam Champion of The Ohio State University for use in
 * Professor Dong Xuan's Mobile Handset System class with permission from Professor Champion.
 * Other Documents provided by him are prefaced with this descriptor at the top of the file -->
 */
public class WifiScanActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


    }


    @Override
    protected Fragment createFragment() {
        return new WifiScanFragment();
    }
}
