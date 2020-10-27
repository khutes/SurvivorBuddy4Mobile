package com.example.survivorbuddy4mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MessageActivity extends AppCompatActivity {

    private String TAG = "[SB4] MessageActivity";
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

        String dc_message = this.getString(R.string.message_server_client_disconnect_message);
        mMessageServer = new MessageServer(5050, this, dc_message, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mMessageServer.startServer();
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    public void setMessageTextViewContent(String text_content) {
        Log.i(TAG, "setMessageTextViewContent");
        message_display.setText(text_content);

    }


}