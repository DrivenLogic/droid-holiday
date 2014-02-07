/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
    ServiceResult.ScanType _scanType;

    /**
     * Constructor
     *
     * @param url location of the Holidays web GUI
     * @param activity current activity
     */
    public HolidayClickListener(String url, Activity activity, ServiceResult.ScanType scanType) {
        _url = url;
        _activity = activity;
        _scanType = scanType;
    }

    /** OnClick Open a web intent in the default browser */
    @Override
    public void onClick(View view)
    {
        if(_scanType== ServiceResult.ScanType.TCP_SCAN)
        {
            ShowDialogBookmark();
        }
        else
        {
            InvokeBrowserIntent();
        }
    }

    /**
     * Bookmark notice dialog
     */
    private void ShowDialogBookmark() {
        AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
        builder.setMessage(R.string.tcp_result_warning)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        InvokeBrowserIntent();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Invoke a browser intent pointed at the holidays control panel
     */
    private void InvokeBrowserIntent()
    {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(_url));
        _activity.startActivity(i);
    }
}