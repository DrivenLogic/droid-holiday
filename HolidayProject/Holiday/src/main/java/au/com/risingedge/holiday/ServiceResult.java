/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

/**
 * a POJO to hold service results
 *
 * @author andrew.stone@drivenlogic.com.au
 */
public class ServiceResult {

    private String _location;
    private String _name;
    private ScanType _scanType;

    public String get_location() { return _location; }
    public String getName() {
        return _name;
    }
    public ScanType getScanType() { return _scanType; }

    /**
     * Constructor
     * @param _location the Holiday's web GUI URL
     * @param _name the Holidays's hostname
     */
    public ServiceResult(String location, String name, ScanType scanType) {
        _location = location;
        _name = name;
        _scanType = scanType;
    }

    public enum ScanType {
        TCP_SCAN, JMDMS
    }
}
