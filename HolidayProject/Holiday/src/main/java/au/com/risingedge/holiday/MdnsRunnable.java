package au.com.risingedge.holiday;

import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

///
/// A Runnable for threaded scanning
///
public class MdnsRunnable implements Runnable {

    private static final String TAG = "HolidayMdnsRunnable";

    private WifiManager.MulticastLock _multicastLock;
    private String _mdnsServiceType = "_iotas._tcp.local."; // _service._protocol.local.
    private JmDNS _jmdns = null;
    private ServiceListener _mdnsServicelistener;
    private WifiManager _wifiManager;
    private IMdnsCallbackListener _callbackListener;
    private android.os.Handler _mdnsResolutionHandler;
    private android.os.Handler _uiHandler;

    MdnsRunnable(IMdnsCallbackListener callbackListener, WifiManager wifiManager, android.os.Handler uiHandler) {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler()); // TODO: move to app class.
        _callbackListener = callbackListener;
        _wifiManager = wifiManager;
        _uiHandler = uiHandler;
    }

    @Override
    public void run() {

        _uiHandler.post(new Runnable() {
            @Override
            public void run() {
                _callbackListener.TaskBusy("looking for Holiday...");
            }
        });

        try {

            String ipAddress = Helpers.getLocalInetAddress().getHostAddress();
            Log.i(TAG, "Local IP Address is: " + ipAddress);

            _jmdns = JmDNS.create(Helpers.getLocalInetAddress(), ipAddress);
            _jmdns.addServiceListener(_mdnsServiceType, _mdnsServicelistener = new ServiceListener() {

                @Override
                public void serviceAdded(final ServiceEvent event) {
                    try {
                        // needs to be in it's own thread to raise events on droid
                        _jmdns.requestServiceInfo(event.getType(), event.getName());
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error requesting service info", e);
                    }
                }

                @Override
                public void serviceResolved(final ServiceEvent serviceEvent) {
                    Log.i(TAG, "Service resolved: " + serviceEvent.getInfo());

                    // TODO: add some null guards


                    // post to the UI thread
                    _uiHandler.post(new Runnable() {
                        public void run() {
                            _callbackListener.TaskCompleted(); // remove the spinner after the first result is available
                            _callbackListener.ServiceLocated(new ServiceResult(serviceEvent.getInfo().getURL(), serviceEvent.getInfo().getName()));
                        }
                    });
                }

                @Override
                public void serviceRemoved(ServiceEvent serviceEvent) {
                    Log.i(TAG, "Service removed : " + serviceEvent.getName() + "." + serviceEvent.getType());
                }

            });

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error on jmDNS setup", e);
        }
    }
}
