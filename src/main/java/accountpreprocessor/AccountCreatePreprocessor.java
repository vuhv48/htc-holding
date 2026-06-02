package accountpreprocessor;

import com.konylabs.middleware.api.OperationData;
import com.konylabs.middleware.api.ServiceRequest;
import com.konylabs.middleware.api.ServicesManager;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.JSONToResult;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccountCreatePreprocessor implements DataPreProcessor2 {

    private static final Logger logger = LogManager.getLogger(AccountCreatePreprocessor.class);
    private static final String SERVICE_ID   = "HoangDBService";
    private static final String OPERATION_ID = "dbxdb_accounts_get";

    public AccountCreatePreprocessor() {}

    @Override
    public boolean execute(HashMap hashMap, DataControllerRequest request,
                           DataControllerResponse response, Result result) throws Exception {

        String accountId = request.getParameter("Account_id");

        if (accountId == null || accountId.trim().isEmpty()) {
            return buildError(response, result, 400, "40000", "Account_id is required");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("$filter", "Account_id eq '" + accountId + "'");

        ServicesManager servicesManager = request.getServicesManager();
        OperationData operationData = servicesManager
                .getOperationDataBuilder()
                .withServiceId(SERVICE_ID)
                .withOperationId(OPERATION_ID)
                .build();

        ServiceRequest serviceRequest = servicesManager
                .getRequestBuilder(operationData)
                .withInputs(params)
                .withHeaders(request.getHeaderMap())
                .build();

        String resultStr = serviceRequest.invokeServiceAndGetJson();

        if (resultStr == null || resultStr.trim().isEmpty()) {
            return buildError(response, result, 500, "50002", "Empty response from DB service");
        }

        JSONObject jsonObject = new JSONObject(resultStr);
        if (jsonObject.optInt("opstatus") != 0) {
            return buildError(response, result, 500, "50001", "Internal service error");
        }

        Result jsonResult = JSONToResult.convert(jsonObject.toString());
        Dataset ds = jsonResult.getDatasetById("accounts");
        if (ds != null && !ds.getAllRecords().isEmpty()) {
            logger.warn("Account_id [{}] already exists", accountId);
            return buildError(response, result, 400, "40001", "Account_id already exists");
        }

        return true;
    }

    private boolean buildError(DataControllerResponse response, Result result,
                                int httpStatus, String errorCode, String errorDesc) {
        response.setStatusCode(httpStatus);
        result.addParam(createParam("errorCode", errorCode));
        result.addParam(createParam("errorDesc", errorDesc));
        return false;
    }

    private Param createParam(String name, String value) {
        Param param = new Param();
        param.setName(name);
        param.setValue(value);
        return param;
    }
}