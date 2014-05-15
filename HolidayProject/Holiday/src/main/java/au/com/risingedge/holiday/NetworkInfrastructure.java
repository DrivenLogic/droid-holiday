/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Query the underling network devices on the device
 *
 * @author andrew.stone@drivenlogic.com.au
 */
public class NetworkInfrastructure {

    private Logger log = LoggerFactory.getLogger(NetworkInfrastructure.class);

    /**
     * Get InetAddress
     *
     * @return The InetAddress object containing adapter information
     */
    public InetAddress getLocalInetAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }

                    // TODO: Future - add ipV6 support.
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
            log.error("Error when looking up IP address", ex);
        }
        return null;
    }
}
