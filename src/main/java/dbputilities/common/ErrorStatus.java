package dbputilities.common;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ErrorStatus {

    // —— Xác thực / phân quyền ——
    AUTH_FAILED(401, "AUTH_FAILED", "Đăng nhập thất bại"),
    UNAUTHORIZED(401, "UNAUTHORIZED", "Chưa xác thực"),
    REFRESH_TOKEN_INVALID(401, "REFRESH_TOKEN_INVALID", "Refresh token không hợp lệ hoặc đã hết hạn"),
    FORBIDDEN(403, "FORBIDDEN", "Không đủ quyền"),

    // —— Dữ liệu đầu vào ——
    VALIDATION_ERROR(400, "VALIDATION_ERROR", "Dữ liệu không hợp lệ"),
    INVALID_ARGUMENT(400, "INVALID_ARGUMENT", "Tham số không hợp lệ"),

    // —— Nghiệp vụ / trạng thái ——
    ACCOUNT_NOT_DELETABLE(403, "ACCOUNT_NOT_DELETABLE", "Tài khoản không được phép xóa"),
    ACCOUNT_NOT_FOUND(404, "ACCOUNT_NOT_FOUND", "Không tìm thấy tài khoản"),
    ADDRESS_NOT_FOUND(404, "ADDRESS_NOT_FOUND", "Không tìm thấy địa chỉ"),
    LOCATION_NOT_FOUND(404, "LOCATION_NOT_FOUND", "Không tìm thấy chi nhánh / địa điểm"),
    ILLEGAL_STATE(409, "ILLEGAL_STATE", "Trạng thái không hợp lệ"),

    // —— Hệ thống ——
    INTERNAL_ERROR(500, "INTERNAL_ERROR", "Lỗi hệ thống không mong đợi");

    private static final Map<String, ErrorStatus> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(ErrorStatus::code, Function.identity()));

    private final int httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorStatus(int httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int httpStatus()         { return httpStatus; }
    public String code()            { return code; }
    public String defaultMessage()  { return defaultMessage; }

    public static Optional<ErrorStatus> resolve(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(BY_CODE.get(code));
    }
}