package au.com.risingedge.holiday;

import java.util.ArrayList;

/**
 * A collection class to hold results.
 */
public class ServiceResults {

    private ArrayList<ServiceResult> _serviceResults = new ArrayList<ServiceResult>();

    public boolean addServiceResult(ServiceResult serviceResult)
    {
        for (ServiceResult existingServiceResult : _serviceResults) {

            // it's here already
            if (existingServiceResult.get_location().equals(serviceResult.get_location())) {
                return false;

                // hostname change?
            }
        }

        // it's not here (keyed by location) so add it.
        _serviceResults.add(serviceResult);
        return true;
    }

    public int size(){
        return _serviceResults.size();
    }

    public ArrayList<ServiceResult> GetResults()
    {
        return _serviceResults;
    }
}
