package photocol.layer.service;

import photocol.definitions.ACLEntry;
import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.exception.HttpMessageException;
import photocol.definitions.response.StatusResponse;
import photocol.layer.store.CollectionStore;
import photocol.layer.store.PhotoStore;
import photocol.layer.store.UserStore;

import java.util.List;

import static photocol.definitions.response.StatusResponse.Status.*;

public class CollectionService {

    private CollectionStore collectionStore;
    private PhotoStore photoStore;
    private UserStore userStore;
    public CollectionService(CollectionStore collectionStore, PhotoStore photoStore, UserStore userStore) {
        this.collectionStore = collectionStore;
        this.photoStore = photoStore;
        this.userStore = userStore;
    }

    // list collections that user has access to: passthrough
    public StatusResponse<List<PhotoCollection>> getUserCollections(int uid, String username) {
        return collectionStore.getUserCollections(uid, username);
    }

    // create collection: passthrough
    public StatusResponse createCollection(int uid, PhotoCollection collection) {
        // make sure collection name is unique
        if(collectionStore.checkIfCollectionExists(uid, uid, collection.uri).status()==STATUS_OK)
            return new StatusResponse<>(STATUS_COLLECTION_NAME_NOT_UNIQUE);

        return collectionStore.createCollection(uid, collection);
    }

    // list items in collection
    public StatusResponse<List<Photo>> getCollection(int uid, String collectionUri, String collectionOwner)
            throws HttpMessageException {
        StatusResponse<Integer> status;

        // get uid
        // TODO: remove
//        if((status=userStore.getUid(collectionOwner)).status()!=STATUS_OK)
//            return new StatusResponse<>(status.status());
        int collectionOwnerUid = userStore.getUid(collectionOwner);

        // make sure collection exists
        if((status=collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri)).status()!=STATUS_OK)
            return new StatusResponse<>(status.status());
        int cid = status.payload();

        // make sure user has access to collection
        if(collectionStore.getUserCollectionRole(uid, cid).status()!=STATUS_OK)
            return new StatusResponse<>(status.status());

        // get list of images in collection
        return collectionStore.getCollectionPhotos(cid);
    }

    // add image to collection
    public StatusResponse addPhoto(int uid, String collectionUri, String collectionOwner, String imageuri)
            throws HttpMessageException {
        StatusResponse<Integer> status;
        // TODO: remove
//        if((status=userStore.getUid(collectionOwner)).status()!=STATUS_OK)
//            return status;
//        int collectionOwnerUid = status.payload();
        int collectionOwnerUid = userStore.getUid(collectionOwner);

        // TODO: get cid and user role in collection in one query to reduce number of queries
        // get cid of collection, make sure it exists
        if((status=collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri)).status()!=STATUS_OK)
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

    // update collection
    public StatusResponse update(int uid, String collectionUri, String collectionOwner, PhotoCollection photoCollection)
            throws HttpMessageException {
        StatusResponse<Integer> status;
        // TODO: remove
//        if((status=userStore.getUid(collectionOwner)).status()!=STATUS_OK)
//            return status;
//        int collectionOwnerUid = status.payload();
        int collectionOwnerUid = userStore.getUid(collectionOwner);

        // these first three stages are exactly the same as above -- make more DRY by putting in separate service fn
        if((status=collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri)).status()!=STATUS_OK)
            return status;
        int cid = status.payload();

        // make sure new uri is unique for the current user, if applicable
        if(photoCollection.name!=null)
            if((status=collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, photoCollection.uri)).status()==STATUS_OK)
                return new StatusResponse(STATUS_COLLECTION_NAME_NOT_UNIQUE);

        // get user role in collection
        if((status=collectionStore.getUserCollectionRole(uid, cid)).status()!=STATUS_OK)
            return status;

        // checking edit permissions
        ACLEntry.Role role = ACLEntry.Role.fromInt(status.payload());
        if(role != ACLEntry.Role.ROLE_OWNER)
            return new StatusResponse(STATUS_INSUFFICIENT_COLLECTION_PERMISSIONS);

        // TODO: enforce strict checks on ACL list

        // get list of uids from usernames
        for(ACLEntry entry : photoCollection.aclList) {
            // TODO: rename
//             if((status=userStore.getUid(entry.username)).status()!=STATUS_OK)
//                 return new StatusResponse(STATUS_USER_NOT_FOUND);
//             entry.setUid(status.payload());
            entry.setUid(userStore.getUid(entry.username));
        }

        // update collection with parameters
        return collectionStore.update(cid, photoCollection);
    }
}
