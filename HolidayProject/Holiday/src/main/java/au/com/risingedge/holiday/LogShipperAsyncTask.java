/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Reads and ships logs for remote diagnostics
 *
 *  @author andrew.stone@drivenlogic.com.au
 */
public class LogShipperAsyncTask extends AsyncTask<Void, Void, String> {

    private Logger _log = LoggerFactory.getLogger(LogShipperAsyncTask.class);
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String[] SEND_TO_MAILBOXES = new String[]{"andrew.stone@drivenlogic.com.au", "mpesce@gmail.com"};
    private final static String LOG_FILENAME = "Holiday.log";
    private Context _context;
    private String _reportTitle = "";

    /**
     * Constructor
     * @param activity
     */
    LogShipperAsyncTask(Activity activity) {
        _context = activity;
    }

    /**
     * Read logs from external storage and get environmental information.
     * @param voids
     * @return
     */
    @Override
    protected String doInBackground(Void... voids) {

        StringBuilder stringBuilder = new StringBuilder().append(LINE_SEPARATOR);

        GetEnvironmentDetails(stringBuilder);
        ReadSdLog(stringBuilder);

        _log.debug("Logs collected, log length is: " + stringBuilder.length());
        return stringBuilder.toString();
    }

    /**
     * Send log via email intent
     * @param result
     */
    @Override
    protected void onPostExecute(String result) {
        SendLogViaEmail(result);
    }

    /**
     * Read our log from the users SD card
     * @param stringBuilder
     * @return
     */
    private void ReadSdLog(StringBuilder stringBuilder)
    {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard,LOG_FILENAME);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
        }
        catch (IOException e) {
            _log.error(String.format("collectAndSendLog failed"), e);
        }
    }

    /**
     * Launch an email intent.
     * @param logText text for the body of the email
     */
    private void SendLogViaEmail(String logText) {

        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("message/rfc822");
        email.putExtra(Intent.EXTRA_EMAIL, SEND_TO_MAILBOXES);
        email.putExtra(Intent.EXTRA_SUBJECT, "Holiday Logs " + _reportTitle);
        email.putExtra(Intent.EXTRA_TEXT, logText);

        _context.startActivity(Intent.createChooser(email, "Send logs..."));
    }

    /**
     * Get as many details about the environment as possible
     * @param stringBuilder a string builder containing our crash report
     */
    public void GetEnvironmentDetails(StringBuilder stringBuilder) {
        try {

            // Report Title
            _reportTitle = android.os.Build.MODEL + " " + android.os.Build.VERSION.RELEASE;

            // Basics
            stringBuilder.append("MODEL: ");
            stringBuilder.append(Build.MODEL);
            stringBuilder.append(LINE_SEPARATOR);

            stringBuilder.append("HARDWARE: ");
            stringBuilder.append(Build.HARDWARE);
            stringBuilder.append(LINE_SEPARATOR);

            stringBuilder.append("ANDROID VERSION: ");
            stringBuilder.append(Build.VERSION.RELEASE);
            stringBuilder.append(LINE_SEPARATOR);

            stringBuilder.append("KERNEL VERSION: ");
            stringBuilder.append(System.getProperty("os.version"));
            stringBuilder.append(LINE_SEPARATOR);

            // Get information from package manager
            final PackageManagerWrapper packageManagerWrapper = new PackageManagerWrapper(_context);
            final PackageInfo packageInfo = packageManagerWrapper.getPackageInfo();

            if (packageInfo != null) {

                stringBuilder.append("Package Version code ");
                stringBuilder.append(Integer.toString(packageInfo.versionCode));
                stringBuilder.append(LINE_SEPARATOR);

                stringBuilder.append("Package Version name ");
                stringBuilder.append(packageInfo.versionName != null ? packageInfo.versionName : "not set");
                stringBuilder.append(LINE_SEPARATOR);
            }

            stringBuilder.append("--------------------");
            stringBuilder.append(LINE_SEPARATOR);
            stringBuilder.append(LINE_SEPARATOR);

        } catch (RuntimeException ex) {
            _log.error("Error while retrieving environmental data", ex);
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}




