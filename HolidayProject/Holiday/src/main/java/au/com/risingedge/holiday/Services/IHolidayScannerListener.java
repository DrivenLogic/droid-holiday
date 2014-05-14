package au.com.risingedge.holiday.Services;

import au.com.risingedge.holiday.ServiceResults;

/**
 * Copyright (C) 2014 Tapestry International Limited. All rights reserved.
 * User: travis
 * Date: 14/05/2014
 */
public interface IHolidayScannerListener {
    void onScanStart(String message);

    /**
     * Returns the results of the scan. This can be called multiple times as more results are found.
     * It also can be called with an empty results which indicates scan timeout reached
     * @param serviceResults the scan results found
     */
    void onScanResults(ServiceResults serviceResults);

}
