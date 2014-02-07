/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * An Activity where scanning tasks are started and results are displayed
 *
 * @author andrew.stone@drivenlogic.com.au
 */
public class MainActivity extends Activity implements IScanCallbackListener {

    private Logger _log = LoggerFactory.getLogger(MainActivity.class);
    private ProgressDialog _progressDialog;
    private WifiManager _wiFiManager;
    private WifiManager.MulticastLock _multicastLock;
    private Handler _uiHandler;
    private ArrayList<ServiceResult> _serviceResults = new ArrayList<ServiceResult>();
    private static final int NO_RESULTS_VIEW_ID = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        _log.debug("Main Activity started");

        // full screen for old devices
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _uiHandler = new Handler();

        RunScan();
    }

    /**
     * Run the jmDNS scan
     * Scan Threads started here
     */
    private void RunScan() {
        // check that WiFi is enabled - if not warn user and open wifi intent
        _wiFiManager = (WifiManager) this.getSystemService(android.content.Context.WIFI_SERVICE);

        if (CheckWifi()) {

            // take a multicast lock
            _multicastLock = _wiFiManager.createMulticastLock("lockString");
            _multicastLock.acquire();

            new Thread(new MdnsRunnable(this, _wiFiManager, _uiHandler)).start();

            _log.debug("Scan running");

            // results check for jmdns in asynchronous mode
            _uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ResultsCheck();
                }
            }, 12000); // check for results after a delay - NB: old slow devices need a bit of time.
        }
    }

    /** onStart() */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /** onStop() */
    @Override
    protected void onStop() {
        super.onStop();
        if((_multicastLock!=null)&&(_multicastLock.isHeld()))
        {
            _multicastLock.release();
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * onResume()
     * Wifi is checked again here
     */
    @Override
    protected void onResume() {
        super.onResume();
        CheckWifi();
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

    /** onOptionsItemSelected - Menu events */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_rescan:
                ActivityRestart();
                return true;

            case R.id.action_help:
                // Holiday help URL
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getResources().getString(R.string.tcp_scan_warning)));
                this.startActivity(i);
                return true;

//            case R.id.action_sendLogs:
//                // ship logs
//                new LogShipperAsyncTask(this).execute();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called buy a worker when a scan locates a service
     * Implementation detail of IScanCallbackListener
     *
     * @param serviceResult
     */
    @Override
    public void ServiceLocated(ServiceResult serviceResult) {
        _serviceResults.add(serviceResult);
        BindHolidayControls();
    }

    /**
     * Called buy a worker when a scan is taking place
     * Implementation detail of IScanCallbackListener
     *
     * @param message the message to show in the spinner
     */
    @Override
    public void ScanStarted(String message) {
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(message);
        _progressDialog.show();

        // A scan is starting flush results
        _serviceResults.clear();
    }

    /**
     * Called by worker when a scan is completed
     * Implementation detail of IScanCallbackListener
     */
    @Override
    public void ScanCompleted() {
        if (_progressDialog != null && _progressDialog.isShowing()) {
            //hide the dialog
            _progressDialog.dismiss();
        }

        ResultsCheck();
    }

    /** Restart the Activity in a way that works with devices pre API 11 */
    private void ActivityRestart() {
        _serviceResults.clear();
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /** Start a TCP scan to look for Holidays */
    private void StartTcpScan(){
        _log.info("Starting TCP scan...");
        RemoveNoResultsControls();
        new TcpScanTask(this).execute();
    }

    /**
     * Check that Wifi is enabled.
     * <p/>
     * Check that WiFi is enabled AND connected - warn user and open wifi intent
     *
     * @return a bool representing the state of the wifi
     */
    private boolean CheckWifi() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        try {
            if (!wifi.isConnected()) {
                _log.info("WiFi is off! - Can't scan - user needs to enable WiFi - promoting user");
                ShowDialogWifiAlert("Please enable WiFi and connect to same network as your Holiday");
                return false;
            } else {
                return true;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            _log.error("Error getting Wifi State");
            ShowDialogWifiAlert("Please enable WiFi and connect to same network as your Holiday");
            return false;
        }
    }

    /**
     * Wifi Alert Box
     *
     * @param string text for the dialog
     */
    private void ShowDialogWifiAlert(String string) {
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
     *
     * @param string text for the dialog
     */
    private void ShowDialogTcpScanWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.tcp_scan_warning)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        StartTcpScan();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /** See if the asynchronous scan operation has yielded any results */
    private void ResultsCheck() {

        _log.info("Results Check, Count: " + _serviceResults.size());
        if (_serviceResults.size() <= 0) {
            _progressDialog.dismiss();
            AddNoResultsControls();
        }
    }

    /**
     * Add the controls for each located device.
     * Binds the UI to the service results collection
     *
     * @param serviceResult POJO containing data from a scan
     */
    private void BindHolidayControls() {
        _log.debug("Binding UI controls");

        // Rebind the results list to the UI

        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.verticalLinearLayout);
        linearLayout.removeAllViews();
        linearLayout.invalidate();

        for(ServiceResult serviceResult : _serviceResults)
        {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.device);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setOnClickListener(new HolidayClickListener(serviceResult.get_location(), this,serviceResult.getScanType()));
            linearLayout.addView(imageView);

            TextView textView = new TextView(this);
            textView.setOnClickListener(new HolidayClickListener(serviceResult.get_location(), this,serviceResult.getScanType()));
            textView.setText(serviceResult.getName());
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setGravity(Gravity.CENTER);
            linearLayout.addView(textView);
        }
    }

    /**
     * Add the controls needed when there were no results
     */
    private void AddNoResultsControls() {
        _log.debug("Creating no results found controls ");
        final LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.verticalLinearLayout);

        // we need somewhere to stash the controls so they can be removed as a group
        LinearLayout NoResultslinearLayout = new LinearLayout(this);
        NoResultslinearLayout.setOrientation(LinearLayout.VERTICAL);
        NoResultslinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        NoResultslinearLayout.setId(NO_RESULTS_VIEW_ID); // API Level <= 17

        TextView textView = new TextView(this);
        textView.setText("Holiday not found");
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(Gravity.CENTER);
        NoResultslinearLayout.addView(textView);

        Button button = new Button(this);
        button.setText("Try a deep scan?");
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setGravity(Gravity.CENTER);

        // let the user run a TCP Scan
        button.setOnClickListener(new
                                          View.OnClickListener() {
                                              @Override
                                              public void onClick(View view) {
                                                  linearLayout.removeAllViews();
                                                  ShowDialogTcpScanWarning();
                                              }
                                          });

        NoResultslinearLayout.addView(button);

        // now add to the parent
        linearLayout.addView(NoResultslinearLayout);
    }

    /** Removes the no results view elements by ID : API Level <= 17 */
    private void RemoveNoResultsControls() {
        _log.debug("Removing no results found controls ");

        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.verticalLinearLayout);
        linearLayout.removeView(this.findViewById(NO_RESULTS_VIEW_ID)); // remove the not found view container
        linearLayout.invalidate();
    }
}

