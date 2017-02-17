package com.pravin103082.fittest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.result.DataReadResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestActivity extends AppCompatActivity {

    public static final String TAG = "IWatch";
    private GoogleApiClient mClient = null;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private Handler mHandler;
    private OnDataPointListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        mHandler = new Handler(getMainLooper());
       // initializeLogging();
        if (!checkPermissions()) {
           // requestPermissions();
        } else {
            buildFitnessClient();
        }
    }

    private void buildFitnessClient() {

        // we will use one listener
        mListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Log.d(TAG, "DataPoint " + field.getName() + " = " + val);
                }
            }
        };
        if (mClient == null && checkPermissions()) {
            mClient = new GoogleApiClient.Builder(this)
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                    .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                    .addApi(Fitness.RECORDING_API)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.SENSORS_API)
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i(TAG, "Connected!!!");
                                    subscribeToFetching();
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    }
                                }
                            }
                    )
                    .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {

                        }
                    })
                    .build();
        }
    }

    private void subscribeToFetching() {
        new BackgroundFetching().execute();
    }

    private class BackgroundFetching extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            try {
                Calendar cal = Calendar.getInstance();
                Date now = new Date();
                cal.setTime(now);
                long endTime = cal.getTimeInMillis();
                cal.add(Calendar.HOUR, -cal.getInstance().get(Calendar.HOUR_OF_DAY));
                cal.add(Calendar.MINUTE, -cal.getInstance().get(Calendar.MINUTE));
                cal.add(Calendar.SECOND, -cal.getInstance().get(Calendar.SECOND));
                long startTime = cal.getTimeInMillis();

                Log.i(TAG, "Start Time: " + android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a", startTime));
                Log.i(TAG, "End Time: " + android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a", endTime));

                DataReadRequest readRequest = new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                        .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                        .enableServerQueries()
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();

                DataReadResult dataReadResult =
                        Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
                SendData(dataReadResult);
            } catch (Exception e) {
                Log.i(TAG, "Error msg: " + e.getMessage());
            }
            return null;
        }

        public void SendData(DataReadResult dataReadResult) {
            try {
                JSONArray array = new JSONArray();

                if (dataReadResult.getBuckets().size() > 0) {
                    for (Bucket bucket : dataReadResult.getBuckets()) {
                        List<DataSet> dataSets = bucket.getDataSets();
                        for (DataSet dataSet : dataSets) {
                            SendDataSet(dataSet, array);
                        }
                    }
                } else if (dataReadResult.getDataSets().size() > 0) {
                    Log.i(TAG, "Number of returned DataSets is: "
                            + dataReadResult.getDataSets().size());
                    for (DataSet dataSet : dataReadResult.getDataSets()) {
                        SendDataSet(dataSet, array);
                    }
                }
                Log.i(TAG, ""
                        + array.toString());
            } catch (Exception e) {
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //mHandler.postDelayed(mUpdateUI, 10000); // 10 second
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateUI); // 10 second
    }

    private static void SendDataSet(DataSet dataSet, JSONArray array) {
        try {

            for (DataPoint dp : dataSet.getDataPoints()) {
                for (Field field : dp.getDataType().getFields()) {
                    if (field.getName().equalsIgnoreCase(Field.FIELD_CALORIES.getName())) {
                        JSONObject object = new JSONObject();
                        object.put("calories", dp.getValue(field));
                        object.put("start_time", dp.getStartTime(TimeUnit.MILLISECONDS));
                        object.put("end_time", dp.getEndTime(TimeUnit.MILLISECONDS));
                        array.put(object);
                    }
                    if (field.getName().equalsIgnoreCase(Field.FIELD_STEPS.getName())) {
                        JSONObject object = new JSONObject();
                        object.put("steps", dp.getValue(field));
                        object.put("start_time", dp.getStartTime(TimeUnit.MILLISECONDS));
                        object.put("end_time", dp.getEndTime(TimeUnit.MILLISECONDS));
                        array.put(object);
                    }
                    if (field.getName().equalsIgnoreCase(Field.FIELD_BPM.getName())) {
                        JSONObject object = new JSONObject();
                        object.put("bpm", dp.getValue(field));
                        object.put("start_time", dp.getStartTime(TimeUnit.MILLISECONDS));
                        object.put("end_time", dp.getEndTime(TimeUnit.MILLISECONDS));
                        array.put(object);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private final Runnable mUpdateUI = new Runnable() {
        public void run() {
            subscribeToFetching();
        }
    };


   /* private void initializeLogging() {
        LogWrapper logWrapper = new LogWrapper();

        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);
        LogView logView = (LogView) findViewById(R.id.sample_logview);
        logView.setTextAppearance(this, R.style.Log);
        logView.setBackgroundColor(Color.WHITE);
        msgFilter.setNext(logView);
        Log.i(TAG, "Ready");
    }*/

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BODY_SENSORS);
        return permissionState == PackageManager.PERMISSION_GRANTED && permissionState1 == PackageManager.PERMISSION_GRANTED;
    }


}