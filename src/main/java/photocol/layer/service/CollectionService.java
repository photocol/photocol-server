package photocol.layer.service;

import photocol.definitions.ACLEntry;
import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.response.StatusResponse;
import photocol.layer.store.CollectionStore;
import photocol.layer.store.PhotoStore;

import java.util.List;

import static photocol.definitions.response.StatusResponse.Status.*;

public class CollectionService {

    private CollectionStore collectionStore;
    private PhotoStore photoStore;
    public CollectionService(CollectionStore collectionStore, PhotoStore photoStore){
        this.collectionStore = collectionStore;
        this.photoStore = photoStore;
    }

    // list collections that user has access to: passthrough
    public StatusResponse<List<PhotoCollection>> getUserCollections(int uid) {
        return collectionStore.getUserCollections(uid);
    }

    // create collection: passthrough
    public StatusResponse createCollection(int uid, PhotoCollection collection) {
        // make sure collection name is unique
        if(collectionStore.checkIfCollectionExists(uid, collection.uri).status()==STATUS_OK)
            return new StatusResponse<>(STATUS_COLLECTION_NAME_NOT_UNIQUE);

        return collectionStore.createCollection(uid, collection);
    }

    // list items in collection
    public StatusResponse<List<Photo>> getCollection(int uid, String collectionUri) {
        // make sure collection exists
        StatusResponse<Integer> status;
        if((status=collectionStore.checkIfCollectionExists(uid, collectionUri)).status()!=STATUS_OK)
            return new StatusResponse<>(status.status());
        int cid = status.payload();

        // make sure user has access to collection
        if(collectionStore.getUserCollectionRole(uid, cid).status()!=STATUS_OK)
            return new StatusResponse<>(status.status());

        // get list of images in collection
        return collectionStore.getCollectionPhotos(cid);
    }

    // add image to collection
    public StatusResponse addImage(int uid, String collectionUri, String imageuri) {
        // TODO: get cid and user role in collection in one query to reduce number of queries
        // get cid of collection, make sure it exists
        StatusResponse<Integer> status;
        if((status=collectionStore.checkIfCollectionExists(uid, collectionUri)).status()!=STATUS_OK)
            return status;
        int cid = status.payload();

        // get user role in collection
        if((status=collectionStore.getUserCollectionRole(uid, cid)).status()!=STATUS_OK)
            return status;

        // checking edit permissions
        ACLEntry.Role role = ACLEntry.Role.fromInt(status.payload());
        if(role!= ACLEntry.Role.ROLE_OWNER && role!= ACLEntry.Role.ROLE_EDITOR)
            return new StatusResponse(STATUS_INSUFFICIENT_COLLECTION_PERMISSIONS);

        // get image pid
        if((status=photoStore.checkPhotoPermissions(imageuri, uid)).status()!=STATUS_OK)
            return status;

        // add image to collection
        int pid = status.payload();
        return collectionStore.addImage(cid, pid);
    }
}
