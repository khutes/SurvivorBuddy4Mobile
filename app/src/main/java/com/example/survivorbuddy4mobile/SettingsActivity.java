package com.example.survivorbuddy4mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    String TAG = "[SB4] SettingsActivity";

    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditor;
    EditText rtspPortInput;
    EditText audioPortInput;
    EditText messagePortInput;
    TextView ipDisplay;

    int defaultRtspPort;
    int defaultAudioPort;
    int defaultMessagePort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_settings);

        rtspPortInput = (EditText) findViewById(R.id.rtsp_port_input);
        audioPortInput = (EditText) findViewById(R.id.audio_port_input);
        messagePortInput = (EditText) findViewById(R.id.message_port_input);

        ipDisplay = (TextView) findViewById(R.id.ip_addr_text_view);


    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");
        String ipAddr = Utils.getIPAddress(true);
        if(ipAddr.equals("")) {
            ipAddr = "None";
        }
        ipAddr = "IP Address: " + ipAddr;
        ipDisplay.setText(ipAddr);
        Log.i(TAG, "IPADDR: " + Utils.getIPAddress(true));

        defaultRtspPort = Integer.parseInt(getString(R.string.default_rtsp_port));
        defaultAudioPort = Integer.parseInt(getString(R.string.default_audio_port));
        defaultMessagePort = Integer.parseInt(getString(R.string.default_message_port));

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        rtspPortInput.setText(Integer.toString(mPreferences.getInt("rtspPort", defaultRtspPort)));
        audioPortInput.setText(Integer.toString(mPreferences.getInt("audioPort", defaultAudioPort)));
        messagePortInput.setText(Integer.toString(mPreferences.getInt("messagePort", defaultMessagePort)));

    }

    public void applySettings(View view) {

        mEditor = mPreferences.edit();
        mEditor.putInt("rtspPort", Integer.parseInt(String.valueOf(rtspPortInput.getText())));
        mEditor.putInt("audioPort", Integer.parseInt(String.valueOf(audioPortInput.getText())));
        mEditor.putInt("messagePort", Integer.parseInt(String.valueOf(messagePortInput.getText())));
        mEditor.commit();

    }

}