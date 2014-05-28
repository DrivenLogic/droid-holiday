package au.com.risingedge.holiday.Services;

import au.com.risingedge.holiday.ServiceResults;

public interface IHolidayScannerListener {
    void onScanStart(String message);

    /**
     * Returns the results of the scan. This can be called multiple times as more results are found.
     * It also can be called with an empty results which indicates scan timeout reached
     * @param serviceResults the scan results found
     */
    void onScanResults(ServiceResults serviceResults);

}
