package au.com.risingedge.holiday;


import javax.jmdns.ServiceInfo;

public interface IMdnsCallbackListener {

    // Found a Service
    void ServiceLocated(ServiceResult serviceResult);

    // Task is looking for services
    void TaskBusy(String message);

    // Task is done
    void TaskCompleted();
}
