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
    String url;
    Activity activity;
    ServiceResult.ScanType scanType;

    /**
     * Constructor
     *
     * @param url location of the Holidays web GUI
     * @param activity current activity
     */
    public HolidayClickListener(String url, Activity activity, ServiceResult.ScanType scanType) {
        this.url = url;
        this.activity = activity;
        this.scanType = scanType;
    }

    /** OnClick Open a web intent in the default browser */
    @Override
    public void onClick(View view)
    {
        if(scanType == ServiceResult.ScanType.TCP_SCAN)
        {
            showDialogBookmark();
        }
        else
        {
            invokeBrowserIntent();
        }
    }

    /**
     * Bookmark notice dialog
     */
    private void showDialogBookmark() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.tcp_result_warning)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        invokeBrowserIntent();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Invoke a browser intent pointed at the holidays control panel
     */
    private void invokeBrowserIntent()
    {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        activity.startActivity(i);
    }
}