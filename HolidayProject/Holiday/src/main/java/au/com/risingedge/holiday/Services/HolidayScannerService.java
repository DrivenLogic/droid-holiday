package au.com.risingedge.holiday.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HolidayScannerService extends Service {

    private HolidayScanner scanner = new HolidayScanner(this);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // sets the service to stay alive
    }

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
