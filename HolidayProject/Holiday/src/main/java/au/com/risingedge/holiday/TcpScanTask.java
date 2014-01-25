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

/**
 * Experimental alternative scanning method
 * TODO: Discuss with Mark.
 *
 * @author andrew.stone@drivenlogic.com.au
 */
public class TcpScanTask extends AsyncTask<Void, Void, Void> {

    private Logger _log = LoggerFactory.getLogger(TcpScanTask.class);
    private static final int TIMEOUT = 300;

    @Override
    protected Void doInBackground(Void... voids) {

        String ipAddress = new NetworkInfrastructure().getLocalInetAddress().getHostAddress();
        String range = ipAddress.substring(0, ipAddress.lastIndexOf('.') + 1);

        for (int i = 1; i < 254; i++) {
            try {
                Socket socket = new Socket();
                SocketAddress address = new InetSocketAddress(range + i, 80);

                socket.connect(address, TIMEOUT);

                _log.info("Ip: " + range + i + " seems to be open..");

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

                    _log.info(stringBuilder.toString());

                    // HACK: perhaps a bit suspect... maybe there is a better part of the API to test... check that this is OK with Mark.
                    HttpURLConnection httpConnection = (HttpURLConnection) new URL("http://" + range + i + "/iotas/0.1/device/moorescloud.holiday/localhost/setlights").openConnection();
                    httpConnection.setDoOutput(true);
                    httpConnection.setRequestMethod("PUT");
                    httpConnection.addRequestProperty("Content-Type", "application/json");
                    OutputStreamWriter out = new OutputStreamWriter(httpConnection.getOutputStream());
                    out.write(stringBuilder.toString());
                    out.flush();
                    out.close();

                    int status = httpConnection.getResponseCode();
                    _log.info("Http result status: " + status);
                    //InputStream response = connection.getInputStream();
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
        return null;
    }
}
