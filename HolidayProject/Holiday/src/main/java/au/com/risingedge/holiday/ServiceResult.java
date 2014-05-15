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

    private String location;
    private String name;
    private ScanType scanType;

    public String getLocation() { return location; }
    public String getName() {
        return name;
    }
    public ScanType getScanType() { return scanType; }

    /**
     * Constructor
     * @param location the Holiday's web GUI URL
     * @param name the Holidays's hostname
     */
    public ServiceResult(String location, String name, ScanType scanType) {
        this.location = location;
        this.name = name;
        this.scanType = scanType;
    }

    public enum ScanType {
        TCP_SCAN, JMDMS
    }
}
