package com.pravin103082.fittest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main2Activity extends AppCompatActivity {

    Button btnSensorApi,btnHistoryApi,btnWeekandTodaysHistoryApi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        btnSensorApi=(Button)findViewById(R.id.btnSensorApi);
        btnHistoryApi=(Button)findViewById(R.id.btnHistoryApi);
        btnWeekandTodaysHistoryApi=(Button)findViewById(R.id.btnWeekandTodaysHistoryApi);

        btnWeekandTodaysHistoryApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this,HistoryActivity.class));
            }
        });
        btnSensorApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this,MainActivity.class));
            }
        });

        btnHistoryApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main2Activity.this,MyStepActivity.class));
            }
        });

    }
}
