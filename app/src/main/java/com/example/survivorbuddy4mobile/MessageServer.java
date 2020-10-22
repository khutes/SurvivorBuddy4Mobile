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

    private String TAG = "MessageServer";

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int portNum;
    private DataInputStream fromClient;

    private MessageActivity messageActivity;


    public MessageServer(int portNum, MessageActivity activity) {
        this.portNum = portNum;
        this.messageActivity = activity;

    }


    public void startServer() {

        Log.i(TAG, "startServer");
        Log.i(TAG, Utils.getIPAddress(true));

        try {
            //connection setup
            serverSocket = new ServerSocket(portNum);
            clientSocket = serverSocket.accept();
            Log.i(TAG, "Connection accepted");
            fromClient = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));


            String inputLine = "";
            Log.i(TAG, "Waiting");
            while(true) {
                byte[] bs = new byte[2048];

                fromClient.read(bs);
                inputLine = new String(bs, StandardCharsets.UTF_8).trim();

                Log.i(TAG, "'" + inputLine + "'");
                updateMessageDisplay(inputLine);

                if("STOP".equals(inputLine)) {
                    break;
                }

            }
            Log.i(TAG, "server loop done");
        } catch(IOException e) {
            Log.e(TAG, "EXCEPTION", e );
        }
    }

    private void updateMessageDisplay(String messageText) {
        Log.i(TAG, "updateMessageDisplay");
        messageActivity.setMD(messageText);
    }






}
