package au.com.risingedge.holiday;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * !! WIP !!
 *
 * Android native mdns - dubbed NSD
 * http://developer.android.com/training/connect-devices-wirelessly/nsd.html
 *
 * Seems to be badly busted ATM:
 * https://code.google.com/p/android/issues/detail?id=35585
 * https://code.google.com/p/android/issues/detail?id=41316
 *
 * This code produces an internal NPE on a galaxy S3.
 *
 */
public class NsdRunnable implements Runnable {

    private Logger _log = LoggerFactory.getLogger(NsdRunnable.class);
    private final static String MDNS_SERVICE_TYPE = "_iotas._tcp.local."; //_<protocol>._<transportlayer>

    private IMdnsCallbackListener _callbackListener;
    private NsdManager _nsdManager;
    private NsdManager.ResolveListener _resolveListener;
    private NsdManager.DiscoveryListener _discoveryListener;
    private Handler _uiHandler;
    private String _resolvedServiceName;

    NsdRunnable(IMdnsCallbackListener callbackListener, Context context, Handler uiHandler) {

        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler()); // TODO: move to app class.

        _callbackListener = callbackListener;
        _nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        _uiHandler = uiHandler;

        initializeResolveListener();
        initializeDiscoveryListener();
    }

    public void initializeResolveListener() {
        _resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                _log.error("Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                _log.error("Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(MDNS_SERVICE_TYPE)) {
                    _log.debug("Same IP.");
                    return;
                }

                //mService = serviceInfo;
                _callbackListener.ServiceLocated(new ServiceResult(serviceInfo.getHost().toString(),serviceInfo.getServiceName()));
            }
        };
    }

    public void initializeDiscoveryListener() {
        _discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                _log.debug("Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                _log.debug("Service discovery success" + service);
                if (!service.getServiceType().equals(MDNS_SERVICE_TYPE)) {
                    _log.debug("Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(_resolvedServiceName)) {
                    _log.debug("Same machine: " + _resolvedServiceName);
                } else if (service.getServiceName().contains(_resolvedServiceName)){
                    _nsdManager.resolveService(service, _resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                _log.error("service lost" + service);
//                if (mService == service) {
//                    mService = null;
//                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                _log.info("Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                _log.error("Discovery failed: Error code:" + errorCode);
                //_nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                _log.error("Discovery failed: Error code:" + errorCode);
                //_nsdManager.stopServiceDiscovery(this);
            }
        };
    }

    @Override
    public void run() {

        // inform the callback that we have started
        _uiHandler.post(new Runnable() {
            @Override
            public void run() {
                _callbackListener.TaskBusy("looking for Holiday...");
            }
        });

        // start the scan..
        _nsdManager.discoverServices(
                MDNS_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, _discoveryListener);
    }
}
