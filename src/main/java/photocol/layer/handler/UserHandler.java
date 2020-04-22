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
        if(req.session().attribute("uid")!=null)
            throw new HttpMessageException(400, LOGGED_IN);

        try {
            SignupRequest signupRequest = gson.fromJson(req.body(), SignupRequest.class);
            if(signupRequest==null || !signupRequest.isValid())
                throw new JsonParseException("Invalid signup request");

            int uid = userService.signUp(signupRequest.toServiceType());

            req.session().invalidate();
            req.session(true).attribute("uid", uid);
            req.session().attribute("username", signupRequest.toServiceType().username);

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
        if(req.session().attribute("uid")!=null)
            throw new HttpMessageException(401, HttpMessageException.Error.LOGGED_IN);

        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            if(loginRequest==null || !loginRequest.isValid())
                throw new JsonParseException("Invalid signup request");

            int uid = userService.logIn(loginRequest.toServiceType());

            req.session().invalidate();
            req.session(true).attribute("uid", uid);
            req.session().attribute("username", loginRequest.toServiceType().username);
            return true;
        } catch (JsonParseException e) {
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
        return req.session().attribute("username");
    }

}
