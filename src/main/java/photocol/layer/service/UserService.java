package photocol.layer.service;

import photocol.definitions.ACLEntry;
import photocol.definitions.PhotoCollection;
import photocol.definitions.User;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.store.CollectionStore;
import photocol.layer.store.PhotoStore;
import photocol.layer.store.UserStore;

import java.util.List;
import java.util.stream.Collectors;

import static photocol.definitions.exception.HttpMessageException.Error.*;

public class UserService {

    private UserStore userStore;
    private PhotoStore photoStore;
    private CollectionStore collectionStore;

    public UserService(UserStore userStore, PhotoStore photoStore, CollectionStore collectionStore) {
        this.userStore = userStore;
        this.photoStore = photoStore;
        this.collectionStore = collectionStore;
    }

    /**
     * Attempt to sign up a user
     * @param user  User object from sign up form
     * @return      Newly-created user uid
     * @throws HttpMessageException on failure
     */
    public int signUp(User user) throws HttpMessageException {
        return userStore.createUser(user);
    }

    /**
     * Attempt to log in a user
     * @param user  User object from sign in form
     * @return      User uid
     * @throws HttpMessageException on failure
     */
    public int logIn(User user) throws HttpMessageException {
        if(!userStore.checkIfUserExists(user.username))
            throw new HttpMessageException(401, USER_NOT_FOUND);
        return userStore.checkCredentials(user);
    }

    /**
     * Get a user's profile, including collections
     * @param username  username of profile to fetch
     * @return          user object with user details on success
     * @throws HttpMessageException
     */
    public User getProfile(String username) throws HttpMessageException {
        User user = this.userStore.getProfile(username);

        // get collections and filter accordingly
        // show public/discoverable collections that the user owns
        int uid = this.userStore.getUid(username);
        List<PhotoCollection> photoCollections = this.collectionStore.getUserCollections(uid, username)
                .stream()
                .filter(collection -> {
                    // only get public/discoverable
                    if(collection.isPublic==0)
                        return false;

                    // only get collections the user owns
                    for(ACLEntry aclEntry : collection.aclList)
                        if(aclEntry.username.equals(username) && aclEntry.role==ACLEntry.Role.ROLE_OWNER)
                            return true;
                    return false;
                })
                .collect(Collectors.toList());
        user.collections = photoCollections;

        return user;
    }

    /**
     * Update a user's profile
     * @param fields    fields of user to update
     * @param uid       user uid
     * @return          true on success
     * @throws HttpMessageException on failure
     */
    public boolean update(User fields, int uid) throws HttpMessageException {
        // right now, can only update display name and profile photo

        // check that profile photo exists and belongs to user (if photo is set)
        if(fields.profilePhoto!=null) {
            int pid = this.photoStore.checkPhotoPermissions(fields.profilePhoto, uid, true);
            fields.profilePhotoPid = pid;
        }

        return this.userStore.update(fields, uid);
    }

}
