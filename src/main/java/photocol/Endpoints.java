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

        Spark.get("/image/:imageuri", photoHandler::permalink);
        Spark.put("/image/upload/:imageuri", photoHandler::upload);
    }

    // for testing only; will throw an exception if called
    private String dummyHandler(Request req, Response res) {
        throw new RuntimeException();
    }
}
