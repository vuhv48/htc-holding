package customerpreprocessor;

import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class GetAccountBalancePreprocessor implements DataPreProcessor2 {

    private static final Logger logger = LogManager.getLogger(GetAccountBalancePreprocessor.class);

    public GetAccountBalancePreprocessor() {}

    @Override
    public boolean execute(HashMap inputParams, DataControllerRequest request,
                           DataControllerResponse response, Result result) throws Exception {

        String customerCode = request.getParameter("customerCode");
        logger.info("GetAccountBalancePreprocessor customerCode={}", customerCode);

        if (customerCode != null && !customerCode.trim().isEmpty()) {
            inputParams.put("customerCode2", customerCode);
        }

        return true;
    }
}