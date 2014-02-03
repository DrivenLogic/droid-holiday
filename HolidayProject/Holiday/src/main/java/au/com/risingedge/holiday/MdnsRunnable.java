/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

import android.net.wifi.WifiManager;
import android.os.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * A Runnable for threaded scanning
 *
 * @author andrew.stone@drivenlogic.com.au
 */
public class MdnsRunnable implements Runnable {

    private Logger _log = LoggerFactory.getLogger(MdnsRunnable.class);

    private final static String MDNS_SERVICE_TYPE = "_iotas._tcp.local."; //_<protocol>._<transportlayer>

    private WifiManager.MulticastLock _multicastLock;
    private JmDNS _jmdns = null;
    private ServiceListener _mdnsServicelistener;
    private WifiManager _wifiManager;
    private IMdnsCallbackListener _callbackListener;
    private Handler _mdnsResolutionHandler;
    private Handler _uiHandler;

    /**
     * Constructor
     *
     * @param callbackListener callback interface for the activity
     * @param wifiManager      current connected WiFi manager for the device
     * @param uiHandler        a handler created by the UI thread
     */
    MdnsRunnable(IMdnsCallbackListener callbackListener, WifiManager wifiManager, Handler uiHandler) {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler()); // TODO: move to app class.
        _callbackListener = callbackListener;
        _wifiManager = wifiManager;
        _uiHandler = uiHandler;
        _mdnsResolutionHandler = new Handler();
    }

    /**
     * The runnable run method
     */
    @Override
    public void run() {

        // inform the callback that we have started
        _uiHandler.post(new Runnable() {
            @Override
            public void run() {
                _callbackListener.TaskBusy("looking for Holiday...");
            }
        });

        try {

            String ipAddress = new NetworkInfrastructure().getLocalInetAddress().getHostAddress();
            _log.info("Local IP Address is: " + ipAddress);

            _jmdns = JmDNS.create(new NetworkInfrastructure().getLocalInetAddress(), ipAddress);
            _jmdns.addServiceListener(MDNS_SERVICE_TYPE, _mdnsServicelistener = new ServiceListener() {

                @Override
                public void serviceAdded(final ServiceEvent event) {
                    try {
                        _log.info("Service added Called - Requesting service info");

                        _mdnsResolutionHandler.post(new Runnable() {
                            public void run() {
                                // needs to be in it's own thread to raise events on droid
                                _jmdns.requestServiceInfo(event.getType(), event.getName());
                            }
                        });

                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        _log.error("Error requesting service info", ex);
                    }
                }

                @Override
                public void serviceResolved(final ServiceEvent serviceEvent) {
                    _log.info("Service resolved: " + serviceEvent.getInfo());

                    if ((serviceEvent.getInfo().getURL() != null) && (serviceEvent.getInfo().getName() != null)) {

                        // post to the UI thread
                        _uiHandler.post(new Runnable() {
                            public void run() {
                                _callbackListener.TaskCompleted(); // remove the spinner after the first result is available
                                _callbackListener.ServiceLocated(new ServiceResult(serviceEvent.getInfo().getURL(), serviceEvent.getInfo().getName()));
                            }
                        });
                    } else {
                        _log.warn("serviceEvent.getInfo() did not contain the required information to locate the Holiday");
                    }
                }

                @Override
                public void serviceRemoved(ServiceEvent serviceEvent) {
                    _log.debug("Service removed: " + serviceEvent.getName() + "." + serviceEvent.getType());
                }

            });

        } catch (IOException ex) {
            ex.printStackTrace();
            _log.error("Error on jmDNS setup", ex);
        }
    }
}
