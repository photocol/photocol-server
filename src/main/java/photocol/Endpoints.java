/* Endpoints and top-level handlers for the Photocol app */

package photocol;

import photocol.layer.handler.CollectionHandler;
import photocol.layer.handler.PhotoHandler;
import photocol.layer.handler.UserHandler;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Endpoints {

    public Endpoints(UserHandler userHandler, CollectionHandler collectionHandler, PhotoHandler photoHandler) {

        // login endpoints
        Spark.get("/signup", userHandler::signUp);
        Spark.get("/login", userHandler::logIn);
        Spark.get("/logout", userHandler::logOut);
        Spark.get("/userdetails", userHandler::userDetails);

        // get user/collection/image data
        Spark.get("/user/:username", this::dummyHandler);
        Spark.get("/collection/:username/:collection", this::dummyHandler);
        Spark.get("/images/:imageuri", photoHandler::permalink);

        // create/edit/delete user/collection/image data
        Spark.put("/collection/:collection", this::dummyHandler);
        Spark.put("/collection/:collection/:image", this::dummyHandler);
        Spark.post("/user/edit", this::dummyHandler);
        Spark.post("/collection/:collection/edit", this::dummyHandler);
        Spark.post("/collection/:collection/:image/edit", this::dummyHandler);

        // catch-all; 404
        Spark.get("/*", this::handle404);
        Spark.post("/*", this::handle404);
        Spark.put("/*", this::handle404);
        Spark.delete("/*", this::handle404);
    }

    private String handle404(Request req, Response res) {
        res.status(404);
        return "";
    }

    // for testing only; will throw an exception if called
    private String dummyHandler(Request req, Response res) {
        throw new RuntimeException();
    }
}
