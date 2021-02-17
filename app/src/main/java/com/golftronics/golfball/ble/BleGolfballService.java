/*
Copyright (c) 2020, Golftronics, LLC
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, is not permitted.

*

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.



 */

package com.golftronics.golfball.ble;

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
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;


/**
 * Service for managing connection and data communication with the BLE car
 */
public class    BleGolfballService extends Service {

    private final static String TAG = BleGolfballService.class.getSimpleName();

    public enum Motor { LEFT, RIGHT, MIDDLE, BOTTOM, VELOCITY, PUTTMADE, POWER}

    private static BluetoothManager mBluetoothManager;
    private static BluetoothAdapter mBluetoothAdapter;
    private static String mBluetoothDeviceAddress;
    private static BluetoothGatt mBluetoothGatt;

    //  Queue for BLE events
    //  This is needed so that rapid BLE events don't get dropped
    private static final Queue<Object> BleQueue = new LinkedList<>();

    // UUID for the custom motor characteristics
    private static final String baseUUID =           "00000000-0000-1000-8000-00805f9b34f";
    private static final String motorServiceUUID =   baseUUID + "0";
    private static final String puttMadeCharUUID =   baseUUID + "2";
    private static final String lastMissedCharUUID =   baseUUID + "3";
    private static final String tachRightCharUUID =  baseUUID + "6";
    private static final String tachMiddleCharUUID =  baseUUID + "7";
    private static final String tachBottomCharUUID =  baseUUID + "4";
    private static final String VelocityCharUUID =  baseUUID + "8";
    public static final String ledCharacteristicUUID =  baseUUID + "9";
    public static final String ReadyCharacteristicUUID =  baseUUID + "1";
    private static final String PowerCharUUID =   baseUUID + "5";
    private static final String CCCD_UUID =          "00002902-0000-1000-8000-00805f9b34fb";

    // Bluetooth Characteristics that we need to read/write

    private static BluetoothGattCharacteristic mLastMissedCharacteristic;
    private static BluetoothGattCharacteristic mTachRightCharacteristic;
    private static BluetoothGattCharacteristic mTachMiddleCharacteristic;
    private static BluetoothGattCharacteristic mTachBottomCharacteristic;
    private static BluetoothGattCharacteristic mrollDistanceCharacteristic;
    private static BluetoothGattCharacteristic mVelocityCharacteristic;
    private static BluetoothGattCharacteristic mLedCharacteristic;
    private static BluetoothGattCharacteristic mputtMadeCharacteristic;
    private static BluetoothGattCharacteristic mReadyCharacteristic;
    private static BluetoothGattCharacteristic mPowerCharacteristic;

    // State (on/off), speed of the motors, and tach values

    private static int motorLastMissed;
    private static int motorRightTach;
    private static int motorMiddleTach;
    private static int motorBottomTach;
    private static int motorVelocity;
    private static int motorVelocityOld;
    private static int motorputtMade;
    private static int motorputtMadeOld;
    private static int motorPower;

    private static boolean mLedSwitchState = false;
    private static boolean mReadySwitchState = false;

    // Actions used during broadcasts to the activity
    public static final String ACTION_CONNECTED =
            "com.golftronics.golfball.ble.ACTION_GATT_CONNECTED";
    public static final String ACTION_DISCONNECTED =
            "com.golftronics.golfball.ble.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_DATA_AVAILABLE =
            "com.golftronics.golfball.ble.ACTION_DATA_AVAILABLE";

    /**
     * This is a binder for the BluetoothLeService
     */
    public class LocalBinder extends Binder {
        BleGolfballService getService() {
            return BleGolfballService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Disconnect from the GATT database and close the connection
        disconnect();
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Implements callback methods for GATT events.
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * This is called on a connection state change (either connection or disconnection)
         * @param gatt The GATT database object
         * @param status Status of the event
         * @param newState New state (connected or disconnected)
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                broadcastUpdate(ACTION_CONNECTED);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(ACTION_DISCONNECTED);
            }
        }

