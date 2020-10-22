package com.example.survivorbuddy4mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MessageActivity extends AppCompatActivity {

    private String TAG = "MessageActivity";
    public TextView message_display;
    private MessageServer mMessageServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        message_display = (TextView) findViewById(R.id.messages_textview);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

        mMessageServer = new MessageServer(5050, this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mMessageServer.startServer();
            }
        }).start();


    }

    public void setMD(String mt) {
        Log.i(TAG, "setMD");
        message_display.setText(mt);
    }


}