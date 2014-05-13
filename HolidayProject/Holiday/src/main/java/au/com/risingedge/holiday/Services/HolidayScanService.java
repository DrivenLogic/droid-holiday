package au.com.risingedge.holiday.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HolidayScanService extends Service {

    private HolidayScanner scanner = new HolidayScanner(this);

    @Override
    public IBinder onBind(Intent intent) {
        return scanner;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        scanner.onCreate();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        scanner.onDestroy();
    }
}
