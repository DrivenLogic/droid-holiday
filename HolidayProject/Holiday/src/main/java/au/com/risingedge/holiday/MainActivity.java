package au.com.risingedge.holiday;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends Activity {

    private static final String TAG = "holidayMainActivity";

    android.net.wifi.WifiManager.MulticastLock lock;
    android.os.Handler handler = new android.os.Handler();

    LinearLayout horizontalLinearLayout;

    private Context activityContext;

    //private String mdnsType = "_iotas._tcp.local."; // _service._protocol.local.
    private String mdnsType = "_sleep-proxy._udp.local.";
    private JmDNS jmdns = null;
    private ServiceListener listener = null;
    private ServiceInfo serviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // catch unhandled Exceptions
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        activityContext = this;

        // full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler.postDelayed(new Runnable() {
            public void run() {
                try
                {
                    android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager)getSystemService(android.content.Context.WIFI_SERVICE);
                    lock = wifiManager.createMulticastLock("multiCastLock");
                    lock.setReferenceCounted(true);
                    lock.acquire();

                    Logger logger = Logger.getLogger(JmDNS.class.getName());
                    ConsoleHandler handler = new ConsoleHandler();
                    logger.addHandler(handler);
                    logger.setLevel(Level.FINER);
                    handler.setLevel(Level.FINER);

                    String ipAddress = getLocalInetAddress().getHostAddress();

                    Log.i(TAG,"Local IP Address is: " + ipAddress);

                    final JmDNS jmdns = JmDNS.create(getLocalInetAddress(),ipAddress);
                    jmdns.addServiceListener("_iotas._tcp.local.", new SampleListener());

                    Log.i(TAG,"jmDNS configured.");

                    ServiceInfo[] serviceInfos = jmdns.list("_iotas._tcp.local.");

                    for(int i =0; i < serviceInfos.length; i++)
                    {
                        Log.i(TAG,serviceInfos[i].getQualifiedName());
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"Error on jmDNS setup",e);
                    return;
                }
            }
        }, 1000);

        //Button myButton = new Button(this);
        //myButton.setText("Push Me");

        //horizontalLinearLayout = (LinearLayout)findViewById(R.id.horizontalLinearLayout);
        //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //horizontalLinearLayout.addView(myButton, lp);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUp() {

        android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager)getSystemService(android.content.Context.WIFI_SERVICE);
        lock = wifiManager.createMulticastLock("mylockthereturn");
        lock.setReferenceCounted(true);
        lock.acquire();

        try {

            jmdns = JmDNS.create(wifiIpAddress(activityContext));
            jmdns.addServiceListener(mdnsType, listener = new ServiceListener() {

                @Override
                public void serviceResolved(ServiceEvent ev) {
                    String additions = "";
                    if (ev.getInfo().getInetAddresses() != null && ev.getInfo().getInetAddresses().length > 0) {
                        additions = ev.getInfo().getInetAddresses()[0].getHostAddress();
                    }
                    Log.w(TAG, "Service resolved:" + ev.getInfo().getQualifiedName() + " port:" + ev.getInfo().getPort() + additions);
                    notifyUser("Service resolved: " + ev.getInfo().getQualifiedName() + " port:" + ev.getInfo().getPort() + additions);
                }

                @Override
                public void serviceRemoved(ServiceEvent ev) {
                    notifyUser("Service removed: " + ev.getName());
                }

                @Override
                public void serviceAdded(ServiceEvent event) {
                    // Required to force serviceResolved to be called again (after the first search)
                    jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                }
            });

        } catch (IOException e) {
            Log.e(TAG,"Error setring up jmDNS",e);
            e.printStackTrace();
            return;
        }
    }


    private void notifyUser(final String msg) {
        handler.postDelayed(new Runnable() {
            public void run() {

                Button myButton = new Button(activityContext);
                myButton.setText(msg);

                horizontalLinearLayout = (LinearLayout)findViewById(R.id.horizontalLinearLayout);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                horizontalLinearLayout.addView(myButton, lp);
            }
        }, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //new Thread(){public void run() {setUp();}}.start();
    }

    @Override
    protected void onStop() {
        if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(mdnsType, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e(TAG,"Error on stopping",e);
            }
            jmdns = null;
        }
        //repo.stop();
        //s.stop();
        lock.release();
        super.onStop();
    }

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
            Log.e(TAG,"Error when looking up IP address",ex);
        }
        return null;
    }

    protected String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.w(TAG, "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }

    ///
    /// Implemention of jmDNS listner class.
    ///
    class SampleListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            Log.i(TAG,"Service added   : " + event.getName() + "." + event.getType());
            jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            Log.i(TAG, "Service removed : " + event.getName() + "." + event.getType());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            Log.i(TAG, "Service resolved: " + event.getInfo());

            String additions = "";
            if (event.getInfo().getInetAddresses() != null && event.getInfo().getInetAddresses().length > 0) {
                additions = event.getInfo().getInetAddresses()[0].getHostAddress();
            }

            notifyUser("Service resolved: " + event.getInfo().getQualifiedName() + " port:" + event.getInfo().getPort() + additions);
        }
    }
}

