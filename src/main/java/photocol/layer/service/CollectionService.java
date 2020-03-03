package photocol.layer.service;

import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.response.StatusResponse;
import photocol.layer.store.CollectionStore;

import java.util.List;

import static photocol.definitions.response.StatusResponse.Status.*;

public class CollectionService {

    private CollectionStore collectionStore;
    public CollectionService(CollectionStore collectionStore){
        this.collectionStore = collectionStore;
    }

    // create collection: passthrough
    public StatusResponse createCollection(int uid, PhotoCollection collection) {
        // make sure collection name is unique
        if(collectionStore.checkIfCollectionExists(uid, collection.name).status()==STATUS_OK)
            return new StatusResponse<>(STATUS_COLLECTION_NAME_NOT_UNIQUE);

        return collectionStore.createCollection(uid, collection);
    }

    // list items in collection
    public StatusResponse<List<Photo>> getCollection(int uid, String collectionName) {
        // make sure collection exists
        StatusResponse<Integer> status;
        if((status=collectionStore.checkIfCollectionExists(uid, collectionName)).status()!=STATUS_OK)
            return new StatusResponse<>(status.status());
        int cid = status.payload();

        // make sure user has access to collection
        if(collectionStore.getUserCollectionRole(uid, cid).status()!=STATUS_OK)
            return new StatusResponse<>(status.status());

        // get list of images in collection
        return collectionStore.getCollectionPhotos(uid, cid);
    }
}