        /**
         * This is called when service discovery has completed.
         *
         * It broadcasts an update to the main activity.
         *
         * @param gatt The GATT database object
         * @param status Status of whether the discovery was successful.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                // Get the characteristics for the motor service
                BluetoothGattService gattService = mBluetoothGatt.getService(UUID.fromString(motorServiceUUID));
                if (gattService == null) return; // return if the motor service is not supported

                mLastMissedCharacteristic = gattService.getCharacteristic(UUID.fromString(lastMissedCharUUID));
                mTachRightCharacteristic = gattService.getCharacteristic(UUID.fromString(tachRightCharUUID));
                mTachMiddleCharacteristic = gattService.getCharacteristic(UUID.fromString(tachMiddleCharUUID));
                mTachBottomCharacteristic = gattService.getCharacteristic(UUID.fromString(tachBottomCharUUID));
                mVelocityCharacteristic = gattService.getCharacteristic(UUID.fromString(VelocityCharUUID));
                mLedCharacteristic = gattService.getCharacteristic(UUID.fromString(ledCharacteristicUUID));
                mputtMadeCharacteristic = gattService.getCharacteristic(UUID.fromString(puttMadeCharUUID));
                mReadyCharacteristic = gattService.getCharacteristic(UUID.fromString(ReadyCharacteristicUUID));
                mPowerCharacteristic = gattService.getCharacteristic(UUID.fromString(PowerCharUUID));



                // Set the CCCD to notify us for the two tach readings
                setCharacteristicNotification(mLastMissedCharacteristic, true);
                setCharacteristicNotification(mTachRightCharacteristic, true);
                setCharacteristicNotification(mTachMiddleCharacteristic, true);
                setCharacteristicNotification(mTachBottomCharacteristic, true);
                setCharacteristicNotification(mVelocityCharacteristic, true);
                setCharacteristicNotification(mputtMadeCharacteristic, true);
                setCharacteristicNotification(mReadyCharacteristic, true);
                setCharacteristicNotification(mPowerCharacteristic, true);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * This handles the BLE Queue. If the queue is not empty, it starts the next event.
         */
        private void handleBleQueue() {
            if(BleQueue.size() > 0) {
                // Determine which type of event is next and fire it off
                if (BleQueue.element() instanceof BluetoothGattDescriptor) {
                    mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor) BleQueue.element());
                } else if (BleQueue.element() instanceof BluetoothGattCharacteristic) {
                    mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic) BleQueue.element());
                }
            }
        }

        /**
         * This is called when a characteristic write has completed. Is uses a queue to determine if
         * additional BLE actions are still pending and launches the next one if there are.
         *
         * @param gatt The GATT database object
         * @param characteristic The characteristic that was written.
         * @param status Status of whether the write was successful.
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            // Pop the item that was written from the queue
            BleQueue.remove();
            // See if there are more items in the BLE queues
            handleBleQueue();
        }

        /**
         * This is called when a CCCD write has completed. It uses a queue to determine if
         * additional BLE actions are still pending and launches the next one if there are.
         *
         * @param gatt The GATT database object
         * @param descriptor The CCCD that was written.
         * @param status Status of whether the write was successful.
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            // Pop the item that was written from the queue
            BleQueue.remove();
            // See if there are more items in the BLE queues
            handleBleQueue();
        }

        /**
         * This is called when a characteristic with notify set changes.
         * It broadcasts an update to the main activity with the changed data.
         *
         * @param gatt The GATT database object
         * @param characteristic The characteristic that was changed
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            // Get the UUID of the characteristic that changed
            String uuid = characteristic.getUuid().toString();

            // Update the appropriate variable with the new value.
            switch (uuid) {
                case lastMissedCharUUID:
                    motorLastMissed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0);
                    break;
                case tachRightCharUUID:
                    motorRightTach = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0);
                    break;
                case tachMiddleCharUUID:
                    motorMiddleTach = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0);
                    break;
                case tachBottomCharUUID:
                    motorBottomTach = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0);
                    break;
                case VelocityCharUUID:
                    motorVelocity = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0);
                    break;
                case puttMadeCharUUID:
                    motorputtMade = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0);
                    break;
                case PowerCharUUID:
                    motorPower = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0);



                    break;
            }
            // Tell the activity that new car data is available
            broadcastUpdate(ACTION_DATA_AVAILABLE);
        }
    };


    /**
     * Sends a broadcast to the listener in the main activity.
     *
     * @param action The type of action that occurred.
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    /**
     * Initialize a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }



        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            return mBluetoothGatt.connect();
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        Log.i(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * This method is used to read the state of the switch from the device
     *
     */


    /**
     * This method is used to switch the device into bootloader mode
     *
     * @param value Turns the bootloader mode on (1) or off(0)
     *
     */
    public void writeLedCharacteristic(boolean value){
        byte[] byteVal = new byte[1];
        if (value){
            byteVal[0] = (byte) (1);
        } else {
            byteVal[0] = (byte) (0);
        }
        Log.i(TAG, "LED" + value);
        mLedSwitchState = value;
        mLedCharacteristic.setValue(byteVal);
        mBluetoothGatt.writeCharacteristic(mLedCharacteristic);
    }

    public void writeReadyCharacteristic(boolean value){
        byte[] byteVal = new byte[1];
        if (value){
            byteVal[0] = (byte) (1);
        } else {
            byteVal[0] = (byte) (0);
        }
        Log.i(TAG, "READY" + value);
        mReadySwitchState = value;
        mReadyCharacteristic.setValue(byteVal);
        mBluetoothGatt.writeCharacteristic(mReadyCharacteristic);
    }

    /**
     * This method returns the state of the bootloader mode
     *
     * @return the value of the bootloader mode
     */
    public boolean getLedSwitchState() {
        return mLedSwitchState;
    }
    public boolean getReadySwitchState() {
        return mReadySwitchState;
    }





    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}.
     *
     * @param characteristic The characteristic to write.
     */
    private void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BleQueue.add(characteristic);
        if (BleQueue.size() == 1) {
            mBluetoothGatt.writeCharacteristic(characteristic);
            Log.i(TAG, "Writing Characteristic");
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.i(TAG, "BluetoothAdapter not initialized");
            return;
        }

        /* Enable or disable the callback notification on the phone */
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        /* Set CCCD value locally and then write to the device to register for notifications */
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(CCCD_UUID));
        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        // Put the descriptor into the write queue
        BleQueue.add(descriptor);
        // If there is only 1 item in the queue, then write it. If more than one, then the callback
        // will handle it
        if (BleQueue.size() == 1) {
            mBluetoothGatt.writeDescriptor(descriptor);
            Log.i(TAG, "Writing Notification");
        }
    }






    /**
     * Get the tach reading for one of the motors
     *
     * @param motor to operate on
     * @return tach value
     */
    public static int getTach(Motor motor) {
        if (motor == Motor.LEFT) {
            return motorLastMissed;
        }
        if (motor == Motor.BOTTOM) {
            return motorBottomTach;
        }

        if (motor == Motor.VELOCITY) {
            return motorVelocity;
        }
        if (motor == Motor.PUTTMADE) {
            return motorputtMade;
        }

        if (motor == Motor.POWER) {
            return motorPower;
        }

        if (motor == Motor.MIDDLE) {
            return motorMiddleTach;

        }else { // Motor == RIGHT) {
            return motorRightTach;
        }
    }

    /**
     * This function returns the UUID of the motor service
     *
     * @return the motor service UUID
     */
    public static UUID getMotorServiceUUID() {
        return UUID.fromString(BleGolfballService.motorServiceUUID);
    }
}
