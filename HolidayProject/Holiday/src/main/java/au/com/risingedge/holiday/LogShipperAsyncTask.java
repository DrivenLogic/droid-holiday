package au.com.risingedge.holiday;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class LogShipperAsyncTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = LogShipperAsyncTask.class.getName();
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    private Activity _activity;
    private String exception;

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

        try {
            String program = "logcat -d -v time";

            Process process = Runtime.getRuntime().exec(program);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(LINE_SEPARATOR).append(line);
            }

        } catch (IOException e) {
            Log.e(TAG, String.format("collectAndSendLog failed"), e);
        }

        Log.d(TAG, "Logs collected, log size is: " + stringBuilder.length());
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
            File dir = new File(sdCard.getAbsolutePath() + "/log.Holiday");
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
            Log.e(TAG, "Error writing log to SD card ", e);
        }
    }

    ///
    /// Kick off an email intent to send the logs
    ///
    private void SendLogViaEmail(String logText) {

        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("message/rfc822");
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"andrew.stone@drivenlogic.com.au", "mpesce@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "Holiday Logs");
        email.putExtra(Intent.EXTRA_TEXT, logText);

        _activity.startActivity(Intent.createChooser(email, "Send logs..."));
    }
}


