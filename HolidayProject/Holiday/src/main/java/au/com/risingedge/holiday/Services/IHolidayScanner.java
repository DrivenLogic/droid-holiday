package au.com.risingedge.holiday.Services;

import android.os.Handler;
import au.com.risingedge.holiday.IScanCallbackListener;

/**
 * Copyright (C) 2014 Tapestry International Limited. All rights reserved.
 * User: travis
 * Date: 13/05/2014
 */
public interface IHolidayScanner {
    void beginMdnsSearch(IScanCallbackListener listener, Handler uiHandler);
}
