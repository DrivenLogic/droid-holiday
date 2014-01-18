package au.com.risingedge.holiday;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.content.DialogInterface;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

///
/// Class for helper methods that don't fit with types.
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

    ///
    /// builds an OK dialog box and shows it in the given context
    ///
    public static void Alert(String string, Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(string)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
