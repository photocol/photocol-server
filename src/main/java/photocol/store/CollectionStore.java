package photocol.store;

import photocol.model.PhotoCollection;
import photocol.model.User;
import photocol.model.response.StatusResponse;

public class CollectionStore {

    public void createCollection(User user, String name) { }

    public StatusResponse<PhotoCollection> getCollection(User username, String collectionName) { return null; }
    public StatusResponse<PhotoCollection> getCollection(int collectionId) { return null; }

    public StatusResponse deleteCollection(User user, String collectionName) { return null; }

}
