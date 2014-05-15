/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday.Services;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Binder;
import au.com.risingedge.holiday.IScanCallbackListener;
import au.com.risingedge.holiday.ServiceResult;
import au.com.risingedge.holiday.ServiceResults;
import au.com.risingedge.holiday.mdns.MdnsScanner;
import au.com.risingedge.holiday.tcp.TcpScanTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HolidayScanner extends Binder implements IHolidayScanner, IScanCallbackListener {

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
        releaseMulticastLock();
    }

    @Override
    public void beginMdnsSearch() {
        log.info("ServiceResults count: " + serviceResults.size());


        aquireMulticastLock();

        mdnsScanner = new MdnsScanner(this);
        mdnsScanner.startScanning();
    }

    @Override
    public void stopMdnsSearch() {
        if (mdnsScanner != null) {
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
    public void serviceLocated(ServiceResult serviceResult) {
        synchronized (serviceResults) {
            serviceResults.addServiceResult(serviceResult);

            if (listener != null){
                listener.onScanResults(serviceResults);
            }
        }
    }

    /**
     * Task is looking for services
     *
     * @param message
     */
    @Override public void scanStarted(String message) {
        listener.onScanStart(message);
    }

    /**
     * done looking
     */
    @Override public void scanCompleted() {
        synchronized (serviceResults) {
            listener.onScanResults(serviceResults);
        }
    }

    private void aquireMulticastLock() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // take a multicast lock
        multicastLock = wifiManager.createMulticastLock("lockString");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
    }

    private void releaseMulticastLock() {
        if ((multicastLock != null) && (multicastLock.isHeld())) {
            multicastLock.release();
        }
    }
}
