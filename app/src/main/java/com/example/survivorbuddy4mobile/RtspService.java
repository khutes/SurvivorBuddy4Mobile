package com.example.survivorbuddy4mobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.pedro.rtspserver.RtspServerCamera1;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtspserver.RtspServerCamera2;

import java.net.SocketException;

/**
 * A service which controls a RtspServer for sending Camera and Mic audio
 */
public class RtspService extends Service implements ConnectCheckerRtsp{


    private RtspServerCamera2 mServerCam = null;
    private String endpoint = "";
    private String TAG = "[SB4] RtspService";
    private NotificationManager mNotificationManager = null;
    private Context mContext = null;
    private int portNum;
    private int defaultPort;
    private String channelID = "rstpServerChannel";
    private int notifyID = 123456;
    private String notifTitle = "Survivor Buddy";

    private SharedPreferences mPreferences;

    private IBinder mBinder = new LocalBinder();


    /**
     * Default constructor
     */
    public RtspService() {
    }

    /**
     * Default onCreate
     */
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
    }

    /**
     * Used notifications to prevent systems from killing service on its own
     */
    private void keepAliveTrick() {
        Log.i(TAG, "keepAliveTrick");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, channelID)
                    .setOngoing(true)
                    .setContentTitle("Survivor Buddy 4.0 ")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText("Survivor Buddy Server Started").build();

            startForeground(1, notification);
        } else {
            Log.i(TAG, "ELSE");
            //startForeground(1, new Notification());
        }
    }


    /**
     * Called on bind. Returns a LocalBinder
     * @param intent Intent
     * @return LocalBinder
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    /**
     * Called automatically on service start. Gets the port number from application settings.
     * Instantiates and starts an RtspServerCamera2
     * @param intent
     * @param flags
     * @param startId
     * @return int START_STICKY, denotes that service should start itself if stopped by system
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        defaultPort = Integer.parseInt(getString(R.string.default_rtsp_port));
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        portNum = Integer.parseInt(String.valueOf(mPreferences.getInt("rtspPort", defaultPort)));


        init_keep_alive();
        mServerCam = new RtspServerCamera2(this, true, this, portNum);


        if(!mServerCam.isStreaming()) {
            if(mServerCam.isRecording() || (mServerCam.prepareAudio() && mServerCam.prepareVideo())) {
                mServerCam.startStream();
                mServerCam.switchCamera();
            }
        }
        return START_STICKY;
    }

    /**
     * Called on service stop
     * Stops the RtspServerCamera2 if running and shows stop notification
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if(mServerCam.isStreaming()) {
            mServerCam.stopStream();
            Log.i(TAG, "AFTER stopStream");
        }
        showNotification("Survivor Buddy Stream Stopped");
    }

    //TODO: Refactor this function
    /**
     * Inits the notification manager used by keepAliveTrick and calls keepAliveTrick
     */
    private void init_keep_alive() {
        Log.i(TAG, "init_keep_alive");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }
        keepAliveTrick();
        Log.i(TAG, "END_init_keep_alive");
    }

    /**
     * Gets the endpoint url of RtpsServerCamera2
     * @return String, The endpoint url
     */
    public String get_endpoint() {
        Log.i(TAG, "get_endpoint");
        return mServerCam.getEndPointConnection();
    }


    /**
     * Displays temportary notification to screen
     * @param text String, the content of the notification
     */
    private void showNotification(String text) {
        Log.i(TAG, "showNotification");
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notifTitle)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Log.i(TAG, "notification built");


        mNotificationManager.notify(notifyID, notification.build());
        Log.i(TAG, "notification END");
    }


    /**
     * Shows notification when client connects to RtpsServerCamera2
     */
    @Override
    public void onConnectionSuccessRtsp() {
        Log.i(TAG, "onConnectionSuccessRtsp");
        showNotification("Client Connected");
    }

    /**
     * Show notification id server connection fails
     * @param reason
     */
    @Override
    public void onConnectionFailedRtsp(String reason) {
        Log.i(TAG, "onConnectionFailedRtsp");
        showNotification("Connection Failed");
    }

    /**
     * NOT USED
     * @param bitrate
     */
    @Override
    public void onNewBitrateRtsp(long bitrate) {
        Log.i(TAG, "onNewBitrateRtsp");
        //bitrate changes will not be supported
    }

    /**
     * Shows notification when client disconnects from rtsp server
     */
    @Override
    public void onDisconnectRtsp() {
        Log.i(TAG, "onDisconnectRtsp");
        showNotification("Disconnected");
    }

    /**
     * Show notification upon authentication error
     */
    @Override
    public void onAuthErrorRtsp() {
        Log.i(TAG, "onAuthErrorRtsp");
        showNotification("Authorization Error");
    }

    /**
     * Shows notification up authentication success
     */
    @Override
    public void onAuthSuccessRtsp() {
        Log.i(TAG, "onAuthSuccessRtsp");
        showNotification("Authorization Success");
    }

    /**
     * Simple LocalBinder
     */
    public class LocalBinder extends Binder {
        /**
         * Returns the current instance of the service
         * @return RtspService, this instance
         */
        public RtspService getRtspServiceInstance() {
            Log.i(TAG, "getRtspServiceInstance");
            return RtspService.this;
        }
    }


}