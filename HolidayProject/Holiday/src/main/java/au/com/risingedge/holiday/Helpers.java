package au.com.risingedge.holiday;
import android.util.Log;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

///
/// helper methods that don't fit with types.
///
public class Helpers {

    private static final String TAG = "holidayHelpers";

    ///
    /// Get local IP Address.
    //
    public static InetAddress getLocalInetAddress() {
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
            Log.e(TAG, "Error when looking up IP address", ex);
        }
        return null;
    }
}
