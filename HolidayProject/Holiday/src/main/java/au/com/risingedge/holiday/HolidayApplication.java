/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

import android.app.Application;
import android.content.Intent;
import au.com.risingedge.holiday.Services.HolidayScannerService;

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

        // Start the Holiday service
        startService(new Intent(this, HolidayScannerService.class));
    }
}
