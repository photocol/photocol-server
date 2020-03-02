package photocol.layer.store;

import photocol.definitions.response.StatusResponse;

import static photocol.definitions.response.StatusResponse.Status.*;

public class PhotoStore {

    // FIXME: dummy code; implement this
    public StatusResponse checkIfPhotoExists(String uri) {
        return new StatusResponse(STATUS_MISC);
    }

    // FIXME: dummy code; implement this
    public StatusResponse createImage(String uri, int uid) {
        return new StatusResponse(STATUS_IMAGE_NAME_INVALID);
    }
}
