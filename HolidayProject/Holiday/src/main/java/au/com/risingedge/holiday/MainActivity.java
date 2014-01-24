package au.com.risingedge.holiday;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements IMdnsCallbackListener {

    private static final String TAG = "holidayMainActivity";
    private ProgressDialog _progressDialog;
    private WifiManager _wiFiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // catch unhandled Exceptions
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        // go full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check that WiFi is enabled - warn user and open wifi intent
        _wiFiManager = (WifiManager) this.getSystemService(android.content.Context.WIFI_SERVICE);
        if (CheckWifi()) {

            // run scan as a task
            //new MdnsAsyncTask(this,_wiFiManager).execute();

            // run multi threaded scan
            new Thread(new MdnsRunnable(this, _wiFiManager, new Handler())).start();

            // run the port scanner
            //new TcpScanTask().execute();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO: rescan may be needed?
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckWifi();
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

//           case R.id.action_sendLogs:
//                // ship logs
//                new LogShipperAsyncTask(this).execute();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void ServiceLocated(ServiceResult serviceResult) {
        AddHolidayControls(serviceResult);
    }

    @Override
    public void TaskBusy(String message) {
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage("Looking for Holiday...");
        _progressDialog.show();
    }

    @Override
    public void TaskCompleted() {
        if (_progressDialog != null && _progressDialog.isShowing()) {
            //hide the dialog
            _progressDialog.dismiss();
        }
    }

    private boolean CheckWifi() {
        // check that WiFi is enabled - warn user and open wifi intent
        if (!_wiFiManager.isWifiEnabled()) {
            Log.w(TAG, "WiFi is off! - Can't scan - user needs to enable WiFi - promoting user");
            Alert("Please enable WiFi");

            // open WiFi settings
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            return false;
        } else {
            return true;
        }
    }

    ///
    /// builds an OK dialog box
    ///
    public void Alert(String string) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(string)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    ///
    /// builds an OK dialog box
    /// Exits the application on click.
    ///
    public void AlertFatal(String string) {
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

    ///
    /// add the controls for each located device
    ///
    public void AddHolidayControls(ServiceResult serviceResult) {
        Log.i(TAG, "Creating device button - " + serviceResult.getName());
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

    ///
    /// add the controls needed when there were no results
    ///
    public void AddNotFoundControls(Activity activity) {
        Log.i(TAG, "Creating no results found controls ");

        LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.verticalLinearLayout);

        TextView textView = new TextView(activity);
        textView.setText("Holiday not found");
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(Gravity.CENTER);
        linearLayout.addView(textView);
    }
}

