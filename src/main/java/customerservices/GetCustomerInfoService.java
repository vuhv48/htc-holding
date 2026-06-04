package customerservices;

import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetCustomerInfoService implements JavaService2 {

    private static final Logger logger = LogManager.getLogger(GetCustomerInfoService.class);

    public GetCustomerInfoService() {
    }

    @Override
    public Object invoke(String methodId, Object[] inputArray,
                         DataControllerRequest request, DataControllerResponse response) throws Exception {

        String customerId = request.getParameter("customerId");
        logger.info("GetCustomerInfoService customerId={}", customerId);

        Result result = new Result();

        if ("CUS001".equals(customerId)) {
            result.addParam(new Param("customerId", "CUS001", "string"));
            result.addParam(new Param("customerCode", "CODE-001", "string"));
            result.addParam(new Param("customerName", "Nguyen Van A", "string"));
            result.addParam(new Param("email", "nguyenvana@gmail.com", "string"));
            result.addParam(new Param("phone", "0901234567", "string"));
            result.addParam(new Param("address", "123 Nguyen Hue, Q1, HCMC", "string"));
            result.addParam(new Param("status", "ACTIVE", "string"));
        } else {
            result.addParam(new Param("errorCode", "404", "string"));
            result.addParam(new Param("errorDesc", "Customer not found", "string"));
        }

        return result;
    }
}