package com.example.survivorbuddy4mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

public class MessageActivity extends AppCompatActivity {

    private String TAG = "[SB4] MessageActivity";
    public TextView messageDisplay;
    private BuddyMessageService mBuddyMessageService;

    private int portNum;
    private int defaultPort;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        messageDisplay = (TextView) findViewById(R.id.messages_textview);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        defaultPort = Integer.parseInt(getString(R.string.default_message_port));
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        portNum = Integer.parseInt(String.valueOf(mPreferences.getInt("messagePort", defaultPort)));


        Intent intent = new Intent(MessageActivity.this, BuddyMessageService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mBroadcastReceiver, new IntentFilter(
                BuddyMessageService.BROADCAST_ACTION
        ));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void setMessageTextViewContent(String text_content) {
        Log.i(TAG, "setMessageTextViewContent");
        messageDisplay.setText(text_content);

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "BINDED");
            BuddyMessageService.LocalBinder mLocalBinder = (BuddyMessageService.LocalBinder)service;
            mBuddyMessageService = mLocalBinder.getBuddyMessageServiceInstance();
            mBuddyMessageService.setupBuddyMessageService(portNum, "_DISC");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setMessageTextViewContent(intent.getStringExtra("displayText"));
        }
    };






}