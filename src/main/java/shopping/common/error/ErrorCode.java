package shopping.common.error;

public enum ErrorCode {
    INVALID_REQUEST("요청 값이 올바르지 않습니다."),
    ROUTE_NOT_FOUND("요청 경로를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED("지원하지 않는 HTTP 메서드입니다."),
    NOT_ACCEPTABLE("지원하지 않는 응답 형식입니다."),
    UNSUPPORTED_MEDIA_TYPE("지원하지 않는 본문 형식입니다."),
    EMAIL_ALREADY_EXISTS("이미 가입된 이메일입니다."),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
