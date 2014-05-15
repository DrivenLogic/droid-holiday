package au.com.risingedge.holiday.Services;

/**
 * Copyright (C) 2014 Tapestry International Limited. All rights reserved.
 * User: travis
 * Date: 13/05/2014
 */
public interface IHolidayScanner {
    void beginMdnsSearch();

    void beginTcpScan();

    void registerListener(IHolidayScannerListener listener);

    void unregisterListener(IHolidayScannerListener listener);

    void stopMdnsSearch();
}
