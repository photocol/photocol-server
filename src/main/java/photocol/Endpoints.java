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
        Spark.post("/signup", userHandler::signUp, gson::toJson);
        Spark.post("/login", userHandler::logIn, gson::toJson);
        Spark.get("/logout", userHandler::logOut, gson::toJson);
        Spark.get("/userdetails", userHandler::userDetails, gson::toJson);

        Spark.get("/userphotos", photoHandler::getUserPhotos, gson::toJson);
        Spark.get("/image/:imageuri", photoHandler::permalink);
        Spark.put("/image/:imageuri/upload", photoHandler::upload, gson::toJson);
        Spark.post("/image/:imageuri/update", photoHandler::update, gson::toJson);

        Spark.post("/collection/new", collectionHandler::createCollection, gson::toJson);
        Spark.post("/collection/:collectionname/update", collectionHandler::createCollection, gson::toJson);
        Spark.post("/collection/:collectionname/photos", collectionHandler::createCollection, gson::toJson);
    }

    // for testing only; will throw an exception if called
    private String dummyHandler(Request req, Response res) {
        throw new RuntimeException();
    }
}
