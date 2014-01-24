package au.com.risingedge.holiday;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

///
/// A click handler class that passes the URL of the given Holiday
///
public class HolidayClickListener implements View.OnClickListener
{
    String _url;
    Activity _activity;

    public HolidayClickListener(String url, Activity activity) {
        _url = url;
        _activity = activity;
    }

    @Override
    public void onClick(View v)
    {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(_url));
        _activity.startActivity(i);
    }
}