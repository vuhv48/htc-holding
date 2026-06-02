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

import java.util.Map;

public class LocationService2 implements JavaService2 {

    private static final Logger logger = LogManager.getLogger(LocationService2.class);
    private static final String INTEGRATION_SERVICE = "HoangDBService";

    public LocationService2() {}

    @Override
    public Object invoke(String methodId, Object[] inputParams, DataControllerRequest request, DataControllerResponse response) throws Exception {
        Result result = new Result();
        try {
            switch (methodId) {
                case "createLocation": return createLocation(inputParams, request);
                case "getLocation":    return getLocation(inputParams, request);
                case "updateLocation": return updateLocation(inputParams, request);
                case "deleteLocation": return deleteLocation(inputParams, request);
                default:
                    return buildError(ErrorStatus.INVALID_ARGUMENT, "Unknown methodId: " + methodId);
            }
        } catch (Exception e) {
            logger.error("Exception occurred: {}", e.getMessage(), e);
            result.addParam("log", e.getMessage());
            return buildError(ErrorStatus.INTERNAL_ERROR, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getInputParams(Object[] inputArray) {
        if (inputArray != null && inputArray.length > 1 && inputArray[1] instanceof Map) {
            return (Map<String, Object>) inputArray[1];
        }
        return new java.util.HashMap<>();
    }

    private Result buildError(ErrorStatus status, String detail) {
        Result error = new Result();
        error.addParam("errcode",  status.code());
        error.addParam("errmsg",   detail != null ? detail : status.defaultMessage());
        error.addParam("opstatus", "1");
        return error;
    }

    private Result createLocation(Object[] inputParams, DataControllerRequest request) throws Exception {
        Map<String, Object> params = getInputParams(inputParams);
        logger.debug("createLocation params: {}", params);
        String resultStr = callIntegration(request, "dbxdb_location_create", params);
        logger.debug("createLocation resultStr: {}", resultStr);
        return JSONToResult.convert(resultStr);
    }

    private Result getLocation(Object[] inputParams, DataControllerRequest request) throws Exception {
        Map<String, Object> params = getInputParams(inputParams);
        String id = (String) params.get("id");
        if (id != null && !id.trim().isEmpty()) {
            params.put("$filter", "id eq " + id);
        }
        logger.debug("getLocation params: {}", params);
        String resultStr = callIntegration(request, "dbxdb_location_get", params);
        logger.debug("getLocation resultStr: {}", resultStr);
        return JSONToResult.convert(resultStr);
    }

    private Result updateLocation(Object[] inputParams, DataControllerRequest request) throws Exception {
        Map<String, Object> params = getInputParams(inputParams);
        logger.debug("updateLocation params: {}", params);
        String resultStr = callIntegration(request, "dbxdb_location_update", params);
        logger.debug("updateLocation resultStr: {}", resultStr);
        return JSONToResult.convert(resultStr);
    }

    private Result deleteLocation(Object[] inputParams, DataControllerRequest request) throws Exception {
        Map<String, Object> params = getInputParams(inputParams);
        logger.debug("deleteLocation params: {}", params);
        String resultStr = callIntegration(request, "dbxdb_location_delete", params);
        logger.debug("deleteLocation resultStr: {}", resultStr);
        return JSONToResult.convert(resultStr);
    }

    private String callIntegration(DataControllerRequest request, String operationId, Map<String, Object> params) throws Exception {
        ServicesManager servicesManager = request.getServicesManager();
        OperationData operationData = servicesManager
                .getOperationDataBuilder()
                .withServiceId(INTEGRATION_SERVICE)
                .withOperationId(operationId)
                .build();
        ServiceRequest serviceRequest = servicesManager
                .getRequestBuilder(operationData)
                .withInputs(params)
                .withHeaders(request.getHeaderMap())
                .build();
        return serviceRequest.invokeServiceAndGetJson();
    }
}