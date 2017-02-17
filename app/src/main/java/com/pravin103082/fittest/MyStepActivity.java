package com.pravin103082.fittest;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MyStepActivity extends AppCompatActivity implements View.OnClickListener,  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Button btnStart,btnEnd;
    private GoogleApiClient mGoogleApiClient;
    int i=0;
    Timer timer;
    TextView steps,cal,act,loctn,txtFields;
    int stepCount=0;
    Set<String> fields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_step);
        fields=new HashSet<>();

        txtFields=(TextView)findViewById(R.id.txtFields);
        btnStart=(Button)findViewById(R.id.btnStart);
        btnEnd=(Button)findViewById(R.id.btnEnd);

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timer!=null){
                    timer.cancel();
                    Toast.makeText(MyStepActivity.this,"Timer End",Toast.LENGTH_LONG).show();
                }
            }
        });
        steps=(TextView)findViewById(R.id.txtStepCount);
        loctn=(TextView)findViewById(R.id.loctn);
        cal=(TextView)findViewById(R.id.cal);
        act=(TextView)findViewById(R.id.act);
        btnStart.setOnClickListener(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .enableAutoManage(this, 0, this)
                .build();

    }

    @Override
    public void onClick(View v) {
        Toast.makeText(MyStepActivity.this,"Timer Started",Toast.LENGTH_LONG).show();
       //new ViewCustomStepCountTask().execute();
        timer=new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.e("RUN",""+i);
                i++;
                Calendar cal = Calendar.getInstance();
                Date now = new Date();
                cal.setTime(now);
                long endTime = cal.getTimeInMillis();
                cal.add(Calendar.SECOND, -5);
                long startTime = cal.getTimeInMillis();

                DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setType(DataSource.TYPE_DERIVED)
                        .setStreamName("estimated_steps")
                        .setAppPackageName("com.google.android.gms")
                        .build();
                DataReadRequest readRequest = new DataReadRequest.Builder()
                        .aggregate(ESTIMATED_STEP_DELTAS,    DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                        .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                        .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                        .aggregate(DataType.TYPE_HEART_RATE_BPM,DataType.AGGREGATE_HEART_RATE_SUMMARY)
                        .aggregate(DataType.TYPE_LOCATION_SAMPLE,DataType.AGGREGATE_LOCATION_BOUNDING_BOX)
                        .aggregate(DataType.TYPE_SPEED,DataType.AGGREGATE_SPEED_SUMMARY)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .bucketByTime(5, TimeUnit.SECONDS)
                        .build();

                DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleApiClient, readRequest).await(1, TimeUnit.MINUTES);
//Used for aggregated data
                if (dataReadResult.getBuckets().size() > 0) {
                    Log.e("History", "Number of buckets: " + dataReadResult.getBuckets().size());
                    for (Bucket bucket : dataReadResult.getBuckets()) {
                        List<DataSet> dataSets = bucket.getDataSets();
                        for (DataSet dataSet : dataSets) {
                            showDataSet(dataSet);
                            Log.d("dataset",dataSet.toString());
                        }
                    }
                }
                //Used for non-aggregated data
                else if (dataReadResult.getDataSets().size() > 0) {
                    Log.e("History", "Number of returned DataSets: " + dataReadResult.getDataSets().size());
                    for (DataSet dataSet : dataReadResult.getDataSets()) {
                        showDataSet(dataSet);
                    }
                }

            }
        },0,10000);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    class ViewCustomStepCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayCustomStepData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
           // showEvents();
        }
    }


    private void displayCustomStepData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.SECOND, -5);
        long startTime = cal.getTimeInMillis();

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS,    DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .aggregate(DataType.TYPE_HEART_RATE_BPM,DataType.AGGREGATE_HEART_RATE_SUMMARY)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(5, TimeUnit.SECONDS)
                .build();

        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleApiClient, readRequest).await(1, TimeUnit.MINUTES);
//Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                    Log.d("dataset",dataSet.toString());
                }
            }
        }
        //Used for non-aggregated data
        else if (dataReadResult.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                showDataSet(dataSet);
            }
        }
    }



    private void showDataSet(DataSet dataSet) {
        for (final DataPoint dp : dataSet.getDataPoints()) {
            for(final Field field : dp.getDataType().getFields()) {
                fields.add(field.getName());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtFields.setText(fields.toString());
                    }
                });

                Log.e("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field)+"\n");
                String fieldName=field.getName();


                if("steps".equals(fieldName)){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // Toast.makeText(yourContext, "Data: ", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(MyStepActivity.this,dp.getValue(field)+"  Steps",Toast.LENGTH_SHORT).show();
                            stepCount=stepCount+Integer.parseInt(dp.getValue(field)+"");
                            steps.setText(stepCount+"");
                        }
                    });
                }else if("duration".equals(fieldName)){
                    runOnUiThread(new Runnable() {
                        public void run() {

                        }
                    });
                }else if("num_segments".equals(fieldName)){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // Toast.makeText(yourContext, "Data: ", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(MyStepActivity.this,dp.getValue(field)+"  Steps",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else if("activity".equals(fieldName)){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // Toast.makeText(yourContext, "Data: ", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(MyStepActivity.this,dp.getValue(field)+"  Steps",Toast.LENGTH_SHORT).show();
                            int activity=dp.getValue(field).asInt();
                            switch (activity){
                                case 0:act.setText("IN_VEHICLE");
                                    break;
                                case 1:act.setText("ON_BICYCLE");
                                    break;
                                case 2:act.setText("ON_FOOT");
                                    break;
                                case 3:act.setText("STILL");
                                    break;
                                case 4:act.setText("UNKNOWN");
                                    break;
                                case 5:act.setText("TILTING");
                                    break;
                                case 7:act.setText("WALKING");
                                    break;
                                case 8:act.setText("RUNNING");
                                    break;


                            }
                        }
                    });
                }else if("calories".equals(fieldName)){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // Toast.makeText(yourContext, "Data: ", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(MyStepActivity.this,dp.getValue(field)+"  Steps",Toast.LENGTH_SHORT).show();
                            cal.setText(dp.getValue(field)+"");
                        }
                    });
                }else if("high_latitude".equals(fieldName)){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // Toast.makeText(yourContext, "Data: ", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(MyStepActivity.this,dp.getValue(field)+"  Steps",Toast.LENGTH_SHORT).show();
                        lat=dp.getValue(field)+"";
                        }
                    });
                }else if("high_longitude".equals(fieldName)){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // Toast.makeText(yourContext, "Data: ", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(MyStepActivity.this,dp.getValue(field)+"  Steps",Toast.LENGTH_SHORT).show();
                            cal.setText(dp.getValue(field)+"");
                            lon=dp.getValue(field)+"";
                        }
                    });
                }

            }
        }
    }
String lat,lon;

}
