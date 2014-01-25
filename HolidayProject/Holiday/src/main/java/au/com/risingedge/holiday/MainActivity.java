/**
 * Holiday For Android - http://moorescloud.com
 *
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

/**
 * An Activity where scanning tasks are started and results are displayed
 *
 * @author andrew.stone@drivenlogic.com.au
 */
public class MainActivity extends Activity implements IMdnsCallbackListener {

    private Logger _log = LoggerFactory.getLogger(MainActivity.class);
    private ProgressDialog _progressDialog;
    private WifiManager _wiFiManager;
    private Handler _uiHandler;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        _log.debug("Main Activity started");

        // full screen for old devices
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _uiHandler = new Handler();

        RunMdnsScan();
    }

    /**
     * Run the jmDNS scan
     * Scan Threads started here
     */
    private void RunMdnsScan() {
        // check that WiFi is enabled - if not warn user and open wifi intent
        _wiFiManager = (WifiManager) this.getSystemService(android.content.Context.WIFI_SERVICE);
        if (CheckWifi()) {

            // run multi threaded scan
            new Thread(new MdnsRunnable(this, _wiFiManager, _uiHandler)).start();

            // run synchronous scan as async task
            //new MdnsAsyncTask(this,_wiFiManager).execute();

            // run the TCP port scanner
            //new TcpScanTask().execute();

            _log.debug("Scan running");
        }

        // results check for jmdns in asynchronous mode
        _uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ResultsCheck();
            }
        }, 10000); // check for results after a delay
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
    }

    /**
     * onResume()
     * Wifi is checked again here
     */
    @Override
    protected void onResume() {
        super.onResume();
        CheckWifi(); // make sure its on when they come back
    }

    /**
     * onCreateOptionsMenu()
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

    /**
     * Called buy a worker when a scan locates a service
     * Implementation detail of IMdnsCallbackListener
     *
     * @param serviceResult
     */
    @Override
    public void ServiceLocated(ServiceResult serviceResult) {
        AddHolidayControls(serviceResult);
    }

    /**
     * Called buy a worker when a scan is taking place
     * Implementation detail of IMdnsCallbackListener
     *
     * @param message the message to show in the spinner
     */
    @Override
    public void TaskBusy(String message) {
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage("Looking for Holiday...");
        _progressDialog.show();
    }

    /**
     * Called by worker when a scan is completed
     * Implementation detail of IMdnsCallbackListener
     */
    @Override
    public void TaskCompleted() {
        if (_progressDialog != null && _progressDialog.isShowing()) {
            //hide the dialog
            _progressDialog.dismiss();
        }
    }

    /**
     * Restart the Activity in a way that works with devices pre API 11
     */
    private void ActivityRestart() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
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
                WifiAlert("Please enable WiFi and connect to same network as your Holiday");
                return false;
            } else {
                return true;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            _log.error("Error getting Wifi State");
            WifiAlert("Please enable WiFi and connect to same network as your Holiday");
            return false;
        }
    }

    /**
     * See if the asynchronous scan operation has yielded any results.
     * Run on a delay
     */
    private void ResultsCheck() {
        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.verticalLinearLayout);
        _log.info("Results control count:" + linearLayout.getChildCount());

        if (linearLayout.getChildCount() <= 0) {
            _progressDialog.dismiss();
            AddNotFoundControls();
        }
    }

    /**
     * Builds an OK dialog box
     *
     * @param string text for the dialog
     */
    private void WifiAlert(String string) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(string)
                .setCancelable(false)
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
     * Builds an OK dialog box
     * Exits the application on click.
     *
     * @param string text for the dialog
     */
    private void AlertFatal(String string) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    /**
     * Add the controls for each located device
     *
     * @param serviceResult POJO containing data from a scan
     */
    private void AddHolidayControls(ServiceResult serviceResult) {
        _log.debug("Creating device button - " + serviceResult.getName());

        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.verticalLinearLayout);

        ImageView imageView = new ImageView(this);
        imageView.setOnClickListener(new HolidayClickListener(serviceResult.getIp(), this));
        imageView.setImageResource(R.drawable.device);
        linearLayout.addView(imageView);

        TextView textView = new TextView(this);
        textView.setOnClickListener(new HolidayClickListener(serviceResult.getIp(), this));
        textView.setText(serviceResult.getName());
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(Gravity.CENTER);
        linearLayout.addView(textView);
    }

    /**
     * Add the controls needed when there were no results
     */
    private void AddNotFoundControls() {
        _log.debug("Creating no results found controls ");
        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.verticalLinearLayout);

        TextView textView = new TextView(this);
        textView.setText("Holiday not found");
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(Gravity.CENTER);
        linearLayout.addView(textView);


        Button button = new Button(this);
        button.setText("Try again?");
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setGravity(Gravity.CENTER);

        // let the user rescan
        button.setOnClickListener(new
                                          View.OnClickListener() {
                                              @Override
                                              public void onClick(View view) {
                                                  ActivityRestart();
                                              }
                                          });

        linearLayout.addView(button);
    }
}

