package com.example.survivorbuddy4mobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.pedro.rtspserver.RtspServerCamera1;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtspserver.RtspServerCamera2;

import java.net.SocketException;

public class RtspService extends Service implements ConnectCheckerRtsp{


    private RtspServerCamera2 mServerCam = null;
    private String endpoint = "";
    private String TAG = "[SB4] RtspService";
    private NotificationManager mNotificationManager = null;
    private Context mContext = null;
    private int portNum = 1935;
    private String channelID = "rstpServerChannel";
    private int notifyID = 123456;
    private String notifTitle = "Survivor Buddy";

    private IBinder mBinder = new LocalBinder();



    public RtspService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
    }

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



    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        // TODO: Return the communication channel to the service.
        return mBinder;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
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

    public String get_endpoint() {
        Log.i(TAG, "get_endpoint");
        return mServerCam.getEndPointConnection();
    }


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


    @Override
    public void onConnectionSuccessRtsp() {
        Log.i(TAG, "onConnectionSuccessRtsp");
        showNotification("Client Connected");
    }

    @Override
    public void onConnectionFailedRtsp(String reason) {
        Log.i(TAG, "onConnectionFailedRtsp");
        showNotification("Connection Failed");
    }

    @Override
    public void onNewBitrateRtsp(long bitrate) {
        Log.i(TAG, "onNewBitrateRtsp");
        //bitrate changes will not be supported
    }

    @Override
    public void onDisconnectRtsp() {
        Log.i(TAG, "onDisconnectRtsp");
        showNotification("Disconnected");
    }

    @Override
    public void onAuthErrorRtsp() {
        Log.i(TAG, "onAuthErrorRtsp");
        showNotification("Authorization Error");
    }

    @Override
    public void onAuthSuccessRtsp() {
        Log.i(TAG, "onAuthSuccessRtsp");
        showNotification("Authorization Success");
    }

    public class LocalBinder extends Binder {
        public RtspService getRtspServiceInstance() {
            Log.i(TAG, "getRtspServiceInstance");
            return RtspService.this;
        }
    }


}