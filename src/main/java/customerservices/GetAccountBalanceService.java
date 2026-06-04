package customerservices;

import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetAccountBalanceService implements JavaService2 {

    private static final Logger logger = LogManager.getLogger(GetAccountBalanceService.class);

    public GetAccountBalanceService() {}

    @Override
    public Object invoke(String methodId, Object[] inputArray,
                         DataControllerRequest request, DataControllerResponse response) throws Exception {

        String customerCode = request.getParameter("customerCode2");
        logger.info("GetAccountBalanceService customerCode={}", customerCode);

        Result result = new Result();

        if ("CODE-001".equals(customerCode)) {
            result.addParam(new Param("accountNo",   "ACC-0099-2024", "string"));
            result.addParam(new Param("currency",    "VND",           "string"));
            result.addParam(new Param("balance",     "15000000",      "string"));
            result.addParam(new Param("lastUpdated", "2026-06-02",    "string"));
            result.addParam(new Param("accountType", "SAVINGS",       "string"));
        } else {
            result.addParam(new Param("errorCode", "404",               "string"));
            result.addParam(new Param("errorDesc", "Account not found", "string"));
        }

        return result;
    }
}