package photocol.layer.service;

import photocol.definitions.User;
import photocol.definitions.response.StatusResponse;
import photocol.layer.DataBase.UserDB;

import static photocol.definitions.response.StatusResponse.Status.*;

public class UserService {

    private UserDB userDB;

    public UserService(UserDB userDB) {
        this.userDB = userDB;
    }

    public StatusResponse<Integer> signUp(User user) {
        if (userDB.checkIfUserExists(user.email).status() == STATUS_USER_NOT_FOUND)
            return userDB.createUser(user.email, user.username, user.passwordHash);
        return new StatusResponse<>(STATUS_CREDENTIALS_NOT_UNIQUE);
    }

    public StatusResponse<Integer> logIn(User user) {
        if(userDB.checkIfUserExists(user.email).status() == STATUS_USER_NOT_FOUND)
            return new StatusResponse<>(STATUS_USER_NOT_FOUND);
        return userDB.checkCredentials(user.email, user.passwordHash);
    }

}
