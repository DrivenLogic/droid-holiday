/**
 * http://moorescloud.com - Android Holiday finder
 * author: andrew.stone@drivenlogic.com.au
 */
package au.com.risingedge.holiday;

import android.util.Log;

///
/// Unhandled exceptions
/// TODO: move to application class.
///
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler
{
	private static final String TAG = "holidayDefaultExceptionHandler";

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

        // clean quit
        System.exit(1);
    }
}
