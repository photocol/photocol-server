package photocol.definitions.response;

// return a status code with an optional payload
public class StatusResponse<T> {
    private Status status;
    private T payload;

    // this class to use error codes semantically
    // TODO: define many possible errors; tightly coupled to endpoints, so need to work on that as well
    public enum Status {
        STATUS_OK(0),
        STATUS_MISC(1),
        STATUS_HTTP_ERROR(2),
        STATUS_CREDENTIALS_NOT_UNIQUE(100),
        STATUS_USER_FOUND(101),
        STATUS_USER_NOT_FOUND(102),
        STATUS_USER_INVALID(103),
        STATUS_CREDENTIALS_INVALID(104),
        STATUS_NOT_LOGGED_IN(105),
        STATUS_LOGGED_IN(106),
        STATUS_USER_CREATED(107),
        STATUS_USER_NOT_CREATED(108),
        STATUS_COLLECTION_NAME_INVALID(200),
        STATUS_COLLECTION_NAME_NOT_UNIQUE(201),
        STATUS_INSUFFICIENT_COLLECTION_PERMISSIONS(202),
        STATUS_INVALID_COLLECTION_DESCRIPTION(203),
        STATUS_COLLECTION_NOT_FOUND(204),       // TODO: add this to wiki
        STATUS_IMAGE_MIMETYPE_INVALID(303),
        STATUS_IMAGE_NAME_INVALID(304),
        STATUS_IMAGE_DESCRIPTION_INVALID(305),
        STATUS_IMAGE_NOT_FOUND(306),
        STATUS_IMAGE_EXISTS_IN_COLLECTION(307);

        private int intStatus;
        Status(int intStatus) {
            this.intStatus = intStatus;
        }
        public int toInt() {
            return this.intStatus;
        };
    };

    public StatusResponse(Status status, T payload) {
        this.status = status;
        this.payload = payload;
    }
    public StatusResponse(Status status) {
        this.status = status;
        this.payload = null;
    }

    public Status status() {
        return status;
    }
    public T payload() {
        return status()==Status.STATUS_OK ? this.payload : null;
    }
}
