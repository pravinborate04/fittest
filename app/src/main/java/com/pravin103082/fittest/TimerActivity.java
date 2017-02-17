package com.pravin103082.fittest;

import android.app.Dialog;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Timer;
import java.util.TimerTask;

public class TimerActivity extends AppCompatActivity {

    int i=0;
    Timer timer;
    Button btnTimerStart,btnTimerEnd,btnDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        btnTimerStart=(Button)findViewById(R.id.btnTimerStart) ;
        btnTimerEnd=(Button)findViewById(R.id.btnTimerEnd);
        btnDialog=(Button)findViewById(R.id.btnDialog);

        btnDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(TimerActivity.this);
                dialog.setContentView(R.layout.layout_nav_devices);
                RadioGroup radioGroup= (RadioGroup) dialog.findViewById(R.id.radioGrpDevice);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch(checkedId)
                        {
                            case R.id.btnManufacturer:
                                Log.e( "onCheckedChanged: ","btnManufacturer" );
                                dialog.dismiss();
                                break;
                            case R.id.btnModel:
                                Log.e( "onCheckedChanged: ","btnModel" );
                                dialog.dismiss();
                                break;
                            case R.id.btnType:
                                Log.e( "onCheckedChanged: ","btnType" );
                                dialog.dismiss();
                                break;
                            case R.id.btnPlatform:
                                Log.e( "onCheckedChanged: ","btnPlatform" );
                                dialog.dismiss();
                                break;
                            case R.id.btnOSVersion:
                                Log.e( "onCheckedChanged: ","btnOSVersion" );
                                dialog.dismiss();
                                break;
                            case R.id.btnAppVersions:
                                Log.e( "onCheckedChanged: ","btnAppVersions" );
                                dialog.dismiss();
                                break;
                            case R.id.btnResolutions:
                                Log.e( "onCheckedChanged: ","btnResolutions" );
                                dialog.dismiss();
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });
        btnTimerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer=new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        Log.e("RUN",""+i);
                        i++;
                    }
                },0,1000);
            }
        });
        btnTimerEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timer!=null){
                    timer.cancel();
                }
            }
        });


       /* new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                Log.e("RUN",""+i);
                i++;
            }
        }, 0, 10000);//put here time 10000 milliseconds=10 second*/
    }


}
