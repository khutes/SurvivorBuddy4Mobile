package com.example.survivorbuddy4mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Permission;

public class MainActivity extends AppCompatActivity{

    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Button server_button = null;
    private TextView url_textview = null;
    private RtspService mServer = null;
    private Boolean boundedRtspService = false;

    private String TAG = "[SB4] MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate");

        //Get view contents
        server_button = (Button) findViewById(R.id.rtsp_service_button);
        url_textview = (TextView) findViewById(R.id.rtsp_url);

        server_button.setText(R.string.start_stream_button);
        if(isServiceRunning(RtspService.class)) {
            server_button.setText(R.string.stop_stream_button);
        }

        if(!hasPermissions()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        if(isServiceRunning(RtspService.class)) {
            safeRtspServiceBind();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        safeRtspServiceUnbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }


    public void startStopRtsp(View view) {
        //check if RtspServer is runnning
        Log.i(TAG, "startStopRtsp");
        if(isServiceRunning(RtspService.class)) {
            safeRtspServiceUnbind();
            stopServiceThread();
            //stopService(new Intent(getApplicationContext(), RtspService.class));
            url_textview.setText(R.string.default_textview_url);
            server_button.setText(R.string.start_stream_button);

        } else {
            safeRtspServiceBind();
            Log.i(TAG, "startStopRtsp: good bind");
            startServiceThread();
            server_button.setText(R.string.stop_stream_button);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermissions() {
        for(String permission : PERMISSIONS) {
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private ServiceConnection mRtspServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            Toast.makeText(MainActivity.this, "Binded to service", Toast.LENGTH_SHORT).show();
            RtspService.LocalBinder mLocalBinder = (RtspService.LocalBinder)service;
            mServer = mLocalBinder.getRtspServiceInstance();
            boundedRtspService = true;
            //displays the stream endpoint url
            url_textview.setText(mServer.get_endpoint());
            Log.i(TAG, "END onServiceConnected");
        }

        //Should never be called
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            Toast.makeText(MainActivity.this, "Unexpected Service Failure", Toast.LENGTH_SHORT);
        }
    };

    private void safeRtspServiceBind() {
        Log.i(TAG, "safeRtspServiceBind");
        /*
        if(isServiceRunning(RtspService.class)) {
            Log.i(TAG, "safeRtspServiceBind: service running");

        */
        if(!boundedRtspService) {
            Log.i(TAG, "safeRtspServiceBind: binding");
            Intent intent = new Intent(MainActivity.this, RtspService.class);
            bindService(intent, mRtspServiceConnection, BIND_AUTO_CREATE);

        }
        else { Log.i(TAG, "safeRtspServiceBind: service NOT running"); }
    }

    private void safeRtspServiceUnbind() {
        Log.i(TAG, "safeRtspServiceUnbind");
        if(isServiceRunning(RtspService.class)) {
            if(boundedRtspService) {
                Log.i(TAG, "safeRtspServiceBind: unbinding");
                unbindService(mRtspServiceConnection);
                boundedRtspService = false;
            }
        }
    }

    private void startServiceThread() {

        startService(new Intent(getApplicationContext(), RtspService.class));
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                startService(new Intent(getApplicationContext(), RtspService.class));
            }
        }).start();
        */
    }

    private void stopServiceThread() {

        stopService(new Intent(getApplicationContext(), RtspService.class));
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopService(new Intent(getApplicationContext(), RtspService.class));
            }
        }).start();
        */
    }


    //--------------------------------------------------------------------
    //Message activity functions on main

    public void startMessageActivity(View view) {
        Log.i(TAG, "startMessageActivity");
        startActivity(new Intent(this, MessageActivity.class));
    }






}