package com.example.survivorbuddy4mobile;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MessageServer {

    private String TAG = "[SB4] MessageServer";

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int portNum;
    private DataInputStream fromClient;
    private Boolean connectionGood;
    private String disconnectMessage;
    private Boolean restartServer;

    private MessageActivity messageActivity;


    public MessageServer(int portNum, MessageActivity activity, String dc_message, Boolean restart_bool) {
        this.portNum = portNum;
        this.messageActivity = activity;
        this.disconnectMessage = dc_message;
        this.restartServer = restart_bool;

    }


    public void startServer() {

        Log.i(TAG, "startServer");
        Log.i(TAG, Utils.getIPAddress(true));

        try {
            do {
                //connection setup
                Log.i(TAG, "serverSocket assign");
                serverSocket = new ServerSocket(portNum);
                Log.i(TAG, "accept()");
                clientSocket = serverSocket.accept();
                Log.i(TAG, "accept() done");
                connectionGood = true;
                Log.i(TAG, "Connection accepted");
                fromClient = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

                String inputLine = "";
                Log.i(TAG, "Waiting");
                while (connectionGood) {
                    byte[] bs = new byte[2048];

                    int len_msg = fromClient.read(bs);
                    //checks for unexpected disconnect
                    if (len_msg == -1) {
                        connectionGood = false;
                        continue;
                    }

                    inputLine = new String(bs, StandardCharsets.UTF_8).trim();

                    if (inputLine.equals(disconnectMessage)) {
                        connectionGood = false;
                        continue;
                    }

                    Log.i(TAG, "read return value: " + len_msg);
                    Log.i(TAG, "message_content: <" + inputLine + ">");

                    messageActivity.setMessageTextViewContent(inputLine);

                }
                Log.i(TAG, "server loop done");
                serverSocket.close();
            } while(restartServer);
        } catch(IOException e) {
            Log.e(TAG, "EXCEPTION", e );
        }
    }

}
