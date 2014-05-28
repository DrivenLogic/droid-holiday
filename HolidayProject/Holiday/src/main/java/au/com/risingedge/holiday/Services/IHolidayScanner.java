package au.com.risingedge.holiday.Services;


public interface IHolidayScanner {
    void beginMdnsSearch();

    void beginTcpScan();

    void registerListener(IHolidayScannerListener listener);

    void unregisterListener(IHolidayScannerListener listener);

    void stopMdnsSearch();
}
