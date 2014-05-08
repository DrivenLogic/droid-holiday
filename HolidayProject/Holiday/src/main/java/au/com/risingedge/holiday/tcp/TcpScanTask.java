/**
 * Holiday For Android - http://moorescloud.com
 *
 * */
package au.com.risingedge.holiday;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * When all else fails use a simple port scan.
 *
 * @author andrew.stone@drivenlogic.com.au
 */
public class TcpScanTask extends AsyncTask<Void, Void, Void> {

    private Logger _log = LoggerFactory.getLogger(TcpScanTask.class);
    private static final int TIMEOUT = 300; // Should be plenty for a lan.
    private IScanCallbackListener _callbackListener;
    private ArrayList<ServiceResult> _serviceResults = new ArrayList<ServiceResult>();

    TcpScanTask(IScanCallbackListener callbackListener)
    {
        _callbackListener = callbackListener;
    }

    /** Notify callback that we are starting work */
    @Override
    protected void onPreExecute() {
        _callbackListener.ScanStarted("Running a deep scan... this will take awhile...");
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

        _callbackListener.ScanCompleted();
    }

    /**
     * A Simple TCP port scanner
     *
     * TODO: Split up the range and process with extra threads
     */
    private void RunTcpScan()
    {
        String ipAddress = new NetworkInfrastructure().getLocalInetAddress().getHostAddress();

        //make sure we are on a private range...
        if(isPrivateIpRange(ipAddress))
        {
            // TODO: Check the sub-net mask for smaller than /24 to reduce possible hosts

            String range = ipAddress.substring(0, ipAddress.lastIndexOf('.') + 1);

            for (int i = 1; i < 255; i++) {
                try {

                    // See if port is open - if it's not, an exception will bubble
                    probeTcpPort(range + i, 80);

                    // still here? its open lets see if IOTAS is on the host...
                    probeIotasApi(range + i);

                }catch (JSONException e) {
                    //  Cant get JSON api probe failed
                } catch (UnknownHostException e) {
                    // bad address - ignore
                } catch (SocketTimeoutException e) {
                    // socket timeout - ignore
                } catch (IOException e) {
                    // socket closed - ignore
                }
            }
        }

        _log.info("Holiday TCP Scan Complete...");
    }

    /**
     * Do a really simple TCP port probe
     * Test a by issuing a TCP connect.
     *
     * @param ip
     * @param port
     * @throws IOException
     */
    private void probeTcpPort(String ip, int port) throws IOException
    {
        Socket socket = new Socket();
        SocketAddress address = new InetSocketAddress(ip, port);

        socket.connect(address, TIMEOUT);

        _log.debug("Ip: " + ip + " appears to be open... on port: " + port);

        socket.close();

        // let exceptions bubble
    }

    /**
     * A method to go and probe the IOTAS API
     * Reads the JSON response from the root of the IOTAS API
     *
     * This is necessary as some embedded web servers return 200's on any URL
     *
     * @param ip
     * @throws JSONException
     */
    private void probeIotasApi(String ip) throws JSONException, IOException {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpEntity httpEntity = null;
        HttpResponse httpResponse = null;

        HttpGet httpGet = new HttpGet("http://" + ip + "/iotas");
        httpResponse = httpClient.execute(httpGet);

        int httpStatus = httpResponse.getStatusLine().getStatusCode();

        if(httpStatus == 200)
        {
            httpEntity = httpResponse.getEntity();
            String jsonResult = EntityUtils.toString(httpEntity);

            if(jsonResult!=null)
            {
                _log.debug("json result: " + jsonResult);

                JSONObject jsonObject = new JSONObject(jsonResult);

                String hostName = jsonObject.getString("host_name");
                String apiVersion = jsonObject.getString("version");

                _log.debug("Found Holiday " +hostName+ " with IOTAS API version: " + apiVersion);
                _serviceResults.add(new ServiceResult("http://" + ip, hostName, ServiceResult.ScanType.TCP_SCAN));
            }
        }

        // let exceptions bubble
    }

    /**
     * Make sure this device is on a private range (192.* , 172.* and 10.*)
     */
    private boolean isPrivateIpRange(String ipAddress)
    {
        InetAddress inetAddress = null;

        try {
            byte[] ip = InetAddress.getByName(ipAddress).getAddress();
            inetAddress = InetAddress.getByAddress(ip);
            return inetAddress.isSiteLocalAddress(); // let java sort it out.
        } catch (UnknownHostException e) {
            _log.debug("Ip private range check failed ", e);
            return false;
        }
    }
}