package dbputilities.locationservices;

import com.konylabs.middleware.api.OperationData;
import com.konylabs.middleware.api.ServiceRequest;
import com.konylabs.middleware.api.ServicesManager;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.JSONToResult;
import com.konylabs.middleware.dataobject.Result;
import dbputilities.common.ErrorStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class LocationService implements JavaService2 {

    private static final Logger logger = LogManager.getLogger(LocationService.class);
    private static final String INTEGRATION_SERVICE = "HoangDBService";

    public LocationService() {}

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
        logger.info("LocationService invoked, methodId={}", methodId);
        try {
            switch (methodId) {
                case "createLocation": return createLocation(request, inputArray);
                case "getLocation":    return getLocation(request, inputArray);
                case "updateLocation": return updateLocation(request, inputArray);
                case "deleteLocation":  return deleteLocation(request, inputArray);
                case "getLocationById": return getLocationById(request, inputArray);
                default:
                    return buildError(ErrorStatus.INVALID_ARGUMENT, "Unknown methodId: " + methodId);
            }
        } catch (Exception e) {
            logger.error("LocationService [{}] failed: {}", methodId, e.getMessage(), e);
            return buildError(ErrorStatus.INTERNAL_ERROR, e.getMessage());
        }
    }

    private Result createLocation(DataControllerRequest request, Object[] inputArray) throws Exception {
        Map<String, Object> params = getInputParams(inputArray);
        return callIntegration(request, "dbxdb_location_create", params);
    }

    private Result getLocation(DataControllerRequest request, Object[] inputArray) throws Exception {
        Map<String, Object> params = getInputParams(inputArray);
        params.put("id", request.getParameter("id"));
        return callIntegration(request, "dbxdb_location_get", params);
    }

    private Result updateLocation(DataControllerRequest request, Object[] inputArray) throws Exception {
        Map<String, Object> params = getInputParams(inputArray);
        return callIntegration(request, "dbxdb_location_update", params);
    }

    private Result getLocationById(DataControllerRequest request, Object[] inputArray) throws Exception {
        Map<String, Object> params = getInputParams(inputArray);
        return callIntegration(request, "dbxdb_getLocations", params);
    }

    private Result deleteLocation(DataControllerRequest request, Object[] inputArray) throws Exception {
        Map<String, Object> params = getInputParams(inputArray);
        return callIntegration(request, "dbxdb_location_delete", params);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getInputParams(Object[] inputArray) {
        if (inputArray != null && inputArray.length > 1 && inputArray[1] instanceof Map) {
            return (Map<String, Object>) inputArray[1];
        }
        return new HashMap<>();
    }

    private Result buildError(ErrorStatus status, String detail) {
        Result error = new Result();
        error.addParam("errcode",  status.code());
        error.addParam("errmsg",   detail != null ? detail : status.defaultMessage());
        error.addParam("opstatus", "1");
        return error;
    }

    private Result callIntegration(DataControllerRequest request, String operationId, Map<String, Object> inputParams) throws Exception {
        ServicesManager servicesManager = request.getServicesManager();
        if (servicesManager == null) {
            throw new IllegalStateException("ServicesManager is null");
        }

        OperationData operationData = servicesManager
                .getOperationDataBuilder()
                .withServiceId(INTEGRATION_SERVICE)
                .withOperationId(operationId)
                .build();

        ServiceRequest serviceRequest = servicesManager
                .getRequestBuilder(operationData)
                .withInputs(inputParams)
                .withHeaders(request.getHeaderMap())
                .build();

        String integrationResponse = serviceRequest.invokeServiceAndGetJson();
        logger.info("LocationService [{}] raw response: {}", operationId, integrationResponse);

        return JSONToResult.convert(integrationResponse);
    }
}