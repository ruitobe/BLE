package com.rui.ble.bluetooth.sensor;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.rui.ble.R;

/**
 * Created by rhuang on 8/20/16.
 */
public class DevView extends Fragment {

    private static final String TAG = "DevViewFragment";

    public static DevView mInstance = null;

    // GUI
    private TableLayout mTable;
    public boolean first = true;

    // House-keeping
    private DevActivity mActivity;
    private boolean mBusy;

    // The last two arguments ensure LayoutParams are inflated properly.
    View view;

    public DevView() {
        super();
    }

   // public DevView getmInstance() {
   //     return mInstance;
   // }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mInstance = this;
        mActivity = (DevActivity) getActivity();

        view = inflater.inflate(R.layout.generic_services_browser, container, false);

        mTable = (TableLayout) view.findViewById(R.id.generic_services_layout);

        // Notify activity that UI has been inflated
        mActivity.onViewInflated(view);

        return view;
    }

    public void showProgressOverlay(String title) {

    }

    public void addRowToTable(TableRow row) {

        if (first) {

            mTable.removeAllViews();
            mTable.addView(row);

            mTable.requestLayout();

            first = false;
        }
        else {
            mTable.addView(row);
            mTable.requestLayout();
        }
    }

    public void removeRowsFromTable() {
        mTable.removeAllViews();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    void setBusy(boolean f) {
        if (f != mBusy)
        {
            //mActivity.showBusyIndicator(f);
            mBusy = f;
        }
    }
}
