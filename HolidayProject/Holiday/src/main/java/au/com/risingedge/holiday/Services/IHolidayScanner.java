package au.com.risingedge.holiday.Services;

import android.os.Handler;

/**
 * Copyright (C) 2014 Tapestry International Limited. All rights reserved.
 * User: travis
 * Date: 13/05/2014
 */
public interface IHolidayScanner {
    void beginMdnsSearch(Handler uiHandler);

    void registerListener(IHolidayScannerListener listener);
}
