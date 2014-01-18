package au.com.risingedge.holiday;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.os.AsyncTask;
import android.util.Log;

public class LogSender {

    private static final String TAG = LogSender.class.getName();
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    public void sendLogs(String exception)
    {
        new CollectLogsTask().execute(exception);
    }

    protected void sendLogs(String logtext, String exception)
    {
        //syncJobSender.sendLog(logtext, exception);
    }

    private class CollectLogsTask extends AsyncTask<String, Void, String>
    {
        private String exception;

        @Override
        protected String doInBackground(String... exceptionOccurred)
        {
            this.exception = exceptionOccurred[0];
            StringBuilder stringBuilder = new StringBuilder().append(LINE_SEPARATOR);

            try
            {
                String program = "logcat -d -v time";

                Process process = Runtime.getRuntime().exec(program);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(LINE_SEPARATOR).append(line);
                }

            }

            catch (IOException e)
            {
                Log.e(TAG, String.format("collectAndSendLog failed"), e);
            }

            Log.d(TAG, "Logs collected, log size is: " + stringBuilder.length());
            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String result)
        {
            sendLogs(result, exception);
        }
    }
}


