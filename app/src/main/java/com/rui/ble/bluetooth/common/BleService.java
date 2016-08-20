package com.rui.ble.bluetooth.common;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by rhuang on 7/26/16.
 */


/**
 * Service for managing connection and
 * data communication with a GATT server hosted on a given Bluetooth LE device.
 */

public class BleService extends Service {

    static final String TAG = "BleService";

    public final static String ACTION_GATT_CONNECTED = "com.rui.ble.bluetooth.common.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.rui.ble.bluetooth.common.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.rui.ble.bluetooth.common.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_READ = "com.rui.ble.bluetooth.common.ACTION_DATA_READ";
    public final static String ACTION_DATA_NOTIFY = "com.rui.ble.bluetooth.common.ACTION_DATA_NOTIFY";
    public final static String ACTION_DATA_WRITE = "com.rui.ble.bluetooth.common.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "com.rui.ble.bluetooth.common.EXTRA_DATA";
    public final static String EXTRA_UUID = "com.rui.ble.bluetooth.common.EXTRA_UUID";
    public final static String EXTRA_STATUS = "com.rui.ble.bluetooth.common.EXTRA_STATUS";
    public final static String EXTRA_ADDRESS = "com.rui.ble.bluetooth.common.EXTRA_ADDRESS";
    public final static int GATT_TIMEOUT = 150;

    // BLE
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private static BleService mThis = null;
    private String mBtDevAddr;

    public Timer disconnectionTimer;
    private final Lock lock = new ReentrantLock();

    private volatile boolean blockingFlag = false;

    //Success
    private volatile int lastGattStatus = 0;
    private volatile BleReq currBleReq = null;

    public enum bleReqOperation {
        wrBlocking,
        wr,
        rdBlocking,
        rd,
        nsBlocking,
    }

    public enum bleReqStatus {
        not_queued,
        queued,
        processing,
        timeout,
        done,
        noSuchReq,
        failed,
    }

    public class BleReq {

        public int id;
        public BluetoothGattCharacteristic characteristic;
        public bleReqOperation bleReqOp;
        public volatile bleReqStatus status;
        public int timeout;
        public int currTimeout;
        public boolean notifyenable;
    }

    private volatile LinkedList<BleReq> procQueue;
    private volatile LinkedList<BleReq> nonBlockQueue;

    public boolean init() {

        mThis = this;

        if(mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager == null)
                return false;
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null) {

            return false;
        }

        procQueue = new LinkedList<BleReq>();
        nonBlockQueue = new LinkedList<BleReq>();

        Thread queueThread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    executeQueue();
                    try {
                        Thread.sleep(0, 100000);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        queueThread.start();
        return true;

    }

    // GATT client callbacks
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if(mGattCallback == null) {
                return;
            }

            BluetoothDevice dev = gatt.getDevice();
            String addr = dev.getAddress();

