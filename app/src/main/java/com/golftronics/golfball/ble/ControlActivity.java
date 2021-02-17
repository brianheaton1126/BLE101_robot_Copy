/*
Copyright (c) 2020, Golftronics, LLC
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification is not permitted.


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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Button;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.widget.Toast;

import java.util.List;

/**
 * This Activity provides the user interface to control the robot.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */







public class ControlActivity extends AppCompatActivity implements BlankFragment.OnFragmentInteractionListener  {



    private PuttViewModel puttViewModel;

    // Objects to access the layout items for Tach, Buttons, and Seek bars
    public static TextView mLastMissedText;
    private static TextView mTachRightText;
    private static TextView mTachMiddleText;
    private static TextView mTachBottomText;
    private static TextView mVelocityText;
    private static TextView mVelocityTextOld;
    private static Switch mled_switch;

    public static TextView mrollDistanceText;
    private static TextView mputtMadeText;
    private static TextView mputtMadeTextOld;
    private static TextView mPowerText;
    private static TextView mVelocityFallOff;
    private static TextView mvelocityMax;
    public static double Velocity2;
    public static double maxAccelx;
    public static double maxAccely;
    public static double maxAccelz;
    public static double xzVector;
    public static double xyVector;

    public static double puttVelocity;
    public static double puttVelocityOld;
    public static Button ready_button;
    public static Button stats_button;
    public static int puttMadeOld;
    public static double puttRollDistanceOld = 0.0;
    public static int playingSound = 0;
    public int numberRollsOld;
    public static int playingBluetoothDisconnected = 0;
    public static int playingSoundStroke = 0;
    public static int playingReadySound = 0;
    public static int bluetoothDiconnectFlag = 0;
    public static int playingBluetoothConnected = 0;
    public static double longestMadeDistanceRoundOld = 0.0;
    public static int playingBadReadingSound = 0;
    public static int puttMadeFlag = 0;
    public static int badReadingFlag = 0;
    public static int ballStoppedFlag = 0;

    private FrameLayout fragmentContainer;

    public int puttNumber = 0;
    public double longestMadeDistanceOld = 0.0;
    public Boolean puttIsMade = false;
    public double longestMadeDistanceMax = 0.0;
    public double velocityChangePerFoot = 0.0;
    public double slope = 0.0;
    public String slopeSelected;
    public double velocityFallOff = 0;







        // This tag is used for debug messages
    private static final String TAG = ControlActivity.class.getSimpleName();

    private static String mDeviceAddress;
    private static BleGolfballService mBleGolfballService;

    /**
     * This manages the lifecycle of the BLE service.
     * When the service starts we get the service object, initialize the service, and connect.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mBleGolfballService = ((BleGolfballService.LocalBinder) service).getService();
            if (!mBleGolfballService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the car database upon successful start-up initialization.
            mBleGolfballService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleGolfballService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_control);

        mLastMissedText = (TextView) findViewById(R.id.last_missed);

        puttViewModel = new ViewModelProvider(this, ViewModelProvider
                .AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(PuttViewModel.class);

        /*puttViewModel.getAllPutts().observe(this, new Observer<List<PuttData>>() {
            @Override
            public void onChanged(List<PuttData> puttData) {


            }




        });*/

