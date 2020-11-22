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

/**
 * The activity which displays messages received from the PC SurvivorBuddy app
 * Starts a Service which starts a TCP server
 */
public class MessageActivity extends AppCompatActivity {

    private String TAG = "[SB4] MessageActivity";
    public TextView messageDisplay;
    private BuddyMessageService mBuddyMessageService;

    private int portNum;
    private int defaultPort;
    private SharedPreferences mPreferences;

    /**
     * Default onCreate
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        messageDisplay = (TextView) findViewById(R.id.messages_textview);

    }

    /**
     * Default onStart, nothing happens here
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Default on resume. Gets the current message port number from settings and starts/binds to
     * the BuddyMessageService. Also registers a BroadcastReceiver paired with the service
     */
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

    /**
     * Default on stop. Nothing happens here
     */
    @Override
    protected void onStop() {
        super.onStop();
        //TODO: May need to add error handling here for unbinding and stopping message server
        Log.i(TAG, "onStop");
    }

    /**
     * Default onDestroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Sets the current message on screen
     * @param text_content a String of the message to be displayed
     */
    public void setMessageTextViewContent(String text_content) {
        Log.i(TAG, "setMessageTextViewContent");
        messageDisplay.setText(text_content);

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /**
         * Automatically called after binding to the BuddyMessageService.
         * Calls BuddyMessageService.setupMessageService and passes the port num
         * @param name ComponentName
         * @param service IBinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "BINDED");
            BuddyMessageService.LocalBinder mLocalBinder = (BuddyMessageService.LocalBinder)service;
            mBuddyMessageService = mLocalBinder.getBuddyMessageServiceInstance();
            mBuddyMessageService.setupBuddyMessageService(portNum, "_DISC");
        }

        /**
         * Called if BuddyMessageService unexpectedly stops
         * @param name ComponentName
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };



    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /**
         * Sets up the BroadcastReceiver paired with BuddyMessageService. Is called when a new
         * broadcast is received. Calls setMessageTextViewContent()
         * @param context a Context
         * @param intent an Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            setMessageTextViewContent(intent.getStringExtra("displayText"));
        }
    };






}