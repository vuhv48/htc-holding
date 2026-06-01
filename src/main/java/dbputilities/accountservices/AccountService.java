package dbputilities.accountservices;

import com.konylabs.middleware.api.OperationData;
import com.konylabs.middleware.api.ServiceRequest;
import com.konylabs.middleware.api.ServicesManager;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.JSONToResult;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;
import dbputilities.common.ErrorStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AccountService implements JavaService2 {

    private static final Logger logger = LogManager.getLogger(AccountService.class);
    private static final String INTEGRATION_SERVICE = "HoangDBService";

    public AccountService() {}

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
        logger.info("AccountService invoked, methodId={}", methodId);
        try {
            switch (methodId) {
                case "createAccount": return createAccount(request);
                case "getAccount":    return getAccount(request);
                case "updateAccount": return updateAccount(request);
                case "deleteAccount": return deleteAccount(request);
                default:
                    return buildError(ErrorStatus.INVALID_ARGUMENT, "Unknown methodId: " + methodId);
            }
        } catch (Exception e) {
            logger.error("AccountService [{}] failed: {}", methodId, e.getMessage(), e);
            return buildError(ErrorStatus.INTERNAL_ERROR, e.getMessage());
        }
    }

    private Result createAccount(DataControllerRequest request) throws Exception {
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("Account_id",           request.getParameter("Account_id"));
        inputParams.put("AccountName",          request.getParameter("AccountName") + "_HOANG_VU_TEST");
        inputParams.put("Membership_id",        request.getParameter("Membership_id"));
        inputParams.put("MembershipName",       request.getParameter("MembershipName"));
        inputParams.put("arrangementId",        request.getParameter("arrangementId"));
        inputParams.put("Type_id",              request.getParameter("Type_id"));
        inputParams.put("Bank_id",              request.getParameter("Bank_id"));
        inputParams.put("StatusDesc",           request.getParameter("StatusDesc"));
        inputParams.put("CurrentBalance",       request.getParameter("CurrentBalance"));
        inputParams.put("AvailableBalance",     request.getParameter("AvailableBalance"));
        inputParams.put("companyLegalUnit",     request.getParameter("companyLegalUnit"));
        inputParams.put("softdeleteflag",       request.getParameter("softdeleteflag"));
        inputParams.put("isBusinessAccount",    request.getParameter("isBusinessAccount"));
        inputParams.put("IsPFM",                request.getParameter("IsPFM"));
        inputParams.put("FavouriteStatus",      request.getParameter("FavouriteStatus"));
        inputParams.put("SupportDeposit",       request.getParameter("SupportDeposit"));
        inputParams.put("SupportCardlessCash",  request.getParameter("SupportCardlessCash"));
        inputParams.put("SupportTransferFrom",  request.getParameter("SupportTransferFrom"));
        inputParams.put("SupportTransferTo",    request.getParameter("SupportTransferTo"));
        inputParams.put("SupportBillPay",       request.getParameter("SupportBillPay"));
        inputParams.put("ShowTransactions",     request.getParameter("ShowTransactions"));
        inputParams.put("EStatementmentEnable", request.getParameter("EStatementmentEnable"));
        inputParams.put("AccountPreference",    request.getParameter("AccountPreference"));
        inputParams.put("InterestRate",         request.getParameter("InterestRate"));
        inputParams.put("CreditLimit",          request.getParameter("CreditLimit"));
        inputParams.put("TransactionLimit",     request.getParameter("TransactionLimit"));
        inputParams.put("TransferLimit",        request.getParameter("TransferLimit"));
        return callIntegration(request, "dbxdb_accounts_create", inputParams);
    }

    private Result getAccount(DataControllerRequest request) throws Exception {
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("Membership_id", request.getParameter("Membership_id"));
        Result result = callIntegration(request, "dbxdb_accounts_get", inputParams);

//        // [START] them field testHoangVanVu vao tung record
//        Dataset ds = result.getDatasetById("accounts");
//        if (ds != null) {
//            for (Record record : ds.getAllRecords()) {
//                Param p = new Param();
//                p.setName("testHoangVanVu");
//                p.setValue("hoang van vu");
//                record.addParam(p);
//            }
//        }
//        // [END] them field testHoangVanVu vao tung record

        return result;
    }

    private Result updateAccount(DataControllerRequest request) throws Exception {
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("Account_id",     request.getParameter("Account_id"));
        inputParams.put("AccountName",    request.getParameter("AccountName") + "_HOANG_VU_TEST");
        inputParams.put("CurrentBalance", request.getParameter("CurrentBalance"));
        inputParams.put("StatusDesc",     request.getParameter("StatusDesc"));
        inputParams.put("softdeleteflag", request.getParameter("softdeleteflag"));
        return callIntegration(request, "dbxdb_accounts_update", inputParams);
    }

    private Result deleteAccount(DataControllerRequest request) throws Exception {
        String accountId = request.getParameter("Account_id");
        if ("10300001".equals(accountId)) {
            return buildError(ErrorStatus.ACCOUNT_NOT_DELETABLE, null);
        }
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("Account_id", accountId);
        return callIntegration(request, "dbxdb_accounts_delete", inputParams);
    }

    private Result buildError(ErrorStatus status, String detail) {
        Result error = new Result();
        error.addParam("errcode",   status.code());
        error.addParam("errmsg",    detail != null ? detail : status.defaultMessage());
        error.addParam("opstatus",  "1");
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
        logger.info("AccountService [{}] raw response: {}", operationId, integrationResponse);

        return JSONToResult.convert(integrationResponse);
    }
}