package photocol.definitions.exception;

public class HttpMessageException extends Exception {

    private final int status;
    private final String message;

    public HttpMessageException(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int status() {
        return this.status;
    }

    public String message() {
        return this.message;
    }
}
