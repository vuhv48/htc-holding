package accountpostprocessor;

import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;

public class AccountGetPostprocessor implements DataPostProcessor2 {

    private static final Logger logger = LogManager.getLogger(AccountGetPostprocessor.class);

    public AccountGetPostprocessor() {}

    @Override
    public Object execute(Result result, DataControllerRequest request, DataControllerResponse response) throws Exception {
        Dataset ds = result.getDatasetById("accounts");
        if (ds == null) {
            return result;
        }

        Iterator<Record> iterator = ds.getAllRecords().iterator();
        while (iterator.hasNext()) {
            Record record = iterator.next();
            Param flagParam = record.getParamByName("softdeleteflag");
            String flag = (flagParam != null) ? flagParam.getValue() : null;
            if ("true".equalsIgnoreCase(flag)) {
                logger.info("Filtered soft-deleted account: {}", record.getParamByName("Account_id") != null ? record.getParamByName("Account_id").getValue() : "unknown");
                iterator.remove();
            }
        }

        return result;
    }
}