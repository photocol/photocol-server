/* Endpoints and top-level handlers for the Photocol app */

package photocol;

import com.google.gson.Gson;
import photocol.layer.handler.CollectionHandler;
import photocol.layer.handler.PhotoHandler;
import photocol.layer.handler.UserHandler;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Endpoints {

    public Endpoints(UserHandler userHandler, CollectionHandler collectionHandler, PhotoHandler photoHandler,
                     Gson gson) {

        // CORS configuration middleware (all routes)
        Spark.before(this::setupCors);

        // TODO: run authentication middleware to reduce redundancy in handlers

        // login endpoints
        Spark.post("/signup", userHandler::signUp, gson::toJson);
        Spark.post("/login", userHandler::logIn, gson::toJson);
        Spark.get("/logout", userHandler::logOut, gson::toJson);
        Spark.get("/userdetails", userHandler::userDetails, gson::toJson);

        Spark.get("/userphotos", photoHandler::getUserPhotos, gson::toJson);
        Spark.get("/image/:imageuri", photoHandler::permalink);
        Spark.put("/image/:imageuri/upload", photoHandler::upload, gson::toJson);
        Spark.post("/image/:imageuri/update", photoHandler::update, gson::toJson);

        Spark.get("/usercollections", collectionHandler::getUserCollections, gson::toJson);
        Spark.post("/collection/new", collectionHandler::createCollection, gson::toJson);
        Spark.get("/collection/:username/:collectionuri", collectionHandler::getCollection, gson::toJson);
        Spark.post("/collection/:username/:collectionuri/update", collectionHandler::updateCollection, gson::toJson);
        Spark.post("/collection/:username/:collectionuri/addphoto", collectionHandler::addPhoto, gson::toJson);
    }

    // CORS middleware
    private void setupCors(Request req, Response res) throws Exception {
        // passthrough on image endpoint
        if(req.uri().startsWith("/image/") && req.requestMethod().equals("GET"))
            return;

        // FIXME: for now, only allowing requests from localhost
        // TODO: how to actually verify origin?
        String origin = req.headers("Origin");
        if(origin==null || !origin.startsWith("http://localhost")) {
            Spark.halt(401);
        }
        res.header("Access-Control-Allow-Origin", origin);
        res.header("Access-Control-Allow-Credentials", "true");
        res.header("Vary", "Origin");

        //CORS requires a preflight request (for PUT only???) and set some options
        //so we set them here and sent an HTTP OK
        if(req.requestMethod().equals("OPTIONS")) {
            res.header("Access-Control-Allow-Headers", "content-type");
            res.header("Access-Control-Allow-Methods", "PUT");
            Spark.halt(200);
        }
    }
}
