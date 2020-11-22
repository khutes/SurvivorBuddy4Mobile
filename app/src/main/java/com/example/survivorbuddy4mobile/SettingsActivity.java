package com.example.survivorbuddy4mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity used to change the port settings for BuddyMessageServer, BuddyAudioServer, and RtspServer
 * Also displays the current IP address of the device
 */
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

    /**
     * Called onCreate
     * Gets the various GUI elements from the layout
     * @param savedInstanceState
     */
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

    /**
     * Called onResume
     * Updates the displayed IP address
     * Updates the port boxes with current port settings
     */
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

    /**
     * Validates if an entered port number is valid
     * @param portNum int, the port number to be validated
     * @return boolean, true if port is valid, false otherwise
     */
    private boolean validatePortNum(int portNum) {
        Log.i(TAG, "validatingPort");
        if(portNum >= 1 && portNum <= 65535) {
            Log.i(TAG, "portGood");
            return true;
        }
        return false;
    }

    /**
     * Writes all the port settings currently displayed to SharedPreferences and checks for validity
     * @param view View
     */
    public void applySettings(View view) {
        Log.i(TAG, "applySettings");
        mEditor = mPreferences.edit();
        int rtsp_num = 0;
        int audio_num = 0;
        int message_num = 0;
        boolean parseGood = true;
        try {
            rtsp_num = Integer.parseInt((String.valueOf(rtspPortInput.getText())));
            audio_num = Integer.parseInt((String.valueOf(audioPortInput.getText())));
            message_num = Integer.parseInt((String.valueOf(messagePortInput.getText())));
        } catch (NumberFormatException e) {
            Toast.makeText(SettingsActivity.this, "One or more invalid ports", Toast.LENGTH_SHORT).show();
            parseGood = false;
        }

        if(parseGood) {

            boolean allApplied = true;

            if (validatePortNum(rtsp_num)) {
                mEditor.putInt("rtspPort", rtsp_num);
            } else {
                allApplied = false;
                Toast.makeText(SettingsActivity.this, "Invalid Phone to PC AV Port", Toast.LENGTH_SHORT).show();
            }

            if (validatePortNum(audio_num)) {
                mEditor.putInt("audioPort", audio_num);
            } else {
                allApplied = false;
                Toast.makeText(SettingsActivity.this, "Invalid PC to Phone Audio Port", Toast.LENGTH_SHORT).show();
            }

            if (validatePortNum(message_num)) {
                mEditor.putInt("messagePort", message_num);
            } else {
                allApplied = false;
                Toast.makeText(SettingsActivity.this, "Invalid PC to Phone Message Port", Toast.LENGTH_SHORT).show();
            }

            mEditor.commit();
            if (allApplied) {
                Toast.makeText(SettingsActivity.this, "Applied", Toast.LENGTH_SHORT).show();
            }
        }

    }

}