            try {
                switch(newState) {

                    case BluetoothProfile.STATE_CONNECTED:
                        broadcastUpdate(ACTION_GATT_CONNECTED, addr, status);
                        break;

                    case BluetoothProfile.STATE_DISCONNECTED:
                        broadcastUpdate(ACTION_GATT_DISCONNECTED, addr, status);
                        break;

                    default:
                        break;
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            BluetoothDevice dev = gatt.getDevice();
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, dev.getAddress(), status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            broadcastUpdate(ACTION_DATA_NOTIFY, characteristic, BluetoothGatt.GATT_SUCCESS);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if(blockingFlag) {
                unlockBlockingThread(status);
            }

            if(nonBlockQueue.size() > 0) {
                lock.lock();

                for(int index = 0; index < nonBlockQueue.size(); index++) {
                    BleReq req = nonBlockQueue.get(index);

                    if(req.characteristic == characteristic) {
                        req.status = bleReqStatus.done;
                        nonBlockQueue.remove(index);
                        break;
                    }
                }

                lock.unlock();
            }
            broadcastUpdate(ACTION_DATA_READ, characteristic, status);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if(blockingFlag) {
                unlockBlockingThread(status);
            }

            if(nonBlockQueue.size() > 0) {
                lock.lock();

                for(int index = 0; index < nonBlockQueue.size(); index++) {
                    BleReq req = nonBlockQueue.get(index);

                    if(req.characteristic == characteristic) {
                        req.status = bleReqStatus.done;
                        nonBlockQueue.remove(index);
                        break;
                    }

                }

                lock.unlock();
            }

            broadcastUpdate(ACTION_DATA_WRITE, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            if(blockingFlag) {
                unlockBlockingThread(status);
            }
            unlockBlockingThread(status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            if(blockingFlag) {
                unlockBlockingThread(status);
            }
        }
    };

    private void broadcastUpdate(final String action, final String addr, final int status) {

        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ADDRESS, addr);
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic, final int status) {

        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
    }

    private void unlockBlockingThread(int status) {
        this.lastGattStatus = status;
        this.blockingFlag = false;
    }

    public boolean checkGatt() {

        if(mBluetoothAdapter == null) {
            // Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        if(mBluetoothGatt == null) {
            // Log.w(TAG, "BluetoothGatt not initialized");
            return false;
        }

        if(this.blockingFlag) {
            // Log.w(TAG, "Cannot start operation: blocked");
            return false;
        }

        return true;

    }

    // Manage BLE service

    public class LocalBinder extends Binder {

        public BleService getService() {

            return BleService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        close();
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        this.init();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }



    // BLE API Implementation

    public boolean addReqToQueue(BleReq req) {

        lock.lock();
        if(procQueue.peekLast() != null) {
            req.id = procQueue.peek().id++;
        }
        else {
            req.id = 0;
            procQueue.add(req);
        }
        lock.unlock();
        return true;
    }

    public bleReqStatus pollForStatusOfReq(BleReq req) {

        lock.lock();
        if(req == currBleReq) {

            bleReqStatus status = currBleReq.status;
            if(status == bleReqStatus.done) {
                currBleReq = null;
            }
            if(status == bleReqStatus.timeout) {
                currBleReq = null;
            }
            lock.unlock();
            return status;
        }
        else {
            lock.unlock();
            return bleReqStatus.noSuchReq;
        }
    }

    public int readCharacteristic(BluetoothGattCharacteristic characteristic) {

        BleReq req = new BleReq();
        req.status = bleReqStatus.not_queued;
        req.characteristic = characteristic;
        req.bleReqOp = bleReqOperation.rdBlocking;
        addReqToQueue(req);

        boolean finished = false;
        while(!finished) {

            bleReqStatus status = pollForStatusOfReq(req);
            if(status == bleReqStatus.done) {
                finished = true;

                return 0;
            }
            else if(status == bleReqStatus.timeout) {
                finished = true;
                return -3;
            }

        }
        return -2;
    }

    public int writeCharacteristic(BluetoothGattCharacteristic characteristic) {

        BleReq req = new BleReq();
        req.status = bleReqStatus.not_queued;
        req.characteristic = characteristic;
        req.bleReqOp = bleReqOperation.wrBlocking;
        addReqToQueue(req);

        boolean finished = false;
        while(!finished) {

            bleReqStatus status = pollForStatusOfReq(req);
            if(status == bleReqStatus.done) {
                finished = true;

                return 0;
            }
            else if(status == bleReqStatus.timeout) {
                finished = true;
                return -3;
            }

        }
        return -2;
    }

    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, byte b) {

        byte[] val = new byte[1];
        val[0] = b;
        characteristic.setValue(val);

        BleReq req = new BleReq();
        req.status = bleReqStatus.not_queued;
        req.characteristic = characteristic;
        req.bleReqOp = bleReqOperation.wrBlocking;
        addReqToQueue(req);

        boolean finished = false;
        while(!finished) {

            bleReqStatus status = pollForStatusOfReq(req);
            if(status == bleReqStatus.done) {
                finished = true;

                return 0;
            }
            else if(status == bleReqStatus.timeout) {
                finished = true;
                return -3;
            }

        }
        return -2;
    }

    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] b) {

        characteristic.setValue(b);

        BleReq req = new BleReq();
        req.status = bleReqStatus.not_queued;
        req.characteristic = characteristic;
        req.bleReqOp = bleReqOperation.wrBlocking;
        addReqToQueue(req);

        boolean finished = false;
        while(!finished) {

            bleReqStatus status = pollForStatusOfReq(req);
            if(status == bleReqStatus.done) {
                finished = true;

                return 0;
            }
            else if(status == bleReqStatus.timeout) {
                finished = true;
                return -3;
            }

        }
        return -2;
    }

    public boolean writeCharacteristicNonBlock(BluetoothGattCharacteristic characteristic) {

        BleReq req = new BleReq();

        req.status = bleReqStatus.not_queued;
        req.characteristic = characteristic;
        req.bleReqOp = bleReqOperation.wr;
        addReqToQueue(req);
        return true;
    }

    public int getNumServices() {

        if(mBluetoothGatt == null) {
            return 0;
        }

        return mBluetoothGatt.getServices().size();
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if(mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }

    public int setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {

        BleReq req = new BleReq();
        req.status = bleReqStatus.not_queued;
        req.characteristic = characteristic;
        req.bleReqOp = bleReqOperation.nsBlocking;
        req.notifyenable = enabled;
        addReqToQueue(req);
        boolean finished = false;

        while(!finished) {
            bleReqStatus status = pollForStatusOfReq(req);
            if(status == bleReqStatus.done) {
                finished = true;
                return 0;
            }
            else if(status == bleReqStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    public boolean isNotificationEnabled(BluetoothGattCharacteristic characteristic) {

        if (characteristic == null) {
            return false;
        }
        if (!checkGatt())
            return false;

        BluetoothGattDescriptor clientCfg = characteristic.getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
        if(clientCfg == null)
            return false;

        return clientCfg.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
    }

    public boolean connect(final String addr) {
        if(mBluetoothAdapter == null || addr == null) {
            return false;

        }

        Log.e(TAG, "mBluetoothAdapter = " + mBluetoothAdapter);

        final BluetoothDevice dev = mBluetoothAdapter.getRemoteDevice(addr);

        int connectionState = mBluetoothManager.getConnectionState(dev, BluetoothProfile.GATT);

        if(connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            // Previously connected device, try to connect.
            if(mBtDevAddr != null && addr.equals(mBtDevAddr) && mBluetoothGatt != null) {
                if(mBluetoothGatt.connect()) {
                    return true;
                } else {
                    return false;
                }
            }

            if(dev == null) {
                return false;
            }
            mBluetoothGatt = dev.connectGatt(this, false, mGattCallback);
            mBtDevAddr = addr;

        } else {
            return false;
        }
        return true;


    }

    public void disconnect(String addr) {
        if(mBluetoothAdapter == null) {
            return;
        }
        final BluetoothDevice dev = mBluetoothAdapter.getRemoteDevice(addr);
        int connectionState = mBluetoothManager.getConnectionState(dev, BluetoothProfile.GATT);

        if(mBluetoothGatt != null) {
            if(connectionState != BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothGatt.disconnect();
            } else {

            }
        }
    }

    public void close() {

        if(mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    public int numOfConnectedDev() {
        int num = 0;

        if(mBluetoothGatt != null) {
            List<BluetoothDevice> devList;
            devList = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
            num = devList.size();
        }
        return num;
    }

    public static BluetoothGatt getBtGatt() {
        return mThis.mBluetoothGatt;
    }

    public static BluetoothManager getBtManager() {
        return mThis.mBluetoothManager;
    }

    public static BleService getBleInstance() {
        return mThis;
    }

    public void waitIdle(int timeout) {
        while(timeout-- > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean refreshDevCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt bluetoothGatt = gatt;
            Method method = bluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if(method != null) {
                boolean bool = ((Boolean) method.invoke(bluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        }
        catch(Exception exception) {
            Log.e(TAG, "An exception occured while refreshing device");
        }

        return false;
    }

    public void timedDisconnect() {
        DisconnectTimerTask disconnectTimerTask;
        this.disconnectionTimer = new Timer();
        disconnectTimerTask = new DisconnectTimerTask(this);
        this.disconnectionTimer.schedule(disconnectTimerTask, 20000);
    }

    public void abortTimedDisconnect() {
        if(this.disconnectionTimer != null) {
            this.disconnectionTimer.cancel();
        }
    }

    public class DisconnectTimerTask extends TimerTask {
        BleService param;
        public DisconnectTimerTask(final BleService param) {
            this.param = param;
        }

        @Override
        public void run() {
            this.param.disconnect(mBtDevAddr);
        }
    }

    public boolean reqConnectionPri(int connectionPri) {
        return this.mBluetoothGatt.requestConnectionPriority(connectionPri);
    }


    private void executeQueue() {
        lock.lock();
        if(currBleReq != null) {
            try {
                currBleReq.currTimeout++;
                if(currBleReq.currTimeout > GATT_TIMEOUT) {
                    currBleReq.status = bleReqStatus.timeout;
                    currBleReq = null;
                }

                Thread.sleep(10, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            lock.unlock();

            return;
        }

        if(procQueue == null) {
            lock.unlock();
            return;
        }

        if(procQueue.size() == 0) {
            lock.unlock();
            return;
        }

        BleReq procReq = procQueue.removeFirst();

        switch (procReq.bleReqOp) {
            case rd:
                break;

            case rdBlocking:
                if(procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                procReq.currTimeout = 0;
                currBleReq = procReq;

                int stat = sendBlockingReadReq(procReq);

                if(stat == -2) {
                    lock.unlock();
                    return;
                }
                break;

            case wr:
                nonBlockQueue.add(procReq);
                sendNonBlockingWriteReq(procReq);
                break;

            case wrBlocking:
                if(procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                currBleReq = procReq;
                stat = sendBlockingWriteReq(procReq);

                if(stat == -2) {
                    Log.d(TAG,"executeQueue wrBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;

            case nsBlocking:
                if(procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }

                currBleReq = procReq;

                stat = sendBlockingNotifySetting(procReq);

                if(stat == -2) {
                    Log.d(TAG,"executeQueue wrBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }

                break;

            default:
                break;

        }

        lock.unlock();
    }

    public int sendNonBlockingReadReq(BleReq req) {

        req.status = bleReqStatus.processing;
        if(!checkGatt()) {
            req.status = bleReqStatus.failed;
            return -2;
        }

        mBluetoothGatt.readCharacteristic(req.characteristic);
        return 0;
    }

    public int sendNoneBlockingWriteReq(BleReq req) {
        req.status = bleReqStatus.processing;
        if(!checkGatt()) {
            req.status = bleReqStatus.failed;
            return -2;
        }
        mBluetoothGatt.writeCharacteristic(req.characteristic);

        return 0;
    }

    public int sendBlockingReadReq(BleReq req) {
        req.status = bleReqStatus.processing;
        int timeout = 0;
        if(!checkGatt()) {
            req.status = bleReqStatus.failed;
            return -2;
        }
        mBluetoothGatt.readCharacteristic(req.characteristic);
        this.blockingFlag = true;
        while(this.blockingFlag) {
            timeout++;
            waitIdle(1);
            if(timeout > GATT_TIMEOUT) {
                this.blockingFlag = false;
                req.status = bleReqStatus.timeout;
                return -1;

            }

        }
        req.status = bleReqStatus.done;
        return lastGattStatus;
    }

    public int sendNonBlockingWriteReq(BleReq req) {

        req.status = bleReqStatus.processing;
        int timeout = 0;
        if(!checkGatt()) {
            req.status = bleReqStatus.failed;
            return -2;
        }
        mBluetoothGatt.writeCharacteristic(req.characteristic);
        this.blockingFlag = true;
        while(this.blockingFlag) {
            timeout++;
            waitIdle(1);
            if(timeout > GATT_TIMEOUT) {
                this.blockingFlag = false;
                req.status = bleReqStatus.timeout;
                return -1;

            }

        }
        req.status = bleReqStatus.done;
        return lastGattStatus;
    }

    public int sendBlockingWriteReq(BleReq req) {

        req.status = bleReqStatus.processing;
        int timeout = 0;
        if(!checkGatt()) {
            req.status = bleReqStatus.failed;
            return -2;
        }
        mBluetoothGatt.writeCharacteristic(req.characteristic);
        this.blockingFlag = true;
        while(this.blockingFlag) {
            timeout++;
            waitIdle(1);
            if(timeout > GATT_TIMEOUT) {
                this.blockingFlag = false;
                req.status = bleReqStatus.timeout;
                return -1;

            }

        }
        req.status = bleReqStatus.done;
        return lastGattStatus;

    }

    public int sendBlockingNotifySetting(BleReq req) {
        req.status = bleReqStatus.processing;

        int timeout = 0;
        if(req.characteristic == null) {
            return -1;
        }
        if(!checkGatt()) {
            return -2;
        }

        if(mBluetoothGatt.setCharacteristicNotification(req.characteristic, req.notifyenable)) {
            BluetoothGattDescriptor clientCfg = req.characteristic.getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);

            if(clientCfg != null) {
                if(req.notifyenable) {

                    clientCfg.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    clientCfg.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                mBluetoothGatt.writeDescriptor(clientCfg);
                this.blockingFlag = true;
                while(this.blockingFlag) {
                    timeout++;
                    waitIdle(1);
                    if(timeout > GATT_TIMEOUT) {
                        this.blockingFlag = false;
                        req.status = bleReqStatus.timeout;

                    }

                }
                req.status = bleReqStatus.done;
                return lastGattStatus;
            }
        }
        // wrong
        return -3;
    }


}
