/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import au.com.risingedge.holiday.Services.HolidayScanServiceConnection;
import au.com.risingedge.holiday.Services.IHolidayScanServiceConnectListener;
import au.com.risingedge.holiday.Services.IHolidayScanner;
import au.com.risingedge.holiday.Services.IHolidayScannerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An Activity where scanning tasks are started and results are displayed
 *
 * @author andrew.stone@drivenlogic.com.au
 */
public class MainActivity extends Activity implements IHolidayScanServiceConnectListener, IHolidayScannerListener {

    private static final int NO_RESULTS_VIEW_ID = 1024;
    private static final long SCAN_TIMEOUT_MILLIS = 10000;
    private enum ViewState {UNINITIALIZED, MDNS_SCAN_IN_PROGRESS, TCP_SCAN_IN_PROGRESS, NO_CONTROLS_FOUND, CONTROLS_FOUND}
    private ViewState viewState = ViewState.UNINITIALIZED;

    private Logger log = LoggerFactory.getLogger(MainActivity.class);
    private IHolidayScanner holidayScanner;

    private HolidayScanServiceConnection scannerServiceConnection;
    private ProgressDialog progressDialog;

    private LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.debug("Main Activity started");

        // full screen for old devices
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = (LinearLayout) this.findViewById(R.id.verticalLinearLayout);

