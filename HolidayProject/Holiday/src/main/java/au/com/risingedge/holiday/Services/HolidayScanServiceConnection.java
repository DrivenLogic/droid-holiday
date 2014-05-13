package au.com.risingedge.holiday.Services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (C) 2014 Tapestry International Limited. All rights reserved.
 * User: travis
 * Date: 13/05/2014
 */
public class HolidayScanServiceConnection implements ServiceConnection {
    private Logger log = LoggerFactory.getLogger(HolidayScanServiceConnection.class);
    private Context context;
    private IHolidayScanServiceConnectListener callback;

    public HolidayScanServiceConnection(Context context, final IHolidayScanServiceConnectListener callback){

        this.context = context;
        this.callback = callback;
    }

    public void connect() {
        Intent intent = new Intent(context, HolidayScanService.class);
        boolean result = context.bindService(intent, this, Context.BIND_AUTO_CREATE);
        log.info("Holiday Scanner bind service result: " + result);
    }

    public void disconnect(){
        context.unbindService(this);
    }

    @Override public void onServiceConnected(ComponentName componentName, IBinder binder) {
        callback.onServiceConnected((IHolidayScanner) binder);
    }

    @Override public void onServiceDisconnected(ComponentName componentName) {

    }
}
