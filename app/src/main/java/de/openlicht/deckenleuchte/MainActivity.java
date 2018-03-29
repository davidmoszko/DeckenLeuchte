package de.openlicht.deckenleuchte;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView twIntroduction;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //ALARMBUTTON

        Button PressAlarm = findViewById(R.id.PressAlarm);

        PressAlarm.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Intent myIntent = new Intent(v.getContext(), PressAlarm.class);
                        startActivity(myIntent);
                    }
                }

        );


        //BLUETOOTHIMAGEBUTTON

        ImageButton PressBluetooth = findViewById(R.id.PressBluetooth);

        PressBluetooth.setOnClickListener(new View.OnClickListener(){
            // When the button is pressed/clicked, it will run the code below
            @Override
            public void onClick(View v){
                // Intent is what you use to start another activity
                //startService(new Intent(getBaseContext(), BluetoothConnectionService.class));
                Intent myIntent = new Intent(v.getContext(), PressBluetooth.class);
                //startService(myIntent);
                startActivity(myIntent);
            }
        });




        //LAMPENHBUTTON

        Button PressLamp = findViewById(R.id.PressLamp);

        PressLamp.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Intent myIntent = new Intent(v.getContext(), PressLamp.class);
                        startActivity(myIntent);
                    }
                }

        );



        //Home Steuerungsbuttons

        Button HomeControl = findViewById(R.id.HomeControl);

        HomeControl.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Intent myIntent = new Intent(v.getContext(), HomeControl.class);
                        startActivity(myIntent);
                    }
                }
        );






        //TEXTFORM ÄNDERN

        twIntroduction = findViewById(R.id.textView2);

        twIntroduction.setTextColor(Color.BLACK);



    }


}