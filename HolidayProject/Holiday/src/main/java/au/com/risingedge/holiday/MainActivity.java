package au.com.risingedge.holiday;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

    private static final String TAG = "holidayMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // catch unhandled Exceptions
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        // go full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // run scan task
        new MdnsAsyncTask(this).execute();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start scanner?
    }

    ///
    /// Clean up on stop - multicast chews battery.
    ///
    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

