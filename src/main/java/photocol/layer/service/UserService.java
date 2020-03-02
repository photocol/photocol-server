package photocol.layer.service;

import photocol.definitions.User;
import photocol.definitions.response.StatusResponse;
import photocol.layer.store.UserStore;

import static photocol.definitions.response.StatusResponse.Status.*;

public class UserService {

    private UserStore userStore;

    public UserService(UserStore userStore) {
        this.userStore = userStore;
    }

    public StatusResponse<Integer> signUp(User user) {
        if (userStore.checkIfUserExists(user.email).status() == STATUS_USER_NOT_FOUND)
            return userStore.createUser(user.email, user.username, user.passwordHash);
        return new StatusResponse<>(STATUS_CREDENTIALS_NOT_UNIQUE);
    }

    public StatusResponse<Integer> logIn(User user) {
        if(userStore.checkIfUserExists(user.email).status() == STATUS_USER_NOT_FOUND)
            return new StatusResponse<>(STATUS_USER_NOT_FOUND);
        return userStore.checkCredentials(user.email, user.passwordHash);
    }

}
