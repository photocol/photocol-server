package photocol.layer.store;

import com.google.gson.Gson;
import photocol.definitions.User;
import photocol.definitions.request.SignupRequest;
import photocol.definitions.response.SuccessFailureResponse;
import spark.Request;
import spark.Response;
import java.util.HashMap;

public class UserStore {

    private Gson gson = new Gson();

    // temporary; until db is set up
    private HashMap<String, User> users = new HashMap<>();

    public String signUp(Request req, Response res) {
        res.type("application/json");
        if(req.contentType() == null || !req.contentType().equals("application/json")) {
            res.status(400);
            return "";
        }

        SignupRequest signupRequest = gson.fromJson(req.body(), SignupRequest.class);
        if(signupRequest == null || !signupRequest.isValid()) {
            res.status(400);
            return "";
        }

        User newUser = new User(signupRequest.username, signupRequest.email, signupRequest.passwordHash);
        users.put(signupRequest.username, newUser);
        req.session().attribute("user", newUser);
        return gson.toJson(signupRequest);
    }

    public String logIn(Request req, Response res) {
        res.type("application/json");
        if(req.contentType() == null || !req.contentType().equals("application/json")) {
            res.status(400);
            return "";
        }

        SignupRequest signupRequest = gson.fromJson(req.body(), SignupRequest.class);
        if(signupRequest == null || !signupRequest.isValid()) {
            res.status(400);
            return "";
        }

        User user = users.get(signupRequest.username);
        if(user==null || !user.validatePasswordHash(signupRequest.passwordHash))
            return gson.toJson(new SuccessFailureResponse(false));
        req.session().attribute("user", user);
        return gson.toJson(new SuccessFailureResponse(true, user));
    }

    public String logOut(Request req, Response res) {
        req.session().invalidate();
        return gson.toJson(new SuccessFailureResponse(true));
    }

    public String getLoggedInUser(Request req, Response res) {
        User currentUser = req.session().attribute("user");
        if(currentUser==null)
            return gson.toJson(new SuccessFailureResponse(false));
        return gson.toJson(new SuccessFailureResponse(true, currentUser.getUsername()));
    }
}
