package com.example.survivorbuddy4mobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.concurrent.TimeUnit;

public class BuddyAudioService extends Service {

    private String TAG = "[SB4] BuddyAudioService";
    private BuddyAudioServer mBuddyAudioServer;
    private int portNum;
    private IBinder mBinder = new LocalBinder();
    private PipedInputStream serverPipe;
    private volatile boolean restartServerBool;
    private volatile boolean readAudioBool;
    private NotificationManager mNotificationManager;
    private int notifyID = 654321;
    private String channelID = "BuddyAudioChannel";
    private String notifTitle = "Survivor Buddy";

    private AudioTrack mAudioTrack;
    private int audioFrequency;
    private int audioChannelConfiguration;
    private int audioEncoding;
    private boolean audioPlaying;
    private int audioBufferSize;

    public BuddyAudioService() {
    }

    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //keeps the service running in the background
        setupNotificationManager();
        keepAliveTrick();
        restartServerBool = true;
        readAudioBool = true;
        return START_STICKY;
    }

    public void runServiceThreads() {
        Log.i(TAG, "runServiceThreads");
        mBuddyAudioServer = new BuddyAudioServer(portNum);
        serverPipe = mBuddyAudioServer.getPipe();

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mBuddyAudioServer.startServer();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "serverThreadEnd");
            }
        });

        Thread audioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    playAudioStream();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "audioThread end");
            }
        });
        serverThread.start();
        audioThread.start();

        try {
            serverThread.join();
            audioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "END all threads");
    }

    public void autoRunThreads() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(restartServerBool) {
                    runServiceThreads();
                }
                Log.i(TAG, "autoRun END");
            }
        }).start();

    }

    private void playAudioStream() throws IOException {
        audioBufferSize = 1024;
        audioChannelConfiguration = AudioFormat.CHANNEL_OUT_MONO;
        audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        audioFrequency = 44100;

        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                audioFrequency,
                audioChannelConfiguration,
                audioEncoding,
                audioBufferSize,
                AudioTrack.MODE_STREAM
        );

        Log.i(TAG, String.valueOf(mAudioTrack.getState()));
        mAudioTrack.play();

        while(!mBuddyAudioServer.pipeSet && !mBuddyAudioServer.stopCalled) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        serverPipe = mBuddyAudioServer.getPipe();

        while(readAudioBool) {
            byte[] bs = new byte[1024];

            int readCode = serverPipe.read(bs);

            if (readCode == -1) {
                break;
            }

            mAudioTrack.write(bs, 0, 1024);
            mAudioTrack.flush();
        }
        mAudioTrack.stop();
        mAudioTrack.release();


    }



    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        restartServerBool = false;
        readAudioBool = false;
        if(mBuddyAudioServer != null) {
            mBuddyAudioServer.stopServer();
        }

        showNotification("Survior Buddy Audio Server Stopped");
    }

    public void setupBuddyAudioService(int portNum) {
        this.portNum = portNum;
    }

    private void setupNotificationManager() {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelID,
                    channelID,
                    NotificationManager.IMPORTANCE_HIGH
            );
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private void keepAliveTrick() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, channelID)
                    .setOngoing(true)
                    .setContentTitle("Survivor Buddy 4.0")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText("Survivor Buddy Audio Server Started").build();
            startForeground(1, notification);
        } else {
            startForeground(1, new Notification());
        }
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


    public class LocalBinder extends Binder {
        public BuddyAudioService getBuddyAudioServiceInstance() {
            return BuddyAudioService.this;
        }
    }

}