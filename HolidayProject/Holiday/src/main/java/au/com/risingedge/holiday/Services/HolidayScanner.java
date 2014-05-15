package au.com.risingedge.holiday.Services;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.SystemClock;
import au.com.risingedge.holiday.IScanCallbackListener;
import au.com.risingedge.holiday.ServiceResult;
import au.com.risingedge.holiday.ServiceResults;
import au.com.risingedge.holiday.mdns.MdnsScanner;
import au.com.risingedge.holiday.tcp.TcpScanTask;
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
    private MdnsScanner mdnsScanner;

    public HolidayScanner(Context context) {
        this.context = context;
    }

    public void onCreate() {
    }

    public void onDestroy() {
        log.info("HolidayScanner service destroyed");
        if ((multicastLock != null) && (multicastLock.isHeld())) {
            multicastLock.release();
        }
    }

    @Override
    public void beginMdnsSearch() {
        log.info("ServiceResults count: " + serviceResults.size());


        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // take a multicast lock
        multicastLock = wifiManager.createMulticastLock("lockString");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        mdnsScanner = new MdnsScanner(this);
        mdnsScanner.startScanning();

        log.debug("Scan running");
        monitorMdnsResults();
    }

    @Override
    public void stopMdnsSearch() {
        if (mdnsScanner != null){
            mdnsScanner.stopScanning();
        }
    }

    @Override
    public void beginTcpScan() {
        new TcpScanTask(listener).execute();
    }

    @Override
    public void registerListener(IHolidayScannerListener listener) {
        this.listener = listener;
    }

    @Override
    public void unregisterListener(IHolidayScannerListener listener) {
        this.listener = null;
    }

    /**
     * Called buy a worker when a scan locates a service
     * Implementation detail of IS canCallbackListener
     *
     * @param serviceResult
     */
    @Override
    public void ServiceLocated(ServiceResult serviceResult) {
        synchronized (serviceResults) {
            serviceResults.addServiceResult(serviceResult);
        }
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
        synchronized (serviceResults) {
            listener.onScanResults(serviceResults);
        }
    }

    private void monitorMdnsResults() {
        // track scan time
        final long ScanStartTime = SystemClock.elapsedRealtime();

        // JMDNS has some quirks that need to be worked around.
        // an an interval check for a lack of results.
        int initialDelay = 4000;   // first check.
        int monitorFrequency = 1500; // subsequent checks...

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long elapsedMilliSeconds = SystemClock.elapsedRealtime() - ScanStartTime;

                // if the service results collection is empty and it's been long enough...
                if ((serviceResults.size() > 0) || (elapsedMilliSeconds > SCAN_TIMEOUT_MILLIS)) {
                    listener.onScanResults(serviceResults);
                }

            }
        }, monitorFrequency, monitorFrequency);
    }

}
