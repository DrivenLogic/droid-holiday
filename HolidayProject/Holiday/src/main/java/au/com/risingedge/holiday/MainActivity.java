package au.com.risingedge.holiday;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;

import static au.com.risingedge.holiday.Helpers.Alert;

public class MainActivity extends Activity {

    private static final String TAG = "holidayMainActivity";

    WifiManager.MulticastLock _multicastLock;
    private Handler _mdnsListenerhandler = new Handler();
    private Handler _uiHandler;
    private String _mdnsServiceType = "_iotas._tcp.local."; // _service._protocol.local. // TODO: move to settings.
    private JmDNS _jmdns = null;
    private Context _context;
    private ServiceListener _listener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // catch unhandled Exceptions
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        // go full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        _uiHandler = new Handler(); // UI thread
        _context = this; // REVIEW: is there a better way to do this? I need to pass the context to a button constructor.

        // Get WiFi manager and enable multicast
        android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager)getSystemService(android.content.Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()){
            Log.w(TAG,"WiFi is off! - Can't scan - please enable WiFi");
            Alert("WiFi is off! - Can't scan - please enable WiFi", _context);
            // TODO: show user a dialog
        }
        else
        {
            // Take multicast lock
            _multicastLock = wifiManager.createMulticastLock("multiCastLock");
            _multicastLock.setReferenceCounted(false);
            _multicastLock.acquire();

            // Start scan
            StartScan(this);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    ///
    /// Start the mdns scan
    ///
    void StartScan(final MainActivity mainActivity) // REVIEW: is there a better pattern than passing a final activity to a runnable?
    {
        _mdnsListenerhandler.postDelayed(
        new Runnable() {
            public void run() {
                try
                {
                    String ipAddress = Helpers.getLocalInetAddress().getHostAddress();
                    Log.i(TAG,"Local IP Address is: " + ipAddress);

                    _jmdns = JmDNS.create(Helpers.getLocalInetAddress(),ipAddress);
                    _jmdns.addServiceListener(_mdnsServiceType, new MdnsListener(_jmdns,mainActivity));

                    Log.i(TAG,"jmDNS configured...");
                }
                catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"Error on jmDNS setup",e);
                    Alert("Error on jmDNS setup " + e.getMessage(), _context);
                    return;
                }
            }},500);
    }

    ///
    /// Called for each holiday found - adds a launcher to the menu
    ///
    void notifyUser(final String msg, final String url) {
        _uiHandler.post(new Runnable() {
            public void run() {

                Log.i(TAG, "Creating button - " + msg);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.verticalLinearLayout);
                //  LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                // button for each holiday located
                Button button = new Button(_context);
                button.setOnClickListener(new HolidayClickListener(url));
                button.setText(msg);

                //  horizontalLinearLayout.addView(button, layoutParams);
                linearLayout.addView(button);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //new Thread(){public void run() {setUp();}}.start();
    }

    ///
    /// Clean up on stop - multicast chews battery.
    ///
    @Override
    protected void onStop() {
        _multicastLock.release(); // release multicast lock.
        if (_jmdns != null) {
            if (_listener != null) {
                _jmdns.removeServiceListener(_mdnsServiceType, _listener);
                _listener = null;
            }
            _jmdns.unregisterAllServices();
            try {
                _jmdns.close();
            } catch (IOException e) {

                e.printStackTrace();
                Log.e(TAG,"Error on stopping",e);
                Alert("Error on stop " + e.getMessage(), _context);
            }
            _jmdns = null;
        }

        super.onStop();
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

    ///
    /// A click handler class that passes the URL of the given Holiday
    ///
    public class HolidayClickListener implements View.OnClickListener
    {
        String url;
        public HolidayClickListener(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View v)
        {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }
}

