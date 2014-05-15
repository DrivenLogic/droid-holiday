package au.com.risingedge.holiday;

import java.util.ArrayList;

/**
 * A collection class to hold results.
 */
public class ServiceResults {

    private ArrayList<ServiceResult> serviceResults = new ArrayList<ServiceResult>();

    public boolean addServiceResult(ServiceResult serviceResult)
    {
        for (ServiceResult existingServiceResult : serviceResults) {

            // it's here already
            if (existingServiceResult.getLocation().equals(serviceResult.getLocation())) {
                return false;

                // hostname change?
            }
        }

        // it's not here (keyed by location) so add it.
        serviceResults.add(serviceResult);
        return true;
    }

    public int size(){
        return serviceResults.size();
    }

    public ArrayList<ServiceResult> getResults()
    {
        return serviceResults;
    }
}
