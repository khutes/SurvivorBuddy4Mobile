package com.example.survivorbuddy4mobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
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

public class RtspService extends Service implements ConnectCheckerRtsp{


    private RtspServerCamera2 mServerCam = null;
    private String endpoint = "";
    private String TAG = "RtspService";
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
        super.onCreate();
    }

    private void keepAliveTrick() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, channelID)
                    .setOngoing(true)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        } else {
            startForeground(1, new Notification());
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        init_keep_alive();
        mServerCam = new RtspServerCamera2(this, true, this, portNum);

        if(!mServerCam.isStreaming()) {
            if(mServerCam.isRecording() || (mServerCam.prepareAudio() && mServerCam.prepareVideo())) {
                mServerCam.startStream();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "RTSP Service Destroyed");
        mServerCam.stopStream();
        showNotification("Survivor Buddy Stream Stopped");
    }

    private void init_keep_alive() {
        Log.i(TAG, "RtspService Created");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }
        keepAliveTrick();
    }

    public String get_endpoint() {
        return mServerCam.getEndPointConnection();
    }


    private void showNotification(String text) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notifTitle)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        mNotificationManager.notify(notifyID, notification.build());
    }


    @Override
    public void onConnectionSuccessRtsp() {
        showNotification("Client Connected");
    }

    @Override
    public void onConnectionFailedRtsp(String reason) {
        showNotification("Connection Failed");
    }

    @Override
    public void onNewBitrateRtsp(long bitrate) {
        //bitrate changes will not be supported
    }

    @Override
    public void onDisconnectRtsp() {
        showNotification("Disconnected");
    }

    @Override
    public void onAuthErrorRtsp() {
        showNotification("Authorization Error");
    }

    @Override
    public void onAuthSuccessRtsp() {
        showNotification("Authorization Success");
    }

    public class LocalBinder extends Binder {
        public RtspService getRtspServiceInstance() {
            return RtspService.this;
        }
    }


}