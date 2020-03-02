package photocol.layer.service;

import photocol.definitions.User;
import photocol.definitions.request.EndpointRequestModel;
import static photocol.definitions.response.StatusResponse.Status;

import photocol.definitions.response.StatusResponse;
import photocol.layer.DataBase.UserDB;
import photocol.layer.store.UserStore;

import static photocol.definitions.response.StatusResponse.Status.*;

public class UserService {

    private UserDB userDB;

    public UserService(UserDB userDB) {
        this.userDB = userDB;
    }

    public StatusResponse<Integer> signUp(User user) {
        if (userDB.checkIfUserExists(user.email).status() == STATUS_USER_NOT_FOUND)
            return userDB.signUp(user.email, user.username, user.passwordHash);
        else
            return new StatusResponse<>(STATUS_CREDENTIALS_NOT_UNIQUE);
    }

    public Status logIn(User user) {
        return STATUS_OK;
    }

}
