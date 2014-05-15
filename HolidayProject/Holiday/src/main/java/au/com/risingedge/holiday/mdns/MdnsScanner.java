/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday.mdns;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import au.com.risingedge.holiday.IScanCallbackListener;
import au.com.risingedge.holiday.NetworkInfrastructure;
import au.com.risingedge.holiday.ServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.io.IOException;


/**
 * A Runnable for threaded scanning
 *
 * @author andrew.stone@drivenlogic.com.au
 */
public class MdnsScanner implements Handler.Callback {

    private static final int START_SCAN = 1;
    private static final int STOP_SCAN = 2;

    private final Handler threadMessageHandler;
    private Logger log = LoggerFactory.getLogger(MdnsScanner.class);

    private final static String MDNS_SERVICE_TYPE = "_iotas._tcp.local.";

    private JmDNS jmdns = null;
    private IScanCallbackListener callbacklistener;
    private MdnsServiceListener mdnsServiceListener;

    /**
     * Constructor
     *
     * @param callbackListener callback interface for the activity
     */
    public MdnsScanner(IScanCallbackListener callbackListener) {
        callbacklistener = callbackListener;
        mdnsServiceListener = new MdnsServiceListener();

        // create our handler
        HandlerThread thread = new HandlerThread("Mdns Scanner Thread");
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
        log.info("Starting worker thread: : thread_id = " + thread.getId());

        // create looper to handle messages
        Looper looper = thread.getLooper();
        threadMessageHandler = new Handler(looper, this);
    }

    /**
     * Start mdns scanning asynchronously
     */
    public void startScanning() {
        threadMessageHandler.sendEmptyMessage(START_SCAN);
    }

    public void stopScanning() {
        if (threadMessageHandler != null){
            threadMessageHandler.sendEmptyMessage(STOP_SCAN);
        }
        callbacklistener = null;

    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case START_SCAN: {
                runStartScan();
                return true;
            }
            case STOP_SCAN: {
                jmdns.removeServiceListener(MDNS_SERVICE_TYPE, mdnsServiceListener);
                threadMessageHandler.getLooper().quit();
                return true;
            }
        }
        return false;
    }

    private void runStartScan() {

        // inform the callback that we have started
        callbacklistener.scanStarted("looking for Holiday...");

        try {

            String ipAddress = new NetworkInfrastructure().getLocalInetAddress().getHostAddress();
            log.info("Local IP Address is: " + ipAddress);

            jmdns = JmDNS.create(new NetworkInfrastructure().getLocalInetAddress(), ipAddress);
            jmdns.addServiceListener(MDNS_SERVICE_TYPE, mdnsServiceListener);

        }
        catch (IOException ex) {
            ex.printStackTrace();
            log.error("Error on jmDNS setup", ex);
        }
    }


    private class MdnsServiceListener implements ServiceListener {

        @Override
        public void serviceAdded(final ServiceEvent event) {
            log.info("Service added Called - Requesting service info");

            final String type = event.getType();
            final String name = event.getName();
            log.info("Service added: type= " + type + ", name: " + name);

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        // needs to be in it's own thread as it blocks
                        log.info("Service added: Requesting service info");
                        jmdns.requestServiceInfo(type, name);
                    }
                    catch (Throwable ex) {
                        ex.printStackTrace();
                        log.error("Error requesting service info", ex);
                    }
                }
            };
            thread.start();
        }

        @Override
        public void serviceResolved(final ServiceEvent serviceEvent) {
            log.info("Service resolved: " + serviceEvent.getInfo());

            if ((serviceEvent.getInfo().getURL() != null) && (serviceEvent.getInfo().getName() != null)) {
                if (callbacklistener != null) {
                    callbacklistener.serviceLocated(new ServiceResult(serviceEvent.getInfo().getURL(), serviceEvent.getInfo().getName(), ServiceResult.ScanType.JMDMS));
                }
            }
            else {
                log.warn("serviceEvent.getInfo() did not contain the required information to locate the Holiday");
            }
        }

        @Override
        public void serviceRemoved(ServiceEvent serviceEvent) {

            // ok in practice it appears this never fires.
            log.debug("Service removed: " + serviceEvent.getName() + "." + serviceEvent.getType());
        }

    }
}
