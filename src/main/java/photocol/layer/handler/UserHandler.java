package photocol.layer.handler;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import photocol.definitions.exception.HttpMessageException;
import photocol.definitions.request.EndpointRequestModel.*;
import photocol.layer.service.UserService;
import spark.Request;
import spark.Response;

import static photocol.definitions.exception.HttpMessageException.Error.*;

public class UserHandler {

    private UserService userService;
    private Gson gson;
    public UserHandler(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }

    /**
     * Sign up a user.
     * @param req   spark request object
     * @param res   spark response object
     * @return      true on success
     * @throws HttpMessageException on error
     */
    public boolean signUp(Request req, Response res) throws HttpMessageException {
        // TODO: remove
//        StatusResponse status;

        if(req.session().attribute("uid")!=null)
            throw new HttpMessageException(400, LOGGED_IN);
            //TODO: remove
//            return new StatusResponse(STATUS_LOGGED_IN);

        try {
            SignupRequest signupRequest = gson.fromJson(req.body(), SignupRequest.class);
            if(signupRequest==null || !signupRequest.isValid())
                throw new JsonParseException("Invalid signup request");

            // TODO: remove
//            if((status=userService.signUp(signupRequest.toServiceType())).status() != STATUS_OK)
//                return status;

            int uid = userService.signUp(signupRequest.toServiceType());

            req.session().invalidate();
            req.session(true).attribute("uid", uid);

            // TODO: remove; for debugging
            System.out.printf("UID %d signed up.%n", uid);

            return true;
        } catch(JsonParseException e) {
            throw new HttpMessageException(400, INPUT_FORMAT_ERROR);
        }
    }

    /**
     * Login a user
     * @param req   spark request object
     * @param res   spark response object
     * @return      true on success
     * @throws HttpMessageException on error
     */
    public boolean logIn(Request req, Response res) throws HttpMessageException {
        // TODO: remove
//        StatusResponse status;
        if(req.session().attribute("uid")!=null)
            // TODO: remove
//            return new StatusResponse(STATUS_LOGGED_IN);
            throw new HttpMessageException(401, HttpMessageException.Error.LOGGED_IN);

        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            if(loginRequest==null || !loginRequest.isValid())
                throw new JsonParseException("Invalid signup request");

//            if((status=userService.logIn(loginRequest.toServiceType())).status() != STATUS_OK)
//                return status;
            int uid = userService.logIn(loginRequest.toServiceType());

            // TODO: remove; for debugging
            System.out.printf("UID %d logged in.%n", uid);

            req.session().invalidate();
            req.session(true).attribute("uid", uid);
            req.session().attribute("username", loginRequest.toServiceType().username);
            return true;
        } catch (JsonParseException e) {
            // TODO: remove
//            res.status(400);
//            return new StatusResponse(STATUS_HTTP_ERROR);
            throw new HttpMessageException(400, INPUT_FORMAT_ERROR);
        }
    }

    /**
     * Clear user login session.
     * @param req   spark request object
     * @param res   spark response object
     * @return      true on success
     */
    public boolean logOut(Request req, Response res) {
        // TODO: remove; for debugging
        System.out.printf("UID %d logged out.%n", (int) req.session().attribute("uid"));

        req.session().invalidate();
        return true;
    }

    /**
     * Check if logged in and return user profile details.
     * @param req   spark request object
     * @param res   spark response object
     * @return      username if logged in
     */
    public String userDetails(Request req, Response res) {
        res.type("application/json");

        // TODO: remove
//        return new StatusResponse<>(STATUS_OK, req.session().attribute("username"));
        return req.session().attribute("username");
    }

}
