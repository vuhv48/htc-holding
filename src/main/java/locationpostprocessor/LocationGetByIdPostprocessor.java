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

import java.util.List;

public class LocationGetByIdPostprocessor implements DataPostProcessor2 {

    private static final Logger logger = LogManager.getLogger(LocationGetByIdPostprocessor.class);

    public LocationGetByIdPostprocessor() {}

    @Override
    public Object execute(Result result, DataControllerRequest request, DataControllerResponse response) throws Exception {
        if (result == null) {
            return null;
        }

        // dbxdb_getLocations trả JSON key "records", nhưng dataset ID trong Result
        // có thể khác tùy cách Fabric parse. Fallback lấy dataset đầu tiên nếu không tìm thấy.
        Dataset ds = result.getDatasetById("records");
        if (ds == null) {
            if (result.getAllDatasets() != null && !result.getAllDatasets().isEmpty()) {
                ds = result.getAllDatasets().get(0);
            } else {
                // result không có dataset nào → trả thẳng raw result để xác nhận postprocessor đang nhận đúng data
                return result;
            }
        }
        if (ds == null || ds.getAllRecords().isEmpty()) {
            logger.warn("getLocationById: no record found");
            // Trả về Result mới thay vì ném exception để Fabric không bị lỗi 8004
            Result notFound = new Result();
            notFound.addParam(new Param("opstatus", "1"));
            notFound.addParam(new Param("errcode",  "LOCATION_NOT_FOUND"));
            notFound.addParam(new Param("errmsg",   "Không tìm thấy chi nhánh / địa điểm"));
            return notFound;
        }

        List<Record> records = ds.getAllRecords();
        // get(0) vì getLocationById chỉ trả về đúng 1 record theo locationId
        Record source = records.get(0);

        Param flagParam = source.getParamByName("softdeleteflag");
        String flag = (flagParam != null) ? flagParam.getValue() : null;
        // Coi record đã xóa mềm như không tồn tại, trả cùng lỗi LOCATION_NOT_FOUND
        // để client không phân biệt được record bị xóa hay không có thật
        if ("true".equalsIgnoreCase(flag)) {
            logger.warn("getLocationById: location is soft-deleted");
            Result deleted = new Result();
            deleted.addParam(new Param("opstatus", "1"));
            deleted.addParam(new Param("errcode",  "LOCATION_NOT_FOUND"));
            deleted.addParam(new Param("errmsg",   "Không tìm thấy chi nhánh / địa điểm"));
            return deleted;
        }

        Result finalResult = new Result();
        // Dùng Record (không phải Dataset) vì response trả về 1 object đơn, không phải array.
        // setId("location") để client nhận JSON key là "location" thay vì "records"
        Record data = new Record();
        data.setId("location");

        data.addParam(new Param("id",              getValue(source, "id")));
        data.addParam(new Param("Name",            getValue(source, "Name")));
        data.addParam(new Param("Code",            getValue(source, "Code")));
        data.addParam(new Param("DisplayName",     getValue(source, "DisplayName")));
        data.addParam(new Param("Description",     getValue(source, "Description")));
        data.addParam(new Param("EmailId",         getValue(source, "EmailId")));
        data.addParam(new Param("PhoneNumber",     getValue(source, "PhoneNumber")));
        data.addParam(new Param("Type_id",         getValue(source, "Type_id")));
        data.addParam(new Param("Status_id",       getValue(source, "Status_id")));
        data.addParam(new Param("Address_id",      getValue(source, "Address_id")));
        data.addParam(new Param("WorkSchedule_id", getValue(source, "WorkSchedule_id")));
        data.addParam(new Param("IsMainBranch",    getValue(source, "IsMainBranch")));
        data.addParam(new Param("companyLegalUnit",getValue(source, "companyLegalUnit")));
        data.addParam(new Param("isMobile",        getValue(source, "isMobile")));
        data.addParam(new Param("softdeleteflag",  getValue(source, "softdeleteflag")));
        data.addParam(new Param("createdts",       getValue(source, "createdts")));
        data.addParam(new Param("lastmodifiedts",  getValue(source, "lastmodifiedts")));

        finalResult.addRecord(data);
        logger.info("getLocationById: returned location id={}", getValue(source, "id"));

        return finalResult;
    }

    // Tránh NullPointerException khi record thiếu field, trả "" thay vì null
    private String getValue(Record record, String paramName) {
        Param param = record.getParamByName(paramName);
        return (param != null) ? param.getValue() : "";
    }
}