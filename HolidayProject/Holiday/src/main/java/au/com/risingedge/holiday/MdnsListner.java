package au.com.risingedge.holiday;

import android.os.Handler;
import android.util.Log;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

///
/// Implementation of jmDNS listner class.
///
class MdnsListener implements ServiceListener {

    private static final String TAG = "holidayMdnsListener";
    Handler mdnsResolutionHandler = new Handler();
    private JmDNS _jmdns = null;
    private MainActivity _mainActivity;

    MdnsListener(JmDNS jmdns, MainActivity mainActivity)
    {
        _jmdns = jmdns;
        _mainActivity = mainActivity; // TODO: clean this up. don't reference the view directly from here.
    }

    // Called when a new service is added to the local mdns cache
    @Override
    public void serviceAdded(final ServiceEvent event) {
        Log.i(TAG, "Service added   : " + event.getName() + "." + event.getType());

        mdnsResolutionHandler.postDelayed(new Runnable() {
            public void run() {
                try
                {
                    _jmdns.requestServiceInfo(event.getType(), event.getName());
                }
                catch (Throwable e) {
                    e.printStackTrace();
                    Log.e(TAG,"Error requesting service info",e);
                    return;
                }
            }
        }, 1000);
    }

    // called when a service is removed from the local mdns cache
    @Override
    public void serviceRemoved(ServiceEvent event) {
        Log.i(TAG, "Service removed : " + event.getName() + "." + event.getType());
    }

    // Called when a service is resolved.
    @Override
    public void serviceResolved(ServiceEvent event) {
        Log.i(TAG, "Service resolved: " + event.getInfo());

        String additions = "";
        if (event.getInfo().getInetAddresses() != null && event.getInfo().getInetAddresses().length > 0) {
            additions = event.getInfo().getInetAddresses()[0].getHostAddress();
        }

        _mainActivity.notifyUser("Service resolved: " + event.getInfo().getQualifiedName(),event.getInfo().getURL()); // TODO: use array members instead
    }
}
