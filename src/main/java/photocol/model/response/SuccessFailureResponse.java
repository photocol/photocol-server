package photocol.model.response;

public class SuccessFailureResponse {
    boolean success;
    Object payload;

    public SuccessFailureResponse(boolean success, Object payload) {
        this.success =  success;
        this.payload = payload;
    }
    public SuccessFailureResponse(boolean success) {
        this.success = success;
        this.payload = null;
    }
}
