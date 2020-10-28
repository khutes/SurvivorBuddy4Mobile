package com.example.survivorbuddy4mobile;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.PipedInputStream;
import java.nio.charset.StandardCharsets;

public class BuddyMessageService extends Service{

    private String TAG = "[SB4] BuddyMessageService";
    private BuddyMessageServer mBuddyMessageServer;
    private int portNum;
    private String disconnectMsg;
    private LocalBinder mBinder = new LocalBinder();
    private PipedInputStream pipeFromServer;
    private volatile boolean restartServerBool;

    public static final String BROADCAST_ACTION = "com.example.survivorbuddy4mobile.service";


    public BuddyMessageService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        Log.i(TAG, "onUnbind");

        restartServerBool = false;
        //close the pipe
        try {
            pipeFromServer.close();
            mBuddyMessageServer.shutdownServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }

    public void setupBuddyMessageService(int port, String disconnectMsg) {
        this.portNum = port;
        this.disconnectMsg = disconnectMsg;
        restartServerBool = true;

        //the main thread of the service
        new Thread(new Runnable() {
            @Override
            public void run() {
                serviceRunLoop();
            }
        }).start();
    }

    private void serviceRunLoop() {

        while(restartServerBool) {

            mBuddyMessageServer = new BuddyMessageServer(portNum, disconnectMsg, true);
            pipeFromServer = mBuddyMessageServer.getPipedStream();

            //start server and display threads
            Thread serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mBuddyMessageServer.startServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            Thread displayThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getDisplayMessage();
                }
            });

            serverThread.start();
            displayThread.start();


            //wait for server and display threads to end
            try {
                serverThread.join();
                displayThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getDisplayMessage() {

        String displayText;

        while(true) {

            byte[] bs = new byte[2048];

            try {
                int readReturn = pipeFromServer.read(bs);
                if(readReturn == -1) {
                    pipeFromServer.close();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            displayText = new String(bs, StandardCharsets.UTF_8).trim();
            sendDisplayText(displayText);

            Log.i(TAG, "HERE1");
            Log.i(TAG, "messageInService: <" + displayText + ">");
        }
    }

    private void sendDisplayText(String text) {
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra("displayText", text);
        sendBroadcast(intent);
    }




    public class LocalBinder extends Binder {
        public BuddyMessageService getBuddyMessageServiceInstance() {
            return BuddyMessageService.this;
        }
    }






}