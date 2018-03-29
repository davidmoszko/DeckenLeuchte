package de.openlicht.deckenleuchte;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;
import java.util.UUID;

public class PressLamp extends AppCompatActivity {

    private static final UUID MY_UUID_INSECURE =
            java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TextView tvAll,twBlue,tvRed,tvCyan,tvWarmWhite,tvColdWhite,tvOrange;

    SeekBar seekBarAllLEDs;

    byte[] bytes = new byte[16];

    BluetoothConnectionService mBluetoothConnection;

    BluetoothDevice mBTDevice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circadian_lamp);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startConnection();



        mBluetoothConnection = new BluetoothConnectionService(PressLamp.this);

        tvAll = findViewById(R.id.textView);
        twBlue = findViewById(R.id.textView2);
        tvRed = findViewById(R.id.textView3);
        tvCyan = findViewById(R.id.textView4);
        tvColdWhite = findViewById(R.id.textView5);
        tvWarmWhite = findViewById(R.id.textView6);
        tvOrange = findViewById(R.id.textView7);

        tvAll.setTextColor(Color.BLACK);
        twBlue.setTextColor(Color.BLACK);
        tvRed.setTextColor(Color.BLACK);
        tvCyan.setTextColor(Color.BLACK);
        tvColdWhite.setTextColor(Color.BLACK);
        tvWarmWhite.setTextColor(Color.BLACK);
        tvOrange.setTextColor(Color.BLACK);


        seekBarAllLEDs = findViewById(R.id.seekBarAllLEDs);

        seekBarAllLEDs.setDrawingCacheBackgroundColor(Color.GRAY);


        seekBarAllLEDs.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                bytes[0] = (byte)(0x47);

                bytes[1+progress] = (byte)(0xFF);

                mBluetoothConnection.write(bytes);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }



    private void startConnection() {
        startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Toast.makeText(this, "Initializing RFCOM Bluetooth Connection for Data Transmission",
                Toast.LENGTH_LONG).show();

        mBluetoothConnection.startClient(device,uuid);
    }


    //BACK BUTTON PART
    public void onClick(View v)
    {
        Intent myIntent = new Intent(this,MainActivity.class);
        startActivity(myIntent);

    }



}
