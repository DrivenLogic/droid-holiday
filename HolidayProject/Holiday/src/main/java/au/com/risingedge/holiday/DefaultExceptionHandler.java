/**
 *
 */
package au.com.risingedge.holiday;

import android.util.Log;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler
{
	private static final String TAG = "holidayDefaultExceptionHandler" + DefaultExceptionHandler.class.getName();

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

        System.exit(1);
    }
}
