package com.example.larslb.triggertest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RecieverService extends Service {
    public RecieverService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
