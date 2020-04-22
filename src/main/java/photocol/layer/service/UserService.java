package photocol.layer.service;

import photocol.definitions.User;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.store.UserStore;

import static photocol.definitions.exception.HttpMessageException.Error.*;

public class UserService {

    private UserStore userStore;

    public UserService(UserStore userStore) {
        this.userStore = userStore;
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

}
