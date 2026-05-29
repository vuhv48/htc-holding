package dbputilities.addressservices;

import com.konylabs.middleware.api.OperationData;
import com.konylabs.middleware.api.ServiceRequest;
import com.konylabs.middleware.api.ServicesManager;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.JSONToResult;
import com.konylabs.middleware.dataobject.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GetAllAddress implements JavaService2 {

    private static final Logger logger = LogManager.getLogger(GetAllAddress.class);
    private static final String INTEGRATION_SERVICE = "TestDatabaseService";

    public GetAllAddress() {}

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
        logger.info("GetAllAddress invoked, methodId={}", methodId);
        try {
            return getAllAddress(request);
        } catch (Exception e) {
            // Log full stack trace để xem lỗi thực sự trên Fabric server log
            logger.error("GetAllAddress failed: {}", e.getMessage(), e);
            Result error = new Result();
            error.addParam("errmsg", e.getMessage());
            error.addParam("errclass", e.getClass().getName());
            error.addParam("opstatus", "1");
            return error;
        }
    }

    private Result getAllAddress(DataControllerRequest request) throws Exception {
        ServicesManager servicesManager = request.getServicesManager();
        if (servicesManager == null) {
            throw new IllegalStateException("ServicesManager is null");
        }

        OperationData operationData = servicesManager
                .getOperationDataBuilder()
                .withServiceId(INTEGRATION_SERVICE)
                .withOperationId("dbxdb_address_get")
                .build();

        Map<String, Object> inputParams = new HashMap<>();

        ServiceRequest serviceRequest = servicesManager
                .getRequestBuilder(operationData)
                .withInputs(inputParams)
                .withHeaders(request.getHeaderMap())
                .build();

        String integrationResponse = serviceRequest.invokeServiceAndGetJson();
        logger.info("getAllAddress raw response: {}", integrationResponse);

        return JSONToResult.convert(integrationResponse);
    }
}