        puttViewModel.getAllLongPutts().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                mLastMissedText.setText(String.valueOf(integer));
            }
        });



        /*puttViewModel.getAllLongPutts().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {


                mLastMissedText.setText(integer);

            }
        });*/









        // Assign the various layout objects to the appropriate variables
        mLastMissedText = (TextView) findViewById(R.id.last_missed);
        mTachRightText = (TextView) findViewById(R.id.tach_right);
        mTachMiddleText = (TextView) findViewById(R.id.tach_middle);
        mTachBottomText = (TextView) findViewById(R.id.tach_bottom);
        mVelocityText = (TextView) findViewById(R.id.Velocity);
        mputtMadeText = (TextView) findViewById(R.id.puttMade);
        mPowerText = (TextView) findViewById(R.id.Power);
        mrollDistanceText = (TextView) findViewById(R.id.rollDistance);
        mVelocityFallOff = (TextView) findViewById(R.id.velocity_falloff);
        mvelocityMax = (TextView) findViewById(R.id.velocity_max);

        mled_switch = (Switch) findViewById(R.id.led_switch);
        ready_button = (Button) findViewById(R.id.ready_button);
        stats_button = (Button) findViewById(R.id .stats);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(ScanActivity.EXTRAS_BLE_ADDRESS);


        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);




        Spinner spinner = (Spinner) findViewById(R.id.spinner);


        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.slope_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.getSelectedItem();


        

        AudioManager audioManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);






        // Bind to the BLE service
        Log.i(TAG, "Binding Service");
        Intent RobotServiceIntent = new Intent(this, BleGolfballService.class);
        bindService(RobotServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        /* This will be called when the LED On/Off switch is touched */
        mled_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                mBleGolfballService.writeLedCharacteristic(isChecked);
            }
        });

        ready_button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v){
                mBleGolfballService.writeReadyCharacteristic(true);
            }
        });

        stats_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment();
            }
        });


        if(puttMadeFlag == 1){


            Toast.makeText(this, "test",Toast.LENGTH_SHORT).show();

        }


    } /* End of onCreate method */


    public void openFragment(){
        BlankFragment fragment = BlankFragment.newInstance();
        FragmentManager fragmentManager =getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.add(R.id.fragment_container, fragment, "BLANK_FRAGMENT").commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mRobotUpdateReceiver, makeRobotUpdateIntentFilter());
        if (mBleGolfballService != null) {
            final boolean result = mBleGolfballService.connect(mDeviceAddress);
            Log.i(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mRobotUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBleGolfballService = null;
    }







    /**
     * Handle broadcasts from the ball service object. The events are:
     * ACTION_CONNECTED: connected to the ball.
     * ACTION_DISCONNECTED: disconnected from the ball.
     * ACTION_DATA_AVAILABLE: received data from the ball.  This can be a result of a read
     * or notify operation.
     */
    private final BroadcastReceiver mRobotUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BleGolfballService.ACTION_CONNECTED:
                    // No need to do anything here. Service discovery is started by the service.
                    playingBluetoothDisconnected = 0;

                    if (bluetoothDiconnectFlag == 1 && playingBluetoothConnected == 0) {
                        final MediaPlayer bluetoothconnected = MediaPlayer.create(getApplicationContext(), R.raw.bluetoothconnected);


                        if (!bluetoothconnected.isPlaying()) {
                            bluetoothconnected.start();
                        }

                        playingBluetoothConnected = 1;
                        bluetoothDiconnectFlag = 0;
                    }



                    break;
                case BleGolfballService.ACTION_DISCONNECTED:
                    /**mPSoCBleRobotService.close();*/

                    bluetoothDiconnectFlag = 1;
                    if (playingBluetoothDisconnected == 0) {
                        final MediaPlayer bluetoothdisconnected = MediaPlayer.create(getApplicationContext(), R.raw.bluetoothdisconnected);
                        playingBluetoothDisconnected = 1;
                        playingBluetoothConnected = 0;

                        if (!bluetoothdisconnected.isPlaying()) {
                            bluetoothdisconnected.start();
                        }
                    }
                    mBleGolfballService.connect(mDeviceAddress);
                    break;
                case BleGolfballService.ACTION_DATA_AVAILABLE:
                    // This is called after a Notify completes
                    //mLastMissedText.setText(String.format("%d", (BleGolfballService.getTach(BleGolfballService.Motor.LEFT) / 2)));
                    mTachRightText.setText(String.format("%d", BleGolfballService.getTach(BleGolfballService.Motor.RIGHT)));
                    mTachMiddleText.setText(String.format("%d", BleGolfballService.getTach(BleGolfballService.Motor.MIDDLE)));


                    double puttRollDistance = (double) BleGolfballService.getTach(BleGolfballService.Motor.LEFT) * 0.22;
                    double puttRollDistanceRound = Math.round(puttRollDistance * 100) / 100D;
                    double puttRollDistanceCompensated = Math.round(puttRollDistance * 100 * 1.1)/ 100D;
                    int puttRollDistanceRoundforSpeech = (int) Math.round(puttRollDistanceRound);
                    String textputtRollDistance = Double.toString(puttRollDistanceRound);
                    String textputtRollDistanceCompensated = Double.toString(puttRollDistanceCompensated);

                    int numberRolls = (BleGolfballService.getTach(BleGolfballService.Motor.LEFT));



                    double longestMadeDistance = (double) BleGolfballService.getTach(BleGolfballService.Motor.BOTTOM)/10.0;
                    double longestMadeDistanceRound = Math.round(longestMadeDistance * 100) / 100D;

                    if(longestMadeDistanceRound != longestMadeDistanceOld && numberRolls > 2 ){

                        if (longestMadeDistanceRound > longestMadeDistanceOld){
                            longestMadeDistanceMax = longestMadeDistanceRound;
                        }



                        PuttData puttData = new PuttData(System.currentTimeMillis(),"Michael",puttNumber,longestMadeDistanceRound,puttRollDistanceCompensated,2,9.5,
                                false,3.0,12.0,1.0, "test");

                        puttViewModel.insert(puttData);





                        longestMadeDistanceOld = longestMadeDistanceRound;
                    }


                    String textlongestMadeDistance = Double.toString(longestMadeDistanceRound);
                    mTachBottomText.setText(textlongestMadeDistance);

                    String textlongestMadeDistanceMax = Double.toString(longestMadeDistanceMax);
                    mvelocityMax.setText(textlongestMadeDistanceMax);



                   /** mputtMadeText.setText(String.format("%d", BleGolfballService.getTach(BleGolfballService.Motor.PUTTMADE)));*/
                    int ballStartRoll =  (BleGolfballService.getTach(BleGolfballService.Motor.PUTTMADE));
                    if (ballStartRoll == 0 && longestMadeDistance == 0.0){
                        ballStoppedFlag = 1;
                    }
                    else{
                        ballStoppedFlag = 0;
                    }
                    if (ballStoppedFlag == 1 ){
                        playingSoundStroke = 0;
                    }

                    mPowerText.setText(String.format("%d", BleGolfballService.getTach(BleGolfballService.Motor.POWER)));

                    maxAccelx = (Math.abs(102.0 - (double) BleGolfballService.getTach(BleGolfballService.Motor.MIDDLE)) / 19.6);
                    maxAccely = (Math.abs(102.0 - (double) BleGolfballService.getTach(BleGolfballService.Motor.RIGHT)) / 19.6);
                    maxAccelz = (Math.abs(125.0 - (double) BleGolfballService.getTach(BleGolfballService.Motor.BOTTOM)) / 20.5);

                    xzVector = Math.sqrt((maxAccelx * maxAccelx) + (maxAccelz * maxAccelz));





                    if (ballStoppedFlag == 0 && numberRolls >2) {

                        playingReadySound = 0;




                    }

                    mrollDistanceText.setText(textputtRollDistanceCompensated);


                    /**final MediaPlayer threeftsix = MediaPlayer.create(getApplicationContext(), R.raw.threeftsix);
                    final MediaPlayer twofeet = MediaPlayer.create(getApplicationContext(), R.raw.twofeet);
                    final MediaPlayer fourfeet = MediaPlayer.create(getApplicationContext(), R.raw.fourfeet);
                    final MediaPlayer fivefeet = MediaPlayer.create(getApplicationContext(), R.raw.fivefeet);
                    final MediaPlayer sixfeet = MediaPlayer.create(getApplicationContext(), R.raw.sixfeet);
                    final MediaPlayer sevenfeet = MediaPlayer.create(getApplicationContext(), R.raw.sevenfeet);
                    final MediaPlayer eightfeet = MediaPlayer.create(getApplicationContext(), R.raw.eightfeet);
                    final MediaPlayer ninefeet = MediaPlayer.create(getApplicationContext(), R.raw.ninefeet);
                    final MediaPlayer tenfeet = MediaPlayer.create(getApplicationContext(), R.raw.tenfeet);*/


                    if (puttRollDistance == 0) {
                        playingSound = 0;

                        if (playingReadySound == 0) {
                            final MediaPlayer ballready = MediaPlayer.create(getApplicationContext(), R.raw.ballready);

                            if (!ballready.isPlaying()) {
                                ballready.start();

                            }

                            badReadingFlag = 0;
                            puttMadeFlag = 0;

                            puttNumber ++;

                            playingReadySound = 1;

                        }
                    }

                    if ((  (((ballStoppedFlag == 1 && longestMadeDistanceRoundOld > 0.9) || (ballStoppedFlag == 0 && longestMadeDistanceRoundOld > 0.9))&& playingBadReadingSound ==0)&& puttMadeFlag == 0)&& numberRolls > 2){

                        final MediaPlayer badreading = MediaPlayer.create(getApplicationContext(), R.raw.badreading);

                        playingBadReadingSound = 1;
                        badReadingFlag = 1;

                        if (!badreading.isPlaying()) {
                            badreading.start();
                        }

                    }



                    /**if (puttRollDistanceCompensated > 1.5 && puttRollDistanceCompensated <= 2.5 && playingSound == 0 && ballStoppedFlag == 1) {

                        final MediaPlayer twofeet = MediaPlayer.create(getApplicationContext(), R.raw.twofeet);

                        playingSound = 1;

                        if (!twofeet.isPlaying()) {
                            twofeet.start();
                        }

                    }*/

                    if (badReadingFlag == 0) {

                        if (puttRollDistanceCompensated > 2.5 && puttRollDistanceCompensated <= 3.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer threefeet = MediaPlayer.create(getApplicationContext(), R.raw.threefeet);

                            playingSound = 1;

                            if (!threefeet.isPlaying()) {
                                threefeet.start();
                            }
                        }


                        if (puttRollDistanceCompensated > 3.5 && puttRollDistanceCompensated <= 4.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer fourfeet = MediaPlayer.create(getApplicationContext(), R.raw.fourfeet);

                            playingSound = 1;

                            if (!fourfeet.isPlaying()) {
                                fourfeet.start();
                            }
                        }


                        if (puttRollDistanceCompensated > 4.5 && puttRollDistanceCompensated <= 5.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer fivefeet = MediaPlayer.create(getApplicationContext(), R.raw.fivefeet);

                            playingSound = 1;

                            if (!fivefeet.isPlaying()) {
                                fivefeet.start();
                            }
                        }


                        if (puttRollDistanceCompensated > 5.5 && puttRollDistanceCompensated <= 6.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer sixfeet = MediaPlayer.create(getApplicationContext(), R.raw.sixfeet);

                            playingSound = 1;

                            if (!sixfeet.isPlaying()) {
                                sixfeet.start();
                            }
                        }


                        if (puttRollDistanceCompensated > 6.5 && puttRollDistanceCompensated <= 7.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer sevenfeet = MediaPlayer.create(getApplicationContext(), R.raw.sevenfeet);

                            playingSound = 1;

                            if (!sevenfeet.isPlaying()) {
                                sevenfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 7.5 && puttRollDistanceCompensated <= 8.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer eightfeet = MediaPlayer.create(getApplicationContext(), R.raw.eightfeet);

                            playingSound = 1;

                            if (!eightfeet.isPlaying()) {
                                eightfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 8.5 && puttRollDistanceCompensated <= 9.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer ninefeet = MediaPlayer.create(getApplicationContext(), R.raw.ninefeet);

                            playingSound = 1;

                            if (!ninefeet.isPlaying()) {
                                ninefeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 9.5 && puttRollDistanceCompensated <= 10.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer tenfeet = MediaPlayer.create(getApplicationContext(), R.raw.tenfeet);

                            playingSound = 1;

                            if (!tenfeet.isPlaying()) {
                                tenfeet.start();
                            }
                        }


                        if (puttRollDistanceCompensated > 10.5 && puttRollDistanceCompensated <= 11.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer elevenfeet = MediaPlayer.create(getApplicationContext(), R.raw.elevenfeet);

                            playingSound = 1;

                            if (!elevenfeet.isPlaying()) {
                                elevenfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 11.5 && puttRollDistanceCompensated <= 12.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twelvefeet = MediaPlayer.create(getApplicationContext(), R.raw.twelvefeet);

                            playingSound = 1;

                            if (!twelvefeet.isPlaying()) {
                                twelvefeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 12.5 && puttRollDistanceCompensated <= 13.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer thirteenfeet = MediaPlayer.create(getApplicationContext(), R.raw.thirteenfeet);

                            playingSound = 1;

                            if (!thirteenfeet.isPlaying()) {
                                thirteenfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 13.5 && puttRollDistanceCompensated <= 14.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer fourteenfeet = MediaPlayer.create(getApplicationContext(), R.raw.fourteenfeet);

                            playingSound = 1;

                            if (!fourteenfeet.isPlaying()) {
                                fourteenfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 14.5 && puttRollDistanceCompensated <= 15.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer fifteenfeet = MediaPlayer.create(getApplicationContext(), R.raw.fifteenfeet);

                            playingSound = 1;

                            if (!fifteenfeet.isPlaying()) {
                                fifteenfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 15.5 && puttRollDistanceCompensated <= 16.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer sixteenfeet = MediaPlayer.create(getApplicationContext(), R.raw.sixteenfeet);

                            playingSound = 1;

                            if (!sixteenfeet.isPlaying()) {
                                sixteenfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 16.5 && puttRollDistanceCompensated <= 17.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer seventeenfeet = MediaPlayer.create(getApplicationContext(), R.raw.seventeenfeet);

                            playingSound = 1;

                            if (!seventeenfeet.isPlaying()) {
                                seventeenfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 17.5 && puttRollDistanceCompensated <= 18.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer eighteenfeet = MediaPlayer.create(getApplicationContext(), R.raw.eighteenfeet);

                            playingSound = 1;

                            if (!eighteenfeet.isPlaying()) {
                                eighteenfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 18.5 && puttRollDistanceCompensated <= 19.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer nineteenfeet = MediaPlayer.create(getApplicationContext(), R.raw.nineteenfeet);

                            playingSound = 1;

                            if (!nineteenfeet.isPlaying()) {
                                nineteenfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 19.5 && puttRollDistanceCompensated <= 20.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twentyfeet = MediaPlayer.create(getApplicationContext(), R.raw.twentyfeet);

                            playingSound = 1;

                            if (!twentyfeet.isPlaying()) {
                                twentyfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 20.5 && puttRollDistanceCompensated <= 21.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twentyonefeet = MediaPlayer.create(getApplicationContext(), R.raw.twentyonefeet);

                            playingSound = 1;

                            if (!twentyonefeet.isPlaying()) {
                                twentyonefeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 21.5 && puttRollDistanceCompensated <= 22.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twentytwofeet = MediaPlayer.create(getApplicationContext(), R.raw.twentytwofeet);

                            playingSound = 1;

                            if (!twentytwofeet.isPlaying()) {
                                twentytwofeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 22.5 && puttRollDistanceCompensated <= 23.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twentythreefeet = MediaPlayer.create(getApplicationContext(), R.raw.twentythreefeet);

                            playingSound = 1;

                            if (!twentythreefeet.isPlaying()) {
                                twentythreefeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 23.5 && puttRollDistanceCompensated <= 24.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twentyfourfeet = MediaPlayer.create(getApplicationContext(), R.raw.twentyfourfeet);

                            playingSound = 1;

                            if (!twentyfourfeet.isPlaying()) {
                                twentyfourfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 24.5 && puttRollDistanceCompensated <= 25.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twentyfivefeet = MediaPlayer.create(getApplicationContext(), R.raw.twentyfivefeet);

                            playingSound = 1;

                            if (!twentyfivefeet.isPlaying()) {
                                twentyfivefeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 25.5 && puttRollDistanceCompensated <= 26.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twentysixfeet = MediaPlayer.create(getApplicationContext(), R.raw.twentysixfeet);

                            playingSound = 1;

                            if (!twentysixfeet.isPlaying()) {
                                twentysixfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 26.5 && puttRollDistanceCompensated <= 27.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twentysevenfeet = MediaPlayer.create(getApplicationContext(), R.raw.twentysevenfeet);

                            playingSound = 1;

                            if (!twentysevenfeet.isPlaying()) {
                                twentysevenfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 27.5 && puttRollDistanceCompensated <= 28.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twentyeightfeet = MediaPlayer.create(getApplicationContext(), R.raw.twentyeightfeet);

                            playingSound = 1;

                            if (!twentyeightfeet.isPlaying()) {
                                twentyeightfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 28.5 && puttRollDistanceCompensated <= 29.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer twentyninefeet = MediaPlayer.create(getApplicationContext(), R.raw.twentyninefeet);

                            playingSound = 1;

                            if (!twentyninefeet.isPlaying()) {
                                twentyninefeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 29.5 && puttRollDistanceCompensated <= 30.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer thirtyfeet = MediaPlayer.create(getApplicationContext(), R.raw.thirtyfeet);

                            playingSound = 1;

                            if (!thirtyfeet.isPlaying()) {
                                thirtyfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 30.5 && puttRollDistanceCompensated <= 31.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer thirtyonefeet = MediaPlayer.create(getApplicationContext(), R.raw.thirtyonefeet);

                            playingSound = 1;

                            if (!thirtyonefeet.isPlaying()) {
                                thirtyonefeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 31.5 && puttRollDistanceCompensated <= 32.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer thirtytwofeet = MediaPlayer.create(getApplicationContext(), R.raw.thirtytwofeet);

                            playingSound = 1;

                            if (!thirtytwofeet.isPlaying()) {
                                thirtytwofeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 32.5 && puttRollDistanceCompensated <= 33.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer thirtythreefeet = MediaPlayer.create(getApplicationContext(), R.raw.thirtythreefeet);

                            playingSound = 1;

                            if (!thirtythreefeet.isPlaying()) {
                                thirtythreefeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 33.5 && puttRollDistanceCompensated <= 34.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer thirtyfourfeet = MediaPlayer.create(getApplicationContext(), R.raw.thirtyfourfeet);

                            playingSound = 1;

                            if (!thirtyfourfeet.isPlaying()) {
                                thirtyfourfeet.start();
                            }
                        }

                        if (puttRollDistanceCompensated > 34.5 && puttRollDistanceCompensated <= 35.5 && playingSound == 0 && ballStoppedFlag == 1) {

                            final MediaPlayer thirtyfivefeet = MediaPlayer.create(getApplicationContext(), R.raw.thirtyfivefeet);

                            playingSound = 1;

                            if (!thirtyfivefeet.isPlaying()) {
                                thirtyfivefeet.start();
                            }
                        }


                    }




                    numberRollsOld = numberRolls;





                    puttVelocity = (double)(BleGolfballService.getTach(BleGolfballService.Motor.VELOCITY)/1);
                    /*double puttVelocityRound = Math.round(puttVelocity*100)/100D;
                    String textputtVelocity = Double.toString(puttVelocityRound);
                    mVelocityText.setText(puttVelocity);*/

                    mVelocityText.setText(String.format("%d", BleGolfballService.getTach(BleGolfballService.Motor.VELOCITY)));

                    int puttMade = (BleGolfballService.getTach(BleGolfballService.Motor.VELOCITY));


                    if (puttMade != puttMadeOld){


                        final MediaPlayer cheersound = MediaPlayer.create(getApplicationContext(), R.raw.cheerloud);
                        cheersound.start();



                        puttMadeFlag = 1;
                        /*puttIsMade = true;*/

                        PuttData puttData = new PuttData(1,"Michael",puttNumber,longestMadeDistanceRound,puttRollDistanceCompensated,2,9.5,
                                true,3.0,12.0,1.0,
                                "stopped");

                        puttViewModel.insert(puttData);






                    }

                    puttMadeOld = (BleGolfballService.getTach(BleGolfballService.Motor.VELOCITY));

                    if (ballStoppedFlag == 1){
                        velocityFallOff = (longestMadeDistanceMax/puttRollDistanceCompensated);
                        double velocityFallOffRound = Math.round(velocityFallOff * 100) / 100D;
                        if(puttRollDistanceCompensated == 0){
                            velocityFallOffRound = 0;
                        }

                        String velocityFallOffText = Double.toString(velocityFallOffRound);


                        mVelocityFallOff.setText((velocityFallOffText));

                    }




                    puttVelocityOld = puttVelocity;
                    double longestMadeDistanceRoundOld = longestMadeDistanceRound;

                    break;
            }

        }
    };


    /**
     * This sets up the filter for broadcasts that we want to be notified of.
     * This needs to match the broadcast receiver cases.
     *
     * @return intentFilter
     */
    private static IntentFilter makeRobotUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleGolfballService.ACTION_CONNECTED);
        intentFilter.addAction(BleGolfballService.ACTION_DISCONNECTED);
        intentFilter.addAction(BleGolfballService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    public abstract class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {


        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {

            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
            // An item was selected. You can retrieve the selected item using

            //parent.getItemAtPosition(position);



        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }



    }



}