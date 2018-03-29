package de.openlicht.deckenleuchte;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;


public class PressBluetooth extends AppCompatActivity implements AdapterView.OnItemClickListener,TimePickerDialog.OnTimeSetListener {

    private static final UUID MY_UUID_INSECURE =
            java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private TextView mTextView;
    private static final String TAG = "PressBluetooth";
    BluetoothAdapter mBluetoothAdapter;
    int StatusBT;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;
    BluetoothConnectionService mBluetoothConnection;
    Button btnStartConnection;
    Button btnSendON;
    Button btnSendOFF;
    Button btnAlarm;
    Button buttonCancelAlarm;
    SeekBar seekBarTest;
    SeekBar seekBarPWM;
    ImageButton BluetoothON;
    ImageButton BluetoothOFF;
    //EditText etSend;
    BluetoothDevice mBTDevice;
    byte[] bytes = new byte[16];
    //byte[] Steigerung = new byte[1];
    int BitIncrease = 0x20;




    // Create a BroadcastReceiver for ACTION_FOUND. ----> for Bluetooth
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }

            }
        }
    };
    // BroadcastReceiver for changes made to bleutooth states such as -> Discoverability ON/OFF

    // Code für Bluetooth Discoverability


    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //now in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Toast.makeText(context, "Discoverability Enabled", Toast.LENGTH_LONG).show();
                        break;
                    //now not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Toast.makeText(context, "Discoverability Enabled. Able to receive connections", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Toast.makeText(context, "Discoverability Disabled. Not able to receive connections", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };


    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive:" + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };


    //BROADCASTRECEIVER4 detects bond state changes (PAIRING STATUS CHANGED)
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 CASES!!!
                //CASE1: already bonded
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, "Device is bonded", Toast.LENGTH_SHORT).show();
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //CASE2: Creating a Bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(context, "Device is creating a bond", Toast.LENGTH_SHORT).show();
                }
                //CASE3: Breaking a Bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Toast.makeText(context, "Discoverability Enabled. Able to receive connections", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    //Destruktor für Bluetooth, falls App geschlossen
/*
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        Toast.makeText(this, "Destruktor für BroadcastReceiver ausgeführt", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }
*/


    public void BluetoothStatus() {
        if (StatusBT == 1) {                                             //IF STATUS IS TRUE
            Toast.makeText(this, "Bluetooth ON",
                    Toast.LENGTH_LONG).show();
        }
        if (StatusBT == 2) {                                            //IF STATUS IS FALSE
            Toast.makeText(this, "Bluetooth OFF",
                    Toast.LENGTH_LONG).show();
        }
    }


