package photocol.layer.service;

import photocol.definitions.User;
import photocol.definitions.request.EndpointRequestModel;
import static photocol.definitions.response.StatusResponse.Status;

import photocol.layer.DataBase.UserDB;
import photocol.layer.store.UserStore;

import static photocol.definitions.response.StatusResponse.Status.*;

public class UserService {

    private UserDB userDB;

    public UserService(UserDB userDB) {
        this.userDB = userDB;
    }

    public Status signUp(User user) {
        if (userDB.checkIfUserExists(user.email) == STATUS_USER_NOT_FOUND) {
            //create users
            if (userDB.signUp(user.email, user.username, user.passwordHash) == STATUS_USER_CREATED) {

            } else if (userDB.signUp(user.email, user.username, user.passwordHash) == STATUS_USER_NOT_CREATED) {

            }

            //TODO change the status code
        } else if (userDB.checkIfUserExists(user.email) == STATUS_USER_FOUND) {
            System.out.println("Email already used");
            //front end stuff
        }
        return STATUS_OK;
    }

    public Status logIn(User user) {
        return STATUS_OK;
    }
}
