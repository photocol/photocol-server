package photocol.layer.service;

import photocol.definitions.ACLEntry;
import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.store.CollectionStore;
import photocol.layer.store.PhotoStore;
import photocol.layer.store.UserStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if(collectionStore.checkIfCollectionExists(uid, uid, collection.uri)!=-1)
            throw new HttpMessageException(401, COLLECTION_NAME_NOT_UNIQUE);

        return collectionStore.createCollection(uid, collection);
    }

    /**
     * Get collection details and photos
     * @param uid               viewer's uid
     * @param collectionUri     collection uri
     * @param collectionOwner   collection owner username
     * @return                  photocollection object with photos and details
     * @throws HttpMessageException on failure
     */
    public PhotoCollection getCollection(int uid, String collectionUri, String collectionOwner)
            throws HttpMessageException {
        // get uid
        int collectionOwnerUid = userStore.getUid(collectionOwner);

        // make sure collection exists
        int cid = collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri);
        if(cid==-1)
            throw new HttpMessageException(404, COLLECTION_NOT_FOUND);

        // make sure user has access to collection
        collectionStore.getUserCollectionRole(uid, cid);

        // get list of images in collection
        return collectionStore.getCollection(cid);
    }

    /**
     * Add image to collection
     * @param uid               current uid
     * @param collectionUri     collection uri
     * @param collectionOwner   collection owner (username)
     * @param imageuri          image uri
     * @param isAdd             true if adding photo, false for removing
     * @return                  true on success
     * @throws HttpMessageException on failure
     */
    public boolean addRemovePhoto(int uid, String collectionUri, String collectionOwner, String imageuri, boolean isAdd)
            throws HttpMessageException {
        int collectionOwnerUid = userStore.getUid(collectionOwner);

        // TODO: get cid and user role in collection in one query to reduce number of queries
        // get cid of collection, make sure it exists
        int cid = collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri);
        if(cid==-1)
            throw new HttpMessageException(404, COLLECTION_NOT_FOUND);

        // get user role in collection
        int userRole = collectionStore.getUserCollectionRole(uid, cid);

        // checking edit permissions
        ACLEntry.Role role = ACLEntry.Role.fromInt(userRole);
        if(role!= ACLEntry.Role.ROLE_OWNER && role!= ACLEntry.Role.ROLE_EDITOR)
            throw new HttpMessageException(401, INSUFFICIENT_COLLECTION_PERMISSIONS);

        // get image pid; only image owner can add or remove photo
        // TODO: allow collection owner to remove any photo from collection
        int pid = photoStore.checkPhotoPermissions(imageuri, uid, true);

        return collectionStore.addRemoveImage(cid, pid, isAdd);
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
        int collectionOwnerUid = userStore.getUid(collectionOwner);

        // TODO: these first three stages are exactly the same as above -- make more DRY by putting in separate service fn
        int cid = collectionStore.checkIfCollectionExists(uid, collectionOwnerUid, collectionUri);
        if(cid==-1)
            throw new HttpMessageException(404, COLLECTION_NOT_FOUND);

        // get user role in collection
        int userRole = collectionStore.getUserCollectionRole(uid, cid);

        // checking edit permissions
        ACLEntry.Role role = ACLEntry.Role.fromInt(userRole);
        if(role != ACLEntry.Role.ROLE_OWNER)
            throw new HttpMessageException(401, INSUFFICIENT_COLLECTION_PERMISSIONS);

        // checking acl list
        int newOwnerUid = uid;
        if(photoCollection.aclList.size() > 0) {
            // validating all acl list changes
            // this checks that no duplicates are added, that no deletions of non-existant users are performed, and
            // that the current user has sufficient permissions to perform deletion actions
            List<ACLEntry> aclList = collectionStore.getAclList(cid);
            Map<Integer, ACLEntry.Role> aclMap = new HashMap<>();
            for(ACLEntry acl : aclList)
                aclMap.put(acl.uid, acl.role);

            for(ACLEntry entry : photoCollection.aclList) {
                entry.setUid(userStore.getUid(entry.username));

                // check that role is not self -- i.e., not current owner
                // current owner can only change acl list of other users
                // to leave, must promote another user to owner and then leave using normal leave collection operation
                if(entry.uid==uid)
                    throw new HttpMessageException(400, ILLEGAL_ACL_ACTION, "CANNOT PERFORM ACL ACTION ON SELF");

                // duplicate acl action on one user not allowed
                if(entry.operation!=null)
                    throw new HttpMessageException(400, ILLEGAL_ACL_ACTION, "DUPLICATE OPERATIONS ON " + entry.username);

                // promoting someone else to owner, will automatically demote self
                if(entry.role==ACLEntry.Role.ROLE_OWNER) {
                    // check that no other role is also set to owner
                    for(ACLEntry ownerCheck : photoCollection.aclList)
                        if(ownerCheck.role==ACLEntry.Role.ROLE_OWNER && ownerCheck.username!=entry.username)
                            throw new HttpMessageException(400, ILLEGAL_ACL_ACTION, "MULTIPLE OWNERS SET");

                    entry.setOperation(aclMap.containsKey(entry.uid)
                            ? ACLEntry.ACLOperation.OP_UPDATE_OWNER
                            : ACLEntry.ACLOperation.OP_INSERT_OWNER);
                    aclMap.put(entry.uid, entry.role);
                    newOwnerUid = entry.uid;
                }

                // removing user, checks if already exists in acl list
                if(entry.role==ACLEntry.Role.ROLE_NONE) {
                    if(!aclMap.containsKey(entry.uid))
                        throw new HttpMessageException(400, ILLEGAL_ACL_ACTION, "REMOVING USER NOT IN COLLECTION");

                    entry.setOperation(ACLEntry.ACLOperation.OP_DELETE);
                    aclMap.put(entry.uid, entry.role);
                }

                // setting user to another role
                if(entry.role==ACLEntry.Role.ROLE_VIEWER || entry.role==ACLEntry.Role.ROLE_EDITOR) {
                    // (does't check if role doesn't change, no problem if it doesn't)
                    entry.setOperation(aclMap.containsKey(entry.uid)
                            ? ACLEntry.ACLOperation.OP_UPDATE
                            : ACLEntry.ACLOperation.OP_INSERT);
                    aclMap.put(entry.uid, entry.role);
                }

                // shouldn't happen, just a check that the above conditionals cover all possible cases
                if(entry.operation==null)
                    throw new HttpMessageException(400, ILLEGAL_ACL_ACTION, "UNRECOGNIZED ACL ACTION");
            }
        }

        // make sure new uri is unique for the current user (if owner) or new owner, if applicable
        if(photoCollection.name!=null || newOwnerUid!=uid)
            if(collectionStore.checkIfCollectionExists(newOwnerUid, newOwnerUid, collectionUri)!=-1)
                throw new HttpMessageException(401, COLLECTION_NAME_NOT_UNIQUE);

        // update collection with parameters
        return collectionStore.update(cid, photoCollection, uid);
    }

    /**
     * Delete a collection and all images within it
     * @param uid           uid of owner
     * @param collectionUri collection uri
     * @return              true on success
     * @throws HttpMessageException on failure
     */
    public boolean deleteCollection(int uid, String collectionUri) throws HttpMessageException {

        int cid = collectionStore.checkIfCollectionExists(uid, uid, collectionUri);
        if(cid==-1)
            throw new HttpMessageException(401, COLLECTION_NOT_FOUND);

        ACLEntry.Role role = ACLEntry.Role.fromInt(collectionStore.getUserCollectionRole(uid, cid));
        if(role != ACLEntry.Role.ROLE_OWNER)
            throw new HttpMessageException(401, INSUFFICIENT_COLLECTION_PERMISSIONS, "NOT COLLECTION OWNER");

        return collectionStore.deleteCollection(cid);
    }
}
