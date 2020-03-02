/* Endpoints and top-level handlers for the Photocol app */

package photocol;

import com.google.gson.Gson;
import photocol.layer.handler.CollectionHandler;
import photocol.layer.handler.PhotoHandler;
import photocol.layer.handler.UserHandler;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Endpoints {

    public Endpoints(UserHandler userHandler, CollectionHandler collectionHandler, PhotoHandler photoHandler,
                     Gson gson) {

        // login endpoints
        Spark.get("/signup", userHandler::signUp, gson::toJson);
        Spark.get("/login", userHandler::logIn, gson::toJson);
        Spark.get("/logout", userHandler::logOut, gson::toJson);
        Spark.get("/userdetails", userHandler::userDetails, gson::toJson);

        Spark.get("/image/:imageuri", photoHandler::permalink);
        Spark.put("/image/:imageuri/upload", photoHandler::upload, gson::toJson);
        Spark.put("/image/:imageuri/update", photoHandler::update, gson::toJson);
    }

    // for testing only; will throw an exception if called
    private String dummyHandler(Request req, Response res) {
        throw new RuntimeException();
    }
}
