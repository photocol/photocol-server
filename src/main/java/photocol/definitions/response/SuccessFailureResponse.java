package photocol.definitions.response;

// phase this out for StatusResponse -- more detail than simply boolean
public class SuccessFailureResponse<T> {
    boolean success;
    T payload;

    public SuccessFailureResponse(boolean success, T payload) {
        this.success =  success;
        this.payload = payload;
    }
    public SuccessFailureResponse(boolean success) {
        this.success = success;
        this.payload = null;
    }
}
