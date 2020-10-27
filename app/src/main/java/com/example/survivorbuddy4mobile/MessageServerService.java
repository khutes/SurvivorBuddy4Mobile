package com.example.survivorbuddy4mobile;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MessageServerService extends Service {
    public MessageServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}