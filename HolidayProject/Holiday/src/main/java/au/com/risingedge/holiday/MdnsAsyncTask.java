package au.com.risingedge.holiday;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

///
/// MDNS scan as a task
///
/// waits for all results before returning
///
public class MdnsAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "HolidayMdnsAsyncTask";

    private WifiManager.MulticastLock _multicastLock;
    private String _mdnsServiceType = "_iotas._tcp.local."; // _service._protocol.local.
    private JmDNS _jmdns;
    private ServiceInfo[] _locatedMdnsServices;
    private WifiManager _wifiManager;
    private IMdnsCallbackListener _callbackListener;

    MdnsAsyncTask(IMdnsCallbackListener callbackListener, WifiManager wifiManager) {

        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler()); // TODO: Move to app class.
        _callbackListener = callbackListener;
        _wifiManager = wifiManager;
    }

    @Override
    protected void onPreExecute() {
        _callbackListener.TaskBusy("Looking for Holiday...");
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
            try {

                // take a multicast lock
                _multicastLock = _wifiManager.createMulticastLock("lockString");
                _multicastLock.acquire();

                Log.i(TAG, "MultiCast Lock State: " + _multicastLock.isHeld()); // This is still 'true' when disabled in firmware >:/

                String ipAddress = Helpers.getLocalInetAddress().getHostAddress();
                Log.i(TAG, "Local IP Address is: " + ipAddress);

                _jmdns = JmDNS.create(Helpers.getLocalInetAddress(), ipAddress);
                _locatedMdnsServices = _jmdns.list(_mdnsServiceType); // list services.

                // release lock
                _multicastLock.release();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error in jmDNS background task", e);
            }

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (_locatedMdnsServices == null || _locatedMdnsServices.length <= 0) {

            Log.i(TAG, "Did not find any holiday devices on network");
        } else {

            Log.i(TAG, "Adding controls to UI ");

            for (ServiceInfo serviceInfo : _locatedMdnsServices) {
                _callbackListener.ServiceLocated(new ServiceResult(serviceInfo.getURL(),serviceInfo.getName()));
            }
        }
        _callbackListener.TaskCompleted();

        super.onPostExecute(v);
    }
}
