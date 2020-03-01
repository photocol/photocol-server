package photocol.layer.service;

import photocol.definitions.User;
import photocol.definitions.request.EndpointRequestModel;
import photocol.definitions.response.StatusResponse;
import photocol.layer.store.UserStore;

import static photocol.definitions.response.StatusResponse.Status.*;

public class UserService {
    //public UserService(UserStore dummy){

   // }
    public StatusResponse.Status signUp(User xxx){
        if (checkIfUserExists(User.email) == 102) {
            //create user;
        }
        else if (checkIfUserExists(User.email) == 101){
            System.out.println("Email already used");
        }
        return null;
    }


    public StatusResponse.Status logIn(User xxx){

        return null;
    }



    public StatusResponse.Status logOut(User xxx){

        return null;
    }

}
