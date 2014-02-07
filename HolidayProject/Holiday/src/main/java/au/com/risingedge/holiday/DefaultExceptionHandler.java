/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Default exception handler
 *
 *  @author andrew.stone@drivenlogic.com.au
 */
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private Logger _log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    /** Constructor */
    public DefaultExceptionHandler()
    {
        Thread.getDefaultUncaughtExceptionHandler();
    }

    /** Logs unhandled exception then exists with an error status */
	@Override
	public void uncaughtException(Thread thread, Throwable throwable)
	{
        throwable.printStackTrace();
        _log.error("Thread unhandled exception handler fired! ",throwable);

        System.exit(1);
    }
}
