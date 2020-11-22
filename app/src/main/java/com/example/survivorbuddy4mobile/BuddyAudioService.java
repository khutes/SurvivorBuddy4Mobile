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

/**
 * BuddyAudioService controls BuddyAudioServer and runs in the background continually until
 * explicitly stopped in BuddyAudioActvity
 * @author Kyle Hutto
 * @version 1.0
 */
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

    /**
     * Unused init function
     */
    public BuddyAudioService() {
    }

    /**
     * Called automatically when binded to
     * @param intent an Intent
     * @return LocalBinder
     */
    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    /**
     * Called automatically when service is started, starts the keep alive notifications
     * @param intent an Intent
     * @param flags int default arg
     * @param startId int default arg
     * @return Retuns START_STICKY so service is auto restarted if stopped by system
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //keeps the service running in the background
        setupNotificationManager();
        keepAliveTrick();
        restartServerBool = true;
        readAudioBool = true;
        return START_STICKY;
    }

    /**
     * Starts BuddyAudioServer in a thread and calls playAudioStream() in another
     */
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

    /**
     * Starts a thread which calls runServiceThreads()
     */
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

    /**
     * Reads data from a PipedInputStream which is connected to BuddyAudioServer and plays that
     * raw byte data through the android speakers. Plays audio in MONO format. Continues until
     * service is stopped.
     * @throws IOException
     */
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


    /**
     * Automatically called when service is stopped. Stops all threads on service and stops
     * stops BuddyAudioServer
     */
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

    /**
     * Wrapper function to help pass the port number from the activity to the service
     * @param portNum int, the port number which will be used by BuddyAudioServer
     */
    public void setupBuddyAudioService(int portNum) {
        this.portNum = portNum;
    }

    /**
     * Creates/inits a NotificationManager
     */
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

    /**
     * Uses notifications to prevent the system from killing the service on it own
     */
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

    /**
     * Displays temporary notification on screen
     * @param text String, the message which will show on the notification
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
     * A simple LocalBinder
     */
    public class LocalBinder extends Binder {
        /**
         * Returns this instance of BuddyAudioService
         * @return BuddyAudioService, this instance
         */
        public BuddyAudioService getBuddyAudioServiceInstance() {
            return BuddyAudioService.this;
        }
    }

}