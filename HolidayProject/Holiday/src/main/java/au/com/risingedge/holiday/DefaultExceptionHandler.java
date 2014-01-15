/**
 *
 */
package au.com.risingedge.holiday;

import android.util.Log;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler
{
	private static final String TAG = "holiday" + DefaultExceptionHandler.class.getName();

    public DefaultExceptionHandler()
    {
        Thread.getDefaultUncaughtExceptionHandler();
    }

	public void initialize()
	{
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable throwable)
	{
        throwable.printStackTrace();

        Log.e(TAG,"Thread unhandled exception handler fired.",throwable);

        // exit the application to prevent the home default being reset when we crash
        System.exit(1);
    }
}
