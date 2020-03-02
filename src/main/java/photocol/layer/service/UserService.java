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

        StatusResponse<Integer> status= userDB.checkIfUserExists(user.email);
        if (status == STATUS_USER_NOT_FOUND) {
            status=userDB.signUp(user.email, user.username, user.passwordHash)).status()
            //create users
            if (status == STATUS_USER_CREATED) {
                return STATUS_OK;
            }
            else {
                return status
            }
            //TODO change the status code
        }
        else if {
            System.out.println("Email already used");
            return STATUS_CREDENTIALS_NOT_UNIQUE;
            //front end stuff
        }

    }

    public Status logIn(User user) {
        return STATUS_OK;
    }

}
