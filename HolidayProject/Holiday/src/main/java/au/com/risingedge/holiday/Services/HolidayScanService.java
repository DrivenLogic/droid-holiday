package au.com.risingedge.holiday.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HolidayScanService extends Service {

    private HolidayScanner scanner = new HolidayScanner();

    public HolidayScanService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return scanner;
    }
}
