/**
 * Holiday For Android - http://moorescloud.com
 * Developed by DrivenLogic.com.au
 * */
package au.com.risingedge.holiday;

/**
 *  An Interface that describes scan events
 *
 *  @author andrew.stone@drivenlogic.com.au
 */
public interface IMdnsCallbackListener {

    /** Found a Service */
    void ServiceLocated(ServiceResult serviceResult);

    /** Task is looking for services */
    void TaskBusy(String message);

    /** done looking */
    void TaskCompleted();

}
