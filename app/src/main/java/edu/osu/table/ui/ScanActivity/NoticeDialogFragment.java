package edu.osu.table.ui.ScanActivity;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import edu.osu.table.R;

/**
 * Document Provided by Professor Adam Champion of The Ohio State University for use in
 * Professor Dong Xuan's Mobile Handset System class with permission from Professor Champion.
 * Other Documents provided by him are prefaced with this descriptor at the top of the file -->
 */

/**
 * DialogFragment asking user to allow permission to do Wi-Fi scans.
 * Code based on http://www.codingdemos.com/android-custom-alertdialog/,
 * but using DialogFragment.
 *
 * Created by adamcchampion on 2018/01/05.
 */

public class NoticeDialogFragment extends DialogFragment {
    private View mView;
    private CheckBox mCheckBox;

    private void setDialogStatus(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getResources().getString(R.string.suppress_dialog_key), isChecked);
        editor.apply();
    }


    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_permission, null);
        mCheckBox = mView.findViewById(R.id.checkbox);

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setDialogStatus(true);
                }
                else {
                    setDialogStatus(false);
                }
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle("Notice")
                .setMessage("This app asks for location permission, which Wi-Fi scans require. Please grant this permission.")
                .setView(mView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                }).create();
    }
}
