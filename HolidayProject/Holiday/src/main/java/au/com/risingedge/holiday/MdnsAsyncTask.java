package au.com.risingedge.holiday;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class MdnsAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "HolidayMdnsAsyncTask";

    WifiManager.MulticastLock _multicastLock;
    private String _mdnsServiceType = "_iotas._tcp.local."; // _service._protocol.local.
    private JmDNS _jmdns = null;
    private ServiceListener _mdnsServicelistener = null;
    ProgressDialog _progressDialog;
    private Activity _activity;
    ServiceInfo[] _locatedMdnsServices;
    WifiManager _wifiManager;

    ///
    /// ctor - pass in activity
    ///
    MdnsAsyncTask(Activity activity) {
        // catch unhandled worker Exceptions
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        _activity = activity;
    }

    ///
    /// Runs on the UI thread
    ///
    @Override
    protected void onPreExecute() {

        _wifiManager = (WifiManager) _activity.getSystemService(android.content.Context.WIFI_SERVICE);

         _progressDialog = new ProgressDialog(_activity);
         _progressDialog.setMessage("Looking for Holiday...");
         _progressDialog.show();

        super.onPreExecute();
    }

    ///
    /// Runs on a thread pool thread
    ///
    @Override
    protected Void doInBackground(Void... voids) {
            try {

                // take a multicast lock
                _multicastLock = _wifiManager.createMulticastLock("lockString");
                _multicastLock.acquire();

                Log.i(TAG, "MultiCast Lock State: " + _multicastLock.isHeld());

                String ipAddress = Helpers.getLocalInetAddress().getHostAddress();
                Log.i(TAG, "Local IP Address is: " + ipAddress);

                _jmdns = JmDNS.create(Helpers.getLocalInetAddress(), ipAddress);
                _locatedMdnsServices = _jmdns.list(_mdnsServiceType); // list services.

                // release lock
                _multicastLock.release();

                // clean up jmdms
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
                        Log.e(TAG, "Error on stopping jmDNS", e);
                    }
                    _jmdns = null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error in jmDNS background task", e);
            }

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {

        if(_progressDialog!=null && _progressDialog.isShowing())
        {
            //hide the dialog
            _progressDialog.dismiss();
        }

        if (_locatedMdnsServices == null || _locatedMdnsServices.length <= 0) {

            Log.i(TAG, "Did not find any holiday devices on network");
            Helpers.AddNotFoundControls(_activity);

        } else {

            Log.i(TAG, "Adding controls to UI ");

            for (ServiceInfo serviceInfo : _locatedMdnsServices) {
                Log.i(TAG, serviceInfo.getName());
                Log.i(TAG, serviceInfo.getURLs()[0]);

                Helpers.AddHolidayControls(
                        _activity,
                        serviceInfo.getName(),
                        serviceInfo.getURLs()[0]);
            }
        }
        super.onPostExecute(v);
    }
}
