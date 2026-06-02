package locationpostprocessor;

import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocationGetPostprocessor implements DataPostProcessor2 {

    private static final Logger logger = LogManager.getLogger(LocationGetPostprocessor.class);

    public LocationGetPostprocessor() {}

    @Override
    public Object execute(Result result, DataControllerRequest request, DataControllerResponse response) throws Exception {
        if (result == null) {
            return null;
        }

        Dataset sourceDs = result.getDatasetById("location");
        if (sourceDs == null) {
            return result;
        }

        Result finalResult = new Result();
        Dataset filteredDs = new Dataset();
        filteredDs.setId("location");

        for (Record record : sourceDs.getAllRecords()) {
            Param flagParam = record.getParamByName("softdeleteflag");
            String flag = (flagParam != null) ? flagParam.getValue() : null;
            if ("false".equalsIgnoreCase(flag)) {
                logger.info("Filtered soft-deleted location: {}",
                        record.getParamByName("id") != null ? record.getParamByName("id").getValue() : "unknown");
            } else {
                filteredDs.addRecord(record);
            }
        }

        finalResult.addDataset(filteredDs);
        return finalResult;
    }
}