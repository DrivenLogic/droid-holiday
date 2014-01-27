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

    public String getIp() {
        return _location;
    }
    public String getName() {
        return _name;
    }

    /**
     * Constructor
     * @param _location the Holiday's web GUI URL
     * @param _name the Holidays's hostname
     */
    public ServiceResult(String _location, String _name) {
        this._location = _location;
        this._name = _name;
    }
}