//______________________________________________________________ ONCREATE PART


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.press_bluetooth);

        btnSendON = findViewById(R.id.btnSendON);
        btnSendOFF = findViewById(R.id.btnSendOFF);
        btnAlarm = findViewById(R.id.btnAlarm);
        btnStartConnection = findViewById(R.id.btnStartConnection);
        buttonCancelAlarm = findViewById(R.id.button_cancel);
        //Button btnEnableDisable_Discoverable = findViewById(R.id.btnDiscoverable);
        mTextView = findViewById(R.id.timeView);
        lvNewDevices = findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();
        seekBarTest = findViewById(R.id.seekBarAllLEDs);
        seekBarTest.setDrawingCacheBackgroundColor(Color.GRAY);
        seekBarTest.setMax(0);
        seekBarTest.setMax(255); //14 bei LED durschalten
        seekBarPWM = findViewById(R.id.seekBarPWM);
        seekBarPWM.setDrawingCacheBackgroundColor(Color.GRAY);
        seekBarPWM.setMax(0);
        seekBarPWM.setMax(255);
        //etSend = findViewById(R.id.etSend);
        BluetoothON = findViewById(R.id.imageBTON);
        BluetoothOFF = findViewById(R.id.imageBTOFF);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lvNewDevices.setOnItemClickListener(PressBluetooth.this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        InitializeButtons();
        InitializeSeekBars();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(mBroadcastReceiver4, filter);

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

    }


    //________________________________ONCREATE END

    //<editor-fold desc="Beispiel" default="collapsed">
    //</editor-fold>


    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        TextView textView = findViewById(R.id.timeView);
        textView.setText("Alarm set to: " + i + ":"+ i1);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, i);
        c.set(Calendar.MINUTE, i1);
        c.set(Calendar.SECOND, 0);

        updateTimeText(c);
        try {
            startAlarm(c);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateTimeText(Calendar c) {
        String timeText = "Alarm set for: ";
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());

        mTextView.setText(timeText);
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void startAlarm(Calendar c) throws InterruptedException {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);                //Will add the alarm for the next day
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

        if (mBTDevice==null){
            Toast.makeText(getBaseContext(), "Please connect a Bluetooth Device",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            AlarmBluetoothWakeUp();
        }
    }


    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        alarmManager.cancel(pendingIntent);
        mTextView.setText("Alarm canceled");

    }


    private void startConnection() {
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    //Starting chat Service Method

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        if(mBTDevice==null){
            Toast.makeText(this, "Please connect a Bluetooth Device", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "Initializing RFCOMM Bluetooth Connection",
                    Toast.LENGTH_LONG).show();
        mBluetoothConnection.startClient(device, uuid);
    }

    //CODE FÜR BLUETOOTH ENABLE BUTTON

    public void EnableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Device without Bluetooth capabilities");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
//            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivity(enableBTIntent);

            StatusBT = 1;
            BluetoothStatus();


            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

        if (mBluetoothAdapter.isEnabled()) {

        }
    }


    //CODE FÜR BLUETOOTH DISABLE BUTTON

    public void DisableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Device without Bluetooth capabilities");
        }

        if (!mBluetoothAdapter.isEnabled()) {

        }

        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();

            StatusBT = 2;
            BluetoothStatus();


            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }


    //Bluetooth part end


    //BLUETOOTH DISCOVERABILITY METHODE   --------> NOT NEEDED!!!!!!!!
