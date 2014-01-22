package au.com.risingedge.holiday;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import static au.com.risingedge.holiday.Helpers.AlertFatal;

public class MainActivity extends Activity {

    private static final String TAG = "holidayMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // catch unhandled Exceptions
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        // go full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check that WiFi is enabled - warn user and quit if it is not
        WifiManager wifiManager = (WifiManager) this.getSystemService(android.content.Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Log.w(TAG, "WiFi is off! - Can't scan - user needs to enable WiFi");
            AlertFatal("Please enable WiFi", this);

        } else {
            // run scan task
            new MdnsAsyncTask(this).execute();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                // Holiday help URL
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://support.moorescloud.com/help/android/"));
                this.startActivity(i);
                return true;

            case R.id.action_sendLogs:
                // ship logs
                new LogShipperAsyncTask(this).execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

