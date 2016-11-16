package com.novacloud.btprint.react;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import java.util.Collections;
import java.util.Set;

/**
 * Created by Nova on 16/10/24.
 */

public class BluetoothPrintViewManager extends SimpleViewManager<View> {

    public static final String REACT_CLASS = "BluetoothPrintView";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    private View deviceListView;

    private static final boolean D = true;
    private static final String TAG = "BlueToothDeviceListView";
    private static final int REQUEST_ENABLE_BLUETOOTH = 2;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private String mConnectedDeviceName = null;
    private ThemedReactContext context;
    private BluetoothService bluetoothService;
    private TextView hintTextView;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    @Override
    public View createViewInstance(ThemedReactContext context) {
        this.context = context;

        deviceListView = LayoutInflater.from(context).inflate(R.layout.device_list, null);
        hintTextView = (TextView)deviceListView.findViewById(R.id.title_hint);

        bluetoothService = BluetoothService.getInstance(mHandler);

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(context, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(context, R.layout.device_name);

        ListView pairedListView = (ListView) deviceListView.findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        ListView newDevicesListView = (ListView) deviceListView.findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        context.addActivityEventListener(activityEventListener);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBtAdapter.enable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH, new Bundle());
        } else {
            init();
        }


        return deviceListView;
    }

    private void init() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver, filter);
        Set<BluetoothDevice> pairedDevices;
        if (mBtAdapter != null) {
            pairedDevices = mBtAdapter.getBondedDevices();
        } else {
            pairedDevices = Collections.emptySet();
        }

        if (pairedDevices.size() > 0) {
            deviceListView.findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = deviceListView.getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }

        doDiscovery();
    }

    @Override
    public void onDropViewInstance(View view) {
        context.unregisterReceiver(mReceiver);
        context.removeActivityEventListener(activityEventListener);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (mBtAdapter == null) return;
        if (D) Log.d(TAG, "doDiscovery()");

        // Turn on sub-title for new devices
        deviceListView.findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String noDevices = deviceListView.getResources().getText(R.string.none_paired).toString();
            String noNewDevice = deviceListView.getResources().getText(R.string.none_found).toString();
            Log.i(TAG, info);

            if (!info.equals(noDevices) && !info.equals(noNewDevice)) {
                String address = info.substring(info.length() - 17);
                bluetoothService.connect( mBtAdapter.getRemoteDevice(address));
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                setProgressBarIndeterminateVisibility(false);
//                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = deviceListView.getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    private final ActivityEventListener activityEventListener = new ActivityEventListener() {
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQUEST_ENABLE_BLUETOOTH:
                    if (resultCode == -1) {
                        init();
                    }
                    break;
            }
        }
    };
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.d(TAG, "STATE_CONNECTED");
                            hintTextView.setText("已连接到：" + bluetoothService.getConnectedDeviceName());
//                            mTitle.setText(R.string.title_connected_to);
//                            mTitle.append(mConnectedDeviceName);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d(TAG, "STATE_CONNECTING");
                            hintTextView.setText("正在连接");
//                            mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Log.d(TAG, "STATE_NONE");
                            hintTextView.setText("未连接");
//                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    // byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    // String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    // byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    // String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(context,
                            "Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(context,
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };
}

