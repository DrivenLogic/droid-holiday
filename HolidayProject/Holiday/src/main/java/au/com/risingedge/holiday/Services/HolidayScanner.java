package au.com.risingedge.holiday.Services;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import au.com.risingedge.holiday.IScanCallbackListener;
import au.com.risingedge.holiday.mdns.MdnsRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (C) 2014 Tapestry International Limited. All rights reserved.
 * User: travis
 * Date: 13/05/2014
 */
public class HolidayScanner extends Binder implements IHolidayScanner {

    private final Context context;
    private WifiManager.MulticastLock multicastLock;
    private Logger log = LoggerFactory.getLogger(HolidayScanner.class);

    public HolidayScanner(Context context) {
        this.context = context;
    }

    public void onCreate() {

    }

    public void onDestroy() {
        if ((multicastLock != null) && (multicastLock.isHeld())) {
            multicastLock.release();
        }
    }

    @Override
    public void beginMdnsSearch(IScanCallbackListener listener, final Handler uiHandler) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // take a multicast lock
        multicastLock = wifiManager.createMulticastLock("lockString");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        new Thread(new MdnsRunnable(listener, wifiManager, uiHandler)).start();

        log.debug("Scan running");
    }
}
