package photocol.layer.handler;

import com.google.gson.Gson;
import photocol.definitions.request.SignupRequest;
import photocol.definitions.response.StatusResponse;
import photocol.layer.service.UserService;
import spark.Request;
import spark.Response;
import static photocol.definitions.response.StatusResponse.Status.*;

public class UserHandler {

    private UserService userService;
    private Gson gson;
    public UserHandler(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }

    public StatusResponse signUp(Request req, Response res) {
        res.type("application/json");
        if(req.session().attribute("user")!=null)
            return new StatusResponse(STATUS_LOGGED_IN);

        SignupRequest signupRequest = gson.fromJson(req.body(),SignupRequest.class);
        if(!signupRequest.isValid()) {
            res.status(400);
            return null;
        }

        return userService.signUp(signupRequest.toUser());
    }

}
