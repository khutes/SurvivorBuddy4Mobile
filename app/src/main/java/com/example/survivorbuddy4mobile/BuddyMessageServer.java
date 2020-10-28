package com.example.survivorbuddy4mobile;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BuddyMessageServer {

    private String TAG = "[SB4] BuddyMessageServer";

    public ServerSocket serverReceiveSocket;
    private Socket clientSocket;
    private int portNum;
    private DataInputStream dataFromClientStream;
    private DataOutputStream dataToClientStream;
    private volatile boolean recvConnectionGood;
    private String disconnectMsg;
    public boolean restartBool;
    public volatile boolean unbinding = false;



    private Boolean pipedStreamConnected;

    private PipedOutputStream toServicePipe;


    public BuddyMessageServer(int port_num, String dc_message, Boolean restart_bool) {
        this.portNum = port_num;
        this.disconnectMsg = dc_message;
        this.restartBool = restart_bool;
        this.toServicePipe = new PipedOutputStream();
        this.pipedStreamConnected = false;
        this.recvConnectionGood = true;
    }

    public void shutdownServer() throws IOException {

        recvConnectionGood = false;


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dataToClientStream.write(disconnectMsg.getBytes(StandardCharsets.UTF_8));
                    dataToClientStream.flush();
                    dataToClientStream.close();
                    toServicePipe.close();
                    clientSocket.close();
                    serverReceiveSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }




            }
        }).start();





    }

    public PipedInputStream getPipedStream() {
        PipedInputStream mp = null;
        try {
            mp = new PipedInputStream(toServicePipe, 2048);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pipedStreamConnected = true;
        return mp;
    }

    public void startServer() throws IOException{

        serverReceiveSocket = new ServerSocket(this.portNum);
        Log.i(TAG, "startServerReceive");
        Log.i(TAG, Utils.getIPAddress(true));
        clientSocket = serverReceiveSocket.accept();

        dataToClientStream = new DataOutputStream(
                new BufferedOutputStream(clientSocket.getOutputStream()));
        dataFromClientStream = new DataInputStream(
                new BufferedInputStream(clientSocket.getInputStream()));



        //starts thread to receive messages from client
        receiveDataLoop();

        serverReceiveSocket.close();


    }

    private void receiveDataLoop() throws IOException {

        String input_line;
        while(recvConnectionGood) {

            byte[] bs = new byte[2048];
            int readReturn = dataFromClientStream.read(bs);

            if(readReturn == -1) {
                Log.i(TAG, "BREAK PIPE");
                dataFromClientStream.close();
                break;
            }
            Log.i(TAG, "HERE3");
            if(pipedStreamConnected) {
                toServicePipe.write(bs);
                toServicePipe.flush();
            }



            input_line = new String(bs, StandardCharsets.UTF_8).trim();

            Log.i(TAG, "message_content: <" + input_line + ">");

            if(input_line.equals(disconnectMsg)) {

                break; }
        }
        toServicePipe.close();
    }





    /*
    private void startServer() {

        Log.i(TAG, "startServer");
        Log.i(TAG, Utils.getIPAddress(true));

        try {
            do {
                //connection setup
                serverSocket = new ServerSocket(portNum);
                clientSocket = serverSocket.accept();
                connectionGood = true;
                fromClient = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

                String inputLine = "";
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
     */
}
