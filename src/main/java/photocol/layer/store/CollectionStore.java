package photocol.layer.store;

import photocol.definitions.PhotoCollection;
import photocol.definitions.User;
import photocol.definitions.response.StatusResponse;

public class CollectionStore {

    public void createCollection(User user, String name) { }

    public StatusResponse<PhotoCollection> getCollection(User username, String collectionName) { return null; }
    public StatusResponse<PhotoCollection> getCollection(int collectionId) { return null; }

    public StatusResponse deleteCollection(User user, String collectionName) { return null; }

}
