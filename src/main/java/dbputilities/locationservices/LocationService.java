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
                case "createLocation":  return createLocation(request, inputArray);
                case "getLocation":     return getLocation(request, inputArray);
                case "updateLocation":  return updateLocation(request, inputArray);
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
        // Lấy toàn bộ params từ inputArray, truyền thẳng vào DB operation
        Map<String, Object> params = getInputParams(inputArray);
        return callIntegration(request, "dbxdb_location_create", params);
    }

    private Result getLocation(DataControllerRequest request, Object[] inputArray) throws Exception {
        Map<String, Object> params = getInputParams(inputArray);

        // Pagination theo chuẩn OData: $top = số record mỗi trang, $skip = số record bỏ qua
        // page là 1-based: page=1 → skip=0, page=2 → skip=pageSize, ...
        int page     = parseIntOrDefault(request.getParameter("page"),     1);
        int pageSize = parseIntOrDefault(request.getParameter("pageSize"), 10);
        int skip     = (page - 1) * pageSize;

        params.put("$top",  String.valueOf(pageSize));
        params.put("$skip", String.valueOf(skip));

        logger.info("getLocation page={} pageSize={} skip={}", page, pageSize, skip);
        return callIntegration(request, "dbxdb_location_get", params);
    }

    private Result updateLocation(DataControllerRequest request, Object[] inputArray) throws Exception {
        Map<String, Object> params = getInputParams(inputArray);
        return callIntegration(request, "dbxdb_location_update", params);
    }

    private Result getLocationById(DataControllerRequest request, Object[] inputArray) throws Exception {
        // Dùng operation dbxdb_getLocations thay vì dbxdb_location_get
        // vì operation này nhận locationId làm filter, trả về đúng 1 record
        Map<String, Object> params = getInputParams(inputArray);
        return callIntegration(request, "dbxdb_getLocations", params);
    }

    private Result deleteLocation(DataControllerRequest request, Object[] inputArray) throws Exception {
        Map<String, Object> params = getInputParams(inputArray);
        return callIntegration(request, "dbxdb_location_delete", params);
    }

    // inputArray[1] là Map params do Fabric truyền vào — dùng thay cho request.getParameter()
    // để nhận được cả params từ Orchestration step output, không chỉ HTTP request
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

    // Trả về defaultValue nếu value null, rỗng, hoặc không parse được thành số
    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return (value != null && !value.trim().isEmpty()) ? Integer.parseInt(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
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