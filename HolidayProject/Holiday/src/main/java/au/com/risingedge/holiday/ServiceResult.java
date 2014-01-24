package au.com.risingedge.holiday;

///
/// POJO to hold service results.
///
public class ServiceResult {

    private String _location;
    private String _name;

    public String getIp() {
        return _location;
    }
    public String getName() {
        return _name;
    }

    public ServiceResult(String _location, String _name) {
        this._location = _location;
        this._name = _name;
    }
}
