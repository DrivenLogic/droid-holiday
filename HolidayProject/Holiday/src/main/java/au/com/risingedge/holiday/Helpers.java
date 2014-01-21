package au.com.risingedge.holiday;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Handler;

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
                        System.exit(1); // this dialog assumes that for it to be shown it a result of a non-recoverable situation.
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    ///
    /// Helper to add the controls for each located device
    ///
    public static void AddHolidayControls(Activity activity, String deviceHostname, String url)
    {
        Log.i(TAG, "Creating device button - " + deviceHostname);
        LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.verticalLinearLayout);

        ImageView imageView = new ImageView(activity);
        imageView.setOnClickListener(new HolidayClickListener(url, activity));
        imageView.setImageResource(R.drawable.device);
        linearLayout.addView(imageView);

        TextView textView = new TextView(activity);
        textView.setOnClickListener(new HolidayClickListener(url, activity));
        textView.setText(deviceHostname);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(Gravity.CENTER);
        linearLayout.addView(textView);
    }

    ///
    /// Helper to add the controls for each located device
    ///
    public static void AddNotFoundControls(Activity activity)
    {
        Log.i(TAG, "Creating no results controls ");

        LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.verticalLinearLayout);

        TextView textView = new TextView(activity);
        textView.setText("Holiday not found");
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(Gravity.CENTER);
        linearLayout.addView(textView);
    }
}
