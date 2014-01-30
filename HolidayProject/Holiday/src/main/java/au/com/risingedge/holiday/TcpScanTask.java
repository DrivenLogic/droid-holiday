/**
 * Holiday For Android - http://moorescloud.com
 *
 * */
package au.com.risingedge.holiday;

import android.os.AsyncTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Experimental alternative scanning method
 * TODO: Discuss with Mark.
 *
 * @author andrew.stone@drivenlogic.com.au
 */
public class TcpScanTask extends AsyncTask<Void, Void, Void> {

    private Logger _log = LoggerFactory.getLogger(TcpScanTask.class);
    private static final int TIMEOUT = 300; // Time to wait for the LAN
    private IMdnsCallbackListener _callbackListener;
    private ArrayList<ServiceResult> _serviceResults = new ArrayList<ServiceResult>();

    TcpScanTask(IMdnsCallbackListener callbackListener)
    {
        _callbackListener = callbackListener;
    }

    /** Notify callback that we are starting work */
    @Override
    protected void onPreExecute() {
        _callbackListener.TaskBusy("Running deep scan... this will take awhile...");
        super.onPreExecute();
    }

    /**
     * Run a TCP scan for Hollidays on the current IP range.
     * @param voids
     * @return
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RunTcpScan();
        return null;
    }

    /**
     * After the task has completed do some logging and tell the callback about it
     */
    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);

        for (ServiceResult serviceResult : _serviceResults) {
            _callbackListener.ServiceLocated(serviceResult);
        }

        _callbackListener.TaskCompleted();
    }

    /**
     * Make sure this device is on a private range
     *
     *  10.0.0.0 - 10.255.255.255
     *  172.16.0.0 - 172.31.255.255
     *  192.168.0.0 - 192.168.255.255
     *
     * @return
     */
    private boolean isPrivateIpRange(String string)
    {
        // Check range

        return  true;
    }

    /**
     * A Simple TCP port scanner
     */
    private void RunTcpScan()
    {
        String ipAddress = new NetworkInfrastructure().getLocalInetAddress().getHostAddress();
        String range = ipAddress.substring(0, ipAddress.lastIndexOf('.') + 1);

        for (int i = 1; i < 254; i++) {
            try {
                Socket socket = new Socket();
                SocketAddress address = new InetSocketAddress(range + i, 80);

                socket.connect(address, TIMEOUT);

                _log.debug("Ip: " + range + i + " seems to be open..");

                socket.close();

                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("{ \"lights\": [ ");
                    for (int x = 0; x < 50; x++) {

                        stringBuilder.append("\"#008000\"");
                        if(x<49)
                            stringBuilder.append(", ");
                    }
                    stringBuilder.append(" ] }");
                    //_log.debug(stringBuilder.toString());

                    // HACK: maybe there is a better part of the API to test... check that with Mark.
                    HttpURLConnection httpConnection = (HttpURLConnection) new URL("http://" + range + i + "/iotas/0.1/device/moorescloud.holiday/localhost/setlights").openConnection();
                    httpConnection.setDoOutput(true);
                    httpConnection.setRequestMethod("PUT");
                    httpConnection.addRequestProperty("Content-Type", "application/json");
                    OutputStreamWriter out = new OutputStreamWriter(httpConnection.getOutputStream());
                    out.write(stringBuilder.toString());
                    out.flush();
                    out.close();

                    int status = httpConnection.getResponseCode();

                    // TODO: Check holiday response JSON

                    _log.debug("Http result status: " + status);

                    if(status == 200)
                    {
                        _log.info("Found a Holiday at:" + range + i);
                        _serviceResults.add(new ServiceResult("http://" + range + i,"Holiday @ " + range + i)); // hostname resolution probably not reliable
                    }

                } catch (IOException e) {
                    // not a holiday!
                }
            } catch (UnknownHostException e) {
                // bad address
            } catch (SocketTimeoutException e) {
                // socket timeout
            } catch (IOException e) {
                // socket closed
            }
        }
        _log.info("Holiday TCP Scan Complete...");
    }
}
