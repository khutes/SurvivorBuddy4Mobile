package com.example.survivorbuddy4mobile;


import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestBuddyMessageServer {

    private int defaultPort = 5050;
    private String defaultDiscMsg = "_DISC";
    private boolean defaultRestartBool = true;



    @Test
    public void receiveData_good_receipt_pipe_connected() throws IOException {
        /*
        Tests that receiveData() returns true upon successful receipt of data and that
        write for the toServicePipe is called with the correct argument
         */

        BuddyMessageServer bms = new BuddyMessageServer(defaultPort, defaultDiscMsg, defaultRestartBool);

        byte[] testByteData = new byte[100];
        Arrays.fill(testByteData, (byte) 1);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(testByteData));

        bms.setDataFromClientStream(dis);

        PipedOutputStream mockPipe = mock(PipedOutputStream.class);
        bms.setWritePipe(mockPipe);
        bms.setPipedStreamConnected(true);

        //run
        boolean r_bool = bms.receiveData();

        //check
        byte[] checkBytes = new byte[2048];
        Arrays.fill(checkBytes, (byte) 0);
        for(int i=0; i<testByteData.length; i++) {
            checkBytes[i] = (byte) 1;
        }

        verify(mockPipe, times(1)).write(checkBytes);
        assert(r_bool);



    }

    @Test
    public void receiveData_good_receipt_pipe_not_connected() throws IOException {
        /*
        Tests that receiveData() returns true upon successful receipt of data and that
        write for the toServicePipe is not called
         */

        BuddyMessageServer bms = new BuddyMessageServer(defaultPort, defaultDiscMsg, defaultRestartBool);

        byte[] testByteData = new byte[100];
        Arrays.fill(testByteData, (byte) 1);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(testByteData));

        bms.setDataFromClientStream(dis);

        PipedOutputStream mockPipe = mock(PipedOutputStream.class);
        bms.setWritePipe(mockPipe);
        bms.setPipedStreamConnected(false);

        //run
        boolean r_bool = bms.receiveData();

        //check
        verify(mockPipe, times(0)).write(testByteData);
        assert(r_bool);

    }


    @Test
    public void receiveData_eof() throws IOException {
        /*
        Tests that receiveData handles when reading from a closed input stream. Should output
        false
         */

        //setup
        BuddyMessageServer bms = new BuddyMessageServer(defaultPort, defaultDiscMsg, defaultRestartBool);

        //write hardcoded data for testing
        byte[] testByteData = new byte[0];  //empty to sim eof
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(testByteData));
        bms.setDataFromClientStream(dis);

        //run
        boolean r_bool = bms.receiveData();


        //check
        assert (r_bool == false);

    }

}
