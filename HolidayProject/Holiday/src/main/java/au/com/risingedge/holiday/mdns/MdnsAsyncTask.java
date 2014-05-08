/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 *  A Task for running a synchronous scan as async task
 *  _jmdns.list() is used to query services
 *
 *  NOTE: some devices return erratically when the approach is used.
 *  Current testing favors the Runnable approach in MdnsRunnable
 *  @see au.com.risingedge.holiday.mdns.MdnsRunnable
 *
 *  @author andrew.stone@drivenlogic.com.au
 */
public class MdnsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Logger _log = LoggerFactory.getLogger(MdnsAsyncTask.class);
    private final static String MDNS_SERVICE_TYPE = "_iotas._tcp.local.";
    private WifiManager.MulticastLock _multicastLock;
    private JmDNS _jmdns;
    private ServiceInfo[] _locatedMdnsServices;
    private WifiManager _wifiManager;
    private IScanCallbackListener _callbackListener;

    /**
     * Constructor
     * @param callbackListener callback interface for the activity
     * @param wifiManager current connected WiFi manager for the device
     */
    MdnsAsyncTask(IScanCallbackListener callbackListener, WifiManager wifiManager) {

        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler()); // TODO: Move to app class.
        _callbackListener = callbackListener;
        _wifiManager = wifiManager;
    }

    /** Notify callback that we are starting work */
    @Override
    protected void onPreExecute() {
        _callbackListener.ScanStarted("Looking for Holiday...");
        super.onPreExecute();
    }

    /**
     * Do the scan here ask jmDNS to list services in a synchronous manner
     * doInBackground() is run on a thread-pool thread
     */
    @Override
    protected Void doInBackground(Void... voids) {
            try {

                // take a multicast lock
                _multicastLock = _wifiManager.createMulticastLock("lockString");
                _multicastLock.acquire();

                //_log.info("MultiCast Lock State: " + _multicastLock.isHeld()); // This is still 'true' when disabled in firmware >:/

                String ipAddress = new NetworkInfrastructure().getLocalInetAddress().getHostAddress();
                _log.info("Local IP Address is: " + ipAddress);

                _jmdns = JmDNS.create(new NetworkInfrastructure().getLocalInetAddress(), ipAddress);
                _locatedMdnsServices = _jmdns.list(MDNS_SERVICE_TYPE); // list services.

                // release lock
                _multicastLock.release();

            } catch (IOException ex) {
                ex.printStackTrace();
                _log.error("Error in jmDNS background task", ex);
            }

        return null;
    }

    /**
     * After the task has completed do some logging and tell the callback about it
     */
    @Override
    protected void onPostExecute(Void v) {
        if (_locatedMdnsServices == null || _locatedMdnsServices.length <= 0) {

            _log.info("Did not find any holiday devices on network");
        } else {

            _log.info("Adding controls to UI ");

            for (ServiceInfo serviceInfo : _locatedMdnsServices) {
                _callbackListener.ServiceLocated(new ServiceResult(serviceInfo.getURL(),serviceInfo.getName(), ServiceResult.ScanType.JMDMS));
            }
        }
        _callbackListener.ScanCompleted();

        super.onPostExecute(v);
    }
}
