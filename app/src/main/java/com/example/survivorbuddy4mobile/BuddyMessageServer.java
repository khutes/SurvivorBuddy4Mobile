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

/**
 * A TCP server which receives packets of text encoded as UTF-8 and writes it to an PipedOutputStream
 */
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


    /**
     * Contructor for BuddyMessageServer
     * @param port_num int, the port number used by the ServerSocket
     * @param dc_message String, the message which is sent to client upon server shutdown or that
     *                   client sends to shutdown the server
     * @param restart_bool Boolean, true if server should be restarted upon shutdown. NOT IMPLEMENTED
     */
    public BuddyMessageServer(int port_num, String dc_message, Boolean restart_bool) {
        this.portNum = port_num;
        this.disconnectMsg = dc_message;
        this.restartBool = restart_bool;
        this.toServicePipe = new PipedOutputStream();
        this.pipedStreamConnected = false;
        this.recvConnectionGood = true;
    }

    /**
     * Stops the server, send client disconnect message
     * @throws IOException
     */
    public void shutdownServer() throws IOException {

        recvConnectionGood = false;


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //if no client connects before shutdown some objects will be null
                    if(dataToClientStream != null) {
                        dataToClientStream.write(disconnectMsg.getBytes(StandardCharsets.UTF_8));
                        dataToClientStream.flush();
                        dataToClientStream.close();
                    }
                    toServicePipe.close();
                    if(clientSocket != null) {
                        clientSocket.close();
                    }
                    serverReceiveSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }




            }
        }).start();





    }

    /**
     * Provides pipedInputStream which is connected to the server's PipedOutputStream
     * @return PipedInputStream
     */
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

    /**
     * Starts the server
     * @throws IOException
     */
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

    /**
     * Loops receiveData() until shutdown/error
     * @throws IOException
     */
    private void receiveDataLoop() throws IOException {

        while(recvConnectionGood) {
            if(!receiveData()) {
                break;
            }
        }
        toServicePipe.close();
    }

    /**
     * Receives data from client and writes it to PipedOutputStream
     * @return boolean, true is read was successful, false otherwise
     * @throws IOException
     */
    public boolean receiveData() throws IOException {

        String input_line;
        byte[] bs = new byte[2048];
        int readReturn = dataFromClientStream.read(bs);

        if(readReturn == -1) {
            Log.i(TAG, "BREAK PIPE");
            dataFromClientStream.close();
            return false;
        }
        Log.i(TAG, "HERE3");
        if(pipedStreamConnected) {
            toServicePipe.write(bs);
            toServicePipe.flush();
        }



        input_line = new String(bs, StandardCharsets.UTF_8).trim();

        Log.i(TAG, "message_content: <" + input_line + ">");

        if(input_line.equals(disconnectMsg)) { return false; }

        return true;

    }

    //Setter function to aid in test development

    /**
     * Helper function for unit tests
     * @param d
     */
    public void setDataFromClientStream(DataInputStream d) {
        this.dataFromClientStream = d;
    }

    /**
     * Helper function for unit tests
     * @param p
     */
    public void setWritePipe(PipedOutputStream p) {
        this.toServicePipe = p;
    }

    /**
     * Helper function for unit tests
     * @param b
     */
    public void setPipedStreamConnected(boolean b) {
        this.pipedStreamConnected = b;
    }

}
