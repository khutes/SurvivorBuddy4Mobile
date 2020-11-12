package com.example.survivorbuddy4mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class BuddyAudioActivity extends AppCompatActivity {

    private String TAG = "[SB4] BuddyAudioActivity";

    private Button audioButton = null;
    private BuddyAudioService mBuddyAudioService;
    private int portNum;
    private int defaultPort;
    private boolean boundBool = false;
    private volatile boolean startThreadsBool = false;

    private SharedPreferences mPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_audio);

        audioButton = (Button) findViewById(R.id.audio_button);

        audioButton.setText(R.string.start_audio_service_button);
        if(isServiceRunning(BuddyAudioService.class)) {
            audioButton.setText(R.string.stop_audio_service_button);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        defaultPort = Integer.parseInt(getString(R.string.default_audio_port));
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        portNum = Integer.parseInt(String.valueOf(mPreferences.getInt("audioPort", defaultPort)));

        if(isServiceRunning(BuddyAudioService.class)){
            bindBAS();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindBAS();
    }

    public void startStopBAService(View view) {
        Log.i(TAG, "startStopBAService");

        if(isServiceRunning(BuddyAudioService.class)) {
            //unbind and stop service
            unbindBAS();
            stopService(new Intent(getApplicationContext(), BuddyAudioService.class));
            audioButton.setText(R.string.start_audio_service_button);

        } else {
            //start and bind service

            startService(new Intent(getApplicationContext(), BuddyAudioService.class));
            bindBAS();
            audioButton.setText(R.string.stop_audio_service_button);
            startThreadsBool = true;
        }
    }

    private void bindBAS() {
        if(!boundBool) {
            bindService(new Intent(BuddyAudioActivity.this, BuddyAudioService.class),
                    mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    private void unbindBAS() {

        if(boundBool) {
            unbindService(mServiceConnection);
            boundBool = false;
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

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BuddyAudioService.LocalBinder mLocalBinder = (BuddyAudioService.LocalBinder)service;
            mBuddyAudioService = mLocalBinder.getBuddyAudioServiceInstance();
            mBuddyAudioService.setupBuddyAudioService(portNum);
            if(startThreadsBool) {
                mBuddyAudioService.autoRunThreads();
                startThreadsBool = false;
            }
            boundBool = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "ERROR: onServiceDisconnected");
            //should never be called
        }
    };


}