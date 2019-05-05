package com.sovell.retail_cabinet.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

public class UsbPermission {

    private Context mContext;
    private boolean UsbPermissionPass=false;

    /**
     * 获得 usb 权限
     */
    public UsbPermission(Context context) {
        this.mContext = context;
        //before open usb device
        //should try to get usb permission
        tryGetUsbPermission();
    }

    UsbManager mUsbManager;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private void tryGetUsbPermission() {
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        mContext.registerReceiver(mUsbPermissionActionReceiver, filter);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);

        //here do emulation to ask all connected usb device for permission
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            //add some conditional check if necessary
            //if(isWeCaredUsbDevice(usbDevice)){
            if (mUsbManager.hasPermission(usbDevice)) {
                //if has already got permission, just goto connect it
                //that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
                //and also choose option: not ask again
                afterGetUsbPermission(usbDevice);
                UsbPermissionPass=true;
            } else {
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);

            }
            //}
        }
    }

    public void setUsbPermissionPass( ) {
        tryGetUsbPermission();
    }

    public boolean getUsbPermissionState(){
        return UsbPermissionPass;
    }

    public void unRegistered() {
        mContext.unregisterReceiver(mUsbPermissionActionReceiver);
    }

    private void afterGetUsbPermission(UsbDevice usbDevice) {
        //call method to set up device communication
        //Toast.makeText(this, String.valueOf("Got permission for usb device: " + usbDevice), Toast.LENGTH_LONG).show();
        //Toast.makeText(this, String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()), Toast.LENGTH_LONG).show();

        doYourOpenUsbDevice(usbDevice);

    }

    private void doYourOpenUsbDevice(UsbDevice usbDevice) {
        //now follow line will NOT show: User has not given permission to device UsbDevice
        UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);

        //add your operation code here
    }

    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if (null != usbDevice) {
                            afterGetUsbPermission(usbDevice);
                        }
                    } else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        Toast.makeText(context, String.valueOf("Permission denied for device" + usbDevice), Toast.LENGTH_LONG).show();
                        UsbPermissionPass=false;
                    }
                }
            }
        }
    };
}
