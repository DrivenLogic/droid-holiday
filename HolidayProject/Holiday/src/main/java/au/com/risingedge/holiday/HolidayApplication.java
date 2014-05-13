/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

import android.app.Application;

/**
 *  The Android application class
 *
 *  @author andrew.stone@drivenlogic.com.au
 */
public class HolidayApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
    }
}
