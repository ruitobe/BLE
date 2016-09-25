package com.rui.ble.bluetooth.sensor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rui.ble.R;
import com.rui.ble.bluetooth.common.BleDevInfo;
import com.rui.ble.bluetooth.common.BleService;
import com.rui.ble.bluetooth.util.CustomTimer;
import com.rui.ble.bluetooth.util.CustomTimerCallback;
import com.rui.ble.user.util.UserDatabaseAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Requests to other activities
    private static final int REQ_ENABLE_BT = 0;
    private static final int REQ_DEVICE_ACT = 1;

    // Log
    private static final String TAG = "MainActivity";

    // GUI
    private static MainActivity mThis = null;
    private Intent mDevIntent;
    private static final int STATUS_DURATION = 5;


    private Toolbar mToolbar;
    private Context mContext;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;


    // BLE management
    private boolean mBtAdapterEnabled = false;
    private boolean mBleSupported = true;
    private boolean mScanning = false;
    private int mNumDevs = 0;
    private int mConnIndex = NO_DEVICE;
    private List<BleDevInfo> mDevInfoList;
    private static BluetoothManager mBtManager;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mBtDev = null;
    private BleService mBleService = null;
    private IntentFilter mFilter;
    private String[] mDevFilter = null;

    // Housekeeping
    private static final int NO_DEVICE = -1;
    private boolean mInitialised = false;
    SharedPreferences prefs = null;

    // ScanView

    private final int SCAN_TIMEOUT = 10; // Seconds
    private final int CONNECT_TIMEOUT = 20; // Seconds

    private DeviceListAdapter mDevListAdapter = null;
    private TextView mEmptyMsg;
    private TextView mStatus;
    private Button mScanBtn = null;
    private ListView mBleDevListView = null;
    private boolean mBusy;

    private CustomTimer mScanTimer = null;
    private CustomTimer mConnectTimer = null;

    private CustomTimer mStatusTimer;

    // Data file stored
    private File dir = null;

    // About navigation drawer
    private UserDatabaseAdapter mUserDatabaseAdapter;

    private void initNavigationDrawer() {
        bindNavigationDrawerResources();
        setToolbar();
        setDrawer();

    }

    private void bindNavigationDrawerResources() {

        mContext = this;
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_activity);
        mNavigationView = (NavigationView) findViewById(R.id.activity_main_navigation_view);

    }

    private void setToolbar() {
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    private void setDrawer() {

        String name = null;
        String email = null;

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.getHeaderView(0).findViewById(R.id.navigation_drawer_header_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDrawerLayout.closeDrawer(GravityCompat.START);
                // TODO: implement some other activity and jump there
                //startActivityWithDelay();
            }

        });

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                mDrawerLayout.closeDrawer(GravityCompat.START);

                switch (item.getItemId()) {
                    case R.id.navigation_view_item_home:
                        item.setChecked(true);
                        setToolbarTitle(R.string.toolbar_title_home);
                        // TODO: implement some other activity and jump there
                        break;
                    case R.id.navigation_view_item_help:
                        // TODO: implement some other activity and jump there
                        //startActivityWithDelay();
                        break;

                    case R.id.navigation_view_item_about:
                        // TODO: implement some other activity and jump there
                        //startActivityWithDelay();
                        break;
                }

                return true;
            }

        });

        // Get current user information from SQLite database
        mUserDatabaseAdapter = new UserDatabaseAdapter(this).open();

        if (mUserDatabaseAdapter.isTableExisted(UserDatabaseAdapter.DATABASE_CURRENT_TABLE) &&
                mUserDatabaseAdapter.getEntryCountInTable(UserDatabaseAdapter.DATABASE_CURRENT_TABLE) == 1) {

            // Set user name and email address for drawer header
            name = mUserDatabaseAdapter.getSingleEntry(UserDatabaseAdapter.DATABASE_CURRENT_TABLE).getName();
            email = mUserDatabaseAdapter.getSingleEntry(UserDatabaseAdapter.DATABASE_CURRENT_TABLE).getEmail();

        }

        TextView nameTV = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.navigation_drawer_account_information_display_name);
        TextView emailTV = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.navigation_drawer_account_information_email);
        nameTV.setText(name);
        emailTV.setText(email);

    }

    private void setToolbarTitle(int string) {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(string);
        }
    }

    // We start this activities with delay to avoid junk while closing the drawer
    private void startActivityWithDelay(final Class activity) {

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                startActivity(new Intent(mContext, activity));
            }
        }, 250);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Init navigation drawer
        initNavigationDrawer();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Init device list container and device filter
        mDevInfoList = new ArrayList<BleDevInfo>();
        Resources res = getResources();

        // This is only for sensor tag， for official release, support all devices!
        mDevFilter = res.getStringArray(R.array.device_filter);

        // Register the BroadcastReceiver
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        mFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);

        // Init widgets
        mStatus = (TextView)findViewById(R.id.ble_status_tv);
        mScanBtn = (Button)findViewById(R.id.ble_scan_btn);
        mBleDevListView = (ListView)findViewById(R.id.ble_lv);
        mBleDevListView.setClickable(true);
        mBleDevListView.setOnItemClickListener(mDevClickListener);
        mEmptyMsg = (TextView)findViewById(R.id.ble_info_tv);
        mBusy = false;

        // Storage
        dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/BLE");
        if (!dir.exists()) {
            dir.mkdir();
        }


        onScanViewReady();


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        }
        else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_activity);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    void onScanViewReady() {

        // In future, if this is the first time running, we need to pop up the licence info

        if(!mInitialised) {

            // This Ble service must be retrieved from the BleAppClass which starts it.
            mBleService = ((BleAppClass)getApplicationContext()).getBleService();

            Log.e(TAG, "mBleService = " + mBleService);

            // Broadcast receiver
            mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBtAdapter = mBtManager.getAdapter();
            registerReceiver(mReceiver, mFilter);
            mBtAdapterEnabled = mBtAdapter.isEnabled();
            if(!mBtAdapterEnabled) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQ_ENABLE_BT);
            }
            mInitialised = true;


        }
        else {
            notifyDataSetChanged();
        }
        // Init widgets
        updateGuiState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBtAdapter = null;
        unregisterReceiver(mReceiver);

        File cache = getCacheDir();
        String path = cache.getPath();

        try {
            Runtime.getRuntime().exec(String.format("rm -rf %s", path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onBluetooth() {
        Intent settingsIntent = new Intent(
                android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(settingsIntent);
    }

    public void updateGuiState() {
        boolean mBtEnabled = mBtAdapter.isEnabled();

        if (mBtEnabled) {
            if (mScanning) {
                // BLE Host connected
                if (mConnIndex != NO_DEVICE) {
                    String txt = mBtDev.getName() + " connected";
                    setStatus(txt);
                } else {
                    setStatus(mNumDevs + " devices");
                }
            }
        } else {

            mDevInfoList.clear();
            notifyDataSetChanged();
        }
    }

    void setBusy(boolean flag) {
        if(flag != mBusy) {
            mBusy = flag;
            if(!mBusy) {
                stopTimers();
                mScanBtn.setEnabled(true);
                mDevListAdapter.notifyDataSetChanged();
            }
            // TODO: here we need to show busy indicator later
        }
    }

    void setError(String error) {

        setBusy(false);
        stopTimers();
        mStatus.setText(error);
    }



    void onConnect() {
        if(mNumDevs > 0) {

            int connState = mBtManager.getConnectionState(mBtDev, BluetoothGatt.GATT);

            switch (connState) {
                case BluetoothGatt.STATE_CONNECTED:
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:

                    if(mBleService == null) {

                        // This Ble service must be retrieved from the BleAppClass which starts it.
                        mBleService = ((BleAppClass)getApplicationContext()).getBleService();
                        Log.e(TAG, "mBleService = " + mBleService);
                    }

                    boolean ok = mBleService.connect(mBtDev.getAddress());

                    if(!ok) {
                        setError("Connection failed");
                    }
                    break;
                default:
                    setError("BLE device busy");
                    break;
            }
        }
    }

    public void onScanBtn(View view) {

        if(mScanning) {
            stopScan();
        }
        else {
            startScan();
        }

    }

    void updateGui(boolean scanning) {
        if (mScanBtn == null)
            return; // UI not ready

        setBusy(scanning);

        if (scanning) {

            // Indicate that scanning has started
            mScanTimer = new CustomTimer(null, SCAN_TIMEOUT, mPgScanCallback);
            mScanBtn.setText("Stop");
            mStatus.setText("Scanning...");
            mEmptyMsg.setText(R.string.no_ble_device);
            updateGuiState();

        } else {
            // Indicate that scanning has stopped
            mScanBtn.setText("Scan");
            mEmptyMsg.setText(R.string.scanning_no);
            notifyDataSetChanged();
        }
    }


    private void startScan() {

        if(mBleSupported) {
            mNumDevs = 0;
            mDevInfoList.clear();
            notifyDataSetChanged();
            scanBleDev(true);
            updateGui(mScanning);

            if(!mScanning) {
                setError("BLE device discovery start failed...");
                setBusy(false);
            }
        }
        else {
            setError("BLE not support on this device...");
        }
    }

    private void stopScan() {

        mScanning = false;
        updateGui(false);
        scanBleDev(false);
    }

    private BleDevInfo createDevInfo(BluetoothDevice dev, int rssi) {

        BleDevInfo devInfo = new BleDevInfo(dev, rssi);

        return devInfo;
    }

    boolean checkDevFilter(String devName) {

        boolean found = false;

        if (devName == null) {
            return found;
        }

        int n = mDevFilter.length;

        if (n > 0) {

            // Now we must support other devices
            // Devices defined in Sensortags
            for (int i = 0; i < n && !found; i++) {

                if (devName.equals(mDevFilter[i])) {
                    found = true;
                }
            }

            return found;
        } else
            // Allow all devices if the device filter is empty
            return true;
    }

    private void addDevice(BleDevInfo dev) {

        mNumDevs++;
        mDevInfoList.add(dev);
        notifyDataSetChanged();

        if (mNumDevs > 1)
            setStatus(mNumDevs + " devices");
        else
            setStatus("1 device");
    }

    private boolean isDevInfoExisted(String addr) {

        for (int i = 0; i < mDevInfoList.size(); i++) {
            if (mDevInfoList.get(i).getBtDev().getAddress()
                    .equals(addr)) {
                return true;
            }
        }
        return false;
    }

    private BleDevInfo findDevInfo(BluetoothDevice dev) {

        for (int i = 0; i < mDevInfoList.size(); i++) {
            if (mDevInfoList.get(i).getBtDev().getAddress()
                    .equals(dev.getAddress())) {
                return mDevInfoList.get(i);
            }
        }
        return null;
    }

    private boolean scanBleDev(boolean enabled) {

        int apiVer = android.os.Build.VERSION.SDK_INT;
        if (apiVer > Build.VERSION_CODES.KITKAT) {

            BluetoothLeScanner scanner = mBtAdapter.getBluetoothLeScanner();
            if (enabled) {

                scanner.startScan(mBleScanCallback);
                mScanning = true;
            }
            else {
                mScanning = false;
                scanner.stopScan(mBleScanCallback);
            }

        }

        else {

            if (enabled) {
                mScanning = mBtAdapter.startLeScan(mBtAdapterLeScanCallback);

            } else {

                mScanning = false;
                mBtAdapter.stopLeScan(mBtAdapterLeScanCallback);
            }
        }


        return mScanning;
    }

    private void startDevActivity() {

        mDevIntent = new Intent(this, DevActivity.class);
        mDevIntent.putExtra(DevActivity.EXTRA_DEVICE, mBtDev);
        startActivityForResult(mDevIntent, REQ_DEVICE_ACT);
    }

    private void stopDevActivity() {
        finishActivity(REQ_DEVICE_ACT);
    }

    public void onDevClick(final int pos) {

        if (mScanning)
            stopScan();

        setBusy(true);
        mBtDev = mDevInfoList.get(pos).getBtDev();

        if (mConnIndex == NO_DEVICE) {
            setStatus("Connecting");
            mConnIndex = pos;


            onConnect();


        } else {
            setStatus("Disconnecting");
            if (mConnIndex != NO_DEVICE) {
                mBleService.disconnect(mBtDev.getAddress());
            }
        }
    }

    public void onScanTimeout() {

        runOnUiThread(new Runnable() {
            public void run() {
                stopScan();
            }
        });
    }

    public void onConnectTimeout() {

        runOnUiThread(new Runnable() {
            public void run() {
                setError("Connection timed out");
            }
        });
        if (mConnIndex != NO_DEVICE) {
            mBleService.disconnect(mBtDev.getAddress());
            mConnIndex = NO_DEVICE;
        }
    }

    private CustomTimerCallback mPgScanCallback = new CustomTimerCallback() {

        public void onTimeout() {
            onScanTimeout();
        }

        public void onTick(int i) {

        }
    };

    // Listener for connect/disconnect expiration
    private CustomTimerCallback mPgConnectCallback = new CustomTimerCallback() {

        public void onTimeout() {
            onConnectTimeout();
            mScanBtn.setEnabled(true);
        }

        public void onTick(int i) {
            // TODO: refresh busy indicator
        }
    };

    // Listener for connect/disconnect expiration
    private CustomTimerCallback mClearStatusCallback = new CustomTimerCallback() {
        public void onTimeout() {
            runOnUiThread(new Runnable() {
                public void run() {
                    setStatus("");
                }
            });
            mStatusTimer = null;
        }

        public void onTick(int i) {
        }
    };

    // Listener for device list
    private AdapterView.OnItemClickListener mDevClickListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

            // Log.d(TAG,"item click");
            mConnectTimer = new CustomTimer(null, CONNECT_TIMEOUT, mPgConnectCallback);
            mScanBtn.setEnabled(false);
            // Force disabling of all Connect buttons
            mDevListAdapter.notifyDataSetChanged();

            onDevClick(pos);
        }
    };


    void setStatus(String status) {
        mStatus.setText(status);
    }

    void setStatus(String status, int duration) {
        setStatus(status);
        mStatusTimer = new CustomTimer(null, duration, mClearStatusCallback);
    }

    private void stopTimers() {

        if (mScanTimer != null) {

            mScanTimer.stop();
            mScanTimer = null;
        }

        if (mConnectTimer != null) {
            mConnectTimer.stop();
            mConnectTimer = null;
        }
    }

    List<BleDevInfo> getDevInfoList() {
        return mDevInfoList;
    }

    void notifyDataSetChanged() {

        List<BleDevInfo> devList = getDevInfoList();
        if (mDevListAdapter == null) {
            mDevListAdapter = new DeviceListAdapter(this, devList);
        }

        mBleDevListView.setAdapter(mDevListAdapter);
        mDevListAdapter.notifyDataSetChanged();

        if (devList.size() > 0) {
            mEmptyMsg.setVisibility(View.GONE);
        } else {
            mEmptyMsg.setVisibility(View.VISIBLE);
        }
    }

    // Activity result handling
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQ_DEVICE_ACT:
                // When the device activity has finished: disconnect the device
                if (mConnIndex != NO_DEVICE) {
                    mBleService.disconnect(mBtDev.getAddress());
                }
                break;

            case REQ_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {

                    Toast.makeText(this, R.string.bt_on, Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(this, R.string.bt_off, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:

                break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                // Bluetooth adapter state change
                switch (mBtAdapter.getState()) {

                    case BluetoothAdapter.STATE_ON:
                        mConnIndex = NO_DEVICE;
                        //startBluetoothLeService();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context, R.string.app_off, Toast.LENGTH_LONG)
                                .show();
                        finish();
                        break;
                    default:
                        Log.w(TAG, "Action STATE CHANGED not processed ");
                        break;
                }

                updateGuiState();

            } else if (BleService.ACTION_GATT_CONNECTED.equals(action)) {

                // GATT connect
                int status = intent.getIntExtra(BleService.EXTRA_STATUS,
                        BluetoothGatt.GATT_FAILURE);

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    setBusy(false);

                    startDevActivity();

                } else
                    setError("Connect failed. Status: " + status);
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                // GATT disconnect
                int status = intent.getIntExtra(BleService.EXTRA_STATUS,
                        BluetoothGatt.GATT_FAILURE);
                stopDevActivity();
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    setBusy(false);
                    setStatus(mBtDev.getName() + " disconnected",
                            STATUS_DURATION);
                } else {
                    //setError("Disconnect Status: " + HCIDefines.hciErrorCodeStrings.get(status));
                }
                mConnIndex = NO_DEVICE;
                mBleService.close();
            } else {
                // Log.w(TAG,"Unknown action: " + action);
            }

        }
    };

    private ScanCallback mBleScanCallback = new ScanCallback() {


        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            // This will trigger each time a new device is found
            final BluetoothDevice dev = result.getDevice();
            if ((dev != null) && (dev.getName() != null)) {

                Log.e(TAG, dev.getName().toString());
            }

            runOnUiThread(new Runnable() {

                public void run() {
                    // No need to filter other devices now!

                    //if (checkDevFilter(dev.getName())) {

                        if (!isDevInfoExisted(dev.getAddress())) {
                            // New device
                            BleDevInfo devInfo = createDevInfo(dev, result.getRssi());
                            addDevice(devInfo);
                        } else {
                            // Already in list, update RSSI info
                            BleDevInfo devInfo = findDevInfo(dev);
                            devInfo.updateRssi(result.getRssi());
                            notifyDataSetChanged();
                        }
                    }
                //}

            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

            for (final ScanResult result : results) {

                final BluetoothDevice dev = result.getDevice();
                runOnUiThread(new Runnable() {

                    public void run() {

                        if (checkDevFilter(dev.getName())) {

                            if (!isDevInfoExisted(dev.getAddress())) {
                                // New device
                                BleDevInfo devInfo = createDevInfo(dev, result.getRssi());
                                addDevice(devInfo);
                            } else {
                                // Already in list, update RSSI info
                                BleDevInfo devInfo = findDevInfo(dev);
                                devInfo.updateRssi(result.getRssi());
                                notifyDataSetChanged();
                            }
                        }
                    }

                });
            }
        }
    };

    private BluetoothAdapter.LeScanCallback mBtAdapterLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        public void onLeScan(final BluetoothDevice dev, final int rssi,
                             byte[] scanRecord) {

            runOnUiThread(new Runnable() {
                public void run() {

                    // In official release, we do not filter any BLE device,
                    // App shall display all scanned devices

                    // Filter devices
                    //if (checkDevFilter(dev.getName())) {
                        if (!isDevInfoExisted(dev.getAddress())) {
                            // New device
                            BleDevInfo devInfo = createDevInfo(dev, rssi);
                            addDevice(devInfo);
                        } else {
                            // Already in list, update RSSI info
                            BleDevInfo devInfo = findDevInfo(dev);
                            devInfo.updateRssi(rssi);
                            notifyDataSetChanged();
                        }
                    //}
                }

            });

        }

    };

    class DeviceListAdapter extends BaseAdapter {

        private List<BleDevInfo> mDevices;
        private LayoutInflater mInflater;

        public DeviceListAdapter(Context context, List<BleDevInfo> devices) {

            mInflater = LayoutInflater.from(context);
            mDevices = devices;
        }

        @Override
        public int getCount() {

            return mDevices.size();
        }

        @Override
        public Object getItem(int position) {

            return mDevices.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) mInflater.inflate(R.layout.element, null);
            }

            BleDevInfo bleDevInfo = mDevices.get(position);
            BluetoothDevice dev = bleDevInfo.getBtDev();
            int rssi = bleDevInfo.getRssi();
            String name;
            name = dev.getName();
            if (name == null) {
                name = new String("Unknown device");
            }


            ImageView iv = (ImageView)vg.findViewById(R.id.devImage);

            if (name.equals("SensorTag2") || name.equals("CC2650 SensorTag"))
                iv.setImageResource(R.drawable.bluetooth);
            else {
                iv.setImageResource(R.drawable.bluetooth);
            }
            // Hack the name so that we don't display the real hardware device name
            if(name.equals("SensorTag2") || name.equals("CC2650 SensorTag"))
            {
                name = "BLE device";
            }

            String descr = name + "\n" + dev.getAddress() + "\nRssi: " + rssi + " dBm";

            ((TextView) vg.findViewById(R.id.descr)).setText(descr);
            // Disable connect button when connecting or connected
            Button bv = (Button)vg.findViewById(R.id.btnConnect);
            bv.setEnabled(mConnectTimer == null);

            return vg;
        }

    }


}
