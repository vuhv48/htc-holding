package locationpreprocessor;

import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class GetLocationByIdPreprocessor implements DataPreProcessor2 {

    private static final Logger logger = LogManager.getLogger(GetLocationByIdPreprocessor.class);

    public GetLocationByIdPreprocessor() {}

    @Override
    public boolean execute(HashMap hashMap, DataControllerRequest request,
                           DataControllerResponse response, Result result) throws Exception {

        String locationId = request.getParameter("locationId");
        logger.info("GetLocationByIdPreprocessor locationId={}", locationId);

        if ("1138408970".equals(locationId)) {
            logger.warn("Access denied for locationId={}", locationId);
            return buildError(response, result, 403, "40300", "Không có quyền xem địa điểm này");
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