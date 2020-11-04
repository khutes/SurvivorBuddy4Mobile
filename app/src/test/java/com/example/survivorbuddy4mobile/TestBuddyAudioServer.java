package com.example.survivorbuddy4mobile;

import android.provider.ContactsContract;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestBuddyAudioServer {


    private int defaultPortNum = 5050;
    private int defaultChunkSize = 1024;


    @Test
    public void receiveData_good_receive() throws IOException {
        /*
        Tests that receiveData returns True when successful and write the
        byte data to writePipe
         */

        //setup
        BuddyAudioServer bas = new BuddyAudioServer(this.defaultPortNum);
        bas.setChunkSize(this.defaultChunkSize);

        //write hardcoded data for testing
        byte[] testByteData = new byte[this.defaultChunkSize];
        Arrays.fill(testByteData, (byte) 2);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(testByteData));

        bas.setDataFromClientStream(dis);

        PipedOutputStream mockPipe = mock(PipedOutputStream.class);
        bas.setWritePipe(mockPipe);

        //run
        boolean r_bool = bas.receiveData();

        //check
        assert(r_bool);
        verify(mockPipe, times(1)).write(testByteData);
    }

    @Test
    public void receiveData_eof() throws IOException {
        /*
        Tests that receiveData handles when reading from a closed input stream. Should output
        false
         */

        //setup
        BuddyAudioServer bas = new BuddyAudioServer(this.defaultPortNum);
        bas.setChunkSize(this.defaultChunkSize);

        //write hardcoded data for testing
        byte[] testByteData = new byte[0];  //empty to sim eof
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(testByteData));
        bas.setDataFromClientStream(dis);

        //run
        boolean r_bool = bas.receiveData();


        //check
        assert(r_bool == false);

    }

}
