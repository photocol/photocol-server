package photocol.definitions.response;

// return a status code with an optional payload
public class StatusResponse<T> {
    private Status status;
    private T payload;

    // this class to use error codes semantically
    // TODO: define many possible errors; tightly coupled to endpoints, so need to work on that as well
    public enum Status {
        STATUS_OK(0);

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
        return status()==Status.STATUS_OK ? null : this.payload;
    }
}
