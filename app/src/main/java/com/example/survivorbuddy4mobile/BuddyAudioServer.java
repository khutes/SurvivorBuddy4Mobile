package com.example.survivorbuddy4mobile;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class BuddyAudioServer {

    private String TAG = "[SB4] BuddyAudioServer";

    private ServerSocket serverReceiveSocket;
    private Socket clientSocket;
    private int portNum;

    private DataInputStream dataFromClientStream;
    private PipedOutputStream writePipe;
    private PipedInputStream readPipe;

    private boolean continueReading;
    private int chunkSize;
    public volatile boolean pipeSet;
    public volatile boolean stopCalled;




    public BuddyAudioServer(int portNum) {
        this.portNum = portNum;
        this.writePipe = new PipedOutputStream();
        this.pipeSet = false;
        this.stopCalled = false;
    }

    public PipedInputStream getPipe() {
        return this.readPipe;
    }


    public void startServer() throws IOException {
        stopCalled = false;
        Log.i(TAG, "startServer");

        serverReceiveSocket = new ServerSocket(this.portNum);
        Log.i(TAG, String.valueOf(portNum));
        Log.i(TAG, "Waiting for connection");
        Log.i(TAG, Utils.getIPAddress(true));
        clientSocket = serverReceiveSocket.accept();
        Log.i(TAG, "good connection");

        dataFromClientStream = new DataInputStream(
                new BufferedInputStream(clientSocket.getInputStream())
        );

        byte[] bs = new byte[2048];
        int readReturn = dataFromClientStream.read(bs);

        if(readReturn == -1) {
            //TODO: Error handling
        } else {
            //first message must be the size of the incoming packets
            chunkSize = Integer.parseInt(new String(bs, StandardCharsets.UTF_8).trim());
            readPipe = new PipedInputStream(writePipe, chunkSize);
            pipeSet = true;
            continueReading = true;
            receiveDataLoop();
            writePipe.close();
        }
        serverReceiveSocket.close();
    }

    public void receiveDataLoop() throws IOException {

        while(continueReading) {

            /*
            int readReturn = 0;
            byte[] incomingBytes = new byte[chunkSize];
            try {
                readReturn = dataFromClientStream.read(incomingBytes);
            } catch(SocketException e) {
                dataFromClientStream.close();
                break;
            }

            if(readReturn == -1) {
                dataFromClientStream.close();
                break;
            }

            writePipe.write(incomingBytes);
            writePipe.flush();
            */

            if(!receiveData()) {
                break;
            }
        }
    }

    public boolean receiveData() throws IOException {
        int readReturn = 0;
        byte[] incomingBytes = new byte[chunkSize];
        try {
            readReturn = dataFromClientStream.read(incomingBytes);
        } catch(SocketException e) {
            dataFromClientStream.close();
            return false;
        }

        if(readReturn == -1) {
            dataFromClientStream.close();
            return false;
        }

        writePipe.write(incomingBytes);
        writePipe.flush();

        return true;

    }


    public void stopServer() {

        continueReading = false;
        stopCalled = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    writePipe.close();
                    pipeSet = false;
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

    //Setter function to aid in test development
    public void setDataFromClientStream(DataInputStream d) {
        this.dataFromClientStream = d;
    }

    public void setWritePipe(PipedOutputStream p) {
        this.writePipe = p;
    }

    public void setChunkSize(int c) {
        this.chunkSize = c;
    }



}