/*

    public void btnEnableDisable_Discoverable (View view) {
        Toast.makeText(this, "Making Device Discoverable for 300 seconds", Toast.LENGTH_SHORT).show();

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);
    }

*/

    // DER TEIL FÜR BLUETOOTH KONFIGURATIONEN IST HIER BEENDET


    public void btnDiscover(View view) {
        Toast.makeText(this, "Looking for unpaired Devices", Toast.LENGTH_SHORT).show();

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Toast.makeText(this, "Canceling Discovery", Toast.LENGTH_SHORT).show();
            checkBTPermission();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDeviceIntent);

        }
        if (!mBluetoothAdapter.isDiscovering()) {
            //check BT permissions in manifest
            checkBTPermission();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDeviceIntent);
        }
    }


    //Method is required for all devices running API23+ -> Check permissions for Bluetooth. Putting proper permissions in the manifest is not enough
    //will only execute on versions > LOLLIPOP
    private void checkBTPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
            permissionCheck += ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
            if (permissionCheck != 0) {

                ActivityCompat.requestPermissions(PressBluetooth.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //ANY NUMBER
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP");
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because it is very memory intense
        mBluetoothAdapter.cancelDiscovery();
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();
        Toast.makeText(this, "Clicked on this Device" + "\nDevice Name:" + deviceName +
                        "\nDevice Address:" + deviceAddress,
                Toast.LENGTH_SHORT).show();
        //create the bond!!
        //NOTE: API18+ required because of Bluetooth changes. Is JellyBean or Android 4.3 API
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Toast.makeText(this, "Pairing with: " + deviceName, Toast.LENGTH_SHORT).show();
            mBTDevices.get(i).createBond();
            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(PressBluetooth.this);
        }
    }

    public void InitializeButtons() {

        buttonCancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlarm();
            }
        });

        btnAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

        //BLUETOOTHBUTTON->ON
        BluetoothON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enabling Bluetooth");
                EnableBT();
            }
        });

        //BLUETOOTHBUTTON->OFF
        BluetoothOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: disabling Bluetooth");
                DisableBT();
            }
        });

        btnSendOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mBTDevice==null){
                    Toast.makeText(getBaseContext(), "Please connect a Bluetooth Device",
                            Toast.LENGTH_SHORT).show();
                }

                else {
                    bytes[0] = (byte) (0x47);

                    for (int k = 1; k < 16; k++) {
                        bytes[k] = (byte) (0x00);

                    }
                    mBluetoothConnection.write(bytes);
                }

            }
        });

        btnSendON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mBTDevice==null){
                    Toast.makeText(getBaseContext(), "Please connect a Bluetooth Device",
                            Toast.LENGTH_SHORT).show();
                }

                else {
                    bytes[0] = (byte) (0x47);

                    for (int k = 1; k < 16; k++) {
                        bytes[k] = (byte) (0xFF);

                    }
                    mBluetoothConnection.write(bytes);
                }

            }
        });
    }

    public void InitializeSeekBars(){
        seekBarTest.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (mBTDevice == null) {
                    Toast.makeText(getBaseContext(), "Please connect a Bluetooth Device",
                            Toast.LENGTH_SHORT).show();
                } else {
                    bytes[0] = (byte) (0x47);
                    for (int k = 1; k < 16; k++) {
                        bytes[k] = (byte) (progress);

                    /*
                    bytes[k] = bytes[progress];
                    bytes[progress] = (byte) (0xFF);
                    bytes[15-progress] = (byte) (0x00);
                    */

                    }
                    mBluetoothConnection.write(bytes);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        seekBarPWM.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (mBTDevice == null) {
                    Toast.makeText(getBaseContext(), "Please connect a Bluetooth Device",
                            Toast.LENGTH_SHORT).show();
                } else {
                    bytes[0] = (byte) (0x48);

                    for (int k = 1; k < 16; k++) {
                        bytes[k] = (byte) (progress);

                    }
                    mBluetoothConnection.write(bytes);

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //statusrequest = ok -> neu senden
                    //Falls das erste Byte nicht erkannt wurde -> pa -> "please again"
                    //df -> default function -> und das passiert bei pa!! das heißt die Leuchte geht in den Ursprungszustand zurück

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void AlarmBluetoothWakeUp() throws InterruptedException {//----> This method sleeps every 1 second and highers the brightness of every LED
        bytes[0] = (byte) 0x48;
        //Initialize Bluetooth Protocol 0x48
        for(int k = 1; k < 16; k++) {
            bytes[k] = 0;
        }

        Handler handler1 = new Handler();
        for(int q = 0; q < 7; q++) {
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for(int k = 1; k < 16; k++) {
                        bytes[k] = (byte)((bytes[k] & 0xFF) + BitIncrease);
                    }
                    mBluetoothConnection.write(bytes);
                    Log.d("LED", "" + (bytes[1]));
                    Log.d("LED", "-------------------------------------------------------");
                }
            },2000*q);

        }

        /*
        for (int k = 1; k < 16; k++) {      //LEDs 0-15 at 00 -> NOT ON
            bytes[k] = (byte) 0x00;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);


        for (int k = 1; k < 16; k++) {
            bytes[k] = (byte) 0x19;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);

        for (int k = 1; k < 16; k++) {
            bytes[k] = (byte) 0x32;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);

        for (int k = 1; k < 16; k++) {
            bytes[k] = (byte) 0x4B;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);

        for (int k = 1; k < 16; k++) {
            bytes[k] = (byte) 0x64;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);

        for (int k = 1; k < 16; k++) {
            bytes[k] = (byte) 0x7D;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);

        for (int k = 1; k < 16; k++) {
            bytes[k] = (byte) 0x96;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);

        for (int k = 1; k < 16; k++) {
            bytes[k] = (byte) 0xAF;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);

        for (int k = 1; k < 16; k++) {
            bytes[k] = (byte) 0xC8;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);

        for (int k = 1; k < 16; k++) {
            bytes[k] = (byte) 0xE1;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);

        for (int k = 1; k < 16; k++) {
            bytes[k] = (byte) 0xFF;
        }
        mBluetoothConnection.write(bytes);
        Thread.sleep(1000);
        */
    }
}