package photocol.layer.service;

import photocol.definitions.ACLEntry;
import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.store.CollectionStore;
import photocol.layer.store.PhotoStore;
import photocol.layer.store.UserStore;

import java.util.List;

import static photocol.definitions.exception.HttpMessageException.Error.*;

public class CollectionService {

    private CollectionStore collectionStore;
    private PhotoStore photoStore;
    private UserStore userStore;
    public CollectionService(CollectionStore collectionStore, PhotoStore photoStore, UserStore userStore) {
        this.collectionStore = collectionStore;
        this.photoStore = photoStore;
        this.userStore = userStore;
    }

    /**
     * List collections that a user has access to (simple passthrough)
     * @param uid       user uid
     * @param username  user username
     * @return          list of PhotoCollection objects that user has access to
     * @throws HttpMessageException on failure
     */
    public List<PhotoCollection> getUserCollections(int uid, String username) throws HttpMessageException {
        return collectionStore.getUserCollections(uid, username);
    }

    /**
     * Create collection (simple passthrough)
     * @param uid           uid of owner
     * @param collection    collection name
     * @return              true on success
     * @throws HttpMessageException on failure
     */
    public boolean createCollection(int uid, PhotoCollection collection) throws HttpMessageException {
        // make sure collection name is unique
        // TODO: remove
//        if(collectionStore.checkIfCollectionExists(uid, uid, collection.uri).status()==STATUS_OK)
//            return new StatusResponse<>(STATUS_COLLECTION_NAME_NOT_UNIQUE);
        if(collectionStore.checkIfCollectionExists(uid, uid, collection.uri)!=-1)
            throw new HttpMessageException(401, COLLECTION_NAME_NOT_UNIQUE);

        return collectionStore.createCollection(uid, collection);
    }

    /**
     * List photos in collection
     * @param uid               viewer's uid
     * @param collectionUri     collection uri
     * @param collectionOwner   collection owner username
     * @return                  list of photos in collection on success
     * @throws HttpMessageException on failure
     */
    public List<Photo> getCollection(int uid, String collectionUri, String collectionOwner)
            throws HttpMessageException {
        // get uid
        // TODO: remove
//        if((status=userStore.getUid(collectionOwner)).status()!=STATUS_OK)
//            return new StatusResponse<>(status.status());
        int collectionOwnerUid = userStore.getUid(collectionOwner);

        // make sure collection exists
        // TODO: remove
//        if((status=collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri)).status()!=STATUS_OK)
//            return new StatusResponse<>(status.status());
//        int cid = status.payload();
        int cid = collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri);
        if(cid==-1)
            throw new HttpMessageException(404, COLLECTION_NOT_FOUND);

        // make sure user has access to collection
        collectionStore.getUserCollectionRole(uid, cid);
        // TODO: remove
//        if(collectionStore.getUserCollectionRole(uid, cid).status()!=STATUS_OK)
//            return new StatusResponse<>(status.status());

        // get list of images in collection
        return collectionStore.getCollectionPhotos(cid);
    }

    /**
     * Add image to collection
     * @param uid               current uid
     * @param collectionUri     collection uri
     * @param collectionOwner   collection owner (username)
     * @param imageuri          image uri
     * @return                  true on success
     * @throws HttpMessageException on failure
     */
    public boolean addPhoto(int uid, String collectionUri, String collectionOwner, String imageuri)
            throws HttpMessageException {
        // TODO: remove
//        if((status=userStore.getUid(collectionOwner)).status()!=STATUS_OK)
//            return status;
//        int collectionOwnerUid = status.payload();
        int collectionOwnerUid = userStore.getUid(collectionOwner);

        // TODO: get cid and user role in collection in one query to reduce number of queries
        // get cid of collection, make sure it exists
        // TODO: remove
//        if((status=collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri)).status()!=STATUS_OK)
//            return status;
//        int cid = status.payload();
        int cid = collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri);
        if(cid==-1)
            throw new HttpMessageException(404, COLLECTION_NOT_FOUND);

        // get user role in collection
        // TODO: remove
//        if((status=collectionStore.getUserCollectionRole(uid, cid)).status()!=STATUS_OK)
//            return status;
        int userRole = collectionStore.getUserCollectionRole(uid, cid);

        // checking edit permissions
        ACLEntry.Role role = ACLEntry.Role.fromInt(userRole);
        if(role!= ACLEntry.Role.ROLE_OWNER && role!= ACLEntry.Role.ROLE_EDITOR)
            throw new HttpMessageException(401, INSUFFICIENT_COLLECTION_PERMISSIONS);

        // get image pid
        int pid = photoStore.checkPhotoPermissions(imageuri, uid);

        return collectionStore.addImage(cid, pid);
    }

    /**
     * Update collection attributes or acl list
     * @param uid               uid of editor
     * @param collectionUri     collection uri
     * @param collectionOwner   collection owner username
     * @param photoCollection   desired parameters to change
     * @return                  true on success
     * @throws HttpMessageException on failure
     */
    public boolean update(int uid, String collectionUri, String collectionOwner, PhotoCollection photoCollection)
            throws HttpMessageException {
        // TODO: remove
//        if((status=userStore.getUid(collectionOwner)).status()!=STATUS_OK)
//            return status;
//        int collectionOwnerUid = status.payload();
        int collectionOwnerUid = userStore.getUid(collectionOwner);

        // TODO: these first three stages are exactly the same as above -- make more DRY by putting in separate service fn
//        if((status=collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri)).status()!=STATUS_OK)
//            return status;
//        int cid = status.payload();
        int cid = collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri);
        if(cid==-1)
            throw new HttpMessageException(404, COLLECTION_NOT_FOUND);

        // make sure new uri is unique for the current user, if applicable
        if(photoCollection.name!=null)
            if(collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, photoCollection.uri)!=-1)
                throw new HttpMessageException(401, COLLECTION_NAME_NOT_UNIQUE);
                // TODO: remove
//            if((status=collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, photoCollection.uri)).status()==STATUS_OK)
//                return new StatusResponse(STATUS_COLLECTION_NAME_NOT_UNIQUE);

        // get user role in collection
//        if((status=collectionStore.getUserCollectionRole(uid, cid)).status()!=STATUS_OK)
//            return status;
        int userRole = collectionStore.getUserCollectionRole(uid, cid);

        // checking edit permissions
        ACLEntry.Role role = ACLEntry.Role.fromInt(userRole);
        if(role != ACLEntry.Role.ROLE_OWNER)
            // TODO: remove
//            return new StatusResponse(STATUS_INSUFFICIENT_COLLECTION_PERMISSIONS);
            throw new HttpMessageException(401, INSUFFICIENT_COLLECTION_PERMISSIONS);

        // TODO: enforce strict checks on ACL list

        // get list of uids from usernames
        for(ACLEntry entry : photoCollection.aclList) {
            // TODO: remove
//             if((status=userStore.getUid(entry.username)).status()!=STATUS_OK)
//                 return new StatusResponse(STATUS_USER_NOT_FOUND);
//             entry.setUid(status.payload());
            entry.setUid(userStore.getUid(entry.username));
        }

        // update collection with parameters
        return collectionStore.update(cid, photoCollection);
    }
}
