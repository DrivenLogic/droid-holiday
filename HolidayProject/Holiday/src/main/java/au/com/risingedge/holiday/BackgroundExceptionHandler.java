package au.com.risingedge.holiday;

import android.util.Log;

///
///
///
public class BackgroundExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "HolidayBackgroundExceptionHandler";

    Cleanable _instance;

    public BackgroundExceptionHandler(Cleanable instance) {
        Thread.getDefaultUncaughtExceptionHandler();

        _instance = instance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        throwable.printStackTrace();

        Log.e(TAG, "BackgroundThread unhandled exception handler fired.", throwable);

        if (throwable instanceof InterruptedException) {

            // REVIEW: Seems like this might be a waste of time?

            Log.e(TAG, "Cleaning up runnable");
            _instance.cleanUp();

        }
    }
}