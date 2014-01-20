package au.com.risingedge.holiday;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import static au.com.risingedge.holiday.Helpers.Alert;

///
/// AsyncTask for mdns Scanning.
///
public class MdnsRunnable implements Runnable, Cleanable {

    private static final String TAG = "HolidayMdnsAsyncTask";

    WifiManager.MulticastLock _multicastLock;
    private String _mdnsServiceType = "_iotas._tcp.local."; // _service._protocol.local.
    private JmDNS _jmdns = null;
    private ServiceListener _mdnsServicelistener = null;
    ProgressDialog _progressDialog;
    private Activity _activity;
    private Handler _uiHandler;

    Handler _mdnsResolutionHandler = new Handler();

    MdnsRunnable(Activity activity, Handler uiHandler)
    {
        _activity = activity;
        _uiHandler = uiHandler;

        // catch unhandled Exceptions
        Thread.setDefaultUncaughtExceptionHandler(new BackgroundExceptionHandler(this));
    }

    @Override
    public void run() {

        WifiManager wifiManager = (WifiManager)_activity.getSystemService(android.content.Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()){
            Log.w(TAG, "WiFi is off! - Can't scan - user needs to enable WiFi");

            // Throw? notify the UI?
        }
        else
        {
            try {

                _uiHandler.post(new Runnable() {
                    public void run() {
                        _progressDialog = new ProgressDialog(_activity);
                        _progressDialog.setMessage("Looking for Holiday...");
                        _progressDialog.show();
                    }
                });

                String ipAddress = Helpers.getLocalInetAddress().getHostAddress();
                Log.i(TAG,"Local IP Address is: " + ipAddress);

                _jmdns = JmDNS.create(Helpers.getLocalInetAddress(),ipAddress);
                _jmdns.addServiceListener(_mdnsServiceType, _mdnsServicelistener = new ServiceListener() {

                    @Override
                    public void serviceAdded(final ServiceEvent event) {
                        _mdnsResolutionHandler.post(new Runnable() {
                            public void run() {
                                try {
                                    // needs to be in it own thread to raise events on droid
                                    _jmdns.requestServiceInfo(event.getType(), event.getName());
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Error requesting service info", e);
                                    return;
                                }
                            }
                        });
                    }

                    @Override
                    public void serviceResolved(final ServiceEvent serviceEvent) {
                        Log.i(TAG, "Service resolved: " + serviceEvent.getInfo());

                        // TODO: add some null guards.

                        // post to the UI thread
                        _uiHandler.post(new Runnable() {
                            public void run() {

                                Log.i(TAG, "Adding controls to UI ");

                                //hide the dialog
                                _progressDialog.dismiss();

                                Helpers.AddDynamicHolidayControls(
                                        _activity,
                                        serviceEvent.getInfo().getName(),
                                        serviceEvent.getInfo().getURL());

                                // TODO: use array members instead

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
                Log.e(TAG,"Error on jmDNS setup",e);
            }
        }
    }

    @Override
    public void cleanUp() {
        Log.i(TAG, "Clean up called");

        _multicastLock.release(); // release multicast lock.

        if (_jmdns != null) {
            if (_mdnsServicelistener != null) {
                _jmdns.removeServiceListener(_mdnsServiceType, _mdnsServicelistener);
                _mdnsServicelistener = null;
            }
            _jmdns.unregisterAllServices();
            try {
                _jmdns.close();
            } catch (IOException e) {

                e.printStackTrace();
                Log.e(TAG,"Error on stopping",e);
                Alert("Error on stop " + e.getMessage(), _activity);
            }
            _jmdns = null;
        }
    }
}


