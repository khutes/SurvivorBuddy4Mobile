package com.example.survivorbuddy4mobile;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.PipedInputStream;
import java.nio.charset.StandardCharsets;

/**
 * A service controlled by MessageActivity. Handled the start/stop of BuddyMessageServer.
 * Relays content received by BuddyMessageServer to MessageActivity.
 */
public class BuddyMessageService extends Service{

    private String TAG = "[SB4] BuddyMessageService";
    private BuddyMessageServer mBuddyMessageServer;
    private int portNum;
    private String disconnectMsg;
    private LocalBinder mBinder = new LocalBinder();
    private PipedInputStream pipeFromServer;
    private volatile boolean restartServerBool;

    public static final String BROADCAST_ACTION = "com.example.survivorbuddy4mobile.service";


    /**
     * Default constructor. Not used
     */
    public BuddyMessageService() {

    }

    /**
     * Automatically called when binded to.
     * @param intent
     * @return LocalBinder for easy passing of parameters
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Automatically called when unbinded from
     * Stops the BuddyMessageServer
     * @param intent
     * @return LocalBinder
     */
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

    /**
     * Starts a BuddyMessageServer in its own thread
     * @param port int, the port number that the BuddyMessageServer will be started with
     * @param disconnectMsg String, currently unused
     */
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

    /**
     * Starts a BuddyMessageServer and getDisplayMessage in their own threads
     */
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

    /**
     * Reads bytes from a PipedInputStream connected to a BuddyMessageServer and sends them to
     * MessageActivity as a broadcast. Continues until pipe is closed
     */
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

    /**
     * Broadcasts the content of a string
     * @param text String, the content of the broadcast
     */
    private void sendDisplayText(String text) {
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra("displayText", text);
        sendBroadcast(intent);
    }


    /**
     * A simple LocalBinder
     */
    public class LocalBinder extends Binder {
        /**
         * Returns this instance of BuddyMessageService. Used for easy parameter passing
         * @return BuddyMessageService, this instance of it
         */
        public BuddyMessageService getBuddyMessageServiceInstance() {
            return BuddyMessageService.this;
        }
    }






}