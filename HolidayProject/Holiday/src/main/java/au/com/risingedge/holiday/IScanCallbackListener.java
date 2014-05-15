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
public interface IScanCallbackListener {

    /** Found a Service */
    void serviceLocated(ServiceResult serviceResult);

    /** Task is looking for services */
    void scanStarted(String message);

    /** done looking */
    void scanCompleted();

}
