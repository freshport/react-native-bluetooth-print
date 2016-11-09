package com.novacloud.btprint.react;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;


/**
 * Created by Nova on 16/10/21.
 */

public class BluetoothPrintNativeModule extends ReactContextBaseJavaModule {

    public BluetoothPrintNativeModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "BluetoothPrint";
    }

    @ReactMethod
    public void orderPrint(ReadableArray array) throws Exception {
        Command.print(array);
    }

    @ReactMethod
    public void hasConnectedToAPrinter(Callback callback) {
        BluetoothService bluetoothService = BluetoothService.getInstance(null);
        callback.invoke(null, bluetoothService.getState() == bluetoothService.STATE_CONNECTED);
    }

    @ReactMethod
    public void setDelay(int delay) {
        BluetoothService bluetoothService = BluetoothService.getInstance(null);
        bluetoothService.delay = delay;
    }

}
