package au.com.risingedge.holiday.Services;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.SystemClock;
import au.com.risingedge.holiday.IScanCallbackListener;
import au.com.risingedge.holiday.ServiceResult;
import au.com.risingedge.holiday.ServiceResults;
import au.com.risingedge.holiday.mdns.MdnsRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Copyright (C) 2014 Tapestry International Limited. All rights reserved.
 * User: travis
 * Date: 13/05/2014
 */
public class HolidayScanner extends Binder implements IHolidayScanner, IScanCallbackListener {

    private static final long SCAN_TIMEOUT_MILLIS = 10000;

    private final Context context;
    private WifiManager.MulticastLock multicastLock;
    private Logger log = LoggerFactory.getLogger(HolidayScanner.class);
    private IHolidayScannerListener listener;
    private ServiceResults serviceResults = new ServiceResults();

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
    public void beginMdnsSearch(final Handler uiHandler) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // take a multicast lock
        multicastLock = wifiManager.createMulticastLock("lockString");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        new Thread(new MdnsRunnable(this, wifiManager, uiHandler)).start();

        log.debug("Scan running");

        // track scan time
        final long ScanStartTime = SystemClock.elapsedRealtime();

        // JMDNS has some quirks that need to be worked around.
        // an an interval check for a lack of results.
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                // post on the UI message pump
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        long endTime = SystemClock.elapsedRealtime();
                        long elapsedMilliSeconds = endTime - ScanStartTime;

                        CheckResults(elapsedMilliSeconds);
                    }
                });
            }

        }, 4000 // first check.
                , 1500); // subsequent checks...
    }

    /** See if the asynchronous scan operation has yielded any results */
    private void CheckResults(long scanTime) {

        // if the service results collection is empty and it's been long enough...
        if ((serviceResults.size() > 0) || (scanTime > SCAN_TIMEOUT_MILLIS)) {
            listener.onScanResults(serviceResults);
        }
    }

    @Override
    public void registerListener(IHolidayScannerListener listener){
        this.listener = listener;
    }

    /**
     * Called buy a worker when a scan locates a service
     * Implementation detail of IS canCallbackListener
     *
     * @param serviceResult
     */
    @Override
    public void ServiceLocated(ServiceResult serviceResult) {
        serviceResults.addServiceResult(serviceResult);
    }

    /**
     * Task is looking for services
     *
     * @param message
     */
    @Override public void ScanStarted(String message) {
        listener.onScanStart(message);
    }

    /**
     * done looking
     */
    @Override public void ScanCompleted() {
        listener.onScanResults(serviceResults);
    }
}
