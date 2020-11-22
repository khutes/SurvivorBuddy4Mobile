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

/**
 * The main screen of the application. All other activities are started here. RtspService is
 * directly controlled here
 */
public class MainActivity extends AppCompatActivity{

    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Button server_button = null;
    //private TextView url_textview = null;
    private RtspService mServer = null;
    private Boolean boundedRtspService = false;

    private String TAG = "[SB4] MainActivity";

    /**
     * Called onCreate
     * Sets the button text for Start/Stop Video Phone to PC button
     * Also check and asks for necessary permissions
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate");

        //Get view contents
        server_button = (Button) findViewById(R.id.rtsp_service_button);
        //url_textview = (TextView) findViewById(R.id.rtsp_url);

        server_button.setText(R.string.start_stream_button);
        if(isServiceRunning(RtspService.class)) {
            server_button.setText(R.string.stop_stream_button);
        }

        if(!hasPermissions()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

    }

    /**
     * Called onStart
     * If rtspService is running it binds to it
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        if(isServiceRunning(RtspService.class)) {
            safeRtspServiceBind();
        }

    }

    /**
     * Called onStop
     * Unbinds from rtspService
     */
    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        safeRtspServiceUnbind();
    }

    /**
     * Default onDestroy
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }


    /**
     * Starts and stops RtspService, changes button text based on option
     * @param view View
     */
    public void startStopRtsp(View view) {
        //check if RtspServer is runnning
        Log.i(TAG, "startStopRtsp");
        if(isServiceRunning(RtspService.class)) {
            safeRtspServiceUnbind();
            stopServiceThread();
            //stopService(new Intent(getApplicationContext(), RtspService.class));
            //url_textview.setText(R.string.default_textview_url);
            server_button.setText(R.string.start_stream_button);

        } else {
            safeRtspServiceBind();
            Log.i(TAG, "startStopRtsp: good bind");
            startServiceThread();
            server_button.setText(R.string.stop_stream_button);
        }
    }

    /**
     * Checks if a service is running
     * @param serviceClass Class, the service to be checked
     * @return boolean, true if service is running, false otherwise
     */
    private boolean isServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if application has necessary permissions
     * @return boolean, true if all permissions are granted, false otherwise
     */
    private boolean hasPermissions() {
        for(String permission : PERMISSIONS) {
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private ServiceConnection mRtspServiceConnection = new ServiceConnection() {
        /**
         * Called after bind to RtspService, used to pass parameters to service
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            Toast.makeText(MainActivity.this, "Binded to service", Toast.LENGTH_SHORT).show();
            RtspService.LocalBinder mLocalBinder = (RtspService.LocalBinder)service;
            mServer = mLocalBinder.getRtspServiceInstance();
            boundedRtspService = true;
            //displays the stream endpoint url
            //url_textview.setText(mServer.get_endpoint());
            Log.i(TAG, "END onServiceConnected");
        }

        //Should never be called

        /**
         * Called when RtspService unexpectedly fails
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            Toast.makeText(MainActivity.this, "Unexpected Service Failure", Toast.LENGTH_SHORT);
        }
    };

    /**
     * Binds to RtspService not yet bound
     */
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

    /**
     * Unbinds from RtspService is service is running and bound
     */
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

    /**
     * Starts the rtspService unbound
     */
    private void startServiceThread() {
        startService(new Intent(getApplicationContext(), RtspService.class));
    }

    /**
     * Stops RtspService unbound
     */
    private void stopServiceThread() {
        stopService(new Intent(getApplicationContext(), RtspService.class));
    }


    /**
     * Starts MessageActivity
     * @param view View
     */
    public void startMessageActivity(View view) {
        Log.i(TAG, "startMessageActivity");
        startActivity(new Intent(this, MessageActivity.class));
    }

    /**
     * Starts BuddyAudioActivity
     * @param view View
     */
    public void startAudioActivity(View view) {
        Log.i(TAG, "startBuddyAudioActivity");
        startActivity(new Intent(this, BuddyAudioActivity.class));
    }

    /**
     * Starts SettingsActivity
     * @param view View
     */
    public void startSettingsActivity(View view) {
        Log.i(TAG, "startSettingsActivity");
        startActivity(new Intent(this, SettingsActivity.class));
    }






}