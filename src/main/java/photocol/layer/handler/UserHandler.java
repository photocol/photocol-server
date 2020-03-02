package photocol.layer.handler;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import photocol.definitions.request.EndpointRequestModel.*;
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
        StatusResponse status;

        if(req.session().attribute("uid")!=null)
            return new StatusResponse(STATUS_LOGGED_IN);

        try {
            SignupRequest signupRequest = gson.fromJson(req.body(), SignupRequest.class);
            if(signupRequest==null || !signupRequest.isValid())
                throw new JsonParseException("Invalid signup request");

            if((status=userService.signUp(signupRequest.toServiceType())).status() != STATUS_OK)
                return status;

            req.session().invalidate();
            req.session(true).attribute("uid", status.payload());

            // TODO: remove; for debugging
            System.out.printf("UID %d signed up.%n", status.payload());

            return new StatusResponse(STATUS_OK);
        } catch(JsonParseException e) {
            res.status(400);
            return new StatusResponse(STATUS_HTTP_ERROR);
        }
    }

    public StatusResponse logIn(Request req, Response res) {
        res.type("application/json");

        StatusResponse status;
        if(req.session().attribute("uid")!=null)
            return new StatusResponse(STATUS_LOGGED_IN);

        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            if(loginRequest==null || !loginRequest.isValid())
                throw new JsonParseException("Invalid signup request");

            if((status=userService.logIn(loginRequest.toServiceType())).status() != STATUS_OK)
                return status;

            // TODO: remove; for debugging
            System.out.printf("UID %d logged in.%n", status.payload());

            req.session().invalidate();
            req.session(true).attribute("uid", status.payload());
            return new StatusResponse(STATUS_OK);
        } catch (JsonParseException e) {
            res.status(400);
            return new StatusResponse(STATUS_HTTP_ERROR);
        }
    }

    public StatusResponse logOut(Request req, Response res) {
        res.type("application/json");
        if(req.session().attribute("uid")==null)
            return new StatusResponse(STATUS_NOT_LOGGED_IN);

        // TODO: remove; for debugging
        System.out.printf("UID %d logged out.%n", req.session().attribute("uid"));

        req.session().invalidate();
        return new StatusResponse(STATUS_OK);
    }

    public StatusResponse<Boolean> userDetails(Request req, Response res) {
        res.type("application/json");

        if(req.session().attribute("uid")==null)
            return new StatusResponse(STATUS_NOT_LOGGED_IN);

        return new StatusResponse<>(STATUS_OK, req.session().attribute("uid")!=null);
    }

}