        scannerServiceConnection = new HolidayScanServiceConnection(this, this);
        scannerServiceConnection.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        holidayScanner.stopMdnsSearch();
        holidayScanner.unregisterListener(this);
        scannerServiceConnection.disconnect();
    }

    /**
     * onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * onStop()
     */
    @Override
    protected void onStop() {
        super.onStop();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * onResume()
     * Wifi is checked again here
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkWifi();
    }

    /**
     * onCreateOptionsMenu()
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * onOptionsItemSelected - Menu events
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_help:
                // Holiday help URL
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getResources().getString(R.string.help_url)));
                this.startActivity(i);
                return true;

            case R.id.action_sendLogs:
                // ship logs
                new LogShipperAsyncTask(this).execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called buy a worker when a scan is taking place
     * Implementation detail of IHolidayScannerListener
     *
     * @param message the message to show in the spinner
     */
    @Override
    public void onScanStart(final String message) {
        final Context context = this;

        // make sure we run all callbacks on the ui thread
        runOnUiThread(new Runnable() {
            @Override public void run() {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage(message);
                progressDialog.show();
            }
        });

    }


    /**
     * Called by worker when a scan is completed
     * Implementation detail of IHolidayScannerListener
     *
     * @param serviceResults
     */
    @Override
    public void onScanResults(final ServiceResults serviceResults) {
        log.debug("onScanResults: size= " + serviceResults.size());

        runOnUiThread(new Runnable() {
            @Override public void run() {
                hideProgressDialog();

                if (serviceResults.size() <= 0) {
                    showNoResultsControls();
                    return;
                }

                // bind the GUI to the results.
                bindHolidayControls(serviceResults);
            }
        });
    }

    /**
     * Run the jmDNS scan
     * Scan Threads started here
     */
    private void beginHolidaySearch() {

        if (batteryIsLow()) {
            // warn that batter is low and scanning may be affected
            showDialogBatteryAlert(getResources().getString(R.string.battery_low_warning));
        }

        // check that WiFi is enabled - if not warn user and open wif
        if (!checkWifi()) {
            return;
        }

        holidayScanner.beginMdnsSearch();
        viewState = ViewState.MDNS_SCAN_IN_PROGRESS;

        startSearchTimeout();
    }

    private void startSearchTimeout() {

        // JMDNS has some quirks that need to be worked around.
        // an an interval check for a lack of results.
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // if the service results collection is empty and it's been long enough...
                if (viewState == ViewState.MDNS_SCAN_IN_PROGRESS) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            hideProgressDialog();
                            showNoResultsControls();
                        }
                    });
                }
            }
        }, SCAN_TIMEOUT_MILLIS);
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            //hide the dialog
            progressDialog.dismiss();
        }
    }

    /**
     * Restart the Activity in a way that works with devices pre API 11
     */
    private void activityRestart() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /** Start a TCP scan to look for Holidays */
    private void startTcpScan(){
        log.info("Starting TCP scan...");
        removeNoResultsControls();
        holidayScanner.beginTcpScan();
        viewState = ViewState.TCP_SCAN_IN_PROGRESS;
    }

    /**
     * Check that Wifi is enabled.
     * <p/>
     * Check that WiFi is enabled AND connected - warn user and open wifi intent
     *
     * @return a bool representing the state of the wifi
     */
    private boolean checkWifi() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        try {
            if (!wifi.isConnected()) {
                log.info("WiFi is off! - Can't scan - user needs to enable WiFi - promoting user");
                showDialogWifiAlert("Please enable WiFi and connect to same network as your Holiday");
                return false;
            }
            else {
                return true;
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            log.error("Error getting Wifi State");
            showDialogWifiAlert("Please enable WiFi and connect to same network as your Holiday");
            return false;
        }
    }

    /**
     * Wifi Alert Box
     *
     * @param string text for the dialog
     */
    private void showDialogWifiAlert(String string) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(string)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // open WiFi settings
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * TCP Scan Alert Box
     */
    private void showDialogTcpScanWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.tcp_scan_warning)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startTcpScan();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Battery Low Alert Box
     *
     * @param string text for the dialog
     */
    private void showDialogBatteryAlert(String string) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(string)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // don't do anything
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Add the controls for each located device.
     * Binds the UI to the service results collection
     *
     * @param serviceResults
     */
    private void bindHolidayControls(ServiceResults serviceResults) {
        log.debug("Binding UI controls");

        // Rebind the results list to the UI
        linearLayout.removeAllViews();
        linearLayout.invalidate();

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (ServiceResult serviceResult : serviceResults.getResults()) {
            View control = inflater.inflate(R.layout.holiday_control, linearLayout, false);
            control.setOnClickListener(new HolidayClickListener(serviceResult.getLocation(), this, serviceResult.getScanType()));

            TextView textView = (TextView) control.findViewById(R.id.holiday_name);
            textView.setText(serviceResult.getName());

            linearLayout.addView(control);
        }
        viewState = ViewState.CONTROLS_FOUND;
    }

    /**
     * Add the controls needed when there were no results
     */
    private void showNoResultsControls() {

        // Check to see if the no results controls are already present.
        if (viewState != ViewState.NO_CONTROLS_FOUND) {
            log.debug("Creating no results found controls ");

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View control = inflater.inflate(R.layout.no_controls, linearLayout, false);
            Button deepScanButton = (Button) control.findViewById(R.id.deep_scan_button);
            deepScanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    linearLayout.removeAllViews();
                    showDialogTcpScanWarning();
                }
            });

            linearLayout.addView(control);

            viewState = ViewState.NO_CONTROLS_FOUND;
        }
        else {
            log.debug("no results control already displayed... skiping add");
        }
    }

    /** Removes the no results view elements by ID : API Level <= 17 */
    private void removeNoResultsControls() {
        log.debug("Removing no results found controls ");

        linearLayout.removeView(this.findViewById(R.id.no_results_view)); // remove the not found view container
        linearLayout.invalidate();
    }

    /**
     * Some devices have a "power saving mode" (Samsung) which can disable features e.g. MDNS.
     * In order to avoid this we warn the user if battery is lower than 35%
     *
     * @return true if battery charge percentage is lower than 35%
     */
    private boolean batteryIsLow() {

        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        float batteryPercent = 0;

        try {

            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPercent = level / (float) scale;

        }
        catch (Throwable ex) {
            log.error("Could not get battery state?", ex);
            return false;
        }

        // this is a best guess as the actual setting is user configurable.
        if (batteryPercent <= 0.35) {
            log.debug("Battery level is: " + batteryPercent + "%");
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void onServiceConnected(IHolidayScanner scanner) {
        holidayScanner = scanner;
        holidayScanner.registerListener(this);

        beginHolidaySearch();
    }
}

