package photocol.definitions.exception;

public class HttpMessageException extends Exception {

    private final int status;
    private final String error;
    private final String details;

    public enum Error {
        UNAUTHORIZED_ORIGIN,
        INPUT_FORMAT_ERROR,
        DATABASE_QUERY_ERROR,
        S3_ERROR,
        CREDENTIALS_NOT_UNIQUE,
        USER_FOUND,
        USER_NOT_FOUND,
        CREDENTIALS_INVALID,
        NOT_LOGGED_IN,
        LOGGED_IN,
        COLLECTION_NAME_INVALID,
        COLLECTION_NAME_NOT_UNIQUE,
        INSUFFICIENT_COLLECTION_PERMISSIONS,
        INVALID_COLLECTION_DESCRIPTION,
        COLLECTION_NOT_FOUND,
        IMAGE_MIMETYPE_INVALID,
        IMAGE_NAME_INVALID,
        IMAGE_DESCRIPTION_INVALID,
        IMAGE_NOT_FOUND,
        IMAGE_EXISTS_IN_COLLECTION,
        ILLEGAL_ACL_ACTION,
        NOT_PHOTO_OWNER
    }

    public HttpMessageException(int status, Error error, String details) {
        this.status = status;
        this.error = error.name();
        this.details = details;
    }

    public HttpMessageException(int status, Error error) {
        this(status, error, null);
    }

    public int status() {
        return this.status;
    }

    public String error() {
        return this.error;
    }

    public String details() {
        return this.details;
    }
}
