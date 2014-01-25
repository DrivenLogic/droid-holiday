/**
 * Holiday For Android - http://moorescloud.com
 *
 * */
package au.com.risingedge.holiday;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

/**
 *  Click handler for located Holidays
 *
 *  @author andrew.stone@drivenlogic.com.au
 */
public class HolidayClickListener implements View.OnClickListener
{
    String _url;
    Activity _activity;

    /**
     * Constructor
     *
     * @param url location of the Holidays web GUI
     * @param activity current activity
     */
    public HolidayClickListener(String url, Activity activity) {
        _url = url;
        _activity = activity;
    }

    /**
     *  OnClick Open a web intent in the default browser
     */
    @Override
    public void onClick(View view)
    {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(_url));
        _activity.startActivity(i);
    }
}