/**
 * Holiday For Android - http://moorescloud.com
 *
 * */
package au.com.risingedge.holiday;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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
    private static final String[] LOG_MAILBOXES = new String[]{"andrew.stone@drivenlogic.com.au", "mpesce@gmail.com"};
    private Activity _activity;

    /**
     * Constructor
     * @param activity
     */
    LogShipperAsyncTask(Activity activity) {
        _activity = activity;
    }

    @Override
    protected String doInBackground(Void... voids) {

        StringBuilder stringBuilder = new StringBuilder().append(LINE_SEPARATOR);

        stringBuilder.append("PHONE: ");
        stringBuilder.append(android.os.Build.MODEL);
        stringBuilder.append(LINE_SEPARATOR);
        stringBuilder.append("DROID VERSION: ");
        stringBuilder.append(android.os.Build.VERSION.RELEASE);
        stringBuilder.append(LINE_SEPARATOR);

            File sdcard = Environment.getExternalStorageDirectory();

            File file = new File(sdcard,"Holiday.log");

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


        _log.debug("Logs collected, log size is: " + stringBuilder.length());
        return stringBuilder.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        SaveLogToSD(result);
        SendLogViaEmail(result);
    }

    ///
    /// Save the log to the SD card
    ///
    private void SaveLogToSD(String logText) {
        try {

            // write out to SD

            // dirs
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath());
            dir.mkdirs();

            // file
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.HHmm.ss");
            Date now = new Date();
            String fileName = formatter.format(now) + ".log";
            File file = new File(dir, "holiday." + fileName);

            // write
            FileWriter writer = new FileWriter(file);// Writes the content to the file
            writer.write(logText);
            writer.flush();
            writer.close();
        } catch (IOException e) {

            e.printStackTrace();
            _log.error("Error writing log to SD card ", e);
        }
    }

    /**
     * Launch an email intent.
     * @param logText text for the body of the email
     */
    private void SendLogViaEmail(String logText) {

        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("message/rfc822");
        email.putExtra(Intent.EXTRA_EMAIL, LOG_MAILBOXES);
        email.putExtra(Intent.EXTRA_SUBJECT, "Holiday Logs");
        email.putExtra(Intent.EXTRA_TEXT, logText);

        _activity.startActivity(Intent.createChooser(email, "Send logs..."));
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


